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

import java.util.Locale;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.MessageSource;
import org.eclipse.stardust.ui.web.common.impl.HierarchicalMessageSource;
import org.springframework.context.NoSuchMessageException;


/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class SpringMessageSource implements HierarchicalMessageSource
{
   private final org.springframework.context.MessageSource springMsgSource;

   private MessageSource parent;

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
}