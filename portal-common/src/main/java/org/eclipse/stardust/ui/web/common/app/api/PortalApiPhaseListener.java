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
package org.eclipse.stardust.ui.web.common.app.api;

import static org.eclipse.stardust.ui.web.common.util.StringUtils.isEmpty;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Robert.Sauer
 * @author Subodh.Godbole
 *
 */
public class PortalApiPhaseListener implements PhaseListener
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(PortalApiPhaseListener.class);
   
   public void beforePhase(PhaseEvent pe)
   {
      if (PhaseId.RENDER_RESPONSE == pe.getPhaseId() && isMainPage(pe.getFacesContext()))
      {
         ExternalContext externalContext = pe.getFacesContext().getExternalContext();

         if ( !(externalContext.getRequest() instanceof HttpServletRequest))
         {
            return;
         }

         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
         String queryString = request.getQueryString();
         if (isEmpty(queryString))
         {
            // request to open views was not passed via query string, so ignore it (e.g.
            // happens when a page opens views via <f:param .. />)
            return;
         }

         if (!CollectionUtils.isEmpty((String[]) externalContext.getRequestParameterValuesMap().get("viewId"))
               || !CollectionUtils.isEmpty((String[]) externalContext.getRequestParameterValuesMap().get("message")))
         {
            // Check if portal is properly bootstrapped or not
            PortalApplication portalApp = getPortalApplication(pe.getFacesContext());
            if (null == portalApp)
            {
               trace.info("Ignoring request to open views via URI as the portal is not properly bootstrapped.");
               return;
            }

            boolean refresh = false;

            if (!CollectionUtils.isEmpty((String[]) externalContext.getRequestParameterValuesMap().get("viewId")))
            {
               refresh = processOpenViewViaViewIds(portalApp, queryString);
            }
            else if (!CollectionUtils.isEmpty((String[]) externalContext.getRequestParameterValuesMap().get("message")))
            {
               refresh = processMessage(portalApp, queryString);
            }

            if (refresh)
            {
               // send redirect to remove query params from URI
               try
               {
                  pe.getFacesContext().getApplication().getNavigationHandler()
                        .handleNavigation(pe.getFacesContext(), null, "pageRefresh");
               }
               catch (Exception e)
               {
                  //TODO: Ignore for now!
                  trace.error("Error while redirecting... " + e.getMessage());
               }
            }
         }
      }
   }

   public void afterPhase(PhaseEvent pe)
   {
      // ignore
   }

   /**
    * @param facesContext
    * @return
    */
   private PortalApplication getPortalApplication(FacesContext facesContext)
   {
      return (PortalApplication) FacesUtils.getBeanFromContext(facesContext, PortalApplication.BEAN_NAME);
   }

   /**
    * @param portalApp
    * @param queryString
    */
   private boolean processMessage(PortalApplication portalApp, String queryString)
   {
      boolean refresh = false;
      String[] queryParams = queryString.split("&");
      try
      {
         for (String queryParam : queryParams)
         {
            if (isEmpty(queryParam))
            {
               continue;
            }

            String queryParamValue;
            String jsonMessage;
            String queryParamName = URLDecoder.decode(queryParam.split("=")[0], "UTF-8");
            if ("message".equals(queryParamName))
            {
               queryParamValue = queryParam.contains("=") ? queryParam.split("=")[1] : "";
               jsonMessage = URLDecoder.decode(queryParamValue, "UTF-8");
               if (StringUtils.isNotEmpty(jsonMessage))
               {
                  portalApp.postMessage(jsonMessage);
                  refresh = true;
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.error("Failed to process query parameters.", e);
         refresh = true;
      }

      return refresh;
   }

   /**
    * @param portalApp
    * @param queryString
    */
   private boolean processOpenViewViaViewIds(PortalApplication portalApp, String queryString)
   {
      // this will allow for multiple views being opened at once
      trace.debug("Query string is " + queryString);

      String[] queryParams = queryString.split("&");
      try
      {
         ViewDefinition viewDef = null;
         StringBuilder viewParams = new StringBuilder();
         
         int nViewsOpened = 0;

         for (String queryParam : queryParams)
         {
            if (isEmpty(queryParam))
            {
               continue;
            }
            
            String queryParamName = URLDecoder.decode(queryParam.split("=")[0], "UTF-8");
            
            if ("viewId".equals(queryParamName))
            {
               // if there was any previous view, go open it now
               openViewFromUri(portalApp, viewDef, viewParams.toString());
               
               // prepare for new view
               viewDef = null;
               viewParams.setLength(0);
               
               String queryParamValue = queryParam.contains("=") ? queryParam.split("=")[1] : "";

               String perspectiveId = null;
               String viewId = URLDecoder.decode(queryParamValue, "UTF-8");
               if ( !isEmpty(viewId) && viewId.contains("::"))
               {
                  perspectiveId = viewId.substring(0, viewId.indexOf("::"));
                  viewId = viewId.substring(viewId.indexOf("::") + 2);
               }

               IPerspectiveDefinition perspectiveDef = isEmpty(perspectiveId)
                     ? portalApp.getPortalUiController().getPerspective()
                     : portalApp.getPortalUiController().getPerspective(perspectiveId);
               if (null != perspectiveDef)
               {
                  viewDef = perspectiveDef.getViewDefinition(viewId);
               }
               
               if (null != viewDef)
               {
                  ++nViewsOpened;
               }
            }
            else if (null != viewDef)
            {
               if (0 < viewParams.length())
               {
                  viewParams.append("&");
               }
               viewParams.append(queryParam);
            }
         }
         
         // if there was any view specified, go open it now
         openViewFromUri(portalApp, viewDef, viewParams.toString());
         
         if (0 < nViewsOpened)
         {
            return true;
         }
      }
      catch (UnsupportedEncodingException uee)
      {
         // well, UTF-8 is guaranteed to be there
         trace.error("Failed decoding query parameters.", uee);
      };

      return false;
   }
   
   private View openViewFromUri(PortalApplication portalApp, ViewDefinition viewDef,
         String viewParams)
   {
      if (null != viewDef)
      {
         StringBuilder viewUrl = new StringBuilder();
         viewUrl.append(viewDef.getInclude());
         if ( !isEmpty(viewParams))
         {
            viewUrl.append("?").append(viewParams);
         }

         View view = portalApp.getPortalUiController().findView(View.createURL(viewDef, null));
         if (null == view)
         {
            view = portalApp.openView(viewDef, viewUrl.toString(), null, false);
         }

         return view;
      }
      
      return null;
   }

   public PhaseId getPhaseId()
   {
      return PhaseId.INVOKE_APPLICATION;
   }

   private boolean isMainPage(final FacesContext facesContext)
   {
      UIViewRoot viewRoot = facesContext.getViewRoot();

      return (null != viewRoot)
            && ("/plugins/common/main.xhtml".equals(viewRoot.getViewId()));
   }
}
