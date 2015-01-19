/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.plugin.support.resources;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils.PluginDescriptor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.VfsResource;
import org.springframework.core.io.support.ResourcePatternResolver;


/**
 * @author rsauer
 * @version $Revision: 31067 $
 */
public class PluginResourceUtils
{
   private static final Logger log = LogManager.getLogger(PluginResourceUtils.class);

   public static final String PATH_PLUGINS = "/plugins/";

   public static final String PATH_META_INF = "META-INF/";

   public static final String EXT_PORTAL_PLUGIN = ".portal-plugin";

   public static final String SLASH = "/";

   private static final Map<ResourcePatternResolver, ConcurrentMap<String, Object>> resourceResolutionCache = new WeakHashMap<ResourcePatternResolver, ConcurrentMap<String, Object>>();

   public static boolean isPluginPath(String path)
   {
      return (null != path) && path.startsWith(PATH_PLUGINS);
   }

   public static String getPluginId(String path)
   {
      String pluginId = null;

      if (isPluginPath(path))
      {
         String uri = path.substring(PATH_PLUGINS.length());

         int idx = uri.indexOf(SLASH);
         if ( -1 != idx)
         {
            pluginId = uri.substring(0, idx);
         }
      }

      return pluginId;
   }

   public static String getFile(String path)
   {
      String file = "";

      if (isPluginPath(path))
      {
         String uri = path.substring(PATH_PLUGINS.length());

         int idx = uri.indexOf(SLASH);
         if ( -1 != idx)
         {
            file = uri.substring(idx);
         }
      }

      return file;
   }

   public static String findPluginUrlPrefix(String pluginId)
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (null == cl)
      {
         cl = PluginResourceUtils.class.getClassLoader();
      }

