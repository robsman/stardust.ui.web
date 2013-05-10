/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation
 *
 * @author Barry.Grotjahn
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.eclipse.stardust.model.xpdl.carnot.spi.IStardustExtensionRegistry;
import org.eclipse.stardust.modeling.validation.ServerConfigurationElement;

public class StardustExtensionRegistry implements IStardustExtensionRegistry
{
   private static StardustExtensionRegistry instance;
   private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
   private static final IConfigurationElement[] EMPTY_REFERENCES = new IConfigurationElement[0];

   public static StardustExtensionRegistry instance()
   {
      if (instance == null)
      {
         instance = new StardustExtensionRegistry();
      }
      return instance;
   }

   public IExtensionPoint getExtensionPoint(String expandedId)
   {
      IExtensionPoint exp = new StardustExtensionPoint();

      try
      {
         Enumeration<URL> resources = classLoader.getResources("plugin.xml");
         while (resources.hasMoreElements())
         {
            URL url = (URL) resources.nextElement();
            InputStream openStream = url.openStream();
            Plugin plugin = read(openStream);
            List<Extension> extensions = plugin != null ? plugin.extensions : null;
            if(extensions != null)
            {
               for(Extension extension : extensions)
               {
                  String extensionPointId = extension.extensionPointId;
                  if(expandedId.equals(extensionPointId))
                  {
                     IExtension ext = new StardustExtension();
                     ((StardustExtensionPoint) exp).addExtension(ext);

                     List<Object> entries = extension.types;
                     if(entries != null)
                     {
                        for(Object object : entries)
                        {
                           if(object instanceof Element)
                           {
                              ServerConfigurationElement configurationElement = new ServerConfigurationElement();
                              configurationElement.addAttribute("class", getAttributeValue(((Element) object), "class"));
                              configurationElement.addAttribute("meta", getAttributeValue(((Element) object), "meta"));
                              List<IConfigurationElement> children = new ArrayList<IConfigurationElement>();

                              NodeList attribute = ((Element) object).getElementsByTagName("attribute");
                              int lengthReferences = attribute.getLength();
                              if(lengthReferences > 0)
                              {

                                 for(int i = 0; i < lengthReferences; i++)
                                 {
                                    Node item = attribute.item(i);
                                    NamedNodeMap attributes = item.getAttributes();
                                    if(attributes != null)
                                    {
                                       ServerConfigurationElement child = new ServerConfigurationElement();

                                       int lengthAttributes = attributes.getLength();
                                       for(int j = 0; j < lengthAttributes; j++)
                                       {
                                          Attr attributeItem = (Attr) attributes.item(j);
                                          String name = attributeItem.getName();
                                          String value = attributeItem.getValue();
                                          child.addAttribute(name, value);
                                       }


                                       children.add(child);
                                    }
                                 }
                              }
                              configurationElement.addChildren("attribute", children.isEmpty() ? EMPTY_REFERENCES : children.toArray(new IConfigurationElement[children.size()]));
                              ((StardustExtension) ext).addConfigurationElement(configurationElement);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      catch (JAXBException e)
      {
      }
      catch (IOException e)
      {
      }

      return exp;
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

      @XmlAnyElement
      List<Object> types;
   }

   private String getAttributeValue(Element element, String id)
   {
      NamedNodeMap attributes = element.getAttributes();
      if(attributes != null)
      {
         int lengthAttributes = attributes.getLength();
         for(int j = 0; j < lengthAttributes; j++)
         {
            Attr attribute = (Attr) attributes.item(j);
            String name = attribute.getName();
            String value = attribute.getValue();
            if(name.equals(id))
            {
               return value;
            }
         }
      }
      return null;
   }
}