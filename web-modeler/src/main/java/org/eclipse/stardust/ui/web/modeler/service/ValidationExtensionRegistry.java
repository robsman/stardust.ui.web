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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.model.xpdl.carnot.spi.SpiConstants;
import org.eclipse.stardust.modeling.validation.IValidationExtensionRegistry;
import org.eclipse.stardust.modeling.validation.ServerConfigurationElement;
import org.eclipse.stardust.modeling.validation.ValidationConstants;

/**
*
* @author Barry.Grotjahn
*
*/
public class ValidationExtensionRegistry implements IValidationExtensionRegistry
{
   private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();   
   private static IValidationExtensionRegistry valdationExtensionRegistry;
   private static final IConfigurationElement[] EMPTY_VALIDATORS = new IConfigurationElement[0];
   private static final String[] EXCLUDE_VALIDATION_IDS = 
   {
      "org.eclipse.stardust.modeling.validation.serializableData",
      "org.eclipse.stardust.modeling.validation.transition",
      "org.eclipse.stardust.modeling.transformation.modelElementValidator",
      "MessageSerializationApplicationValidator",
      "org.eclipse.stardust.modeling.transformation.application.validation.MessageTransformationApplicationValidator",
      "externalWebAppValidator"      
   };
      
   ValidationExtensionRegistry()
   {
   }   
   
   public IConfigurationElement[] filterElements(String extensionPointId_) throws IOException
   {      
      List<IConfigurationElement> result = CollectionUtils.newList();
      
      Enumeration<URL> resources = classLoader.getResources("plugin.xml");
      while (resources.hasMoreElements())
      {
         URL url = (URL) resources.nextElement();
         InputStream openStream = url.openStream();
         try
         {
            Plugin plugin = read(openStream);
            List<Extension> extensions = plugin != null ? plugin.extensions : null;
            if(extensions != null)
            {
               for(Extension extension : extensions)
               {
                  String extensionPointId = extension.extensionPointId;
                  
                  if(extensionPointId.equals(extensionPointId_))
                  {
                     if(extensionPointId_.equals(ValidationConstants.MODEL_VALIDATOR_EXTENSION_POINT))
                     {
                        List<ModelValidator> validators = extension.modelValidators;
                        if(validators != null)
                        {
                           for(ModelValidator validator : validators)
                           {
                              Class<?> validatorClass = validator.validatorClass;
                              ServerConfigurationElement configurationElement = new ServerConfigurationElement();
                              configurationElement.setTheClass(validatorClass);
                              result.add(configurationElement);
                           }
                        }
                     }
                     else
                     {
                        List<ModelElementValidator> validators = extension.modelElementValidators;
                        if(validators != null)
                        {
                           for(ModelElementValidator validator : validators)
                           {
                              if(!excludeValidation(validator.id))
                              {                              
                                 Class<?> validatorClass = validator.validatorClass;                              
                                 
                                 ServerConfigurationElement configurationElement = new ServerConfigurationElement();
                                 configurationElement.setTheClass(validatorClass);
                                 configurationElement.addAttribute(ValidationConstants.EP_ATTR_TARGET_TYPE, validator.targetType);
                                 configurationElement.addAttribute("pageContributor", "disabled");
                                 
                                 List<Filter> filters = validator.filters;
                                 List<IConfigurationElement> children = new ArrayList<IConfigurationElement>();
                                 if(filters != null)
                                 {
                                    for(Filter filter : filters)
                                    {
                                       ServerConfigurationElement filterElement = new ServerConfigurationElement();
                                       filterElement.addAttribute(SpiConstants.ATTR_NAME, filter.name);
                                       filterElement.addAttribute(SpiConstants.ATTR_VALUE, filter.value);
                                       children.add(filterElement);
                                    }
                                 }
                                 configurationElement.addChildren("filter", children.isEmpty() ? EMPTY_VALIDATORS : children.toArray(new IConfigurationElement[children.size()]));
                                 
                                 result.add(configurationElement);
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
      }
      
      return result.isEmpty() ? EMPTY_VALIDATORS : result.toArray(new IConfigurationElement[result.size()]);      
   }
   
   private boolean excludeValidation(String validationId)
   {
      for(String excludeId :EXCLUDE_VALIDATION_IDS)
      {
         if(excludeId.equals(validationId))
         {
            return true;
         }
      }
      
      return false;
   }
   
   public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId)
   {
      try
      {
         return filterElements(extensionPointId);
      }
      catch (IOException e)
      {
      }
      
      return EMPTY_VALIDATORS;
   }
      
   public static IValidationExtensionRegistry getInstance()
   {
      if (null == valdationExtensionRegistry)
      {
         valdationExtensionRegistry = new ValidationExtensionRegistry();
      }
      
      return valdationExtensionRegistry;
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
      
      @XmlElement(name = "modelValidator")
      List<ModelValidator> modelValidators;
      
      @XmlElement(name = "modelElementValidator")
      List<ModelElementValidator> modelElementValidators;      
   }

   static class ModelValidator
   {
      @XmlAttribute(name = "class")
      Class<?> validatorClass;
   }   
   
   static class ModelElementValidator
   {
      @XmlAttribute(name = "class")
      Class<?> validatorClass;

      @XmlAttribute(name = "id")
      String id;
      
      @XmlAttribute(name = "targetType")
      String targetType;
      
      @XmlElement(name = "filter")
      List<Filter> filters;            
   }      
   
   static class Filter
   {
      @XmlAttribute(name = "name")
      String name;

      @XmlAttribute(name = "value")
      String value;      
   }
}