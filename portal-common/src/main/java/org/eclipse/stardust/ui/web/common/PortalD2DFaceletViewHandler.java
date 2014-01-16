package org.eclipse.stardust.ui.web.common;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.icesoft.faces.facelets.D2DFaceletViewHandler;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * @author Subodh.Godbole
 * @author Yogesh.Manware
 * 
 */
public class PortalD2DFaceletViewHandler extends D2DFaceletViewHandler
{
   private static final Logger logger = LogManager.getLogger(PortalD2DFaceletViewHandler.class);

   private int flag = 0;

   public PortalD2DFaceletViewHandler()
   {
      super();
   }

   @Override
   public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException
   {
      try
      {
         if (flag == 3 || flag == 6)
         {
            throw new NullPointerException();
         }
         else
         {
            super.renderView(context, viewToRender);
            flag++;
            if (flag > 7)
            {
               flag = 0;
            }
         }
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
            res.sendRedirect(res.encodeRedirectURL(req.getContextPath() + "/plugins/common/internalServerError.iface"));
            flag++;
         }
         else
         {
            logger.error("Session Expired. Redirecting to Login Screen", e);
            res.sendRedirect(res.encodeRedirectURL(req.getContextPath() + "/plugins/common/invalidSession.iface"));
         }
      }
   }
}
