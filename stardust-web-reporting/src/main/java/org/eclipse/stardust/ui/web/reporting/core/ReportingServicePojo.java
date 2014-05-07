package org.eclipse.stardust.ui.web.reporting.core;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import javax.script.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.reporting.common.JsonMarshaller;
import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.common.RestUtil;
import org.eclipse.stardust.ui.web.reporting.common.mapping.reponse.RecordSetDataBuilder;
import org.eclipse.stardust.ui.web.reporting.common.mapping.reponse.SeriesDataBuilder;
import org.eclipse.stardust.ui.web.reporting.common.mapping.reponse.ValuesArray;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.*;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidationHelper;
import org.eclipse.stardust.ui.web.reporting.core.Constants.DataSetType;
import org.eclipse.stardust.ui.web.reporting.core.Constants.FactField;
import org.eclipse.stardust.ui.web.reporting.core.Constants.QueryType;
import org.eclipse.stardust.ui.web.reporting.core.Constants.TimeUnit;
import org.eclipse.stardust.ui.web.reporting.core.aggregation.ValueAggregator;
import org.eclipse.stardust.ui.web.reporting.core.aggregation.ValueGroup;
import org.eclipse.stardust.ui.web.reporting.core.aggregation.ValueGroupKey;
import org.eclipse.stardust.ui.web.reporting.core.aggregation.functions.*;
import org.eclipse.stardust.ui.web.reporting.core.handler.*;
import org.eclipse.stardust.ui.web.reporting.core.handler.activity.AiColumnHandlerRegistry;
import org.eclipse.stardust.ui.web.reporting.core.handler.process.PiColumnHandlerRegistry;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;

public class ReportingServicePojo
{
   private static final Logger trace = LogManager.getLogger(ReportingServicePojo.class);

   private ServiceFactory serviceFactory;

   private QueryService queryService;

   private JsonMarshaller jsonMarshaller;

   final String GROUPING_NOT_SELECTED = "None";

   public ReportingServicePojo(ServiceFactory serviceFactory)
   {
      this.serviceFactory = serviceFactory;
      this.queryService = this.serviceFactory.getQueryService();
      this.jsonMarshaller = new JsonMarshaller();
   }

   /**
    *
    * @return
    * @throws ParseException
    * @throws UnsupportedFilterException
    */
   public JsonObject getReportData(JsonObject reportJson)
         throws UnsupportedFilterException, ParseException
   {

      // workaround until the method signature changes and this method here just receives
      // the raw json string
      JsonMarshaller jm = new JsonMarshaller();
      String reportDefJson = jm.gson().toJson(reportJson);
      ReportDefinition reportDefinition = jm.gson().fromJson(reportDefJson,
            ReportDefinition.class);
      // validate only if enabled - the helper will take care of it
      ValidationHelper.validate(reportDefinition);

      ReportDataSet dataSet = reportDefinition.getDataSet();
      QueryType queryType = QueryType.parse(dataSet.getPrimaryObject());
      QueryBuilder queryBuilder = new QueryBuilder();

      final long queryStart;
      final long queryEnd;

      final long responseStart;
      final long responseEnd;
      JsonObject response;
      switch (queryType)
      {
         case ACTIVITY_INSTANCE:
            queryStart = System.currentTimeMillis();
            ActivityInstanceQuery aiQuery = queryBuilder
                  .buildActivityInstanceQuery(dataSet);
            ActivityInstances allActivityInstances = queryService
                  .getAllActivityInstances(aiQuery);
            queryEnd = System.currentTimeMillis();

            responseStart = System.currentTimeMillis();
            response = generateResponse(dataSet, new AiColumnHandlerRegistry(),
                  allActivityInstances);
            responseEnd = System.currentTimeMillis();
            break;
         case PROCESS_INSTANCE:
            queryStart = System.currentTimeMillis();
            ProcessInstanceQuery piQuery = queryBuilder
                  .buildProcessInstanceQuery(dataSet);
            ProcessInstances allProcessInstances = queryService
                  .getAllProcessInstances(piQuery);
            queryEnd = System.currentTimeMillis();

            responseStart = System.currentTimeMillis();
            response = generateResponse(dataSet, new PiColumnHandlerRegistry(),
                  allProcessInstances);
            responseEnd = System.currentTimeMillis();
            break;
         default:
            throw new RuntimeException("Unsupported QueryType: " + queryType);
      }

      if(trace.isDebugEnabled())
      {
         long queryDuration = queryEnd - queryStart;
         long responseDuration = responseEnd - responseStart;
         trace.debug("Executing query took :"+queryDuration);
         trace.debug("Generating json took :"+responseDuration);
      }

      return response;
   }

   private TimeUnit getTimeUnit(String unit)
   {
      if(StringUtils.isNotEmpty(unit))
      {
         return TimeUnit.parse(unit);
      }

      return null;
   }

