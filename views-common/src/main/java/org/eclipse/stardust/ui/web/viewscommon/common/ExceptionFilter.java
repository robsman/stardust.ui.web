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

import javax.servlet.*;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * Catches and handles all ServletException to hide them from the user. If an exception
 * occurs the filter forwards to the logout page including an error param in the
 * forwarding url.
 * 
 * @author fuhrmann
 * @version $Revision$
 */
public class ExceptionFilter implements Filter
{
   private final static String DEFAULT_LOGOUT_PAGE = "/ipp/common/logout.jsp";

   public static final String ERROR_PARAM = "exceptionError";
   
   protected final static Logger trace = LogManager.getLogger(ExceptionFilter.class);

   /**
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {}

   /**
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
    *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException
   {
      try
      {
         chain.doFilter(req, res);
      }
      catch (ServletException e)
      {
         trace.error("An error occurred. Root cause was: ", e.getRootCause());
         if(!res.isCommitted())
         {
            RequestDispatcher dispatcher = req.getRequestDispatcher(DEFAULT_LOGOUT_PAGE
                  + "?" + ERROR_PARAM + "=1");
            dispatcher.forward(req, res);
         }
      }
   }

   /**
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig fc) throws ServletException
   {}

}
