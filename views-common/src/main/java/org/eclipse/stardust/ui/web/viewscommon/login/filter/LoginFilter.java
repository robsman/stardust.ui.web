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
package org.eclipse.stardust.ui.web.viewscommon.login.filter;

import static java.util.Arrays.asList;
import static org.eclipse.stardust.ui.web.common.util.CollectionUtils.newArrayList;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.beans.ApplicationContext;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.login.dialogs.LoginDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.PluginResourceUtils;

import com.icesoft.faces.context.effects.JavascriptContext;



public class LoginFilter implements Filter
{

   protected FilterConfig filterCfg;
   private ServletContext servletContext;
   
   private final static String LOGIN_PAGE = "loginPage";
   private final static String DEFAULT_LOGIN_PAGE = "plugins/views-common/login.iface";
   
   private final static String LOGOUT_PAGE = "logoutPage";
   private final static String DEFAULT_LOGOUT_PAGE = "/ipp/common/ippPortalLogout.jsp";
   
   private final static String MAIN_PAGE = "mainPage";
   private final static String DEFAULT_MAIN_PAGE = "/main.html";
   
   private final static String PRINCIPAL_USER_ROLES = "principalUserRoles";
   
   private final static String PARAM_PUBLIC_URI_PATTERNS = "publicUriPatterns";
   
   private static final String ANY_PLUGIN_URI_PREFIX = "/plugins/<anyId>";
   
   private static final String DEFAULT_PUBLIC_URI_PATTERNS = ANY_PLUGIN_URI_PREFIX
         + "/public/*";
   
   private static final String PRINCIPAL_LOGIN_INIT_PAGE = "/plugins/common/initializeSession.iface";
   
   private String loginPage;
   private String logoutPage;
   private String mainPage;
   
   private List<String> principalUserRoles;
   
   private List<String> publicUris = newArrayList();
   private List<String> publicAnyPluginUris = newArrayList();
   
   protected final static Logger trace = LogManager.getLogger(LoginFilter.class);
   
   public final static String RETURN_URL_PARAM = "returnUrl";
   public final static String SINGLE_VIEW_PREFIX = "portalSingleView";

   public void destroy()
   {
      this.filterCfg = null;
      this.principalUserRoles = null;
   }
   
   private void forwardToPage(HttpServletRequest request, HttpServletResponse response,
         String page, boolean forwardFacesMessages)
   {
      RequestDispatcher dispatcher = request.getRequestDispatcher(page);
      try
      {
         dispatcher.forward(request, response);
      }
      catch(Exception e)
      {
         trace.error("Unable to forward to " + page, e);
      }
   }
   
   private boolean handlePrincipalLogin(HttpServletRequest request,
         HttpServletResponse response, FacesContext facesContext,
         SessionContext sessionContext)
   {
      try
      {
         trace.info("Setting session by LoginFilter");
         sessionContext.initPrincipalSession(request);
      }
      catch(Exception e)
      {
         trace.error("Error occurred durin login", e);
         
         HttpSession httpSession = request.getSession(false);
         if(httpSession != null)
         {
            httpSession.invalidate();
         }
         request.getSession(true);
         
         forwardToPage(request, response, logoutPage, false);
         return false;
      }
      return true;
   }
   
   private void handleJsfNavigation(FacesContext facesContext, String viewId,
         String outcome)
   {
      NavigationHandler navHandler = facesContext.getApplication().getNavigationHandler();
      ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
      if(navHandler != null && viewHandler != null)
      {
         UIViewRoot loginView = viewHandler.createView(facesContext, viewId);
         facesContext.setViewRoot(loginView);
         
         navHandler.handleNavigation(facesContext, null, outcome);
      }
   }
   
