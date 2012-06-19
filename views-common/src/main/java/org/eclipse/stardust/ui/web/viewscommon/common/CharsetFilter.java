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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Character encoding filter for ServletRequest.
 * 
 * <p>
 * Required configuration in <code>web.xml</code> would look like:
 * </p>
 * 
 * <pre>
 * {@code
 * <filter>
 *   <filter-name>IppPortalCharsetFilter</filter-name>
 *   <filter-class>org.eclipse.stardust.ui.web.viewscommon.common.CharsetFilter</filter-class>
 * </filter>
 * <filter-mapping>
 *   <filter-name>IppPortalCharsetFilter</filter-name>
 *   <url-pattern>/uploadHtml</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * 
 * @version $Revision: $
 */
public class CharsetFilter implements Filter
{
   private String encoding;

   /**
    *
    */
   public void init(FilterConfig config) throws ServletException
   {
      encoding = config.getInitParameter("requestEncoding");
      if (null == encoding)
      {
         encoding = "UTF-8";
      }
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException,
         ServletException
   {
      // Respect the client-specified character encoding
      // (see HTTP specification section 3.4.1)
      if (null == request.getCharacterEncoding())
      {
         request.setCharacterEncoding(encoding);
      }

      // Set the default response content type and encoding
      response.setContentType("text/html; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");

      next.doFilter(request, response);
   }

   public void destroy()
   {

   }
}