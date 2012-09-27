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

import org.eclipse.stardust.ui.web.common.spi.user.IAuthorizationProvider;
import org.eclipse.stardust.ui.web.common.spi.user.User;

/**
 * @author Yogesh.Manware
 * 
 */
public class IppAuthorizationProvider implements IAuthorizationProvider
{
   public Boolean isAuthorized(User user, String permissionId)
   {
      if (user instanceof IppUser)
      {
         return ((IppUser) user).hasUiPermission(permissionId);
      }
      return null;
   }

   /**
    * @author Yogesh.Manware
    * 
    */
   public static class IppFactory implements Factory
   {
      public IAuthorizationProvider getAuthorizationProvider()
      {
         return new IppAuthorizationProvider();
      }
   }
}
