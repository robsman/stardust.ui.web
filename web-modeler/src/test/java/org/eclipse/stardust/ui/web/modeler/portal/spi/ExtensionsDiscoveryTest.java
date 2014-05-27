package org.eclipse.stardust.ui.web.modeler.portal.spi;

import static java.util.Arrays.asList;

import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

public class ExtensionsDiscoveryTest
{

   @Test
   public void test()
   {
      ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader(
            getClass().getClassLoader()));

      System.out.println(ExtensionDiscoveryUtils.findExtensions(resolver, asList("viewManager", "diagramToolbar", "proeprtyPage")));
   }

}
