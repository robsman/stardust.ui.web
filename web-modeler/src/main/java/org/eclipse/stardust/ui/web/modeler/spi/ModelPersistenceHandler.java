package org.eclipse.stardust.ui.web.modeler.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.emf.ecore.EObject;

public interface ModelPersistenceHandler<T extends EObject>
{

   boolean canLoadModel(String contentName);

   ModelDescriptor<T> loadModel(String contentName, InputStream modelContent);

   String generateDefaultFileName(T model);

   void saveModel(T model, OutputStream modelContent) throws IOException;

   static class ModelDescriptor<T extends EObject>
   {
      public final String id;

      public final String name;

      public final T model;

      public ModelDescriptor(String id, String name, T  model)
      {
         this.id = id;
         this.name = name;
         this.model = model;
      }
   }
}
