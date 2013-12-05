package org.eclipse.stardust.ui.web.modeler.marshaling;

public class DefaultClassLoaderProvider implements ClassLoaderProvider
{
   /**
    *
    */
   public ClassLoader classLoader()
   {
      return getClass().getClassLoader();
   }
}
