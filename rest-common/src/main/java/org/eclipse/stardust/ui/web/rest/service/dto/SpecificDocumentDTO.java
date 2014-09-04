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
public class SpecificDocumentDTO
{
   private String id;

   private String name;

   private String type;

   private String data;

   private String inDataPathId;

   private String dataPathId;

   private DocumentDTO document;

   /**
    * 
    */
   public SpecificDocumentDTO()
   {

   }

   /**
    * @return the id
    */
   public String getId()
   {
      return id;
   }

   /**
    * @param id
    *           the id to set
    */
   public void setId(String id)
   {
      this.id = id;
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
    * @return the type
    */
   public String getType()
   {
      return type;
   }

   /**
    * @param type
    *           the type to set
    */
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * @return the data
    */
   public String getData()
   {
      return data;
   }

   /**
    * @param data
    *           the data to set
    */
   public void setData(String data)
   {
      this.data = data;
   }

   /**
    * @return the inDataPathId
    */
   public String getInDataPathId()
   {
      return inDataPathId;
   }

   /**
    * @param inDataPathId
    *           the inDataPathId to set
    */
   public void setInDataPathId(String inDataPathId)
   {
      this.inDataPathId = inDataPathId;
   }

   /**
    * @return
    */
   public String getOutDataPathId()
   {
      return dataPathId;
   }

   /**
    * @param outDataPathId
    */
   public void setOutDataPathId(String outDataPathId)
   {
      this.dataPathId = outDataPathId;
   }

   /**
    * @return the document
    */
   public DocumentDTO getDocument()
   {
      return document;
   }

   /**
    * @param document
    *           the document to set
    */
   public void setDocument(DocumentDTO document)
   {
      this.document = document;
   }

}
