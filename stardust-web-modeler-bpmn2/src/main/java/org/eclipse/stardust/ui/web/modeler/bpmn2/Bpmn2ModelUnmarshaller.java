package org.eclipse.stardust.ui.web.modeler.bpmn2;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.DataJto;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;
import org.eclipse.stardust.ui.web.modeler.model.GatewayJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelElementJto;
import org.eclipse.stardust.ui.web.modeler.model.ProcessDefinitionJto;

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
         updateEvent((Event) modelElement,
               jsonIo.gson().fromJson(jto, EventJto.class));
      }
      else if (modelElement instanceof DataObject)
      {
         updateDataObject((DataObject) modelElement,
               jsonIo.gson().fromJson(jto, DataJto.class));
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
      storeAttributes(process, processJson);
   }

   /**
    * 
    * @param activity
    * @param activityJson
    */
   private void updateActivity(Activity activity, ActivityJto activityJson)
   {
      storeAttributes(activity, activityJson);
   }
   
   private void updateGateway(Gateway activity, GatewayJto activityJson)
   {
      storeAttributes(activity, activityJson);
   }

   private void updateEvent(Event activity, EventJto activityJson)
   {
      storeAttributes(activity, activityJson);
   }

   private void updateDataObject(DataObject activity, DataJto activityJson)
   {
      storeAttributes(activity, activityJson);
   }

   /**
    * 
    * @param element
    * @param attribs
    */
   private void storeAttributes(BaseElement element, ModelElementJto json)
   {
      JsonObject attributesJson = json.attributes;
      Map<String, String> attributes = new HashMap<String, String>();

      if (attributesJson != null)
      {
         for (Map.Entry<String, ? > entry : attributesJson.entrySet())
         {
            String key = entry.getKey();

            if (attributesJson.get(key).isJsonNull())
            {
               trace.info("Setting extended attribute " + key + " to null.");
            }
            else
            {
               trace.info("Setting extended attribute " + key + " to "
                     + attributesJson.get(key).getAsString());

               attributes.put(key, attributesJson.get(key).getAsString());
            }
         }
      }

      setExtensions(element, attributes);
   }

   /**
    * 
    * @param element
    * @param attribs
    */
   private void setExtensions(BaseElement element, Map<String, String> attribs)
   {
      Bpmn2ExtensionUtils.setExtensionAttributes(element, "bpmn", attribs);
   }
}
