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
package org.eclipse.stardust.ui.web.viewscommon.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class EMailAddressValidator implements Validator
{
   public EMailAddressValidator()
   {
      viewsCommonMessage= MessagesViewsCommonBean.getInstance();
   }
   private final MessagesViewsCommonBean viewsCommonMessage;
   /**
    * Validate semicolon separated email addresses
    * 
    * @param semicolon
    *           separated emailAddresses
    * @return null if input is empty otherwise list of invalid email addresses
    */
   public static List<String> validateEmailAddresses(String emailAddresses)
   {
      List<String> invalidMailAddr = null;
      if (!StringUtils.isEmpty(emailAddresses))
      {
         invalidMailAddr = new ArrayList<String>();
         for (String emailId : emailAddresses.split(";"))
         {
            emailId = emailId.trim();
            if (StringUtils.isNotEmpty(emailId) && !validateEmailAddress(emailId))
            {
               invalidMailAddr.add(emailId);
            }
         }
      }
      return invalidMailAddr;
   }

   /**
    * validate single mail address
    * 
    * @param emailAddress
    * @return
    */
   public static boolean validateEmailAddress(String emailAddress)
   {
      // Convert to Lower Case so that (or because) in RegEx only lower case can be used
      // (or is used)
      emailAddress = emailAddress.trim().toLowerCase();

      // Set the email pattern string
      boolean matchFound = false;
      Pattern p = Pattern
            .compile("^[a-z][a-z|0-9|]*([_][a-z|0-9]+)*([.][a-z|0-9]+([_][a-z|0-9]+)*)?@[a-z][a-z|0-9|]*\\.([a-z][a-z|0-9]*(\\.[a-z][a-z|0-9]*)?)$");

      // Match the given string with the pattern
      if (StringUtils.isNotEmpty(emailAddress))
      {
         Matcher m = p.matcher(emailAddress);
         // check whether match is found
         matchFound = m.matches();
      }
      return matchFound;
   }

   public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
   {
      String mailAddress = (String) value;

      if (mailAddress != "" && !validateEmailAddress(mailAddress))
      {
         FacesMessage message = new FacesMessage();
         String msg= viewsCommonMessage.getString("common.invalidFormat.error" );
         message.setDetail(component.getAttributes().get("name") +msg);
         message.setSummary(component.getAttributes().get("name") +msg);
         message.setSeverity(FacesMessage.SEVERITY_ERROR);

         throw new ValidatorException(message);
      }
   }
}