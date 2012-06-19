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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.plugin.resources.impl;

import static java.util.Collections.singletonList;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.engine.core.repository.RepositorySpaceKey;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.EnumerationIteratorWrapper;
import org.eclipse.stardust.ui.web.plugin.support.AbstractPortalResourcesServlet;
import org.eclipse.stardust.ui.web.plugin.support.PortalPluginResourcesServlet;
import org.eclipse.stardust.ui.web.plugin.support.resources.spi.ResourceRequestProcessor;


/**
 * @author Subodh.Godbole
 *
 */
public class DmsResourceRequestProcessor implements ResourceRequestProcessor
{
   public static String[] dmsUrlPrefixes = new String[] {"/" + RepositorySpaceKey.SKINS.getId() + "/"};;
   public static final String PARAM_MODE = "mode";

   private static final Logger trace = LogManager.getLogger(DmsResourceRequestProcessor.class);
   private static DmsResourceRequestProcessor instance = new DmsResourceRequestProcessor();

   private PortalDmsResourcesServlet resourceServlet;

   /**
    * 
    */
   public DmsResourceRequestProcessor()
   {
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.plugin.support.resources.spi.ResourceRequestProcessor#handleRequest(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.ServletContext)
    */
   public void handleRequest(final String requestedPath, HttpServletRequest request, HttpServletResponse response,
         ServletContext servletContext) throws ServletException, IOException
   {
      if(resourceServlet == null)
      {
         synchronized (this)
         {
            if(resourceServlet == null)
            {
               initialize(servletContext);
            }
         }
      }

      final String pathPrefix = getDmsPathPrefix(requestedPath);
      
      // serve content from DMS, if available
      HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request)
      {
         @Override
         public String getServletPath()
         {
            return pathPrefix;
         }
         
         @Override
         public String getPathInfo()
         {
            return requestedPath.substring(pathPrefix.length());
         }
      };
      
      resourceServlet.service(wrapper, response);
   }

   /**
    * @param servletContext
    */
   private void initialize(final ServletContext servletContext) throws ServletException
   {
      resourceServlet = new PortalDmsResourcesServlet();
      resourceServlet.init(new ServletConfig()
      {
         public String getInitParameter(String name)
         {
            return AbstractPortalResourcesServlet.DEBUG_INIT_PARAM.equals(name) ? "production".equals(servletContext
                  .getInitParameter(PARAM_MODE)) ? "false" : "true" : null;
         }

         public Enumeration< ? > getInitParameterNames()
         {
            return new EnumerationIteratorWrapper<String>(singletonList(PortalPluginResourcesServlet.DEBUG_INIT_PARAM)
                  .iterator());
         }

         public ServletContext getServletContext()
         {
            return servletContext;
         }

         public String getServletName()
         {
            return servletContext.getServletContextName() + " - Dms Resources Servlet";
         }
      });
   }
   
   /**
    * @param requestedPath
    * @return
    */
   private static String getDmsPathPrefix(String requestedPath)
   {
      for (String dmsUrlPrefix : dmsUrlPrefixes)
      {
         if (requestedPath.startsWith(dmsUrlPrefix))
         {
            return dmsUrlPrefix;
         }
      }

      return "";
   }

   /**
    * @param requestedPath
    * @return
    */
   private static boolean isDmsUrl(String requestedPath)
   {
      for (String dmsUrlPrefix : dmsUrlPrefixes)
      {
         if (requestedPath.startsWith(dmsUrlPrefix))
         {
            return true;
         }
      }

      return false;
   }
   
   /**
    * @author Subodh.Godbole
    *
    */
   public static class DmsFactory implements Factory
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.plugin.support.resources.spi.ResourceRequestProcessor.Factory#getRequestProcessor(java.lang.String)
       */
      public ResourceRequestProcessor getRequestProcessor(String requestedPath)
      {
         if(isDmsUrl(requestedPath))
         {
            return instance;
         }
         
         return null;
      }
   }
}
