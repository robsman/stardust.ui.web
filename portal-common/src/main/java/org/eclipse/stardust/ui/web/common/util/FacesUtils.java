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
package org.eclipse.stardust.ui.web.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.faces.FactoryFinder;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.el.ValueBinding;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.plaf.basic.BasicMenuUI;

import org.eclipse.stardust.ui.web.common.ToolbarSection;
import org.eclipse.stardust.ui.web.common.app.PortalApplicationSingleView;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.Constants;
import org.eclipse.stardust.ui.web.html5.ManagedBeanUtils;

/**
 * Provides Utility methods that can be called from XHTMLs
 * @author Subodh.Godbole
 */
public class FacesUtils implements Constants
{
   private static final Logger trace = LogManager.getLogger(FacesUtils.class);
   
   private static final List<ClassLoader> registeredCl = CollectionUtils.newList();
   
   private static final String QUERY_SEPARATOR = "&;";

   /**
    * @param list
    * @param obj
    * @return
    */
   public static boolean contains(List<Object> list, Object obj)
   {
      return list.contains(obj);
   }

   /**
    * @param mainString
    * @param subString
    * @return
    */
   public static boolean contains(String mainString, String subString)
   {
      return mainString.contains(subString);
   }

   /**
    * @param mainString
    * @param subString
    * @return
    */
   public static boolean endsWith(String mainString, String subString)
   {
      return mainString.endsWith(subString);
   }

   /**
    * @param view
    * @param toolbarSection
    * @return
    */
   public static boolean isToolbarEnabledForView(View view, ToolbarSection toolbarSection)
   {
      boolean result = false;

      if (null != view && null != toolbarSection)
      {
	      if (StringUtils.isEmpty(toolbarSection.getRequiredView()))
	      {
	         // when no required view specified, the toolbar is not view-specific and 
	         // should be shown always
	         result = true;
	      }
	      else
	      {
	         if ((view != null) && (view.getDefinition() != null))
	         {
	            result = toolbarSection.getRequiredView().contains(
	               view.getDefinition().getName());
	         }
	      }
      }
      
      return result;
   }

