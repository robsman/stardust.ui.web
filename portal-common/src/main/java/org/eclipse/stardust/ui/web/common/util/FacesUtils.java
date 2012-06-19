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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.FactoryFinder;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.el.ValueBinding;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.ui.web.common.ToolbarSection;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;


/**
 * Provides Utility methods that can be called from XHTMLs
 * @author Subodh.Godbole
 */
public class FacesUtils
{
   private static final Logger trace = LogManager.getLogger(FacesUtils.class);
   
   private static final List<ClassLoader> registeredCl = CollectionUtils.newList();

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

      if (null != view)
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
            trace.debug("[instanceOf]: Object:" + object.getClass().getName() + " is not instanceof: " + className);
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
      trace.debug("getObjectPropertyMapping() -> " + object + " : " + property);
      
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
               trace.debug("getObjectPropertyMapping(): Loop -> " + nestedObject + " : " + nestedProperty);

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
            
            trace.debug("getObjectPropertyMapping(): Loop END -> " + nestedObject + " : " + nestedProperty);
            
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
      return getBeanFromContext(FacesContext.getCurrentInstance(), beanName);
   }
   
   /**
    * @param context
    * @param beanName
    * @return
    */
   public static Object getBeanFromContext(FacesContext context, String beanName)
   {
      return context.getApplication().getVariableResolver().resolveVariable(
            FacesContext.getCurrentInstance(), beanName);
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
   
   public static Locale getLocaleFromView()
   {
      FacesContext facesCtx = FacesContext.getCurrentInstance();
      return getLocaleFromView(facesCtx);
   }
   
   public static Locale getLocaleFromView(FacesContext facesContext)
   {
      FacesContext facesCtx = FacesContext.getCurrentInstance();
      UIViewRoot view = facesCtx.getViewRoot(); 
      return view != null ? view.getLocale() :
         facesCtx.getExternalContext().getRequestLocale();
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
   
   public static void sendRedirect(String toUrl) throws IOException
   {
      ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
      HttpServletResponse response = (HttpServletResponse) ectx.getResponse();
      response.sendRedirect(response.encodeRedirectURL(toUrl));
   }
   
   /**
    * 
    */
   public static void refreshPage()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext,
            null, "pageRefresh");
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
}
