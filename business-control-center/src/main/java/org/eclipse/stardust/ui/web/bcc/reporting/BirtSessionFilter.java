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
package org.eclipse.stardust.ui.web.bcc.reporting;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.StringUtils;


/**
 * Servlet filter used to save the recently used BIRT session id
 */
public class BirtSessionFilter implements Filter
{
   
   public static final String BIRT_SESSION_ID_ATTRIBUTE = "birtSessionId";

   public void init(FilterConfig filterConfig) throws ServletException
   {
   }

   public void destroy()
   {
   }

   public void doFilter(ServletRequest request, ServletResponse response,
         FilterChain filterChain) throws IOException, ServletException
   {
      String birtSessionId = request.getParameter("__sessionId");
      if ( !StringUtils.isEmpty(birtSessionId))
      {
         ((HttpServletRequest) request).getSession().setAttribute(BIRT_SESSION_ID_ATTRIBUTE,
               birtSessionId);
      }

      filterChain.doFilter(request, response);
   }

}
