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

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.html5.rest.HTML5FrameworkServices;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils;
import org.eclipse.stardust.ui.web.plugin.utils.ResourceInfo;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.CollectionUtils;

/**
 * @author Subodh.Godbole
 *
 */
public class ResourceDependencyUtils
{
   private static final Logger trace = LogManager.getLogger(HTML5FrameworkServices.class);
   
   private static final String PLUGIN_DEPENDENCY_DESCRIPTOR = "**/portal-plugin-dependencies.json";
   private static final String PLUGIN_DEPENDENCY_PLUGINS = "portal-plugins";
   private static final String PLUGIN_DEPENDENCY_LIBS = "libs";
   private static final String PLUGIN_DEPENDENCY_SCRIPTS = "scripts";
   private static final String PLUGIN_DEPENDENCY_STYLES = "styles";

   /**
    * @param resolver
    * @return
    */
   @SuppressWarnings("unchecked")
   public static List<ResourceDependency> discoverDependencies(ResourcePatternResolver resolver)
   {
      List<ResourceDependency> resourceDependencies = new ArrayList<ResourceDependency>();

      List<ResourceInfo> allResources = PluginUtils.findResourcesByPlugin(resolver, PLUGIN_DEPENDENCY_DESCRIPTOR, true);

      for (ResourceInfo rInfo: allResources)
      {
         try
         {
            Map<String, Object> deps = GsonUtils.readJsonMap(rInfo.getResourceContents());
   
            ResourceDependency resDep = new ResourceDependency(rInfo.getPluginId(),
                  (List<String>) deps.get(PLUGIN_DEPENDENCY_PLUGINS), (List<String>) deps.get(PLUGIN_DEPENDENCY_LIBS),
                  (List<String>)deps.get(PLUGIN_DEPENDENCY_SCRIPTS), (List<String>)deps.get(PLUGIN_DEPENDENCY_STYLES));
   
            if(CollectionUtils.isEmpty(resDep.getLibs()))
            {
               resDep.setLibs(discoverPluginResources(resolver, rInfo, PLUGIN_DEPENDENCY_LIBS, "*.js"));
            }
            else
            {
               // TODO: Replace cdn urls with local path if available
               prefixPluginWebRoot(resDep.getLibs(), rInfo.getResourceBaseWebUri());
            }
   
            if(CollectionUtils.isEmpty(resDep.getScripts()))
            {
               resDep.setScripts(discoverPluginResources(resolver, rInfo, PLUGIN_DEPENDENCY_SCRIPTS, "*.js"));
            }
            else
            {
               prefixPluginWebRoot(resDep.getScripts(), rInfo.getResourceBaseWebUri());
            }
   
            if(CollectionUtils.isEmpty(resDep.getStyles()))
            {
               resDep.setStyles(discoverPluginResources(resolver, rInfo, PLUGIN_DEPENDENCY_STYLES, "*.css"));
            }
            else
            {
               prefixPluginWebRoot(resDep.getStyles(), rInfo.getResourceBaseWebUri());
            }
   
            resourceDependencies.add(resDep);
         }
         catch (Exception e)
         {
            trace.error("Unable to process plugin dependency for " + rInfo.getPluginId(), e);
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

      trace.debug("TARGET ORDER=\n" + targetOrder);

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
    * @param resolver
    * @param rInfo
    * @param type
    * @param pattern
    * @return
    */
   private static List<String> discoverPluginResources(ResourcePatternResolver resolver, ResourceInfo rInfo,
         String type, String pattern)
   {
      try
      {
         String baseUri = rInfo.getResource().createRelative(type).getURI().toString();
         String webUriBase = baseUri.substring(rInfo.getWebContentBaseUri().length());

         // Get Main Resources i.e. Resources at root level and not from sub folders
         List<String> allResources = PluginUtils.findWebResources(resolver, rInfo.getPluginId(), webUriBase, baseUri, "*" + pattern);
         
         // Get All Resources i.e. Resources including sub folders
         List<String> mainResources = PluginUtils.findWebResources(resolver, rInfo.getPluginId(), webUriBase, baseUri, "**/" + pattern);

         // Finally, club together, by maintaining main resources at top
         for (String resource : mainResources)
         {
            if (!allResources.contains(resource))
            {
               allResources.add(resource);
            }
         }
         
         return allResources;
      }
      catch (Exception e)
      {
         trace.error("Could not discover plugin resources of type '" + type + "' for '" + rInfo.getPluginId() + "'", e);
         return new ArrayList<String>();
      }
   }

   /**
    * @param list
    * @param prefix
    */
   private static void prefixPluginWebRoot(List<String> list, String prefix)
   {
      for(int i = 0; i < list.size(); i++)
      {
         String str = list.get(i);
         if(null != str)
         {
            if (!str.startsWith("//") && !str.startsWith("http://") && !str.startsWith("https://"))
            {
               list.set(i, prefix + str);
            }
         }
      }
   }
}