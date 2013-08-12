package org.eclipse.stardust.ui.web.html5;

import java.io.IOException;
import java.io.InputStream;

import javax.naming.OperationNotSupportedException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;

/**
 * @author HTML5.TEAM
 * @author Subodh.Godbole
 *
 */
public class EnhancedJarResourceFilter implements Filter
{
   private static final Logger trace = LogManager.getLogger(EnhancedJarResourceFilter.class);

   private String replacePattern = "/";

   /* (non-Javadoc)
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig cfg) throws ServletException
   {
      replacePattern = getInitParameter(cfg, "replacePattern", replacePattern);
   }

   /* (non-Javadoc)
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
         ServletException
   {
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) res;

      try
      {
         String path = getResourcePath(request);

         if (path != null && path.startsWith(replacePattern))
         {
            path = path.substring(path.indexOf(replacePattern) + replacePattern.length());
         }

         if (path != null && path.startsWith("/"))
         {
            path = path.substring(1);
         }

         if (StringUtils.isNotEmpty(path))
         {
            InputStream in = getClass().getClassLoader().getResourceAsStream(path);
            if (in != null)
            {
               determineContentType(path, response);
               IOUtils.copy(in, response.getOutputStream());
               return;
            }
         }
      }
      catch (Exception ex)
      {
         // Discarded, go follow the filter chain.
      }

      chain.doFilter(req, res);
   }

   /**
    * @param request
    * @return
    */
   private String getResourcePath(HttpServletRequest request)
   {
      String path = "";
      if (null != request.getServletPath())
      {
         path = request.getServletPath();
      }
      if (null != request.getPathInfo())
      {
         path += request.getPathInfo();
      }
      
      if (trace.isDebugEnabled())
      {
         trace.debug("ResourcePath: " + path);
      }
      
      return path;
   }

   /* (non-Javadoc)
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {}

   /**
    * @param path
    * @param response
    */
   private void determineContentType(String path, HttpServletResponse response) throws OperationNotSupportedException
   {
      // TODO: find content-type from config
      if (path.endsWith(".js") || path.endsWith(".json"))
      {
         response.setContentType("application/javascript");
      }
      else if (path.endsWith(".css") || path.endsWith(".less"))
      {
         response.setContentType("text/css");
      }
      else if (path.endsWith(".jsp")) // Adding this as temporary fix
      {
         throw new OperationNotSupportedException();
      }
      else
      {
         response.setContentType("text/html");
      }
   }

   /**
    * @param cfg
    * @param name
    * @param def
    * @return
    */
   private String getInitParameter(FilterConfig cfg, String name, String def)
   {
      String ret = cfg.getInitParameter(name);
      return ret != null ? ret : def;
   }
}
