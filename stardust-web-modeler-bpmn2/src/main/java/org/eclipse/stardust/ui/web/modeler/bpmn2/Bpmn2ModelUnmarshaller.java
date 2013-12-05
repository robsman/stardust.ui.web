package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2DcFactory;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Factory;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Package;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.findContainingModel;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.getModelUuid;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils.getExtensionAsJson;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils.setExtensionFromJson;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.ElementRefUtils.resolveElementIdFromReference;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.ElementRefUtils.resolveModelIdFromReference;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.JsonExtensionPropertyUtils.syncExtProperty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.Iterator;

import javax.xml.XMLConstants;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.DataStore;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.ItemAwareElement;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.ManualTask;
import org.eclipse.bpmn2.Operation;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Performer;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ReceiveTask;
import org.eclipse.bpmn2.Resource;
import org.eclipse.bpmn2.ResourceRole;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.ScriptTask;
import org.eclipse.bpmn2.SendTask;
import org.eclipse.bpmn2.ServiceTask;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.EObjectMorpher;
import org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.service.XsdSchemaUtils;

import org.eclipse.xsd.XSDSchema;

public class Bpmn2ModelUnmarshaller implements ModelUnmarshaller
{
   private static final Logger trace = LogManager.getLogger(Bpmn2ModelUnmarshaller.class);

   private Bpmn2Binding bpmn2Binding;

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   void setBinding(Bpmn2Binding bpmn2Binding)
   {
      this.bpmn2Binding = bpmn2Binding;
   }

   @Override
   public void populateFromJson(EObject modelElement, JsonObject json)
   {
      trace.info("Populate object " + modelElement + " from JSON " + json);

      if (modelElement instanceof ItemDefinition)
      {
         updateItemDefinition((ItemDefinition) modelElement, json);
      }
      else if (modelElement instanceof Resource)
      {
         updateProcessParticipant((Resource) modelElement, json);
      }
      else if (modelElement instanceof Process)
      {
         updateProcessDefinition((Process) modelElement, json);
      }
      else if (modelElement instanceof Activity)
      {
         updateActivity((Activity) modelElement, json);
      }
      else if (modelElement instanceof Gateway)
      {
         updateGateway((Gateway) modelElement, json);
      }
      else if (modelElement instanceof Event)
      {
         updateEvent((Event) modelElement, json);
      }
      else if (modelElement instanceof DataStore)
      {
         updateDataStore((DataStore) modelElement, json);
      }
      else if (modelElement instanceof DataObject)
      {
         updateDataObject((DataObject) modelElement, json);
      }
      else if (modelElement instanceof BPMNShape)
      {
         BPMNShape shape = (BPMNShape) modelElement;

         trace.info("Shape: " + shape);
         trace.info("Json: " + json);

         if (shape.getBpmnElement() instanceof Participant)
         {
            updatePool(shape, (Participant) shape.getBpmnElement(), json);
         }
         else if (shape.getBpmnElement() instanceof Lane)
         {
            updateSwimlane(shape, (Lane) shape.getBpmnElement(), json);
         }
         else if (shape.getBpmnElement() instanceof Activity)
         {
            updateActivity((Activity) shape.getBpmnElement(),
                  json.get(ModelerConstants.MODEL_ELEMENT_PROPERTY).getAsJsonObject());
         }
         else if (shape.getBpmnElement() instanceof Gateway)
         {
            updateGateway((Gateway) shape.getBpmnElement(),
                  json.get(ModelerConstants.MODEL_ELEMENT_PROPERTY).getAsJsonObject());
         }
         else if (shape.getBpmnElement() instanceof Event)
         {
            updateEvent((Event) shape.getBpmnElement(),
                  json.get(ModelerConstants.MODEL_ELEMENT_PROPERTY).getAsJsonObject());
         }
         else if (shape.getBpmnElement() instanceof DataObject)
         {
            updateDataObject((DataObject) shape.getBpmnElement(),
                  json.get(ModelerConstants.MODEL_ELEMENT_PROPERTY).getAsJsonObject());
         }
         else
         {
            throw new UnsupportedOperationException("Not yet implemented: "
                  + shape.getBpmnElement());
         }
      }
      else
      {
         throw new UnsupportedOperationException("Not yet implemented: " + modelElement);
      }
   }

