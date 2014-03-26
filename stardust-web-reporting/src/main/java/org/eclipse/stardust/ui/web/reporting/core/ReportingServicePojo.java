package org.eclipse.stardust.ui.web.reporting.core;

import java.io.IOException;
import java.net.MalformedURLException;
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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.ui.web.reporting.common.JsonMarshaller;
import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.common.RestUtil;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;
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
         JsonArray filters, JsonObject parametersJson) throws UnsupportedFilterException,
         ParseException
   {
      DateFormat dateFormat = DateFormat.getDateInstance();

      for (int n = 0; n < filters.size(); ++n)
      {
         JsonObject filterJson = filters.get(n).getAsJsonObject();
         JsonPrimitive valueJson = null;
         JsonArray valuesJson = null;

         if (filterJson.get("value").isJsonPrimitive())
         {
            valueJson = filterJson.get("value").getAsJsonPrimitive();
         }
         else if (filterJson.get("value").isJsonArray())
         {
            valuesJson = filterJson.get("value").getAsJsonArray();
         }
         else
         {
            throw new IllegalArgumentException(
                  "Unexpected type for filter value of filter "
                        + filterJson.get("dimension").getAsString() + ".");
         }

         JsonElement parameterValueJson = null;

         // Overwrite by parameter, if parameter is set

         if (parametersJson.has("filters." + filterJson.get("dimension").getAsString()))
         {

            parameterValueJson = parametersJson
                  .get("filters." + filterJson.get("dimension").getAsString())
                  .getAsJsonObject().get("value");
         }

         if (filterJson.get("dimension").getAsString().equals("processName"))
         {
            if (valueJson != null && !valueJson.isJsonNull())
            {
               query.where(new ProcessDefinitionFilter(parameterValueJson == null
                     || parameterValueJson.isJsonNull()
                     ? valueJson.getAsString()
                     : parameterValueJson.getAsString()));
            }

            break;
         }
         else
         {
            if (filterJson.get("dimension").getAsString().equals("startTimestamp"))
            {
               if (!valueJson.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.START_TIME.greaterOrEqual(dateFormat
                        .parse(valueJson.getAsString()).getTime()));

               }

               if (!valueJson.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(dateFormat
                        .parse(valueJson.getAsString()).getTime()));

               }
            }
            else if (filterJson.get("dimension").getAsString()
                  .equals("terminationTimestamp"))
            {
               if (!valueJson.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.TERMINATION_TIME
                        .greaterOrEqual(dateFormat.parse(valueJson.getAsString())
                              .getTime()));

               }

               if (!valueJson.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.TERMINATION_TIME
                        .lessOrEqual(dateFormat.parse(valueJson.getAsString()).getTime()));
               }
            }
            else if (filterJson.get("dimension").getAsString().equals("priority"))
            {
               if (!valueJson.isJsonNull())
               {
                  if (filterJson.get("operator").getAsString().equals("equal"))
                  {
                     query.where(ProcessInstanceQuery.PRIORITY
                           .isEqual(parameterValueJson == null
                                 || parameterValueJson.isJsonNull() ? valueJson
                                 .getAsLong() : parameterValueJson.getAsLong()));
                  }
               }
            }
            else if (filterJson.get("dimension").getAsString().equals("startingUserName"))
            {
               if (!valueJson.isJsonNull())
               {
                  // TODO Find user by account and use OID
                  if (filterJson.get("operator").getAsString().equals("equals"))
                  {
                     query.where(ProcessInstanceQuery.STARTING_USER_OID.isEqual(valueJson
                           .getAsLong()));
                  }
               }
            }
            else if (filterJson.get("dimension").getAsString().equals("state"))
            {
               if (parameterValueJson != null && !parameterValueJson.isJsonNull())
               {
                  valuesJson = parameterValueJson.getAsJsonArray();
               }

               ProcessInstanceState[] processInstanceStates = new ProcessInstanceState[valuesJson
                     .size()];

               for (int m = 0; m < valuesJson.size(); ++m)
               {
                  if (valuesJson.get(m).isJsonNull())
                  {
                     break;
                  }

                  if (valuesJson.get(m).getAsString().equals("Created"))
                  {
                     processInstanceStates[m] = ProcessInstanceState.Created;
                  }
                  else if (valuesJson.get(m).getAsString().equals("Active"))
                  {
                     processInstanceStates[m] = ProcessInstanceState.Active;
                  }
                  else if (valuesJson.get(m).getAsString().equals("Completed"))
                  {
                     processInstanceStates[m] = ProcessInstanceState.Completed;
                  }
                  else if (valuesJson.get(m).getAsString().equals("Aborted"))
                  {
                     processInstanceStates[m] = ProcessInstanceState.Aborted;
                  }
                  else
                  {
                     throw new IllegalArgumentException("State "
                           + valuesJson.get(m).getAsString()
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

               if (filterJson.get("operator").getAsString().equals("equal"))
               {
                  query.where(DataFilter.isEqual(filterJson.get("dimension")
                        .getAsString(),
                        parameterValueJson == null || parameterValueJson.isJsonNull()
                              ? valueJson.getAsString()
                              : parameterValueJson.getAsString()));
               }
               else if (filterJson.get("operator").getAsString().equals("equal"))
               {
                  query.where(DataFilter.notEqual(filterJson.get("dimension")
                        .getAsString(),
                        parameterValueJson == null || parameterValueJson.isJsonNull()
                              ? valueJson.getAsString()
                              : parameterValueJson.getAsString()));
               }
               else if (filterJson.get("operator").getAsString().equals("notEqual"))
               {
                  query.where(DataFilter.notEqual(filterJson.get("dimension")
                        .getAsString(),
                        parameterValueJson == null || parameterValueJson.isJsonNull()
                              ? valueJson.getAsString()
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
         JsonArray filters, JsonObject parametersJson) throws UnsupportedFilterException,
         ParseException
   {
      DateFormat dateFormat = DateFormat.getDateInstance();

      for (int n = 0; n < filters.size(); ++n)
      {
         JsonObject filterJson = filters.get(n).getAsJsonObject();

         trace.debug("Filter: " + filterJson.toString());

         // TODO: These need to be overwritten by parameters

         JsonObject valueJson = filterJson.get("values").getAsJsonObject();

         if (filterJson.get("dimension").getAsString().equals("processName"))
         {
            if (!valueJson.isJsonNull())
            {
               query.where(new ProcessDefinitionFilter(valueJson.getAsString()));
            }

            break;
         }
         else if (filterJson.get("dimension").getAsString().equals("activityName"))
         {
            if (!valueJson.isJsonNull())
            {
               query.where(new ActivityFilter(valueJson.getAsString()));
            }

            break;
         }
         else
         {
            if (filterJson.get("dimension").getAsString().equals("startTimestamp"))
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
            else if (filterJson.get("dimension").getAsString()
                  .equals("lastModificationTimestamp"))
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
            else if (filterJson.get("dimension").getAsString().equals("criticality"))
            {
               if (!valueJson.isJsonNull())
               {
                  if (filterJson.get("operator").getAsString().equals("equal"))
                  {
                     query.where(ActivityInstanceQuery.CRITICALITY.isEqual(valueJson
                           .getAsLong()));
                  }
               }
            }
            else if (filterJson.get("dimension").getAsString()
                  .equals("participantPerformerName"))
            {
               if (!valueJson.isJsonNull())
               {
                  // TODO Find user by account and use OID
                  if (filterJson.get("operator").getAsString().equals("equals"))
                  {
                     query.where(ActivityInstanceQuery.CURRENT_PERFORMER_OID
                           .isEqual(valueJson.getAsLong()));
                  }
               }
            }
            else if (filterJson.get("dimension").getAsString().equals("state"))
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

               if (filterJson.get("operator").getAsString().equals("equal"))
               {
                  query.where(DataFilter.isEqual(filterJson.get("dimension")
                        .getAsString(), valueJson.getAsString()));
               }
               else if (filterJson.get("operator").getAsString().equals("equal"))
               {
                  query.where(DataFilter.notEqual(filterJson.get("dimension")
                        .getAsString(), valueJson.getAsString()));
               }
               else if (filterJson.get("operator").getAsString().equals("notEqual"))
               {
                  query.where(DataFilter.notEqual(filterJson.get("dimension")
                        .getAsString(), valueJson.getAsString()));
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
         JsonObject dataSetJson = reportJson.get("dataSet").getAsJsonObject();
         String dataSetType = dataSetJson.get("type").getAsString();
         String primaryObject = dataSetJson.get("primaryObject").getAsString();
         String fact = dataSetJson.get("fact").getAsString();
         String firstDimension = dataSetJson.get("firstDimension").getAsString();
         JsonObject parametersJson = reportJson.get("parameters").getAsJsonObject();
         JsonArray filters = dataSetJson.get("filters").getAsJsonArray();
         final String groupByCriterion = dataSetJson.has("groupBy") ? dataSetJson.get(
               "groupBy").getAsString() : null;
         JsonObject result = new JsonObject();
         DateFormat dateFormat = DateFormat.getDateInstance();

         // Obtain cumulation criteria

         long firstDimensionCumulationIntervalCount = 1;
         String firstDimensionCumulationIntervalUnit = "d";

         if (dataSetJson.has("firstDimensionCumulationIntervalCount"))
         {
            firstDimensionCumulationIntervalCount = dataSetJson.get(
                  "firstDimensionCumulationIntervalCount").getAsLong();
         }

         if (dataSetJson.has("firstDimensionCumulationIntervalUnit"))
         {
            firstDimensionCumulationIntervalUnit = dataSetJson.get(
                  "firstDimensionCumulationIntervalUnit").getAsString();
         }

         long firstDimensionCumulationInterval = firstDimensionCumulationIntervalCount
               * ReportingUtil.convertDurationUnit(firstDimensionCumulationIntervalUnit);

         // Obtain external data

         Map<String, Map<String, String>> externalData = null;
         JsonObject externalJoinJson = null;
         String internalKey = null;

         if (dataSetJson.get("joinExternalData").getAsBoolean())
         {
            JsonArray externalJoinsJson = dataSetJson.get("externalJoins")
                  .getAsJsonArray();
            externalJoinJson = externalJoinsJson.get(0).getAsJsonObject();
            internalKey = externalJoinJson.get("internalKey").getAsString();
            externalData = retrieveExternalData(externalJoinJson);

            trace.info("Internal Key: " + internalKey);
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

               if (firstDimension.equals("startTimestamp"))
               {
                  long fromTimestamp = 0; // Beginning of the era

                  if (dataSetJson.has("firstDimensionFrom"))
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
                     else if (!dataSetJson.get("firstDimensionFrom").isJsonNull())
                     {
                        query.where(ProcessInstanceQuery.START_TIME
                              .greaterOrEqual(fromTimestamp = dateFormat.parse(
                                    dataSetJson.get("firstDimensionFrom").getAsString())
                                    .getTime()));
                     }
                  }

                  // Distinguish between to/from and to/duration

                  if (dataSetJson.has("firstDimensionTo"))
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
                     else if (!dataSetJson.get("firstDimensionTo").isJsonNull())
                     {
                        query.where(ProcessInstanceQuery.START_TIME
                              .lessOrEqual(dateFormat.parse(
                                    dataSetJson.get("firstDimensionTo").getAsString())
                                    .getTime()));
                     }
                  }
                  else if (dataSetJson.has("firstDimensionDuration"))
                  {
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
                     else if (!dataSetJson.get("firstDimensionDuration").isJsonNull())
                     {
                        query.where(ProcessInstanceQuery.START_TIME
                              .lessOrEqual(fromTimestamp
                                    + dataSetJson.get("firstDimensionDurationCount")
                                          .getAsLong()
                                    + ReportingUtil.convertDurationUnit(dataSetJson.get(
                                          "firstDimensionDurationUnit").getAsString())));
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

                  if (dataSetJson.has("firstDimensionValueList"))
                  {
                     JsonArray valueList = dataSetJson.get("firstDimensionValueList")
                           .getAsJsonArray();

                     // query.where(ProcessInstanceQuery.PROC_DEF_NAME.equals(valueList.get(0).getAsString()));

                  }
               }
               else if (firstDimension.equals("startingUserName"))
               {
                  query.orderBy(ProcessInstanceQuery.STARTING_USER_OID);

                  if (dataSetJson.has("firstDimensionValue"))
                  {

                  }
               }
               else if (firstDimension.equals("state"))
               {
                  query.orderBy(ProcessInstanceQuery.STATE);

                  if (dataSetJson.has("firstDimensionValueList"))
                  {

                  }
               }
               else if (firstDimension.equals("priority"))
               {
                  query.orderBy(ProcessInstanceQuery.PRIORITY);

                  if (dataSetJson.has("firstDimensionValueList"))
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

                  addComputedColumns(dataSetJson.get("computedColumns").getAsJsonArray(),
                        processInstanceJson);
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
         JsonObject externalJoinJson)
   {
      try
      {
         String restUri = externalJoinJson.get("restUri").getAsString();
         String externalDataJsonResponse = RestUtil.performRestJsonCall(restUri);

         JsonArray externalDataJson = jsonMarshaller
               .readJsonObject(externalDataJsonResponse).get("list").getAsJsonArray();

         trace.info("External Data:");
         trace.info(externalDataJson.toString());

         Map<String, Map<String, String>> externalData = new HashMap<String, Map<String, String>>();
         JsonArray externalJoinFieldsJson = externalJoinJson.get("fields")
               .getAsJsonArray();

         for (int n = 0; n < externalDataJson.size(); n++)
         {
            JsonObject recordJson = externalDataJson.get(n).getAsJsonObject();
            Map<String, String> record = new HashMap<String, String>();

            for (int m = 0; m < externalJoinFieldsJson.size(); m++)
            {
               JsonObject externalJoinFieldJson = externalJoinFieldsJson.get(m)
                     .getAsJsonObject();

               if (externalJoinFieldJson.get("id").getAsString()
                     .equals(externalJoinJson.get("externalKey").getAsString()))
               {
                  externalData.put(
                        recordJson.get(externalJoinFieldJson.get("id").getAsString())
                              .getAsString(), record);
               }

               // TODO Other type mapping than string (central mapping
               // function f(type,object, container))

               record.put(externalJoinFieldJson.get("useAs").getAsString(), recordJson
                     .get(externalJoinFieldJson.get("id").getAsString()).getAsString());
            }
         }

         trace.info("Map");
         trace.info(externalData);

         return externalData;
      }
      catch (MalformedURLException e)
      {
         trace.error(e);

         throw new RuntimeException(e);
      }
      catch (IOException e)
      {
         trace.error(e);

         throw new RuntimeException(e);
      }
   }

   /**
    *
    * @param computedColumns
    * @param recordJson
    */
   private static void addComputedColumns(JsonArray computedColumns, JsonObject recordJson)
   {
      for (int n = 0; n < computedColumns.size(); ++n)
      {
         JsonObject computedColumn = computedColumns.get(n).getAsJsonObject();
         // TODO Type conversion

         JsonUtil.addPrimitiveObjectToJsonObject(
               recordJson,
               computedColumn.get("id").getAsString(),
               evaluateComputedColumn(recordJson, computedColumn.get("formula")
                     .getAsString()));
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
