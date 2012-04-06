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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.core.runtime.utils.PermissionHelper;
import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class IppUser implements User
{
   private static final long serialVersionUID = 1L;

   private org.eclipse.stardust.engine.api.runtime.User ippUser;
   
   private PermissionHelper permissionHelper = null;

   private String uniqueUserId;
   
   private String displayName = null;

   /**
    * Gets the logged in user
    */
   public IppUser()
   {
      this(SessionContext.findSessionContext().getUser());
   }

   /**
    * @param ippUser
    */
   public IppUser(org.eclipse.stardust.engine.api.runtime.User ippUser)
   {
      this.ippUser = ippUser;
      if(ippUser == null)
      {
         throw new IllegalStateException("User can not be Null");
      }
      // To make the user unique across Partitions
      uniqueUserId = StringUtils.join(":", ippUser.getPartitionId(), String.valueOf(ippUser.getOID()), ippUser.getId());
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.spi.user.User#isInRole(java.lang.String)
    */
   public boolean isInRole(String role)
   {
      if (ippUser != null)
      {
         if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(role))
         {
            return ippUser.isAdministrator();
         }
         else
         {
            QName qname = QName.valueOf(role);
            String namespace = qname.getNamespaceURI();
            role = qname.getLocalPart();
            for (Iterator< ? > grantIter = ippUser.getAllGrants().iterator(); grantIter.hasNext();)
            {
               Grant grant = (Grant) grantIter.next();
               if ((namespace == null || CompareHelper.areEqual(namespace, grant.getNamespace()))
                     && CompareHelper.areEqual(grant.getId(), role))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }
   
   /**
    * @return
    */
   public PermissionHelper getPermissionHelper()
   {
      if(permissionHelper == null)
      {
         permissionHelper = new PermissionHelper(ippUser, null);
      }
      return permissionHelper;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.spi.user.User#isAdministrator()
    */
   public boolean isAdministrator()
   {
      return ippUser.isAdministrator();
   }

   public String getFirstName()
   {
      return ippUser.getFirstName();
   }

   public String getLastName()
   {
      return ippUser.getLastName();
   }

   public String getLoginName()
   {
      return ippUser.getAccount();
   }

   public String getUID()
   {
      return uniqueUserId;
   }
   
   public String getDisplayName()
   {
      if (null == displayName)
      {
         displayName = UserUtils.getUserDisplayLabel(ippUser);
      }
      return displayName;
   }
}
