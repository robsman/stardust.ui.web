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

package org.eclipse.stardust.ui.web.modeler.portal;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalApplicationEventScript;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;


/**
 *
 * @author Marc.Gille
 *
 */
public class AbstractAdapterView implements ViewEventHandler {
	private ModelService modelService;
	private String viewPath;
   private String anchorId;
   private String keyParam;

   /**
    * @param viewPath
    * @param anchorId
    * @param keyParam
    */
   public AbstractAdapterView(String viewPath, String anchorId, String keyParam)
   {
      super();

      this.viewPath = viewPath;
      this.anchorId = anchorId;
      this.keyParam = keyParam;
   }

	/**
	 *
	 * @return
	 */
   public ModelService getModelService() {
		return modelService;
	}

   /**
    *
    * @param modelService
    */
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	/**
	 *
	 */
	public void handleEvent(ViewEvent event) {
		String pagePath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		pagePath += viewPath;
		String iframeId = "mf_" + event.getView().getIdentityParams();

		switch (event.getType())
		{
		case TO_BE_ACTIVATED:
            try
            {
               // Using reflection. To be converted to direct call later
               ReflectionUtils.invokeMethod(PortalApplication.getInstance(), "processPanamaCall", event.getView(),
                     pagePath + event.getView().getParams(), false);
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }

            break;

		case TO_BE_DEACTIVATED:
			//PortalApplication.getInstance().addEventScript("InfinityBpm.ProcessPortal.deactivateContentFrame('" + iframeId + "');");
			// fireResizeIframeEvent();
			break;

		case CLOSED:
			//PortalApplication.getInstance().addEventScript(
			//		"InfinityBpm.ProcessPortal.closeContentFrame('" + iframeId + "');");
			break;

		case LAUNCH_PANELS_ACTIVATED:
		case LAUNCH_PANELS_DEACTIVATED:
		case FULL_SCREENED:
		case RESTORED_TO_NORMAL:
      case PINNED:
		case PERSPECTIVE_CHANGED:
		   //fireResizeIframeEvent();
			break;
		}
	}

	private void fireResizeIframeEvent()
   {
      PortalApplication.getInstance().addEventScript(
            "InfinityBpm.ProcessPortal.resizeIFrames();");
   }

   /**
    * @param style
    */
   private void changeMouseCursorStyle(String style)
   {
      PortalApplicationEventScript.getInstance().addEventScript(
            "InfinityBpm.Core.changeMouseCursorStyle(\"" + style + "\");");
   }
}