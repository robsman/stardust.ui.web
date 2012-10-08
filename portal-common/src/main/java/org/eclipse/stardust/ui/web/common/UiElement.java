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
package org.eclipse.stardust.ui.web.common;

import static org.eclipse.stardust.ui.web.common.util.StringUtils.isEmpty;

import java.io.Serializable;
import java.util.Locale;

import org.eclipse.stardust.ui.web.common.util.FacesUtils;

/**
 * @author robert.sauer
 * @version $Revision: $
 */
public abstract class UiElement implements MessageSource, Serializable
{
   public static final String PRE_LABEL = "label";
   public static final String PRE_LABEL_TITLE = "labelTitle";
   public static final String PRE_TITLE = "title";
   
   private static final long serialVersionUID = 1L;

   public static final String COMMON_MSG_CODE_PREFIX = "common";

   private final String name;

   private final String include;
   
   private final String definedIn;
   
   private final boolean global;

   private MessageSourceProvider messagesProvider;

   public UiElement(String name, String include, String definedIn, boolean global)
   {
      this.name = name;
      this.include = include;
      this.definedIn = definedIn;
      this.global = global;
   }

   protected abstract String getMessageCodePrefix();

   public String getMessage(String code, Locale locale)
   {
      return getMessage(code, null, locale, false);
   }

   public String getMessage(String code, String defaultMessage, Locale locale)
   {
      return getMessage(code, defaultMessage, locale, true);
   }

   public boolean hasMessage(String code, Locale locale)
   {
      // If locale is null then get the default locale from browser session
      if(locale == null)
         locale  = FacesUtils.getLocaleFromRequest();

      // E.g. views.<viewName>.<code>
      String scopeKey = applyKeyScope(code, name, getMessageCodePrefix());
      boolean hasStandardMsg = hasMessageForScopeKey(scopeKey, locale);
      
      if(!hasStandardMsg)
      {
         // Key: E.g. views.common.<code>
         String scopeKey2 = applyKeyScope(code, COMMON_MSG_CODE_PREFIX, getMessageCodePrefix());
         boolean hasMsg2 = hasMessageForScopeKey(scopeKey2, locale);

         if(!hasMsg2)
         {
            // E.g. common.<code>
            String scopeKey3 = applyKeyScope(code, COMMON_MSG_CODE_PREFIX, "");
            return hasMessageForScopeKey(scopeKey3, locale);
         }
      }  
   
      return hasStandardMsg;
   }

   /**
    * @param scopeKey
    * @param defaultMessage
    * @param locale
    * @param useDefaultMsg
    * @return
    */
   public String getMessage(String code, String defaultMessage, Locale locale, boolean useDefaultMsg)
   {
      // If locale is null then get the default locale from browser session
      if(locale == null)
         locale  = FacesUtils.getLocaleFromRequest();

      // Standard Key: E.g. views.<viewName>.<code>
      String scopeKey = applyKeyScope(code, name, getMessageCodePrefix());
      String message = getMessageForScopeKey(scopeKey, defaultMessage, locale, useDefaultMsg);

      if(hasMessageForScopeKey(scopeKey, locale))
         return message;

      // Common UiElement Key: E.g. views.common.<code>
      String scopeKey2 = applyKeyScope(code, COMMON_MSG_CODE_PREFIX, getMessageCodePrefix());
      if(hasMessageForScopeKey(scopeKey2, locale))
         return getMessageForScopeKey(scopeKey2, defaultMessage, locale, useDefaultMsg);

      // Common Key: E.g. common.<code>
      String scopeKey3 = applyKeyScope(code, COMMON_MSG_CODE_PREFIX, "");
      if(hasMessageForScopeKey(scopeKey3, locale))
         return getMessageForScopeKey(scopeKey3, defaultMessage, locale, useDefaultMsg);

      // Default Standard Key
      return message;
   }

   /**
    * @param scopeKey
    * @param defaultMessage
    * @param locale
    * @param useDefaultMsg
    * @return
    */
   private String getMessageForScopeKey(String scopeKey, String defaultMessage, Locale locale, boolean useDefaultMsg)
   {
      if ((null != messagesProvider) && (null != messagesProvider.getMessages()))
      {
         if(useDefaultMsg)
            return messagesProvider.getMessages().getMessage(scopeKey, defaultMessage, locale);
         else
            return messagesProvider.getMessages().getMessage(scopeKey, locale);
      }
      else
      {
         return "$" + scopeKey + "$";
      }
   }

   /**
    * @param scopeKey
    * @param locale
    * @return
    */
   private boolean hasMessageForScopeKey(String scopeKey, Locale locale)
   {
      if ((null != messagesProvider) && (null != messagesProvider.getMessages()))
      {
         return messagesProvider.getMessages().hasMessage(scopeKey, locale);
      }
      else
      {
         return false;
      }
   }
   
   public void setMessagesProvider(MessageSourceProvider messagesProvider)
   {
      this.messagesProvider = messagesProvider;
   }

   private String applyKeyScope(String key, String uiElementName, String prefix)
   {
      if (!isEmpty(key))
      {
         StringBuilder prefixedKey = new StringBuilder(prefix.length() + uiElementName.length() + 1 + key.length());
         prefixedKey.append(prefix).append(uiElementName).append(".").append(key);

         return prefixedKey.toString();
      }
      else
      {
         return key;
      }
   }

   public String getName()
   {
      return name;
   }

   public String getInclude()
   {
      return include;
   }

   public String getDefinedIn()
   {
      return definedIn;
   }

   public boolean isGlobal()
   {
      return global;
   }
}
