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
package org.eclipse.stardust.ui.web.common.app;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.LaunchPanel;
import org.eclipse.stardust.ui.web.common.MenuSection;
import org.eclipse.stardust.ui.web.common.ToolbarSection;
import org.eclipse.stardust.ui.web.common.UiElement;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.common.util.UserUtils;


/**
 * Implementing Authorization Proxy Programmatically
 * 
 * TODO This may be converted to spring supported Proxy/Interceptors/Advisors etc, Need to explore spring support.
 * The requirement is not to Proxy PerspectiveDefinition all the time,
 * But only when building the list for a user in PortalUiController at session scope
 *
 * @author Subodh.Godbole
 */
public class PerspectiveAuthorizationProxy implements java.lang.reflect.InvocationHandler
{
   private IPerspectiveDefinition perspectiveDefinition;

   private List<ViewDefinition> views;
   private List<LaunchPanel> launchPanels;
   private List<ToolbarSection> toolbarSections;
   private List<MenuSection> menuSections;
   
   /**
    * @param perspectiveDefinition
    */
   public PerspectiveAuthorizationProxy(IPerspectiveDefinition perspectiveDefinition)
   {
      this.perspectiveDefinition = perspectiveDefinition;
   }

   /**
    * @param pd
    * @return
    */
   public static IPerspectiveDefinition newInstance(IPerspectiveDefinition pd)
   {
      return (IPerspectiveDefinition) Proxy.newProxyInstance(pd.getClass().getClassLoader(), pd.getClass()
            .getInterfaces(), new PerspectiveAuthorizationProxy(pd));
   }

   /* (non-Javadoc)
    * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      Object result;
      
      try
      {
         result = method.invoke(perspectiveDefinition, args);

         if("getViews".equals(method.getName()))
         {
            result = getAuthorizedViews(result);
         }
         else if("getLaunchPanels".equals(method.getName()))
         {
            result = getAuthorizedLaunchPanels(result);
         }
         else if("getToolbarSections".equals(method.getName()))
         {
            result = getAuthorizedToolbarSections(result);
         }
         else if("getMenuSections".equals(method.getName()))
         {
            result = getAuthorizedMenuSections(result);
         }
      }
      catch (InvocationTargetException e)
      {
         throw e.getTargetException();
      }
      catch (Exception e)
      {
         throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
      }

      return result;
   }

   /**
    * @param result
    * @return
    */
   @SuppressWarnings("unchecked")
   private Object getAuthorizedViews(Object result)
   {
      if (null == views)
      {
         views = new ArrayList<ViewDefinition>();
         
         if (null != result)
         {
            List<ViewDefinition> allViews = (List)result;
            for (ViewDefinition viewDefinition : allViews)
            {
               if (isAuthorized(viewDefinition))
               {
                  views.add(viewDefinition);
               }
            }
         }
      }

      return views;
   }

   /**
    * @param result
    * @return
    */
   @SuppressWarnings("unchecked")
   private Object getAuthorizedLaunchPanels(Object result)
   {
      if (null == launchPanels)
      {
         launchPanels = new ArrayList<LaunchPanel>();

         if (null != result)
         {
            List<LaunchPanel> allLps = (List<LaunchPanel>)result;
            for (LaunchPanel launchPanel : allLps)
            {
               if (isAuthorized(launchPanel))
               {
                  launchPanels.add(launchPanel);
               }
            }
         }
      }
      
      return launchPanels;
   }

   /**
    * @param result
    * @return
    */
   @SuppressWarnings("unchecked")
   private Object getAuthorizedToolbarSections(Object result)
   {
      if (null == toolbarSections)
      {
         toolbarSections = new ArrayList<ToolbarSection>();

         if (null != result)
         {
            List<ToolbarSection> allToolbars = (List<ToolbarSection>)result;
            for (ToolbarSection toolbar : allToolbars)
            {
               if (isAuthorized(toolbar))
               {
                  toolbarSections.add(toolbar);
               }
            }
         }
      }
      
      return toolbarSections;
   }

   /**
    * @param result
    * @return
    */
   @SuppressWarnings("unchecked")
   private Object getAuthorizedMenuSections(Object result)
   {
      if (null == menuSections)
      {
         menuSections = new ArrayList<MenuSection>();

         if (null != result)
         {
            List<MenuSection> allMenus = (List<MenuSection>)result;
            for (MenuSection menu : allMenus)
            {
               if (isAuthorized(menu))
               {
                  menuSections.add(menu);
               }
            }
         }
      }
      
      return menuSections;
   }

   /**
    * @param uiElement
    * @return
    */
   private boolean isAuthorized(UiElement uiElement)
   {
      return UserUtils.isAuthorized(PortalApplication.getInstance().getLoggedInUser(), uiElement.getRequiredRolesSet(),
            uiElement.getExcludeRolesSet());
   }
}
