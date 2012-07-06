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

import org.eclipse.stardust.ui.web.common.ResourcePaths;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEventHandler;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.icesoft.faces.context.effects.JavascriptContext;

@Component
@Scope("session")
public class ProcessesView extends AbstractLaunchPanel implements
		ResourcePaths, PerspectiveEventHandler {
	private boolean sessionLogPanelExpanded = true;
	
	public ProcessesView() {
		super("processesView");
		SessionSharedObjectsMap sessionMap = SessionSharedObjectsMap
				.getCurrent();
		sessionMap.setObject("SESSION_CONTEXT",
				SessionContext.findSessionContext());
	}
	
	@Override
	public void toggle() {
		super.toggle();

		if (isExpanded()) {
			activateIframe();
		} else {
			deActivateIframe();
		}
	}

	public void toggleSessionLogPanel() {
        setSessionLogPanelExpanded(!this.sessionLogPanelExpanded);		
		if (isSessionLogPanelExpanded()) {
			activateSessionLogPanelIframe();
		} else {
			deactivateSessionLogPanelIframe();
		}
	}

	public boolean isSessionLogPanelExpanded() {
		return sessionLogPanelExpanded;
	}

	public void setSessionLogPanelExpanded(boolean sessionLogPanelExpanded) {
		this.sessionLogPanelExpanded = sessionLogPanelExpanded;
	}


	/**
    * 
    */
	private static void deActivateIframe() {
		String deActivateIframeJS = "InfinityBpm.ProcessPortal.deactivateContentFrame('modelOutlineFrame');";
		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
				deActivateIframeJS);
		PortalApplication.getInstance().addEventScript(deActivateIframeJS);
	}

	/**
	    * 
	    */
		private static void deactivateSessionLogPanelIframe() {
			String deactivateSessionLogPanelPanelIframeJS = "InfinityBpm.ProcessPortal.deactivateContentFrame('sessionLogPanelFrame');";

			JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
					deactivateSessionLogPanelPanelIframeJS);
			PortalApplication.getInstance().addEventScript(
					deactivateSessionLogPanelPanelIframeJS);
		}

		/**
    * 
    */
	private static void activateIframe() {
		String deActivateIframeJS = "InfinityBpm.ProcessPortal.createOrActivateContentFrame('modelOutlineFrame', '../bpm-modeler/launchpad/outline.xhtml', {anchorId:'outlineAnchor', width:280, height:800, maxWidth:350, maxHeight:1000, anchorYAdjustment:10, zIndex:200, noUnloadWarning: 'true'});";
		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
				deActivateIframeJS);
		PortalApplication.getInstance().addEventScript(deActivateIframeJS);
	}

	private static void activateSessionLogPanelIframe() {
		String activateSessionLogPanelIframeJS = "InfinityBpm.ProcessPortal.createOrActivateContentFrame('sessionLogPanelFrame', '../bpm-modeler/launchpad/sessionLogPanel.xhtml', {anchorId:'sessionLogPanelAnchor', width:280, height:400, maxWidth:350, maxHeight:1000, anchorYAdjustment:10, zIndex:200, noUnloadWarning: 'true'});";

		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
				activateSessionLogPanelIframeJS);
		PortalApplication.getInstance().addEventScript(
				activateSessionLogPanelIframeJS);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
	}

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.event.PerspectiveEventHandler#handleEvent(org
    * .eclipse.stardust.ui.web.common.event.PerspectiveEvent)
    */
   public void handleEvent(PerspectiveEvent event)
   {
      switch (event.getType())
      {
      case ACTIVATED:
      case LAUNCH_PANELS_ACTIVATED:
         if (isExpanded() && PortalApplication.getInstance().isLaunchPanelsActivated())
         {
            activateIframe();
         }
         if (isSessionLogPanelExpanded() && PortalApplication.getInstance().isLaunchPanelsActivated())
         {
            activateSessionLogPanelIframe();
         }
         break;
      case DEACTIVATED:
      case LAUNCH_PANELS_DEACTIVATED:
         deActivateIframe();
         deactivateSessionLogPanelIframe();
         FacesUtils.refreshPage();
         break;
      }
   }
}
