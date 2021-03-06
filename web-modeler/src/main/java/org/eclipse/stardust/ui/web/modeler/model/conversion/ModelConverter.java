package org.eclipse.stardust.ui.web.modeler.model.conversion;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.deepCopy;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.conversion.jto.DIDiagramJto;
import org.eclipse.stardust.ui.web.modeler.model.conversion.jto.DILaneJto;
import org.eclipse.stardust.ui.web.modeler.model.conversion.jto.DIPoolJto;
import org.eclipse.stardust.ui.web.modeler.model.conversion.jto.ModelEntryJto;
import org.eclipse.stardust.ui.web.modeler.model.conversion.jto.ProcessEntryJto;

public class ModelConverter
{
   private static final Logger trace = LogManager.getLogger(ModelConverter.class);

   private final JsonMarshaller jsonIo;

   private final RequestExecutor requestExecutor;

   public ModelConverter(JsonMarshaller jsonIo, RequestExecutor requestExecutor)
   {
      this.jsonIo = jsonIo;
      this.requestExecutor = requestExecutor;
   }

   public String convertModel(String modelId, String targetFormat)
   {
      JsonObject allModelsJson = requestExecutor.loadAllModels();

      String newModelId = null;
      JsonObject modelJson = allModelsJson.getAsJsonObject("loaded").getAsJsonObject(modelId);
      if (null != modelJson)
      {
         ModelEntryJto modelJto = jsonIo.gson().fromJson(modelJson, ModelEntryJto.class);

         assert ModelerConstants.MODEL_KEY.equals(modelJto.type);

         ModelConversionContext conversionContext = new ModelConversionContext(modelId, targetFormat);

         for (Map.Entry<String, JsonElement> processJson : modelJto.processes.entrySet())
         {
            ProcessEntryJto processJto = jsonIo.gson().fromJson(processJson.getValue(), ProcessEntryJto.class);

            assert ModelerConstants.PROCESS_KEY.equals(processJto.type);

            loadProcessDiagram(conversionContext, processJto);
         }

         newModelId = recreateModel(modelJto, conversionContext);
      }

      return newModelId;
   }

   private String recreateModel(ModelEntryJto modelJto,
         ModelConversionContext conversionContext)
   {
      JsonObject newModelReqJson = new JsonObject();
      newModelReqJson.addProperty("modelFormat", conversionContext.getTargetFormat());
      newModelReqJson.addProperty(ModelerConstants.NAME_PROPERTY, //
            modelJto.name + " - Clone");
      JsonObject newModelJson = applyCreateCommand(null, "model.create", newModelReqJson,
            ModelerConstants.MODEL_KEY);

      if (trace.isDebugEnabled())
      {
         trace.debug("Created model: " + newModelJson);
      }

      String modelUuid = extractAsString(newModelJson, "uuid");
      String modelId = extractAsString(newModelJson, "id");
      // TODO map new to old ID

      conversionContext.registerNewModelIdentifiers(modelId, modelUuid);

      trace.info("Created new model: " + modelUuid + " / " + modelId);

      for (Map.Entry<String, JsonElement> typeEntry : modelJto.typeDeclarations.entrySet())
      {
         JsonObject typeJson = typeEntry.getValue().getAsJsonObject();

         assert "typeDeclaration".equals(extractAsString(typeJson,
               ModelerConstants.TYPE_PROPERTY));

         // TODO map new to old ID
         //recreateTypeDeclaration(typeJson, conversionContext);
      }

      for (Map.Entry<String, JsonElement> variableEntry : modelJto.dataItems.entrySet())
      {
         JsonObject variableJson = variableEntry.getValue().getAsJsonObject();

         assert "data".equals(extractAsString(variableJson,
               ModelerConstants.TYPE_PROPERTY));

         // TODO map new to old ID
         recreateVariable(variableJson, conversionContext);
      }

      recreateParticipants(modelJto.participants, conversionContext);

      for (Map.Entry<String, JsonElement> processEntry : modelJto.processes.entrySet())
      {
         ProcessEntryJto processJto = jsonIo.gson().fromJson(processEntry.getValue(), ProcessEntryJto.class);

         assert ModelerConstants.PROCESS_KEY.equals(processJto.type);

         // TODO map new to old ID
         recreateProcess(modelId, processJto, conversionContext.forProcess(processJto.id));
      }

      return modelId;
   }

