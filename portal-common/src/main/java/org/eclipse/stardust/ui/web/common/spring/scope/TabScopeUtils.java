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
package org.eclipse.stardust.ui.web.common.spring.scope;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

import com.icesoft.faces.webapp.http.portlet.PortletExternalContext;
import com.icesoft.faces.webapp.http.servlet.ServletExternalContext;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class TabScopeUtils
{
   private static final Logger trace = LogManager.getLogger(TabScopeUtils.class);

   private static final String PARAM_CURRENT_MANAGER = TabScopeUtils.class.getName()
         + ".currentTabScopeManager";
   
   public static Object resolveBean(String beanName, TabScopeManager context)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ExternalContext externalContext = fc.getExternalContext();
      if (externalContext instanceof ServletExternalContext)
      {
         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
         
         // save backup of current binding to cope with nested tags?
         Object contextBackup = request.getAttribute(PARAM_CURRENT_MANAGER);
         request.setAttribute(PARAM_CURRENT_MANAGER, context);
         try
         {
            return FacesUtils.getBeanFromContext(fc, beanName);
         }
         finally
         {
            if (null != contextBackup)
            {
               request.setAttribute(PARAM_CURRENT_MANAGER, contextBackup);
            }
            else
            {
               request.removeAttribute(PARAM_CURRENT_MANAGER);
            }
         }
      }
      else if (externalContext instanceof PortletExternalContext)
      {
         // TODO portlets
         trace.warn("Portlets are not yet supported.");
      }
      
      return null;
   }

   public static void bindTabScope(TabScopeManager tabScopeManager)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ExternalContext externalContext = context.getExternalContext();
      if (externalContext instanceof ServletExternalContext)
      {
         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
         
         // TODO save backup of current binding to cope with nested tags?
         
         request.setAttribute(PARAM_CURRENT_MANAGER, tabScopeManager);
      }
      else if (externalContext instanceof PortletExternalContext)
      {
         // TODO portlets
         trace.warn("Portlets are not yet supported.");
      }
   }
   
   public static TabScopeManager getCurrentTabScope()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if (facesContext.getExternalContext() instanceof ServletExternalContext)
      {
         HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext()
               .getRequest();
         
         Object currentlyBoundManager = request.getAttribute(PARAM_CURRENT_MANAGER);
         if (currentlyBoundManager instanceof TabScopeManager)
         {
            return (TabScopeManager) currentlyBoundManager;
         }
         else
         {
            trace.warn("Invalid tab scope manager: " + currentlyBoundManager,
                  new Throwable());
         }
      }
      else if (facesContext.getExternalContext() instanceof PortletExternalContext)
      {
         // TODO portlet support
         trace.warn("Portlets are not yet supported.");
      }
      
      return null;
   }

   public static void unbindTabScope(TabScopeManager tabScopeManager)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ExternalContext externalContext = context.getExternalContext();
      if (externalContext instanceof ServletExternalContext)
      {
         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
         Object currentlyBoundManager = request.getAttribute(PARAM_CURRENT_MANAGER);
         if (currentlyBoundManager == tabScopeManager)
         {
            request.removeAttribute(PARAM_CURRENT_MANAGER);
         }
   
         // TODO restore backup of previous binding to cope with nested tags?
      }
      else if (externalContext instanceof PortletExternalContext)
      {
         // TODO portlets
         trace.warn("Portlets are not yet supported.");
      }
   }
}
