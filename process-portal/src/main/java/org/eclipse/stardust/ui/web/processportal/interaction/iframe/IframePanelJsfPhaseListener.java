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
package org.eclipse.stardust.ui.web.processportal.interaction.iframe;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.spring.scope.TabScopeUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.utils.JsfBackingBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;

import com.icesoft.faces.context.effects.JavascriptContext;



/**
 * @author sauer
 * @version $Revision: $
 */
public class IframePanelJsfPhaseListener implements PhaseListener
{
   private static final long serialVersionUID = 1;

   private static final Logger trace = LogManager.getLogger(IframePanelJsfPhaseListener.class);
   
   public void beforePhase(PhaseEvent event)
   {
      FacesContext facesContext = event.getFacesContext();
      
      if ( !(facesContext.getExternalContext().getRequest() instanceof HttpServletRequest))
      {
         trace.warn("Only HTTP servlet environments are currently supported.");
         return;
      }
      
      if (isLoginOrMainPage(facesContext))
      {
         // login or main view, don't intercept
         return;
      }

      manageViewScope(facesContext, true);
   }

   public void afterPhase(PhaseEvent event)
   {
      final FacesContext facesContext = event.getFacesContext();

      if ( !(facesContext.getExternalContext().getRequest() instanceof HttpServletRequest))
      {
         trace.warn("Only HTTP servlet environments are currently supported.");
         return;
      }

      if (isLoginOrMainPage(facesContext))
      {
         // login or main view, don't intercept
         return;
      }

      try
      {
         @SuppressWarnings("unchecked")
         final Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
         
         String panelCommand = (String) sessionMap.get(IframePanelConstants.KEY_COMMAND);
         if (isEmpty(panelCommand))
         {
            // no action required
            return;
         }

         // extract request URI to match against URL in activity definition
         HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
         
         String requestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
	
         if (requestUri == null) 
         {
        	 requestUri = request.getRequestURI();
         }
         
         if ( !isEmpty(requestUri) && requestUri.startsWith(request.getContextPath()))
         {
            requestUri = requestUri.substring(request.getContextPath().length());
         }
         
         if ((null != facesContext) && null != facesContext.getViewRoot())
         {
            if ((PhaseId.RESTORE_VIEW == event.getPhaseId())
                  && IframePanelConstants.CMD_IFRAME_PANEL_INITIALIZE.equals(panelCommand))
            {
               // perform in data mapping if required
               String interactionId = (String) sessionMap.get(IframePanelConstants.KEY_INTERACTION_ID);

               InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(
                     facesContext, InteractionRegistry.BEAN_ID);
               Interaction interaction = ((null != registry) && !isEmpty(interactionId))
                     ? registry.getInteraction(interactionId)
                     : null;
               
               if (null != interaction)
               {
                  ApplicationContext jsfContext = interaction.getDefinition();
                  if (null != jsfContext)
                  {
                     String viewId = (String) sessionMap.get(IframePanelConstants.KEY_VIEW_ID);
                     if ( isEmpty(viewId))
                     {
                        viewId = (String) jsfContext.getAttribute("jsf:url");
                     }
                     if ( !isEmpty(viewId) && !viewId.startsWith("/"))
                     {
                        viewId = "/" + viewId;
                     }
                     
                     if (facesContext.getViewRoot().getViewId().equals(viewId) || requestUri.equals(viewId))
                     {
                        trace.info("About to perform IN data mappings for IPP activity panel view "
                              + viewId);
                        
                        // first time connecting against an interaction, IN mappings should be performed
                        JsfBackingBeanUtils.performBackingBeanInDataMappings(jsfContext,
                              interaction.getInDataValues());

                        sessionMap.remove(IframePanelConstants.KEY_COMMAND);
                        sessionMap.remove(IframePanelConstants.KEY_INTERACTION_ID);
                        sessionMap.remove(IframePanelConstants.KEY_VIEW_ID);
                     }
                  }
               }
            }
            else if ((PhaseId.INVOKE_APPLICATION == event.getPhaseId())
                  && (IframePanelConstants.CMD_IFRAME_PANEL_COMPLETE.equals(panelCommand) || IframePanelConstants.CMD_IFRAME_PANEL_SUSPEND_AND_SAVE.equals(panelCommand)))
            {
               // perform out data mapping if required
               
               try
               {
                  final String interactionId = (String) sessionMap.get(IframePanelConstants.KEY_INTERACTION_ID);
                  
                  InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(
                        facesContext, InteractionRegistry.BEAN_ID);
                  Interaction interaction = ((null != registry) && !isEmpty(interactionId))
                        ? registry.getInteraction(interactionId)
                        : null;
                  
                  if (null != interaction)
                  {
                     ApplicationContext jsfContext = interaction.getDefinition();
                     if (null != jsfContext)
                     {
                        String viewId = (String) sessionMap.get(IframePanelConstants.KEY_VIEW_ID);
                        if ( isEmpty(viewId))
                        {
                           viewId = (String) jsfContext.getAttribute("jsf:url");
                        }
                        if ( !isEmpty(viewId) && !viewId.startsWith("/"))
                        {
                           viewId = "/" + viewId;
                        }
                        
                        if (facesContext.getViewRoot().getViewId().equals(viewId)
                              || requestUri.equals(viewId))
                        {
                           trace.info("About to perform OUT data mappings for IPP activity panel view "
                                 + viewId);
                           
                           try
                           {
                              Map outMap = JsfBackingBeanUtils.performBackingBeanOutDataMappings(jsfContext);

                              interaction.setOutDataValues(outMap);
                              interaction.setStatus(Interaction.Status.Complete);

                              sessionMap.remove(IframePanelConstants.KEY_INTERACTION_ID);
                              sessionMap.remove(IframePanelConstants.KEY_VIEW_ID);

                              if (IframePanelUtils.isIceFaces(facesContext))
                              {
                                 // confirm completion of AI panel command
                                 JavascriptContext.addJavascriptCall(facesContext,
                                       "confirmIppAiClosePanelCommand('" + panelCommand
                                             + "');");
                              }
                              else
                              {
                                 trace.error("FacesContext other than ICEfaces is not supported...", new Throwable());
                              }
                           }
                           catch (ValidatorException ve)
                           {
                              trace.info(
                                    "ValidationException from IPP activity panel view "
                                          + viewId);
                              if (trace.isDebugEnabled())
                              {
                                 trace.debug("Trace: ",ve);
                              }
                           }
                           finally
                           {
                              // Command is considered executed and thereby removed.
                              sessionMap.remove(IframePanelConstants.KEY_COMMAND);
                           }
                        }
                     }
                  }
               }
               catch (PortalException pe)
               {
                  trace.warn("", pe);
               }
            }
            else if ((PhaseId.RENDER_RESPONSE == event.getPhaseId())
                  && (IframePanelConstants.CMD_IFRAME_PANEL_COMPLETE.equals(panelCommand) 
                        || IframePanelConstants.CMD_IFRAME_PANEL_SUSPEND_AND_SAVE.equals(panelCommand)))
            {
               try
               {
                  final String interactionId = (String) sessionMap.get(IframePanelConstants.KEY_INTERACTION_ID);
                  
                  InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(
                        facesContext, InteractionRegistry.BEAN_ID);
                  Interaction interaction = ((null != registry) && !isEmpty(interactionId))
                        ? registry.getInteraction(interactionId)
                        : null;
                  
                  if (null != interaction)
                  {
                     ApplicationContext jsfContext = interaction.getDefinition();
                     if (null != jsfContext)
                     {
                        String viewId = (String) sessionMap.get(IframePanelConstants.KEY_VIEW_ID);
                        if ( isEmpty(viewId))
                        {
                           viewId = (String) jsfContext.getAttribute("jsf:url");
                        }
                        if ( !isEmpty(viewId) && !viewId.startsWith("/"))
                        {
                           viewId = "/" + viewId;
                        }
                        
                        if (facesContext.getViewRoot().getViewId().equals(viewId)
                              || requestUri.equals(viewId))
                        {
                           // Cleanup Session
                           trace.info("About to perform Cleaning Session Map for IPP activity panel view  " + viewId);
                           sessionMap.remove(IframePanelConstants.KEY_COMMAND);
                           sessionMap.remove(IframePanelConstants.KEY_INTERACTION_ID);
                           sessionMap.remove(IframePanelConstants.KEY_VIEW_ID);
                        }
                     }
                  }
               }
               catch (Exception e)
               {
                  trace.warn("", e);
               }
            }
         }
      }
      finally
      {
         manageViewScope(facesContext, false);
      }
   }

   public PhaseId getPhaseId()
   {
      return PhaseId.ANY_PHASE;
   }

   private void manageViewScope(FacesContext facesContext, boolean doBind)
   {
      if (null != facesContext.getExternalContext().getSession(false))
      {
         // see if this session aleady contains a properly initialized UI controller
         PortalUiController portalUiController = (PortalUiController) facesContext.getExternalContext()
               .getSessionMap()
               .get(PortalUiController.BEAN_NAME);
         if (null != portalUiController)
         {
            // if there is an active view, use it for managing view scope
            View activeView = portalUiController.getActiveView();
            if (null != activeView)
            {
               if (doBind)
               {
                  TabScopeUtils.bindTabScope(activeView);
               }
               else
               {
                  TabScopeUtils.unbindTabScope(activeView);
               }
            }
         }
      }
   }
   
   private boolean isLoginOrMainPage(final FacesContext facesContext)
   {
      UIViewRoot viewRoot = facesContext.getViewRoot();

      return (null != viewRoot)
            && ("/plugins/common/login.xhtml".equals(viewRoot.getViewId()) || "/plugins/common/main.xhtml".equals(viewRoot.getViewId()));
   }

}
