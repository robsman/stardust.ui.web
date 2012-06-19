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

import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.viewscommon.beans.ApplicationContext;


/**
 * This utility contains all methods related to external security (both Authentication and
 * Authorization)
 * 
 * @author Yogesh.Manware
 * 
 */
public class ExternalSecurityUtils
{
   private Boolean externalAuthorization;
   private Boolean externalAuthentication;
   private Boolean principalAuthentication;

   /**
    * This method returns true if external Authorization is active. Authorization
    * configuration determines whether authorization should be 1. Internal, i.e. using
    * user data stored in the audit trail 2. External, i.e. using user data provided by a
    * configured synchronization provider
    * 
    * @return
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
    * This method returns true if external Authentication is active. Authentication
    * configuration determines whether authentication should be 1. Internal, i.e.
    * authentication is done internally which means that the engine authenticates against
    * the audit trail or a synchronization provider (depending on the authorization
    * configuration) 2. Principal, i.e. authentication is done externally using J2EE
    * Principals 3. Jaas, i.e. authentication is done externally using JAAS
    * 
    * @return
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

   /**
    * This method returns true if external Authentication of type "J2EE principal" is used
    * This requires following entry in the carnot.properties file
    * Security.Authentication.Mode = principal
    * 
    * @return
    */
   public boolean isPrincipalAuthentication()
   {
      if (null == principalAuthentication)
      {
         principalAuthentication = ApplicationContext.isPrincipalLogin();
      }
      
      return principalAuthentication;
   }
}
