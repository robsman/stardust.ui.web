package org.eclipse.stardust.ui.web.reporting.core;

import java.io.IOException;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.reporting.common.JsonMarshaller;
import org.eclipse.stardust.ui.web.reporting.common.RestUtil;
import org.eclipse.stardust.ui.web.reporting.common.mapping.reponse.RecordSetDataBuilder;
import org.eclipse.stardust.ui.web.reporting.common.mapping.reponse.SeriesDataBuilder;
import org.eclipse.stardust.ui.web.reporting.common.mapping.reponse.ValuesArray;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.*;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidationHelper;
import org.eclipse.stardust.ui.web.reporting.core.Constants.AiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.Constants.DataSetType;
import org.eclipse.stardust.ui.web.reporting.core.Constants.FactField;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.Constants.QueryType;
import org.eclipse.stardust.ui.web.reporting.core.Constants.TimeUnit;
import org.eclipse.stardust.ui.web.reporting.core.aggregation.AbstractGroupKey;
import org.eclipse.stardust.ui.web.reporting.core.aggregation.ValueAggregator;
import org.eclipse.stardust.ui.web.reporting.core.aggregation.ValueGroup;
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

   public JsonObject getReportData(JsonObject reportJson, Collection<ReportParameter> parameters)
   {
      ReportParameter[] reportParameterArray
         = parameters.toArray(new ReportParameter[parameters.size()]);

      return getReportData(reportJson, reportParameterArray);
   }

   public JsonObject getReportData(JsonObject reportJson, ReportParameter...parameters)
   {
      Map<String, ReportParameter> parameterMap = new HashMap<String, ReportParameter>();
      if(parameters != null)
      {
         for(ReportParameter rp: parameters)
         {
            parameterMap.put(rp.getId(), rp);
         }
      }

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
      QueryBuilder queryBuilder = new QueryBuilder(parameterMap);

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
   //TODO: Performance optimization :
   //grouping / building response for each group locally instead of building big datastructure.
   //try to implement via in memory database(h2) - was trying this but run out of time
   private <T> JsonObject generateResponse(ReportDataSet dataSet,
         AbstractColumnHandlerRegistry<T, ? extends Query> handlerRegistry,
         AbstractQueryResult<T> results)
   {
      DataSetType dataSetType = DataSetType.parse(dataSet.getType());
      QueryType queryType = QueryType.parse(dataSet.getPrimaryObject());
      List<GroupColumn> groupColumns = getGroupColumns(dataSetType, dataSet);

      //aggregate the values base on the(explicit & implicit) grouping criteria
      ValueAggregator<T> aggregator = new ValueAggregator<T>(queryService, results, groupColumns, handlerRegistry);
      Map<AbstractGroupKey<T>, ValueGroup<T>> aggregateResults = aggregator.aggregate();

      switch (dataSetType)
      {
         case SERIESGROUP:
            String seriesKey = queryType.getId();

            TimeUnit factDurationUnit = getTimeUnit(dataSet.getFactDurationUnit());
            RequestColumn factColumn = new RequestColumn(dataSet.getFact(), factDurationUnit);

            SeriesDataBuilder sdb = new SeriesDataBuilder();
            //get the results and also apply the aggregate functions
            for(AbstractGroupKey<T> groupKey: aggregateResults.keySet())
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
            List<RequestColumn> requestColumns = getRequestColumns(dataSetType, dataSet);

            RecordSetDataBuilder responseBuilder
               = new RecordSetDataBuilder(requestColumns);
            HandlerContext ctx = new HandlerContext(queryService, results.size());
            for(AbstractGroupKey<T> groupKey: aggregateResults.keySet())
            {
               T groupEntitiy = groupKey.getCriteriaEntitiy();
               for (RequestColumn requestedColumn : requestColumns)
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
         return new GroupByColumn(groupBy);
      }

      return null;
   }

   //hackaround to determine if the column is a descriptor - the
   //meta inforomation if a column is a descriptor should be passed from ui
   //for now - assume that a column is a descriptor column if it cannot be mapped
   private boolean isDescriptorColumn(RequestColumn column)
   {
      String id = column.getId();
      for(PiDimensionField field: Constants.PiDimensionField.values())
      {
         if(field.getId().equals(id))
         {
            return false;
         }
      }

      for(AiDimensionField field: Constants.AiDimensionField.values())
      {
         if(field.getId().equals(id))
         {
            return false;
         }
      }

      for(FactField field: Constants.FactField.values())
      {
         if(field.getId().equals(id))
         {
            return false;
         }
      }

      return true;
   }

   private ReportComputedColumn getComputedColumnDefinition(String id, ReportDataSet dataSet)
   {
      List<ReportComputedColumn> computedColumnDefinitions = dataSet.getComputedColumns();
      if(computedColumnDefinitions != null)
      {
         for(ReportComputedColumn computedColumnDefinition: computedColumnDefinitions)
         {
            if(id.equals(computedColumnDefinition.getId()))
            {
               return computedColumnDefinition;
            }
         }
      }

      return null;
   }

   private void setMetaInformation(List<? extends RequestColumn> columns, ReportDataSet dataSet)
   {
      //set meta information - ideally - all these values would already come via the column
      //definitions - reality check - this will never happen
      for(RequestColumn rc: columns)
      {
         //a column cannot be computed / descriptor at the same time
         ReportComputedColumn computedColumnDef = getComputedColumnDefinition(rc.getId(), dataSet);
         if(computedColumnDef != null)
         {
            rc.setComputed(true);
            rc.setComputationFormula(computedColumnDef.getFormula());
         }
         else
         {
            rc.setDescriptor(isDescriptorColumn(rc));
         }
      }
   }

   private List<GroupColumn> getGroupColumns(DataSetType dataSetType, ReportDataSet dataSet)
   {
      List<GroupColumn> groupColumns = new ArrayList<GroupColumn>();
      if(dataSetType == DataSetType.SERIESGROUP)
      {
         //implicit grouping information - according to ui team: dimension is always grouped
         //but will not result in an own series - thats why you need two Grouping classes - to distinguish
         Interval dimensionInterval = getDimensionInterval(dataSet);
         GroupColumn dimensionColumn = new GroupColumn(dataSet.getFirstDimension(), dimensionInterval);
         groupColumns.add(dimensionColumn);
      }

      //explicit grouping information
      GroupByColumn groupByColumn = getGroupByColumn(dataSet);
      if(groupByColumn != null)
      {
         groupColumns.add(groupByColumn);
      }

      //set meta information (descriptors/computed columns, etc)
      setMetaInformation(groupColumns, dataSet);
      return groupColumns;
   }

   private List<RequestColumn> getRequestColumns(DataSetType dataSetType, ReportDataSet dataSet)
   {
      List<RequestColumn> requestColumns
         = new ArrayList<RequestColumn>();
      for (String s : dataSet.getColumns())
      {
         RequestColumn requestColumn = new RequestColumn(s);
         requestColumns.add(requestColumn);
      }

      //set meta information (descriptors/computed columns, etc)
      setMetaInformation(requestColumns, dataSet);
      return requestColumns;
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
}
