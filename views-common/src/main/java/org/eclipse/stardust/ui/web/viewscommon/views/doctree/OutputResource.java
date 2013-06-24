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

import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IResourceDataProvider;

import com.icesoft.faces.context.Resource;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class OutputResource implements Resource, Serializable
{
   IResourceDataProvider outputResourceDataProvider;

   private static final long serialVersionUID = 1L;

   private final Date lastModified;

   private transient InputStream inputStream;

   private DownloadPopupDialog downloadPopupDialog;

   /**
    * @param outputResourceDataProvider
    * @param downloadPopup
    */
   public OutputResource(IResourceDataProvider outputResourceDataProvider,
         DownloadPopupDialog downloadPopup)
   {
      this.downloadPopupDialog = downloadPopup;
      this.lastModified = new Date();
      this.outputResourceDataProvider = outputResourceDataProvider;
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
         inputStream = new ByteArrayInputStream(outputResourceDataProvider.getBytes());
      }
      else
      {
         /*
          * IMPORTANT
          *
          * Calling reset() sets the stream pointer to the start of the stream hence
          * making it possible to read from the same stream again.
          *
          * Without reset, an empty content would be read as the pointer must have reached
          * the end of the stream.
          */
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
      return outputResourceDataProvider.getResourceName();
   }

   public Date lastModified()
   {
      return lastModified;
   }

   public void withOptions(Options arg0) throws IOException
   {
   }

   public String getMimeType()
   {
      return outputResourceDataProvider.getMimeType();
   }

   public String getLabel()
   {
      return outputResourceDataProvider.getResourceName();
   }
}