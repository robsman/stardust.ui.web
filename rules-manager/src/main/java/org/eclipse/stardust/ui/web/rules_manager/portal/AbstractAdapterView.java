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

package org.eclipse.stardust.ui.web.rules_manager.portal;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.rules_manager.service.RulesManagementService;


/**
 *
 * @author Marc.Gille
 *
 */
public class AbstractAdapterView implements ViewEventHandler {
	private RulesManagementService rulesManagementService;
	private String viewPath;
   private String anchorId;

   /**
    *
    * @param viewPath
    * @param anchorId
    */
	public AbstractAdapterView(String viewPath, String anchorId)
   {
      super();

      this.viewPath = viewPath;
      this.anchorId = anchorId;
   }

	/**
	 *
	 * @return
	 */
   public RulesManagementService getRulesManagementService() {
		return rulesManagementService;
	}

   /**
    *
    * @param modelService
    */
	public void setRulesManagementService(RulesManagementService modelService) {
		this.rulesManagementService = modelService;
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
			break;

		case TO_BE_DEACTIVATED:
			break;

		case CLOSED:
			break;

		case LAUNCH_PANELS_ACTIVATED:
		case LAUNCH_PANELS_DEACTIVATED:
		case FULL_SCREENED:
		case RESTORED_TO_NORMAL:
      case PINNED:
		case PERSPECTIVE_CHANGED:
			PortalApplication.getInstance().addEventScript(
					"InfinityBpm.ProcessPortal.resizeContentFrame('"
					+ iframeId + "', {anchorId:'" + anchorId + "'});");
			break;
		}

	}
}
