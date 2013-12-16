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
   public static final String VIEW_ICON_PARAM_KEY = "viewIcon";
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
         Map<String, Object> viewParams = View.parseParams(params.get("viewParams"));
         updateViewImage(view, viewParams);
         view.getViewParams().putAll(viewParams);
         view.resolveLabelAndDescription();
         PortalApplication.getInstance().updateViewTitle(view);
         PortalApplication.getInstance().updateViewIconClass(view);
      }
   }

   /**
    * Updates a view tab's icon.
    */
   public void updateViewIconForElement()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, String> params = context.getExternalContext().getRequestParameterMap();
      String uuid = params.get("uuid");
      String iconURI = params.get("iconURI");
      if (null != uuid)
      {
         List<View> openViews = PortalApplication.getInstance().getOpenViews();
         for (View view : openViews)
         {
            Map<String, Object> viewParams = view.getViewParams();
            if (uuid.equals(viewParams.get("uuid"))
                  || uuid.equals(viewParams.get("modelUUID")))
            {
               view.setIcon(iconURI);
               PortalApplication.getInstance().updateViewIconClass(view);
            }
         }
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
            if (null != viewParams
                  &&(uuid.equals(viewParams.get("uuid"))
                        || uuid.equals(viewParams.get("modelUUID"))
                        || uuid.equals(viewParams.get("parentUUID"))))
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

   /**
    * @param view
    * @param viewParams
    */
   private void updateViewImage(View view, Map<String, Object> viewParams)
   {
      if (null != viewParams && null != viewParams.get(VIEW_ICON_PARAM_KEY))
      {
         view.setIcon((String) viewParams.get(VIEW_ICON_PARAM_KEY));
         PortalApplication.getInstance().updateViewIconClass(view);
         viewParams.remove(VIEW_ICON_PARAM_KEY);
      }
   }
}
