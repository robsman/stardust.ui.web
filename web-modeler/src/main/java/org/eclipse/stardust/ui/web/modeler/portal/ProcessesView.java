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

import javax.annotation.Resource;
import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEventHandler;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.icesoft.faces.context.effects.JavascriptContext;

/**
 * @author Shrikant.Gangal
 *
 */
@Component
@Scope("session")
public class ProcessesView extends AbstractLaunchPanel implements
		PerspectiveEventHandler {

   /**
    *
    */
   @Resource
   SessionLogPanel sessionLogPanel;

	/**
	 *
	 */
	public ProcessesView() {
		super("processesView");
		SessionSharedObjectsMap sessionMap = SessionSharedObjectsMap
				.getCurrent();
		sessionMap.setObject("SESSION_CONTEXT",
				SessionContext.findSessionContext());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel#toggle()
	 */
	@Override
	public void toggle() {
		super.toggle();
		if (isExpanded()) {
			activateIframe();
		} else {
			deActivateIframe();
		}
		sessionLogPanel.repositionPanelIframe();
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
	private static void activateIframe() {
		String deActivateIframeJS = "InfinityBpm.ProcessPortal.createOrActivateContentFrame('modelOutlineFrame', '../bpm-modeler/launchpad/outline.xhtml', {anchorId:'outlineAnchor', width:280, height:570, maxWidth:350, maxHeight:1000, anchorYAdjustment:10, zIndex:200, noUnloadWarning: 'true'});";
		JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
				deActivateIframeJS);
		PortalApplication.getInstance().addEventScript(deActivateIframeJS);
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
         //Create "process-models" folder if it doesn't exist already.
         DocumentMgmtUtility.createFolderIfNotExists("/process-models");

         if (isExpanded() && PortalApplication.getInstance().isLaunchPanelsActivated())
         {
            activateIframe();
         }
         break;
      case DEACTIVATED:
      case LAUNCH_PANELS_DEACTIVATED:
         deActivateIframe();
         FacesUtils.refreshPage();
         break;
      }
   }
}
