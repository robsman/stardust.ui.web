package org.eclipse.stardust.ui.web.html5;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.ui.web.common.util.StringUtils;

/**
 * @author Subodh.Godbole
 *
 */
public class HTML5LandingPageFilter implements Filter
{
   private static final String DEFAULT_LANDING_PAGE = "/portal-shell/index.html";

   private String landingPage;

   /* (non-Javadoc)
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig cfg) throws ServletException
   {
      landingPage = getInitParameter(cfg, "landingPage", landingPage);
      if (StringUtils.isEmpty(landingPage))
      {
         landingPage = DEFAULT_LANDING_PAGE;
      }
   }

   /* (non-Javadoc)
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
         ServletException
   {

      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) res;

      if(!response.isCommitted())
      {
         // This is required in HTTP header for IE9
         response.setHeader("X-UA-Compatible", "IE=10,chrome=1");
         request.getRequestDispatcher(landingPage).forward(request, response);
         return;
      }

      chain.doFilter(req, res);
   }

   /* (non-Javadoc)
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {
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
