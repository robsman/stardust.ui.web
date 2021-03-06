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
package org.eclipse.stardust.ui.web.common;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.icesoft.faces.facelets.D2DFaceletViewHandler;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.app.InternalErrorHandler;
import org.eclipse.stardust.ui.web.jsf.icefaces.IceFacesUtils;

/**
 * @author Subodh.Godbole
 * @author Yogesh.Manware
 * 
 */
public class PortalD2DFaceletViewHandler extends D2DFaceletViewHandler
{
   private static final Logger logger = LogManager.getLogger(PortalD2DFaceletViewHandler.class);

   /**
    * 
    */
   public PortalD2DFaceletViewHandler()
   {
      super();
   }

   /**
    * @param delegate
    */
   public PortalD2DFaceletViewHandler(ViewHandler delegate)
   {
      super(delegate);
   }

   @Override
   public String calculateRenderKitId(FacesContext context)
   {
      String kitId;

      if (IceFacesUtils.isIceFaces(context))
      {
         kitId = super.calculateRenderKitId(context);
      }
      else
      {
         // ID as per http://myfaces.apache.org/trinidad/installation.html
         kitId = "org.apache.myfaces.trinidad.core";
      }

      return kitId;
   }

   @Override
   public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException
   {
      try
      {
         super.renderView(context, viewToRender);
      }
      catch (Exception e)
      {
         if (viewToRender.getViewId().contains("/plugins/common/internalServerError.xhtml"))
         {
            logger.error(
                  "Redirected back to ourselves, there must be a problem with the internalServerError.xhtml page", e);
            return;
         }

         HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
         HttpServletResponse res = (HttpServletResponse) context.getExternalContext().getResponse();
         HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();

         // Check session
         if (session != null)
         {
            logger.error("Internal Server Error has occurred. Please contact your Administrator", e);

            InternalErrorHandler errorHandler = InternalErrorHandler.getInstance();
            errorHandler.setException(e);

            if (viewToRender.getViewId().contains("/plugins/common/portalSingleViewLaunchPanelsOnly.xhtml"))
            {
               errorHandler.setDisplayLoginUrl(true);
            }
            else
            {
               errorHandler.setDisplayLoginUrl(false);
            }

            res.sendRedirect(res.encodeRedirectURL(req.getContextPath() + "/plugins/common/internalServerError.iface"));
         }
         else
         {
            logger.error("Session Expired. Redirecting to Login Screen", e);
            res.sendRedirect(res.encodeRedirectURL(req.getContextPath() + "/plugins/common/invalidSession.iface"));
         }
      }
   }
}
