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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class EMailAddressValidator implements Validator
{
   private static final Logger trace = LogManager.getLogger(EMailAddressValidator.class);
   
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
      boolean matchFound = false;
      try
      {
         if (StringUtils.isNotEmpty(emailAddress))
         {
            // Validate the email address with InternetAddress,confirming RFC822
            // standard.
            new InternetAddress(emailAddress).validate();
            matchFound = true;
         }
      }
      catch (AddressException e)
      {
         trace.warn("Email address validation failed :" + e.getLocalizedMessage());
      }
      return matchFound;
   }

   public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
   {
      String mailAddress = (String) value;

      if ((!mailAddress.equals("")) && !validateEmailAddress(mailAddress))
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