   private Interval getDimensionInterval(ReportDataSet dataSet)
   {
      Long unitValue
         = dataSet.getFirstDimensionCumulationIntervalCount();

      if(unitValue != null)
      {
         TimeUnit unit = getTimeUnit(dataSet.getFirstDimensionCumulationIntervalUnit());
         return new Interval(unit, unitValue);
      }

      return null;
   }

   private GroupByColumn getGroupByColumn(ReportDataSet dataSet)
   {
      String groupBy = dataSet.getGroupBy();
      if(StringUtils.isNotEmpty(groupBy)
            && !GROUPING_NOT_SELECTED.equals(groupBy))
      {
         return new GroupByColumn(groupBy, null);
      }

      return null;
   }

   //TODO: Performance optimization :
   //grouping / building response for each group locally instead of building big datastructure.
   //try to implement via in memory database(h2) - was trying this but run out of time
   private <T> JsonObject generateResponse(ReportDataSet dataSet,
         AbstractColumnHandlerRegistry<T, ? extends Query> handlerRegistry,
         AbstractQueryResult<T> results)
   {
      DataSetType dataSetType = DataSetType.parse(dataSet.getType());
      QueryType queryType = QueryType.parse(dataSet.getPrimaryObject());
      String seriesKey = queryType.getId();

      List<RequestColumn> requestedColumns = new ArrayList<RequestColumn>();
      List<GroupColumn> groupColumns = new ArrayList<GroupColumn>();

      TimeUnit factDurationUnit = getTimeUnit(dataSet.getFactDurationUnit());
      RequestColumn factColumn = new RequestColumn(dataSet.getFact(), factDurationUnit);

      Interval dimensionInterval = getDimensionInterval(dataSet);
      String dimensionId = dataSet.getFirstDimension();

      //each distinct value for group by will result in an own series
      //where the distinct value is the key to the series
      GroupByColumn groupByColumn = getGroupByColumn(dataSet);
      if(groupByColumn != null)
      {
         groupColumns.add(groupByColumn);
      }

      //according to ui team - dimension is always grouped but will not result in an own series
      //it should do result, to be consequent and to be able to treat them generic
      groupColumns.add(new GroupColumn(dimensionId, dimensionInterval));

      //aggregate the values base on the grouping criteria
      ValueAggregator<T> aggregator
         = new ValueAggregator<T>(queryService, results, groupColumns, handlerRegistry);
      Map<ValueGroupKey<T>, ValueGroup<T>> aggregateResults
         = aggregator.aggregate();

      switch (dataSetType)
      {
         case SERIESGROUP:
            SeriesDataBuilder sdb = new SeriesDataBuilder();
            //get the results and also apply the aggregate functions
            for(ValueGroupKey<T> groupKey: aggregateResults.keySet())
            {
               T groupEntitiy
                  = groupKey.getCriteriaEntitiy();
               ValueGroup<T> group = aggregateResults.get(groupKey);
               ValuesArray seriesValues = new ValuesArray();

               for(GroupColumn gc: groupColumns)
               {
                  HandlerContext providerContext = new HandlerContext(queryService, 0);
                  providerContext.setColumn(gc);

                  IColumnHandler< ? , T, ? extends Query> columnHandler
                     = handlerRegistry.getColumnHandler(gc);
                  Object seriesValue
                     = columnHandler.provideObjectValue(providerContext, groupEntitiy);
                  //todo return a wrapepr object and let JsonUtil#addPrimitiveObjectToJsonObject
                  //do the job of formatting it
                  if(seriesValue instanceof Date && gc.getInterval() != null)
                  {
                     seriesValue = ReportingUtil.formatDate((Date)seriesValue, gc.getInterval().getUnit());
                  }

                  if(gc instanceof GroupByColumn)
                  {
                     seriesKey = (seriesValue != null)? seriesValue.toString() : "NULL";
                  }
                  else
                  {
                     //sort by dimension
                     seriesValues.setSortIndex(0);
                     seriesValues.addValue(seriesValue);
                  }
               }

               //Max, Min, Avg, Std Dev and Count
               CountFunction<T> countFunction = new CountFunction<T>();
               if(!FactField.COUNT.getId().equals(factColumn.getId()))
               {
                  IFactValueProvider<T> factProvider
                     = handlerRegistry.getFactValueProvider(factColumn);

                  //Max, Min, Avg, Std Dev and Count add it in that order - the client expects it like that
                  MaxFunction<T> maxFunction = new MaxFunction<T>(queryService, factColumn, factProvider);
                  Number max = maxFunction.apply(group);
                  seriesValues.addValue(max);

                  MinFunction<T> minFunction = new MinFunction<T>(queryService, factColumn, factProvider);
                  Number min = minFunction.apply(group);
                  seriesValues.addValue(min);

                  AvgFunction<T> avgFunction = new AvgFunction<T>(queryService, factColumn, factProvider);
                  Number avg = avgFunction.apply(group);
                  seriesValues.addValue(avg);

                  StdDevFunction<T> stdDevFunction = new StdDevFunction<T>(queryService, factColumn, factProvider);
                  Number stdDev = stdDevFunction.apply(group);
                  seriesValues.addValue(stdDev);
               }
               long count = countFunction.apply(group).longValue();
               seriesValues.addValue(count);

               sdb.add(seriesKey, seriesValues);
            }
            return sdb.getResult();
         case RECORDSET:
            for (String s : dataSet.getColumns())
            {
               requestedColumns.add(new RequestColumn(s));
            }

            RecordSetDataBuilder responseBuilder
               = new RecordSetDataBuilder(requestedColumns);
            HandlerContext ctx = new HandlerContext(queryService, results.size());
            for(ValueGroupKey<T> groupKey: aggregateResults.keySet())
            {
               T groupEntitiy = groupKey.getCriteriaEntitiy();
               for (RequestColumn requestedColumn : requestedColumns)
               {
                  ctx.setColumn(requestedColumn);
                  IPropertyValueProvider< ? , T> mappingHandler = handlerRegistry
                        .getPropertyValueProvider(requestedColumn);
                  Object value = mappingHandler.provideObjectValue(ctx, groupEntitiy);
                  responseBuilder.addValue(value);
               }
               responseBuilder.nextRow();
            }
            return responseBuilder.getResult();
         default:
            throw new RuntimeException("Unsupported DataSetType: " + dataSetType);
      }
   }

