package org.eclipse.stardust.ui.web.modeler.portal.spi;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.ui.web.plugin.support.resources.PluginResourceUtils.resolveResources;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils.PluginDescriptor;

public class ExtensionDiscoveryUtils
{
   private static final Logger trace = LogManager.getLogger(ExtensionDiscoveryUtils.class);

   public static List<String> findExtensions(ResourcePatternResolver resolver, String category)
   {
      List<String> extensions = newArrayList();

      try
      {
         List<PluginDescriptor> pluginDescriptors = PluginUtils.getAllPlugins(resolver);
         for (PluginDescriptor plugin : pluginDescriptors)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Inspecting portal plugin '" + plugin.id + "' (" + plugin.baseUri
                     + ") for modeler extensions ...");
            }

            String webUriPrefix = plugin.id + "/";

            String webContentBaseUri = plugin.baseUri;
            if ( !webContentBaseUri.endsWith("/"))
            {
               webContentBaseUri += "/";
            }

            String modelerExtensionsBaseUri = webContentBaseUri + "js/modelerExtensions/";

            List<Resource> extensionResources;
            try
            {
               extensionResources = resolveResources(resolver, modelerExtensionsBaseUri + category + "/**/*.js");
            }
            // JBoss is throwing an IOException instead of FileNotFoundException if a file cannot be found
            catch (IOException ioe)
            {
               // failed discovering, skip category for this plugin
               extensionResources = emptyList();
            }

            for (Resource extensionResource : extensionResources)
            {
               String extensionResUri = extensionResource.getURI().toString();
               String extensionWebUri = webUriPrefix + extensionResUri.substring(webContentBaseUri.length());

               trace.info("Discovered '" + category + "' modeler extensions descriptor at " + extensionWebUri);

               extensions.add(extensionWebUri);
            }
         }
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return extensions;
   }
}
