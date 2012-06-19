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
package org.eclipse.stardust.ui.web.processportal.interaction.embedded.jsf;

import static org.eclipse.stardust.ui.web.common.util.StringUtils.isEmpty;

import java.util.Arrays;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.jsf.icefaces.IceFacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.utils.JsfBackingBeanUtils;

import com.icesoft.faces.context.effects.JavascriptContext;



/**
 * @author sauer
 * @version $Revision: $
 */
public class IppIceFacesJsfPhaseListener implements PhaseListener
{
   private static final long serialVersionUID = 1;

   private static final Logger trace = LogManager.getLogger(IppIceFacesJsfPhaseListener.class);
   
   public void beforePhase(PhaseEvent event)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      
      if ( !IceFacesUtils.isIceFaces(facesContext))
      {
         return;
      }
      
      if (null != facesContext.getViewRoot() && trace.isDebugEnabled())
      {
         trace.debug("Before: " + facesContext.getViewRoot().getViewId() + " --> "
               + event.getPhaseId());
      }
   }
   
   public void afterPhase(PhaseEvent event)
   {
      final FacesContext facesContext = FacesContext.getCurrentInstance();

      if (IceFacesUtils.isIceFaces(facesContext))
      {
         if ( !(facesContext.getExternalContext().getRequest() instanceof HttpServletRequest))
         {
            trace.warn("Only HTTP servlet environments are currently supported.");
            return;
         }

         final Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
         
         String panelCommand = (String) sessionMap.get(IceFacesConstants.KEY_ICEFACES_PANEL_COMMAND);
         if (isEmpty(panelCommand))
         {
            // no action required
            return;
         }

         // extract request URI to match against URL in activity definition
         HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
         String requestUri = request.getRequestURI();
         if ( !isEmpty(requestUri) && requestUri.startsWith(request.getContextPath()))
         {
            requestUri = requestUri.substring(request.getContextPath().length());
         }
         
         if ((null != facesContext) && null != facesContext.getViewRoot())
         {
            if ((PhaseId.RESTORE_VIEW == event.getPhaseId())
                  && IceFacesConstants.CMD_ICEFACES_PANEL_INITIALIZE.equals(panelCommand))
            {
               // perform in data mapping if required
               String interactionId = (String) sessionMap.get(IceFacesConstants.KEY_INTERACTION_ID);

               InteractionRegistry registry = (InteractionRegistry) FacesUtils.getBeanFromContext(
                     facesContext, InteractionRegistry.BEAN_ID);
               Interaction interaction = ((null != registry) && !isEmpty(interactionId))
                     ? registry.getInteraction(interactionId)
                     : null;
               
               if (null != interaction)
               {
                  ApplicationContext jsfContext = interaction.getDefinition();
                  if (null != jsfContext)
                  {
                     String viewId = (String) jsfContext.getAttribute("jsf:url");
                     if ( !isEmpty(viewId) && !viewId.startsWith("/"))
                     {
                        viewId = "/" + viewId;
                     }
                     
                     if (facesContext.getViewRoot().getViewId().equals(viewId) || requestUri.equals(viewId))
                     {
                        trace.info("About to perform IN data mappings for IPP activity panel view "
                              + viewId);

                        sessionMap.remove(IceFacesConstants.KEY_ICEFACES_PANEL_COMMAND);
                        sessionMap.remove(IceFacesConstants.KEY_INTERACTION_ID);

                        JsfBackingBeanUtils.performBackingBeanInDataMappings(
                              jsfContext, interaction.getInDataValues());
                     }
                  }
               }
            }
            else if ((PhaseId.INVOKE_APPLICATION == event.getPhaseId())
                  && (IceFacesConstants.CMD_ICEFACES_PANEL_COMPLETE.equals(panelCommand) || IceFacesConstants.CMD_ICEFACES_PANEL_SUSPEND_AND_SAVE.equals(panelCommand)))
            {
               // perform out data mapping if required
               
               try
               {
                  final String interactionId = (String) sessionMap.get(IceFacesConstants.KEY_INTERACTION_ID);
                  
                  InteractionRegistry registry = (InteractionRegistry) FacesUtils.getBeanFromContext(
                        facesContext, InteractionRegistry.BEAN_ID);
                  Interaction interaction = ((null != registry) && !isEmpty(interactionId))
                        ? registry.getInteraction(interactionId)
                        : null;
                  
                  if (null != interaction)
                  {
                     ApplicationContext jsfContext = interaction.getDefinition();
                     if (null != jsfContext)
                     {
                        String viewId = (String) jsfContext.getAttribute("jsf:url");
                        if ( !isEmpty(viewId) && !viewId.startsWith("/"))
                        {
                           viewId = "/" + viewId;
                        }
                        
                        if (facesContext.getViewRoot().getViewId().equals(viewId)
                              || requestUri.equals(viewId))
                        {
                           trace.info("About to perform OUT data mappings for IPP activity panel view "
                                 + viewId);
                           
                           interaction.setOutDataValues(JsfBackingBeanUtils.performBackingBeanOutDataMappings(jsfContext));
                           interaction.setStatus(Interaction.Status.Complete);
                           
                           sessionMap.remove(IceFacesConstants.KEY_ICEFACES_PANEL_COMMAND);
                           sessionMap.remove(IceFacesConstants.KEY_INTERACTION_ID);

                           // confirm completion of AI panel command
                           JavascriptContext.addJavascriptCall(facesContext,
                                 "confirmIppAiClosePanelCommand('" + panelCommand + "');");
                        }
                     }
                  }
               }
               catch (PortalException pe)
               {
                  trace.warn("", pe);
               }
            }
         }
      }
   }

   public PhaseId getPhaseId()
   {
      // TODO restrict to proper phase
      return PhaseId.ANY_PHASE;
   }

}
