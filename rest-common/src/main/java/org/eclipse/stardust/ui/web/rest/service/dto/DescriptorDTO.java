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
public class DescriptorDTO
{
   private String id;

   private String name;

   private String value;

   /**
    * 
    */
   public DescriptorDTO()
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
    * @return the value
    */
   public String getValue()
   {
      return value;
   }

   /**
    * @param value
    *           the value to set
    */
   public void setValue(String value)
   {
      this.value = value;
   }

}