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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;


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

   private PluginResourceUtils()
   {
      // utility class
   }
}
