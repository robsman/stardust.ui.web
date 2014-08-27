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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.PermissionState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission;
import org.eclipse.stardust.engine.core.runtime.utils.Permissions;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUser;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUserProvider;



public class AuthorizationUtils
{

   public static String PERMISSION_MANAGE_CASE =Permissions.PREFIX + "processDefinition" + '.' + ExecutionPermission.Id.modifyCase ;
   
   
   private AuthorizationUtils()
   {

   }
   
   public static boolean hasAbortPermission(ActivityInstance activityInstance)
   {
      boolean hasAbortPermission = PermissionState.Granted.equals(activityInstance
            .getPermission(Permissions.ACTIVITY_ABORT_ACTIVITY_INSTANCES));
      if (activityInstance.getActivity().isInteractive())
      {
         hasAbortPermission = hasAbortPermission ? activityInstance.getActivity().isAbortable() : hasAbortPermission;
      }
      return hasAbortPermission;
   }

   /**
    * returns Grant for Manage case permission for specified ProcessInstance
    * 
    * @param processInstance
    * @return
    */
   public static boolean hasManageCasePermission(ProcessInstance processInstance)
   {
      return PermissionState.Granted.equals(processInstance.getPermission(PERMISSION_MANAGE_CASE));      
   }


   public static boolean hasAbortPermission(ProcessInstance processInstance)
   {
      return PermissionState.Granted.equals(processInstance
            .getPermission(Permissions.PROCESS_DEFINITION_ABORT_PROCESS_INSTANCES));
   }

   public static boolean hasDelegatePermission(ActivityInstance activityInstance)
   {
      boolean hasDelegatePermission = PermissionState.Granted.equals(activityInstance
            .getPermission(Permissions.ACTIVITY_DELEGATE_TO_OTHER));

      return hasDelegatePermission;
   }

   public static boolean hasPIModifyPermission(ProcessInstance processInstance)
   {
      boolean hasPermission = PermissionState.Granted.equals(processInstance
            .getPermission(Permissions.PROCESS_DEFINITION_MODIFY_PROCESS_INSTANCES));
      return hasPermission;
   }

   /**
    * @return
    */
   public static boolean canForceSuspend()
   {
      return getIppUser().hasPermission(ExecutionPermission.Id.forceSuspend);
   }

   /**
    * @return
    */
   public static boolean canManageAuthorization()
   {
      return getIppUser().hasPermission(ExecutionPermission.Id.manageAuthorization);
   }
   
   /**
    * @return
    */
   public static boolean canManageDeputies()
   {
      return getIppUser().hasPermission(ExecutionPermission.Id.manageDeputies);
   }

   /**
    * @return
    */
   public static boolean canManageDaemons()
   {
      return getIppUser().hasPermission(ExecutionPermission.Id.manageDaemons);
   }
   
   /**
    * @return
    */
   public static boolean canCreateCase()
   {
      return getIppUser().hasPermission(ExecutionPermission.Id.createCase);
   }
   
   /**
    * @return
    */
   public static boolean hasSpawnProcessPermission()
   {
      return getIppUser().hasPermission(ExecutionPermission.Id.spawnSubProcessInstance);
   }
   
   /**
    * @return
    */
   public static boolean hasAbortAndJoinProcessInstancePermission()
   {
      return getIppUser().hasPermission(ExecutionPermission.Id.joinProcessInstance);
   }
   
   /**
    * @return
    */
   public static boolean hasAbortAndStartProcessInstancePermission()
   {
       return getIppUser().hasPermission(ExecutionPermission.Id.spawnPeerProcessInstance);
   }

   public static IppUser getIppUser()
   {
      return (IppUser) IppUserProvider.getInstance().getUser();
   }
}
