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
package org.eclipse.stardust.ui.web.plugin.utils;

import java.io.IOException;

import org.springframework.core.io.Resource;

/**
 * @author Subodh.Godbole
 *
 */
public class ResourceInfo
{
   private static final String WEB_PREFIX = "plugins/";

   private String pluginId;
   private String pluginLocation;
   private String pluginBaseUri;

   private Resource resource;
   private String resourceBaseUri;
   private String resourceBaseWebUri;
   private String resourceContents;

   /**
    * @param pluginId
    * @param pluginLocation
    * @param pluginBaseUri
    * @param resource
    * @param contents
    */
   public ResourceInfo(String pluginId, String pluginLocation, String pluginBaseUri, Resource resource, String contents)
         throws IOException
   {
      this.pluginId = pluginId;
      this.pluginLocation = pluginLocation;
      this.pluginBaseUri = pluginBaseUri;
      this.resource = resource;
      this.resourceContents = contents;

      String resourceUri = resource.getURI().toString();
      this.resourceBaseUri = resourceUri.substring(0, resourceUri.lastIndexOf("/")) + "/";

      this.resourceBaseWebUri = WEB_PREFIX + pluginId + "/"; 
      if (!this.resourceBaseUri.equals(this.pluginBaseUri.length()))
      {
         this.resourceBaseWebUri += this.resourceBaseUri.substring(this.pluginBaseUri.length());
         if (!this.resourceBaseWebUri.endsWith("/")) // Safety Check
         {
            this.resourceBaseWebUri += "/";
         }
      }
   }
   
   /**
    * @param pluginId
    * @param pluginLocation
    * @param webContentBaseUri
    * @param resource
    */
   public ResourceInfo(String pluginId, String pluginLocation, String webContentBaseUri, Resource resource)
         throws IOException
   {
      this(pluginId, pluginLocation, webContentBaseUri, resource, null);
   }

   @Override
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append("pluginId:").append(pluginId)
            .append(", pluginBaseUri:").append(pluginBaseUri)
            .append(", resourceBaseUri:").append(resourceBaseUri)
            .append(", resourceBaseWebUri:").append(resourceBaseWebUri);
      return sb.toString();
   }

   public String getPluginId()
   {
      return pluginId;
   }

   public String getPluginLocation()
   {
      return pluginLocation;
   }

   public String getPluginBaseUri()
   {
      return pluginBaseUri;
   }

   public Resource getResource()
   {
      return resource;
   }

   public String getResourceContents()
   {
      return resourceContents;
   }

   public String getResourceBaseUri()
   {
      return resourceBaseUri;
   }

   public String getResourceBaseWebUri()
   {
      return resourceBaseWebUri;
   }
}