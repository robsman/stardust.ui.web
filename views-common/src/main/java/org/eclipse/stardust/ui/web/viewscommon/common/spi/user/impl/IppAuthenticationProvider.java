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

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.spi.user.AuthenticationProvider;
import org.eclipse.stardust.ui.web.viewscommon.login.InfinityStartup;


/**
 * @author Subodh.Godbole
 *
 */
public class IppAuthenticationProvider implements AuthenticationProvider
{
   private InfinityStartup infinityStartup;
   private static final Logger trace = LogManager.getLogger(IppAuthenticationProvider.class);
   
   /**
    * 
    */
   public IppAuthenticationProvider()
   {
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.spi.user.authentication.AuthenticationProvider#initialize(javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   public void initialize(ServletContext servletContext, HttpServletRequest request,
         HttpServletResponse response)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("#IppAuthenticationProvider()....");
      }
      infinityStartup = new InfinityStartup(servletContext, request, response);
      infinityStartup.createSession();
      
      if (trace.isDebugEnabled())
      {
         trace.debug("#IppAuthenticationProvider initialized....");
      }
      
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.spi.user.authentication.AuthenticationProvider#showPage()
    */
   public void showPage() throws IOException
   {
      infinityStartup.showPage();
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public static class IppFactory implements Factory
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.spi.user.authentication.AuthenticationProvider.Factory#getAuthenticationProvider()
       */
      public AuthenticationProvider getAuthenticationProvider()
      {
         return new IppAuthenticationProvider();
      }
   }
}
