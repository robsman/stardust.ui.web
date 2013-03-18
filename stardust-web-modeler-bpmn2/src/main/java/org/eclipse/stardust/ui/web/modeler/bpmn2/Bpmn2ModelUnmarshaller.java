package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Factory;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Package;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.findContainingModel;

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
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.ManualTask;
import org.eclipse.bpmn2.Operation;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ReceiveTask;
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
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;

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

      if (modelElement instanceof Process)
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

         if (shape.getBpmnElement() instanceof Activity)
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

            startEvent.getEventDefinitions().add(
                  getEventDefinitionForEventClass(eventJson.get(
                        ModelerConstants.EVENT_CLASS_PROPERTY).getAsString()));
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

            endEvent.getEventDefinitions().add(
                  getEventDefinitionForEventClass(eventJson.get(
                        ModelerConstants.EVENT_CLASS_PROPERTY).getAsString()));
         }
      }

      storeDescription(event, eventJson);
      storeExtensions(event, eventJson);
   }

   /**
    * 
    * @param dataObject
    * @param dataJson
    */
   private void updateDataStore(DataStore dataObject, JsonObject dataJson)
   {
      if (dataJson.has(ModelerConstants.NAME_PROPERTY))
      {
         dataObject.setName(dataJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      storeDescription(dataObject, dataJson);
      storeExtensions(dataObject, dataJson);
   }

   private void updateDataObject(DataObject dataObject, JsonObject dataJson)
   {
      if (dataJson.has(ModelerConstants.NAME_PROPERTY))
      {
         dataObject.setName(dataJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      storeDescription(dataObject, dataJson);
      storeExtensions(dataObject, dataJson);
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
