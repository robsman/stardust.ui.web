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
/**
 * 
 */
package org.eclipse.stardust.ui.web.bcc.jsf;

import java.io.Serializable;
import java.util.Iterator;

import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



public class UserItem implements Serializable
{
   private final static long serialVersionUID = 1l;
   
   private User user;

   private long roleCount;

   private long directItemCount;

   private long indirectItemCount;

   private boolean loggedIn;
   
   private Boolean hasAdminRole;
   
   private Authorization authorization;
   
   private String userName;

   public UserItem(User user, boolean loggedIn)
   {
      setUser(user);
      this.loggedIn = loggedIn;
   }

   public void setUser(User user)
   {
      this.user = user;
      authorization = null;
      hasAdminRole = null;
      this.userName = UserUtils.getUserDisplayLabel(user);
   }

   public void computeRoleCount()
   {
      roleCount = user.getAllGrants().size();
   }

   public User getUser()
   {
      return user;
   }

   public long getDirectItemCount()
   {
      return directItemCount;
   }

   public void addDirectItemCount(long count)
   {
      directItemCount += count;
   }

   public void removeDirectItemCount(long count)
   {
      directItemCount -= count;
      directItemCount = directItemCount > 0 ? directItemCount : 0;
   }

   public long getIndirectItemCount()
   {
      return indirectItemCount;
   }

   public void addIndirectItemCount(long count)
   {
      indirectItemCount += count;
   }

   public void removeIndirectItemCount(long count)
   {
      indirectItemCount -= count;
      indirectItemCount = indirectItemCount > 0 ? indirectItemCount : 0;
   }

   public long getItemCount()
   {
      return indirectItemCount + directItemCount;
   }

   public String getUserName()
   {
      return  userName;
   }

   public long getRoleCount()
   {
      return roleCount;
   }

   public void addRoles(long count)
   {
      roleCount += count;
   }

   public void removeRoles(long count)
   {
      roleCount -= count;
      roleCount = roleCount < 0 ? 0 : roleCount;
   }

   public boolean isLoggedIn()
   {
      return loggedIn;
   }

   // @Override
   public boolean equals(Object obj)
   {
      boolean isEqual = false;
      if (obj == this)
      {
         isEqual = true;
      }
      else if (obj instanceof UserItem)
      {
         isEqual = user.equals(((UserItem) obj).getUser());
      }
      else
      {
         isEqual = user.equals(obj);
      }
      return isEqual;
   }

   public Authorization getAuthorization()
   {
      if (authorization == null && user != null)
      {
         try
         {
            setFullUserDetails();
            if (WorkflowFacade.isUserAdmin(user))
            {
               authorization = Authorization.full;
            }
            else
            {
               authorization = Authorization.none;
            }
         }
         catch (InvalidServiceException e)
         {
            PageMessage.setMessage(e);
         }
      }
      return authorization;
   }
   
   public int getAuthorizationId()
   {
      Authorization auth = getAuthorization();
      return auth != null ? auth.getValue() : Authorization.NONE;
   }

   public boolean isUserAdmin()
   {
      if(this.hasAdminRole == null)
      {
         this.hasAdminRole = Boolean.FALSE;
         if(user != null)
         {
            setFullUserDetails();
            this.hasAdminRole = new Boolean(WorkflowFacade.isUserAdmin(this.user));
         }
      }
      return this.hasAdminRole.booleanValue();
   }
   
   private void setFullUserDetails()
   {
      if(user != null && !UserDetailsLevel.Full.equals(user.getDetailsLevel()))
      {
         WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
         UserQuery query = UserQuery.findAll();
         UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Full);
         userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
         query.setPolicy(userPolicy);
         query.getFilter().add(UserQuery.OID.isEqual(user.getOID()));
         Iterator iter = facade.getAllUsers(query).iterator();
         if(iter.hasNext())
         {
            this.user = (User) iter.next();
         }
      }
   }
}