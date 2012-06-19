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

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class MailServiceImpl implements MailService
{
   private static String mailServer = Parameters.instance().getString(EngineProperties.MAIL_HOST);
   private MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();

   
   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.correspondence.MailService#sendMail(java
    * .util.Map, java.lang.String, java.lang.String, java.lang.String, java.util.List)
    */
   public boolean sendMail(Map<RecipientType, String[]> recipientDetails, String from, String subject,
         String emailContent, List<Attachment> attachments) throws MessagingException
   {
      boolean sendMailSuccess = false;
      
      // Set the host SMTP address
      Properties props = new Properties();
      if (StringUtils.isEmpty(mailServer))
      {
         MessageDialog.addErrorMessage(propsBean.getString("views.correspondenceView.details.smtpNotValidError"));
      }
      else
      {
         props.put("mail.smtp.host", mailServer);

         // Create some properties and get the default session
         Session session = Session.getDefaultInstance(props, null);
         session.setDebug(false);

         // Create a message
         Message message = new MimeMessage(session);

         // Set the from and to address
         InternetAddress addressFrom = new InternetAddress(from);
         message.setFrom(addressFrom);

         // Set recipient mail addresses
         setMailAddress(message, RecipientType.TO, recipientDetails);
         setMailAddress(message, RecipientType.CC, recipientDetails);
         setMailAddress(message, RecipientType.BCC, recipientDetails);

         message.setSubject(subject);

         Multipart multiPart = new MimeMultipart();

         BodyPart bodyPart = new MimeBodyPart();
         bodyPart.setContent(emailContent, "text/html");

         multiPart.addBodyPart(bodyPart);

         if (attachments != null)
         {
            for (int n = 0; n < attachments.size(); ++n)
            {
               BodyPart attachmentPart = new MimeBodyPart();

               DataSource source = null;
               try
               {
                  source = new ByteArrayDataSource(attachments.get(n).getContent(), attachments.get(n).getContentType());
                  attachmentPart.setDataHandler(new DataHandler(source));
                  attachmentPart.setFileName(attachments.get(n).getName());
                  multiPart.addBodyPart(attachmentPart);
               }
               catch (Exception e)
               {
                  ExceptionHandler.handleException(e,
                        propsBean.getString("views.correspondenceView.invalidAttachment"),
                        ExceptionHandler.MessageDisplayMode.CUSTOM_AND_EXCEPTION_MSG);
               }
            }
         }
         message.setContent(multiPart);
         try
         {
            Transport.send(message);
            sendMailSuccess = true;
         }
         catch (SendFailedException sendFailedException)
         {
            ExceptionHandler.handleException(sendFailedException,
                  propsBean.getString("views.correspondenceView.details.invalidFromToCcBccFax"),
                  ExceptionHandler.MessageDisplayMode.CUSTOM_AND_EXCEPTION_MSG);
         }
         catch (MessagingException me)
         {
            if (me.getNextException() instanceof UnknownHostException)
            {
               ExceptionHandler.handleException(me,
                     propsBean.getString("views.correspondenceView.details.smtpNotValidError"),
                     ExceptionHandler.MessageDisplayMode.CUSTOM_AND_EXCEPTION_MSG);
            }
            else
            {
               ExceptionHandler.handleException(me,
                     propsBean.getString("views.correspondenceView.details.smtpServerGeneralError"),
                     ExceptionHandler.MessageDisplayMode.CUSTOM_AND_EXCEPTION_MSG);
            }
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e,
                  propsBean.getString("views.correspondenceView.details.smtpServerGeneralError"),
                  ExceptionHandler.MessageDisplayMode.CUSTOM_AND_EXCEPTION_MSG);
         }
      }
      return sendMailSuccess;
   }

   /**
    * @param message
    * @param type
    * @param recipientDetails
    * @throws AddressException
    * @throws MessagingException
    */
   private void setMailAddress(Message message, RecipientType type, Map<RecipientType, String[]> recipientDetails)
         throws AddressException, MessagingException
   {
      String[] addresses = recipientDetails.get(type);
      if (null != addresses)
      {
         message.setRecipients(type, formInternetAddrArray(addresses));
      }
   }

   /**
    * @param emailIds
    * @return
    * @throws AddressException
    */
   private InternetAddress[] formInternetAddrArray(String[] emailIds) throws AddressException
   {
      InternetAddress[] address = new InternetAddress[emailIds.length];
      for (int i = 0; i < emailIds.length; i++)
      {
         address[i] = new InternetAddress(emailIds[i]);
      }
      return address;
   }
}
