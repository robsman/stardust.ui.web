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

package org.eclipse.stardust.ui.web.modeler.xpdl.validation;

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
import org.eclipse.stardust.model.xpdl.carnot.spi.SpiConstants;
import org.eclipse.stardust.modeling.validation.ExtensionDescriptor;
import org.eclipse.stardust.modeling.validation.IValidationExtensionRegistry;
import org.eclipse.stardust.modeling.validation.PojoExtensionDescriptor;
import org.eclipse.stardust.modeling.validation.ServerConfigurationElement;
import org.eclipse.stardust.modeling.validation.ValidationConstants;

/**
*
* @author Barry.Grotjahn
*
*/
public class ValidationExtensionRegistry implements IValidationExtensionRegistry
{
   private static final Logger trace = LogManager.getLogger(ValidationExtensionRegistry.class);

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
      "externalWebAppValidator",
      "org.eclipse.stardust.modeling.validation.trigger",
      "org.eclipse.stardust.modeling.validation.conditionalPerformer",
      "org.eclipse.stardust.modeling.validation.dataMapping",
      "org.eclipse.stardust.modeling.validation.excludeUserAction",
      "org.eclipse.stardust.modeling.validation.setDataAction",
      "org.eclipse.stardust.modeling.validation.timerEventCondition",
      "org.eclipse.stardust.modeling.validation.entity20BeanData",
      "org.eclipse.stardust.modeling.validation.entityBeanData",
      "org.eclipse.stardust.modeling.validation.primitiveData",
      "org.eclipse.stardust.modeling.validation.plainXmlData",
      "dmsDocumentValidator",
      "dmsDocumentListValidator",
      "dmsFolderValidator",
      "dmsFolderListValidator",
      "org.eclipse.stardust.modeling.validation.entity30BeanData",
      "org.eclipse.stardust.modeling.validation.jmsApplication",
      "org.eclipse.stardust.modeling.validation.plainJavaApplication",
      "org.eclipse.stardust.modeling.validation.jfcContext",
      "org.eclipse.stardust.modeling.validation.exceptionEventCondition"
   };

   ValidationExtensionRegistry()
   {
   }

   @Override
   public List<ExtensionDescriptor> getExtensionDescriptorsFor(String extensionPointId_)
   {
      List<ExtensionDescriptor> result = newList();

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
               trace.error("Failed parsing validation extension descriptor " + url, je);
            }
            finally
            {
               openStream.close();
            }
         }
      }
      catch (IOException ioe)
      {
         trace.error("Failed resolving validation extensions: " + extensionPointId_, ioe);
         return emptyList();
      }

      for (Plugin plugin : plugins)
      {
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
                           result.add(new PojoExtensionDescriptor(configurationElement));
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

                              result.add(new PojoExtensionDescriptor(configurationElement));
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return result;
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