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
package org.eclipse.stardust.ui.web.common.util;

import java.util.Set;

import org.eclipse.stardust.ui.web.common.spi.user.User;

/**
 * @author Subodh.Godbole
 * 
 */
public class UserUtils
{
   /**
    * Utility Class
    */
   private UserUtils()
   {}

   /**
    * @param user
    * @param requriedRoles
    * @param excludeRoles
    * @return
    */
   public static boolean isAuthorized(User user, Set<String> requriedRoles, Set<String> excludeRoles)
   {
      // Empty means for all
      if (CollectionUtils.isEmpty(requriedRoles) || checkRoles(user, requriedRoles))
      {
         // Exclude Overrides the Required Roles
         // If a user contains a required role but he also contains one of the exclude
         // roles Then Exclude List takes precedence
         if (CollectionUtils.isEmpty(excludeRoles) || !checkRoles(user, excludeRoles))
         {
            return true;
         }
      }

      return false;
   }

   /**
    * @param user
    * @param roles
    * @return true if user has at least one role from the set
    */
   private static boolean checkRoles(User user, Set<String> roles)
   {
      for (String role : roles)
      {
         if (user.isInRole(role))
         {
            return true;
         }
      }
      return false;
   }
}