   /**
    * @param object
    * @param className
    * @return
    */
   public static Boolean instanceOf(Object object, String className)
   {
      if(object != null && !StringUtils.isEmpty(className))
      {
         try
         {
            Class<?> targetClass = Class.forName(className);
            object.getClass().asSubclass(targetClass);
            return true;
         }
         catch(Exception e)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("[instanceOf]: Object:" + object.getClass().getName() + " is not instanceof: " + className);
            }
         }
      }

      return false;
   }
   
   /**
    * @param object
    * @param property
    * @return Returns Map with:
    *   key: object     value: Target Invokable Object
    *   key: property   value: Property to be invoked on Target Object
    */
   public static Map<String, Object> getObjectPropertyMapping(Object object, String property)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("getObjectPropertyMapping() -> " + object + " : " + property);
      }
      
      Map<String, Object> returnMap = new HashMap<String, Object>();
      if(object == null || StringUtils.isEmpty(property))
      {
         returnMap.put("object", object);
         returnMap.put("property", property);
      }
      else
      {
         String nestedProperty = property;
         try
         {
            Object nestedObject = object;
            StringTokenizer propTokens = new StringTokenizer(property, ".");
            int tokens = propTokens.countTokens();
            while(propTokens.hasMoreTokens())
            {
               nestedProperty = propTokens.nextToken();
               if (trace.isDebugEnabled())
               {
                  trace.debug("getObjectPropertyMapping(): Loop -> " + nestedObject + " : " + nestedProperty);
               }
               if(tokens == 1) // Process only till second last Token
                  break;

               if(nestedProperty.endsWith("]")) // COMPLEX Property with LIST
               {
                  int start = nestedProperty.indexOf("[");
                  int end = nestedProperty.indexOf("]");

                  int dataIndex = Integer.parseInt(nestedProperty.substring(start+1, end));
                  nestedProperty = nestedProperty.substring(0, start);

                  nestedObject = ReflectionUtils.invokeGetterMethod(nestedObject, nestedProperty);
                  if(nestedObject instanceof List)
                  {
                     nestedObject = ((List)nestedObject).get(dataIndex);
                  }
                  else
                  {
                     throw new IllegalStateException("Dynamic Property '" + nestedProperty +
                           "' doesn't return List");
                  }
                  nestedProperty = "";
               }
               else
               {
                  nestedObject = ReflectionUtils.invokeGetterMethod(nestedObject, nestedProperty);
               }
               tokens--;
            }
            
            if (trace.isDebugEnabled())
            {
               trace.debug("getObjectPropertyMapping(): Loop END -> " + nestedObject + " : " + nestedProperty);
            }
            
            returnMap.put("object", nestedObject);
            returnMap.put("property", nestedProperty);
         }
         catch(Exception e)
         {
            throw new IllegalStateException(
                  "Nested Property: " + nestedProperty + " not found in Root Property: " + 
                  property + ", For Object " + object);
         }
      }
      
      return returnMap;
   }
   
   /**
    * @param beanName
    * @return
    */
   public static Object getBeanFromContext(String beanName)
   {
      return ManagedBeanUtils.getManagedBean(beanName);
   }
   
   /**
    * @param context
    * @param beanName
    * @return
    */
   public static Object getBeanFromContext(FacesContext context, String beanName)
   {
      return ManagedBeanUtils.getManagedBean(context, beanName);
   }
   
   /**
    * @param exeception
    * @return
    */
   public static String getStackTrace(Throwable exeception)
   {
      StringWriter sw = new StringWriter();
      if(exeception != null)
         exeception.printStackTrace(new PrintWriter(sw));

      return sw.toString();
   }
   
   public static FacesContext getFacesContext(ServletContext servletContext,
         HttpServletRequest request, HttpServletResponse response)
   {
      FacesContext facesContext = null;
      ClassLoader cl = _getClassLoader();
      if(!registeredCl.contains(cl))
      {
         // If FacesContext was not created before, we have to get the instance
         // in a safe manner. Otherwise this exception could be thrown:
         // java.lang.IllegalStateException: Factory already available for this class loader.
         facesContext = getSafeFacesContext(servletContext, request, response);
         registeredCl.add(cl);
      }
      else
      {
         facesContext = getInternalFacesContext(servletContext, request, response);
      }
            
      return facesContext;
   }
   
   private static synchronized FacesContext getSafeFacesContext(ServletContext servletContext,
         HttpServletRequest request, HttpServletResponse response)
   {
      return getInternalFacesContext(servletContext, request, response);
   }
   
   private static FacesContext getInternalFacesContext(ServletContext servletContext,
         HttpServletRequest request, HttpServletResponse response)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if (facesContext == null)
      {
         FacesContextFactory contextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
         LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY); 
         Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

         facesContext = contextFactory.getFacesContext(servletContext, request, response, lifecycle);
      }
      
      return facesContext;
   }
   
   static private ClassLoader _getClassLoader()
   {
     return Thread.currentThread().getContextClassLoader();
   }
   
   public static String getRequestParameter(String param)
   {
      ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
      HttpServletRequest req = (HttpServletRequest) ectx.getRequest();
      if(req.getParameterMap().containsKey(param))
      {
         return req.getParameter(param);
      }
      return null;
   }
   
   /**
    * @param paramName
    * @return
    */
   public static String getQueryParameterValue(final String paramName)
   {
      ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
      HttpServletRequest req = (HttpServletRequest) ectx.getRequest();

      Map<String, List<String>> queryParameters = parseQueryString(req.getQueryString());
      if (queryParameters.containsKey(paramName))
      {
         return queryParameters.get(paramName).get(0);
      }
      return null;
   }

   /**
    * @param facesContext
    * @param paramName
    * @return
    */
   public static String getQueryParameterValue(String queryString, final String paramName)
   {
      Map<String, List<String>> queryParameters = parseQueryString(queryString);
      if (queryParameters.containsKey(paramName))
      {
         return queryParameters.get(paramName).get(0);
      }
      return null;
   }

   /**
    * @param facesContext
    * @param paramName
    * @return
    */
   public static String getQueryParameterValue(FacesContext facesContext, final String paramName)
   {
      ExternalContext ectx = facesContext.getExternalContext();
      HttpServletRequest req = (HttpServletRequest) ectx.getRequest();

      Map<String, List<String>> queryParameters = parseQueryString(req.getQueryString());
      if (queryParameters.containsKey(paramName))
      {
         return queryParameters.get(paramName).get(0);
      }
      return null;
   }
   
   /**
    * @param paramName
    * @return
    */
   public static List<String> getQueryParameterValues(final String paramName)
   {
      ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
      HttpServletRequest req = (HttpServletRequest) ectx.getRequest();

      Map<String, List<String>> queryParameters = parseQueryString(req.getQueryString());
      if (queryParameters.containsKey(paramName))
      {
         return queryParameters.get(paramName);
      }
      return null;
   }
   
   /**
    * @param queryString
    * @return
    */
   public static Map<String, List<String>> parseQueryString(final String queryString)
   {
      final Map<String, List<String>> queryParameters = new LinkedHashMap<String, List<String>>();

      if (queryString != null)
      {
         StringTokenizer stringTokenizer = new StringTokenizer(queryString, QUERY_SEPARATOR);

         while (stringTokenizer.hasMoreTokens())
         {
            String queryParam = stringTokenizer.nextToken();

            int indexOfEq = queryParam.indexOf('=');

            String paramName;
            String paramValue;

            try
            {
               if (indexOfEq != -1)
               {
                  paramName = queryParam.substring(0, indexOfEq);
                  paramValue = queryParam.substring(indexOfEq + 1);

                  if (StringUtils.isNotEmpty(paramName))
                  {
                     paramName = URLDecoder.decode(paramName, "UTF-8");

                     if (StringUtils.isNotEmpty(paramValue))
                     {
                        paramValue = URLDecoder.decode(paramValue, "UTF-8");
                     }
                     List<String> paramValues = queryParameters.get(paramName);

                     if (paramValues == null)
                     {
                        paramValues = new ArrayList<String>();
                        queryParameters.put(paramName, paramValues);
                     }
                     paramValues.add(paramValue);
                  }
               }
            }
            catch (Exception e)
            {
               trace.error("Failed decoding query parameters.", e);
            }
         }
      }
      return queryParameters;
   }
   
   public static void sendRedirect(String toUrl) throws IOException
   {
      ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
      HttpServletResponse response = (HttpServletResponse) ectx.getResponse();
      response.sendRedirect(response.encodeRedirectURL(toUrl));
   }

   /**
    * @deprecated 
    */
   public static void refreshPage()
   {
      handleNavigation("pageRefresh");
   }

   /**
    * Navigation happens on relative path as refresh/redirect is to same page
    */
   public static void handleNavigation(String navigationRuleId)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      ExternalContext externalContext = facesContext.getExternalContext();
      try
      {
         String requestURI = ((HttpServletRequest) externalContext.getRequest()).getRequestURI();
         if (requestURI.endsWith("portalSingleViewMain.iface"))
         {
            String url = "portalSingleViewMain.iface" + PortalApplicationSingleView.getSingleViewParams();
            externalContext.redirect(url);
         }
         else if (requestURI.endsWith("portalMain.iface"))
         {
            externalContext.redirect("portalMain.iface");
         }
         else if (requestURI.endsWith("main.iface"))
         {
            externalContext.redirect("main.iface");
         }
      }
      catch (IOException e)
      {
         trace.error("Failed navigation for request URI", e);
      }
   }

   /**
    * @param facesContext
    * @param clientId
    * @param message
    */
   public static void addErrorMessage(FacesContext facesContext, String clientId, String message)
   {
      facesContext.addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
   }

   /**
    * @param clientId
    * @param message
    */
   public static void addErrorMessage(String clientId, String message)
   {
      addErrorMessage(FacesContext.getCurrentInstance(), clientId, message);
   }

   /**
    * Clears ALL previously submitted form fields values
    * @param component
    */
   public static void clearFacesTreeValues()
   {
      clearFacesTreeValues(FacesContext.getCurrentInstance().getViewRoot());
   }

   /**
    * Clears ALL previously submitted form fields values
    * @param component
    */
   @SuppressWarnings("unchecked")
   public static void clearFacesTreeValues(UIComponent component)
   {
      clearFacesComponentValues(component);

      Iterator subComponents = component.getFacetsAndChildren();
      while (subComponents.hasNext())
      {
         clearFacesTreeValues((UIComponent) subComponents.next());
      }
   }
   
   /**
    * 
    * @param component
    */
   public static void clearFacesComponentValues(UIComponent component)
   {
      if (component instanceof EditableValueHolder)
      {
         try
         {
            /* The reflection code below caters to all subclasses of javax.faces.componentUIInput
             * that have the isDisabled method. */
            Object ret = ReflectionUtils.invokeMethod(component, "isDisabled");
            if ((Boolean) ret)
            {
               return;
            }
         }
         catch (Exception e)
         {
            // Do nothing. Carry on with the regular flow.
         }
         
         EditableValueHolder editableValueHolder = (EditableValueHolder) component;
         editableValueHolder.setSubmittedValue(null);
         editableValueHolder.setValue(null);
      }
   }

   /**
    * Returns the UIComponent from FacesMessage clientId
    * 
    * @param parent
    * @param partialClientId
    * @return
    */
   public static UIComponent matchComponentInHierarchy(UIComponent parent, String partialClientId)
   {
      UIComponent uiComponent = null;

      if (parent != null)
      {

         String parentClientId = parent.getClientId(FacesContext.getCurrentInstance());

         if ((parentClientId != null) && (parentClientId.indexOf(partialClientId) >= 0))
         {
            uiComponent = parent;
         }
         else
         {
            Iterator<UIComponent> itr = parent.getFacetsAndChildren();
            if (itr != null)
            {
               while (itr.hasNext())
               {
                  UIComponent child = itr.next();
                  uiComponent = matchComponentInHierarchy(child, partialClientId);

                  if (uiComponent != null)
                  {
                     break;
                  }
               }
            }
         }
      }

      return uiComponent;
   }

   
   /**
    * returns servers base URL
    * 
    * @return
    */
   public static String getServerBaseURL()
   {
      HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
            .getRequest();

      return getServerBaseURL(request);
   }

   /**
    * returns servers base URL
    * 
    * @return
    */
   public static String getServerBaseURL(HttpServletRequest request)
   {
      return new StringBuffer(request.getScheme()).append("://").append(request.getServerName()).append(":").append(
            request.getServerPort()).append(request.getContextPath()).toString();
   }

