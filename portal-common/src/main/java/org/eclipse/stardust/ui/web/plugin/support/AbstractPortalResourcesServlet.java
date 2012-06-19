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

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.plugin.support.resources.CachingResourceLoader;
import org.eclipse.stardust.ui.web.plugin.support.resources.ResourceLoader;
import org.eclipse.stardust.ui.web.plugin.support.resources.ServletContextResourceLoader;
import org.eclipse.stardust.ui.web.plugin.support.resources.URLUtils;


/**
 * An Abstract Servlet which serves up resources by delegating to a ResourceLoader.
 * Resource Loader needs to be provided by extending this class and overriding getResourceLoader()  
 * Provide out of box functionality for managing the web communication / url connection etc
 */
// TODO use ClassLoader.getResources() and make hierarchical
// TODO verify request headers and (cached) response headers
// TODO set "private" cache headers in debug mode?
public abstract class AbstractPortalResourcesServlet extends HttpServlet
{
   private static final Logger trace = LogManager.getLogger(AbstractPortalResourcesServlet.class);

   private static final long serialVersionUID = 1L;

   /**
    * Context parameter for activating debug mode, which will disable caching.
    */
   public static final String DEBUG_INIT_PARAM = "debug";

   // One year in milliseconds. (Actually, just short of on year, since
   // RFC 2616 says Expires should not be more than one year out, so
   // cutting back just to be safe.)
   public static final long ONE_YEAR_MILLIS = 31363200000L;

   // Size of buffer used to read in resource contents
   private static final int BUFFER_SIZE = 2048;

   private boolean debugMode;

   private Map<String, ResourceLoader> resourceLoaders;

   /**
    * Override of Servlet.destroy();
    */
   @Override
   public void destroy()
   {
      this.resourceLoaders = null;

      super.destroy();
   }

