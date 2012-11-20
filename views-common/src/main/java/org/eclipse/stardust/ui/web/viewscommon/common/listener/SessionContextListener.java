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
package org.eclipse.stardust.ui.web.viewscommon.common.listener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.viewscommon.beans.ApplicationContext;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



public class SessionContextListener implements HttpSessionListener
{
   protected final static Logger trace = LogManager.getLogger(SessionContextListener.class);

   private static final String ENABLE_HEADLESS_CORRECTION_ON_WINDOWS_CONTEXT_PARAMETER = "com.infinity.bpm.web.ENABLE_HEADLESS_CORRECTION_ON_WINDOWS";

   public void sessionCreated(HttpSessionEvent event)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("#sessionCreated(HttpSessionEvent event)");
         if (null != event.getSession())
         {
            trace.debug("#Session Id:  " + event.getSession().getId());
         }
      }
      
      correctHeadlessSetting(event.getSession().getServletContext());
      
      if (trace.isDebugEnabled())
      {
         trace.debug("#Returning from sessionCreated(HttpSessionEvent event)");
      }
   }

   /**
    * Will set the "java.awt.headless" to false if running on windows and
    * com.infinity.bpm.web.ENABLE_HEADLESS_CORRECTION_ON_WINDOWS servlet context parameter
    * is not explicitly set to false.
    * 
    * @param servletContext
    */
   private void correctHeadlessSetting(ServletContext servletContext)
   {
      String doCorrect = servletContext
            .getInitParameter(ENABLE_HEADLESS_CORRECTION_ON_WINDOWS_CONTEXT_PARAMETER);

      if (doCorrect == null || Boolean.valueOf(doCorrect).booleanValue())
      {
         if (isWindows())
         {
            System.setProperty("java.awt.headless", "false");
         }
      }
   }

   private boolean isWindows()
   {
      String osName;

      try
      {
         osName = System.getProperty("os.name");
      }
      catch (SecurityException se)
      {
         trace.info("Failed determining OS: " + se.getMessage());
         osName = null;
      }

      if (osName == null)
      {
         return false;
      }
      // see http://lopica.sourceforge.net/os.html
      if (osName.toLowerCase().indexOf("windows") != -1)
      {
         return true;
      }
      return false;
   }

   public void sessionDestroyed(HttpSessionEvent event)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("#sessionDestroyed(HttpSessionEvent event)");
      }
      
      HttpSession session = event.getSession();
      
      boolean sessionUnregistered = ApplicationContext.unregisterSession(session);
      
      if (trace.isDebugEnabled())
      {
         trace.debug("#ApplicationContext.unregisterSession(session)....");
      }
      
      if (session != null)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("#Session Id:  " + session.getId());
         }
         
         Object attr = session.getAttribute(SessionContext.BEAN_ID);
         if (attr instanceof SessionContext)
         {
            SessionContext sessionCtx = (SessionContext) attr;
            if (sessionCtx.isSessionInitialized())
            {
               ServiceFactory serviceFactory = sessionCtx.getServiceFactory();
               serviceFactory.close();
               session.setAttribute(SessionContext.BEAN_ID, null);
               if (trace.isDebugEnabled())
               {
                  trace.debug("#resetting ServiceFactory done....");
               }
            }
         }

         if (trace.isDebugEnabled())
         {
            if (sessionUnregistered)
            {
               trace.debug("session '" + session.getId() + "' successful unregistered");
            }
            else
            {
               trace.debug("session '" + session.getId() + "' not found");
            }
         }
      }
      if (trace.isDebugEnabled())
      {
         trace.debug("#sessionDestroyed(HttpSessionEvent event) exiting...");
      }
   }

}
