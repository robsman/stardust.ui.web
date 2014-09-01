package org.eclipse.stardust.ui.web.html5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.html5.rest.HTML5FrameworkServices;
import org.eclipse.stardust.ui.web.html5.utils.ResourceDependency;
import org.eclipse.stardust.ui.web.html5.utils.ResourceDependencyUtils;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Subodh.Godbole
 *
 */
public class HTML5LandingPageFilter implements Filter
{
   private static final Logger trace = LogManager.getLogger(HTML5FrameworkServices.class);

   private static final String DEFAULT_LANDING_PAGE = "/portal-shell/index.html";
   private static final String SCRIPTS_PLACE_HOLDER = "<!-- DEPENDENCY_SCRIPTS_TO_BE_INJECTED -->";
   private static final String STYLES_PLACE_HOLDER = "<!-- DEPENDENCY_STYLES_TO_BE_INJECTED -->";

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
         
         ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(request.getSession()
               .getServletContext());

         try
         {
            // At the movement, landing page does not belong to 'plugins'
            Resource[] matchedResources = appContext.getResources("classpath*:" + landingPage);
            if (matchedResources.length > 0)
            {
               String landingPageContent = PluginUtils.readResource(matchedResources[0]);

               ArrayList<String> allScripts = new ArrayList<String>();
               ArrayList<String> allStyles = new ArrayList<String>();

               List<ResourceDependency> resourceDependencies = ResourceDependencyUtils.discoverDependencies(appContext);
               for (ResourceDependency resourceDependency : resourceDependencies)
               {
                  allScripts.addAll(resourceDependency.getLibs());
                  allScripts.addAll(resourceDependency.getScripts());
                  allStyles.addAll(resourceDependency.getStyles());
               }

               if (trace.isDebugEnabled())
               {
                  trace.debug("Dependency Scripts to Inject: " + allScripts);
               }

               if (trace.isDebugEnabled())
               {
                  trace.debug("Dependency Styles to Inject: " + allStyles);
               }

               // Process Scripts
               StringBuffer sbScripts = new StringBuffer();
               for (String script : allScripts)
               {
                  sbScripts.append("\n\t<script src=\"" + script + "\"></script>");
               }

               if (-1 != landingPageContent.indexOf(SCRIPTS_PLACE_HOLDER))
               {
                  landingPageContent = landingPageContent.replace(SCRIPTS_PLACE_HOLDER, sbScripts);
               }
               else
               {
                  trace.error("Landing page does not have a place holder '" + SCRIPTS_PLACE_HOLDER
                        + "' to inject the dependency scripts");
               }

               // Process Styles
               StringBuffer sbStyles = new StringBuffer();
               for (String style : allStyles)
               {
                  sbStyles.append("\n\t<link rel=\"stylesheet\" href=\"" + style + "\"/>");
               }

               if (-1 != landingPageContent.indexOf(STYLES_PLACE_HOLDER))
               {
                  landingPageContent = landingPageContent.replace(STYLES_PLACE_HOLDER, sbStyles);
               }
               else
               {
                  trace.error("Landing page does not have a place holder '" + STYLES_PLACE_HOLDER
                        + "' to inject the dependency styles");
               }

               response.getWriter().print(landingPageContent);
            }
         }
         catch (Exception e)
         {
            trace.error("Could not process landing page", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }

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
