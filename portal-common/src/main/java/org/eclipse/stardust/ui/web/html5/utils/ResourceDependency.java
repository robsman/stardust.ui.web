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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Subodh.Godbole
 *
 */
public class ResourceDependency
{
   private String pluginId;
   private String pluginLocation;
   private List<String> portalPlugins = new ArrayList<String>();
   private List<String> libs = new ArrayList<String>();
   private List<String> scripts = new ArrayList<String>();
   private List<String> styles = new ArrayList<String>();
   private Map<String, List<String>> skip = new HashMap<String, List<String>>();

   /**
    * 
    */
   public ResourceDependency(String pluginId)
   {
      this.pluginId = pluginId;
   }
      
   /**
    * @param pluginId
    * @param pluginLocation
    * @param portalPlugins
    * @param libs
    * @param scripts
    * @param styles
    * @param skip
    */
   public ResourceDependency(String pluginId, String pluginLocation, List<String> portalPlugins, List<String> libs,
         List<String> scripts, List<String> styles, Map<String, List<String>> skip)
   {
      this.pluginId = pluginId;
      this.pluginLocation = pluginLocation;
      this.portalPlugins = portalPlugins;
      this.libs = libs;
      this.scripts = scripts;
      this.styles = styles;
      this.skip = skip;

      if (null == this.libs)
      {
         this.libs = new ArrayList<String>();
      }
      
      if (null == this.scripts)
      {
         this.scripts = new ArrayList<String>();
      }
      
      if (null == this.styles)
      {
         this.styles = new ArrayList<String>();
      }

      if (null == this.skip)
      {
         this.skip = new HashMap<String, List<String>>();
      }
   }

   public String getPluginId()
   {
      return pluginId;
   }

   public String getPluginLocation()
   {
      return pluginLocation;
   }

   public List<String> getPortalPlugins()
   {
      return portalPlugins;
   }

   public void setPortalPlugins(List<String> portalPlugins)
   {
      this.portalPlugins = portalPlugins;
   }

   public List<String> getLibs()
   {
      return libs;
   }

   public void setLibs(List<String> libs)
   {
      this.libs = libs;
   }

   public List<String> getScripts()
   {
      return scripts;
   }

   public void setScripts(List<String> scripts)
   {
      this.scripts = scripts;
   }

   public List<String> getStyles()
   {
      return styles;
   }

   public void setStyles(List<String> styles)
   {
      this.styles = styles;
   }

   public Map<String, List<String>> getSkip()
   {
      return skip;
   }
}