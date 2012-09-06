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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;



/**
 * @author ankita.patel
 * @version $Revision: $
 */
public class UserDetailsTableEntry extends DefaultRowModel
{
   private User user;

   private String name;

   private long oID;

   private String account;

   private String realm;

   private Date validFrom;

   private Date validTo;

   private String email;

   private boolean selectedRow;
   
   private IParametricCallbackHandler parametricCallbackHandler;
   
   /**
    * @param user
    * @param name
    * @param oid
    * @param account
    * @param realm
    * @param validFrom
    * @param validTo
    * @param email
    */
   public UserDetailsTableEntry(User user, String name, long oid, String account,
         String realm, Date validFrom, Date validTo, String email)
   {
      super();
      this.user = user;
      this.name = name;
      oID = oid;
      this.account = account;
      this.realm = realm;
      this.validFrom = validFrom;
      this.validTo = validTo;
      this.email = email;
      
   }

   public UserDetailsTableEntry(User user, String name, long oid, String account,
         String realm, Date validFrom, Date validTo, String email, boolean selectedRow)
   {
      this(user, name, oid, account, realm, validFrom, validTo, email);
      this.selectedRow = selectedRow;
      
   }

   /**
    * 
    */
   public UserDetailsTableEntry()
   {
   }

   /**
    * @param selectedRow
    */
   public void setSelectedRow(boolean selectedRow)
   {
      this.selectedRow = selectedRow;
      if (null != parametricCallbackHandler)
      {
         parametricCallbackHandler.setParameter("selectedUser", this);
         parametricCallbackHandler.handleEvent(EventType.APPLY);
      }
   }

   
   public User getUser()
   {
      return user;
   }

   public void setUser(User user)
   {
      this.user = user;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public long getOID()
   {
      return oID;
   }

   public void setOID(long oid)
   {
      oID = oid;
   }

   public String getAccount()
   {
      return account;
   }

   public void setAccount(String account)
   {
      this.account = account;
   }

   public String getRealm()
   {
      return realm;
   }

   public void setRealm(String realm)
   {
      this.realm = realm;
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

   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   public boolean isSelectedRow()
   {
      return selectedRow;
   }

   public void setParametricCallbackHandler(IParametricCallbackHandler parametricCallbackHandler)
   {
      this.parametricCallbackHandler = parametricCallbackHandler;
   }
}
