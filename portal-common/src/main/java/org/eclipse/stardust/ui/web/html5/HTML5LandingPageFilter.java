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

/**
 * @author Subodh.Godbole
 *
 */
public class HTML5LandingPageFilter implements Filter
{
   private String landingPage;

   /* (non-Javadoc)
    * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig cfg) throws ServletException
   {
      landingPage = getInitParameter(cfg, "landingPage", landingPage);
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
