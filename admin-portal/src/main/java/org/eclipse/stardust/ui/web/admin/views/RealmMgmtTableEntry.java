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

import org.eclipse.stardust.engine.api.runtime.UserRealm;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;



/**
 * @author ankita.patel
 * @version $Revision: $
 */
public class RealmMgmtTableEntry extends DefaultRowModel
{
   private UserRealm userRealm;
   
   private String id;
   private String name;
   private String description;
   private boolean selectedRow;

   /**
    * 
    */
   public RealmMgmtTableEntry()
   {
   // TODO Auto-generated constructor stub
   }
   
   /**
    * @param userRealm
    * @param id
    * @param name
    * @param description
    */
   public RealmMgmtTableEntry(UserRealm userRealm, String id, String name,
         String description,boolean selectedRow)
   {
      super();
      this.userRealm = userRealm;
      this.id = id;
      this.name = name;
      this.description = description;
      this.selectedRow = selectedRow;
      
   }

   public UserRealm getUserRealm()
   {
      return userRealm;
   }

   public void setUserRealm(UserRealm userRealm)
   {
      this.userRealm = userRealm;
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

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
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