      return findPluginUrlPrefix(pluginId, cl);
   }

   public static String findPluginUrlPrefix(String pluginId, ClassLoader cl)
   {
      String prefix = null;

      String pdName = PATH_META_INF + pluginId + EXT_PORTAL_PLUGIN;

      if (log.isDebugEnabled())
      {
         log.debug("About to resolve plugin descriptor: " + pdName);
      }

      URL pdUrl = cl.getResource(pdName);

      if (null != pdUrl)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Found plugin descriptor " + pdName + " at URL " + pdUrl);
         }

         try
         {
            InputStream pdIs = pdUrl.openStream();
            try
            {
               BufferedReader pdReader = new BufferedReader(new InputStreamReader(pdIs));

               String rootPath = pdReader.readLine();
               if (rootPath.startsWith(SLASH))
               {
                  rootPath = rootPath.substring(1);
               }

               prefix = pdUrl.toString();
               prefix = prefix.substring(0, prefix.length() - pdName.length()) + rootPath;
               if (prefix.endsWith(SLASH))
               {
                  prefix = prefix.substring(0, prefix.length() - 1);
               }

               if (log.isDebugEnabled())
               {
                  log.debug("Resolved plugin ID " + pluginId + " to URL prefix " + prefix);
               }
            }
            finally
            {
               pdIs.close();
            }
         }
         catch (IOException ioe)
         {
            log.warn("Failed reading plugin descriptor " + pdName, ioe);
         }
      }

      return prefix;
   }

   /**
    *
    * @param resolver
    * @param resourcePath
    * @return
    * @throws IOException
    */
   public static Set<String> getMatchingFileNames(ResourcePatternResolver resolver, String resourcePath)
         throws IOException
   {
      Set<String> allFiles = new HashSet<String>();

      try
      {
         // TODO restrict to only plugin JARs, not any JAR having a matching package structure (classpath*: vs pluginDescriptor.baseUri)
         Set<String> resourcePathRoots = new HashSet<String>();
         for (PluginDescriptor pluginDescriptor : PluginUtils.getAllPlugins(resolver))
         {
            resourcePathRoots.add(pluginDescriptor.resourcesRoot);
         }

         for (String resourcePathRoot : resourcePathRoots)
         {
            try
            {
               List<Resource> resources = resolveResources(resolver, "classpath*:" + resourcePathRoot + resourcePath);
               for (Resource resource : resources)
               {
                  log.debug("found resoruce -> " + resource.getFilename());
                  allFiles.add(resource.getFilename());
               }
            }
            catch (Exception e)
            {
               log.debug("Exception occurred while search resoruces for : " + "classpath*:" + resourcePathRoot
                     + resourcePath, e);
            }
         }
      }
      catch (Exception e)
      {
         log.debug("Exception occurred while finding files." + resourcePath);
      }

      return allFiles;
   }

   /**
    * Resolves resources matching a given pattern against the deployment.
    * <p>
    * Applies a resolution cache (by default, can be disabled globally).
    *
    * @param resolver
    *           the Spring resource pattern resolver to be used
    * @param locationPattern
    *           the resource pattern
    * @return a list of resources matching teh given pattern
    * @throws IOException
    */
   public static List<Resource> resolveResources(ResourcePatternResolver resolver,
         String locationPattern) throws IOException
   {
      ConcurrentMap<String, Object> resolutionCache = null;

      if (Parameters.instance().getBoolean("Carnot.Client.Caching.PluginResourceResolution.Enabled", true))
      {
         synchronized (resourceResolutionCache)
         {
            resolutionCache = resourceResolutionCache.get(resolver);
            if (null == resolutionCache)
            {
               resolutionCache = newConcurrentHashMap();
               resourceResolutionCache.put(resolver, resolutionCache);
            }
         }

         // try to take advantage of a previously succeeded resolution ...
         Object resolvedResources = resolutionCache.get(locationPattern);
         if (resolvedResources instanceof Resource[])
         {
            // cache hit, yay!
            return asList((Resource[]) resolvedResources);
         }
         else if (resolvedResources instanceof String[])
         {
            // cache hit, yay!, but need to translate from URIs to actual resources
            List<Resource> resources = emptyList();
            String[] resourceUris = (String[]) resolvedResources;
            if (!isEmpty(resourceUris))
            {
               resources = newArrayList(resourceUris.length);
               for (String resourceUri : resourceUris)
               {
                  // translate cached URI to resource (avoiding life-cycle problems when
                  // caching resources directly)
                  resources.add(resolver.getResource(resourceUri));
               }
            }
            return resources;
         }
         else if (null != resolvedResources)
         {
            log.error("Ignoring unexpected resource resolution cache entry for pattern " + locationPattern + ": "
                  + resolvedResources.getClass());
         }
      }

      // try to resolve pattern against deployment ...
      List<Resource> resources = newArrayList();
      boolean foundJbossVfsResources = false;
      for (Resource resource : resolver.getResources(locationPattern))
      {
         if (log.isDebugEnabled())
         {
            log.debug("Found resource -> " + resource.getFilename());
         }

         resources.add(resource);

         foundJbossVfsResources |= resource instanceof VfsResource;
      }

      if (null != resolutionCache)
      {
         // by default, prefer to put resource URIs into the resolution cache, but for
         // JBoss, URI to resource resolution did not seem to work properly, so VFS
         // resources will preferably be cached directly
         boolean cacheResourcesInsteadOfUris = Parameters.instance().getBoolean(
               "Carnot.Client.Caching.PluginResourceResolution.CacheResources", foundJbossVfsResources);

         if (cacheResourcesInsteadOfUris)
         {
            resolutionCache.putIfAbsent(locationPattern, resources.toArray(new Resource[resources.size()]));
         }
         else
         {
            List<String> resourceUris = newArrayList(resources.size());
            for (Resource resource : resources)
            {
               // avoiding life-cycle problems with Resource by caching only resource URIs
               // and re-materializing resources upon resolution request
               resourceUris.add(resource.getURI().toString());
            }
            resolutionCache.putIfAbsent(locationPattern, resourceUris.toArray(new String[resourceUris.size()]));
         }
      }
      return resources;
   }

   private PluginResourceUtils()
   {
      // utility class
   }
}
