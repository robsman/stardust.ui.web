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
package org.eclipse.stardust.ui.web.common.spring;

import static org.eclipse.stardust.ui.web.common.util.StringUtils.isEmpty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.MessageSource;
import org.eclipse.stardust.ui.web.common.impl.HierarchicalMessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;


/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class SpringMessageSource implements HierarchicalMessageSource
{
   private static final long serialVersionUID = -4723507212828384604L;

   private transient org.springframework.context.MessageSource springMsgSource;

   private MessageSource parent;
   
   private String[] baseNames;

   public SpringMessageSource(org.springframework.context.MessageSource springMsgSource)
   {
      this.springMsgSource = springMsgSource;
   }

   public String getMessage(String code, Locale locale)
   {
      return getMessage(code, null, locale);
   }

   public String getMessage(String code, String defaultMessage, Locale locale)
   {
      return getMessage(code, defaultMessage, locale, true);
   }
   
   public boolean hasMessage(String code, Locale locale)
   {
      String message = getMessage(code, "", locale, false);
      return message != null;
   }
   
   public void setParentMessageSource(MessageSource parent)
   {
      this.parent = parent;
   }
   
   /**
    * @param code
    * @param defaultMessage
    * @param locale
    * @param replaceWithDefault
    * @return
    */
   private String getMessage(String code, String defaultMessage, Locale locale, boolean replaceWithDefault)
   {
      String message = null;
      if (null != springMsgSource)
      {
         try
         {
            if (null == locale)
            {
               locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            }
            message = springMsgSource.getMessage(code, null, locale);
         }
         catch (NoSuchMessageException nsme)
         {
            // ignore
         }
      }

      if ((null == message) && (null != parent))
      {
         message = parent.getMessage(code, locale);
      }

      if (null == message && replaceWithDefault)
      {
         message = isEmpty(defaultMessage) ? code : defaultMessage;
      }

      return message;
   }
   
   /**
    * @param out
    * @throws IOException
    */
   private void writeObject(ObjectOutputStream out) throws IOException
   {
      // handle transient variables
      if (springMsgSource instanceof ResourceBundleMessageSource)
      {
         ResourceBundleMessageSource resourceBundle = (ResourceBundleMessageSource) springMsgSource;
         baseNames = extractBaseNames(resourceBundle.toString());
      }
      out.defaultWriteObject();
   }

   /**
    * @param in
    * @throws IOException
    * @throws ClassNotFoundException
    */
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      // handle transient variables
      if (null != baseNames)
      {
         ResourceBundleMessageSource resourceBundle = new ResourceBundleMessageSource();
         resourceBundle.setBasenames(baseNames);
         this.springMsgSource = resourceBundle;
      }
   }

   /**
    * utility method to extract Basenames from configuration string
    * 
    * @param configurationString
    * @return
    */
   private String[] extractBaseNames(String configurationString)
   {
      String[] baseNames = null;
      if (StringUtils.isNotEmpty(configurationString))
      {
         String baseNameStr = configurationString.substring(configurationString.indexOf("basenames=[") + 11,
               configurationString.indexOf("]"));

         if (StringUtils.isNotEmpty(baseNameStr))
         {
            baseNames = baseNameStr.split(",");
         }
      }
      return baseNames;
   }
}