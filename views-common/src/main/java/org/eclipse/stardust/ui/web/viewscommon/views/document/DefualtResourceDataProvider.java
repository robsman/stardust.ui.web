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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;

/**
 *
 * @author Yogesh.Manware
 */
public class DefualtResourceDataProvider implements IResourceDataProvider
{
   private String resourceName;

   private String mimeType;

   private DocumentManagementService dms;

   private boolean forDocument;

   private String resourceId;

   /**
    * @param resourceName
    * @param resourceId
    * @param mimeType
    * @param dms
    * @param forDocument
    */
   public DefualtResourceDataProvider(String resourceName, String resourceId,
         String mimeType, DocumentManagementService dms, boolean forDocument)
   {
      super();
      this.mimeType = mimeType;
      this.dms = dms;
      this.forDocument = forDocument;
      this.resourceId = resourceId;

      if (forDocument)
      {
         this.resourceName = resourceName;
      }
      else
      {
         this.resourceName = resourceName + ".zip";
      }
   }

   public String getResourceName()
   {
      return resourceName;
   }

   public String getMimeType()
   {
      return mimeType;
   }

   /*
    * (non-Javadoc)
    *
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.document.IOutputResourceDataProvider
    * #getBytes()
    */
   public byte[] getBytes()
   {
      // download a file
      if (forDocument)
      {
         if (dms.getDocument(resourceId) != null)
         {
            return dms.retrieveDocumentContent(resourceId);
         }
      }
      // download a folder
      else
      {
         return DocumentMgmtUtility.backupToZipFile(resourceId, dms);
      }
      return null;
   }
}
