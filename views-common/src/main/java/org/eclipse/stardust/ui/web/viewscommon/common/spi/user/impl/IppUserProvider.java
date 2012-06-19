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

import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;




/**
 * @author Subodh.Godbole
 *
 */
public class IppUserProvider implements UserProvider
{
   private static final long serialVersionUID = 1L;

   private User user;
   
   private Boolean externalAuthorization;
   private Boolean externalAuthentication;
   
   /**
    * @return
    */
   public static IppUserProvider getInstance()
   {
      return (IppUserProvider)FacesUtils.getBeanFromContext("userProvider");
   }
   
   public User getUser()
   {
      if(user == null)
      {
         user = new IppUser();
      }
      return user;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.spi.user.UserProvider#isExternalAuthentication()
    */
   public boolean isExternalAuthentication()
   {
      if (null == externalAuthentication)
      {
         UserService userService = ServiceFactoryUtils.getUserService();
         if (null != userService)
         {
            externalAuthentication = !userService.isInternalAuthentication();
         }
         else
         {
            externalAuthentication = false;
         }
      }
      
      return externalAuthentication;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.spi.user.UserProvider#isExternalAuthorization()
    */
   public boolean isExternalAuthorization()
   {
      if (null == externalAuthorization)
      {
         UserService userService = ServiceFactoryUtils.getUserService();
         if (null != userService)
         {
            externalAuthorization = !userService.isInternalAuthorization();
         }
         else
         {
            externalAuthorization = false;
         }
      }
      
      return externalAuthorization;
   }

   /**
    * @return
    */
   public static User wrapUser(org.eclipse.stardust.engine.api.runtime.User user)
   {
      return new IppUser(user);
   }
}