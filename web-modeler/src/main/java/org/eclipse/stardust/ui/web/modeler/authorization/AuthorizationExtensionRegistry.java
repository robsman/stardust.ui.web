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

package org.eclipse.stardust.ui.web.modeler.authorization;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.newList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.modeling.validation.*;

/**
*
* @author rainer.pielmann
*
*/
public class AuthorizationExtensionRegistry 
{
   private static final Logger trace = LogManager.getLogger(AuthorizationExtensionRegistry.class);

   private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
   List<IConfigurationElement> extensionList = null;
   private static AuthorizationExtensionRegistry authorizationExtensionRegistry;   
   private static final String[] EXCLUDE_PERMISSION_IDS =
   {
    //Todo: Adapt Testcase when uncommenting this!
     //"activity.modifyAttributes", "processDefinition.modifyAttributes", "data.modifyUserData"
   };

   AuthorizationExtensionRegistry()
   {
   }

   
   private boolean excludePermission(String permissionID)
   {
      for(String excludeId :EXCLUDE_PERMISSION_IDS)
      {
         if(excludeId.equals(permissionID))
         {
            return true;
         }
      }

      return false;
   }

   public static AuthorizationExtensionRegistry getInstance()
   {
      if (null == authorizationExtensionRegistry)
      {
         authorizationExtensionRegistry = new AuthorizationExtensionRegistry();
      }

      return authorizationExtensionRegistry;
   }

   private static JAXBContext context;

   private static JAXBContext getContext() throws JAXBException
   {
      if (context == null)
      {
         context = JAXBContext.newInstance(Plugin.class);
      }
      return context;
   }

   public static Plugin read(InputStream stream) throws JAXBException
   {
      JAXBContext context = getContext();
      Unmarshaller u = context.createUnmarshaller();
      return (Plugin) u.unmarshal(stream);
   }

   @XmlRootElement(name = "plugin")
   static class Plugin
   {
      @XmlElement(name = "extension")
      List<Extension> extensions;
   }

   static class Extension
   {
      @XmlAttribute(name = "point")
      String extensionPointId;
     
      @XmlElement(name = "permission")
      List<Permission> permissions;
   }

  
   static class Permission
   {
      @XmlAttribute(name = "defaultParticipant")
      String defaultParticipant;

      @XmlAttribute(name = "id")
      String id;

      @XmlAttribute(name = "name")
      String name;

      @XmlAttribute(name = "scope")
      String scope;
      
      @XmlAttribute(name = "fixed")
      String fixed;
   }

   public List<IConfigurationElement> getExtensionList(String pluginId,
         String extensionPointId_)
   {
      if (extensionList != null)
      {
         return extensionList;
      }
      else
      {
         extensionList = new ArrayList<IConfigurationElement>();
         List<Plugin> plugins = newList();
         try
         {
            Enumeration<URL> resources = classLoader.getResources("plugin.xml");
            while (resources.hasMoreElements())
            {
               URL url = (URL) resources.nextElement();
               InputStream openStream = url.openStream();
               try
               {
                  Plugin plugin = read(openStream);
                  plugins.add(plugin);
               }
               catch (JAXBException je)
               {
                  trace.error("Failed parsing validation extension descriptor " + url,
                        je);
               }
               finally
               {
                  openStream.close();
               }
            }
         }
         catch (IOException ioe)
         {
            trace.error("Failed resolving permission extensions: " + extensionPointId_,
                  ioe);
            return emptyList();
         }

         for (Plugin plugin : plugins)
         {
            List<Extension> extensions = plugin != null ? plugin.extensions : null;
            if (extensions != null)
            {
               for (Extension extension : extensions)
               {
                  String extensionPointId = extension.extensionPointId;
                  if (extensionPointId.equals(extensionPointId_))
                  {
                     if (extensionPointId_.equals(
                           "org.eclipse.stardust.modeling.authorization.modelElementPermission"))
                     {
                        List<Permission> permissions = extension.permissions;
                        if (permissions != null)
                        {
                           for (Permission permission : permissions)
                           {
                              if (!excludePermission(permission.scope + "." + permission.id))
                              {
                                 ServerConfigurationElement configurationElement = new ServerConfigurationElement();
                                 configurationElement.addAttribute("id", permission.id);
                                 configurationElement.addAttribute("name",
                                       permission.name);
                                 configurationElement.addAttribute("scope",
                                       permission.scope);
                                 configurationElement.addAttribute("defaultParticipant",
                                       permission.defaultParticipant);
                                 configurationElement.addAttribute("fixed",
                                       permission.fixed);
                                 extensionList.add(configurationElement);
                              }                               
                           }
                        }
                     }
                  }
               }
            }
         }
         return extensionList;
      }

   }
}