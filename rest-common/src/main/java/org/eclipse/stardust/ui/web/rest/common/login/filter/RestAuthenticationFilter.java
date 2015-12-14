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
package org.eclipse.stardust.ui.web.rest.common.login.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RestAuthenticationFilter implements Filter
{
   private static String LOGIN_REST_PATH = "/portal/user/login";
   /**
    *
    */
   @Override
   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
         throws IOException, ServletException
   {

      if (servletRequest instanceof HttpServletRequest)
      {
         HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        
         // check if it is login url
         boolean loginUrl = false;
         if (LOGIN_REST_PATH.equals(httpServletRequest.getPathInfo()))
         {
            loginUrl = true;
         }

         SessionContext sessionContext = (SessionContext) ManagedBeanUtils.getManagedBean(SessionContext.BEAN_ID);

         if (loginUrl || (sessionContext != null && sessionContext.isSessionInitialized()))
         {
            filterChain.doFilter(servletRequest, servletResponse);
         }
         else
         {
            if (servletResponse instanceof HttpServletResponse)
            {
               HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
               httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            else
            {
               throw new IllegalAccessError();
            }
         }
      }
   }

   /**
    *
    */
   @Override
   public void init(FilterConfig filterConfig) throws ServletException
   {}

   /**
    *
    */
   @Override
   public void destroy()
   {}
}
