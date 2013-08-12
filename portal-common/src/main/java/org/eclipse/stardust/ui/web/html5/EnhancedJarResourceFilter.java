package org.eclipse.stardust.ui.web.html5;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

   private static enum CompareType
   {
      STARTS_WITH,
      ENDS_WITH,
      CONTAINS,
      REG_EX
   }

   private String replacePattern = "/";

   private List<String> skipPaths;
   private List<String> skipExtenssions;
   private List<String> restrictLibs;

   /* (non-Javadoc)
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig cfg) throws ServletException
   {
      replacePattern = getInitParameter(cfg, "replacePattern", replacePattern);

      String skipPath = getInitParameter(cfg, "skipPaths", "");
      skipPaths = prepareList(skipPath);

      String skipExtenssion = getInitParameter(cfg, "skipExtenssions", "");
      skipExtenssions = prepareList(skipExtenssion);

      String restrictLib = getInitParameter(cfg, "restrictLibs", "");
      restrictLibs = prepareList(restrictLib);

      trace.info("EnhancedJarResourceFilter configured with");
      trace.info("\tskipPaths = " + skipPaths);
      trace.info("\tskipExtenssions = " + skipExtenssions);
      trace.info("\trestrictLibs = " + restrictLibs);
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
         if (trace.isDebugEnabled())
         {
            trace.debug("Looking for Resource Path: " + path);
         }

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
            if (!compare(skipPaths, "/" + path, CompareType.STARTS_WITH)
                  && !compare(skipExtenssions, stripUrlParams("/" + path), CompareType.ENDS_WITH))
            {
               URL resource = getClass().getClassLoader().getResource(path);
   
               // TODO: To use RegEx
               if (null != resource
                     && (isEmpty(restrictLibs) || compare(restrictLibs, resource.getPath(), CompareType.CONTAINS)))
               {
                  InputStream in = getClass().getClassLoader().getResourceAsStream(path);
                  if (in != null)
                  {
                     determineContentType(path, response);
                     IOUtils.copy(in, response.getOutputStream());
                     
                     if (trace.isDebugEnabled())
                     {
                        trace.debug("\tFound Resource Path: " + path);
                     }
                     return;
                  }
               }
            }
         }

         if (trace.isDebugEnabled())
         {
            trace.debug("\tSkipped Resource Path: " + path);
         }
      }
      catch (Exception ex)
      {
         // Discarded, go follow the filter chain.
      }

      chain.doFilter(req, res);
   }

   /**
    * @param list
    * @param value
    */
   private List<String> prepareList(String value)
   {
      List<String> list = new ArrayList<String>();
      
      if (StringUtils.isNotEmpty(value))
      {
         Iterator<String> it = StringUtils.split(value, ",");
         while (it.hasNext())
         {
            String part = it.next();
            if (StringUtils.isNotEmpty(part))
            {
               list.add(part.trim());
            }
         }
      }

      return list;
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
      return path;
   }

   /**
    * Iterate over list to compare value as per type
    * @param list
    * @param value
    * @return
    */
   private boolean compare(List<String> list, String value, CompareType type)
   {
      for (String str : list)
      {
         switch(type)
         {
         case STARTS_WITH:
            if (value.startsWith(str))
            {
               return true;
            }
            break;
         case ENDS_WITH:
            if (value.endsWith(str))
            {
               return true;
            }
            break;
         case CONTAINS:
         default:
            if (value.contains(str))
            {
               return true;
            }
            break;
         }
      }
      return false;
   }

   /**
    * @param value
    * @return
    */
   private String stripUrlParams(String value)
   {
      if (StringUtils.isNotEmpty(value))
      {
         if (value.indexOf("?") > -1)
         {
            value = value.substring(0, value.indexOf("?"));
         }
         if (value.indexOf("#") > -1)
         {
            value = value.substring(0, value.indexOf("#"));
         }
      }      
      return value;
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
   private void determineContentType(String path, HttpServletResponse response)
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
