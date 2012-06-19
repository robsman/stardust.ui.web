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

import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;

/**
 * 
 * @author Yogesh.Manware
 *
 */
public class DocumentTypeWrapper extends org.eclipse.stardust.engine.extensions.dms.data.DocumentType
{
   private static final long serialVersionUID = 1L;
   private int modelOID;

   public DocumentTypeWrapper(DocumentType documentType, int modelOID)
   {
      super(documentType.getDocumentTypeId(), documentType.getSchemaLocation());
      this.modelOID = modelOID;  
   }

   /**
    * @return the modelOID
    */
   public int getModelOID()
   {
      return modelOID;
   }

   /**
    * @param modelOID
    *           the modelOID to set
    */
   public void setModelOID(int modelOID)
   {
      this.modelOID = modelOID;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((getDocumentTypeId() == null) ? 0 : getDocumentTypeId().hashCode());
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof DocumentType)
      {
         boolean equal = true;
         DocumentType docType = (DocumentType) obj;

         String docTypeId = docType.getDocumentTypeId();

         if (docTypeId != null && getDocumentTypeId() == null || docTypeId == null && getDocumentTypeId() != null)
         {
            equal = false;
         }
         if (equal && docTypeId != null && !docTypeId.equals(getDocumentTypeId()))
         {
            equal = false;
         }

         return equal;
      }
      return super.equals(obj);
   }
}