   private void recreateTypeDeclaration(JsonObject typeJson,
         ModelConversionContext modelConversionContext)
   {
      if (0 < System.currentTimeMillis())
      {
         throw new UnsupportedOperationException("Not yet implemented.");
      }

      JsonObject newTypeJson = new JsonObject();
      newTypeJson.addProperty(ModelerConstants.NAME_PROPERTY,
            extractAsString(typeJson, ModelerConstants.NAME_PROPERTY));

      // TODO handle non-primitive types as well
      newTypeJson.addProperty(ModelerConstants.PRIMITIVE_TYPE,
            extractAsString(typeJson, ModelerConstants.PRIMITIVE_TYPE));
      if (null == extractAsString(newTypeJson, ModelerConstants.PRIMITIVE_TYPE))
      {
         newTypeJson.addProperty(ModelerConstants.PRIMITIVE_TYPE,
               ModelerConstants.STRING_PRIMITIVE_DATA_TYPE);
      }
      JsonObject createVariableChanges = applyChange(modelConversionContext.newModelId(),
            "primitiveData.create", modelConversionContext.newModelId(), newTypeJson);

      if (trace.isDebugEnabled())
      {
         trace.debug("Create data response: " + createVariableChanges);
      }

      JsonArray newVariablesJson = createVariableChanges.getAsJsonArray("added");
      if (0 == newVariablesJson.size())
      {
         newVariablesJson = createVariableChanges.getAsJsonArray("modified");
      }
      String variableUuid = extractAsString(newVariablesJson.get(0).getAsJsonObject(),
            "uuid");
      String variableId = extractAsString(newVariablesJson.get(0).getAsJsonObject(), "id");

      modelConversionContext.registerNewDataId(extractString(typeJson, ModelerConstants.ID_PROPERTY), variableId);
   }

