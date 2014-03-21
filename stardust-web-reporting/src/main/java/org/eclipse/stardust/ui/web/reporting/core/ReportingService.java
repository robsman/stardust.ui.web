/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessStateFilter;
import org.eclipse.stardust.engine.api.query.UnsupportedFilterException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.reporting.common.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ReportingService
{
   private static final Logger trace = LogManager.getLogger(ReportingService.class);
   
   private static final String PUBLIC_REPORT_DEFINITIONS_DIR = "/reports";
   private static final String PARTICIPANTS_REPORT_DEFINITIONS_DIR = "/participants/reports/";
   
   @Resource
   private SessionContext sessionContext;
   private DocumentManagementService documentManagementService;
   private UserService userService;
   private QueryService queryService;
   private JsonMarshaller jsonIo;
   /**
    * Stores uncommitted changes.
    */
   private Map<String, JsonObject> reportDefinitionCache;
   private Map<String, JsonObject> reportDefinitionJsons;
   private Map<String, Map<String, ValueProvider>> valueProviders;

   public ReportingService()
   {
      super();

      jsonIo = new JsonMarshaller();
      reportDefinitionCache = new HashMap<String, JsonObject>();
      reportDefinitionJsons = new HashMap<String, JsonObject>();

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

            return new Double(time.getTime() - ((ProcessInstance) object).getStartTime().getTime());
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
            return new Double(((ActivityInstance) object).getLastModificationTime().getTime()
                  - ((ActivityInstance) object).getStartTime().getTime());
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

   private ServiceFactory getServiceFactory()
   {
      return sessionContext.getServiceFactory();
   }

   /**
    * 
    * @return
    */
   DocumentManagementService getDocumentManagementService()
   {
      if (documentManagementService == null)
      {
         documentManagementService = getServiceFactory().getDocumentManagementService();
      }

      return documentManagementService;
   }

   /**
    * 
    * @return
    */
   private UserService getUserService()
   {
      if (userService == null)
      {
         userService = getServiceFactory().getUserService();
      }

      return userService;
   }

   /**
    * 
    * @return
    */
   private QueryService getQueryService()
   {
      if (queryService == null)
      {
         queryService = getServiceFactory().getQueryService();
      }

      return queryService;
   }

   /**
    * 
    * @return
    */
   public JsonObject getModelData(ServletContext servletContext)
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

         for (ProcessDefinition processDefinition : getQueryService().getAllProcessDefinitions())
         {
            JsonObject processJson = new JsonObject();

            processJson.addProperty("id", processDefinition.getId());
            processJson.addProperty("name", processDefinition.getName());

            processesJson.add(processDefinition.getId(), processJson);

            for (DataPath dataPath : (List<DataPath>) processDefinition.getAllDataPaths())
            {
               if (dataPath.isDescriptor())
               {
                  if (!descriptorsMap.containsKey(dataPath.getId()))
                  {
                     JsonObject descriptorJson = new JsonObject();

                     descriptorsJson.add(dataPath.getId(), descriptorJson);

                     descriptorJson.addProperty("id", dataPath.getId());
                     descriptorJson.addProperty("name", dataPath.getName());
                     descriptorJson.addProperty("type", dataPath.getMappedType().getSimpleName());
                     descriptorsMap.put(dataPath.getId(), dataPath);
                  }
               }
            }
         }

         JsonObject participantsJson = new JsonObject();

         resultJson.add("participants", participantsJson);
         
         List<QualifiedModelParticipantInfo> qParticipantInfoList = getAllModelParticipants(servletContext, sessionContext, false);

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

   //TODO: move this method to ViewsCommon#ParticipantUtils later
   // do we need to cache models/ participants for better performances
   
   public static List<QualifiedModelParticipantInfo> getAllModelParticipants(ServletContext servletContext, SessionContext sessionContext, boolean filterPredefinedModel)
   {
      Collection<DeployedModel> allModels = ModelCache.getModelCache(sessionContext, servletContext).getActiveModels();
         
      List<QualifiedModelParticipantInfo> allParticipants = new ArrayList<QualifiedModelParticipantInfo>();
      Set<String> allParticipantQIDs = new HashSet<String>();
      boolean isAdminAdded = false;

      for (DeployedModelDescription model : allModels)
      {
         if (filterPredefinedModel && PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
         {
            continue;
         }
         Collection<Participant> participants = ModelCache.getModelCache(sessionContext, servletContext).getAllParticipants();
         
         for (Participant participant : participants)
         {
            if (participant instanceof QualifiedModelParticipantInfo)
            {
               boolean isAdminRole = ParticipantUtils.isAdministratorRole(participant);

               // Administrator should be added only once
               if (!isAdminAdded && isAdminRole)
               {
                  allParticipants.add((QualifiedModelParticipantInfo) participant);
                  isAdminAdded = true;
               }
               else if (!isAdminRole)
               {
                  if (!allParticipantQIDs.contains(participant.getQualifiedId()))
                  {
                     allParticipants.add((QualifiedModelParticipantInfo) participant);
                     allParticipantQIDs.add(participant.getQualifiedId());
                  }
               }
            }
         }
      }
      return allParticipants;
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

   /**
    * 
    * @param jsonObject
    * @param primitiveObject
    */
   private static void addPrimitiveObjectToJsonObject(JsonObject jsonObject, String key, Object value)
   {
      if (value == null)
      {
         jsonObject.addProperty(key, (String) null);
      }
      else if (value instanceof Boolean)
      {
         jsonObject.addProperty(key, (Boolean) value);

      }
      else if (value instanceof Character)
      {
         jsonObject.addProperty(key, (Character) value);

      }
      else if (value instanceof Number)
      {
         jsonObject.addProperty(key, (Number) value);

      }
      else
      {
         jsonObject.addProperty(key, value.toString());
      }
   }

   /**
    * 
    * @param jsonElement
    * @return
    */
   private static Object convertJsonPrimitiveToObject(JsonElement jsonElement)
   {
      if (jsonElement.isJsonNull())
      {
         return null;
      }
      else if (jsonElement.isJsonPrimitive())
      {
         if (jsonElement.getAsJsonPrimitive().isString())
         {
            return jsonElement.getAsString();
         }
         else if (jsonElement.getAsJsonPrimitive().isBoolean())
         {
            return jsonElement.getAsBoolean();
         }
         else if (jsonElement.getAsJsonPrimitive().isNumber())
         {
            return jsonElement.getAsNumber();
         }
         else
         {
            throw new RuntimeException("JSON entry " + jsonElement + " has an unknown primitive type.");
         }
      }
      else
      {
         throw new RuntimeException("JSON entry " + jsonElement + " is not a primitive.");
      }
   }

   /**
    * 
    * @return
    */
   public ValueProvider getValueProvider(String primaryObject, final String property)
   {

      if (valueProviders.containsKey(primaryObject) && valueProviders.get(primaryObject).containsKey(property))
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

      throw new IllegalArgumentException("No property found for primary object " + primaryObject + " and property "
            + property + ".");
   };

   /**
    * Converts the key of a duration unit into the equivalent in seconds.
    * 
    * @param unit
    * @return
    */
   private long convertDurationUnit(String unit)
   {
      if (unit.equals("m"))
      {
         return 1000 * 60;
      }
      else if (unit.equals("h"))
      {
         return 1000 * 60 * 60;
      }
      else if (unit.equals("d"))
      {
         return 1000 * 60 * 60 * 24;
      }
      else if (unit.equals("w"))
      {
         return 1000 * 60 * 60 * 24 * 7;
      }
      else if (unit.equals("M"))
      {
         return 1000 * 60 * 60 * 24 * 30; // TODO Consider calendar?
      }
      else if (unit.equals("Y"))
      {
         return 1000 * 60 * 60 * 24 * 30 * 256; // TODO Consider calendar?
      }

      throw new IllegalArgumentException("Duration unit \"" + unit + "\" is not supported.");
   }

   /**
    * 
    * @return
    * @throws ParseException
    * @throws UnsupportedFilterException
    */
   public JsonObject getReportData(JsonObject reportJson) throws UnsupportedFilterException, ParseException
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
         final String groupByCriterion = dataSetJson.has("groupBy") ? dataSetJson.get("groupBy").getAsString() : null;
         JsonObject result = new JsonObject();
         DateFormat dateFormat = DateFormat.getDateInstance();

         // Obtain cumulation criteria

         long firstDimensionCumulationIntervalCount = 1;
         String firstDimensionCumulationIntervalUnit = "d";

         if (dataSetJson.has("firstDimensionCumulationIntervalCount"))
         {
            firstDimensionCumulationIntervalCount = dataSetJson.get("firstDimensionCumulationIntervalCount")
                  .getAsLong();
         }

         if (dataSetJson.has("firstDimensionCumulationIntervalUnit"))
         {
            firstDimensionCumulationIntervalUnit = dataSetJson.get("firstDimensionCumulationIntervalUnit")
                  .getAsString();
         }

         long firstDimensionCumulationInterval = firstDimensionCumulationIntervalCount
               * convertDurationUnit(firstDimensionCumulationIntervalUnit);

         // Obtain external data

         Map<String, Map<String, String>> externalData = null;
         JsonObject externalJoinJson = null;
         String internalKey = null;

         if (dataSetJson.get("joinExternalData").getAsBoolean())
         {
            JsonArray externalJoinsJson = dataSetJson.get("externalJoins").getAsJsonArray();
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
                           && parametersJson.get("startTimestamp").getAsJsonObject().has("from"))
                     {
                        query.where(ProcessInstanceQuery.START_TIME.greaterOrEqual(fromTimestamp = dateFormat.parse(
                              parametersJson.get("startTimestamp").getAsJsonObject().get("from").getAsString())
                              .getTime()));
                     }
                     else if (!dataSetJson.get("firstDimensionFrom").isJsonNull())
                     {
                        query.where(ProcessInstanceQuery.START_TIME.greaterOrEqual(fromTimestamp = dateFormat.parse(
                              dataSetJson.get("firstDimensionFrom").getAsString()).getTime()));
                     }
                  }

                  // Distinguish between to/from and to/duration

                  if (dataSetJson.has("firstDimensionTo"))
                  {
                     if (parametersJson.has("startTimestamp")
                           && parametersJson.get("startTimestamp").getAsJsonObject().has("to"))
                     {
                        query.where(ProcessInstanceQuery.START_TIME
                              .lessOrEqual(dateFormat.parse(
                                    parametersJson.get("startTimestamp").getAsJsonObject().get("to").getAsString())
                                    .getTime()));
                     }
                     else if (!dataSetJson.get("firstDimensionTo").isJsonNull())
                     {
                        query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(dateFormat.parse(
                              dataSetJson.get("firstDimensionTo").getAsString()).getTime()));
                     }
                  }
                  else if (dataSetJson.has("firstDimensionDuration"))
                  {
                     if (parametersJson.has("startTimestamp")
                           && parametersJson.get("startTimestamp").getAsJsonObject().has("durationValue"))
                     {
                        query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(fromTimestamp
                              + parametersJson.get("startTimestamp").getAsJsonObject().get("durationCount").getAsLong()
                              * convertDurationUnit(parametersJson.get("startTimestamp").getAsJsonObject()
                                    .get("durationUnit").getAsString())));
                     }
                     else if (!dataSetJson.get("firstDimensionDuration").isJsonNull())
                     {
                        query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(fromTimestamp
                              + dataSetJson.get("firstDimensionDurationCount").getAsLong()
                              + convertDurationUnit(dataSetJson.get("firstDimensionDurationUnit").getAsString())));
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
                     JsonArray valueList = dataSetJson.get("firstDimensionValueList").getAsJsonArray();

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

               for (ProcessInstance processInstance : getQueryService().getAllProcessInstances(query))
               {
                  // Filter for unique descriptors

                  if (descriptorMap == null)
                  {
                     descriptorMap = new HashMap<String, String>();

                     for (String key : ((ProcessInstanceDetails) processInstance).getDescriptors().keySet())
                     {
                        if (!descriptorMap.containsKey(key))
                        {
                           descriptorMap.put(key, key);
                        }
                     }
                  }

                  JsonObject processInstanceJson = new JsonObject();

                  series.add(processInstanceJson);

                  processInstanceJson.addProperty("processId", processInstance.getProcessID());
                  processInstanceJson.addProperty("processName", processInstance.getProcessName());
                  processInstanceJson.addProperty("startTimestamp", processInstance.getStartTime().getTime());

                  if (processInstance.getTerminationTime() != null)
                  {
                     processInstanceJson.addProperty("terminationTimestamp", processInstance.getTerminationTime()
                           .getTime());
                  }

                  processInstanceJson.addProperty("state", processInstance.getState().getName());
                  processInstanceJson.addProperty("priority", processInstance.getPriority());

                  // Map descriptors

                  for (String key : descriptorMap.keySet())
                  {
                     addPrimitiveObjectToJsonObject(processInstanceJson, key,
                           ((ProcessInstanceDetails) processInstance).getDescriptorValue(key));
                  }

                  // Join external data

                  if (internalKey != null && processInstanceJson.has(internalKey)
                        && !processInstanceJson.get(internalKey).isJsonNull())
                  {
                     String internalKeValue = processInstanceJson.get(internalKey).getAsString();

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

                  addComputedColumns(dataSetJson.get("computedColumns").getAsJsonArray(), processInstanceJson);
               }
            }
            else
            {
               TimestampSeriesCumulator cumulator = new TimestampSeriesCumulator(getValueProvider(primaryObject, fact),
                     getValueProvider(primaryObject, firstDimension),
                     isDiscreteDimension(primaryObject, firstDimension), !fact.equals("count"));

               // Grouping criterion

               if (groupByCriterion != null)
               {
                  cumulator.setGroupCriterionProvider(getValueProvider(primaryObject, groupByCriterion));
               }

               JsonArray groupIds = new JsonArray();

               result.add("groupIds", groupIds);
               result.add("seriesGroup", cumulator.createCumulatedSeriesGroup((List) getQueryService()
                     .getAllProcessInstances(query), firstDimensionCumulationInterval, groupIds));
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

               for (ActivityInstance activityInstance : getQueryService().getAllActivityInstances(query))
               {
                  // Filter for unique descriptors

                  if (descriptorMap == null)
                  {
                     descriptorMap = new HashMap<String, String>();

                     for (String key : ((ProcessInstanceDetails) activityInstance.getProcessInstance())
                           .getDescriptors().keySet())
                     {
                        if (!descriptorMap.containsKey(key))
                        {
                           descriptorMap.put(key, key);
                        }
                     }
                  }

                  JsonObject activityInstanceJson = new JsonObject();

                  series.add(activityInstanceJson);

                  activityInstanceJson.addProperty("activityId", activityInstance.getActivity().getId());
                  activityInstanceJson.addProperty("activityName", activityInstance.getActivity().getName());
                  activityInstanceJson.addProperty("processId", activityInstance.getProcessInstance().getProcessID());
                  activityInstanceJson.addProperty("processName", activityInstance.getProcessInstance()
                        .getProcessName());
                  activityInstanceJson.addProperty("startTimestamp", activityInstance.getStartTime().getTime());
                  activityInstanceJson.addProperty("id", activityInstance.getActivity().getId());
                  activityInstanceJson.addProperty("state", activityInstance.getState().getName());
                  activityInstanceJson.addProperty("criticality", activityInstance.getCriticality());

                  if (activityInstance.getPerformedBy() != null)
                  {
                     activityInstanceJson.addProperty("userPerformerName", activityInstance.getPerformedBy().getName());
                  }

                  activityInstanceJson.addProperty("participantPerformerName",
                        activityInstance.getParticipantPerformerName());

                  // Map descriptors

                  for (String key : descriptorMap.keySet())
                  {
                     Object value = ((ProcessInstanceDetails) activityInstance.getProcessInstance())
                           .getDescriptorValue(key);

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
               TimestampSeriesCumulator cumulator = new TimestampSeriesCumulator(getValueProvider(primaryObject, fact),
                     getValueProvider(primaryObject, firstDimension),
                     isDiscreteDimension(primaryObject, firstDimension), !fact.equals("count"));

               // Grouping criterion

               if (groupByCriterion != null)
               {
                  cumulator.setGroupCriterionProvider(getValueProvider(primaryObject, groupByCriterion));
               }

               JsonArray groupIds = new JsonArray();

               result.add("groupIds", groupIds);
               result.add("seriesGroup", cumulator.createCumulatedSeriesGroup((List) getQueryService()
                     .getAllActivityInstances(query), firstDimensionCumulationInterval, groupIds));
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
    * TODO: This code should be completed looking at the implementation of Process
    * Instance Search in the Portal.
    * 
    * @throws ParseException
    * @throws UnsupportedFilterException
    * 
    */
   public void addProcessInstanceQueryFilters(ProcessInstanceQuery query, JsonArray filters, JsonObject parametersJson)
         throws UnsupportedFilterException, ParseException
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
            throw new IllegalArgumentException("Unexpected type for filter value of filter "
                  + filterJson.get("dimension").getAsString() + ".");
         }

         JsonElement parameterValueJson = null;

         // Overwrite by parameter, if parameter is set

         if (parametersJson.has("filters." + filterJson.get("dimension").getAsString()))
         {

            parameterValueJson = parametersJson.get("filters." + filterJson.get("dimension").getAsString())
                  .getAsJsonObject().get("value");
         }

         if (filterJson.get("dimension").getAsString().equals("processName"))
         {
            if (!valueJson.isJsonNull())
            {
               query.where(new ProcessDefinitionFilter(parameterValueJson == null || parameterValueJson.isJsonNull()
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
                  query.where(ProcessInstanceQuery.START_TIME.greaterOrEqual(dateFormat.parse(valueJson.getAsString())
                        .getTime()));

               }

               if (!valueJson.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(dateFormat.parse(valueJson.getAsString())
                        .getTime()));

               }
            }
            else if (filterJson.get("dimension").getAsString().equals("terminationTimestamp"))
            {
               if (!valueJson.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.TERMINATION_TIME.greaterOrEqual(dateFormat.parse(
                        valueJson.getAsString()).getTime()));

               }

               if (!valueJson.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.TERMINATION_TIME.lessOrEqual(dateFormat.parse(
                        valueJson.getAsString()).getTime()));
               }
            }
            else if (filterJson.get("dimension").getAsString().equals("priority"))
            {
               if (!valueJson.isJsonNull())
               {
                  if (filterJson.get("operator").getAsString().equals("equal"))
                  {
                     query.where(ProcessInstanceQuery.PRIORITY.isEqual(parameterValueJson == null
                           || parameterValueJson.isJsonNull() ? valueJson.getAsLong() : parameterValueJson.getAsLong()));
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
                     query.where(ProcessInstanceQuery.STARTING_USER_OID.isEqual(valueJson.getAsLong()));
                  }
               }
            }
            else if (filterJson.get("dimension").getAsString().equals("state"))
            {
               if (parameterValueJson != null && !parameterValueJson.isJsonNull())
               {
                  valuesJson = parameterValueJson.getAsJsonArray();
               }

               ProcessInstanceState[] processInstanceStates = new ProcessInstanceState[valuesJson.size()];

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
                     throw new IllegalArgumentException("State " + valuesJson.get(m).getAsString()
                           + " unknown for process instance state.");
                  }
               }

               ProcessStateFilter processStateFilter = new ProcessStateFilter(processInstanceStates);

               query.where(processStateFilter);
            }
            else
            {
               // Descriptors

               if (filterJson.get("operator").getAsString().equals("equal"))
               {
                  query.where(DataFilter.isEqual(filterJson.get("dimension").getAsString(), parameterValueJson == null
                        || parameterValueJson.isJsonNull() ? valueJson.getAsString() : parameterValueJson.getAsString()));
               }
               else if (filterJson.get("operator").getAsString().equals("equal"))
               {
                  query.where(DataFilter.notEqual(filterJson.get("dimension").getAsString(), parameterValueJson == null
                        || parameterValueJson.isJsonNull() ? valueJson.getAsString() : parameterValueJson.getAsString()));
               }
               else if (filterJson.get("operator").getAsString().equals("notEqual"))
               {
                  query.where(DataFilter.notEqual(filterJson.get("dimension").getAsString(), parameterValueJson == null
                        || parameterValueJson.isJsonNull() ? valueJson.getAsString() : parameterValueJson.getAsString()));
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
   public void addActivityInstanceQueryFilters(ActivityInstanceQuery query, JsonArray filters, JsonObject parametersJson)
         throws UnsupportedFilterException, ParseException
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
                  query.where(ActivityInstanceQuery.START_TIME.greaterOrEqual(dateFormat.parse(valueJson.getAsString())
                        .getTime()));

               }

               if (!valueJson.isJsonNull())
               {
                  query.where(ProcessInstanceQuery.START_TIME.lessOrEqual(dateFormat.parse(valueJson.getAsString())
                        .getTime()));

               }
            }
            else if (filterJson.get("dimension").getAsString().equals("lastModificationTimestamp"))
            {
               if (!valueJson.isJsonNull())
               {
                  query.where(ActivityInstanceQuery.LAST_MODIFICATION_TIME.greaterOrEqual(dateFormat.parse(
                        valueJson.getAsString()).getTime()));

               }

               if (!valueJson.isJsonNull())
               {
                  query.where(ActivityInstanceQuery.LAST_MODIFICATION_TIME.lessOrEqual(dateFormat.parse(
                        valueJson.getAsString()).getTime()));
               }
            }
            else if (filterJson.get("dimension").getAsString().equals("criticality"))
            {
               if (!valueJson.isJsonNull())
               {
                  if (filterJson.get("operator").getAsString().equals("equal"))
                  {
                     query.where(ActivityInstanceQuery.CRITICALITY.isEqual(valueJson.getAsLong()));
                  }
               }
            }
            else if (filterJson.get("dimension").getAsString().equals("participantPerformerName"))
            {
               if (!valueJson.isJsonNull())
               {
                  // TODO Find user by account and use OID
                  if (filterJson.get("operator").getAsString().equals("equals"))
                  {
                     query.where(ActivityInstanceQuery.CURRENT_PERFORMER_OID.isEqual(valueJson.getAsLong()));
                  }
               }
            }
            else if (filterJson.get("dimension").getAsString().equals("state"))
            {
               JsonArray valuesJson = valueJson.getAsJsonArray();
               ActivityInstanceState[] activityInstanceStates = new ActivityInstanceState[valuesJson.size()];

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
                     throw new IllegalArgumentException("State " + valuesJson.get(m).getAsString()
                           + " unknown for activity instance state.");
                  }
               }

               ActivityStateFilter activityStateFilter = new ActivityStateFilter(activityInstanceStates);

               query.where(activityStateFilter);

            }
            else
            {
               // Descriptors

               if (filterJson.get("operator").getAsString().equals("equal"))
               {
                  query.where(DataFilter.isEqual(filterJson.get("dimension").getAsString(), valueJson.getAsString()));
               }
               else if (filterJson.get("operator").getAsString().equals("equal"))
               {
                  query.where(DataFilter.notEqual(filterJson.get("dimension").getAsString(), valueJson.getAsString()));
               }
               else if (filterJson.get("operator").getAsString().equals("notEqual"))
               {
                  query.where(DataFilter.notEqual(filterJson.get("dimension").getAsString(), valueJson.getAsString()));
               }
            }
         }
      }

   }

   /**
    * Might be invoked for saving of multiple Report Definitions or directly (whereby json
    * contains a top-level element "report").
    * 
    * @param json
    */
   public JsonObject saveReportDefinition(JsonObject reportJson)
   {
      try
      {

         JsonObject storageJson = reportJson.get("storage").getAsJsonObject();
         String name = reportJson.get("name").getAsString();
         String location = storageJson.get("location").getAsString();

         Folder folder = null;

         if (location.equals("publicFolder"))
         {
            folder = findOrCreateFolder(PUBLIC_REPORT_DEFINITIONS_DIR);
         }
         else if (location.equals("personalFolder"))
         {
            folder = findOrCreateFolder(getUserDocumentFolderPath());
         }
         else if (location.equals("participantFolder"))
         {
            folder = findOrCreateFolder(getParticipantDocumentFolderPath(storageJson.get("participant").getAsString()));
         }

         // Mark Report Definition as saved

         reportJson.get("storage").getAsJsonObject().addProperty("state", "saved");

         saveReportDefinitionDocument(reportJson, folder, name);

         // Add to cache

         reportDefinitionJsons.put(folder.getPath() + "/" + name, reportJson);

         trace.debug(reportDefinitionJsons);

         return reportJson;
      }
      finally
      {
      }
   }

   /**
    * 
    * @param json
    */
   public void saveReportDefinitions(JsonObject json)
   {
      for (Map.Entry<String, JsonElement> entry : json.entrySet())
      {
         saveReportDefinition(entry.getValue().getAsJsonObject());
      }
   }

   /**
    * 
    * @param json
    */
   public void renameReportDefinition(String path, String name)
   {
      try
      {
         // Replace in cache

         JsonObject reportJson = reportDefinitionJsons.remove(path);

         reportDefinitionJsons.put(renameReportDefinitionDocument(path, name), reportJson);
      }
      finally
      {
      }
   }

   /**
    * 
    * @param json
    */
   public JsonObject loadReportDefinition(String path)
   {
      if (reportDefinitionJsons.get(path) != null)
      {
         return reportDefinitionJsons.get(path);
      }
      else
      {
         Document document = getDocumentManagementService().getDocument(path);

         if (document != null)
         {
            return jsonIo.readJsonObject(new String(getDocumentManagementService().retrieveDocumentContent(
                  document.getId())));
         }
         else
         {
            throw new ObjectNotFoundException("Document " + path + " does not exist.");
         }
      }
   }

   /**
    * 
    * @param json
    * @return
    */
   public JsonObject deleteReportDefinition(String reportPath)
   {
      try
      {
         trace.debug("deleting report template: " + reportPath);
         getDocumentManagementService().removeDocument(reportPath);
         return new JsonObject();
      }
      finally
      {
      }
   }

   /**
    * Returns the folder if exist otherwise create new folder
    * 
    * @param folderPath
    * @return
    */
   public Folder findOrCreateFolder(String folderPath)
   {
      Folder folder = getDocumentManagementService().getFolder(folderPath);

      if (null == folder)
      {
         // folder does not exist yet, create it
         String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
         String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

         if (StringUtils.isEmpty(parentPath))
         {
            // Top-level reached

            return getDocumentManagementService().createFolder("/", DmsUtils.createFolderInfo(childName));
         }
         else
         {
            Folder parentFolder = findOrCreateFolder(parentPath);

            return getDocumentManagementService().createFolder(parentFolder.getId(),
                  DmsUtils.createFolderInfo(childName));
         }
      }
      else
      {
         return folder;
      }
   }

   /**
    * 
    * @return
    */
   private String getUserDocumentFolderPath()
   {
      return "/realms/" + getUserService().getUser().getRealm().getId() + "/users/"
            + getUserService().getUser().getId() + "/documents/reports/designs";
   }

   /**
    * 
    * @return
    */
   private String getParticipantDocumentFolderPath(String participant)
   {
      return PARTICIPANTS_REPORT_DEFINITIONS_DIR + participant;
   }

   /**
    * TODO Split off persistence part
    * @param servletContext 
    * @param httpRequest 
    */
   public JsonObject loadReportDefinitions(ServletContext servletContext, HttpServletRequest httpRequest)
   {
      try
      { 
         Folder publicFolder = findOrCreateFolder(PUBLIC_REPORT_DEFINITIONS_DIR);
         Folder personalFolder = findOrCreateFolder(getUserDocumentFolderPath());
         Folder participantFolder = findOrCreateFolder(PARTICIPANTS_REPORT_DEFINITIONS_DIR);

         JsonObject rootFolderJson = new JsonObject();
         JsonArray subFoldersJson = new JsonArray();
         rootFolderJson.add("subFolders", subFoldersJson);

         subFoldersJson.add(getReportDefinitions(publicFolder, "Public Report Definitions")); // I18N
         subFoldersJson.add(getReportDefinitions(personalFolder, "Personal Report Definitions")); // I18N
         
         //Prepare Participants subfolders
         JsonObject participantsFolderJson = new JsonObject(); 
         participantsFolderJson.addProperty("name", "Participants Report Definitions"); // I18N
         participantsFolderJson.addProperty("id", participantFolder.getId());
         participantsFolderJson.addProperty("path", participantFolder.getPath());   
         
         subFoldersJson.add(participantsFolderJson); 
         
         List<Folder> subfolders = participantFolder.getFolders();
         
         PortalApplication pa = (PortalApplication) RestControllerUtils.resolveSpringBean("ippPortalApp",
               servletContext);
         User loggedInUser = pa.getLoggedInUser();

         //add relevant participants
         JsonArray participantFoldersJson = new JsonArray();
         participantsFolderJson.add("subFolders", participantFoldersJson);
         
         for (Folder participantSubFolder : subfolders)
         {
            participantSubFolder = findOrCreateFolder(participantSubFolder.getPath());

            // check the permissions to current user
            if (loggedInUser.isInRole(participantSubFolder.getName()) || loggedInUser.isAdministrator())
            {
               ModelCache modelCache = ModelCache.getModelCache(sessionContext, servletContext);
               Participant  participant = modelCache.getParticipant(participantSubFolder.getName(), null);
               
               JsonObject participantFolderJson = getReportDefinitions(participantSubFolder, participant.getName());
               
               if (participantFolderJson != null)
               {
                  participantFoldersJson.add(participantFolderJson);
               }
            }
         }
       

         return rootFolderJson;
      }
      catch (Exception e)
      {
         //TODO: remove later
         e.printStackTrace();
         trace.debug("Error Occurred while loading report definitions");
         return null;
      }
      finally
      {
      }
   }

   /**
    * @param folder
    * @param label
    * @return
    */
   private JsonObject getReportDefinitions(Folder folder, String label)
   {
      JsonObject folderJson = new JsonObject();

      if (StringUtils.isNotEmpty(label))
      {
         folderJson.addProperty("name", label);
         folderJson.addProperty("id", folder.getId());
         folderJson.addProperty("path", folder.getPath());
      }
      
      if (folder != null)
      {
         JsonArray reportDefinitionsJson = new JsonArray();
         folderJson.add("reportDefinitions", reportDefinitionsJson);

         @SuppressWarnings("unchecked")
         List<Document> candidateReportDefinitionsDocuments = folder.getDocuments();

         for (Document reportDefinitionDocument : candidateReportDefinitionsDocuments)
         {
            if (reportDefinitionDocument.getName().endsWith(".bpmrpt"))
            {
               String content = new String(getDocumentManagementService().retrieveDocumentContent(
                     reportDefinitionDocument.getId()));

               JsonObject reportDefinitionJson = jsonIo.readJsonObject(content);

               reportDefinitionsJson.add(reportDefinitionJson);
               reportDefinitionJson.addProperty("id", reportDefinitionDocument.getId());
               reportDefinitionJson.addProperty(
                     "name",
                     reportDefinitionDocument.getName().substring(0,
                           reportDefinitionDocument.getName().indexOf(".bpmrpt")));
               reportDefinitionJson.addProperty("path", reportDefinitionDocument.getPath());
            }
         }
      }

      return folderJson;
   }

   /**
	 *
	 */
   private void saveReportDefinitionDocument(JsonObject reportDefinitionJson, Folder folder, String name)
   {
      String reportContent = reportDefinitionJson.toString();
      String path = folder.getPath() + "/" + name + ".bpmrpt";
      Document reportDesignDocument = getDocumentManagementService().getDocument(path);

      if (null == reportDesignDocument)
      {
         DocumentInfo documentInfo = DmsUtils.createDocumentInfo(name + ".bpmrpt");

         documentInfo.setOwner(getServiceFactory().getWorkflowService().getUser().getAccount());
         documentInfo.setContentType(MimeTypesHelper.DEFAULT.getType());

         reportDesignDocument = getDocumentManagementService().createDocument(folder.getPath(), documentInfo,
               reportContent.getBytes(), null);

         // Create initial version

         // getDocumentManagementService().versionDocument(
         // reportDesignDocument.getId(), null);
      }
      else
      {
         getDocumentManagementService().updateDocument(reportDesignDocument, reportContent.getBytes(), null, false,
               null, false);
      }
   }

   /**
    * TODO Should be more elegant
    * 
    * @param path
    */
   private String renameReportDefinitionDocument(String path, String name)
   {
      Document reportDefinitionDocument = getDocumentManagementService().getDocument(path);
      String folderPath = path.substring(0, path.lastIndexOf('/'));
      DocumentInfo documentInfo = DmsUtils.createDocumentInfo(name + ".bpmrpt");

      documentInfo.setOwner(getServiceFactory().getWorkflowService().getUser().getAccount());
      documentInfo.setContentType(MimeTypesHelper.DEFAULT.getType());

      byte[] content = getDocumentManagementService().retrieveDocumentContent(path);

      getDocumentManagementService().createDocument(folderPath, documentInfo, content, null);
      getDocumentManagementService().removeDocument(reportDefinitionDocument.getId());

      return folderPath + "/" + name + ".bpmrpt";
   }

   /**
    * Retrieves external join data via REST and creates a map with the join key as key and
    * a map with all external fields and their 'useAs' field names as keys and their
    * values as values.
    * 
    * @param externalJoinJson
    * @return
    */
   public Map<String, Map<String, String>> retrieveExternalData(JsonObject externalJoinJson)
   {
      try
      {
         URL url = new URL(externalJoinJson.get("restUri").getAsString());
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();

         connection.setRequestMethod("GET");
         connection.setRequestProperty("Accept", "application/json");

         if (connection.getResponseCode() != 200)
         {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
         }

         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

         String output;
         StringBuffer buffer = new StringBuffer();

         while ((output = bufferedReader.readLine()) != null)
         {
            buffer.append(output);
         }

         connection.disconnect();

         // TODO Add heuristics on objects or arrays

         JsonArray recordsJson = jsonIo.readJsonObject(buffer.toString()).get("list").getAsJsonArray();

         trace.info("External Data:");
         trace.info(recordsJson.toString());

         Map<String, Map<String, String>> externalData = new HashMap<String, Map<String, String>>();
         JsonArray externalJoinFieldsJson = externalJoinJson.get("fields").getAsJsonArray();

         for (int n = 0; n < recordsJson.size(); n++)
         {
            JsonObject recordJson = recordsJson.get(n).getAsJsonObject();
            Map<String, String> record = new HashMap<String, String>();

            for (int m = 0; m < externalJoinFieldsJson.size(); m++)
            {
               JsonObject externalJoinFieldJson = externalJoinFieldsJson.get(m).getAsJsonObject();

               if (externalJoinFieldJson.get("id").getAsString()
                     .equals(externalJoinJson.get("externalKey").getAsString()))
               {
                  externalData.put(recordJson.get(externalJoinFieldJson.get("id").getAsString()).getAsString(), record);
               }

               // TODO Other type mapping than string (central mapping
               // function f(type,object, container))

               record.put(externalJoinFieldJson.get("useAs").getAsString(),
                     recordJson.get(externalJoinFieldJson.get("id").getAsString()).getAsString());
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

         addPrimitiveObjectToJsonObject(recordJson, computedColumn.get("id").getAsString(),
               evaluateComputedColumn(recordJson, computedColumn.get("formula").getAsString()));
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
            scope.put(entry.getKey(), convertJsonPrimitiveToObject(entry.getValue()));
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
}
