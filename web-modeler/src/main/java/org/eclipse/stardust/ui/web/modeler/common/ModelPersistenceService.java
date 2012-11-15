package org.eclipse.stardust.ui.web.modeler.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;

@Service
public class ModelPersistenceService
{
   private static final Logger trace = LogManager.getLogger(ModelPersistenceService.class);

   private final List<ModelPersistenceHandler<? extends EObject>> persistenceHandlers;

   @Autowired
   public ModelPersistenceService(
         List<ModelPersistenceHandler<? extends EObject>> persistenceHandlers)
   {
      this.persistenceHandlers = persistenceHandlers;
   }

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

   public <T extends EObject> String generateDefaultFileName(T model)
   {
      @SuppressWarnings("unchecked")
      ModelPersistenceHandler<T> handler = (ModelPersistenceHandler<T>) findPersistenceHandler(model.getClass());
      if (null != handler)
      {
         return handler.generateDefaultFileName(model);
      }

      return null;
   }

   public <T extends EObject> boolean saveMode(T model, OutputStream modelContent)
   {
      @SuppressWarnings("unchecked")
      ModelPersistenceHandler<T> handler = (ModelPersistenceHandler<T>) findPersistenceHandler(model.getClass());
      if (null != handler)
      {
         try
         {
            handler.saveModel(model, modelContent);
            return true;
         }
         catch (IOException ioe)
         {
            trace.warn("Failed saving model.", ioe);
         }
      }

      return false;
   }

   protected <T extends EObject> ModelPersistenceHandler<T> findPersistenceHandler(Class<T> modelType)
   {
      for (ModelPersistenceHandler<?> handler : persistenceHandlers)
      {
         for (Type interfaceType : handler.getClass().getGenericInterfaces())
         {
            if ((interfaceType instanceof ParameterizedType)
                  && ModelPersistenceHandler.class.equals(((ParameterizedType) interfaceType).getRawType())
                  && (((ParameterizedType) interfaceType).getActualTypeArguments()[0] instanceof Class))
            {
               Class<?> argumantType = (Class<?>) ((ParameterizedType) interfaceType).getActualTypeArguments()[0];
               if (argumantType.isAssignableFrom(modelType))
               {
                  @SuppressWarnings("unchecked")
                  ModelPersistenceHandler<T> persistenceHandler = (ModelPersistenceHandler<T>) handler;

                  return persistenceHandler;
               }
            }
         }
      }

      return null;
   }
}