   private void handleInvalidSession(HttpServletRequest request,
         HttpServletResponse response, FacesContext facesContext)
   {
      String user = request.getParameter("j_username");
      String password = request.getParameter("j_password");
      if(!StringUtils.isEmpty(user) && !StringUtils.isEmpty(password))
      {
         request.getSession(true);
         LoginDialogBean loginBean = (LoginDialogBean) FacesUtils.getBeanFromContext(
               facesContext, LoginDialogBean.BEAN_ID);
         if(loginBean != null)
         {
            loginBean.setAccount(user);
            loginBean.setPassword(password);
            loginBean.setRealm(request.getParameter("realm"));
            loginBean.setDomain(request.getParameter("domain"));
            loginBean.setPartition(request.getParameter("partition"));
            try
            {
               String outcome = loginBean.login();
               if(Constants.WORKFLOW_PRINCIPAL_LOGIN.equals(outcome))
               {
                  // cannot be handled yet - go back to the logout page
                  // TODO: If you try to forward to the proxy page on JBOSS
                  //       an error 400 is thrown with the message:
                  //       "Invalid direct reference to form login page"
                  forwardToPage(request, response, logoutPage, false);
               }
               else
               {
                  handleJsfNavigation(facesContext, "/" + loginPage, outcome);
               }
            }
            catch(LoginFailedException e)
            {
               
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   @SuppressWarnings("unchecked")
   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException
   {
      final HttpServletRequest request = (HttpServletRequest) req;
      final HttpServletResponse response = (HttpServletResponse) res;

      String preForwardUri = (String)request.getAttribute("javax.servlet.forward.request_uri");
      String preForwardContextPath = (String)request.getAttribute("javax.servlet.forward.context_path");

      String requestUri = StringUtils.isEmpty(preForwardUri)
            ? request.getRequestURI()
            : preForwardUri;
      String contextPath = StringUtils.isEmpty(preForwardContextPath)
            ? request.getContextPath()
            : preForwardContextPath;
      
      if (isPublicUri(requestUri.substring(contextPath.length()))
            && !requestUri.endsWith(loginPage))
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Bypassing login check for public URI: " + requestUri);
         }

         chain.doFilter(request, response);
         return;
      }
      FacesContext facesContext = FacesUtils.getFacesContext(servletContext, request, response);
     
      SessionContext sessionContext = SessionContext.findSessionContext(facesContext);

      includeCustomJS(facesContext);

      if(null != sessionContext)
      {
         if( !sessionContext.isSessionInitialized())
         {
            if((null != request.getUserPrincipal()) && isUserInRoleList(request))
            {
               // initialize session
               if( !handlePrincipalLogin(request, response, facesContext, sessionContext))
               {
                  return;
               }
            }
            if(!sessionContext.isSessionInitialized())
            {
               if(!requestUri.contains(loginPage))
               {
                  trace.info("Redirect to login, because session was not initialized.");
                  StringBuffer url = new StringBuffer(request.getContextPath());
                  if (requestUri.endsWith(PRINCIPAL_LOGIN_INIT_PAGE))
                  {
                     url.append("/").append(loginPage); // While login for principal user,
                                                        // no session present
                  }
                  else
                  {
                     url.append(logoutPage); // Always forward to logout, so that checks
                                             // applied and cleanup happens properly,
                                             // before login page is displayed
                  }
                  
                  Map<String, String> urlParams = new LinkedHashMap<String, String>();

                  // Add Return URL only if requested page is not a default main page itself
                  // In this case return URL is not required as any ways user will be 
                  // redirected to main page after successful login 
                  if (!requestUri.endsWith(mainPage))
                  {
                     String fileName = requestUri.substring(requestUri.lastIndexOf("/") + 1);
                     if (!fileName.startsWith(SINGLE_VIEW_PREFIX))
                     {
                        //In Principal mode we have to go always through container managed security via the login proxy.
                        if (!ApplicationContext.isPrincipalLogin())
                        {
                           urlParams.put(RETURN_URL_PARAM, requestUri);
                        }
                     }
                  }

                  String key;
                  Enumeration<String> currentUrlParams = request.getParameterNames();
                  while (currentUrlParams.hasMoreElements())
                  {
                     key = currentUrlParams.nextElement();
                     if ("j_username".equals(key) || "j_password".equals(key))
                     {
                        continue;
                     }
                     urlParams.put(key, request.getParameter(key));
                  }

                  if (urlParams.size() > 0)
                  {
                     url.append("?");
                     for (Entry<String, String> entry : urlParams.entrySet())
                     {
                        url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                     }
                     url.deleteCharAt(url.length() - 1);
                  }
                  response.sendRedirect(response.encodeRedirectURL(url.toString()));
                  return;
               }
               else
               {
                  if( !request.isRequestedSessionIdValid())
                  {
                     handleInvalidSession(request, response, facesContext);
                  }
               }
            }
         }
         else if(sessionContext.isSessionInitialized()
               && !ApplicationContext.isPrincipalLogin()
               && requestUri.indexOf(loginPage) > -1)
         {
            LoginDialogBean loginBean = (LoginDialogBean)FacesUtils.getBeanFromContext(
                  facesContext, LoginDialogBean.BEAN_ID);
            handleJsfNavigation(facesContext, "/" + loginPage, loginBean.getNavigationOutcome());
         }
      }

      chain.doFilter(request, response);
   }

   /**
    * This function injects required JS in the context.
    * This is required for JSF Based Activity Panels. This needs to be done at login,
    * Doing this at later stage causes issues when used with ICEPush, like 'unload' event gets fired at browser.
    */
   protected void includeCustomJS(FacesContext facesContext)
   {
      try
      {
         if (null != facesContext)
         {
            String jsFile = "/plugins/processportal/integration/iframe/iframe-panel-server-support.js";
      
            if (!asList(JavascriptContext.getIncludedLibs(facesContext)).contains(jsFile))
            {
               trace.debug("Injecting ICEfaces UI server support library.");
               JavascriptContext.includeLib(jsFile, facesContext);
            }
         }
      }
      catch (Exception e)
      {
         trace.error("Cannot include Custom JS at this point" + e.getMessage());
      }
   }

   private boolean isUserInRoleList(HttpServletRequest request)
   {
      boolean userInList = principalUserRoles.isEmpty() ? true : false;
      for(Iterator<String> roleIter = principalUserRoles.iterator(); roleIter.hasNext() && !userInList;)
      {
         userInList = request.isUserInRole(roleIter.next());
      }
      return userInList;
   }

   public void init(FilterConfig filterCfg) throws ServletException
   {
      this.filterCfg = filterCfg;
      this.servletContext = filterCfg.getServletContext();

      // Login Page
      this.loginPage = filterCfg.getInitParameter(LOGIN_PAGE);
      if(StringUtils.isEmpty(loginPage))
      {
         this.loginPage = DEFAULT_LOGIN_PAGE;
      }
      else
      {
         if(loginPage.charAt(0) == '/')
         {
            loginPage = loginPage.substring(1);
         }
      }

      // Logout Page 
      this.logoutPage = filterCfg.getInitParameter(LOGOUT_PAGE);
      if(StringUtils.isEmpty(logoutPage))
      {
         this.logoutPage = DEFAULT_LOGOUT_PAGE;
      }

      // Main Page
      this.mainPage = filterCfg.getInitParameter(MAIN_PAGE);
      if(StringUtils.isEmpty(mainPage))
      {
         this.mainPage = DEFAULT_MAIN_PAGE;
      }
      else
      {
         if(mainPage.charAt(0) == '/')
         {
            mainPage = mainPage.substring(1);
         }
      }

      // Principal User Roles
      Iterator<String> roleIter = StringUtils.split(
            filterCfg.getInitParameter(PRINCIPAL_USER_ROLES), ",");
      this.principalUserRoles = newArrayList();
      while(roleIter.hasNext())
      {
         principalUserRoles.add(roleIter.next());
      }
      
      // Public URI Patterns
      String publicUriPatterns = filterCfg.getInitParameter(PARAM_PUBLIC_URI_PATTERNS);
      if (null == publicUriPatterns)
      {
         publicUriPatterns = DEFAULT_PUBLIC_URI_PATTERNS;
      }

      for (String pattern : publicUriPatterns.split(","))
      {
         if ( !StringUtils.isEmpty(pattern))
         {
            if (pattern.contains("*"))
            {
               // there most be at most one wildcard, and it must either be at the first
               // or at the last position
               if (( !pattern.startsWith("*") && !pattern.endsWith("*"))
                     || (pattern.startsWith("*") && pattern.substring(1).contains("*"))
                     || (pattern.endsWith("*") && pattern.substring(0,
                           pattern.length() - 1).contains("*")))
               {
                  trace.warn("Ignoring invalid publicUriPattern '"
                        + pattern
                        + "'. A '*' wildcards must bei either at the first or last position.");
                  continue;
               }
            }
            
            trace.info("URIs matching '" + pattern + "' will be publicly accessible.");

            if (pattern.startsWith(ANY_PLUGIN_URI_PREFIX))
            {
               publicAnyPluginUris.add(pattern.substring(ANY_PLUGIN_URI_PREFIX.length()));
            }
            else
            {
               publicUris.add(pattern);
            }
         }
      }
      
      if (publicAnyPluginUris.isEmpty() && publicUris.isEmpty())
      {
         trace.info("Publicly accessible URIs are disabled.");
      }
   }

   private boolean isPublicUri(String requestUri)
   {
      boolean isPublic = false;
      
      if (PluginResourceUtils.isPluginPath(requestUri) && !publicAnyPluginUris.isEmpty())
      {
         String pluginPath = PluginResourceUtils.getFile(requestUri);
         
         for (String pattern : publicAnyPluginUris)
         {
            if (isMatch(pattern, pluginPath))
            {
               isPublic = true;
               break;
            }
         }
      }
      
      if ( !isPublic && !publicUris.isEmpty())
      {
         for (String pattern : publicUris)
         {
            if (isMatch(pattern, requestUri))
            {
               isPublic = true;
               break;
            }
         }
      }
      
      return isPublic;
   }
   
   private static boolean isMatch(String pattern, String pluginPath)
   {
      if (pattern.startsWith("*"))
      {
         return pluginPath.endsWith(pattern.substring(1));
      }
      else if (pattern.endsWith("*"))
      {
         return pluginPath.startsWith(pattern.substring(0, pattern.length() - 1));
      }
      else
      {
         return pluginPath.equals(pattern);
      }
   }
}
