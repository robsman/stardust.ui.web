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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;



/**
 * @author Subodh.Godbole
 *
 */
public class DocumentTypeWrapper
{
   private DocumentType documentType;
   private TypeDeclaration typeDeclaration;
   private String modelId;

   /**
    * String Manipulation needed because DocumentType does not expose the method to get OID
    * @param documentType
    */
   public DocumentTypeWrapper(DocumentType documentType)
   {
      this(documentType, ModelUtils.getModelForDocumentType(documentType));
   }

   /**
    * @param documentType
    * @param model
    */
   public DocumentTypeWrapper(DocumentType documentType, DeployedModel model)
   {
      this.documentType = documentType;
      
      if (null != model)
      {
         this.modelId = model.getId();
         this.typeDeclaration = model.getTypeDeclaration(documentType);
      }
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
      if (null != getDocumentType())
      result = prime * result + ((getDocumentType().getDocumentTypeId() == null) ? 0 : getDocumentType().getDocumentTypeId().hashCode());
      return result;
   }
   
   @Override
   public boolean equals(Object obj)
   {
         DocumentTypeWrapper docWrapperObj=(DocumentTypeWrapper) obj;
         return getDocumentType().getDocumentTypeId().equals(docWrapperObj.getDocumentType().getDocumentTypeId());
   }

   public String getDocumentTypeI18nName()
   {
      // TODO: Currently not supported
      return getDocumentTypeName();
   }

   public String getDocumentTypeId()
   {
      return documentType.getDocumentTypeId();
   }

   public String getDocumentTypeName()
   {
      return null != typeDeclaration ? typeDeclaration.getName() : "";
   }

   public String getModelId()
   {
      return modelId;
   }

   public DocumentType getDocumentType()
   {
      return documentType;
   }

   public TypeDeclaration getTypeDeclaration()
   {
      return typeDeclaration;
   }
}
