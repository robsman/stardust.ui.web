package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Factory;

import java.util.Map;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Operation;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ServiceTask;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2FlowNodeBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.DataJto;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;
import org.eclipse.stardust.ui.web.modeler.model.GatewayJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelElementJto;
import org.eclipse.stardust.ui.web.modeler.model.ProcessDefinitionJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ActivitySymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.DataSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.EventSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.GatewaySymbolJto;

public class Bpmn2ModelUnmarshaller implements ModelUnmarshaller
{
   private static final Logger trace = LogManager.getLogger(Bpmn2ModelUnmarshaller.class);

   private Bpmn2Binding bpmn2Binding;

   private Bpmn2FlowNodeBuilder flowNodeBuilder = new Bpmn2FlowNodeBuilder();

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
         updateProcessDefinition((Process) modelElement,
               jsonIo.gson().fromJson(jto, ProcessDefinitionJto.class));
      }
      else if (modelElement instanceof Activity)
      {
         updateActivity((Activity) modelElement,
               jsonIo.gson().fromJson(jto, ActivityJto.class));
      }
      else if (modelElement instanceof Gateway)
      {
         updateGateway((Gateway) modelElement,
               jsonIo.gson().fromJson(jto, GatewayJto.class));
      }
      else if (modelElement instanceof Event)
      {
         updateEvent((Event) modelElement, jsonIo.gson().fromJson(jto, EventJto.class));
      }
      else if (modelElement instanceof DataObject)
      {
         updateDataObject((DataObject) modelElement,
               jsonIo.gson().fromJson(jto, DataJto.class));
      }
      else if (modelElement instanceof BPMNShape)
      {
         BPMNShape shape = (BPMNShape) modelElement;

         trace.info("Shape: " + shape);
         trace.info("Json: " + jto);

         if (shape.getBpmnElement() instanceof Activity)
         {
            updateActivity(
                  (Activity) shape.getBpmnElement(),
                  ((ActivitySymbolJto) jsonIo.gson().fromJson(jto,
                        ActivitySymbolJto.class)).modelElement);
         }
         else if (shape.getBpmnElement() instanceof Gateway)
         {
            updateGateway(
                  (Gateway) shape.getBpmnElement(),
                  ((GatewaySymbolJto) jsonIo.gson().fromJson(jto, GatewaySymbolJto.class)).modelElement);
         }
         else if (shape.getBpmnElement() instanceof Event)
         {
            updateEvent((Event) shape.getBpmnElement(), ((EventSymbolJto) jsonIo.gson()
                  .fromJson(jto, EventSymbolJto.class)).modelElement);
         }
         else if (shape.getBpmnElement() instanceof DataObject)
         {
            updateDataObject(
                  (DataObject) shape.getBpmnElement(),
                  ((DataSymbolJto) jsonIo.gson().fromJson(jto, DataSymbolJto.class)).modelElement);
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
   private void updateProcessDefinition(Process process, ProcessDefinitionJto processJson)
   {
      trace.info("Updating Process from JSON " + processJson.toString());
      
      process.setName(processJson.name);

      storeExtensions(process, processJson);
   }

   /**
    * 
    * @param activity
    * @param activityJson
    */
   private void updateActivity(Activity activity, ActivityJto activityJson)
   {
      Process p = (Process) activity.eContainer();

      trace.info("Contains element: " + p.getFlowElements().contains(activity));

      if (activityJson.name != null)
      {
         activity.setName(activityJson.name);
      }

      trace.info("Activity Type: " + activity);
      trace.info("Activity JSON Type: " + activityJson.activityType);

      if (activityJson.activityType != null)
      {
         if (activityJson.activityType == ModelerConstants.MANUAL_ACTIVITY)
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
         else if (activityJson.activityType == ModelerConstants.APPLICATION_ACTIVITY)
         {
            if (activityJson.participantFullId != null)
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
            else
            {
               ServiceTask serviceTask = null;

               if ( !(activity instanceof ServiceTask))
               {
                  trace.error("Conversion to Service Task not yet supported");
               }
               else
               {
                  serviceTask = (ServiceTask) activity;

                  if (true)
                  {
                     serviceTask.setImplementation("##WebService");

                     Operation operation = bpmn2Factory().createOperation();

                     operation.setId(activityJson.applicationFullId);

                     serviceTask.setOperationRef(operation);
                  }
                  else
                  {
                     serviceTask.setImplementation("##unspecified");
                  }
               }
            }
         }
      }

      storeExtensions(activity, activityJson);
   }

   /**
    * 
    * @param gateway
    * @param gatewayJson
    */
   private void updateGateway(Gateway gateway, GatewayJto gatewayJson)
   {
      if (gatewayJson.name == null)
      {
         gateway.setName(gatewayJson.name);
      }

      // gateway.setGatewayDirection(GatewayDirection.CONVERGING);

      if (gatewayJson.gatewayType == ModelerConstants.XOR_GATEWAY_TYPE)
      {
         // gateway instanceof ExclusiveGateway)
      }
      else if (gatewayJson.gatewayType == ModelerConstants.AND_GATEWAY_TYPE)
      {
         // gateway instanceof ParallelGateway
      }

      storeExtensions(gateway, gatewayJson);
   }

   /**
    * 
    * @param event
    * @param eventJson
    */
   private void updateEvent(Event event, EventJto eventJson)
   {
      if (eventJson.name != null)
      {
         event.setName(eventJson.name);
      }

      if (eventJson.eventType == ModelerConstants.START_EVENT)
      {
         // event instanceof StartEvent
      }
      // else if (eventJson.eventType == ModelerConstants.START_EVENT)
      // {
      // // event instanceof BoundaryEvent
      // }
      else if (eventJson.eventType == ModelerConstants.STOP_EVENT)
      {
         // event instanceof EndEvent
      }

      storeExtensions(event, eventJson);
   }

   /**
    * 
    * @param dataObject
    * @param dataJson
    */
   private void updateDataObject(DataObject dataObject, DataJto dataJson)
   {
      if (dataJson.name != null)
      {
         dataObject.setName(dataJson.name);
      }

      storeExtensions(dataObject, dataJson);
   }

   /**
    * 
    * @param element
    * @param jto
    */
   private void storeExtensions(BaseElement element, ModelElementJto jto)
   {
      // TODO This method will need a general mechanism of deep overwrite with keeping existing properties intact
      
      if ((null != jto.attributes) && !jto.attributes.entrySet().isEmpty())
      {
         JsonObject coreExtensions = new JsonObject();
         JsonObject attributes = getAttributes(element);

         // Merge attributes

         for (Map.Entry<String, ? > entry : jto.attributes.entrySet())
         {
            String key = entry.getKey();

            if (jto.attributes.get(key).isJsonNull())
            {
               trace.info("Setting extended attribute " + key + " to null.");
            }
            else
            {
               trace.info("Setting extended attribute " + key + " to "
                     + jto.attributes.get(key).getAsString());
               attributes.addProperty(key, jto.attributes.get(key).getAsString());
            }
         }

         coreExtensions.add(ModelerConstants.ATTRIBUTES_PROPERTY, attributes);

         Bpmn2ExtensionUtils.setExtensionFromJson(element, "core", coreExtensions);
      }
   }

   /**
    * 
    * @param element
    * @return
    */
   private JsonObject getAttributes(BaseElement element)
   {
      JsonElement attributes = Bpmn2ExtensionUtils.getExtensionAsJson(element, "core")
            .get(ModelerConstants.ATTRIBUTES_PROPERTY);

      if (attributes != null)
      {
         return attributes.getAsJsonObject();
      }
      else
      {
         return new JsonObject();
      }

   }

}
