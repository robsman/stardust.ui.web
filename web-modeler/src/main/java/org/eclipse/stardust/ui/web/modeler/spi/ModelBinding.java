package org.eclipse.stardust.ui.web.modeler.spi;

import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;

public abstract class ModelBinding<M extends EObject>
{
   public static final Logger trace = LogManager.getLogger(ModelBinding.class);

   private final ModelingSession session;

   protected ModelBinding(ModelingSession session)
   {
      this.session = session;
   }

   public abstract boolean isCompatible(EObject model);

   public abstract String getModelFormat(M model);

   public abstract String getModelId(M model);

   public ModelingSession getModelingSession()
   {
      return session;
   }

   public abstract ModelNavigator<M> getNavigator();

   public abstract ModelMarshaller getMarshaller();

   public abstract ModelUnmarshaller getUnmarshaller();

   public abstract boolean isReadOnly(M model);

   /**
    *
    * @param model
    * @return
    *
    * TODO use JTO as return type
    */
   public abstract JsonArray validateModel(M model);

   public ModelPersistenceHandler<M> getPersistenceHandler(M model)
   {
      return getModelingSession().modelPersistenceService().findPersistenceHandler(
            (Class<M>) model.getClass());
   }

   public void updateModelElement(EObject modelElement, JsonObject jto)
   {
      getUnmarshaller().populateFromJson(modelElement, jto);
   }

   public void serializeModel(M model, OutputStream oStream,
         Map<String, String> options)
   {
      getModelingSession().modelPersistenceService().saveMode(model, oStream);
   }
}
