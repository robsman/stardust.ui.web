package org.eclipse.stardust.ui.web.modeler.bpmn2;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;

public class Bpmn2ModelUnmarshaller implements ModelUnmarshaller
{
   private Bpmn2Binding bpmn2Binding;

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   void setBinding(Bpmn2Binding bpmn2Binding)
   {
      this.bpmn2Binding = bpmn2Binding;
   }

   @Override
   public void populateFromJson(EObject modelElement, JsonObject jto)
   {
      throw new UnsupportedOperationException("Not yet implemented.");
   }
}
