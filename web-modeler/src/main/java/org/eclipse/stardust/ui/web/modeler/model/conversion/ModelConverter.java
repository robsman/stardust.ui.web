package org.eclipse.stardust.ui.web.modeler.model.conversion;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.deepCopy;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
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

   public String convertModel(String modelId)
   {
      JsonObject allModelsJson = requestExecutor.loadAllModels();

      String newModelId = null;
      JsonObject modelJson = allModelsJson.getAsJsonObject("loaded").getAsJsonObject(modelId);
      if (null != modelJson)
      {
         ModelEntryJto modelJto = jsonIo.gson().fromJson(modelJson, ModelEntryJto.class);

         assert ModelerConstants.MODEL_KEY.equals(modelJto.type);

         ModelConversionContext conversionContext = new ModelConversionContext();

         for (JsonElement processJson : modelJto.processes)
         {
            ProcessEntryJto processJto = jsonIo.gson().fromJson(processJson, ProcessEntryJto.class);

            assert ModelerConstants.PROCESS_KEY.equals(processJto.type);

            buildFlowNodeToProcessMapping(conversionContext, processJto);

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

      conversionContext.registerNewModelId(modelId);

      trace.info("Created new model: " + modelUuid + " / " + modelId);

      for (JsonElement variableEntry : modelJto.dataItems)
      {
         JsonObject variableJson = variableEntry.getAsJsonObject();

         assert "data".equals(extractAsString(variableJson,
               ModelerConstants.TYPE_PROPERTY));

         // TODO map new to old ID
         recreateVariable(modelId, variableJson, conversionContext);
      }

      // TODO recreate participants

      for (JsonElement processEntry : modelJto.processes)
      {
         ProcessEntryJto processJto = jsonIo.gson().fromJson(processEntry, ProcessEntryJto.class);

         assert ModelerConstants.PROCESS_KEY.equals(processJto.type);

         // TODO map new to old ID
         recreateProcess(modelId, processJto, conversionContext.forProcess(processJto.id));
      }

      return modelId;
   }

   private void recreateVariable(String modelId, JsonObject variableJson,
         ModelConversionContext modelConversionContext)
   {
      JsonObject newVariableJson = new JsonObject();
      newVariableJson.addProperty(ModelerConstants.NAME_PROPERTY,
            extractAsString(variableJson, ModelerConstants.NAME_PROPERTY));

      // TODO handle non-primitive types as well
      newVariableJson.addProperty(ModelerConstants.PRIMITIVE_TYPE,
            extractAsString(variableJson, ModelerConstants.PRIMITIVE_TYPE));
      if (null == extractAsString(newVariableJson, ModelerConstants.PRIMITIVE_TYPE))
      {
         newVariableJson.addProperty(ModelerConstants.PRIMITIVE_TYPE,
               ModelerConstants.STRING_PRIMITIVE_DATA_TYPE);
      }
      JsonObject createVariableChanges = applyChange(modelId, "primitiveData.create",
            modelId, newVariableJson);

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

   private void recreateProcess(String newModelId, ProcessEntryJto processJto,
         ProcessConversionContext processConversionContext)
   {
      JsonObject newProcessJson = new JsonObject();
      newProcessJson.addProperty(ModelerConstants.NAME_PROPERTY, processJto.name);
      newProcessJson.addProperty("defaultPoolName", "Default Pool");
      newProcessJson.addProperty("defaultLaneName", "Default Lane");
      JsonObject createProcessChanges = applyChange(newModelId, "process.create",
            newModelId, newProcessJson);

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

      if ((null == diagramJto.poolSymbols) || (0 == diagramJto.poolSymbols.size()))
      {
         return;
      }

      // TODO recreate diagram
      for (JsonElement poolJson : diagramJto.poolSymbols)
      {
         DIPoolJto poolJto = jsonIo.gson().fromJson(poolJson, DIPoolJto.class);

         if ((null == poolJto.laneSymbols) || (0 == poolJto.laneSymbols.size()))
         {
            continue;
         }

         List<JsonObject> symbolsToRecreate = newArrayList();

         for (JsonElement laneJson : poolJto.laneSymbols)
         {
            DILaneJto laneJto = jsonIo.gson().fromJson(laneJson, DILaneJto.class);

            recreateFlowNodes(newModelId, processJto, processConversionContext,
                  newLaneJson, laneJto.activitySymbols, symbolsToRecreate);

            recreateFlowNodes(newModelId, processJto, processConversionContext,
                  newLaneJson, laneJto.gatewaySymbols, symbolsToRecreate);

            recreateFlowNodes(newModelId, processJto, processConversionContext,
                  newLaneJson, laneJto.eventSymbols, symbolsToRecreate);

            if ( !symbolsToRecreate.isEmpty())
            {
               // recreate data symbols as well ...
               recreateFlowNodes(newModelId, processJto, null, newLaneJson,
                     laneJto.dataSymbols, symbolsToRecreate);

               JsonObject newPoolPatchJson = new JsonObject();
               newPoolPatchJson.add(ModelerConstants.X_PROPERTY, poolJto.x);
               newPoolPatchJson.add(ModelerConstants.Y_PROPERTY, poolJto.y);
               newPoolPatchJson.add(ModelerConstants.WIDTH_PROPERTY, poolJto.width);
               newPoolPatchJson.add(ModelerConstants.HEIGHT_PROPERTY, poolJto.height);
               newPoolPatchJson.add(ModelerConstants.ORIENTATION_PROPERTY, poolJto.orientation);
               applyChange(newModelId, "modelElement.update",
                     Long.toString(extractLong(newPoolJson, ModelerConstants.OID_PROPERTY)),
                     newPoolPatchJson);

               JsonObject newLanePatchJson = new JsonObject();
               newLanePatchJson.add(ModelerConstants.X_PROPERTY, laneJto.x);
               newLanePatchJson.add(ModelerConstants.Y_PROPERTY, laneJto.y);
               newLanePatchJson.add(ModelerConstants.WIDTH_PROPERTY, laneJto.width);
               newLanePatchJson.add(ModelerConstants.HEIGHT_PROPERTY, laneJto.height);
               newLanePatchJson.add(ModelerConstants.ORIENTATION_PROPERTY, laneJto.orientation);
//               newLanePatchJson.addProperty(ModelerConstants.PARENT_SYMBOL_ID_PROPERTY, extractString(newPoolJson, ModelerConstants.ID_PROPERTY));
               applyChange(newModelId, "modelElement.update",
                     Long.toString(extractLong(newLaneJson, ModelerConstants.OID_PROPERTY)),
                     newLanePatchJson);

               for (JsonObject nodeSymbolJson : symbolsToRecreate)
               {
                  if (ModelerConstants.ACTIVITY_SYMBOL.equals(extractString(
                        nodeSymbolJson, ModelerConstants.TYPE_PROPERTY)))
                  {
                     recreateActivitySymbol(newModelId, newLaneJson, nodeSymbolJson,
                           processConversionContext);
                  }
                  else if (ModelerConstants.GATEWAY_SYMBOL.equals(extractString(
                        nodeSymbolJson, ModelerConstants.TYPE_PROPERTY)))
                  {
                     recreateGatewaySymbol(newModelId, newLaneJson, nodeSymbolJson,
                           processConversionContext);
                  }
                  else if (ModelerConstants.EVENT_SYMBOL.equals(extractString(
                        nodeSymbolJson, ModelerConstants.TYPE_PROPERTY)))
                  {
                     recreateEventSymbol(newModelId, newLaneJson, nodeSymbolJson,
                           processConversionContext);
                  }
                  else if (ModelerConstants.DATA_SYMBOL.equals(extractString(
                        nodeSymbolJson, ModelerConstants.TYPE_PROPERTY)))
                  {
                     recreateDataSymbol(newLaneJson, nodeSymbolJson,
                           processConversionContext);
                  }
               }
            }

            // TODO recurse into sub-lanes
         }

         for (JsonElement connectionSymbolJson : diagramJto.connections)
         {
            if (processConversionContext.hasNewElementOid(extractLong(
                  connectionSymbolJson.getAsJsonObject(),
                  ModelerConstants.FROM_MODEL_ELEMENT_OID))
                  && processConversionContext.hasNewElementOid(extractLong(
                        connectionSymbolJson.getAsJsonObject(),
                        ModelerConstants.TO_MODEL_ELEMENT_OID)))
            {
               recreateConnectionSymbol(newModelId, newPoolJson,
                     connectionSymbolJson.getAsJsonObject(),
                     processConversionContext.forModel());
            }
         }
      }
   }

   private void recreateFlowNodes(String newModelId, ProcessEntryJto processJson,
         ProcessConversionContext conversionContext, JsonObject newLaneJson,
         JsonArray flowNodesJson, List<JsonObject> symbolsToRecreate)
   {
      if (null != flowNodesJson)
      {
         for (JsonElement nodeSymbolJson : flowNodesJson)
         {
            Long flowNodeOid = nodeSymbolJson.getAsJsonObject().has(
                  ModelerConstants.MODEL_ELEMENT_PROPERTY) //
                  ? extractLong(nodeSymbolJson.getAsJsonObject(),
                        ModelerConstants.MODEL_ELEMENT_PROPERTY,
                        ModelerConstants.OID_PROPERTY) //
                  : null;

            if ((null != flowNodeOid)
                  && processJson.id.equals(conversionContext.forModel()
                        .retrieveProcessIdForFlowNode(flowNodeOid)))
            {
               symbolsToRecreate.add(nodeSymbolJson.getAsJsonObject());
            }
         }
      }
   }

   private void buildFlowNodeToProcessMapping(ModelConversionContext conversionContext,
         ProcessEntryJto processJto)
   {
      // parse diagram, find contained processes
      for (JsonElement activitiyJson : processJto.activities)
      {
         conversionContext.mapFlowNodeOidToProcessId(GsonUtils.extractLong(
               activitiyJson.getAsJsonObject(), ModelerConstants.OID_PROPERTY),
               processJto.id);
      }
      for (JsonElement gatewayJson : processJto.gateways)
      {
         conversionContext.mapFlowNodeOidToProcessId(GsonUtils.extractLong(
               gatewayJson.getAsJsonObject(), ModelerConstants.OID_PROPERTY),
               processJto.id);
      }
      for (JsonElement eventJson : processJto.events)
      {
         conversionContext
               .mapFlowNodeOidToProcessId(GsonUtils.extractLong(
                     eventJson.getAsJsonObject(), ModelerConstants.OID_PROPERTY),
                     processJto.id);
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
      for (JsonElement poolJson : diagramJto.poolSymbols)
      {
         DIPoolJto poolJto = jsonIo.gson().fromJson(poolJson, DIPoolJto.class);
         if (null == poolJto.laneSymbols)
         {
            continue;
         }

         for (JsonElement laneJson : poolJto.laneSymbols)
         {
            DILaneJto laneJto = jsonIo.gson().fromJson(laneJson, DILaneJto.class);

            inspectFlowSymbols(conversionContext, diagramJson, laneJto.activitySymbols);
            inspectFlowSymbols(conversionContext, diagramJson, laneJto.gatewaySymbols);
            inspectFlowSymbols(conversionContext, diagramJson, laneJto.eventSymbols);

            // TODO recurse into sub-lanes
         }
      }
   }

   private void inspectFlowSymbols(ModelConversionContext conversionContext,
         JsonObject diagramJson, JsonArray flowSymbolsJson)
   {
      if (null != flowSymbolsJson)
      {
         for (JsonElement activitySymbolJson : flowSymbolsJson)
         {
            inspectFlowSymbol(conversionContext, diagramJson, activitySymbolJson);
         }
      }
   }

   private void inspectFlowSymbol(ModelConversionContext conversionContext,
         JsonObject diagramJson, JsonElement flowSymbolJson)
   {
      Long flowNodeOid = extractLong(flowSymbolJson.getAsJsonObject(),
            ModelerConstants.MODEL_ELEMENT_PROPERTY,
            ModelerConstants.OID_PROPERTY);

      String processId = conversionContext.retrieveProcessIdForFlowNode(flowNodeOid);
      if ( !isEmpty(processId))
      {
         conversionContext.forProcess(processId).registerDiagramForProcess(diagramJson);
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

      JsonObject changesJson = applyChange(
            newModelId,
            "activitySymbol.create",
            Long.toString(extractLong(laneSymbolJson, ModelerConstants.OID_PROPERTY)), //
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
                  Long.toString(conversionContext.newElementOid(extractLong(
                        activitySymbolJson, ModelerConstants.OID_PROPERTY))),
                  activitySymbolJson);
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

      JsonObject changesJson = applyChange(
            newModelId,
            "gateSymbol.create",
            Long.toString(extractLong(laneSymbolJson, ModelerConstants.OID_PROPERTY)), //
            gatewaySymbolJson);

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

      JsonObject changesJson = applyChange(
            newModelId,
            "eventSymbol.create",
            Long.toString(extractLong(laneSymbolJson, ModelerConstants.OID_PROPERTY)), //
            eventSymbolJson);

      mapNewElementIdentifiers(eventSymbolJson, changesJson, conversionContext.forModel());

      // push element update to fully sync properties
      for (JsonElement addedElementJson : changesJson.getAsJsonArray("added"))
      {
         if (ModelerConstants.EVENT_SYMBOL.equals(extractString(
               addedElementJson.getAsJsonObject(), ModelerConstants.TYPE_PROPERTY)))
         {
            applyChange(newModelId, "modelElement.update",
                  Long.toString(conversionContext.newElementOid(extractLong(
                        eventSymbolJson, ModelerConstants.OID_PROPERTY))),
                  eventSymbolJson);
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

      JsonObject changesJson = applyChange(
            conversionContext.newModelId(),
            "dataSymbol.create",
            Long.toString(extractLong(laneSymbolJson, ModelerConstants.OID_PROPERTY)), //
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

      Long newSourceSymbolOid = conversionContext.newElementOid(extractLong(
            connectionSymbolJson, ModelerConstants.FROM_MODEL_ELEMENT_OID));
      Long newTargetSymbolOid = conversionContext.newElementOid(extractLong(connectionSymbolJson,
            ModelerConstants.TO_MODEL_ELEMENT_OID));

      connectionSymbolJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
            newSourceSymbolOid);
      connectionSymbolJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_OID,
            newTargetSymbolOid);

      JsonObject changesJson = applyChange(
            newModelId,
            "connection.create",
            Long.toString(extractLong(poolSymbolJson, ModelerConstants.OID_PROPERTY)), //
            connectionSymbolJson);

      if ( !isEmpty(oldElementId))
      {
         connectionJson.addProperty(ModelerConstants.ID_PROPERTY, oldElementId);
      }

      String symbolType = extractString(connectionSymbolJson, ModelerConstants.TYPE_PROPERTY);
      long oldConnectionOid = extractLong(connectionJson, ModelerConstants.OID_PROPERTY);
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
            applyChange(newModelId, "modelElement.update",
                  Long.toString(newConnectionSymbolOid), connectionSymbolJson);
         }
      }
   }

   private JsonObject applyCreateCommand(String modelUuid, String commandId,
         JsonObject changeJson, String expectedResult)
   {
      return applyCreateCommand(modelUuid, commandId, null, changeJson, expectedResult);
   }

   private JsonObject applyCreateCommand(String modelUuid, String commandId, String contextElementId,
         JsonObject changeJson, String expectedResult)
   {
      JsonObject responseJson = applyChange(modelUuid, commandId, contextElementId, changeJson);

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

   private JsonObject applyChange(String modelUuid, String commandId,
         String contextElementId, JsonObject changeJson)
   {
      JsonObject cmdJson = new JsonObject();
      if ( !isEmpty(modelUuid))
      {
         cmdJson.addProperty("modelId", modelUuid);
      }
      cmdJson.addProperty("commandId", commandId);
      cmdJson.add("changeDescriptions", new JsonArray());
      ((JsonArray) cmdJson.get("changeDescriptions")).add(new JsonObject());
      if ( !isEmpty(contextElementId))
      {
         ((JsonObject) ((JsonArray) cmdJson.get("changeDescriptions")).get(0)).addProperty(
               ModelerConstants.OID_PROPERTY, contextElementId);
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
