package org.eclipse.stardust.ui.web.modeler.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.defaults.DefaultElementsInitializer;
import org.eclipse.stardust.model.xpdl.builder.spi.ModelInitializer;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecordingModelPersistenceService extends ModelPersistenceService
{


   @Autowired
   public RecordingModelPersistenceService(
         List<ModelPersistenceHandler< ? extends EObject>> persistenceHandlers)
   {
      super(persistenceHandlers);
      // TODO Auto-generated constructor stub
   }


   private static final Logger trace = LogManager.getLogger(RecordingModelPersistenceService.class);

   public ModelPersistenceHandler.ModelDescriptor<? extends EObject> loadModel(String modelContentName, InputStream modelContent)
   {
      for (ModelPersistenceHandler<?> handler : persistenceHandlers)
      {
         if (handler.canLoadModel(modelContentName))
         {
            ModelPersistenceHandler.ModelDescriptor<? extends EObject> descriptor = handler.loadModel(
                  modelContentName, modelContent);
            if (null != descriptor)
            {
               return descriptor;
            }
         }
      }

      return null;
   }

}
