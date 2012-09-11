package org.eclipse.stardust.ui.web.modeler.spi;

import java.io.InputStream;

import org.eclipse.emf.ecore.EObject;

public interface ModelPersistenceHandler
{

   ModelDescriptor loadModel(String contentName, InputStream modelContent);

   static class ModelDescriptor
   {
      public final String id;

      public final String name;

      public final EObject model;

      public ModelDescriptor(String id, String name, EObject model)
      {
         this.id = id;
         this.name = name;
         this.model = model;
      }
   }
}