//moved this method from BeanUtil
 @Deprecated
 public static ValueBinding createValueBinding(String expr)
 {
    return FacesContext.getCurrentInstance().getApplication().createValueBinding(expr);
 }

   /**
    * @return
    */
   public static String getUserAgent()
   {
      String userAgent = null;

      Object reqObject = FacesContext.getCurrentInstance().getExternalContext().getRequest();
      if (reqObject instanceof HttpServletRequest)
      {
         HttpServletRequest request = (HttpServletRequest) reqObject;
         userAgent = request.getHeader("user-agent");

         // for WebSphere
         if (null == userAgent)
         {
            userAgent = request.getHeader("User-Agent");
         }
      }
      else
      {
         trace.error("Received" + reqObject + ", Not supoprting environment other than HttpServletRequest");
      }
      return userAgent;
   }
   
   public static Locale getLocaleFromRequest()
   {
      return ManagedBeanUtils.getLocale();
   }
   
   /**
    * 
    * @return
    */
   public static String getPortalTitle()
   {
      String headingNlsKey = FacesContext.getCurrentInstance().getExternalContext().getInitParameter(LOGIN_HEADING);
      // If message-bundle not found, search in common message bundle
      String baseName = FacesContext.getCurrentInstance().getExternalContext().getInitParameter(COMMON_MESSAGE_BUNDLE);
      return getPortalTitle(headingNlsKey, baseName, getLocaleFromRequest());
   }

   public static String getPortalTitle(String headerKey, String bundleBasName, Locale locale)
   {
      String result = null;

      if (!StringUtils.isEmpty(headerKey) && (-1 != headerKey.indexOf("#")))
      {
         String bundleName = null;
         String nlsKey = null;
         Iterator<String> i = StringUtils.split(headerKey, "#");
         if (i.hasNext())
         {
            bundleName = i.next();
         }
         if (i.hasNext())
         {
            nlsKey = i.next();
         }

         if (!StringUtils.isEmpty(bundleName) && !StringUtils.isEmpty(nlsKey))
         {
            result = getString(locale, bundleName, nlsKey, bundleBasName);
         }
      }

      if (null == result)
      {
         result = MessagePropertiesBean.getInstance().getString("portalFramework.title");
      }

      return result;
   }

  /**
   * 
   * @param locale
   * @param bundleName
   * @param key
   * @param bundleBasName
   * @return
   */
   public static String getString(Locale locale, String bundleName, String key, String bundleBasName)
   {
      String text = null;
      String failureMsg = null;
      try
      {
         if (bundleName != null)
         {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, getCurrentClassLoader());
            text = bundle != null ? bundle.getString(key) : null;
            if (text == null)
            {
               if (bundleBasName != null)
               {
                  bundle = ResourceBundle.getBundle(bundleBasName, locale, getCurrentClassLoader());
                  text = bundle != null ? bundle.getString(key) : null;
               }
            }
         }
      }
      catch (MissingResourceException e)
      {
         failureMsg = "cannot find '" + key + "' in ResourceBundle";
      }
      catch (Exception e)
      {
         failureMsg = "error getting value of '" + key + "' in resource bundle '";
      }
      finally
      {
         if (failureMsg != null)
         {
            trace.error(failureMsg);
         }
      }
      return text;
   }

   /**
    * 
    * @return
    */
   protected static ClassLoader getCurrentClassLoader()
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      if (null == loader)
      {
         loader = FacesUtils.class.getClassLoader();
      }

      return loader;
   }
}
   