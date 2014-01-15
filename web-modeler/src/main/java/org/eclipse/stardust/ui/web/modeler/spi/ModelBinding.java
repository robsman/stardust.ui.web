package org.eclipse.stardust.ui.web.modeler.spi;

import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;

public abstract class ModelBinding<M extends EObject>
{
   public static final Logger trace = LogManager.getLogger(ModelBinding.class);

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

   public abstract String getModelFormat(M model);

   public abstract String getModelId(M model);

   public ModelingSession getModelingSession()
   {
      return session;
   }

   public ModelNavigator<M> getNavigator()
   {
      return navigator;
   }

   public ModelPersistenceHandler<M> getPersistenceHandler(M model)
   {
      return getModelingSession().modelPersistenceService().findPersistenceHandler(
            (Class<M>) model.getClass());
   }

   public void updateModelElement(EObject modelElement, JsonObject jto)
   {
      unmarshaller.populateFromJson(modelElement, jto);
   }

   public ModelMarshaller getMarshaller()
   {
      return marshaller;
   }


   public void serializeModel(M model, OutputStream oStream,
         Map<String, String> options)
   {
      getModelingSession().modelPersistenceService().saveMode(model, oStream);
   }
}
