package org.eclipse.stardust.ui.web.modeler.spi;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;

public abstract class ModelBinding<M extends EObject>
{
   protected final ModelingSession session;

   protected final ModelNavigator<M> navigator;

   protected final ModelMarshaller marshaller;

   protected final ModelUnmarshaller unmarshaller;

   protected ModelBinding(ModelingSession session, ModelNavigator<M> navigator,
         ModelMarshaller marshaller, ModelUnmarshaller unmarshaller)
   {
      this.session = session;
      this.navigator = navigator;
      this.marshaller = marshaller;
      this.unmarshaller = unmarshaller;
   }

   public abstract boolean isCompatible(EObject model);

   public abstract String getModelId(M model);

   public ModelingSession getModelingSession()
   {
      return session;
   }

   public ModelNavigator<M> getNavigator()
   {
      return navigator;
   }

   public void updateModelElement(EObject modelElement, JsonObject jto)
   {
      unmarshaller.populateFromJson(modelElement, jto);
   }

   public ModelMarshaller getMarshaller()
   {
      return marshaller;
   }

}
