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
package org.eclipse.stardust.ui.web.plugin.utils;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.io.CloseableUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author Subodh.Godbole
 *
 */
public class PluginUtils
{
   private static final Logger trace = LogManager.getLogger(PluginUtils.class);
   
   /**
    * @param resolver
    * @param pattern
    * @param fetchContents
    * @return
    */
   public static List<ResourceInfo> findPluginResources(ResourcePatternResolver resolver, String pattern, boolean fetchContents)
   {
      List<ResourceInfo> allResources = newArrayList();

      try
      {
         List<Pair<String, String>> allPlugins = getAllPlugins(resolver);

         for (Pair<String, String> plugin : allPlugins)
         {
            String pluginBaseUri = plugin.getSecond();

            String locationPattern = pluginBaseUri + pattern;
            
            Resource[] matchedResources;
            try
            {
               matchedResources = resolver.getResources(locationPattern);
            }
            catch (Exception e)
            {
               // Failed, possibly no match found. Ignore.
               matchedResources = new Resource[0];
            }

            ResourceInfo rInfo;
            for (Resource resource : matchedResources)
            {
               if (resource.exists())
               {
                  try
                  {
                     if (fetchContents)
                     {
                        rInfo = new ResourceInfo(plugin.getFirst(), pluginBaseUri, resource, readResource(resource));
                     }
                     else
                     {
                        rInfo = new ResourceInfo(plugin.getFirst(), pluginBaseUri, resource);
                     }
                     allResources.add(rInfo);
                  }
                  catch (Exception e)
                  {
                     trace.error("Could not read resource: " + resource.getFile().getAbsolutePath(), e);
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.error("Unable to find resources from plugins", e);
      }

      return allResources;
   }
   
   /**
    * @param resolver
    * @param pluginId
    * @param baseUri
    * @param pattern
    * @return
    */
   public static List<String> findWebResources(ResourcePatternResolver resolver, String pluginId, String webUriBase, String baseUri, String pattern)
   {
      List<String> allResources = newArrayList();

      try
      {
         String webUriPrefix = "plugins/" + pluginId + "/" + webUriBase;
         String locationPattern = baseUri + pattern;
         
         Resource[] matchedResources;
         try
         {
            matchedResources = resolver.getResources(locationPattern);
         }
         catch (Exception e)
         {
            // Failed, possibly no match found. Ignore
            matchedResources = new Resource[0];
         }

         for (Resource resource : matchedResources)
         {
            String resourceUri = resource.getURI().toString();
            String extensionWebUri = webUriPrefix + resourceUri.substring(baseUri.length());
            allResources.add(extensionWebUri);
         }
      }
      catch (Exception e)
      {
         trace.error("Unable to find resources from plugins", e);
      }

      return allResources;
   }

   /**
    * @param resolver
    * @return
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   public static List<Pair<String, String>> getAllPlugins(ResourcePatternResolver resolver)
   {
      List<Pair<String, String>> allPlugins = newArrayList();

      try
      {
         Resource[] resources = resolver.getResources("classpath*:/META-INF/*.portal-plugin");
         for (Resource resource : resources)
         {
            String pluginId = resource.getFilename().substring(0, resource.getFilename().lastIndexOf("."));
            if (trace.isDebugEnabled())
            {
               trace.debug("Inspecting portal plugin '" + pluginId + "' (" + resource.getURI() + ")");
            }

            InputStream isPluginDescriptor = resource.getInputStream();
            try
            {
               String firstLine = new BufferedReader(new InputStreamReader(isPluginDescriptor)).readLine();
               Resource pluginBaseUriReader = resource.createRelative("../").createRelative(firstLine);
               String pluginBaseUri = pluginBaseUriReader.getURI().toString();
               if ( !pluginBaseUri.endsWith("/"))
               {
                  pluginBaseUri += "/";
               }

               allPlugins.add(new Pair(pluginId, pluginBaseUri));
            }
            finally
            {
               CloseableUtil.closeQuietly(isPluginDescriptor);
            }
         }
      }
      catch (IOException e)
      {
         trace.error("Unable to process plugins", e);
      }

      return allPlugins;
   }

   /**
    * @param resource
    * @return
    */
   public static String readResource(Resource resource) throws IOException
   {
      InputStream inputStream = resource.getInputStream();

      try
      {
         ByteArrayOutputStream contents = new ByteArrayOutputStream();
         int read = 0;
         byte[] bytes = new byte[1024];
         while ((read = inputStream.read(bytes)) != -1)
         {
            contents.write(bytes, 0, read);
         }
         
         return contents.toString();
      }
      finally
      {
         CloseableUtil.closeQuietly(inputStream);
      }
   }
}