   private void updateItemDefinition(ItemDefinition itemDefinition, JsonObject json)
   {
      // TODO handle type rename

      JsonObject declarationJson = json.getAsJsonObject("typeDeclaration");
      JsonObject typeJson = (null != declarationJson)
            ? declarationJson.getAsJsonObject("type")
            : null;
      if ((null != typeJson)
            && "SchemaType".equals(typeJson.getAsJsonPrimitive("classifier")
                  .getAsString()))
      {
         // TODO consolidate, see {@link XsdSchemaUtils#updateXsdSchemaType}
         JsonObject schemaJson = declarationJson.getAsJsonObject("schema");

         XSDSchema schema = (XSDSchema) Bpmn2ExtensionUtils.getExtensionElement(
               itemDefinition, "schema", XMLConstants.W3C_XML_SCHEMA_NS_URI);

         if (schemaJson.has("targetNamespace"))
         {
            schema.setTargetNamespace(GsonUtils.safeGetAsString(schemaJson, "targetNamespace"));
         }

         JsonObject locations = GsonUtils.safeGetAsJsonObject(schemaJson, "locations");
         if (locations != null)
         {
            // TODO
            // XsdSchemaUtils.updateImports(facade, schema, locations);
         }

         if (schemaJson.has("types"))
         {
            XsdSchemaUtils.updateXSDTypeDefinitions(schema, GsonUtils.safeGetAsJsonArray(schemaJson, "types"), locations);
         }

         if (schemaJson.has("elements"))
         {
            XsdSchemaUtils.updateElementDeclarations(schema, GsonUtils.safeGetAsJsonArray(schemaJson, "elements"), locations);
         }
      }
      // TODO handle reference types
   }

