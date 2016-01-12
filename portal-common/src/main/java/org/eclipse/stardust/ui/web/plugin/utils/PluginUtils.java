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

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.ui.web.plugin.support.resources.PluginResourceUtils.resolveResources;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.WeakHashMap;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
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
   
   private static WeakHashMap<Resource, PluginDescriptor> pluginResolutionCache = new WeakHashMap<Resource, PluginDescriptor>();

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
         List<PluginDescriptor> allPlugins = getAllPlugins(resolver);

         for (PluginDescriptor plugin : allPlugins)
         {
            String pluginBaseUri = plugin.baseUri;

            String locationPattern = pluginBaseUri + pattern;
            
            List<Resource> matchedResources;
            try
            {
               matchedResources = resolveResources(resolver, locationPattern);
            }
            catch (Exception e)
            {
               // Failed, possibly no match found. Ignore.
               matchedResources = emptyList();
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
                        rInfo = new ResourceInfo(plugin.id, plugin.location, pluginBaseUri, resource, readResource(resource));
                     }
                     else
                     {
                        rInfo = new ResourceInfo(plugin.id, plugin.location, pluginBaseUri, resource);
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
    * @param skip
    * @return
    */
   public static List<WebResource> findWebResources(ResourcePatternResolver resolver, String pluginId, String webUriBase, String baseUri, String pattern, List<String> skip)
   {
      List<WebResource> allResources = newArrayList();

      try
      {
         if (!webUriBase.endsWith("/"))
         {
            webUriBase += "/";
         }
         
         if (!baseUri.endsWith("/"))
         {
            baseUri += "/";
         }

         String webUriPrefix = "plugins/" + pluginId + "/" + webUriBase;
         String locationPattern = baseUri + pattern;
         
         List<Resource> matchedResources;
         try
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Location pattern to search for plugins: " + locationPattern);
            }
            matchedResources = resolveResources(resolver, locationPattern);
         }
         // JBoss is throwing an IOException instead of FileNotFoundException if a file cannot be found
         catch (IOException ioe)
         {
            // Failed, possibly no match found. Ignore.
            matchedResources = emptyList();
         }

         for (Resource resource : matchedResources)
         {
            String resourceUri = resource.getURI().toString();
            String pluginUri = resourceUri.substring(baseUri.length());
            if (!contains(pluginUri, skip))
            {
               String extensionWebUri = webUriPrefix + pluginUri;
               allResources.add(new WebResource(extensionWebUri, resource));
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
    * @param str
    * @param skip
    * @return
    */
   private static boolean contains(String str, List<String> skip)
   {
      if (CollectionUtils.isNotEmpty(skip))
      {
         for(String file : skip)
         {
            if (str.startsWith(file))
            {
               return true;
            }
         }
      }

      return false;
   }

   public static class PluginDescriptor
   {
      public final String id;

      public final String location;

      public final String resourcesRoot;

      public final String baseUri;

      public PluginDescriptor(String id, String location, String resourcesRoot, String baseUri)
      {
         super();
         this.id = id;
         this.location = location;
         this.resourcesRoot = resourcesRoot;
         this.baseUri = baseUri;
      }
   }

   /**
    * @param resolver
    * @return
    */
   public static synchronized List<PluginDescriptor> getAllPlugins(ResourcePatternResolver resolver)
   {
      List<PluginDescriptor> resolvedPlugins = newArrayList();
      try
      {
         List<Resource> resources;
         try
         {
            // scan for plugin descriptor files
            resources = resolveResources(resolver, "classpath*:/META-INF/*.portal-plugin");
         }
         catch (IOException ioe)
         {
            // JBoss is unable to find META-INF some times, workaround for the scenario
            trace.debug("exception occurred while searching resources with classpath*:/META-INF/*.portal-plugin");
            resources = resolveResources(resolver, "classpath*:/**/*.portal-plugin");
         }

         for (Resource resource : resources)
         {
            PluginDescriptor pluginDescriptor = pluginResolutionCache.get(resource);

            if (null == pluginDescriptor)
            {
               String pluginId = resource.getFilename().substring(0, resource.getFilename().lastIndexOf("."));
               
               if (trace.isDebugEnabled())
               {
                  trace.debug("Inspecting portal plugin '" + pluginId + "' (" + resource.getURI() + ")");
               }

               String resourceUri = resource.getURI().toString();
               String pluginLocation = resourceUri.substring(0, resourceUri.lastIndexOf("/META-INF/"));
               if (pluginLocation.endsWith("!"))
               {
                  pluginLocation = pluginLocation.substring(0, pluginLocation.length() - 1);
               }

               InputStream isPluginDescriptor = resource.getInputStream();
               try
               {
                  String resourcesRoot = new BufferedReader(new InputStreamReader(isPluginDescriptor)).readLine();
                  Resource pluginBaseUriReader = resource.createRelative("../").createRelative(resourcesRoot);
                  String pluginBaseUri = pluginBaseUriReader.getURI().toString();
                  if ( !pluginBaseUri.endsWith("/"))
                  {
                     pluginBaseUri += "/";
                  }

                  pluginDescriptor = new PluginDescriptor(pluginId, pluginLocation, resourcesRoot, pluginBaseUri);

                  if (Parameters.instance().getBoolean("Carnot.Client.Caching.PluginResolution.Enabled", true))
                  {
                     pluginResolutionCache.put(resource, pluginDescriptor);
                  }
               }
               finally
               {
                  CloseableUtil.closeQuietly(isPluginDescriptor);
               }
            }

            if (null != pluginDescriptor)
            {
               resolvedPlugins.add(pluginDescriptor);
            }
         }
      }
      catch (IOException e)
      {
         trace.error("Unable to process plugins", e);
      }

      return resolvedPlugins;
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

   /**
    * @param resource
    * @param contents
    * @return
    * @throws IOException
    */
   public static void writeResource(File file, String contents) throws IOException
   {
      
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(contents.getBytes());
   }
}
