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
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class UserGroupsTableEntry extends DefaultRowModel
{
   private UserGroup userGroup;

   private String name;

   private long oid;

   private String id;

   private Date validFrom;

   private Date validTo;

   private boolean selectedRow;
   

   /**
    * @param userGroup
    * @param name
    * @param oid
    * @param id
    * @param validFrom
    * @param validTo
    */
   public UserGroupsTableEntry(UserGroup userGroup, String name, long oid, String id,
         Date validFrom, Date validTo, boolean selectedRow)
   {
      super();
      this.userGroup = userGroup;
      this.name = name;
      this.oid = oid;
      this.id = id;
      this.validFrom = validFrom;
      this.validTo = validTo;
      this.selectedRow = selectedRow;
   }

   /**
    * 
    */
   public UserGroupsTableEntry()
   {
   // TODO Auto-generated constructor stub
   }

   public UserGroup getUserGroup()
   {
      return userGroup;
   }

   public String getName()
   {
      return name;
   }

   public long getOid()
   {
      return oid;
   }

   public String getId()
   {
      return id;
   }

   public Date getValidFrom()
   {
      return validFrom;
   }

   public Date getValidTo()
   {
      return validTo;
   }

   public boolean isSelectedRow()
   {
      return selectedRow;
   }

   public void setSelectedRow(boolean selectedRow)
   {
      this.selectedRow = selectedRow;
   }

 
}
