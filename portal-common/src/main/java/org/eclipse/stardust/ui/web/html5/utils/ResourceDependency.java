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
import java.util.List;

/**
 * @author Subodh.Godbole
 *
 */
public class ResourceDependency
{
   private String pluginId;
   private List<String> portalPlugins = new ArrayList<String>();
   private List<String> libs = new ArrayList<String>();
   private List<String> scripts = new ArrayList<String>();
   private List<String> styles = new ArrayList<String>();

   /**
    * 
    */
   public ResourceDependency(String pluginId)
   {
      this.pluginId = pluginId;
   }
      
   /**
    * @param pluginId
    * @param portalPlugins
    * @param libs
    * @param scripts
    * @param styles
    */
   public ResourceDependency(String pluginId, List<String> portalPlugins, List<String> libs, List<String> scripts,
         List<String> styles)
   {
      this.pluginId = pluginId;
      this.portalPlugins = portalPlugins;
      this.libs = libs;
      this.scripts = scripts;
      this.styles = styles;
   }

   public String getPluginId()
   {
      return pluginId;
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
}