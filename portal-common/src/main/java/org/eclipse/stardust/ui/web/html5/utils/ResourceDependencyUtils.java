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
package org.eclipse.stardust.ui.web.html5.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils;
import org.eclipse.stardust.ui.web.plugin.utils.ResourceInfo;
import org.eclipse.stardust.ui.web.plugin.utils.WebResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author Subodh.Godbole
 *
 */
public class ResourceDependencyUtils
{
   private static final Logger trace = LogManager.getLogger(ResourceDependencyUtils.class);
   
   private static final String PLUGIN_DEPENDENCY_DESCRIPTOR = "**/portal-plugin-dependencies.json";
   private static final String PLUGIN_DEPENDENCY_PLUGINS = "portal-plugins";
   private static final String PLUGIN_DEPENDENCY_LIBS = "libs";
   private static final String PLUGIN_DEPENDENCY_SCRIPTS = "scripts";
   private static final String PLUGIN_DEPENDENCY_STYLES = "styles";
   private static final String PLUGIN_DEPENDENCY_SKIP = "skip";
  
   /**
    * @param resolver
    * @return
    */
   @SuppressWarnings("unchecked")
   public static List<ResourceDependency> discoverDependencies(ResourcePatternResolver resolver)
   {
      List<ResourceDependency> resourceDependencies = new ArrayList<ResourceDependency>();

      List<ResourceInfo> allResources = PluginUtils.findPluginResources(resolver, PLUGIN_DEPENDENCY_DESCRIPTOR, true);

      for (ResourceInfo rInfo: allResources)
      {
         try
         {
            Map<String, Object> deps = readJsonContents(rInfo);
            if (null != deps)
            {
               ResourceDependency resDep = new ResourceDependency(rInfo.getPluginId(), rInfo.getPluginLocation(), rInfo.getResource(),
                     (List<String>) deps.get(PLUGIN_DEPENDENCY_PLUGINS),
                     getWebResourceList(rInfo, (List<String>) deps.get(PLUGIN_DEPENDENCY_LIBS)),
                     getWebResourceList(rInfo, (List<String>)deps.get(PLUGIN_DEPENDENCY_SCRIPTS)),
                     getWebResourceList(rInfo, (List<String>)deps.get(PLUGIN_DEPENDENCY_STYLES)),
                     (Map<String, List<String>>)deps.get(PLUGIN_DEPENDENCY_SKIP));

               if (null != (List<String>) deps.get(PLUGIN_DEPENDENCY_LIBS))
               {
                  if(CollectionUtils.isEmpty(resDep.getLibs()))
                  {
                     resDep.setLibs(discoverPluginResources(resolver, rInfo, PLUGIN_DEPENDENCY_LIBS, "*.js", true, resDep
                           .getSkip().get(PLUGIN_DEPENDENCY_LIBS)));
                  }
                  else
                  {
                     discoverAndReplaceWithLocalPath(resolver, resDep.getLibs(), rInfo);
                     prefixResourceWebUri(resDep.getLibs(), rInfo.getResourceBaseWebUri() + PLUGIN_DEPENDENCY_LIBS);
                  }
               }
      
               if (null != (List<String>) deps.get(PLUGIN_DEPENDENCY_SCRIPTS))
               {
                  if(CollectionUtils.isEmpty(resDep.getScripts()))
                  {
                     resDep.setScripts(discoverPluginResources(resolver, rInfo, PLUGIN_DEPENDENCY_SCRIPTS, "*.js", true,
                           resDep.getSkip().get(PLUGIN_DEPENDENCY_SCRIPTS)));
                  }
                  else
                  {
                     prefixResourceWebUri(resDep.getScripts(), rInfo.getResourceBaseWebUri() + PLUGIN_DEPENDENCY_SCRIPTS);
                  }
               }

               if (null != (List<String>) deps.get(PLUGIN_DEPENDENCY_STYLES))
               {
                  if(CollectionUtils.isEmpty(resDep.getStyles()))
                  {
                     resDep.setStyles(discoverPluginResources(resolver, rInfo, PLUGIN_DEPENDENCY_STYLES, "*.css", false,
                           resDep.getSkip().get(PLUGIN_DEPENDENCY_STYLES)));
                  }
                  else
                  {
                     prefixResourceWebUri(resDep.getStyles(), rInfo.getResourceBaseWebUri() + PLUGIN_DEPENDENCY_STYLES);
                  }
               }

               resourceDependencies.add(resDep);
            }
            else
            {
               trace.error("Skipping dependency descriptor for: " + rInfo);
            }
         }
         catch (Exception e)
         {
            trace.error("Unexpected error in processing dependency descriptor for: " + rInfo, e);
         }
      }
      
      // Sort based on plugin interdependency
      final List<String> targetOrder = new ArrayList<String>();
      for (ResourceDependency resDep : resourceDependencies)
      {
         int index = targetOrder.indexOf(resDep.getPluginId());
         if (index == -1)
         {
            targetOrder.add(resDep.getPluginId());
            index = targetOrder.indexOf(resDep.getPluginId());
         }

         for (String parentPlugin : resDep.getPortalPlugins())
         {
            if (!targetOrder.contains(parentPlugin))
            {
               targetOrder.add(index, parentPlugin);
            }
         }
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Dependency descriptors sorted by interdependency: " + targetOrder);
      }

      Collections.sort(resourceDependencies, new Comparator<ResourceDependency>()
      {
         public int compare(ResourceDependency rd1, ResourceDependency rd2)
         {
            Integer index1 = targetOrder.indexOf(rd1.getPluginId());
            Integer index2 = targetOrder.indexOf(rd2.getPluginId());
            
            return index1.compareTo(index2);
         }
      });
      
      return resourceDependencies;
   }

