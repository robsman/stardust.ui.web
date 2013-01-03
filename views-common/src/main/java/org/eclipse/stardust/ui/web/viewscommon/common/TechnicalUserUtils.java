/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;

public class TechnicalUserUtils
{

   protected static final Logger trace = LogManager.getLogger(TechnicalUserUtils.class);

   public static final String TECH_USER_PARAM_ACCOUNT = "Security.ResetPassword.TechnicalUser.Account";
   public static final String TECH_USER_PARAM_PASSWORD = "Security.ResetPassword.TechnicalUser.Password";
   public static final String TECH_USER_PARAM_REALM = "Security.ResetPassword.TechnicalUser.Realm";

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
      try
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Technical User about to log in...");
         }

         Parameters parameters = Parameters.instance();

         String user = parameters.getString(TECH_USER_PARAM_ACCOUNT);
         String pwd = parameters.getString(TECH_USER_PARAM_PASSWORD);
         String realm = parameters.getString(TECH_USER_PARAM_REALM);

         Map<String, String> properties = CollectionUtils.newHashMap();
         properties.put(SecurityProperties.REALM, realm);

         if (StringUtils.isEmpty(user) || StringUtils.isEmpty(pwd) || StringUtils.isEmpty(realm))
         {
            user = "motu";
            pwd = "motu";
            realm = "carnot";

            trace.info("The default user credentials were used to initiate the 'Reset Password' request. Please configure a new technical user.");
         }
         else
         {
            trace.debug("Technical User is found to be configured. Using the same to Reset Password");
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
            trace.debug("Technical User Logged in...");
         }
      }
      finally
      {
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
   }
}
