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

import org.eclipse.stardust.ui.web.plugin.utils.WebResource;
import org.springframework.core.io.Resource;

/**
 * @author Subodh.Godbole
 *
 */
public class ResourceDependency
{
   private String pluginId;
   private String pluginLocation;
   private Resource descriptorResource;
   
   private List<String> portalPlugins = new ArrayList<String>();
   private List<WebResource> libs = new ArrayList<WebResource>();
   private List<WebResource> scripts = new ArrayList<WebResource>();
   private List<WebResource> styles = new ArrayList<WebResource>();
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
   public ResourceDependency(String pluginId, String pluginLocation, Resource descriptorResource, List<String> portalPlugins, List<WebResource> libs,
         List<WebResource> scripts, List<WebResource> styles, Map<String, List<String>> skip)
   {
      this.pluginId = pluginId;
      this.pluginLocation = pluginLocation;
      this.descriptorResource = descriptorResource;
      this.portalPlugins = portalPlugins;
      this.libs = libs;
      this.scripts = scripts;
      this.styles = styles;
      this.skip = skip;

      if (null == this.portalPlugins)
      {
         this.portalPlugins = new ArrayList<String>();
      }

      if (null == this.libs)
      {
         this.libs = new ArrayList<WebResource>();
      }
      
      if (null == this.scripts)
      {
         this.scripts = new ArrayList<WebResource>();
      }
      
      if (null == this.styles)
      {
         this.styles = new ArrayList<WebResource>();
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

   public Resource getDescriptorResource()
   {
      return descriptorResource;
   }
   
   public List<String> getPortalPlugins()
   {
      return portalPlugins;
   }

   public void setPortalPlugins(List<String> portalPlugins)
   {
      this.portalPlugins = portalPlugins;
   }

   public List<WebResource> getLibs()
   {
      return libs;
   }

   public void setLibs(List<WebResource> libs)
   {
      this.libs = libs;
   }

   public List<WebResource> getScripts()
   {
      return scripts;
   }

   public void setScripts(List<WebResource> scripts)
   {
      this.scripts = scripts;
   }

   public List<WebResource> getStyles()
   {
      return styles;
   }

   public void setStyles(List<WebResource> styles)
   {
      this.styles = styles;
   }

   public Map<String, List<String>> getSkip()
   {
      return skip;
   }
}