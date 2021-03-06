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
package org.eclipse.stardust.ui.web.viewscommon.login;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.CredentialProvider;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;

public class ResetPasswordServlet extends HttpServlet
{
   /**
    * 
    */
   private static final long serialVersionUID = -8452274833668988165L;

   private final static String DEFAULT_LOGIN_PAGE = "plugins/views-common/login.iface";

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      PrintWriter out = resp.getWriter();
      resp.setContentType("text/html");
      try
      {
         String account = req.getParameter("account");
         String partition = req.getParameter("partition");
         String realm = req.getParameter("realm");
         String token = req.getParameter("token");
         Map<String, String> properties = CollectionUtils.newHashMap();
         // fetch serviceFactory from partition
         if (!StringUtils.isEmpty(partition))
         {
            properties.put(SecurityProperties.PARTITION, partition);
         }

         UserService userService = ServiceFactoryLocator.get(CredentialProvider.PUBLIC_LOGIN).getUserService();
         properties = getLoginProperties(partition, realm);
         userService.resetPassword(account, properties, token);
         out.println("Password generated and sent to registered Email Id</br>");
      }
      catch (Exception e)
      {
         out.println("Request failed - " + e.getMessage() + "<br/>");
      }
      finally
      {
         out.println("Click <a href=\"" + DEFAULT_LOGIN_PAGE + "\">here</a> to Login");
      }
   }

   private Map<String, String> getLoginProperties(String tenant, String userRealm)
   {
      Boolean promptForPartition = Parameters.instance().getBoolean(SecurityProperties.PROMPT_FOR_PARTITION, false);
      Boolean promptForRealm = Parameters.instance().getBoolean(SecurityProperties.PROMPT_FOR_REALM, false);
      Boolean promptForDomain = Parameters.instance().getBoolean(SecurityProperties.PROMPT_FOR_DOMAIN, false);

      String partition = null;
      String realm = null;
      String domain = null;
      if (promptForPartition)
      {
         partition = StringUtils.isEmpty(tenant) ? Parameters.instance().getString(
               SecurityProperties.DEFAULT_PARTITION, "") : tenant;
      }
      if (promptForRealm)
      {
         realm = StringUtils.isEmpty(userRealm)
               ? Parameters.instance().getString(SecurityProperties.DEFAULT_REALM, "")
               : userRealm;
      }
      if (promptForDomain)
      {
         domain = Parameters.instance().getString(SecurityProperties.DEFAULT_DOMAIN, "");
      }

      Map<String, String> properties = CollectionUtils.newHashMap();
      if (promptForDomain && !StringUtils.isEmpty(domain))
      {
         properties.put(SecurityProperties.DOMAIN, domain);
      }
      if (promptForPartition || !StringUtils.isEmpty(partition))
      {
         properties.put(SecurityProperties.PARTITION, partition);
      }
      if (promptForRealm && !StringUtils.isEmpty(realm))
      {
         properties.put(SecurityProperties.REALM, realm);
      }
      return Collections.unmodifiableMap(properties);
   }
}
