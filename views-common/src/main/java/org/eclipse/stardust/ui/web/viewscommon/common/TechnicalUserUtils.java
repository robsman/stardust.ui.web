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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class TechnicalUserUtils
{

   protected static final Logger trace = LogManager.getLogger(TechnicalUserUtils.class);

   public static final String TECH_USER_ACCOUNT = "motu";
   public static final String TECH_USER_PASSWORD = "motu";
   public static final String TECH_USER_REALM = "carnot";

   /**
    * 
    * @param loginProperties
    * @return
    * @throws PortalException
    */
   public static SessionContext login(Map<String, String> loginProperties) throws PortalException
   {
      SessionContext sessionCtx;

      // Login With Technical User
      trace.debug("Technical User about to log in...");

      Parameters parameters = Parameters.instance();

      String user = parameters.getString(Constants.TECH_USER_PARAM_ACCOUNT);
      String pwd = parameters.getString(Constants.TECH_USER_PARAM_PASSWORD);
      String realm = parameters.getString(Constants.TECH_USER_PARAM_REALM);

      Map<String, String> properties = CollectionUtils.newHashMap();
      properties.put(SecurityProperties.REALM, realm);

      if (StringUtils.isEmpty(user) || StringUtils.isEmpty(pwd) || StringUtils.isEmpty(realm))
      {
         user = TECH_USER_ACCOUNT;
         pwd = TECH_USER_PASSWORD;
         realm = TECH_USER_REALM;

         trace.info("The default user credentials were used to initiate the 'Technical User login' request. Please configure a new technical user.");
      }
      else
      {
         trace.debug("Technical User is found to be configured. Using the same to login");
      }

      // Set the partition of Tech User same as the current user
      if (loginProperties.containsKey(SecurityProperties.PARTITION))
      {
         properties.put(SecurityProperties.PARTITION, loginProperties.get(SecurityProperties.PARTITION));
      }

      sessionCtx = SessionContext.findSessionContext();
      sessionCtx.initInternalSession(user, pwd, properties);

      if (trace.isDebugEnabled())
      {
         trace.debug("Technical User" + user + " Logged in...");
      }
      return sessionCtx;
   }

   /**
    * 
    * @param sessionCtx
    */
   public static void logout(SessionContext sessionCtx)
   {
      sessionCtx.logout();
      trace.debug("Technical User Logged out...");
   }
}
