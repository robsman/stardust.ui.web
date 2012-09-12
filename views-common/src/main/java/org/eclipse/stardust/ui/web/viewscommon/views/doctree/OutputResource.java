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
package org.eclipse.stardust.ui.web.viewscommon.views.doctree;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;

import com.icesoft.faces.context.Resource;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class OutputResource implements Resource, Serializable
{

   private static final long serialVersionUID = 1L;
   private String resourceName;
   private final Date lastModified;
   private transient InputStream inputStream;
   private transient DocumentManagementService dms;
   private String mimeType = "";
   private String resourceId = null;
   private DownloadPopupDialog downloadPopupDialog;
   private boolean forDocument;

   /**
    * custom constructor, gets invoked when single file is downloaded
    * 
    * @param resourceName
    * @param resourceId
    * @param mimeType
    * @param downloadPopup
    * @param dms
    */
   public OutputResource(String resourceName, String resourceId, String mimeType,
         DownloadPopupDialog downloadPopup, DocumentManagementService dms, boolean forDocument)
   {
      if (forDocument)
      {
         this.resourceName = resourceName;
      }
      else
      {
         this.resourceName = resourceName + ".zip";
      }

      this.downloadPopupDialog = downloadPopup;
      this.mimeType = mimeType;
      this.lastModified = new Date();
      this.resourceId = resourceId;
      this.dms = dms;
      this.forDocument = forDocument;
   }

   /**
    * gets invoked when user clicks on download button
    * 
    */
   public InputStream open() throws IOException
   {
      if (null != downloadPopupDialog)
      {
         downloadPopupDialog.closePopup();
      }
     
      if (inputStream == null)
      {
         // download a file
         if (forDocument)
         {
            if (dms.getDocument(resourceId) != null)
            {
               inputStream = new ByteArrayInputStream(dms.retrieveDocumentContent(resourceId));
            }
         }
         // download a folder
         else
         {
            inputStream = new ByteArrayInputStream(DocumentMgmtUtility.backupToZipFile(
                  resourceId, dms));
         }
      }
      else
      {
         /*
          * IMPORTANT
          * 
          * Calling reset() sets the stream pointer to the start of the stream hence making it possible to
          * read from the same stream again.
          * 
          * Without reset, an empty content would be read as the pointer must have reached the end of the stream. 
          * */
         inputStream.reset();
      }
      
      if (null == inputStream)
      {
         MessagesViewsCommonBean propsBean = new MessagesViewsCommonBean();
         throw new IOException(propsBean.getString("views.common.downloanFile.error"));
      }
      
      return inputStream;
   }

   public String calculateDigest()
   {
      return resourceName;
   }

   public Date lastModified()
   {
      return lastModified;
   }

   public void withOptions(Options arg0) throws IOException
   {}

   public String getMimeType()
   {
      return mimeType;
   }

   public String getLabel()
   {
      return resourceName;
   }
}