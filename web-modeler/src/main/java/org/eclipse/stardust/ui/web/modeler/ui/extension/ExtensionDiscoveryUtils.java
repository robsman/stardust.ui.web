package org.eclipse.stardust.ui.web.modeler.ui.extension;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.io.CloseableUtil;

public class ExtensionDiscoveryUtils
{
   private static final Logger trace = LogManager.getLogger(ExtensionDiscoveryUtils.class);

   public static Map<String, List<String>> findExtensions(ResourcePatternResolver resolver, List<String> categories)
   {
      Map<String, List<String>> allExtensions = newHashMap();

      try
      {
         Resource[] resources = resolver.getResources("classpath*:/META-INF/*.portal-plugin");
         for (Resource resource : resources)
         {
            String pluginId = resource.getFilename().substring(0, resource.getFilename().lastIndexOf("."));
            trace.debug("Inspecting portal plugin '" + pluginId + "' (" + resource.getURI() + ") for modeler extensions ...");

            String webUriPrefix = "plugins/" + pluginId + "/";

            InputStream isPluginDescriptor = resource.getInputStream();
            try
            {
               String firstLine = new BufferedReader(new InputStreamReader(isPluginDescriptor)).readLine();
               Resource webContentReader = resource.createRelative("../").createRelative(firstLine);
               String webContentBaseUri = webContentReader.getURI().toString();
               if ( !webContentBaseUri.endsWith("/"))
               {
                  webContentBaseUri += "/";
               }

               String modelerExtensionsBaseUri = webContentBaseUri + "js/modelerExtensions/";

               for (String category : categories)
               {
                  if ( !allExtensions.containsKey(category))
                  {
                     allExtensions.put(category, new ArrayList<String>());
                  }
                  List<String> extensions = allExtensions.get(category);

                  List<Resource> extensionResources;
                  try
                  {
                     extensionResources = discoverModelerExtensions(resolver,
                           modelerExtensionsBaseUri, category);
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
            finally
            {
               CloseableUtil.closeQuietly(isPluginDescriptor);
            }
         }
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return allExtensions;
   }

   private static List<Resource> discoverModelerExtensions(ResourcePatternResolver resolver,
         String modelerExtensionsBaseUri, String category) throws IOException
   {
      List<Resource> extensions = newArrayList();

      Resource[] jsModules = resolver.getResources(modelerExtensionsBaseUri + category + "/**/*.js");
      for (Resource jsModule : jsModules)
      {
         extensions.add(jsModule);
      }

      return extensions;
   }
}