   /**
    * Retrieves external join data via REST and creates a map with the join key as key and
    * a map with all external fields and their 'useAs' field names as keys and their
    * values as values.
    *
    * @param externalJoinJson
    * @return
    */
   public Map<String, Map<String, String>> retrieveExternalData(
         ReportExternalJoin externalJoin)
   {
      try
      {
         String restUri = externalJoin.getRestUri();
         String externalDataJsonResponse = RestUtil.performRestJsonCall(restUri);
         JsonArray externalDataJson = jsonMarshaller
               .readJsonObject(externalDataJsonResponse).get("list").getAsJsonArray();

         trace.info("External Data:");
         trace.info(externalDataJson.toString());

         Map<String, Map<String, String>> externalData = new HashMap<String, Map<String, String>>();

         List<ReportExternalJoinField> joinFields = externalJoin.getFields();
         for (int n = 0; n < externalDataJson.size(); n++)
         {
            JsonObject recordJson = externalDataJson.get(n).getAsJsonObject();
            Map<String, String> record = new HashMap<String, String>();

            for (ReportExternalJoinField joinField : joinFields)
            {
               String joinFieldId = joinField.getId();
               if (joinFieldId.equals(externalJoin.getExternalKey()))
               {
                  externalData.put(recordJson.get(joinFieldId).getAsString(), record);
               }

               record.put(joinField.getUseAs(), recordJson.get(joinFieldId).getAsString());
            }
         }

         trace.info("external data map");
         trace.info(externalData);
         return externalData;
      }
      catch (IOException e)
      {
         trace.error(e);

         throw new RuntimeException(e);
      }
   }

   @SuppressWarnings("unused")
   private void addComputedColumns(List<ReportComputedColumn> computedColumns,
         JsonObject jsonObject)
   {
      for (ReportComputedColumn computedColumn : computedColumns)
      {
         String columnId = computedColumn.getId();
         String columnFormula = computedColumn.getFormula();

         JsonUtil.addPrimitiveObjectToJsonObject(jsonObject, columnId,
               evaluateComputedColumn(jsonObject, columnFormula));
      }
   }

   /**
    *
    * @param input
    * @return
    */
   private static Object evaluateComputedColumn(JsonObject input, String expression)
   {
      try
      {
         ScriptEngineManager manager = new ScriptEngineManager();
         ScriptEngine engine = manager.getEngineByName("JavaScript");
         ScriptContext context = new SimpleScriptContext();
         Bindings scope = context.getBindings(ScriptContext.ENGINE_SCOPE);

         // Add column values to scope
         for (Map.Entry<String, JsonElement> entry : input.entrySet())
         {
            if (entry.getValue().isJsonPrimitive())
            {
               JsonPrimitive jsonPrimitive = (JsonPrimitive) entry.getValue();
               scope.put(entry.getKey(), JsonUtil.convertPrimitiveToJava(jsonPrimitive));
            }
         }

         // Execute script
         return engine.eval(expression, context);
      }
      catch (ScriptException e)
      {
         trace.error(e);

         // Any scripting error result in a non-calulated value
         return null;
      }
   }

   /**
    *
    * @return
    */
   public boolean isDiscreteDimension(String primaryObject, String dimension)
   {
      if (dimension.equals("startTimestamp") || dimension.equals("terminationTimestamp"))
      {
         return false;
      }

      return true;
   };
}
