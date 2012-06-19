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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.FactoryFinder;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;


public abstract class FacesUtils
{
   private static final List registeredCl = CollectionUtils.newList();
   
   public static void showFacesMessage(FacesContext facesCtx, String clientId, FacesMessage facesMsg)
   {
      boolean msgInList = false;
      Iterator msgIter = facesCtx.getMessages();
      while(msgIter.hasNext() && !msgInList)
      {
         Object obj = msgIter.next();
         if(obj instanceof FacesMessage)
         {
            FacesMessage fm = (FacesMessage)obj;
            msgInList = CompareHelper.compare(facesMsg.getSummary(), fm.getSummary()) == 0 &&
               CompareHelper.compare(facesMsg.getDetail(), fm.getDetail()) == 0;
         }
      }
      if(!msgInList)
      {
         facesCtx.addMessage(clientId, facesMsg);
      }
   }
   
   public static void showFacesMessage(String clientId, FacesMessage facesMsg)
   {
      showFacesMessage(FacesContext.getCurrentInstance(), clientId, facesMsg);
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
   
   
   
  /* public static void executeJScript(String jsToExecute)
   {
      FacesContext facesCtx = FacesContext.getCurrentInstance();
      ExtendedRenderKitService service =  (ExtendedRenderKitService)
         Service.getRenderKitService(facesCtx, ExtendedRenderKitService.class);
      if(service != null)
      {
         service.addScript(facesCtx, jsToExecute);
      }
   }*/
   
   static private ClassLoader _getClassLoader()
   {
     return Thread.currentThread().getContextClassLoader();
   }
   
   /**
    * @author Yogesh.Manware
    * Returns the clientId for a component with componentId
    */
   public static String getClientId(String componentId)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      UIViewRoot uiViewRoot = facesContext.getViewRoot();
      UIComponent uiComponent = findComponent(uiViewRoot, componentId);
      return uiComponent.getClientId(facesContext);
   }

   /**
    * @author Yogesh.Manware
    * Finds component with the given id
    */
   private static UIComponent findComponent(UIComponent uiComponent, String componentId)
   {
      if (componentId.equals(uiComponent.getId()))
      {
         return uiComponent;
      }
      @SuppressWarnings("unchecked")
      Iterator<UIComponent> children = uiComponent.getFacetsAndChildren();
      while (children.hasNext())
      {
         UIComponent requiredComponent = findComponent(children.next(), componentId);
         if (requiredComponent != null)
         {
            return requiredComponent;
         }
      }
      return null;
   }

}
