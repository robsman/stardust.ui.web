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
package org.eclipse.stardust.ui.web.admin.views;

import java.util.Date;

import org.eclipse.stardust.engine.api.runtime.UserGroup;


public class UserGroupBean
{
   private String id;
   private String name;
   private String description;
   private Date validFrom;
   private Date validTo;

   public UserGroupBean()
   {
      this(null, null, null, null, null);
   }
   
   public UserGroupBean(UserGroup userGroup)
   {
      this(userGroup.getId(), userGroup.getName(), userGroup.getDescription(), 
            userGroup.getValidFrom(), userGroup.getValidTo());
   }
   
   public UserGroupBean(String id, String name, String description, Date validFrom, Date validTo)
   {
      super();
      this.id = id;
      this.name = name;
      this.description = description;
      this.validFrom = validFrom;
      this.validTo = validTo;
   }

   public String getDescription()
   {
      return description;
   }


   public void setDescription(String description)
   {
      this.description = description;
   }


   public String getId()
   {
      return id;
   }


   public void setId(String id)
   {
      this.id = id;
   }


   public String getName()
   {
      return name;
   }


   public void setName(String name)
   {
      this.name = name;
   }


   public Date getValidFrom()
   {
      return validFrom;
   }


   public void setValidFrom(Date validFrom)
   {
      this.validFrom = validFrom;
   }


   public Date getValidTo()
   {
      return validTo;
   }


   public void setValidTo(Date validTo)
   {
      this.validTo = validTo;
   }
}