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

   private DurationUnit getDurationUnit(String unit)
   {
      if(StringUtils.isNotEmpty(unit))
      {
         return DurationUnit.parse(unit);
      }

      return null;
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
      try
      {
         //workaround until the method signature changes and this method here just receives
         //the raw json string
         JsonMarshaller jm = new JsonMarshaller();
         String reportDefJson = jm.gson().toJson(reportJson);
         ReportDefinition reportDefinition = jm.gson().fromJson(reportDefJson,
               ReportDefinition.class);
         //validate only if enabled - the helper will take care of it
         ValidationHelper.validate(reportDefinition);

         ReportDataSet dataSet = reportDefinition.getDataSet();
         QueryType queryType = QueryType.parse(dataSet.getPrimaryObject());
         QueryBuilder queryBuilder = new QueryBuilder();

         switch(queryType)
         {
            case ACTIVITY_INSTANCE:
               ActivityInstanceQuery aiQuery
                  = queryBuilder.buildActivityInstanceQuery(dataSet);
               ActivityInstances allActivityInstances
                  = queryService.getAllActivityInstances(aiQuery);
               return generateResponse(dataSet, new AiColumnHandlerRegistry(), allActivityInstances);
            case PROCESS_INSTANCE:
               ProcessInstanceQuery piQuery
                  = queryBuilder.buildProcessInstanceQuery(dataSet);
               ProcessInstances allProcessInstances
                  = queryService.getAllProcessInstances(piQuery);
               return generateResponse(dataSet, new PiColumnHandlerRegistry(), allProcessInstances);
            default:
               throw new RuntimeException("Unsupported QueryType: "+queryType);
         }




//         final ReponseDataBuilder responseBuilder;
//         String rawDataSetType = dataSet.getType();
//         DataSetType dataSetType = DataSetType.parse(rawDataSetType);
//         switch(dataSetType)
//         {
//            case SERIESGROUP:
//               responseBuilder = new SeriesResponseDataBuilder(columnDefinitions);
//               break;
//            case RECORDSET:
//               responseBuilder = new RecordSetResponseDataBuilder(columnDefinitions);
//               break;
//            default:
//               throw new RuntimeException("Unsupported DataSetType: "+dataSetType);
//         }





//         String dataSetType = dataSet.getType();
//
//         String primaryObject = dataSet.getPrimaryObject();
//         String fact = dataSet.getFact();
//         String firstDimension = dataSet.getFirstDimension();
//         JsonObject parametersJson = reportDefinition.getParameters().getAsJsonObject();
//         List<ReportFilter> filters = dataSet.getFilters();
//         final String groupByCriterion = dataSet.getGroupBy();
//
//         JsonObject result = new JsonObject();
//         DateFormat dateFormat = DateFormat.getDateInstance();
//
//         // Obtain cumulation criteria
//         final long firstDimensionCumulationIntervalCount;
//         final String firstDimensionCumulationIntervalUnit;
//
//         if (dataSet.getFirstDimensionCumulationIntervalCount() != null)
//         {
//            firstDimensionCumulationIntervalCount = dataSet
//                  .getFirstDimensionCumulationIntervalCount();
//         }
//         else
//         {
//            firstDimensionCumulationIntervalCount = 1;
//         }
//
//         if (dataSet.getFirstDimensionCumulationIntervalUnit() != null)
//         {
//            firstDimensionCumulationIntervalUnit = dataSet
//                  .getFirstDimensionCumulationIntervalUnit();
//         }
//         else
//         {
//            firstDimensionCumulationIntervalUnit = "d";
//         }
//
//         long firstDimensionCumulationInterval = firstDimensionCumulationIntervalCount
//               * ReportingUtil.convertDurationUnit(firstDimensionCumulationIntervalUnit);
//
//         // Obtain external data
//
//         Map<String, Map<String, String>> externalData = null;
//         JsonObject externalJoinJson = null;
//         String internalKey = null;
//
//         // TODO: review questionable logic
//         if (dataSet.isJoinExternalData())
//         {
//            List<ReportExternalJoin> externalJoins = dataSet.getExternalJoins();
//            if (externalJoins != null && !externalJoins.isEmpty())
//            {
//               ReportExternalJoin externalJoin = externalJoins.get(0);
//               String interalKey = externalJoin.getInternalKey();
//               trace.info("Internal Key: " + internalKey);
//
//               externalData = retrieveExternalData(externalJoin);
//            }
//         }
//
//
//
//         // Obtain Audit Trail data
//         if (primaryObject.equals("processInstance"))
//         {
//            ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
//
//            addProcessInstanceQueryFilters(query, filters, parametersJson);
//
//            // Set order and restrictions
//
//            if (firstDimension != null)
//            {
//               // TODO The following code structure should be applicable
//               // for all duration dimensions, parameterized by dimension
//               // (first, second)
//
//               String firstDimensionValue = dataSet.getFirstDimensionValue();
//               List<String> firstDimensionValueList = dataSet
//                     .getFirstDimensionValueList();
//
//               if (firstDimension.equals("startTimestamp"))
//               {
//                  long fromTimestamp = 0; // Beginning of the era
//                  String firstDimensionFrom = dataSet.getFirstDimensionFrom();
//                  String firstDimensionTo = dataSet.getFirstDimensionTo();
//                  String firstDimensionDuration = dataSet.getFirstDimensionDuration();
//
//                  if (StringUtils.isNotEmpty(firstDimensionFrom))
//                  {
//                     if (parametersJson.has("startTimestamp")
//                           && parametersJson.get("startTimestamp").getAsJsonObject()
//                                 .has("from"))
//                     {
//                        query.where(ProcessInstanceQuery.START_TIME
//                              .greaterOrEqual(fromTimestamp = dateFormat.parse(
//                                    parametersJson.get("startTimestamp")
//                                          .getAsJsonObject().get("from").getAsString())
//                                    .getTime()));
//                     }
//                     else
//                     {
//                        query.where(ProcessInstanceQuery.START_TIME
//                              .greaterOrEqual(fromTimestamp = dateFormat.parse(
//                                    firstDimensionFrom).getTime()));
//                     }
//                  }
//
//                  // Distinguish between to/from and to/duration
//
//                  if (StringUtils.isNotEmpty(firstDimensionTo))
//                  {
//                     if (parametersJson.has("startTimestamp")
//                           && parametersJson.get("startTimestamp").getAsJsonObject()
//                                 .has("to"))
//                     {
//                        query.where(ProcessInstanceQuery.START_TIME
//                              .lessOrEqual(dateFormat.parse(
//                                    parametersJson.get("startTimestamp")
//                                          .getAsJsonObject().get("to").getAsString())
//                                    .getTime()));
//                     }
//                     else
//                     {
//                        query.where(ProcessInstanceQuery.START_TIME
//                              .lessOrEqual(dateFormat.parse(firstDimensionTo).getTime()));
//                     }
//                  }
//                  else if (StringUtils.isNotEmpty(firstDimensionDuration))
//                  {
//                     Long firstDimensionDurationCount = dataSet
//                           .getFirstDimensionDurationCount();
//                     String firstDimensionDurationUnit = dataSet
//                           .getFirstDimensionDurationUnit();
//
//                     if (parametersJson.has("startTimestamp")
//                           && parametersJson.get("startTimestamp").getAsJsonObject()
//                                 .has("durationValue"))
//                     {
//                        query.where(ProcessInstanceQuery.START_TIME
//                              .lessOrEqual(fromTimestamp
//                                    + parametersJson.get("startTimestamp")
//                                          .getAsJsonObject().get("durationCount")
//                                          .getAsLong()
//                                    * ReportingUtil.convertDurationUnit(parametersJson
//                                          .get("startTimestamp").getAsJsonObject()
//                                          .get("durationUnit").getAsString())));
//                     }
//                     else
//                     {
//                        query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(fromTimestamp
//                              + firstDimensionDurationCount
//                              + ReportingUtil
//                                    .convertDurationUnit(firstDimensionDurationUnit)));
//                     }
//                  }
//               }
//               else if (firstDimension.equals("terminationTimestamp"))
//               {
//                  // TODO Replicate above
//               }
//               else if (firstDimension.equals("processName"))
//               {
//                  query.orderBy(ProcessInstanceQuery.PROC_DEF_NAME);
//
//                  if (firstDimensionValueList != null
//                        && !firstDimensionValueList.isEmpty())
//                  {
//                     // query.where(ProcessInstanceQuery.PROC_DEF_NAME.equals(valueList.get(0).getAsString()));
//                  }
//               }
//               else if (firstDimension.equals("startingUserName"))
//               {
//                  query.orderBy(ProcessInstanceQuery.STARTING_USER_OID);
//
//                  if (firstDimensionValue != null)
//                  {
//
//                  }
//               }
//               else if (firstDimension.equals("state"))
//               {
//                  query.orderBy(ProcessInstanceQuery.STATE);
//
//                  if (firstDimensionValueList != null
//                        && !firstDimensionValueList.isEmpty())
//                  {
//
//                  }
//               }
//               else if (firstDimension.equals("priority"))
//               {
//                  query.orderBy(ProcessInstanceQuery.PRIORITY);
//
//                  if (firstDimensionValueList != null
//                        && !firstDimensionValueList.isEmpty())
//                  {
//
//                  }
//               }
//
//               // TODO Handle descriptor filters
//            }
//
//            // TODO Add second dimension filters
//
//            // TODO Decide whether descriptors are needed
//
//            query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
//
//            if (dataSetType.equals("recordSet"))
//            {
//               JsonArray series = new JsonArray();
//
//               result.add("recordSet", series);
//
//               Map<String, String> descriptorMap = null;
//
//               for (ProcessInstance processInstance : queryService
//                     .getAllProcessInstances(query))
//               {
//                  // Filter for unique descriptors
//
//                  if (descriptorMap == null)
//                  {
//                     descriptorMap = new HashMap<String, String>();
//
//                     for (String key : ((ProcessInstanceDetails) processInstance)
//                           .getDescriptors().keySet())
//                     {
//                        if (!descriptorMap.containsKey(key))
//                        {
//                           descriptorMap.put(key, key);
//                        }
//                     }
//                  }
//
//                  JsonObject processInstanceJson = new JsonObject();
//
//                  series.add(processInstanceJson);
//
//                  processInstanceJson.addProperty("processId",
//                        processInstance.getProcessID());
//                  processInstanceJson.addProperty("processName",
//                        processInstance.getProcessName());
//                  processInstanceJson.addProperty("startTimestamp", processInstance
//                        .getStartTime().getTime());
//
//                  if (processInstance.getTerminationTime() != null)
//                  {
//                     processInstanceJson.addProperty("terminationTimestamp",
//                           processInstance.getTerminationTime().getTime());
//                  }
//
//                  processInstanceJson.addProperty("state", processInstance.getState()
//                        .getName());
//                  processInstanceJson.addProperty("priority",
//                        processInstance.getPriority());
//
//                  // Map descriptors
//
//                  for (String key : descriptorMap.keySet())
//                  {
//                     JsonUtil.addPrimitiveObjectToJsonObject(processInstanceJson, key,
//                           ((ProcessInstanceDetails) processInstance)
//                                 .getDescriptorValue(key));
//                  }
//
//                  // Join external data
//
//                  if (internalKey != null && processInstanceJson.has(internalKey)
//                        && !processInstanceJson.get(internalKey).isJsonNull())
//                  {
//                     String internalKeValue = processInstanceJson.get(internalKey)
//                           .getAsString();
//
//                     trace.info("Internal Key Value: " + internalKeValue);
//
//                     Map<String, String> record = externalData.get(internalKeValue);
//
//                     trace.info("Record: " + record);
//
//                     if (record != null)
//                     {
//                        for (String key : record.keySet())
//                        {
//                           trace.info("Key: " + key);
//                           trace.info("Value: " + record.get(key));
//
//                           processInstanceJson.addProperty(key, record.get(key));
//                        }
//                     }
//                  }
//
//                  // Add computed columns
//                  List<ReportComputedColumn> computedColumns = dataSet
//                        .getComputedColumns();
//                  if (computedColumns != null)
//                  {
//                     addComputedColumns(computedColumns, processInstanceJson);
//                  }
//
//               }
//            }
//            else
//            {
//               TimestampSeriesCumulator cumulator = new TimestampSeriesCumulator(
//                     getValueProvider(primaryObject, fact), getValueProvider(
//                           primaryObject, firstDimension), isDiscreteDimension(
//                           primaryObject, firstDimension), !fact.equals("count"));
//
//               // Grouping criterion
//
//               if (groupByCriterion != null)
//               {
//                  cumulator.setGroupCriterionProvider(getValueProvider(primaryObject,
//                        groupByCriterion));
//               }
//
//               JsonArray groupIds = new JsonArray();
//
//               result.add("groupIds", groupIds);
//               result.add("seriesGroup", cumulator.createCumulatedSeriesGroup(
//                     (List) queryService.getAllProcessInstances(query),
//                     firstDimensionCumulationInterval, groupIds));
//            }
//         }
//         else if (primaryObject.equals("activityInstance"))
//         {
//            ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
//
//            addActivityInstanceQueryFilters(query, filters, parametersJson);
//
//            // Set order
//
//            if (firstDimension != null)
//            {
//               if (firstDimension.equals("activityName"))
//               {
//                  query.orderBy(ActivityInstanceQuery.ACTIVITY_NAME);
//               }
//               else if (firstDimension.equals("processName"))
//               {
//                  query.orderBy(ActivityInstanceQuery.PROC_DEF_NAME);
//               }
//               else if (firstDimension.equals("userPerformerName"))
//               {
//                  query.orderBy(ActivityInstanceQuery.CURRENT_USER_PERFORMER_OID);
//               }
//               else if (firstDimension.equals("participantPerformerName"))
//               {
//                  query.orderBy(ActivityInstanceQuery.CURRENT_PERFORMER_OID);
//               }
//               else if (firstDimension.equals("state"))
//               {
//                  query.orderBy(ActivityInstanceQuery.STATE);
//               }
//            }
//
//            if (dataSetType.equals("recordSet"))
//            {
//               JsonArray series = new JsonArray();
//
//               result.add("recordSet", series);
//
//               Map<String, String> descriptorMap = null;
//
//               for (ActivityInstance activityInstance : queryService
//                     .getAllActivityInstances(query))
//               {
//                  // Filter for unique descriptors
//
//                  if (descriptorMap == null)
//                  {
//                     descriptorMap = new HashMap<String, String>();
//
//                     for (String key : ((ProcessInstanceDetails) activityInstance
//                           .getProcessInstance()).getDescriptors().keySet())
//                     {
//                        if (!descriptorMap.containsKey(key))
//                        {
//                           descriptorMap.put(key, key);
//                        }
//                     }
//                  }
//
//                  JsonObject activityInstanceJson = new JsonObject();
//
//                  series.add(activityInstanceJson);
//
//                  activityInstanceJson.addProperty("activityId", activityInstance
//                        .getActivity().getId());
//                  activityInstanceJson.addProperty("activityName", activityInstance
//                        .getActivity().getName());
//                  activityInstanceJson.addProperty("processId", activityInstance
//                        .getProcessInstance().getProcessID());
//                  activityInstanceJson.addProperty("processName", activityInstance
//                        .getProcessInstance().getProcessName());
//                  activityInstanceJson.addProperty("startTimestamp", activityInstance
//                        .getStartTime().getTime());
//                  activityInstanceJson.addProperty("id", activityInstance.getActivity()
//                        .getId());
//                  activityInstanceJson.addProperty("state", activityInstance.getState()
//                        .getName());
//                  activityInstanceJson.addProperty("criticality",
//                        activityInstance.getCriticality());
//
//                  if (activityInstance.getPerformedBy() != null)
//                  {
//                     activityInstanceJson.addProperty("userPerformerName",
//                           activityInstance.getPerformedBy().getName());
//                  }
//
//                  activityInstanceJson.addProperty("participantPerformerName",
//                        activityInstance.getParticipantPerformerName());
//
//                  // Map descriptors
//
//                  for (String key : descriptorMap.keySet())
//                  {
//                     Object value = ((ProcessInstanceDetails) activityInstance
//                           .getProcessInstance()).getDescriptorValue(key);
//
//                     if (value == null)
//                     {
//                        activityInstanceJson.addProperty(key, (String) null);
//
//                     }
//                     else if (value instanceof Boolean)
//                     {
//                        activityInstanceJson.addProperty(key, (Boolean) value);
//
//                     }
//                     else if (value instanceof Character)
//                     {
//                        activityInstanceJson.addProperty(key, (Character) value);
//
//                     }
//                     else if (value instanceof Number)
//                     {
//                        activityInstanceJson.addProperty(key, (Number) value);
//
//                     }
//                     else
//                     {
//                        activityInstanceJson.addProperty(key, (String) value);
//                     }
//                  }
//               }
//            }
//            else
//            {
//               TimestampSeriesCumulator cumulator = new TimestampSeriesCumulator(
//                     getValueProvider(primaryObject, fact), getValueProvider(
//                           primaryObject, firstDimension), isDiscreteDimension(
//                           primaryObject, firstDimension), !fact.equals("count"));
//
//               // Grouping criterion
//
//               if (groupByCriterion != null)
//               {
//                  cumulator.setGroupCriterionProvider(getValueProvider(primaryObject,
//                        groupByCriterion));
//               }
//
//               JsonArray groupIds = new JsonArray();
//
//               result.add("groupIds", groupIds);
//               result.add("seriesGroup", cumulator.createCumulatedSeriesGroup(
//                     (List) queryService.getAllActivityInstances(query),
//                     firstDimensionCumulationInterval, groupIds));
//            }
//
//         }
//         else if (primaryObject.equals("role"))
//         {
//            if (dataSetType.equals("recordSet"))
//            {
//            }
//         }
//
//         return result;
      }
      finally
      {
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