   /**
    * Override of Servlet.init();
    */
   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);

      configureServlet("true".equalsIgnoreCase(config.getInitParameter(DEBUG_INIT_PARAM)));
   }

   private void configureServlet(boolean debugMode)
   {
      this.debugMode = debugMode;
      
      trace.info("Started resource servlet with debug mode set to " + debugMode);

      this.resourceLoaders = new HashMap<String, ResourceLoader>();
   }

   @Override
   public void service(ServletRequest request, ServletResponse response)
         throws ServletException, IOException
   {
      try
      {
         super.service(request, response);
      }
      catch (ServletException e)
      {
         trace.error("", e);
         throw e;
      }
      catch (IOException e)
      {
         if ( !canIgnore(e))
         {
            trace.error("", e);
         }

         throw e;
      }
   }

   /**
    * Override of HttpServlet.doGet()
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      ResourceLoader loader = findResourceLoader(request);

      String resourcePath = getResourcePath(request);
      URL url = loader.getResource(request, resourcePath);

      // Make sure the resource is available
      if (null == url)
      {
         response.sendError(HttpServletResponse.SC_NOT_FOUND);
         return;
      }

      // Stream the resource contents to the servlet response
      URLConnection connection = url.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(false);

      setHeaders(connection, response);

      InputStream in = connection.getInputStream();
      OutputStream out = response.getOutputStream();
      byte[] buffer = new byte[BUFFER_SIZE];

      try
      {
         pipeBytes(in, out, buffer);
      }
      finally
      {
         try
         {
            in.close();
         }
         finally
         {
            out.close();
         }
      }
   }

   /**
    * Override of HttpServlet.getLastModified()
    */
   @Override
   protected long getLastModified(HttpServletRequest request)
   {
      try
      {
         ResourceLoader loader = findResourceLoader(request);

         String resourcePath = getResourcePath(request);
         URL url = loader.getResource(request, resourcePath);

         if (null == url)
         {
            return super.getLastModified(request);
         }

         return URLUtils.getLastModified(url);
      }
      catch (IOException e)
      {
         // Note: API problem with HttpServlet.getLastModified()
         // should throw ServletException, IOException
         return super.getLastModified(request);
      }
   }

   /**
    * Returns the resource path from the http servlet request.
    * 
    * @param request
    *           the http servlet request
    * 
    * @return the resource path
    */
   protected String getResourcePath(HttpServletRequest request)
   {
      return request.getServletPath() + request.getPathInfo();
   }

   /**
    * Returns the resource loader for the requested servlet path.
    */
   private ResourceLoader findResourceLoader(HttpServletRequest request)
   {
      final String servletPath = request.getServletPath();
      ResourceLoader loader = resourceLoaders.get(servletPath);

      if (null == loader)
      {
         loader = getResourceLoader(request);
         if(null == loader)
         {
            // default to serving resources from the servlet context
            trace.warn("Unable to find ResourceLoader for ResourceServlet"
                  + " at servlet path:" + servletPath);
            loader = new ServletContextResourceLoader(getServletContext())
            {
               @Override
               public URL getResource(HttpServletRequest request, String path) throws IOException
               {
                  return super.getResource(request, path);
               }
            };
         }
         
         // Enable resource caching, but only if we aren't debugging
         if ( !debugMode && isSupportsCaching())
         {
            loader = new CachingResourceLoader(loader);
         }

         resourceLoaders.put(servletPath, loader);
      }

      return loader;
   }
   
   /**
    * @param request
    * @return
    */
   protected abstract ResourceLoader getResourceLoader(HttpServletRequest request);
   
   /**
    * @return
    */
   protected abstract boolean isSupportsCaching();
   
   /**
    * Reads the specified input stream into the provided byte array storage and writes it
    * to the output stream.
    */
   private static void pipeBytes(InputStream in, OutputStream out, byte[] buffer)
         throws IOException
   {
      int length;

      while ((length = (in.read(buffer))) >= 0)
      {
         out.write(buffer, 0, length);
      }
   }

   /**
    * Sets HTTP headers on the response which tell the browser to cache the resource
    * indefinitely.
    */
   private void setHeaders(URLConnection connection, HttpServletResponse response)
   {
      String contentType = connection.getContentType();
      if (contentType == null || "content/unknown".equals(contentType))
      {
         URL url = connection.getURL();
         String resourcePath = url.getPath();
         if (resourcePath.endsWith(".css"))
         {
            contentType = "text/css";
         }
         else if (resourcePath.endsWith(".js"))
         {
            contentType = "application/x-javascript";
         }
         else
         {
            contentType = getServletContext().getMimeType(resourcePath);
         }
      }
      if(contentType != null)
      {
         response.setContentType(contentType);
      }

      int contentLength = connection.getContentLength();
      if (contentLength >= 0)
      {
         response.setContentLength(contentLength);
      }

      long lastModified;
      try
      {
         lastModified = URLUtils.getLastModified(connection);
      }
      catch (IOException exception)
      {
         lastModified = -1;
      }

      if (lastModified >= 0)
      {
         response.setDateHeader("Last-Modified", lastModified);
      }

      // If we're not in debug mode, set cache headers
      if ( !debugMode)
      {
         // We set two headers: Cache-Control and Expires.
         // This combination lets browsers know that it is
         // okay to cache the resource indefinitely.

         // Set Cache-Control to "Public".
         response.setHeader("Cache-Control", "Public");

         // Set Expires to current time + one year.
         long currentTime = System.currentTimeMillis();

         response.setDateHeader("Expires", currentTime + ONE_YEAR_MILLIS);
      }
   }

   private static boolean canIgnore(Throwable t)
   {
      if (t instanceof InterruptedIOException)
      {
         // All "interrupted" IO is not notable
         return true;
      }
      else if (t instanceof SocketException)
      {
         // And any sort of SocketException should also be
         // ignored (Internet Explorer is a prime source of these,
         // as it doesn't try to close down sockets properly
         // when a user cancels)
         return true;
      }
      else if (t instanceof IOException)
      {
         String message = t.getMessage();
         // Check for "Broken pipe" and "connection was aborted"/
         // "connection abort" messages
         if ((message != null)
               && ((message.indexOf("Broken pipe") >= 0) || (message.indexOf("abort") >= 0)))
            return true;
      }
      return false;
   }

}
