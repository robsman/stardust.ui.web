package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.Operation;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ServiceTask;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.emf.ecore.EObject;

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
      process.setName(processJson.name);

      storeAttributes(process, processJson);
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

      storeAttributes(activity, activityJson);
      
//      if (activityJson.activityType == ModelerConstants.APPLICATION_ACTIVITY &&
//            !activity instanceof ServiceTask)
//      {
//         ServiceTask serviceTask;
//         Operation op = new Op;
//         
//         op.se
//         serviceTask.setOperationRef(arg0)
//      }
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

      storeAttributes(gateway, gatewayJson);
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

      storeAttributes(event, eventJson);
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

      storeAttributes(dataObject, dataJson);
   }

   /**
    * 
    * @param element
    * @param attribs
    */
   private void storeAttributes(BaseElement element, ModelElementJto json)
   {
      Map<String, Object> extensions = getExtensions(element);

      String attributesString = (String) extensions.get(ModelerConstants.ATTRIBUTES_PROPERTY);
      JsonObject attributes = null;

      if (attributesString != null)
      {
         trace.info("Reading JSON from attributes string " + attributesString);

         attributes = jsonIo.readJsonObject(attributesString);
      }
      else
      {
         trace.info("Creating new JSON Object");

         attributes = new JsonObject();
      }

      if (json.attributes != null)
      {
         for (Map.Entry<String, ? > entry : json.attributes.entrySet())
         {
            String key = entry.getKey();

               trace.info("Setting extended attribute " + key + " to "
                     + json.attributes.get(key).getAsString());

               attributes.addProperty(key, json.attributes.get(key).getAsString());
         }
      }

      trace.info("Attributes JSON String: " + attributes.toString());

      extensions.put(ModelerConstants.ATTRIBUTES_PROPERTY, attributes.toString());
      setExtensions(element, extensions);
   }

   /**
    * 
    * @param element
    * @param attribs
    */
   private void setExtensions(BaseElement element, Map<String, Object> attribs)
   {
      Bpmn2ExtensionUtils.setExtensionAttributes(element, "bpmn", attribs);
   }
   
   /**
    * 
    * @param element
    * @return
    */
   private Map<String, Object> getExtensions(BaseElement element)
   {
      List<Map<String, Object>> extensionAttributes = Bpmn2ExtensionUtils.getExtensionAttributes(
            element, "bpmn");
      Map<String, Object> result = new HashMap<String,Object>();//emptyMap();
      
      if ( !extensionAttributes.isEmpty())
      {
         result = extensionAttributes.get(0);
      }
      
      return result;
   }
}