   private void updateProcessParticipant(Resource resource, JsonObject json)
   {
      trace.info("Updating Model Participant from JSON " + json.toString());

      if (json.has(ModelerConstants.NAME_PROPERTY))
      {
         resource.setName(json.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      JsonObject extJson = getExtensionAsJson(resource, "core");

      if (ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY.equals(extractString(extJson,
            ModelerConstants.PARTICIPANT_TYPE_PROPERTY))
            || ModelerConstants.TEAM_LEADER_TYPE_KEY.equals(extractString(extJson,
                  ModelerConstants.PARTICIPANT_TYPE_PROPERTY)))
      {
         syncExtProperty(ModelerConstants.CARDINALITY, json, extJson);
         syncExtProperty(ModelerConstants.PARENT_UUID_PROPERTY, json, extJson);
      }
      else if (ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY.equals(extractString(
            extJson, ModelerConstants.PARTICIPANT_TYPE_PROPERTY)))
      {
         // TODO
         syncExtProperty(ModelerConstants.PARENT_UUID_PROPERTY, json, extJson);
         syncExtProperty(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY, json, extJson);
      }
      else if (ModelerConstants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE_KEY.equals(extractString(
            extJson, ModelerConstants.PARTICIPANT_TYPE_PROPERTY)))
      {
         // TODO
         syncExtProperty(ModelerConstants.PARENT_UUID_PROPERTY, json, extJson);
         syncExtProperty(ModelerConstants.PARTICIPANT_TYPE_PROPERTY, json, extJson);
      }

      setExtensionFromJson(resource, "core", extJson);

      storeDescription(resource, json);
      storeExtensions(resource, json);
   }

   /**
    *
    * @param process
    * @param processJson
    */
   private void updateProcessDefinition(Process process, JsonObject processJson)
   {
      trace.info("Updating Process from JSON " + processJson.toString());

      if (processJson.has(ModelerConstants.NAME_PROPERTY))
      {
         process.setName(processJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      storeDescription(process, processJson);
      storeExtensions(process, processJson);
   }

   private void updateShape(BPMNShape shape, JsonObject shapeJson)
   {
      if (null == shape.getBounds())
      {
         // TODO set only if some coordinate has really changed
         shape.setBounds(bpmn2DcFactory().createBounds());
      }
      if (shapeJson.has(ModelerConstants.X_PROPERTY))
      {
         shape.getBounds().setX(GsonUtils.extractInt(shapeJson, ModelerConstants.X_PROPERTY));
      }
      if (shapeJson.has(ModelerConstants.Y_PROPERTY))
      {
         shape.getBounds().setY(GsonUtils.extractInt(shapeJson, ModelerConstants.Y_PROPERTY));
      }
      if (shapeJson.has(ModelerConstants.WIDTH_PROPERTY))
      {
         shape.getBounds().setWidth(GsonUtils.extractInt(shapeJson, ModelerConstants.WIDTH_PROPERTY));
      }
      if (shapeJson.has(ModelerConstants.HEIGHT_PROPERTY))
      {
         shape.getBounds().setHeight(GsonUtils.extractInt(shapeJson, ModelerConstants.HEIGHT_PROPERTY));
      }
   }

   private void updatePool(BPMNShape poolShape, Participant participant, JsonObject poolJson)
   {
      updateShape(poolShape, poolJson);

      if (poolJson.has(ModelerConstants.ORIENTATION_PROPERTY))
      {
         poolShape.setIsHorizontal(ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL
               .equals(extractString(poolJson, ModelerConstants.ORIENTATION_PROPERTY)));
      }
   }

   private void updateSwimlane(BPMNShape laneShape, Lane lane, JsonObject laneJson)
   {
      updateShape(laneShape, laneJson);

      if (laneJson.has(ModelerConstants.ORIENTATION_PROPERTY))
      {
         laneShape.setIsHorizontal(ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL
               .equals(extractString(laneJson, ModelerConstants.ORIENTATION_PROPERTY)));
      }

      if (laneJson.has(ModelerConstants.PARTICIPANT_FULL_ID))
      {
         // TODO resolve performer and associate with activities
         String modelId = resolveModelIdFromReference(extractString(
               laneJson, ModelerConstants.PARTICIPANT_FULL_ID));
         Definitions model = (Definitions) bpmn2Binding.getModelingSession().modelRepository().findModel(modelId);
         if (model != Bpmn2Utils.findContainingModel(lane))
         {
            // TODO cross model references
            throw new UnsupportedOperationException("Cross model references are not yet supported.");
         }

         EObject performer = Bpmn2Navigator.findRootElement(
               model,
               resolveElementIdFromReference(extractString(laneJson,
                     ModelerConstants.PARTICIPANT_FULL_ID)), Resource.class);
         if (performer instanceof Resource)
         {
            // associate performer with lane's activities
            for(FlowNode laneNode : lane.getFlowNodeRefs())
            {
               // TODO restrict to interactive activities
               if (laneNode instanceof Activity)
               {
                  boolean hasRequestedPerformer = false;
                  Activity activity = (Activity) laneNode;
                  for (Iterator<ResourceRole> i = activity.getResources().iterator(); i.hasNext(); )
                  {
                     ResourceRole resourceRole = i.next();
                     if (resourceRole instanceof Performer)
                     {
                        if (resourceRole.getResourceRef() == performer)
                        {
                           hasRequestedPerformer = true;
                        }
                        else
                        {
                           i.remove();
                        }
                     }
                  }
                  if ( !hasRequestedPerformer)
                  {
                     Performer performerSpec = Bpmn2Utils.bpmn2Factory().createPerformer();
                     performerSpec.setResourceRef((Resource) performer);
                     activity.getResources().add(performerSpec);
                  }
               }
            }
         }
      }
   }

   /**
    *
    * @param activity
    * @param activityJson
    */
   private void updateActivity(Activity activity, JsonObject activityJson)
   {
      Process p = (Process) activity.eContainer();

      trace.info("Contains element: " + p.getFlowElements().contains(activity));

      if (activityJson.has(ModelerConstants.NAME_PROPERTY))
      {
         activity.setName(activityJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      trace.info("Activity Type: " + activity);
      trace.info("Activity JSON: " + activityJson);

      if (activityJson.has(ModelerConstants.TASK_TYPE))
      {
         // if
         // (ModelerConstants.NONE_TASK_KEY.equals(activityJson.get(ModelerConstants.TASK_TYPE)))
         // {
         // NoneTask noneTask = null;
         //
         // if ( !(activity instanceof NoneTask))
         // {
         // noneTask = (NoneTask) switchElementType(activity,
         // bpmn2Package().getNoneTask());
         // }
         // else
         // {
         // noneTask = (NoneTask) activity;
         // }
         // }
         // else
         if (ModelerConstants.MANUAL_TASK_KEY.equals(activityJson.get(ModelerConstants.TASK_TYPE)))
         {
            ManualTask manualTask = null;

            if ( !(activity instanceof ManualTask))
            {
               manualTask = (ManualTask) switchElementType(activity,
                     bpmn2Package().getManualTask());
            }
            else
            {
               manualTask = (ManualTask) activity;
            }
         }
         else if (ModelerConstants.USER_TASK_KEY.equals(activityJson.get(ModelerConstants.TASK_TYPE)))
         {
            UserTask userTask = null;

            if ( !(activity instanceof UserTask))
            {
               userTask = (UserTask) switchElementType(activity,
                     bpmn2Package().getUserTask());
            }
            else
            {
               userTask = (UserTask) activity;
            }

            userTask.setImplementation("##unspecified");
         }
         else if (ModelerConstants.SERVICE_TASK_KEY.equals(activityJson.get(ModelerConstants.TASK_TYPE)))
         {
            ServiceTask serviceTask = null;

            if ( !(activity instanceof ServiceTask))
            {
               serviceTask = (ServiceTask) switchElementType(activity,
                     bpmn2Package().getServiceTask());
            }
            else
            {
               serviceTask = (ServiceTask) activity;
            }

            // Encode Stardust Application Types

            if (true)
            {
               serviceTask.setImplementation("##WebService");

               Operation operation = bpmn2Factory().createOperation();

               operation.setId(activityJson.get(
                     ModelerConstants.APPLICATION_FULL_ID_PROPERTY).getAsString());

               serviceTask.setOperationRef(operation);
            }
            else
            {
               serviceTask.setImplementation("##unspecified");
            }
         }
         else if (ModelerConstants.SCRIPT_TASK_KEY.equals(activityJson.get(ModelerConstants.TASK_TYPE)))
         {
            ScriptTask scriptTask = null;

            if ( !(activity instanceof ScriptTask))
            {
               scriptTask = (ScriptTask) switchElementType(activity,
                     bpmn2Package().getScriptTask());
            }
            else
            {
               scriptTask = (ScriptTask) activity;
            }
         }
         else if (ModelerConstants.SEND_TASK_KEY.equals(activityJson.get(ModelerConstants.TASK_TYPE)))
         {
            SendTask sendTask = null;

            if ( !(activity instanceof SendTask))
            {
               sendTask = (SendTask) switchElementType(activity,
                     bpmn2Package().getSendTask());
            }
            else
            {
               sendTask = (SendTask) activity;
            }
         }
         else if (ModelerConstants.RECEIVE_TASK_KEY.equals(activityJson.get(ModelerConstants.TASK_TYPE)))
         {
            ReceiveTask receiveTask = null;

            if ( !(activity instanceof ReceiveTask))
            {
               receiveTask = (ReceiveTask) switchElementType(activity,
                     bpmn2Package().getReceiveTask());
            }
            else
            {
               receiveTask = (ReceiveTask) activity;
            }
         }
         // else if
         // (ModelerConstants.RULE_TASK_KEY.equals(activityJson.get(ModelerConstants.TASK_TYPE)))
         // {
         // RuleTask ruleTask = null;
         //
         // if ( !(activity instanceof RuleTask))
         // {
         // ruleTask = (RuleTask) switchElementType(activity,
         // bpmn2Package().getRuleTask());
         // }
         // else
         // {
         // ruleTask = (RuleTask) activity;
         // }
         // }
      }
      else if (ModelerConstants.SUBPROCESS_ACTIVITY.equals(activityJson.get(ModelerConstants.ACTIVITY_TYPE)))
      {
         SubProcess subProcess;

         if ( !(activity instanceof SubProcess))
         {
            subProcess = (SubProcess) switchElementType(activity,
                  bpmn2Package().getSubProcess());
         }
         else
         {
            subProcess = (SubProcess) activity;
         }
      }

      storeDescription(activity, activityJson);
      storeExtensions(activity, activityJson);
   }

   /**
    *
    * @param gateway
    * @param gatewayJson
    */
   private void updateGateway(Gateway gateway, JsonObject gatewayJson)
   {
      if (gatewayJson.has(ModelerConstants.NAME_PROPERTY))
      {
         gateway.setName(gatewayJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      // gateway.setGatewayDirection(GatewayDirection.CONVERGING);

      if (ModelerConstants.XOR_GATEWAY_TYPE.equals(gatewayJson.get(ModelerConstants.GATEWAY_TYPE_PROPERTY)))
      {
         if ( !(gateway instanceof ExclusiveGateway))
         {
            gateway = (Gateway) switchElementType(gateway,
                  bpmn2Package().getExclusiveGateway());
         }
      }
      else if (ModelerConstants.AND_GATEWAY_TYPE.equals(gatewayJson.get(ModelerConstants.GATEWAY_TYPE_PROPERTY)))
      {
         if ( !(gateway instanceof ParallelGateway))
         {
            gateway = (Gateway) switchElementType(gateway,
                  bpmn2Package().getParallelGateway());
         }
      }

      storeDescription(gateway, gatewayJson);
      storeExtensions(gateway, gatewayJson);
   }

   /**
    *
    * @param event
    * @param eventJson
    */
   private void updateEvent(Event event, JsonObject eventJson)
   {
      if (eventJson.has(ModelerConstants.NAME_PROPERTY))
      {
         event.setName(eventJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      if (eventJson.has(ModelerConstants.EVENT_TYPE_PROPERTY))
      {
         String eventType = eventJson.get(ModelerConstants.EVENT_TYPE_PROPERTY)
               .getAsString();

         if (eventType.equals(ModelerConstants.START_EVENT))
         {
            StartEvent startEvent = null;

            if ( !(event instanceof StartEvent))
            {
               startEvent = (StartEvent) switchElementType(event,
                     bpmn2Package().getStartEvent());
            }
            else
            {
               startEvent = (StartEvent) event;
            }

            String eventClass = extractAsString(eventJson,
                  ModelerConstants.EVENT_CLASS_PROPERTY);
            if (!isEmpty(eventClass) && !ModelerConstants.NONE_EVENT_CLASS_KEY.equals(eventClass))
            {
               startEvent.getEventDefinitions().add(
                     getEventDefinitionForEventClass(eventClass));
            }
         }
         else if (eventType.equals(ModelerConstants.INTERMEDIATE_EVENT))
         {
            if (eventJson.has(ModelerConstants.BINDING_ACTIVITY_UUID))
            {
               BoundaryEvent boundaryEvent = null;

               if ( !(event instanceof BoundaryEvent))
               {
                  boundaryEvent = (BoundaryEvent) switchElementType(event,
                        bpmn2Package().getBoundaryEvent());
               }
               else
               {
                  boundaryEvent = (BoundaryEvent) event;
               }

               boundaryEvent.getEventDefinitions().add(
                     getEventDefinitionForEventClass(eventJson.get(
                           ModelerConstants.EVENT_CLASS_PROPERTY).getAsString()));

               Process process = Bpmn2Utils.findContainingProcess(boundaryEvent);

               boundaryEvent.setAttachedToRef(Bpmn2ExtensionUtils.findActivityById(
                     process, eventJson.get(ModelerConstants.BINDING_ACTIVITY_UUID)
                           .getAsString()));
               boundaryEvent.setCancelActivity(eventJson.get(
                     ModelerConstants.INTERRUPTING_PROPERTY).getAsBoolean());
            }
            else
            {
               if (eventJson.get(ModelerConstants.THROWING_PROPERTY).getAsBoolean())
               {
                  IntermediateThrowEvent intermediateThrowEvent = null;

                  if ( !(event instanceof IntermediateThrowEvent))
                  {
                     intermediateThrowEvent = (IntermediateThrowEvent) switchElementType(
                           event, bpmn2Package().getIntermediateThrowEvent());
                  }
                  else
                  {
                     intermediateThrowEvent = (IntermediateThrowEvent) event;
                  }

                  intermediateThrowEvent.getEventDefinitions().add(
                        getEventDefinitionForEventClass(eventJson.get(
                              ModelerConstants.EVENT_CLASS_PROPERTY).getAsString()));
               }
               else
               {
                  IntermediateCatchEvent intermediateCatchEvent = null;

                  if ( !(event instanceof IntermediateCatchEvent))
                  {
                     intermediateCatchEvent = (IntermediateCatchEvent) switchElementType(
                           event, bpmn2Package().getIntermediateCatchEvent());
                  }
                  else
                  {
                     intermediateCatchEvent = (IntermediateCatchEvent) event;
                  }

                  intermediateCatchEvent.getEventDefinitions().add(
                        getEventDefinitionForEventClass(eventJson.get(
                              ModelerConstants.EVENT_CLASS_PROPERTY).getAsString()));
               }
            }
         }
         else if (eventJson.get(ModelerConstants.EVENT_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.STOP_EVENT))
         {
            EndEvent endEvent = null;

            if ( !(event instanceof EndEvent))
            {
               endEvent = (EndEvent) switchElementType(event,
                     bpmn2Package().getEndEvent());
            }
            else
            {
               endEvent = (EndEvent) event;
            }

            String eventClass = extractString(eventJson,
                  ModelerConstants.EVENT_CLASS_PROPERTY);
            if (!isEmpty(eventClass))
            {
               endEvent.getEventDefinitions().add(
                     getEventDefinitionForEventClass(eventClass));
            }
         }
      }

      storeDescription(event, eventJson);
      storeExtensions(event, eventJson);
   }

   /**
    *
    * @param dataStore
    * @param dataJson
    */
   private void updateDataStore(DataStore dataStore, JsonObject dataJson)
   {
      if (dataJson.has(ModelerConstants.NAME_PROPERTY))
      {
         dataStore.setName(dataJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      storeDescription(dataStore, dataJson);
      storeExtensions(dataStore, dataJson);

      updateVariable(dataStore, dataJson);
   }

   private void updateDataObject(DataObject dataObject, JsonObject dataJson)
   {
      if (dataJson.has(ModelerConstants.NAME_PROPERTY))
      {
         dataObject.setName(dataJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      storeDescription(dataObject, dataJson);
      storeExtensions(dataObject, dataJson);

      updateVariable(dataObject, dataJson);
   }

   private void updateVariable(ItemAwareElement variable, JsonObject dataJson)
   {
      JsonObject extJson = getExtensionAsJson(variable, "core");

      if (dataJson.has(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY))
      {
         extJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY, ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);
         extJson.add(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY,
               dataJson.get(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY));
      }
      else if (dataJson.has(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
      {
         extJson.remove(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY);

         extJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY, ModelerConstants.STRUCTURED_DATA_TYPE_KEY);

         ItemDefinition referencedType = null;

         String structuredDataTypeFullId = extractAsString(dataJson,
               ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY);
         if ( !isEmpty(structuredDataTypeFullId))
         {
            Definitions model = findContainingModel(variable);
            if ((null != model)
                  && (structuredDataTypeFullId.startsWith(getModelUuid(model) + ":")))
            {
               String structuredDataTypeId = structuredDataTypeFullId.substring(getModelUuid(
                     model).length() + 1);
               for (RootElement rootElement : model.getRootElements())
               {
                  if ((rootElement instanceof ItemDefinition)
                        && structuredDataTypeId.equals(((ItemDefinition) rootElement).getId()))
                  {
                     referencedType = (ItemDefinition) rootElement;
                     break;
                  }
               }
            }
            // TODO handle references to external type IDs
         }

         variable.setItemSubjectRef(referencedType);
      }

      setExtensionFromJson(variable, "core", extJson);
   }

   /**
    *
    * @param element
    * @param jto
    */
   private void storeDescription(BaseElement element, JsonObject jto)
   {
      Documentation description = Bpmn2ExtensionUtils.getDescription(element);

      if (description == null)
      {
         element.getDocumentation().add(
               description = bpmn2Factory().createDocumentation());

         description.setId("description");
      }

      description.setTextFormat("plain/text");
      if (jto.has(ModelerConstants.DESCRIPTION_PROPERTY))
      {
         description.setText(jto.get(ModelerConstants.DESCRIPTION_PROPERTY).getAsString());
      }
   }

   /**
    *
    * @param event
    * @return
    */
   public EventDefinition getEventDefinitionForEventClass(String eventClass)
   {
      if (ModelerConstants.TIMER_EVENT_CLASS_KEY.equals(eventClass))
      {
         return (EventDefinition) Bpmn2Utils.bpmn2Factory().createTimerEventDefinition();
      }
      else if (ModelerConstants.MESSAGE_EVENT_CLASS_KEY.equals(eventClass))
      {
         return (EventDefinition) Bpmn2Utils.bpmn2Factory()
               .createMessageEventDefinition();
      }
      else if (ModelerConstants.ERROR_EVENT_CLASS_KEY.equals(eventClass))
      {
         return (EventDefinition) Bpmn2Utils.bpmn2Factory().createErrorEventDefinition();
      }
      else
      {
         throw new IllegalArgumentException("Unknown event class " + eventClass + ".");
      }
   }

   /**
    *
    * @param element
    * @param jto
    */
   private void storeExtensions(BaseElement element, JsonObject jto)
   {
      JsonObject coreExtensions = Bpmn2ExtensionUtils.getExtensionAsJson(element, "core");

      Bpmn2ExtensionUtils.overwriteJson(coreExtensions, jto);
      Bpmn2ExtensionUtils.setExtensionFromJson(element, "core", coreExtensions);
   }

   /**
    *
    * @param current
    * @param newType
    * @return
    */
   private EObject switchElementType(EObject current, EClass newType)
   {
      Definitions model = findContainingModel(current);

      // switch to target type and reconnect target for source in model
      EObject target = EObjectMorpher.morphType(current, newType);

      if (null != model)
      {
         bpmn2Binding.reassociateUuid(model, current, target);
         bpmn2Binding.reassociateOid(model, current, target);
      }

      return target;
   }
}
