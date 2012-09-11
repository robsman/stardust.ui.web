package org.eclipse.stardust.ui.web.modeler.portal;

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
}
