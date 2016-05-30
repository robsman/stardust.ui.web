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

import javax.annotation.Resource;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalApplicationEventScript;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEventHandler;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.rules_manager.store.RulesManagementStrategy;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;

/**
 * @author Marc.Gille
 * 
 */
@Component
@Scope("session")
public class RulesManagerLaunchPanel extends AbstractLaunchPanel
      implements PerspectiveEventHandler
{
   private static final Logger trace = LogManager.getLogger(RulesManagerLaunchPanel.class);
   
   @Resource(name="rulesManagementStrategy")
   private RulesManagementStrategy rulesManagementStrategy;

   /**
	 *
	 */
   public RulesManagerLaunchPanel()
   {
      super("rulesManagerLaunchPanel");
      SessionSharedObjectsMap sessionMap = SessionSharedObjectsMap.getCurrent();
      sessionMap.setObject("SESSION_CONTEXT", SessionContext.findSessionContext());
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel#toggle()
    */
   @Override
   public void toggle()
   {
      super.toggle();
      if (isExpanded())
      {
         activateIframe();
      }
      else
      {
         deActivateIframe();
      }
   }

   /**
    *
    */
   private static void deActivateIframe()
   {
      String deActivateIframeJS = "InfinityBpm.ProcessPortal.deactivateContentFrame('ruleSetOutlineFrame');";
      PortalApplicationEventScript.getInstance().addEventScript(deActivateIframeJS);
   }

   /**
	 *
	 */
   private static void activateIframe()
   {
       String outlinePath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() 
       		+ "/plugins/rules-manager/launchpad/outline.html";
      String deActivateIframeJS = "InfinityBpm.ProcessPortal.createOrActivateContentFrame('ruleSetOutlineFrame', '" + outlinePath + "', {anchorId:'portalLaunchPanels:rulesOutlineAnchor', autoResize: true, zIndex:800, noUnloadWarning: 'true', frmAttrs: {repotitionOnScroll: false}});";
      PortalApplicationEventScript.getInstance().addEventScript(deActivateIframeJS);
   }

   @Override
   public void update()
   {
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
         rulesManagementStrategy.initialize(event.getParams());

         // Panel should be expanded by default
         // Set it to expanded and activate outline IFRAME
         setExpanded(true);
         activateIframe();
      case LAUNCH_PANELS_ACTIVATED:
         // Creates "process-models" folder if it doesn't exist already.
         DocumentMgmtUtility.createFolderIfNotExists("/process-models");

         Boolean launchPanelActivated = null;
         try
         {
            // If web modeler is set as default perspective ,on first login activation
            // PortalApplication loading is not complete
            launchPanelActivated = PortalApplication.getInstance().isLaunchPanelsActivated();
         }
         catch (BeanCreationException e)
         {
            trace.warn("PortalApplication instance not found"+e.getLocalizedMessage());
         }
         if (isExpanded() && (launchPanelActivated == null || launchPanelActivated))
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

   public void setRulesManagementStrategy(RulesManagementStrategy rulesManagementStrategy)
   {
      this.rulesManagementStrategy = rulesManagementStrategy;
   }
}
