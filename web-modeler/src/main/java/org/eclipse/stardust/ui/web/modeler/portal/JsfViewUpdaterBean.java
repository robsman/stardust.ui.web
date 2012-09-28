package org.eclipse.stardust.ui.web.modeler.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;

/**
 * @author Shrikant.Gangal
 *
 */
public class JsfViewUpdaterBean
{
   /**
    * Finds and updates a view's parameter.
    */
   public void updateView()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, String> params = context.getExternalContext().getRequestParameterMap();
      String viewId = params.get("viewId");
      String viewKey = params.get("viewIdentity");
      View view = PortalApplication.getInstance().getViewById(viewId, viewKey);
      if (null != view)
      {
         view.getViewParams().putAll(View.parseParams(params.get("viewParams")));
         view.resolveLabelAndDescription();
      }
   }

   /**
    * Retrieves elements "uuid" paramter from the request, and deletes any open view for
    * this element. If the element being deleted is a model then it closes any open
    * views for its child elements too.
    *
    */
   public void closeViewsForElement()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, String> params = context.getExternalContext().getRequestParameterMap();
      String uuid = params.get("uuid");
      if (null != uuid)
      {
         List<View> openViews = PortalApplication.getInstance().getOpenViews();
         List<View> viewsToClose = new ArrayList<View>();
         for (View view : openViews)
         {
            Map<String, Object> viewParams = view.getViewParams();
            if (uuid.equals(viewParams.get("uuid"))
                  || uuid.equals(viewParams.get("modelUUID")))
            {
               viewsToClose.add(view);
            }
         }

         for (View view : viewsToClose)
         {
            PortalApplication.getInstance().closeView(view);
         }
      }
   }
}
