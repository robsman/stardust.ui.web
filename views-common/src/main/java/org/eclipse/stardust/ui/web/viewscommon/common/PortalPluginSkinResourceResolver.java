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
import static org.eclipse.stardust.ui.web.plugin.support.resources.PluginResourceUtils.resolveResources;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils.PluginDescriptor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class PortalPluginSkinResourceResolver
{
   public static final Logger trace = LogManager.getLogger(PortalPluginSkinResourceResolver.class);
   public static final String IE_USER_AGENT = "_ie";
   public static final String SAFARI_USER_AGENT = "_safari";

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
         ApplicationContext context = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
         List<PluginDescriptor> resolvedPlugins = PluginUtils.getAllPlugins(context);
         if(CollectionUtils.isEmpty(resolvedPlugins))
         {
            return allExtensions;
         }
         for (PluginDescriptor plugin : resolvedPlugins)
         {
            String webUriPrefix = plugin.id + "/";

            try
            {
               String webContentBaseUri = plugin.baseUri;

               List<Resource> extensionResources;
               try
               {
                  extensionResources = discoverSkinExtensions(context, webContentBaseUri, pluginFolder, fileName);
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
                  // Skip adding browser version specific .css fileNames.
                  if (extensionResUri.contains(IE_USER_AGENT) || extensionResUri.contains(SAFARI_USER_AGENT))
                  {
                     continue;
                  }
                  // Create webURi something like <plugin-id> + public/skins + folder
                  // containing file ex: <views-common/public/skins/skin1/images>
                  String extensionWebUri = webUriPrefix + extensionResUri.substring(webContentBaseUri.length());
                  // Split the above path to create 2 paths ex: a) <views-common> b)
                  // skin1/images
                  String[] splitArr = extensionWebUri.split(pluginFolder + "/");
                  String extensionWebUriKey = (pluginFolder.startsWith(Constants.SKIN_FOLDER, 0) ? webUriPrefix
                        .substring(0, webUriPrefix.lastIndexOf("/")) : "") // if
                                                                           // plugin-folder
                                                                           // is
                                                                           // plugin-id>/public/skins>
                                                                           // skip this
                                                                           // String
                                                                           // manipulation
                                                                           // which
                                                                           // returns the
                                                                           // <plugin-id>
                        + pluginFolder
                        + (splitArr[1].contains("/") ? "/" + splitArr[1].substring(0, splitArr[1].indexOf("/")) : ""); // if
                                                                                                                 // splitArr[1]
                                                                                                                 // contains
                                                                                                                 // the
                                                                                                                 // fileName
                                                                                                                 // only
                                                                                                                 // i.e
                                                                                                                 // Red.css
                                                                                                                 // skip
                                                                                                                 // this
                                                                                                                 // manipulation
                                                                                                                 // which
                                                                                                                 // returns
                                                                                                                 // the
                                                                                                                 // parent
                                                                                                                 // folder
                                                                                                                 // for
                                                                                                                 // skinFile(i.e
                                                                                                                 // skin1/images)

                  List<String> resourceFile = allExtensions.get(extensionWebUriKey);
                  if (null == resourceFile)
                  {
                     resourceFile = CollectionUtils.newArrayList();
                     allExtensions.put(extensionWebUriKey, resourceFile);
                  }
                  resourceFile.add(extensionResUri);
                  trace.info("Discovered '" + pluginFolder + "' plugin resource at " + extensionWebUri);
               }
            }
            finally
            {
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
    * @param extensionBaseUri
    * @param category
    * @param fileName
    * @return
    * @throws IOException
    */
   private static List<Resource> discoverSkinExtensions(ResourcePatternResolver resolver,
         String extensionBaseUri, String category, String fileName) throws IOException
   {
      List<Resource> jsModules = null;
      if (null == fileName)
      {
         jsModules = resolveResources(resolver,
               extensionBaseUri + category.substring(category.indexOf("/") + 1)
                     + "/*/*.*");
      }
      else
      {
         jsModules = resolveResources(resolver,
               extensionBaseUri + category.substring(category.indexOf("/") + 1) + "/**/"
                     + fileName);
      }

      return jsModules;
   }
}
