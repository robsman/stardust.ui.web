package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Factory;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Package;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.findContainingModel;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Operation;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
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
   public void populateFromJson(EObject modelElement, JsonObject jto)
   {
      trace.info("Populate object " + modelElement + " from JSON " + jto);

      if (modelElement instanceof Process)
      {
         updateProcessDefinition((Process) modelElement, jto);
      }
      else if (modelElement instanceof Activity)
      {
         updateActivity((Activity) modelElement, jto);
      }
      else if (modelElement instanceof Gateway)
      {
         updateGateway((Gateway) modelElement, jto);
      }
      else if (modelElement instanceof Event)
      {
         updateEvent((Event) modelElement, jto);
      }
      else if (modelElement instanceof DataObject)
      {
         updateDataObject((DataObject) modelElement, jto);
      }
      else if (modelElement instanceof BPMNShape)
      {
         BPMNShape shape = (BPMNShape) modelElement;

         trace.info("Shape: " + shape);
         trace.info("Json: " + jto);

         if (shape.getBpmnElement() instanceof Activity)
         {
            updateActivity((Activity) shape.getBpmnElement(),
                  jto.get(ModelerConstants.MODEL_ELEMENT_PROPERTY).getAsJsonObject());
         }
         else if (shape.getBpmnElement() instanceof Gateway)
         {
            updateGateway((Gateway) shape.getBpmnElement(),
                  jto.get(ModelerConstants.MODEL_ELEMENT_PROPERTY).getAsJsonObject());
         }
         else if (shape.getBpmnElement() instanceof Event)
         {
            updateEvent((Event) shape.getBpmnElement(),
                  jto.get(ModelerConstants.MODEL_ELEMENT_PROPERTY).getAsJsonObject());
         }
         else if (shape.getBpmnElement() instanceof DataObject)
         {
            updateDataObject((DataObject) shape.getBpmnElement(),
                  jto.get(ModelerConstants.MODEL_ELEMENT_PROPERTY).getAsJsonObject());
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

      if (activityJson.has(ModelerConstants.ACTIVITY_TYPE))
      {
         if (ModelerConstants.MANUAL_ACTIVITY.equals(activityJson.get(ModelerConstants.ACTIVITY_TYPE)))
         {
            UserTask userTask = null;

            if ( !(activity instanceof UserTask))
            {
               trace.error("Conversion to User Task not yet supported");
            }
            else
            {
               userTask = (UserTask) activity;

               userTask.setImplementation("##unspecified");
            }
         }
         else if (ModelerConstants.APPLICATION_ACTIVITY.equals(activityJson.get(ModelerConstants.ACTIVITY_TYPE)))
         {
            if (activityJson.has(ModelerConstants.PARTICIPANT_FULL_ID))
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

                  userTask.setImplementation("##unspecified");
               }
            }
            else
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

                  if (true)
                  {
                     serviceTask.setImplementation("##WebService");

                     Operation operation = bpmn2Factory().createOperation();

                     operation.setId(activityJson.get(ModelerConstants.APPLICATION_FULL_ID_PROPERTY).getAsString());

                     serviceTask.setOperationRef(operation);
                  }
                  else
                  {
                     serviceTask.setImplementation("##unspecified");
                  }
               }
            }
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

      if (eventJson.get(ModelerConstants.EVENT_TYPE_PROPERTY)
            .getAsString()
            .equals(ModelerConstants.START_EVENT))
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
      }
      // else if (eventJson.eventType == ModelerConstants.START_EVENT)
      // {
      // // event instanceof BoundaryEvent
      // }
      else if (eventJson.get(ModelerConstants.EVENT_TYPE_PROPERTY)
            .getAsString()
            .equals(ModelerConstants.STOP_EVENT))
      {
      }

      storeDescription(event, eventJson);
      storeExtensions(event, eventJson);
   }

   /**
    * 
    * @param dataObject
    * @param dataJson
    */
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
      description.setText(jto.get(ModelerConstants.DESCRIPTION_PROPERTY).getAsString());
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

      EObject target = EObjectMorpher.morphType(current, newType);

      // reconnect target for source in model
      EObjectMorpher.replace(current, target);

      if (null != model)
      {
         bpmn2Binding.reassociateUuid(model, current, target);
         bpmn2Binding.reassociateOid(model, current, target);
      }

      return target;
   }
}
