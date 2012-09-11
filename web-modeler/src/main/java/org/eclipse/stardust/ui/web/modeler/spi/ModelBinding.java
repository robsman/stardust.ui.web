package org.eclipse.stardust.ui.web.modeler.spi;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ModelElementJto;
import org.eclipse.stardust.ui.web.modeler.model.di.NodeSymbolJto;

public abstract class ModelBinding
{
   protected final ModelMarshaller marshaller;

   protected final ModelUnmarshaller unmarshaller;

   protected ModelBinding(ModelMarshaller marshaller, ModelUnmarshaller unmarshaller)
   {
      this.marshaller = marshaller;
      this.unmarshaller = unmarshaller;
   }

   public abstract boolean isCompatible(EObject model);

   public abstract <T extends ModelElementJto> EObject createModelElement(EObject model,
         T jto);

   public abstract void attachModelElement(EObject container, EObject modelElement);

   public abstract <T extends NodeSymbolJto<? extends ModelElementJto>> EObject createNodeSymbol(
         EObject model, T jto, EObject modelElement);

   public abstract void attachNodeSymbol(EObject container, EObject nodeSymbol);

   public void updateModelElement(EObject modelElement, JsonObject jto)
   {
      unmarshaller.populateFromJson(modelElement, jto);
   }

   public ModelMarshaller getMarshaller()
   {
      return marshaller;
   }
}
