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
package org.eclipse.stardust.ui.web.plugin.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.plugin.support.resources.PluginResourceUtils;

import com.sun.facelets.impl.DefaultResourceResolver;

/**
 * @author rsauer
 * @version $Revision: 31067 $
 */
public class PortalPluginFaceletsResourceResolver extends DefaultResourceResolver
{
   private static final Logger log = LogManager.getLogger(PortalPluginFaceletsResourceResolver.class);

   private Map/*<String, String>*/ urlPrefixes = new HashMap/*<String, String>*/();

   public URL resolveUrl(String path)
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
                     log.debug("Resolving path '" + path + "' to facelet URL " + result);
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
         result = super.resolveUrl(path);
      }
      
      return result;
   }

   private synchronized String findPluginUrlPrefix(String pluginId)
   {
      String prefix = (String) urlPrefixes.get(pluginId);

      if (null == prefix)
      {
         prefix = PluginResourceUtils.findPluginUrlPrefix(pluginId);

         if ( !StringUtils.isEmpty(prefix))
         {
            log.info("Resolved plugin ID " + pluginId + " to URL prefix " + prefix);
            
            urlPrefixes.put(pluginId, prefix);
         }
      }

      return prefix;
   }

   public String toString()
   {
      return "/plugin/-ResourceResolver";
   }

}
