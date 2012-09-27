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

import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.ui.web.common.PerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.PerspectiveExtension;
import org.eclipse.stardust.ui.web.common.UiElementWithPermissions;
import org.eclipse.stardust.ui.web.common.spi.user.IAuthorizationProvider;
import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.plugin.support.ServiceLoaderUtils;

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
    * @param perspectiveDef
    * @author Yogesh.Manware
    * @return
    */
   public static boolean isAuthorized(User user, PerspectiveDefinition perspectiveDef)
   {
      Boolean isAuthorized = getAuthorizationProvider().isAuthorized(user, perspectiveDef.getName());

      if (null != isAuthorized)
      {
         return isAuthorized;
      }
      else
      {
         return isAuthorized(user, perspectiveDef.getRequiredRolesSet(), perspectiveDef.getExcludeRolesSet());
      }
   }

   /**
    * @param user
    * @param perspectiveExt
    * @author Yogesh.Manware
    * @return
    */
   public static boolean isAuthorized(User user, PerspectiveExtension perspectiveExt)
   {
      Boolean isAuthorized = getAuthorizationProvider().isAuthorized(user, perspectiveExt.getName());

      if (null != isAuthorized)
      {
         return isAuthorized;
      }
      else
      {
         return isAuthorized(user, perspectiveExt.getRequiredRolesSet(), perspectiveExt.getExcludeRolesSet());
      }
   }

   /**
    * @param user
    * @param uiElement
    * @author Yogesh.Manware
    * @return
    */
   public static boolean isAuthorized(User user, UiElementWithPermissions uiElement)
   {
      Boolean isAuthorized = getAuthorizationProvider().isAuthorized(user, uiElement.getName());

      if (null != isAuthorized)
      {
         return isAuthorized;
      }
      else
      {
         return isAuthorized(user, uiElement.getRequiredRolesSet(), uiElement.getExcludeRolesSet());
      }
   }

   /**
    * @param user
    * @param requriedRoles
    * @param excludeRoles
    * @return
    */
   private static boolean isAuthorized(User user, Set<String> requriedRoles, Set<String> excludeRoles)
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

   /**
    * @return AuthorizationProvider
    * @author Yogesh.Manware
    */
   private static IAuthorizationProvider getAuthorizationProvider()
   {
      Iterator<IAuthorizationProvider.Factory> serviceProviders = ServiceLoaderUtils
            .searchProviders(IAuthorizationProvider.Factory.class);

      IAuthorizationProvider.Factory factory = null;

      if (null != serviceProviders)
      {
         while (serviceProviders.hasNext())
         {
            factory = serviceProviders.next();
            if (null != factory)
            {
               return factory.getAuthorizationProvider();
            }
         }
      }
      return null;
   }

}
