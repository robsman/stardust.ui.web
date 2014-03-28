package org.eclipse.stardust.ui.web.reporting.core;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
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
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.ui.web.reporting.common.JsonMarshaller;
import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.common.RestUtil;
import org.eclipse.stardust.ui.web.reporting.common.mapping.*;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.*;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidationHelper;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidationProblem;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidationProblemsException;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidatorApp;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;

public class ReportingServicePojo
{
   private static final Logger trace = LogManager.getLogger(ReportingServicePojo.class);

   private ServiceFactory serviceFactory;

   private QueryService queryService;

   private IModelService modelService;

   private JsonMarshaller jsonMarshaller;

   @Deprecated
   // TODO: get rid of any hardcoded value from the prototype
   private Map<String, Map<String, ValueProvider>> valueProviders;

   public ReportingServicePojo(ServiceFactory serviceFactory, IModelService modelService)
   {
      this.serviceFactory = serviceFactory;
      this.modelService = modelService;
      this.queryService = serviceFactory.getQueryService();
      this.jsonMarshaller = new JsonMarshaller();

      valueProviders = new HashMap<String, Map<String, ValueProvider>>();

      Map<String, ValueProvider> map = new HashMap<String, ValueProvider>();

      valueProviders.put("processInstance", map);

      map.put("count", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return new Long(1);
         }
      });
      map.put("duration", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            Date time = ((ProcessInstance) object).getTerminationTime();

            if (time == null)
            {
               time = new Date();
            }

            return new Double(time.getTime()
                  - ((ProcessInstance) object).getStartTime().getTime());
         }
      });
      map.put("startTimestamp", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ProcessInstance) object).getStartTime().getTime();
         }
      });
      map.put("terminationTimestamp", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            if (((ProcessInstance) object).getTerminationTime() != null)
            {
               return ((ProcessInstance) object).getTerminationTime().getTime();
            }
            else
            {
               return Long.MAX_VALUE;
            }
         }
      });
      map.put("processName", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ProcessInstance) object).getProcessName();
         }
      });
      map.put("startingUserName", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ProcessInstance) object).getStartingUser().getName();
         }
      });
      map.put("state", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ProcessInstance) object).getState().toString();
         }
      });
      map.put("priority", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ProcessInstance) object).getPriority();
         }
      });

      map = new HashMap<String, ValueProvider>();

      valueProviders.put("activityInstance", map);

      map.put("count", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return new Long(1);
         }
      });
      map.put("duration", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return new Double(((ActivityInstance) object).getLastModificationTime()
                  .getTime() - ((ActivityInstance) object).getStartTime().getTime());
         }
      });
      map.put("startTimestamp", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ActivityInstance) object).getStartTime().getTime();
         }
      });
      map.put("lastModificationTimestamp", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ActivityInstance) object).getLastModificationTime().getTime();
         }
      });
      map.put("activityName", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ActivityInstance) object).getActivity().getName();
         }
      });
      map.put("processName", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ActivityInstance) object).getProcessInstance().getProcessName();
         }
      });
      map.put("userPerformerName", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ActivityInstance) object).getUserPerformerName();
         }
      });
      map.put("participantPerformerName", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ActivityInstance) object).getParticipantPerformerName();
         }
      });
      map.put("state", new ValueProvider()
      {
         public Object getValue(Object object)
         {
            return ((ActivityInstance) object).getState();
         }
      });
   }

   /**
    *
    * @return
    */
   public JsonObject getModelData()
   {
      try
      {
         JsonObject resultJson = new JsonObject();
         JsonObject processesJson = new JsonObject();
         JsonObject descriptorsJson = new JsonObject();

         resultJson.add("processDefinitions", processesJson);
         resultJson.add("descriptors", descriptorsJson);

         // Ensures uniqueness of descriptor entries across all Process
         // Definitions

         Map<String, Object> descriptorsMap = new HashMap<String, Object>();

         for (ProcessDefinition processDefinition : queryService
               .getAllProcessDefinitions())
         {
            JsonObject processJson = new JsonObject();

            processJson.addProperty("id", processDefinition.getQualifiedId());
            processJson.addProperty("name", processDefinition.getName());
            processJson.addProperty("auxiliary",
                  ProcessDefinitionUtils.isAuxiliaryProcess(processDefinition));

            processesJson.add(processDefinition.getId(), processJson);

            for (DataPath dataPath : (List<DataPath>) processDefinition.getAllDataPaths())
            {
               if (dataPath.isDescriptor())
               {
                  if (!descriptorsMap.containsKey(dataPath.getId()))
                  {
                     JsonObject descriptorJson = new JsonObject();

                     descriptorsJson.add(dataPath.getId(), descriptorJson);

                     descriptorJson.addProperty("id", dataPath.getQualifiedId());
                     descriptorJson.addProperty("name", dataPath.getName());
                     descriptorJson.addProperty("type", dataPath.getMappedType()
                           .getSimpleName());
                     descriptorsMap.put(dataPath.getId(), dataPath);
                  }
               }
            }
            
            //add all activities
            JsonArray activities = new JsonArray();
            
            for (Object  activityObj : processDefinition.getAllActivities()) 
            {
               Activity activity = (Activity) activityObj;
               JsonObject activityJsonObj = new JsonObject();
               
               activityJsonObj.addProperty("id", activity.getQualifiedId());   
               activityJsonObj.addProperty("name", activity.getName());
               activityJsonObj.addProperty("auxiliary", ActivityInstanceUtils.isAuxiliaryActivity(activity));
               activityJsonObj.addProperty("interactive", activity.isInteractive());
               activities.add(activityJsonObj);
            }
            processJson.add("activities", activities);
         }

         JsonObject participantsJson = new JsonObject();

         resultJson.add("participants", participantsJson);

         List<QualifiedModelParticipantInfo> qParticipantInfoList = modelService
               .getAllModelParticipants(false);
         for (QualifiedModelParticipantInfo participant : qParticipantInfoList)
         {
            JsonObject participantJson = new JsonObject();

            participantJson.addProperty("id", participant.getQualifiedId());
            participantJson.addProperty("name", participant.getName());

            participantsJson.add(participant.getId(), participantJson);
         }

         return resultJson;
      }
      finally
      {
      }
   }

   /**
    * TODO: This code should be completed looking at the implementation of Process
    * Instance Search in the Portal.
    *
    * @throws ParseException
    * @throws UnsupportedFilterException
    *
    */
   private void addProcessInstanceQueryFilters(ProcessInstanceQuery query,
         List<ReportFilter> filters, JsonObject parametersJson)
         throws UnsupportedFilterException, ParseException
   {
      DateFormat dateFormat = DateFormat.getDateInstance();
      for (ReportFilter filter : filters)
      {
         JsonElement filterValue = filter.getValue();
         String dimension = filter.getDimension();
         String operator = filter.getOperator();

         JsonPrimitive primitiveValue = null;
         JsonArray arrayValue = null;

         if (filterValue.isJsonPrimitive())
         {
            primitiveValue = filterValue.getAsJsonPrimitive();
         }
         else if (filterValue.isJsonArray())
         {
            arrayValue = filterValue.getAsJsonArray();
         }
         else
         {
            throw new IllegalArgumentException(
                  "Unexpected type for filter value of filter " + dimension + ".");
         }

         JsonElement parameterValueJson = null;

         // Overwrite by parameter, if parameter is set

         if (parametersJson.has("filters." + dimension))
         {
            parameterValueJson = parametersJson.get("filters." + dimension)
                  .getAsJsonObject().get("value");
         }

         if (dimension.equals("processName"))
         {
            if (primitiveValue != null && !primitiveValue.isJsonNull())
            {
               query.where(new ProcessDefinitionFilter(parameterValueJson == null
                     || parameterValueJson.isJsonNull()
                     ? primitiveValue.getAsString()
                     : parameterValueJson.getAsString()));
            }

            break;
         }
         else
         {
            if (dimension.equals("startTimestamp"))
            {
               if (!primitiveValue.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.START_TIME.greaterOrEqual(dateFormat
                        .parse(primitiveValue.getAsString()).getTime()));

               }

               if (!primitiveValue.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(dateFormat
                        .parse(primitiveValue.getAsString()).getTime()));

               }
            }
            else if (dimension.equals("terminationTimestamp"))
            {
               if (!primitiveValue.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.TERMINATION_TIME
                        .greaterOrEqual(dateFormat.parse(primitiveValue.getAsString())
                              .getTime()));

               }

               if (!primitiveValue.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.TERMINATION_TIME
                        .lessOrEqual(dateFormat.parse(primitiveValue.getAsString())
                              .getTime()));
               }
            }
            else if (dimension.equals("priority"))
            {
               if (!primitiveValue.isJsonNull())
               {
                  if (operator.equals("equal"))
                  {
                     query.where(ProcessInstanceQuery.PRIORITY
                           .isEqual(parameterValueJson == null
                                 || parameterValueJson.isJsonNull() ? primitiveValue
                                 .getAsLong() : parameterValueJson.getAsLong()));
                  }
               }
            }
            else if (dimension.equals("startingUserName"))
            {
               if (!primitiveValue.isJsonNull())
               {
                  // TODO Find user by account and use OID
                  if (operator.equals("equals"))
                  {
                     query.where(ProcessInstanceQuery.STARTING_USER_OID
                           .isEqual(primitiveValue.getAsLong()));
                  }
               }
            }
            else if (dimension.equals("state"))
            {
               if (parameterValueJson != null && !parameterValueJson.isJsonNull())
               {
                  arrayValue = parameterValueJson.getAsJsonArray();
               }

               ProcessInstanceState[] processInstanceStates = new ProcessInstanceState[arrayValue
                     .size()];

               for (int m = 0; m < arrayValue.size(); ++m)
               {
                  if (arrayValue.get(m).isJsonNull())
                  {
                     break;
                  }

                  if (arrayValue.get(m).getAsString().equals("Created"))
                  {
                     processInstanceStates[m] = ProcessInstanceState.Created;
                  }
                  else if (arrayValue.get(m).getAsString().equals("Active"))
                  {
                     processInstanceStates[m] = ProcessInstanceState.Active;
                  }
                  else if (arrayValue.get(m).getAsString().equals("Completed"))
                  {
                     processInstanceStates[m] = ProcessInstanceState.Completed;
                  }
                  else if (arrayValue.get(m).getAsString().equals("Aborted"))
                  {
                     processInstanceStates[m] = ProcessInstanceState.Aborted;
                  }
                  else
                  {
                     throw new IllegalArgumentException("State "
                           + arrayValue.get(m).getAsString()
                           + " unknown for process instance state.");
                  }
               }

               ProcessStateFilter processStateFilter = new ProcessStateFilter(
                     processInstanceStates);

               query.where(processStateFilter);
            }
            else
            {
               // Descriptors

               if (operator.equals("equal"))
               {
                  query.where(DataFilter.isEqual(dimension, parameterValueJson == null
                        || parameterValueJson.isJsonNull()
                        ? primitiveValue.getAsString()
                        : parameterValueJson.getAsString()));
               }
               else if (operator.equals("equal"))
               {
                  query.where(DataFilter.notEqual(dimension, parameterValueJson == null
                        || parameterValueJson.isJsonNull()
                        ? primitiveValue.getAsString()
                        : parameterValueJson.getAsString()));
               }
               else if (operator.equals("notEqual"))
               {
                  query.where(DataFilter.notEqual(dimension, parameterValueJson == null
                        || parameterValueJson.isJsonNull()
                        ? primitiveValue.getAsString()
                        : parameterValueJson.getAsString()));
               }
            }
         }
      }

   }

   /**
    * TODO: This code should be completed looking at the implementation of Process
    * Instance Search in the Portal.
    *
    * @throws ParseException
    * @throws UnsupportedFilterException
    *
    */
   private void addActivityInstanceQueryFilters(ActivityInstanceQuery query,
         List<ReportFilter> filters, JsonObject parametersJson)
         throws UnsupportedFilterException, ParseException
   {
      DateFormat dateFormat = DateFormat.getDateInstance();

      for (ReportFilter filter : filters)
      {
         trace.debug("Filter: " + filter);
         JsonElement filterValue = filter.getValue();
         String dimension = filter.getDimension();
         String operator = filter.getOperator();

         // TODO: These need to be overwritten by parameters
         JsonObject valueJson = filter.getValues().getAsJsonObject();

         if (dimension.equals("processName"))
         {
            if (!valueJson.isJsonNull())
            {
               query.where(new ProcessDefinitionFilter(valueJson.getAsString()));
            }

            break;
         }
         else if (dimension.equals("activityName"))
         {
            if (!valueJson.isJsonNull())
            {
               query.where(new ActivityFilter(valueJson.getAsString()));
            }

            break;
         }
         else
         {
            if (dimension.equals("startTimestamp"))
            {
               if (!valueJson.isJsonNull())
               {
                  query.where(ActivityInstanceQuery.START_TIME.greaterOrEqual(dateFormat
                        .parse(valueJson.getAsString()).getTime()));

               }

               if (!valueJson.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(dateFormat
                        .parse(valueJson.getAsString()).getTime()));

               }
            }
            else if (dimension.equals("lastModificationTimestamp"))
            {
               if (!valueJson.isJsonNull())
               {
                  query.where(ActivityInstanceQuery.LAST_MODIFICATION_TIME
                        .greaterOrEqual(dateFormat.parse(valueJson.getAsString())
                              .getTime()));

               }

               if (!valueJson.isJsonNull())
               {
                  query.where(ActivityInstanceQuery.LAST_MODIFICATION_TIME
                        .lessOrEqual(dateFormat.parse(valueJson.getAsString()).getTime()));
               }
            }
            else if (dimension.equals("criticality"))
            {
               if (!valueJson.isJsonNull())
               {
                  if (operator.equals("equal"))
                  {
                     query.where(ActivityInstanceQuery.CRITICALITY.isEqual(valueJson
                           .getAsLong()));
                  }
               }
            }
            else if (dimension.equals("participantPerformerName"))
            {
               if (!valueJson.isJsonNull())
               {
                  // TODO Find user by account and use OID
                  if (operator.equals("equals"))
                  {
                     query.where(ActivityInstanceQuery.CURRENT_PERFORMER_OID
                           .isEqual(valueJson.getAsLong()));
                  }
               }
            }
            else if (dimension.equals("state"))
            {
               JsonArray valuesJson = valueJson.getAsJsonArray();
               ActivityInstanceState[] activityInstanceStates = new ActivityInstanceState[valuesJson
                     .size()];

               for (int m = 0; m < valuesJson.size(); ++m)
               {
                  if (valuesJson.get(m).isJsonNull())
                  {
                     break;
                  }

                  if (valuesJson.get(m).getAsString().equals("Application"))
                  {
                     activityInstanceStates[m] = ActivityInstanceState.Application;
                  }
                  else if (valuesJson.get(m).getAsString().equals("Suspended"))
                  {
                     activityInstanceStates[m] = ActivityInstanceState.Suspended;
                  }
                  else if (valuesJson.get(m).getAsString().equals("Hibernated"))
                  {
                     activityInstanceStates[m] = ActivityInstanceState.Hibernated;
                  }
                  else if (valuesJson.get(m).getAsString().equals("Completed"))
                  {
                     activityInstanceStates[m] = ActivityInstanceState.Completed;
                  }
                  else if (valuesJson.get(m).getAsString().equals("Aborting"))
                  {
                     activityInstanceStates[m] = ActivityInstanceState.Aborting;
                  }
                  else if (valuesJson.get(m).getAsString().equals("Aborted"))
                  {
                     activityInstanceStates[m] = ActivityInstanceState.Aborted;
                  }
                  else if (valuesJson.get(m).getAsString().equals("Interrupted"))
                  {
                     activityInstanceStates[m] = ActivityInstanceState.Interrupted;
                  }
                  else
                  {
                     throw new IllegalArgumentException("State "
                           + valuesJson.get(m).getAsString()
                           + " unknown for activity instance state.");
                  }
               }

               ActivityStateFilter activityStateFilter = new ActivityStateFilter(
                     activityInstanceStates);

               query.where(activityStateFilter);

            }
            else
            {
               // Descriptors

               if (operator.equals("equal"))
               {
                  query.where(DataFilter.isEqual(dimension, valueJson.getAsString()));
               }
               else if (operator.equals("equal"))
               {
                  query.where(DataFilter.notEqual(dimension, valueJson.getAsString()));
               }
               else if (operator.equals("notEqual"))
               {
                  query.where(DataFilter.notEqual(dimension, valueJson.getAsString()));
               }
            }
         }
      }
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
         String dataSetType = dataSet.getType();
         String primaryObject = dataSet.getPrimaryObject();
         String fact = dataSet.getFact();
         String firstDimension = dataSet.getFirstDimension();
         JsonObject parametersJson = reportDefinition.getParameters().getAsJsonObject();
         List<ReportFilter> filters = dataSet.getFilters();
         final String groupByCriterion = dataSet.getGroupBy();

         JsonObject result = new JsonObject();
         DateFormat dateFormat = DateFormat.getDateInstance();

         // Obtain cumulation criteria
         final long firstDimensionCumulationIntervalCount;
         final String firstDimensionCumulationIntervalUnit;

         if (dataSet.getFirstDimensionCumulationIntervalCount() != null)
         {
            firstDimensionCumulationIntervalCount = dataSet
                  .getFirstDimensionCumulationIntervalCount();
         }
         else
         {
            firstDimensionCumulationIntervalCount = 1;
         }

         if (dataSet.getFirstDimensionCumulationIntervalUnit() != null)
         {
            firstDimensionCumulationIntervalUnit = dataSet
                  .getFirstDimensionCumulationIntervalUnit();
         }
         else
         {
            firstDimensionCumulationIntervalUnit = "d";
         }

         long firstDimensionCumulationInterval = firstDimensionCumulationIntervalCount
               * ReportingUtil.convertDurationUnit(firstDimensionCumulationIntervalUnit);

         // Obtain external data

         Map<String, Map<String, String>> externalData = null;
         JsonObject externalJoinJson = null;
         String internalKey = null;

         // TODO: review questionable logic
         if (dataSet.isJoinExternalData())
         {
            List<ReportExternalJoin> externalJoins = dataSet.getExternalJoins();
            if (externalJoins != null && !externalJoins.isEmpty())
            {
               ReportExternalJoin externalJoin = externalJoins.get(0);
               String interalKey = externalJoin.getInternalKey();
               trace.info("Internal Key: " + internalKey);

               externalData = retrieveExternalData(externalJoin);
            }
         }

         // Obtain Audit Trail data
         if (primaryObject.equals("processInstance"))
         {
            ProcessInstanceQuery query = ProcessInstanceQuery.findAll();

            addProcessInstanceQueryFilters(query, filters, parametersJson);

            // Set order and restrictions

            if (firstDimension != null)
            {
               // TODO The following code structure should be applicable
               // for all duration dimensions, parameterized by dimension
               // (first, second)

               String firstDimensionValue = dataSet.getFirstDimensionValue();
               List<String> firstDimensionValueList = dataSet
                     .getFirstDimensionValueList();

               if (firstDimension.equals("startTimestamp"))
               {
                  long fromTimestamp = 0; // Beginning of the era
                  String firstDimensionFrom = dataSet.getFirstDimensionFrom();
                  String firstDimensionTo = dataSet.getFirstDimensionTo();
                  String firstDimensionDuration = dataSet.getFirstDimensionDuration();

                  if (StringUtils.isNotEmpty(firstDimensionFrom))
                  {
                     if (parametersJson.has("startTimestamp")
                           && parametersJson.get("startTimestamp").getAsJsonObject()
                                 .has("from"))
                     {
                        query.where(ProcessInstanceQuery.START_TIME
                              .greaterOrEqual(fromTimestamp = dateFormat.parse(
                                    parametersJson.get("startTimestamp")
                                          .getAsJsonObject().get("from").getAsString())
                                    .getTime()));
                     }
                     else
                     {
                        query.where(ProcessInstanceQuery.START_TIME
                              .greaterOrEqual(fromTimestamp = dateFormat.parse(
                                    firstDimensionFrom).getTime()));
                     }
                  }

                  // Distinguish between to/from and to/duration

                  if (StringUtils.isNotEmpty(firstDimensionTo))
                  {
                     if (parametersJson.has("startTimestamp")
                           && parametersJson.get("startTimestamp").getAsJsonObject()
                                 .has("to"))
                     {
                        query.where(ProcessInstanceQuery.START_TIME
                              .lessOrEqual(dateFormat.parse(
                                    parametersJson.get("startTimestamp")
                                          .getAsJsonObject().get("to").getAsString())
                                    .getTime()));
                     }
                     else
                     {
                        query.where(ProcessInstanceQuery.START_TIME
                              .lessOrEqual(dateFormat.parse(firstDimensionTo).getTime()));
                     }
                  }
                  else if (StringUtils.isNotEmpty(firstDimensionDuration))
                  {
                     Long firstDimensionDurationCount = dataSet
                           .getFirstDimensionDurationCount();
                     String firstDimensionDurationUnit = dataSet
                           .getFirstDimensionDurationUnit();

                     if (parametersJson.has("startTimestamp")
                           && parametersJson.get("startTimestamp").getAsJsonObject()
                                 .has("durationValue"))
                     {
                        query.where(ProcessInstanceQuery.START_TIME
                              .lessOrEqual(fromTimestamp
                                    + parametersJson.get("startTimestamp")
                                          .getAsJsonObject().get("durationCount")
                                          .getAsLong()
                                    * ReportingUtil.convertDurationUnit(parametersJson
                                          .get("startTimestamp").getAsJsonObject()
                                          .get("durationUnit").getAsString())));
                     }
                     else
                     {
                        query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(fromTimestamp
                              + firstDimensionDurationCount
                              + ReportingUtil
                                    .convertDurationUnit(firstDimensionDurationUnit)));
                     }
                  }
               }
               else if (firstDimension.equals("terminationTimestamp"))
               {
                  // TODO Replicate above
               }
               else if (firstDimension.equals("processName"))
               {
                  query.orderBy(ProcessInstanceQuery.PROC_DEF_NAME);

                  if (firstDimensionValueList != null
                        && !firstDimensionValueList.isEmpty())
                  {
                     // query.where(ProcessInstanceQuery.PROC_DEF_NAME.equals(valueList.get(0).getAsString()));
                  }
               }
               else if (firstDimension.equals("startingUserName"))
               {
                  query.orderBy(ProcessInstanceQuery.STARTING_USER_OID);

                  if (firstDimensionValue != null)
                  {

                  }
               }
               else if (firstDimension.equals("state"))
               {
                  query.orderBy(ProcessInstanceQuery.STATE);

                  if (firstDimensionValueList != null
                        && !firstDimensionValueList.isEmpty())
                  {

                  }
               }
               else if (firstDimension.equals("priority"))
               {
                  query.orderBy(ProcessInstanceQuery.PRIORITY);

                  if (firstDimensionValueList != null
                        && !firstDimensionValueList.isEmpty())
                  {

                  }
               }

               // TODO Handle descriptor filters
            }

            // TODO Add second dimension filters

            // TODO Decide whether descriptors are needed

            query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);

            if (dataSetType.equals("recordSet"))
            {
               JsonArray series = new JsonArray();

               result.add("recordSet", series);

               Map<String, String> descriptorMap = null;

               for (ProcessInstance processInstance : queryService
                     .getAllProcessInstances(query))
               {
                  // Filter for unique descriptors

                  if (descriptorMap == null)
                  {
                     descriptorMap = new HashMap<String, String>();

                     for (String key : ((ProcessInstanceDetails) processInstance)
                           .getDescriptors().keySet())
                     {
                        if (!descriptorMap.containsKey(key))
                        {
                           descriptorMap.put(key, key);
                        }
                     }
                  }

                  JsonObject processInstanceJson = new JsonObject();

                  series.add(processInstanceJson);

                  processInstanceJson.addProperty("processId",
                        processInstance.getProcessID());
                  processInstanceJson.addProperty("processName",
                        processInstance.getProcessName());
                  processInstanceJson.addProperty("startTimestamp", processInstance
                        .getStartTime().getTime());

                  if (processInstance.getTerminationTime() != null)
                  {
                     processInstanceJson.addProperty("terminationTimestamp",
                           processInstance.getTerminationTime().getTime());
                  }

                  processInstanceJson.addProperty("state", processInstance.getState()
                        .getName());
                  processInstanceJson.addProperty("priority",
                        processInstance.getPriority());

                  // Map descriptors

                  for (String key : descriptorMap.keySet())
                  {
                     JsonUtil.addPrimitiveObjectToJsonObject(processInstanceJson, key,
                           ((ProcessInstanceDetails) processInstance)
                                 .getDescriptorValue(key));
                  }

                  // Join external data

                  if (internalKey != null && processInstanceJson.has(internalKey)
                        && !processInstanceJson.get(internalKey).isJsonNull())
                  {
                     String internalKeValue = processInstanceJson.get(internalKey)
                           .getAsString();

                     trace.info("Internal Key Value: " + internalKeValue);

                     Map<String, String> record = externalData.get(internalKeValue);

                     trace.info("Record: " + record);

                     if (record != null)
                     {
                        for (String key : record.keySet())
                        {
                           trace.info("Key: " + key);
                           trace.info("Value: " + record.get(key));

                           processInstanceJson.addProperty(key, record.get(key));
                        }
                     }
                  }

                  // Add computed columns
                  List<ReportComputedColumn> computedColumns = dataSet
                        .getComputedColumns();
                  if (computedColumns != null)
                  {
                     addComputedColumns(computedColumns, processInstanceJson);
                  }

               }
            }
            else
            {
               TimestampSeriesCumulator cumulator = new TimestampSeriesCumulator(
                     getValueProvider(primaryObject, fact), getValueProvider(
                           primaryObject, firstDimension), isDiscreteDimension(
                           primaryObject, firstDimension), !fact.equals("count"));

               // Grouping criterion

               if (groupByCriterion != null)
               {
                  cumulator.setGroupCriterionProvider(getValueProvider(primaryObject,
                        groupByCriterion));
               }

               JsonArray groupIds = new JsonArray();

               result.add("groupIds", groupIds);
               result.add("seriesGroup", cumulator.createCumulatedSeriesGroup(
                     (List) queryService.getAllProcessInstances(query),
                     firstDimensionCumulationInterval, groupIds));
            }
         }
         else if (primaryObject.equals("activityInstance"))
         {
            ActivityInstanceQuery query = ActivityInstanceQuery.findAll();

            addActivityInstanceQueryFilters(query, filters, parametersJson);

            // Set order

            if (firstDimension != null)
            {
               if (firstDimension.equals("activityName"))
               {
                  query.orderBy(ActivityInstanceQuery.ACTIVITY_NAME);
               }
               else if (firstDimension.equals("processName"))
               {
                  query.orderBy(ActivityInstanceQuery.PROC_DEF_NAME);
               }
               else if (firstDimension.equals("userPerformerName"))
               {
                  query.orderBy(ActivityInstanceQuery.CURRENT_USER_PERFORMER_OID);
               }
               else if (firstDimension.equals("participantPerformerName"))
               {
                  query.orderBy(ActivityInstanceQuery.CURRENT_PERFORMER_OID);
               }
               else if (firstDimension.equals("state"))
               {
                  query.orderBy(ActivityInstanceQuery.STATE);
               }
            }

            if (dataSetType.equals("recordSet"))
            {
               JsonArray series = new JsonArray();

               result.add("recordSet", series);

               Map<String, String> descriptorMap = null;

               for (ActivityInstance activityInstance : queryService
                     .getAllActivityInstances(query))
               {
                  // Filter for unique descriptors

                  if (descriptorMap == null)
                  {
                     descriptorMap = new HashMap<String, String>();

                     for (String key : ((ProcessInstanceDetails) activityInstance
                           .getProcessInstance()).getDescriptors().keySet())
                     {
                        if (!descriptorMap.containsKey(key))
                        {
                           descriptorMap.put(key, key);
                        }
                     }
                  }

                  JsonObject activityInstanceJson = new JsonObject();

                  series.add(activityInstanceJson);

                  activityInstanceJson.addProperty("activityId", activityInstance
                        .getActivity().getId());
                  activityInstanceJson.addProperty("activityName", activityInstance
                        .getActivity().getName());
                  activityInstanceJson.addProperty("processId", activityInstance
                        .getProcessInstance().getProcessID());
                  activityInstanceJson.addProperty("processName", activityInstance
                        .getProcessInstance().getProcessName());
                  activityInstanceJson.addProperty("startTimestamp", activityInstance
                        .getStartTime().getTime());
                  activityInstanceJson.addProperty("id", activityInstance.getActivity()
                        .getId());
                  activityInstanceJson.addProperty("state", activityInstance.getState()
                        .getName());
                  activityInstanceJson.addProperty("criticality",
                        activityInstance.getCriticality());

                  if (activityInstance.getPerformedBy() != null)
                  {
                     activityInstanceJson.addProperty("userPerformerName",
                           activityInstance.getPerformedBy().getName());
                  }

                  activityInstanceJson.addProperty("participantPerformerName",
                        activityInstance.getParticipantPerformerName());

                  // Map descriptors

                  for (String key : descriptorMap.keySet())
                  {
                     Object value = ((ProcessInstanceDetails) activityInstance
                           .getProcessInstance()).getDescriptorValue(key);

                     if (value == null)
                     {
                        activityInstanceJson.addProperty(key, (String) null);

                     }
                     else if (value instanceof Boolean)
                     {
                        activityInstanceJson.addProperty(key, (Boolean) value);

                     }
                     else if (value instanceof Character)
                     {
                        activityInstanceJson.addProperty(key, (Character) value);

                     }
                     else if (value instanceof Number)
                     {
                        activityInstanceJson.addProperty(key, (Number) value);

                     }
                     else
                     {
                        activityInstanceJson.addProperty(key, (String) value);
                     }
                  }
               }
            }
            else
            {
               TimestampSeriesCumulator cumulator = new TimestampSeriesCumulator(
                     getValueProvider(primaryObject, fact), getValueProvider(
                           primaryObject, firstDimension), isDiscreteDimension(
                           primaryObject, firstDimension), !fact.equals("count"));

               // Grouping criterion

               if (groupByCriterion != null)
               {
                  cumulator.setGroupCriterionProvider(getValueProvider(primaryObject,
                        groupByCriterion));
               }

               JsonArray groupIds = new JsonArray();

               result.add("groupIds", groupIds);
               result.add("seriesGroup", cumulator.createCumulatedSeriesGroup(
                     (List) queryService.getAllActivityInstances(query),
                     firstDimensionCumulationInterval, groupIds));
            }

         }
         else if (primaryObject.equals("role"))
         {
            if (dataSetType.equals("recordSet"))
            {
            }
         }

         return result;
      }
      finally
      {
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
               scope.put(entry.getKey(), JsonUtil.convertToJavaObject(jsonPrimitive));
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

   @Deprecated
   /**
    *
    * @return
    */
   public ValueProvider getValueProvider(String primaryObject, final String property)
   {

      if (valueProviders.containsKey(primaryObject)
            && valueProviders.get(primaryObject).containsKey(property))
      {
         return valueProviders.get(primaryObject).get(property);
      }
      else
      {
         if (primaryObject.equals("processInstance"))
         {
            return new ValueProvider()
            {
               public Object getValue(Object object)
               {
                  return ((ProcessInstanceDetails) object).getDescriptorValue(property);
               }
            };
         }
         else if (primaryObject.equals("activityInstance"))
         {
            return new ValueProvider()
            {
               public Object getValue(Object object)
               {
                  return ((ActivityInstanceDetails) object).getDescriptorValue(property);
               }
            };
         }
      }

      throw new IllegalArgumentException("No property found for primary object "
            + primaryObject + " and property " + property + ".");
   };
}
