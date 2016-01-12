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
package org.eclipse.stardust.ui.web.common.spi.user;

import java.util.Set;

/**
 * @author Yogesh.Manware
 * 
 */
public interface IAuthorizationProvider
{
   Boolean isAuthorized(User user, String permissionId);

   /**
    * @param permissionId
    * @param roles
    * @param allow
    *           -> true, deny -> false
    */
   void addDefaultPermissions(String permissionId, Set<String> roles, boolean allow);

   /**
    * @author Yogesh.Manware
    * 
    */
   interface Factory
   {
      IAuthorizationProvider getAuthorizationProvider();
   }
}
