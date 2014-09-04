/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
public class DocumentDTO
{
   private String uuid;

   private String name;

   private String contentType;

   private String path;

   private int numPages;

   private DocumentTypeDTO documentType;

   public DocumentDTO()
   {

   }

   /**
    * @return the uuid
    */
   public String getUuid()
   {
      return uuid;
   }

   /**
    * @param uuid
    *           the uuid to set
    */
   public void setUuid(String uuid)
   {
      this.uuid = uuid;
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name
    *           the name to set
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @return the contentType
    */
   public String getContentType()
   {
      return contentType;
   }

   /**
    * @param contentType
    *           the contentType to set
    */
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }

   /**
    * @return the path
    */
   public String getPath()
   {
      return path;
   }

   /**
    * @param path
    *           the path to set
    */
   public void setPath(String path)
   {
      this.path = path;
   }

   /**
    * @return the numPages
    */
   public int getNumPages()
   {
      return numPages;
   }

   /**
    * @param numPages
    *           the numPages to set
    */
   public void setNumPages(int numPages)
   {
      this.numPages = numPages;
   }

   /**
    * @return the documentType
    */
   public DocumentTypeDTO getDocumentType()
   {
      return documentType;
   }

   /**
    * @param documentType
    *           the documentType to set
    */
   public void setDocumentType(DocumentTypeDTO documentType)
   {
      this.documentType = documentType;
   }

}