   private void recreateVariable(JsonObject variableJson,
         ModelConversionContext modelConversionContext)
   {
      JsonObject newVariableJson = new JsonObject();
      newVariableJson.addProperty(ModelerConstants.NAME_PROPERTY,
            extractAsString(variableJson, ModelerConstants.NAME_PROPERTY));

      String commandId;
      if (ModelerConstants.STRUCTURED_DATA_TYPE_KEY.equals(extractAsString(variableJson, ModelerConstants.DATA_TYPE_PROPERTY)))
      {
         commandId = "structuredData.create";

         if (variableJson.has(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
         {
            String structTypeFullId = extractString(variableJson,
                  ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY);
            newVariableJson.addProperty(
                  ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY,
                  modelConversionContext.newStructuredTypeFullId(structTypeFullId));
         }
      }
      // TODO handle other non-primitive types as well
      else
      {
         commandId = "primitiveData.create";

         newVariableJson.addProperty(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY,
               extractAsString(variableJson, ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY));
         if (null == extractAsString(newVariableJson, ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY))
         {
            newVariableJson.addProperty(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY,
                  ModelerConstants.STRING_PRIMITIVE_DATA_TYPE);
         }

         // HACK XPDL's create primitive uses this property name
         newVariableJson.addProperty(
               ModelerConstants.PRIMITIVE_TYPE,
               extractString(newVariableJson,
                     ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY));
      }
      JsonObject createVariableChanges = applyChange(modelConversionContext.newModelId(),
            commandId, modelConversionContext.newModelId(), newVariableJson);

      if (trace.isDebugEnabled())
      {
         trace.debug("Create data response: " + createVariableChanges);
      }

      JsonArray newVariablesJson = createVariableChanges.getAsJsonArray("added");
      if (0 == newVariablesJson.size())
      {
         newVariablesJson = createVariableChanges.getAsJsonArray("modified");
      }
      String variableUuid = extractAsString(newVariablesJson.get(0).getAsJsonObject(),
            "uuid");
      String variableId = extractAsString(newVariablesJson.get(0).getAsJsonObject(), "id");

      modelConversionContext.registerNewDataId(extractString(variableJson, ModelerConstants.ID_PROPERTY), variableId);
   }

   private void recreateParticipants(JsonObject participantsJson,
         ModelConversionContext conversionContext)
   {
      List<JsonObject> pendingParticipants = newArrayList();

      for (Map.Entry<String, JsonElement> participantEntry : participantsJson.entrySet())
      {
         JsonObject participantJson = participantEntry.getValue().getAsJsonObject();

         String participantType = extractAsString(participantJson,
               ModelerConstants.TYPE_PROPERTY);
         assert ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY.equals(participantType)
               || ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY
                     .equals(participantType)
               || ModelerConstants.TEAM_LEADER_TYPE_KEY.equals(participantType)
               || ModelerConstants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE_KEY
                     .equals(participantType);

         pendingParticipants.add(participantJson);
      }

      Set<String> convertedParticipants = newHashSet();
      List<JsonObject> pendingTeamLeads = newArrayList();
      Map<String, String> idToUuidMapping = newHashMap();
      while ( !pendingParticipants.isEmpty())
      {
         for (JsonObject participantJson : pendingParticipants)
         {
            // convert participants starting from root of inheritance hierarchy
            if (!participantJson.has(ModelerConstants.PARENT_UUID_PROPERTY)
                  || convertedParticipants.contains(extractString(participantJson,
                        ModelerConstants.PARENT_UUID_PROPERTY)))
            {
               // TODO map new to old ID
               recreateParticipant(participantJson, conversionContext);

               if (participantJson.has(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY))
               {
                  pendingTeamLeads.add(participantJson);
               }

               convertedParticipants.add(extractString(participantJson,
                     ModelerConstants.UUID_PROPERTY));
               idToUuidMapping.put(
                     extractString(participantJson, ModelerConstants.ID_PROPERTY),
                     extractString(participantJson, ModelerConstants.UUID_PROPERTY));

               if (participantJson.has(ModelerConstants.CHILD_PARTICIPANTS_KEY))
               {
                  JsonArray childParticipants = participantJson.getAsJsonArray(ModelerConstants.CHILD_PARTICIPANTS_KEY);
                  for (JsonElement childParticipant : childParticipants)
                  {
                     pendingParticipants.add(childParticipant.getAsJsonObject());
                  }
               }

               pendingParticipants.remove(participantJson);
               break;
            }
         }
      }

      for (JsonObject pendingTeamLead : pendingTeamLeads)
      {
         String originalTeamLeadId = extractString(pendingTeamLead,
               ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY);
         if (-1 != originalTeamLeadId.indexOf(":"))
         {
            originalTeamLeadId = originalTeamLeadId.substring(originalTeamLeadId
                  .indexOf(":") + 1);
         }

         JsonObject newTeamLeadJson = new JsonObject();
         newTeamLeadJson.addProperty(ModelerConstants.UUID_PROPERTY, conversionContext
               .newParticipantUuid(idToUuidMapping.get(originalTeamLeadId)));

         JsonObject updatedTeamJson = applyChange(conversionContext.newModelId(),
               "organization.updateTeamLeader",
               conversionContext.newElementOid(extractLong(pendingTeamLead,
                     ModelerConstants.OID_PROPERTY)), newTeamLeadJson);

         if (trace.isDebugEnabled())
         {
            trace.debug("Update team-lead response: " + updatedTeamJson);
         }
      }
   }

   private void recreateParticipant(JsonObject participantJson,
         ModelConversionContext modelConversionContext)
   {
      JsonObject newParticipantJson = new JsonObject();
      newParticipantJson.addProperty(ModelerConstants.NAME_PROPERTY,
            extractAsString(participantJson, ModelerConstants.NAME_PROPERTY));

      String createCommand = null;
      String participantType = extractString(participantJson, ModelerConstants.TYPE_PROPERTY);
      if (ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY.equals(participantType))
      {
         createCommand = "role.create";
      }
      else if (ModelerConstants.TEAM_LEADER_TYPE_KEY.equals(participantType))
      {
         createCommand = "role.create";
         participantType = ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY;
      }
      else if (ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY.equals(participantType))
      {
         createCommand = "organization.create";
      }
      else if (ModelerConstants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE_KEY.equals(participantType))
      {
         createCommand = "conditionalPerformer.create";
      }
      else
      {
         throw new IllegalArgumentException("Unexpected participant type: " + participantType);
      }

      String contextElementId = modelConversionContext.newModelId();
      if (participantJson.has(ModelerConstants.PARENT_UUID_PROPERTY))
      {
         // resolve new parent UUID
         contextElementId = modelConversionContext.newParticipantUuid(extractString(
               participantJson, ModelerConstants.PARENT_UUID_PROPERTY));
      }

      JsonObject newParticipantJson2 = applyCreateCommand(
            modelConversionContext.newModelId(), createCommand, contextElementId,
            newParticipantJson, participantType);

      if (trace.isDebugEnabled())
      {
         trace.debug("Create participant response: " + newParticipantJson2);
      }

      String newParticipantUuid = extractAsString(newParticipantJson2, ModelerConstants.UUID_PROPERTY);
      String newParticipantId = extractAsString(newParticipantJson2, ModelerConstants.ID_PROPERTY);
      long newParticipantOid = extractLong(newParticipantJson2, ModelerConstants.OID_PROPERTY);

      modelConversionContext.registerNewParticipantId(
            extractString(participantJson, ModelerConstants.ID_PROPERTY),
            newParticipantId);
      modelConversionContext.registerNewParticipantUuid(
            extractString(participantJson, ModelerConstants.UUID_PROPERTY),
            newParticipantUuid);
      modelConversionContext.registerNewElementOid(
            extractLong(participantJson, ModelerConstants.OID_PROPERTY),
            newParticipantOid);
   }

   private void recreateProcess(String newModelId, ProcessEntryJto processJto,
         ProcessConversionContext processConversionContext)
   {
      JsonObject newProcessJson = new JsonObject();
      newProcessJson.addProperty(ModelerConstants.NAME_PROPERTY, processJto.name);
      newProcessJson.addProperty("defaultPoolName", "Default Pool");
      newProcessJson.addProperty("defaultLaneName", "Default Lane");
      JsonObject createProcessChanges = applyChange(newModelId, "process.create",
            processConversionContext.newModelId(), newProcessJson);

      if (trace.isDebugEnabled())
      {
         trace.debug("Create process response: " + createProcessChanges);
      }

      String newProcessUuid = null;
      String newProcessId = null;
      JsonObject newPoolJson = new JsonObject();
      JsonObject newLaneJson = new JsonObject();
      for (JsonElement addedElement : createProcessChanges.getAsJsonArray("added"))
      {
         if (ModelerConstants.PROCESS_KEY.equals(addedElement.getAsJsonObject()))
         {
            newProcessUuid = extractString(addedElement.getAsJsonObject(), ModelerConstants.UUID_PROPERTY);
            newProcessId = extractAsString(addedElement.getAsJsonObject(), ModelerConstants.ID_PROPERTY);
         }
         else if (addedElement.getAsJsonObject().has(ModelerConstants.POOL_SYMBOLS))
         {
            newPoolJson = addedElement.getAsJsonObject()
                  .getAsJsonObject(ModelerConstants.POOL_SYMBOLS)
                  .entrySet().iterator().next().getValue()
                  .getAsJsonObject();

            newLaneJson = newPoolJson
                  .getAsJsonArray(ModelerConstants.LANE_SYMBOLS)
                  .get(0)
                  .getAsJsonObject();
         }
      }

      // recreate diagram

      List<JsonObject> processDiagrams = processConversionContext.retrieveProcessDiagrams();
      JsonObject diagramJson = !processDiagrams.isEmpty()
            ? processDiagrams.get(0)
            : new JsonObject();

      DIDiagramJto diagramJto = jsonIo.gson().fromJson(diagramJson, DIDiagramJto.class);

      if ((null == diagramJto.poolSymbols) || diagramJto.poolSymbols.entrySet().isEmpty())
      {
         return;
      }

      // TODO recreate diagram
      for (Map.Entry<String, JsonElement> poolJson : diagramJto.poolSymbols.entrySet())
      {
         DIPoolJto poolJto = jsonIo.gson().fromJson(poolJson.getValue(), DIPoolJto.class);

         if (isEmpty(poolJto.processId) || !poolJto.processId.equals(processJto.id))
         {
            // pool does not belong to this process
            // TODO revisit once we support collaboration diagrams
            continue;
         }

         if ((null == poolJto.laneSymbols) || (0 == poolJto.laneSymbols.size()))
         {
            continue;
         }

         for (JsonElement laneJson : poolJto.laneSymbols)
         {
            DILaneJto laneJto = jsonIo.gson().fromJson(laneJson, DILaneJto.class);

            JsonObject newPoolPatchJson = new JsonObject();
            newPoolPatchJson.add(ModelerConstants.X_PROPERTY, poolJto.x);
            newPoolPatchJson.add(ModelerConstants.Y_PROPERTY, poolJto.y);
            newPoolPatchJson.add(ModelerConstants.WIDTH_PROPERTY, poolJto.width);
            newPoolPatchJson.add(ModelerConstants.HEIGHT_PROPERTY, poolJto.height);
            newPoolPatchJson.add(ModelerConstants.ORIENTATION_PROPERTY, poolJto.orientation);
            applyChange(newModelId, "modelElement.update",
                  extractLong(newPoolJson, ModelerConstants.OID_PROPERTY),
                  newPoolPatchJson);

            JsonObject newLanePatchJson = new JsonObject();
            newLanePatchJson.add(ModelerConstants.X_PROPERTY, laneJto.x);
            newLanePatchJson.add(ModelerConstants.Y_PROPERTY, laneJto.y);
            newLanePatchJson.add(ModelerConstants.WIDTH_PROPERTY, laneJto.width);
            newLanePatchJson.add(ModelerConstants.HEIGHT_PROPERTY, laneJto.height);
            newLanePatchJson.add(ModelerConstants.ORIENTATION_PROPERTY, laneJto.orientation);
//               newLanePatchJson.addProperty(ModelerConstants.PARENT_SYMBOL_ID_PROPERTY, extractString(newPoolJson, ModelerConstants.ID_PROPERTY));
            applyChange(newModelId, "modelElement.update",
                  extractLong(newLaneJson, ModelerConstants.OID_PROPERTY),
                  newLanePatchJson);

            for (Map.Entry<String, JsonElement> nodeSymbolJson : laneJto.activitySymbols
                  .entrySet())
            {
               recreateActivitySymbol(newModelId, newLaneJson, nodeSymbolJson.getValue()
                     .getAsJsonObject(), processConversionContext);
            }
            for (Map.Entry<String, JsonElement> nodeSymbolJson : laneJto.gatewaySymbols
                  .entrySet())
            {
               recreateGatewaySymbol(newModelId, newLaneJson, nodeSymbolJson.getValue()
                     .getAsJsonObject(), processConversionContext);
            }
            for (Map.Entry<String, JsonElement> nodeSymbolJson : laneJto.eventSymbols
                  .entrySet())
            {
               recreateEventSymbol(newModelId, newLaneJson, nodeSymbolJson.getValue()
                     .getAsJsonObject(), processConversionContext);
            }
            for (Map.Entry<String, JsonElement> nodeSymbolJson : laneJto.dataSymbols
                  .entrySet())
            {
               recreateDataSymbol(newLaneJson, nodeSymbolJson.getValue()
                     .getAsJsonObject(), processConversionContext);
            }

            // TODO recurse into sub-lanes
         }

         for (Map.Entry<String, JsonElement> connectionSymbolJson : diagramJto.connections.entrySet())
         {
            Long fromElementOid = extractLong(connectionSymbolJson.getValue()
                  .getAsJsonObject(), ModelerConstants.FROM_MODEL_ELEMENT_OID);
            Long toElementOid = extractLong(connectionSymbolJson.getValue()
                  .getAsJsonObject(), ModelerConstants.TO_MODEL_ELEMENT_OID);
            if (null == toElementOid)
            {
               // HACK end event OIDs are encoded as String
               String oidString = extractString(connectionSymbolJson.getValue().getAsJsonObject(),
                     ModelerConstants.TO_MODEL_ELEMENT_OID);
               if (!isEmpty(oidString))
               {
                  toElementOid = Long.parseLong(oidString);
               }
            }

            if (processConversionContext.hasNewElementOid(fromElementOid)
                  && processConversionContext.hasNewElementOid(toElementOid))
            {
               recreateConnectionSymbol(newModelId, newPoolJson, connectionSymbolJson
                     .getValue().getAsJsonObject(), processConversionContext.forModel());
            }
         }
      }
   }

   private void loadProcessDiagram(ModelConversionContext conversionContext,
         ProcessEntryJto processJto)
   {
      // load associated diagram

      JsonObject diagramJson = requestExecutor.loadProcessDiagram(processJto.modelId, processJto.id);

      if (trace.isDebugEnabled())
      {
         trace.debug("Diagram for process " + processJto.id + ": " + diagramJson);
      }

      DIDiagramJto diagramJto = jsonIo.gson().fromJson(diagramJson, DIDiagramJto.class);

      if (null == diagramJto.poolSymbols)
      {
         return;
      }

      // parse diagram, find contained processes
      for (Map.Entry<String, JsonElement> poolJson : diagramJto.poolSymbols.entrySet())
      {
         DIPoolJto poolJto = jsonIo.gson().fromJson(poolJson.getValue(), DIPoolJto.class);

         if ( !isEmpty(poolJto.processId))
         {
            conversionContext.forProcess(poolJto.processId).registerDiagramForProcess(diagramJson);
         }
      }
   }

   private void recreateActivitySymbol(String newModelId, JsonObject laneSymbolJson,
         JsonObject activitySymbolJson, ProcessConversionContext conversionContext)
   {
      activitySymbolJson = deepCopy(activitySymbolJson);

      JsonObject activityJson = activitySymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
      if ( !activityJson.has(ModelerConstants.ACTIVITY_TYPE))
      {
         activityJson.addProperty(ModelerConstants.ACTIVITY_TYPE, ModelerConstants.TASK_ACTIVITY);
      }
      if (ModelerConstants.TASK_ACTIVITY.equals(extractString(activityJson,
            ModelerConstants.ACTIVITY_TYPE)))
      {
         if ( !activityJson.has(ModelerConstants.TASK_TYPE))
         {
            activityJson.addProperty(ModelerConstants.TASK_TYPE, ModelerConstants.NONE_TASK_KEY);
         }
         else if ( !ModelerConstants.NONE_TASK_KEY.equals(extractString(activityJson, ModelerConstants.TASK_TYPE)))
         {
            if ( !activityJson.has(ModelerConstants.APPLICATION_FULL_ID_PROPERTY))
            {
               // no valid application, reset to None task
               activityJson.addProperty(ModelerConstants.TASK_TYPE, ModelerConstants.NONE_TASK_KEY);
            }
         }
      }

      // remove ID to trigger creation of underlying activity
      activityJson.remove(ModelerConstants.ID_PROPERTY);

      JsonObject changesJson = applyChange(newModelId, "activitySymbol.create",
            extractLong(laneSymbolJson, ModelerConstants.OID_PROPERTY), //
            activitySymbolJson);

      mapNewElementIdentifiers(activitySymbolJson, changesJson, conversionContext.forModel());

      // push element update to fully sync properties
      for (JsonElement addedElementJson : changesJson.getAsJsonArray("added"))
      {
         if (ModelerConstants.ACTIVITY_SYMBOL.equals(extractString(
               addedElementJson.getAsJsonObject(), ModelerConstants.TYPE_PROPERTY)))
         {
            activitySymbolJson.addProperty(ModelerConstants.PARENT_SYMBOL_ID_PROPERTY,
                  extractString(laneSymbolJson, ModelerConstants.ID_PROPERTY));

            applyChange(newModelId, "modelElement.update",
                  conversionContext.newElementOid(extractLong(activitySymbolJson,
                        ModelerConstants.OID_PROPERTY)), activitySymbolJson);
         }
      }
   }

   private void recreateGatewaySymbol(String newModelId, JsonObject laneSymbolJson,
         JsonObject gatewaySymbolJson, ProcessConversionContext conversionContext)
   {
      gatewaySymbolJson = deepCopy(gatewaySymbolJson);

      JsonObject gatewayJson = gatewaySymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
      if ( !gatewayJson.has(ModelerConstants.GATEWAY_TYPE_PROPERTY))
      {
         gatewayJson.addProperty(ModelerConstants.GATEWAY_TYPE_PROPERTY, ModelerConstants.XOR_GATEWAY_TYPE);

      }

      // remove ID to trigger creation of underlying gateway
      gatewayJson.remove(ModelerConstants.ID_PROPERTY);

      gatewaySymbolJson.addProperty(ModelerConstants.PARENT_SYMBOL_ID_PROPERTY,
            extractString(laneSymbolJson, ModelerConstants.ID_PROPERTY));

      JsonObject changesJson = applyChange(newModelId, "gateSymbol.create",
            extractLong(laneSymbolJson, ModelerConstants.OID_PROPERTY), gatewaySymbolJson);

      mapNewElementIdentifiers(gatewaySymbolJson, changesJson, conversionContext.forModel());
   }

   private void recreateEventSymbol(String newModelId, JsonObject laneSymbolJson,
         JsonObject eventSymbolJson, ProcessConversionContext conversionContext)
   {
      eventSymbolJson = deepCopy(eventSymbolJson);

      JsonObject eventJson = eventSymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      // remove ID to trigger creation of underlying event
      eventJson.remove(ModelerConstants.ID_PROPERTY);

      eventSymbolJson.addProperty(ModelerConstants.PARENT_SYMBOL_ID_PROPERTY,
            extractString(laneSymbolJson, ModelerConstants.ID_PROPERTY));

      JsonObject changesJson = applyChange(newModelId, "eventSymbol.create",
            extractLong(laneSymbolJson, ModelerConstants.OID_PROPERTY), eventSymbolJson);

      mapNewElementIdentifiers(eventSymbolJson, changesJson, conversionContext.forModel());

      // push element update to fully sync properties
      for (JsonElement addedElementJson : changesJson.getAsJsonArray("added"))
      {
         if (ModelerConstants.EVENT_SYMBOL.equals(extractString(
               addedElementJson.getAsJsonObject(), ModelerConstants.TYPE_PROPERTY)))
         {
            applyChange(newModelId, "modelElement.update",
                  conversionContext.newElementOid(extractLong(eventSymbolJson,
                        ModelerConstants.OID_PROPERTY)), eventSymbolJson);
         }
      }
   }

   private void recreateDataSymbol(JsonObject laneSymbolJson, JsonObject dataSymbolJson,
         ProcessConversionContext conversionContext)
   {
      dataSymbolJson = deepCopy(dataSymbolJson);

      dataSymbolJson.addProperty(ModelerConstants.PARENT_SYMBOL_ID_PROPERTY,
            extractString(laneSymbolJson, ModelerConstants.ID_PROPERTY));

      // translate dataId, dataFullId
      String originalDataId = extractString(dataSymbolJson, ModelerConstants.DATA_ID_PROPERTY);
      if (isEmpty(originalDataId))
      {
         originalDataId = extractString(dataSymbolJson, ModelerConstants.DATA_FULL_ID_PROPERTY);
         originalDataId = originalDataId.substring(originalDataId.indexOf(":") + 1);
      }
      dataSymbolJson.addProperty(ModelerConstants.DATA_ID_PROPERTY,
            conversionContext.newDataId(originalDataId));
      dataSymbolJson.addProperty(ModelerConstants.DATA_FULL_ID_PROPERTY,
            conversionContext.newModelId() + ":" + conversionContext.newDataId(originalDataId));

      JsonObject changesJson = applyChange(conversionContext.newModelId(),
            "dataSymbol.create",
            extractLong(laneSymbolJson, ModelerConstants.OID_PROPERTY), //
            dataSymbolJson);

      mapNewElementIdentifiers(dataSymbolJson, changesJson, conversionContext.forModel());
   }

   private void mapNewElementIdentifiers(JsonObject oldSymbolJson,
         JsonObject changesJson, ModelConversionContext modelConversionContext)
   {
      String symbolType = extractString(oldSymbolJson, ModelerConstants.TYPE_PROPERTY);

      long oldSymbolOid = extractLong(oldSymbolJson, ModelerConstants.OID_PROPERTY);
      for (JsonElement addedElementJson : changesJson.getAsJsonArray("added"))
      {
         if (symbolType.equals(extractString(
               addedElementJson.getAsJsonObject(), ModelerConstants.TYPE_PROPERTY)))
         {
            long newSymbolOid = extractLong(addedElementJson.getAsJsonObject(),
                  ModelerConstants.OID_PROPERTY);

            if (trace.isDebugEnabled())
            {
               trace.debug("Mapping " + symbolType + ": " + oldSymbolOid + " => "
                     + newSymbolOid);
            }
            modelConversionContext.registerNewElementOid(oldSymbolOid, newSymbolOid);

            if (oldSymbolJson.has(ModelerConstants.MODEL_ELEMENT_PROPERTY)
                  && addedElementJson.getAsJsonObject().has(
                        ModelerConstants.MODEL_ELEMENT_PROPERTY))
            {
               String elementType = extractString(oldSymbolJson,
                     ModelerConstants.MODEL_ELEMENT_PROPERTY,
                     ModelerConstants.TYPE_PROPERTY);

               Long oldElementOid = extractLong(oldSymbolJson,
                     ModelerConstants.MODEL_ELEMENT_PROPERTY,
                     ModelerConstants.OID_PROPERTY);
               Long newElementOid = extractLong(addedElementJson.getAsJsonObject(),
                     ModelerConstants.MODEL_ELEMENT_PROPERTY,
                     ModelerConstants.OID_PROPERTY);

               if ((null != oldElementOid) && (null != newElementOid))
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Mapping " + elementType + ": " + oldElementOid + " => "
                           + newElementOid);
                  }
                  modelConversionContext.registerNewElementOid(oldElementOid, newElementOid);
               }
            }
         }
      }
   }

   private void recreateConnectionSymbol(String newModelId, JsonObject poolSymbolJson,
         JsonObject connectionSymbolJson, ModelConversionContext conversionContext)
   {
      JsonObject  connectionJson = connectionSymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      String oldElementId = extractString(connectionJson, ModelerConstants.ID_PROPERTY);
      connectionJson.remove(ModelerConstants.ID_PROPERTY);

      Long fromElementOid = extractLong(connectionSymbolJson,
            ModelerConstants.FROM_MODEL_ELEMENT_OID);
      Long toElementOid = extractLong(connectionSymbolJson,
            ModelerConstants.TO_MODEL_ELEMENT_OID);
      if (null == toElementOid)
      {
         // HACK end event OIDs are encoded as String
         String oidString = extractString(connectionSymbolJson,
               ModelerConstants.TO_MODEL_ELEMENT_OID);
         if (!isEmpty(oidString))
         {
            toElementOid = Long.parseLong(oidString);
         }
      }

      Long newSourceSymbolOid = conversionContext.newElementOid(fromElementOid);
      Long newTargetSymbolOid = conversionContext.newElementOid(toElementOid);

      connectionSymbolJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
            newSourceSymbolOid);
      connectionSymbolJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_OID,
            newTargetSymbolOid);

      JsonObject changesJson = applyChange(newModelId, "connection.create",
            extractLong(poolSymbolJson, ModelerConstants.OID_PROPERTY), //
            connectionSymbolJson);

      if ( !isEmpty(oldElementId))
      {
         connectionJson.addProperty(ModelerConstants.ID_PROPERTY, oldElementId);
      }

      String symbolType = extractString(connectionSymbolJson, ModelerConstants.TYPE_PROPERTY);
