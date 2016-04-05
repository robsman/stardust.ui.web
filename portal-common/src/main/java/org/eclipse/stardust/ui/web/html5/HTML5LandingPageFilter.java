package org.eclipse.stardust.ui.web.html5;

import static org.eclipse.stardust.ui.web.plugin.support.resources.PluginResourceUtils.resolveResources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.stardust.ui.web.common.Constants;
import org.eclipse.stardust.ui.web.common.app.messaging.MessageProcessor;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.spi.theme.ThemeProvider;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.SecurityUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.html5.rest.HTML5FrameworkServices;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.html5.utils.ResourceDependency;
import org.eclipse.stardust.ui.web.html5.utils.ResourceDependencyUtils;
import org.eclipse.stardust.ui.web.plugin.support.resources.PluginResourceUtils;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils;
import org.eclipse.stardust.ui.web.plugin.utils.WebResource;
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
   private static final String SKIN_AND_OTHER_STYLES_PLACE_HOLDER = "<!-- SKIN_AND_OTHER_STYLES_TO_BE_INJECTED -->";


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
         if (handleTripOverSession(request, response))
         {
            return;
         }

         // This is required in HTTP header for IE9
         response.setHeader("X-UA-Compatible", "IE=edge,chrome=1");

         response.setContentType("text/html; charset=UTF-8");
         ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(request.getSession(false)
               .getServletContext());
         
         // At the movement, cookie overrides the config value, so set the cookie too!
         response.addCookie(new Cookie("i18next", HTML5FrameworkServices.getLocaleCode(null, request.getSession(false)
               .getServletContext())));

         try
         {
            // At the movement, landing page does not belong to 'plugins'
            List<Resource> matchedResources = resolveResources(appContext, "classpath*:" + landingPage);
            if ( !matchedResources.isEmpty())
            {
               String landingPageContent = PluginUtils.readResource(matchedResources.get(0));

               ArrayList<String> allScripts = new ArrayList<String>();
               ArrayList<String> allStyles = new ArrayList<String>();

               // Dependency Resources
               List<ResourceDependency> resourceDependencies = ResourceDependencyUtils.discoverDependencies(appContext);
               for (ResourceDependency resourceDependency : resourceDependencies)
               {
                  allScripts.addAll(getWebResourceUrls(resourceDependency.getLibs()));
                  allScripts.addAll(getWebResourceUrls(resourceDependency.getScripts()));
                  allStyles.addAll(getWebResourceUrls(resourceDependency.getStyles()));
               }

               if (trace.isDebugEnabled())
               {
                  trace.debug("Dependency Scripts to Inject: " + allScripts);
                  trace.debug("Dependency Styles to Inject: " + allStyles);
               }

               // Process Scripts & Styles
               landingPageContent = injectArtifacts(allScripts, landingPageContent, SCRIPTS_PLACE_HOLDER, true);
               landingPageContent = injectArtifacts(allStyles, landingPageContent, STYLES_PLACE_HOLDER, false);

               // Process other Styles
               List<String> otherStyles = new ArrayList<String>();
               otherStyles.addAll(getPluginViewIconStyleSheets(appContext)); // Icons Styles (for legacy requirements)
               otherStyles.addAll(getThemeStyleSheets(request.getSession(false).getServletContext())); // Theme Styles

               if (trace.isDebugEnabled())
               {
                  trace.debug("Other Styles to Inject: " + otherStyles);
               }

               landingPageContent = injectArtifacts(otherStyles, landingPageContent, SKIN_AND_OTHER_STYLES_PLACE_HOLDER, false);
               // Return the contents
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

   /**
    * TODO: Refactor at common place?
    * @param appContext
    * @return
    */
   public List<String> getPluginViewIconStyleSheets(ApplicationContext appContext)
   {
      final String STYLE_FILE_POSTFIX = "-icons.css";
      final String STYLES_FILE_PATH = "css/*-icons.css";

      List<String> styles = new ArrayList<String>();

      try
      {
         Set<String> cssFileNames = PluginResourceUtils.getMatchingFileNames(appContext, STYLES_FILE_PATH);
         
         for (String fileName : cssFileNames)
         {
            String pluginName = (fileName.substring(0, fileName.indexOf(STYLE_FILE_POSTFIX)));
            styles.add("plugins/" + pluginName + "/css/" + fileName);
         }
      }
      catch (Exception e)
      {
         trace.warn("Unable to retrieve View Icon Styles", e);
      }

      return styles;
   }

   /**
    * TODO: Refactor at common place?
    * @param context
    * @return
    */
   public List<String> getThemeStyleSheets(ServletContext context)
   {
      List<String> styles = new ArrayList<String>();

      try
      {
         ThemeProvider themeProvider = RestControllerUtils.resolveSpringBean(ThemeProvider.class, context);
         List<String> themeStyleSheets = themeProvider.getStyleSheets();
         if (CollectionUtils.isNotEmpty(themeStyleSheets))
         {
            for (String style : themeStyleSheets)
            {
               if (StringUtils.isNotEmpty(style))
               {
                  styles.add(style.startsWith("/") ? style.substring(1) : style);
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.warn("Unable to retrieve Theme Styles", e);
      }
      
      return styles;
   }

   /**
    * @param list
    * @return
    */
   private List<String> getWebResourceUrls(List<WebResource> list)
   {
      List<String> newList = new ArrayList<String>();

      for (WebResource webResource : list)
      {
         newList.add(webResource.webUri);
      }
      
      return newList;
   }

   /**
    * @param request
    * @param response
    * @return
    * @throws IOException
    */
   private boolean handleTripOverSession(HttpServletRequest request, HttpServletResponse response) throws IOException
   {
      HttpSession session = request.getSession(false);
      
      try
      {
         // Check for valid session
         RestControllerUtils.resolveSpringBean(UserProvider.class, session.getServletContext()).getUser();

         try
         {
            String uiCmd = FacesUtils.getQueryParameterValue(request.getQueryString(), Constants.URL_PARAM_UI_COMMAND);
            if (StringUtils.isNotEmpty(uiCmd))
            {
               // After redirect, need to close all of the already open views (if any) to avoid any side effects
               String cleanAllViewsCmd = "{'type': 'CleanAllViews', 'data': {}}";
               uiCmd = MessageProcessor.prependMessage(uiCmd, cleanAllViewsCmd);
               
               // Redirect
               String landingPage = FacesUtils.getServerBaseURL(request) + "/main.html";
               landingPage += "#uicommand=" + uiCmd;
               response.sendRedirect(response.encodeRedirectURL(landingPage));

               return true;
            }
         }
         catch (Throwable t)
         {
            trace.error("Error occurred while recovering session", t);
            // TODO?
         }

         return false;
      }
      catch (Throwable t)
      {
         trace.error("Landing page was accessed without authenticated session", t);

         try
         {
            trace.error("Redirecting to login page.");

            if (null == session)
            {
               session = request.getSession(true);
            }

            String redirectUri = FacesUtils.getServerBaseURL(request);

            String uiCmd = FacesUtils.getQueryParameterValue(request.getQueryString(), Constants.URL_PARAM_UI_COMMAND);
            if (StringUtils.isNotEmpty(uiCmd))
            {
               redirectUri += "?uicommand=" + uiCmd;
            }

            redirectUri = SecurityUtils.sanitizeValue(redirectUri);
            response.sendRedirect(response.encodeRedirectURL(redirectUri));
         }
         catch(Throwable tt)
         {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }

         return true;
      }
   }

   /**
    * @param list
    * @param content
    * @param placeholder
    * @param scripts
    * @return
    */
   private String injectArtifacts(List<String> list, String content, String placeholder, boolean scripts)
   {
      StringBuffer toInject = new StringBuffer();

      if (scripts)
      {
         for (String path : list)
         {
            toInject.append("\n\t<script src=\"" + path + "\"></script>");
         }
      }
      else
      {
         for (String path : list)
         {
            toInject.append("\n\t<link rel=\"stylesheet\" href=\"" + path + "\"/>");
         }
      }

      if (-1 != content.indexOf(placeholder))
      {
         content = content.replace(placeholder, toInject);
      }
      else
      {
         trace.error("Landing page does not have a place holder '" + placeholder + "' to inject the artifacts");
      }
      
      return content;
   }
}
