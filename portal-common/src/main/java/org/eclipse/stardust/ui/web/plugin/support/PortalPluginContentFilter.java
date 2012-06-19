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

import static org.eclipse.stardust.ui.web.common.util.StringUtils.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.plugin.support.resources.spi.ResourceRequestProcessor;


/**
 * TODO: This class can be renamed to PortalContentFilter as Plugins part is now moved as/into ResourceRequestProcessor
 * @author robert.sauer
 * @version $Revision: $
 */
public class PortalPluginContentFilter implements Filter
{
   private static final Logger trace = LogManager.getLogger(PortalPluginContentFilter.class);

   private ServletContext servletContext;
   private Map<String, ResourceRequestProcessor> processorCache = new Hashtable<String, ResourceRequestProcessor>();
   private List<ResourceRequestProcessor.Factory> requestProcessorFactories;
   
   /* (non-Javadoc)
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
         throws IOException, ServletException
   {
      if (request instanceof HttpServletRequest)
      {
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;

         final String requestedPath = isEmpty(httpRequest.getPathInfo()) //
               ? httpRequest.getServletPath()
               : httpRequest.getServletPath() + httpRequest.getPathInfo();
         
         // Search Processor
         ResourceRequestProcessor requestProcessor = getResourceRequestProcessor(requestedPath);
         if (requestProcessor != null)
         {
            requestProcessor.handleRequest(requestedPath, httpRequest, httpResponse, servletContext);
         }
         else
         {
            trace.info("No Provioders/Request Processors defined for '" + requestedPath + "'... forwarding to FilterChain...");
            filterChain.doFilter(request, response);
         }
      }
      else
      {
         filterChain.doFilter(request, response);
      }
   }

   /* (non-Javadoc)
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(final FilterConfig fc) throws ServletException
   {
      servletContext = fc.getServletContext();

      requestProcessorFactories = new ArrayList<ResourceRequestProcessor.Factory>();

      Iterator<ResourceRequestProcessor.Factory> serviceProviders = ServiceLoaderUtils
            .searchProviders(ResourceRequestProcessor.Factory.class);
      if (null != serviceProviders)
      {
         while (serviceProviders.hasNext())
         {
            requestProcessorFactories.add(serviceProviders.next());
         }
      }
      trace.info("ResourceRequestProcessor Factories Found " + requestProcessorFactories);
   }

   /* (non-Javadoc)
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {
   }

   /**
    * @param requestedPath
    * @return
    */
   private ResourceRequestProcessor getResourceRequestProcessor(String requestedPath)
   {
      if (!processorCache.containsKey(requestedPath))
      {
         ResourceRequestProcessor requestProcessor;
         for (ResourceRequestProcessor.Factory factory : requestProcessorFactories)
         {
            requestProcessor = factory.getRequestProcessor(requestedPath);
            if (null != requestProcessor)
            {
               trace.debug("Found ResourceRequestProcessor for '" + requestedPath + "' = " + requestProcessor);
               processorCache.put(requestedPath, requestProcessor);
               break;
            }
         }
      }
      else
      {
         trace.debug("Serving ResourceRequestProcessor from cache for - " + requestedPath);
      }

      return processorCache.get(requestedPath);
   }
}