   /**
    * @param rInfo
    * @param list
    * @return
    */
   private static List<WebResource> getWebResourceList(ResourceInfo rInfo, List<String> list)
   {
      List<WebResource> webResourceList = new ArrayList<WebResource>();

      if (null != list)
      {
         for (String str : list)
         {
            try
            {
               webResourceList.add(new WebResource(str, rInfo.getResource().createRelative(str)));
            }
            catch(Exception e)
            {
               trace.error("Could not resolve " + str, e);
            }
         }
      }

      return webResourceList;
   }

   /**
    * @param rInfo
    * @return
    */
   private static Map<String, Object> readJsonContents(ResourceInfo rInfo)
   {
      try
      {
         Map<String, Object> deps = GsonUtils.readJsonMap(rInfo.getResourceContents());

         if (isListOfStrings(deps.get(PLUGIN_DEPENDENCY_PLUGINS))
               && isListOfStrings(deps.get(PLUGIN_DEPENDENCY_LIBS))
               && isListOfStrings(deps.get(PLUGIN_DEPENDENCY_SCRIPTS))
               && isListOfStrings(deps.get(PLUGIN_DEPENDENCY_STYLES))
               && isMapOfStringWithListStrings(deps.get(PLUGIN_DEPENDENCY_SKIP)))
         {
            return deps;
         }

         trace.error("Dependency descriptor is in incorrect format for: " + rInfo.getResourceBaseUri());
      }
      catch (Exception e)
      {
         trace.error("Unable to read dependency descriptor for: " + rInfo.getResourceBaseUri(), e);
      }
      
      return null;
   }

