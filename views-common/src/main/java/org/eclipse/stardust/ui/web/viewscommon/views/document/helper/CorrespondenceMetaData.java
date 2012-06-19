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
package org.eclipse.stardust.ui.web.viewscommon.views.document.helper;

import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class CorrespondenceMetaData extends Metadata
{
   private boolean correspondencInfoAvailble = false;

   /**
    * @param document
    */
   @SuppressWarnings("unchecked")
   public CorrespondenceMetaData(Map<String, Object> baseProperties)
   {
      super((Map<String, Object>) baseProperties.get(CommonProperties.FAX_EMAIL_MESSAGE_INFO));
      correspondencInfoAvailble = CollectionUtils.isNotEmpty(getProperties());
   }

   public boolean getFaxEnabled()
   {
      return getBooleanValue("faxEnabled");
   }

   public void setFaxEnabled(boolean faxEnabled)
   {
      setBooleanValue("faxEnabled", faxEnabled);
   }

   public boolean getMailEnabled()
   {
      return getBooleanValue("mailEnabled");
   }

   public void setMailEnabled(boolean mailEnabled)
   {
      setBooleanValue("mailEnabled", mailEnabled);
   }

   public String getFaxNumber()
   {
      return getStringValue("faxNumber");
   }

   public void setFaxNumber(String faxNumber)
   {
      setStringValue("faxNumber", faxNumber);
   }

   public String getSubject()
   {
      return getStringValue("subject");
   }

   public void setSubject(String subject)
   {
      setStringValue("subject", subject);
   }

   public String getRecipients()
   {
      return getStringValue("recipients");
   }

   public void setRecipients(String recipients)
   {
      setStringValue("recipients", recipients);
   }

   public String getCarbonCopyRecipients()
   {
      return getStringValue("carbonCopyRecipients");
   }

   public void setCarbonCopyRecipients(String carbonCopyRecipients)
   {
      setStringValue("carbonCopyRecipients", carbonCopyRecipients);
   }

   public String getBlindCarbonCopyRecipients()
   {
      return getStringValue("blindCarbonCopyRecipients");
   }

   public void setBlindCarbonCopyRecipients(String blindCarbonCopyRecipients)
   {
      setStringValue("blindCarbonCopyRecipients", blindCarbonCopyRecipients);
   }

   public String getSender()
   {
      return getStringValue("sender");
   }

   public void setSender(String sender)
   {
      setStringValue("sender", sender);
   }

   public Date getSendDate()
   {
      return getDateValue("sendDate");
   }

   public void setSendDate(Date sendDate)
   {
      setDateValue("sendDate", sendDate);
   }

   public String getAttachments()
   {
      return getStringValue("attachments");
   }

   public void setAttachments(String attachments)
   {
      setStringValue("attachments", attachments);
   }

   public boolean isCorrespondencInfoAvailble()
   {
      return correspondencInfoAvailble;
   }
}