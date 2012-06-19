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
package org.eclipse.stardust.ui.web.viewscommon.views.correspondence;

import java.util.List;
import java.util.Map;

import javax.mail.Message.RecipientType;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public interface MailService
{

   /**
    * Assist sending mail
    * 
    * @param recipientDetails
    * @param from
    * @param subject
    * @param emailContent
    * @param attachments
    * @return true if the mail sent successfully
    * @throws Exception
    * 
    */
   public boolean sendMail(Map<RecipientType, String[]> recipientDetails, String from, String subject, String emailContent, List<Attachment> attachments)
         throws Exception;
}