/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */

@DTOClass
public class UserGroupDTO extends AbstractDTO
{

   @DTOAttribute("id")
   private String id;

   @DTOAttribute("OID")
   private long oid;

   @DTOAttribute("name")
   private String name;

   @DTOAttribute("validFrom.time")
   private Long validFrom;

   @DTOAttribute("validTo.time")
   private Long validTo;

   @DTOAttribute("description")
   private String description;

   /**
	 * 
	 */
   public UserGroupDTO()
   {}

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
    * @return the oid
    */
   public long getOid()
   {
      return oid;
   }

   /**
    * @param oid
    *           the oid to set
    */
   public void setOid(long oid)
   {
      this.oid = oid;
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
    * @return the validFrom
    */
   public Long getValidFrom()
   {
      return validFrom;
   }

   /**
    * @param validFrom
    *           the validFrom to set
    */
   public void setValidFrom(Long validFrom)
   {
      this.validFrom = validFrom;
   }

   /**
    * @return the validTo
    */
   public Long getValidTo()
   {
      return validTo;
   }

   /**
    * @param validTo
    *           the validTo to set
    */
   public void setValidTo(Long validTo)
   {
      this.validTo = validTo;
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

}