   /**
    * @param objToTest
    * @return
    */
   private static boolean isListOfStrings(Object objToTest)
   {
      if (null == objToTest)
      {
         return true;
      }
      else if (objToTest instanceof List<?>)
      {
         List<?> listToTest = (List<?>)objToTest;
         for (Object object : listToTest)
         {
            if (!(object instanceof String))
            {
               return false;
            }
         }
         
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * @param objToTest
    * @return
    */
   private static boolean isMapOfStringWithListStrings(Object objToTest)
   {
      if (null == objToTest)
      {
         return true;
      }
      else if (objToTest instanceof Map<?, ?>)
      {
         Map<?, ?> mapToTest = (Map<?, ?>)objToTest;
         for (Entry<?, ?> entry : mapToTest.entrySet())
         {
            if ( !(entry.getKey() instanceof String && isListOfStrings(entry.getValue())) )
            {
               return false;
            }
         }
         
         return true;
      }
      else
      {
         return false;
      }
   }
   
   /**
    * @param resolver
    * @param rInfo
    * @param type
    * @param pattern
    * @param asc
    * @param skip
    * @return
    */
   private static List<WebResource> discoverPluginResources(ResourcePatternResolver resolver, ResourceInfo rInfo,
         String type, String pattern, boolean asc, List<String> skip)
   {
      try
      {
         String baseUri = rInfo.getResource().createRelative(type).getURI().toString();
         String webUriBase = baseUri.substring(rInfo.getPluginBaseUri().length());

         // Get All Resources i.e. Resources including sub folders
         List<WebResource> allResources = PluginUtils.findWebResources(resolver, rInfo.getPluginId(), webUriBase, baseUri, "**/" + pattern, skip);
         
         // Sort
         sortByFolderHierarchy(allResources, asc);
         
         return allResources;
      }
      catch (Exception e)
      {
         trace.error("Could not discover plugin resources of type '" + type + "' for '" + rInfo.getPluginId() + "'", e);
         return new ArrayList<WebResource>();
      }
   }

   /**
    * @param list
    * @param prefix
    */
   private static void prefixResourceWebUri(List<WebResource> list, String prefix)
   {
      if (!prefix.endsWith("/"))
      {
         prefix += "/";
      }

      for(int i = 0; i < list.size(); i++)
      {
         WebResource webResource = list.get(i);
         if(null != webResource.webUri && !isCdnUri(webResource.webUri))
         {
            webResource.webUri = prefix + webResource.webUri;
         }
      }
   }

   /**
    * @param resolver
    * @param list
    * @param rInfo
    */
   private static void discoverAndReplaceWithLocalPath(ResourcePatternResolver resolver, List<WebResource> list, ResourceInfo rInfo)
   {
      for(int i = 0; i < list.size(); i++)
      {
         WebResource webResource = list.get(i);
         if(null != webResource.webUri && isCdnUri(webResource.webUri))
         {
            // URI format = //web/path/libname/version/library.js
            List<String> uriParts = StringUtils.splitAndKeepOrder(webResource.webUri, "/");
            if (uriParts.size() > 3)
            {
               String location = rInfo.getResourceBaseUri() + "libs/";
               location += uriParts.get(uriParts.size() - 3) + "/" + uriParts.get(uriParts.size() - 2) + "/"
                     + uriParts.get(uriParts.size() - 1);
               
               try
               {
                  Resource resource = resolver.getResource(location);
                  if (resource.exists())
                  {
                     String localUri = resource.getURI().toString().substring(rInfo.getResourceBaseUri().length());
                     webResource.webUri = localUri;
                  }
               }
               catch (Exception e)
               {
                  trace.warn("Unexpected error in replacing javascript with local Path for: '" + webResource.webUri
                        + "' under plugin: " + rInfo.getPluginId(), e);
               }
            }
         }
      }
   }

   /**
    * @param uri
    * @return
    */
   private static boolean isCdnUri(String uri)
   {
      return uri.startsWith("//") || uri.startsWith("http://") || uri.startsWith("https://");
   }

   /**
    * @param list
    * @param asc
    */
   private static void sortByFolderHierarchy(List<WebResource> list, boolean asc)
   {
      Collections.sort(list, new Comparator<WebResource>()
      {
         @Override
         public int compare(WebResource wr1, WebResource wr2)
         {
            String[] folders1 = wr1.webUri.substring(0, wr1.webUri.lastIndexOf("/")).split("/");
            String[] folders2 = wr2.webUri.substring(0, wr2.webUri.lastIndexOf("/")).split("/");

            // Compare folders
            int length = folders1.length < folders2.length ? folders1.length : folders2.length;
            for (int i = 0; i < length; i++)
            {
               int comp = folders1[i].compareTo(folders2[i]);
               if (comp != 0)
               {
                  return comp;
               }
            }

            // Files are from same folder hierarchy, so compare remaining part
            if (folders1.length == folders2.length)
            {
               // Compare file name without extension
               String name1 = wr1.webUri.substring(wr1.webUri.lastIndexOf("/") + 1);
               name1 = name1.substring(0, name1.lastIndexOf("."));

               String name2 = wr2.webUri.substring(wr2.webUri.lastIndexOf("/") + 1);
               name2 = name2.substring(0, name2.lastIndexOf("."));
               
               return name1.compareTo(name2);
            }
            else
            {
               // Files from sub folders appear later
               return ((Integer)folders1.length).compareTo(folders2.length);
            }
         }
      });

      if(!asc)
      {
         Collections.reverse(list);
      }
   }
   
   public static List<ResourceDependency> discoverAllDependencies(ResourcePatternResolver resolver)
   {
      List<ResourceDependency> resourceDependencies = new ArrayList<ResourceDependency>();

      List<ResourceInfo> allResources = PluginUtils.findPluginResources(resolver, "", true);

      for (ResourceInfo rInfo : allResources)
      {

         ResourceDependency resDep = new ResourceDependency(rInfo.getPluginId(), rInfo.getPluginLocation(),
               rInfo.getResource(), null, null, null, null, null);

         if (CollectionUtils.isEmpty(resDep.getLibs()))
         {
            resDep.setLibs(discoverPluginResources(resolver, rInfo, "", "*.js", true, null));
         }
         else
         {
            discoverAndReplaceWithLocalPath(resolver, resDep.getLibs(), rInfo);
            prefixResourceWebUri(resDep.getLibs(), rInfo.getResourceBaseWebUri());
         }

         if (CollectionUtils.isEmpty(resDep.getStyles()))
         {
            resDep.setStyles(discoverPluginResources(resolver, rInfo, "", "*.css", false, null));
         }
         else
         {
            prefixResourceWebUri(resDep.getStyles(), rInfo.getResourceBaseWebUri());
         }

         resourceDependencies.add(resDep);

      }

      // Sort based on plugin interdependency
      final List<String> targetOrder = new ArrayList<String>();
      for (ResourceDependency resDep : resourceDependencies)
      {
         int index = targetOrder.indexOf(resDep.getPluginId());
         if (index == -1)
         {
            targetOrder.add(resDep.getPluginId());
            index = targetOrder.indexOf(resDep.getPluginId());
         }

         for (String parentPlugin : resDep.getPortalPlugins())
         {
            if (!targetOrder.contains(parentPlugin))
            {
               targetOrder.add(index, parentPlugin);
            }
         }
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Dependency descriptors sorted by interdependency: " + targetOrder);
      }

      Collections.sort(resourceDependencies, new Comparator<ResourceDependency>()
      {
         public int compare(ResourceDependency rd1, ResourceDependency rd2)
         {
            Integer index1 = targetOrder.indexOf(rd1.getPluginId());
            Integer index2 = targetOrder.indexOf(rd2.getPluginId());

            return index1.compareTo(index2);
         }
      });

      return resourceDependencies;
   }
}