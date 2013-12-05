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
package org.eclipse.stardust.ui.web.viewscommon.login.util;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

/**
 * @author Subodh.Godbole
 *
 */
public class LoginUtils
{
   private static final Logger trace = LogManager.getLogger(LoginUtils.class);

   /**
    * 
    */
   public static void initialize()
   {
      trace.info("Initializing for Login");
      SessionContext sessionCtx = SessionContext.findSessionContext();

      // User display name preference are not fetched with UserService.getUser()
      UserUtils.loadDisplayPreferenceForUser(sessionCtx.getUser());
      
      // Touch UserProvider > User, so that bean is fully initialized
      // and can be accessed from REST calls (without FacesContext)
      UserProvider userProvider = (UserProvider)FacesUtils.getBeanFromContext("userProvider");
      userProvider.getUser().getDisplayName();

      // Completely Initialize Application,
      // so that it and can be accessed from REST calls (without FacesContext)
      PortalApplication.getInstance();
      trace.info("Initialization successful for Login");
   }
}
