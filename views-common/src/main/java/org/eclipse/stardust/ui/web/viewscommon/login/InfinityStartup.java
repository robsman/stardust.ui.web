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
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.ExceptionFilter;
import org.eclipse.stardust.ui.web.viewscommon.beans.ApplicationContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.FacesUtils;


public class InfinityStartup
{
   private static final Logger trace = LogManager.getLogger(InfinityStartup.class);
   
   public final static String LOGIN_PAGE = "carnot.LOGIN_PAGE";
   
   public final static String RETURN_URL_PARAM = "returnUrl";
   
   private final static String TIMEOUT = "Carnot.Portal.SessionInvalidate.Timeout";
   
   private final ServletContext servletContext;
   
   private final HttpServletRequest request;

   private final HttpServletResponse response;

   private final String params;
   
   private final int timeout; // milliseconds

   public InfinityStartup(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("#creating InfinityStartup....");
      }
      
      this.servletContext = servletContext;
      this.request = request;
      this.response = response;
      
      this.params = getParams(request);
      if (trace.isDebugEnabled())
      {
         trace.debug("#InfinityStartup created....");
      }
      
      //read parameter
      this.timeout = Parameters.instance().getInteger(TIMEOUT, 1000);
   }
   
   private static void copyParam(StringBuffer params, Map<String, String[]> reqParamMap, String key)
   {
      if(reqParamMap.containsKey(key))
      {
         String paramValue = reqParamMap.get(key)[0];
         if(!StringUtils.isEmpty(paramValue))
         {
            if(params.length() > 0)
            {
               params.append("&");
            }
            params.append(key).append("=").append(paramValue);
         }
      }
   }
   
   protected static String getParams(HttpServletRequest request)
   {
      StringBuffer params = new StringBuffer();
      Map<String, String[]> reqParamMap = request.getParameterMap();
      copyParam(params, reqParamMap, ExceptionFilter.ERROR_PARAM);
      copyParam(params, reqParamMap, RETURN_URL_PARAM);
      copyParam(params, reqParamMap, "tenant");
      if(!reqParamMap.containsKey("tenant"))
      {
         HttpSession httpSession = request.getSession();
         if(httpSession != null)
         {
            try
            {
               String tenant = (String) httpSession.getAttribute("infinity.tenant");
               if(!StringUtils.isEmpty(tenant))
               {
                  if(params.length() > 0)
                  {
                     params.append("&");
                  }               
                  params.append("tenant=").append(tenant);
               }
            }
            catch (IllegalStateException e) {
               // session is already invalidated
            }
         }
      }
      if(params.length() > 0)
      {
         params.insert(0, "?");
      }
      return params.toString();
   }
   
   public void createSession()
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("#InfinityStartup createSession()....");
      }
      HttpSession httpSession = request.getSession();
      if(null != httpSession)
      { 
         List<FacesMessage> msgs = CollectionUtils.newList();

         FacesContext facesContext = null;
         if (ApplicationContext.isPrincipalLogin())
         {
            try
            {
               facesContext = FacesUtils.getFacesContext(httpSession.getServletContext(),
                     request, response);
            }
            catch (SecurityException se)
            {
               trace.warn("Failed obtaining JSF context, potential previous JSF messages will be lost.", se);
            }

            if (null != facesContext)
            {
               for (java.util.Iterator<FacesMessage> msgIter = facesContext.getMessages(); msgIter.hasNext();)
               {
                  msgs.add(msgIter.next());
               }
            }
         }
         if (trace.isDebugEnabled())
         {
            trace.debug("#Invalidating Session....");
         }

         if (request.isRequestedSessionIdValid())
         {
            try
            {
               httpSession.invalidate();
               
               //Following code is necessary as in case of Tomcat server, 
               //session invalidation happens in background thread and 
               //sometimes it results in IllegalStateException
               
               int counter = timeout/100;
               while (counter > 0 && request.isRequestedSessionIdValid())
               {
                  trace.debug("#Waiting for 100ms...");
                  Thread.sleep(100);
                  counter-- ;
               }
            }
            catch (Exception e)
            {
               trace.warn("#Exception occurred while invalidating session");
            }
         }
         
		 if (trace.isDebugEnabled())
         {
            trace.debug("#creating new session");
         }
         request.getSession(true);
         
         if (trace.isDebugEnabled())
         {
            trace.debug("#exiting from createSession()");
         }
      }
   }
   
   protected String getLoginPage()
   {
      String page = null;
      if(ApplicationContext.isPrincipalLogin())
      {
         page = servletContext.getInitParameter(Constants.PRINCIPAL_PAGE);
      }
      else
      {
         page = servletContext.getInitParameter(LOGIN_PAGE);
      }
      return StringUtils.isNotEmpty(page) ? page : "/plugins/views-common/login.iface";
   }
   
   public void showPage() throws IOException
   {
      String page = getLoginPage();

      response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + page
            + params));
   }
}