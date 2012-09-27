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
package org.eclipse.stardust.ui.web.viewscommon.views.authorization;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.stardust.engine.api.dto.RuntimePermissionsDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;

/**
 * 
 * This is a wrapper class to manage general and ui permissions
 * 
 * @author Yogesh.Manware
 * 
 */
public class PermissionsDetails
{
   private static final long serialVersionUID = -6314888067527212016L;

   private Map<String, List<String>> uiPermissions;

   private RuntimePermissionsDetails generalPermission;

   /**
    * @param permissions
    */
   public PermissionsDetails(Map<String, List<String>> permissions)
   {
      this.uiPermissions = permissions;
   }

   /**
    * @return
    */
   public Set<String> getAllPermissionIds()
   {
      Set<String> permissionIds = new HashSet<String>(uiPermissions.keySet());
      permissionIds.addAll(generalPermission.getAllPermissionIds());
      return Collections.unmodifiableSet(permissionIds);
   }

   /**
    * @param permissionId
    * @return
    */
   public Set<ModelParticipantInfo> getGrants(String permissionId)
   {
      Set<ModelParticipantInfo> externalGrants;

      if (UiPermissionUtils.isGeneralPermissionId(permissionId))
      {
         externalGrants = generalPermission.getGrants(permissionId);
      }
      else
      {
         externalGrants = UiPermissionUtils.externalize(uiPermissions.get(permissionId));
      }

      return externalGrants;
   }

   /**
    * @param permissionId
    * @param grants
    */
   public void setGrants(String permissionId, Set<ModelParticipantInfo> grants)
   {
      if (UiPermissionUtils.isGeneralPermissionId(permissionId))
      {
         generalPermission.setGrants(permissionId, grants);
      }
      else
      {
         uiPermissions.put(permissionId, UiPermissionUtils.internalize(grants));
      }
   }

   /**
    * @param permissionId
    */
   public void setAllGrant(String permissionId)
   {
      if (UiPermissionUtils.isGeneralPermissionId(permissionId))
      {
         generalPermission.setAllGrant(permissionId);
      }
      else
      {
         uiPermissions.put(permissionId, Collections.singletonList(Authorization2.ALL));
      }
   }

   /**
    * @param permissionId
    * @return
    */
   public boolean hasAllGrant(String permissionId)
   {
      if (UiPermissionUtils.isGeneralPermissionId(permissionId))
      {
         return generalPermission.hasAllGrant(permissionId);
      }
      else
      {
         List<String> grants = uiPermissions.get(permissionId);

         if (grants != null)
         {
            if (grants.contains(Authorization2.ALL))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * @param permissionId
    * @return
    */
   public boolean isDefaultGrant(String permissionId)
   {
      if (UiPermissionUtils.isGeneralPermissionId(permissionId))
      {
         return generalPermission.isDefaultGrant(permissionId);
      }
      else
      {
         return UiPermissionUtils.isDefaultPermission(permissionId, uiPermissions.get(permissionId));
      }
   }

   /**
    * @return
    */
   public Map<String, Serializable> getUIPermissionMap()
   {
      Map<String, Serializable> permissionMap = new HashMap<String, Serializable>();
      for (Entry<String, List<String>> entry : uiPermissions.entrySet())
      {
         permissionMap.put(entry.getKey(), (Serializable) entry.getValue());
      }
      return Collections.unmodifiableMap(permissionMap);
   }

   public RuntimePermissionsDetails getGeneralPermission()
   {
      return generalPermission;
   }

   public void setGeneralPermission(RuntimePermissionsDetails generalPermission)
   {
      this.generalPermission = generalPermission;
   }
}