//      long oldConnectionOid = extractLong(connectionJson, ModelerConstants.OID_PROPERTY);
      for (JsonElement addedElementJson : changesJson.getAsJsonArray("added"))
      {
         if (symbolType.equals(extractString(
               addedElementJson.getAsJsonObject(), ModelerConstants.TYPE_PROPERTY)))
         {
            long newConnectionSymbolOid = extractLong(addedElementJson.getAsJsonObject(),
                  ModelerConstants.OID_PROPERTY);
//            long newConnectionOid = extractLong(addedElementJson.getAsJsonObject(),
//                  ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.OID_PROPERTY);
//
//            if (trace.isDebugEnabled())
//            {
//               trace.debug("Mapping " + symbolType + ": " + oldConnectionOid + " => "
//                     + newConnectionOid);
//            }
            applyChange(newModelId, "modelElement.update", newConnectionSymbolOid,
                  connectionSymbolJson);
         }
      }
   }

   private JsonObject applyCreateCommand(String modelUuid, String commandId,
         JsonObject changeJson, String expectedResult)
   {
      return applyCreateCommand(modelUuid, commandId, null, changeJson, expectedResult);
   }

   private JsonObject applyCreateCommand(String modelUuid, String commandId, Object contextElementId,
         JsonObject changeJson, String expectedResult)
   {
      JsonObject responseJson = doApplyChange(modelUuid, commandId, contextElementId, changeJson);

      if (trace.isDebugEnabled())
      {
         trace.debug("Create '" + expectedResult + "' response: " + responseJson);
      }

      JsonArray addedElementsJson = responseJson.getAsJsonArray("added");
      for (JsonElement addedElementJson : addedElementsJson)
      {
         if (addedElementJson.isJsonObject()
               && addedElementJson.getAsJsonObject().has(ModelerConstants.TYPE_PROPERTY)
               && expectedResult.equals(extractString(addedElementJson.getAsJsonObject(),
                     ModelerConstants.TYPE_PROPERTY)))
         {
            return addedElementJson.getAsJsonObject();
         }
      }

      throw new InternalException("No element of type '" + expectedResult
            + "' was created.");
   }

   private JsonObject applyChange(String modelId, String commandId,
         String contextElementId, JsonObject changeJson)
   {
      return doApplyChange(modelId, commandId, contextElementId, changeJson);
   }

   private JsonObject applyChange(String modelId, String commandId,
         long contextElementId, JsonObject changeJson)
   {
      return doApplyChange(modelId, commandId, contextElementId, changeJson);
   }

   private JsonObject doApplyChange(String modelId, String commandId,
         Object contextElementId, JsonObject changeJson)
   {
      JsonObject cmdJson = new JsonObject();
      if ( !isEmpty(modelId))
      {
         cmdJson.addProperty("modelId", modelId);
      }
      cmdJson.addProperty("commandId", commandId);
      cmdJson.add("changeDescriptions", new JsonArray());
      ((JsonArray) cmdJson.get("changeDescriptions")).add(new JsonObject());
      if (contextElementId instanceof String)
      {
         if (modelId.equals(contextElementId))
         {
            ((JsonObject) ((JsonArray) cmdJson.get("changeDescriptions")).get(0)).addProperty(
                  ModelerConstants.OID_PROPERTY, (String) contextElementId);
         }
         else
         {
            ((JsonObject) ((JsonArray) cmdJson.get("changeDescriptions")).get(0)).addProperty(
                  ModelerConstants.UUID_PROPERTY, (String) contextElementId);
         }
      }
      else if (contextElementId instanceof Long)
      {
         ((JsonObject) ((JsonArray) cmdJson.get("changeDescriptions")).get(0)).addProperty(
               ModelerConstants.OID_PROPERTY, (Long) contextElementId);
      }
      ((JsonObject) ((JsonArray) cmdJson.get("changeDescriptions")).get(0)).add(
            "changes", changeJson);

      String idBackup = extractString(changeJson, ModelerConstants.ID_PROPERTY);
      if ( !isEmpty(idBackup))
      {
         changeJson.remove(ModelerConstants.ID_PROPERTY);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Applying change: " + cmdJson);
      }

      try
      {
         JsonObject responseJson = requestExecutor.applyChange(cmdJson);
         return responseJson.getAsJsonObject("changes");
      }
      catch (Exception e)
      {
         trace.warn("Failed applying change.", e);

         return null;
      }
      finally
      {
         if ( !isEmpty(idBackup))
         {
            changeJson.addProperty(ModelerConstants.ID_PROPERTY, idBackup);
         }
      }
   }
}
