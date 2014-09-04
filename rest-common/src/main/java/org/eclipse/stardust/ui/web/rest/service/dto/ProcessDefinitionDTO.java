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

import java.util.List;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
public class ProcessDefinitionDTO
{
   private String id;

   private String name;

   private String description;

   private long modelOid;

   private List<SpecificDocumentDTO> specificDocuments;

   /**
    * 
    */
   public ProcessDefinitionDTO()
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
    * @return the description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * @param description
    *           the description to set
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return the modelOid
    */
   public long getModelOid()
   {
      return modelOid;
   }

   /**
    * @param oid
    *           the modelOid to set
    */
   public void setModelOid(long oid)
   {
      this.modelOid = oid;
   }

   /**
    * @return the specificDocuments
    */
   public List<SpecificDocumentDTO> getSpecificDocuments()
   {
      return specificDocuments;
   }

   /**
    * @param specificDocuments
    *           the specificDocuments to set
    */
   public void setSpecificDocuments(List<SpecificDocumentDTO> specificDocuments)
   {
      this.specificDocuments = specificDocuments;
   }

}
