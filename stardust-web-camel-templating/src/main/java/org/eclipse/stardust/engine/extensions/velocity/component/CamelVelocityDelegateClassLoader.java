package org.eclipse.stardust.engine.extensions.velocity.component;

import org.apache.camel.component.velocity.CamelVelocityClasspathResourceLoader;

public class CamelVelocityDelegateClassLoader extends ClassLoader
{

   private static final String CAMLE_VELOCITY_CLASSPATH_RESOURCE_LOADER = CamelVelocityClasspathResourceLoader.class
         .getName();

   public CamelVelocityDelegateClassLoader(ClassLoader parent)
   {
      super(parent);
   }

   protected Class< ? > findClass(String name) throws ClassNotFoundException
   {
      if (CAMLE_VELOCITY_CLASSPATH_RESOURCE_LOADER.equals(name))
      {
         return CamelVelocityClasspathResourceLoader.class;
      }
      return super.findClass(name);

   }

}
