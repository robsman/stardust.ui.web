package org.eclipse.stardust.ui.web.reporting.core;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.stardust.ui.web.reporting.common.mapping.reponse.RecordSetResponseDataBuilder;
import org.eclipse.stardust.ui.web.reporting.common.mapping.reponse.ReponseDataBuilder;
import org.eclipse.stardust.ui.web.reporting.common.mapping.reponse.SeriesResponseDataBuilder;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.*;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidationHelper;
import org.eclipse.stardust.ui.web.reporting.core.Constants.DataSetType;
import org.eclipse.stardust.ui.web.reporting.core.Constants.DurationUnit;
import org.eclipse.stardust.ui.web.reporting.core.Constants.QueryType;
import org.eclipse.stardust.ui.web.reporting.core.handler.AbstractColumnHandlerRegistry;
import org.eclipse.stardust.ui.web.reporting.core.handler.IMappingHandler;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.activity.AiColumnHandlerRegistry;
import org.eclipse.stardust.ui.web.reporting.core.handler.process.PiColumnHandlerRegistry;

public class ReportingServicePojo
{
   private static final Logger trace = LogManager.getLogger(ReportingServicePojo.class);

   private ServiceFactory serviceFactory;

   private QueryService queryService;

   private JsonMarshaller jsonMarshaller;

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


      switch (queryType)
      {
         case ACTIVITY_INSTANCE:

            ActivityInstanceQuery aiQuery = queryBuilder
                  .buildActivityInstanceQuery(dataSet);
            long start = System.currentTimeMillis();
            ActivityInstances allActivityInstances = queryService
                  .getAllActivityInstances(aiQuery);
            long end = System.currentTimeMillis();
            System.err.println("RSP: "+(end-start));

            return generateResponse(dataSet, new AiColumnHandlerRegistry(),
                  allActivityInstances);
         case PROCESS_INSTANCE:
            ProcessInstanceQuery piQuery = queryBuilder
                  .buildProcessInstanceQuery(dataSet);
            ProcessInstances allProcessInstances = queryService
                  .getAllProcessInstances(piQuery);
            return generateResponse(dataSet, new PiColumnHandlerRegistry(),
                  allProcessInstances);
         default:
            throw new RuntimeException("Unsupported QueryType: " + queryType);
      }




   }

   private <T> JsonObject generateResponse(ReportDataSet dataSet,
         AbstractColumnHandlerRegistry<T, ? extends Query> handlerRegistry,
         AbstractQueryResult<T> results)
   {
      DataSetType dataSetType = DataSetType.parse(dataSet.getType());
      List<RequestColumn> requestedColumns = new ArrayList<RequestColumn>();

      final ReponseDataBuilder responseBuilder;
      switch (dataSetType)
      {
         case SERIESGROUP:
            DurationUnit factDurationUnit = getDurationUnit(dataSet.getFactDurationUnit());
            RequestColumn factColumn = new RequestColumn(dataSet.getFact(),
                  factDurationUnit);

            DurationUnit dimensionDurationUnit = getDurationUnit(dataSet
                  .getFirstDimensionDurationUnit());
            RequestColumn dimensionColumn = new RequestColumn(
                  dataSet.getFirstDimension(), dimensionDurationUnit);

            requestedColumns.add(factColumn);
            requestedColumns.add(dimensionColumn);
            responseBuilder = new SeriesResponseDataBuilder(requestedColumns);
            break;
         case RECORDSET:
            for (String s : dataSet.getColumns())
            {
               requestedColumns.add(new RequestColumn(s, null));
            }
            responseBuilder = new RecordSetResponseDataBuilder(requestedColumns);
            break;
         default:
            throw new RuntimeException("Unsupported DataSetType: " + dataSetType);
      }

      HandlerContext ctx = new HandlerContext(queryService, results.size());
      for (T t : results)
      {
         responseBuilder.next();
         for (RequestColumn requestedColumn : requestedColumns)
         {
            IMappingHandler< ? , T> mappingHandler = handlerRegistry
                  .getMappingHandler(requestedColumn);
            DurationUnit durationUnit = requestedColumn.getDurationUnit();

            //set context data
            ctx.putContextData(HandlerContext.DURATION_UNIT_ID, durationUnit);
            Object value = mappingHandler.provideObjectValue(ctx, t);
            responseBuilder.addValue(requestedColumn, value);
         }
      }

      return responseBuilder.getResult();
   }

   private DurationUnit getDurationUnit(String unit)
   {
      if(StringUtils.isNotEmpty(unit))
      {
         return DurationUnit.parse(unit);
      }

      return null;
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
