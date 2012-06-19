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
package org.eclipse.stardust.ui.web.viewscommon.login.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.security.InvalidPasswordException;
import org.eclipse.stardust.common.security.InvalidPasswordException.FailureCode;
import org.eclipse.stardust.engine.api.runtime.PasswordRules;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;


/**
 * @author Subodh.Godbole
 * 
 */
public class PasswordUtils
{
   private static final Logger trace = LogManager.getLogger(PasswordUtils.class);

   /**
    * @return
    */
   public static PasswordRules getPasswordRules()
   {
      PasswordRules passwordRules = null;

      try
      {
         ServiceFactory sf = SessionContext.findSessionContext().getServiceFactory();
         passwordRules = sf.getAdministrationService().getPasswordRules();
      }
      catch (Exception e)
      {
         trace.error("Unable to retrieve PasswordRules", e);
      }

      return passwordRules;
   }

   /**
    * @param ipe
    * @param lineBreak
    * @return
    */
   public static String decodeInvalidPasswordMessage(InvalidPasswordException ipe,
         String lineBreak)
   {
      String msg, param;
      StringBuffer errMessages = new StringBuffer();

      if (StringUtils.isEmpty(lineBreak))
      {
         lineBreak = "     \u2022     "; 
         
      }

      List<FailureCode> filureCodes = ipe.getFailureCodes();
      if (filureCodes != null && filureCodes.size() > 0)
      {
         PasswordRules passwordRules = getPasswordRules();
         MessagePropertiesBean msgBean = MessagePropertiesBean.getInstance();
         for (FailureCode failureCode : filureCodes)
         {
            if(passwordRules != null)
            {
               msg = msgBean.getParamString("common.passwordRules.message."
                     + failureCode.toString(), getPasswordRuleParams(passwordRules, failureCode));
            }
            else
            {
               msg = msgBean.getString("common.passwordRules.message." + failureCode.toString());
            }

            errMessages.append(msg);
            errMessages.append(lineBreak);
         }
      }

      return errMessages.toString();
   }

   /**
    * @param passwordRules
    * @param failureCode
    * @return
    */
   private static String[] getPasswordRuleParams(PasswordRules passwordRules,
         FailureCode failureCode)
   {
      List<String> params = new ArrayList<String>();

      if(passwordRules != null)
      {
         if (failureCode == FailureCode.MINIMAL_PASSWORD_LENGTH)
         {
            params.add(String.valueOf(passwordRules.getMinimalPasswordLength()));
         }
         else if (failureCode == FailureCode.LETTER)
         {
            params.add(String.valueOf(passwordRules.getLetters()));
         }
         else if (failureCode == FailureCode.DIGITS)
         {
            params.add(String.valueOf(passwordRules.getDigits()));
         }
         else if (failureCode == FailureCode.MIXED_CASE)
         {
            params.add(String.valueOf(passwordRules.getMixedCase()));
         }
         else if (failureCode == FailureCode.PUNCTUATION)
         {
            params.add(String.valueOf(passwordRules.getPunctuation()));
         }
         else if (failureCode == FailureCode.DIFFERENT_CHARACTERS)
         {
            params.add(String.valueOf(passwordRules.getDifferentCharacters()));
         }
         else if (failureCode == FailureCode.PREVIOUS_PASSWORDS)
         {
            params.add(String.valueOf(passwordRules.getPasswordTracking()));
         }
      }

      return params.toArray(new String[0]);
   }
}
