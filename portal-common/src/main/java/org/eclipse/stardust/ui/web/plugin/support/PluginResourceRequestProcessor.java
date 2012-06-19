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
package org.eclipse.stardust.ui.web.plugin.support;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.stardust.ui.web.common.util.CollectionUtils.newArrayList;
import static org.eclipse.stardust.ui.web.common.util.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.common.util.StringUtils.join;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.EnumerationIteratorWrapper;
import org.eclipse.stardust.ui.web.plugin.support.resources.PluginResourceUtils;
import org.eclipse.stardust.ui.web.plugin.support.resources.spi.ResourceRequestProcessor;


/**
 * @author Subodh.Godbole
 *
 */
public class PluginResourceRequestProcessor implements ResourceRequestProcessor
{
   public static final String PARAM_MARKUP_URL_PATTERNS = "markup-url-patterns";
   public static final String PARAM_MODE = "mode";

   private static final Logger trace = LogManager.getLogger(PluginResourceRequestProcessor.class);
   private static PluginResourceRequestProcessor instance = new PluginResourceRequestProcessor();

   private PortalPluginResourcesServlet resourceServlet;
   private String[] markupSuffixes;
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.plugin.support.resources.spi.ResourceRequestProcessor#handleRequest(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.ServletContext)
    */
   public void handleRequest(final String requestedPath, HttpServletRequest request, HttpServletResponse response,
         ServletContext servletContext) throws ServletException, IOException
   {
      if (resourceServlet == null)
      {
         synchronized (this)
         {
            if (resourceServlet == null)
            {
               initialize(servletContext);
            }
         }
      }

      RequestDispatcher markupDispatcher = null;
      for (String markupSuffix : markupSuffixes)
      {
         if (requestedPath.endsWith(markupSuffix))
         {
            // find dispatcher based on suffix
            markupDispatcher = request.getRequestDispatcher("/*" + markupSuffix);
            break;
         }
      }
      
      if (null != markupDispatcher)
      {
         HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request)
         {
            @Override
            public void setRequest(ServletRequest request)
            {
               // ignored on purpose to keep original request intact
            }

            @Override
            public String getServletPath()
            {
               // simulate the request was matched by suffix instead of by prefix
               return requestedPath;
            }
            
            @Override
            public String getPathInfo()
            {
               // simulate the request was matched by suffix instead of by prefix
               return null;
            }
         };
         
         markupDispatcher.forward(wrapper, response);
      }
      else
      {
         // serve resources from plugin
         HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request)
         {
            @Override
            public String getServletPath()
            {
               return PluginResourceUtils.PATH_PLUGINS;
            }
            
            @Override
            public String getPathInfo()
            {
               return requestedPath.substring(PluginResourceUtils.PATH_PLUGINS.length());
            }
         };
         
         resourceServlet.service(wrapper, response);
      }
   }

   /**
    * @param servletContext
    * @throws ServletException
    */
   private void initialize(final ServletContext servletContext) throws ServletException
   {
      if (!isEmpty(servletContext.getInitParameter(PARAM_MARKUP_URL_PATTERNS)))
      {
         String[] suffixPatterns = servletContext.getInitParameter(PARAM_MARKUP_URL_PATTERNS).split(",");

         List<String> suffixes = newArrayList();
         for (String suffixPattern : suffixPatterns)
         {
            if (suffixPattern.startsWith("*") && !suffixPattern.substring(1).contains("*"))
            {
               // strip off leading wildcard
               suffixes.add(suffixPattern.substring(1));
            }
            else
            {
               trace.warn("Ignoring invalid " + PARAM_MARKUP_URL_PATTERNS + " entry \"" + suffixPattern
                     + "\" for filter [" + servletContext.getServletContextName() + "].");
            }
         }

         this.markupSuffixes = suffixes.toArray(new String[suffixes.size()]);
      }
      else
      {
         this.markupSuffixes = new String[] {".iface"};
      }

      // created embedded resources servlet
      this.resourceServlet = new PortalPluginResourcesServlet();
      resourceServlet.init(new ServletConfig()
      {
         public String getInitParameter(String name)
         {
            return PortalPluginResourcesServlet.DEBUG_INIT_PARAM.equals(name) ? "production".equals(servletContext
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
            return servletContext.getServletContextName() + " - Plugin Resources Servlet";
         }
      });

      trace.info("Initialized portal-plugin Request Processor'" + servletContext.getServletContextName() + "'" //
            + " using markup URL patterns: *" + join(asList(markupSuffixes).iterator(), ", *"));
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public static class PluginFactory implements Factory
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.plugin.support.resources.spi.ResourceRequestProcessor.Factory#getRequestProcessor(java.lang.String)
       */
      public ResourceRequestProcessor getRequestProcessor(String requestedPath)
      {
         if (PluginResourceUtils.isPluginPath(requestedPath))
         {
            return instance;
         }

         return null;
      }
   }
}
