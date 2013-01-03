/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.common;

import static java.util.Collections.emptyList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.io.CloseableUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class PortalPluginSkinResourceResolver
{
   public static final Logger trace = LogManager.getLogger(PortalPluginSkinResourceResolver.class);
   private static ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

   /**
    * 
    * @param pluginFolder
    * @param fileName
    * @return
    */
   public static Map<String, List<String>> findPluginSkins(String pluginFolder, String fileName)
   {
      Map<String, List<String>> allExtensions = new TreeMap<String, List<String>>(new Comparator<String>()
      {
         public int compare(String a, String b)
         {
            return b.compareTo(a);
         }
      });
      try
      {
         Resource[] resources = resolver.getResources("classpath*:/META-INF/*.portal-plugin");
         for (Resource resource : resources)
         {
            String pluginId = resource.getFilename().substring(0, resource.getFilename().lastIndexOf("."));
            String webUriPrefix = pluginId + "/";

            InputStream isPluginDescriptor = resource.getInputStream();
            try
            {
               String firstLine = new BufferedReader(new InputStreamReader(isPluginDescriptor)).readLine();
               Resource webContentReader = resource.createRelative("../").createRelative(firstLine);
               String webContentBaseUri = webContentReader.getURI().toString();
               if (!webContentBaseUri.endsWith("/"))
               {
                  webContentBaseUri += "/";
               }

               List<Resource> extensionResources;
               try
               {
                  extensionResources = discoverModelerExtensions(resolver, webContentBaseUri, pluginFolder, fileName);
               }
               // JBoss is throwing an IOException instead of FileNotFoundException if a
               // file cannot be found
               catch (IOException ioe)
               {
                  // failed discovering, skip category for this plugin
                  extensionResources = emptyList();
               }

               for (Resource extensionResource : extensionResources)
               {
                  // File URI
                  String extensionResUri = extensionResource.getURI().toString();
                  // Create webURi something like <plugin-id> + public/skins + folder
                  // containing file ex: <views-common/public/skins/skin1/images>
                  String extensionWebUri = webUriPrefix + extensionResUri.substring(webContentBaseUri.length());
                  // Split the above path to create 2 paths ex: a) <views-common> b)
                  // skin1/images
                  String[] splitArr = extensionWebUri.split(pluginFolder + "/");
                  String extensionWebUriKey = webUriPrefix.substring(0, webUriPrefix.lastIndexOf("/")) + pluginFolder
                        + "/" + splitArr[1].substring(0, splitArr[1].indexOf("/"));

                  List<String> resourceFile = allExtensions.get(extensionWebUriKey);
                  if (null == resourceFile)
                  {
                     resourceFile = CollectionUtils.newArrayList();
                     allExtensions.put(extensionWebUriKey, resourceFile);
                  }
                  resourceFile.add(extensionResUri);
                  trace.info("Discovered '" + pluginFolder + "' modeler extensions descriptor at " + extensionWebUri);
               }
            }
            finally
            {
               CloseableUtil.closeQuietly(isPluginDescriptor);
            }
         }
      }
      catch (Exception e)
      {
      }
      return allExtensions;

   }

   /**
    * 
    * @param resolver
    * @param modelerExtensionsBaseUri
    * @param category
    * @param fileName
    * @return
    * @throws IOException
    */
   private static List<Resource> discoverModelerExtensions(ResourcePatternResolver resolver,
         String modelerExtensionsBaseUri, String category, String fileName) throws IOException
   {
      List<Resource> extensions = CollectionUtils.newArrayList();
      Resource[] jsModules = null;
      if (null == fileName)
         jsModules = resolver.getResources(modelerExtensionsBaseUri + category.substring(category.indexOf("/") + 1)
               + "/*/*.*");
      else
         jsModules = resolver.getResources(modelerExtensionsBaseUri + category.substring(category.indexOf("/") + 1)
               + "/**/" + fileName);
      for (Resource jsModule : jsModules)
      {
         extensions.add(jsModule);
      }

      return extensions;
   }
}
