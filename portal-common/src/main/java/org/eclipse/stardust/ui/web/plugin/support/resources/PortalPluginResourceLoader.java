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
package org.eclipse.stardust.ui.web.plugin.support.resources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author rsauer
 * @version $Revision: 31067 $
 */
public class PortalPluginResourceLoader extends ClassLoaderResourceLoader
{
   private static final Logger log = LogManager.getLogger(PortalPluginResourceLoader.class);

   private Map<String, String> urlPrefixes = new HashMap<String, String>();

   public PortalPluginResourceLoader(ResourceLoader parent)
   {
      super(parent);
   }

   @Override
   protected URL findResource(HttpServletRequest request, String path) throws IOException
   {
      URL result = null;
      
      if (PluginResourceUtils.isPluginPath(path))
      {
         String pluginId = PluginResourceUtils.getPluginId(path);
         if ( !StringUtils.isEmpty(pluginId))
         {
            String urlPrefix = findPluginUrlPrefix(pluginId);

            if ( !StringUtils.isEmpty(urlPrefix))
            {
               try
               {
                  String file = PluginResourceUtils.getFile(path);
                  
                  result = new URL(urlPrefix + file);

                  if (log.isDebugEnabled())
                  {
                     log.debug("Resolving path '" + path + "' to resource URL " + result);
                  }
               }
               catch (MalformedURLException mue)
               {
                  log.warn("Failed resolving plugin URL " + path, mue);
               }
            }
         }
      }

      if (null == result)
      {
         result = super.findResource(request, path);
      }
      
      return result;
   }

   private synchronized String findPluginUrlPrefix(String pluginId)
   {
      String prefix = urlPrefixes.get(pluginId);

      if (null == prefix)
      {
         prefix = PluginResourceUtils.findPluginUrlPrefix(pluginId, getClassLoader());

         if ( !StringUtils.isEmpty(prefix))
         {
            log.info("Resolved plugin ID " + pluginId + " to URL prefix " + prefix);
            
            urlPrefixes.put(pluginId, prefix);
         }
      }

      return prefix;
   }

}
