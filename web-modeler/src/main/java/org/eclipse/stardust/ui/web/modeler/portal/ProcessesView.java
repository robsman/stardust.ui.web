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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalApplicationEventScript;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEventHandler;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;

import org.springframework.beans.factory.BeanCreationException;
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

   private static final Logger trace = LogManager.getLogger(ProcessesView.class);

   private boolean initialized = false;
   /**
    *
    */
   @Resource
   SessionLogPanel sessionLogPanel;
   private String profile;

   /**
    *
    */
   public ProcessesView() {
      super("processesView");
      SessionSharedObjectsMap sessionMap = SessionSharedObjectsMap
            .getCurrent();
      sessionMap.setObject("SESSION_CONTEXT",
            SessionContext.findSessionContext());

      profile = ModelingConfigurationPanel.getProfile();

      // My processes panel should be expanded by default
      // Set it to expanded and activate outline IFRAME
      setExpanded(true);
      activateIframe();
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
      PortalApplicationEventScript.getInstance().addEventScript(deActivateIframeJS);
   }

    /**
     *
     */
    private static void activateIframe() {
        String outlinePath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
           + "/plugins/bpm-modeler/launchpad/outline.html";
        String activateIframeJS = "InfinityBpm.ProcessPortal.createOrActivateContentFrame('modelOutlineFrame', '" + outlinePath + "', {anchorId:'modelerLaunchPanels:outlineAnchor', autoResize: true, heightAdjustment: -93, zIndex:800, noUnloadWarning: 'true', frmAttrs: {repotitionOnScroll: false}});";
        JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
                activateIframeJS);
        PortalApplicationEventScript.getInstance().addEventScript(activateIframeJS);

      //resize iframe
      PortalApplicationEventScript.getInstance().addEventScript(
            "InfinityBpm.ProcessPortal.resizeIFrames();");


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
      boolean toggled = false;
      switch (event.getType())
      {
      case ACTIVATED:
         if(!initialized){
            changeMouseCursorStyle("progress");
            toggled = true;
            initialized = true;
         }
      case LAUNCH_PANELS_ACTIVATED:
         if(!toggled){
            changeMouseCursorStyle("default");
         }
         //Create "process-models" folder if it doesn't exist already.
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

         if (null != sessionLogPanel && sessionLogPanel.isExpanded())
         {
            SessionLogPanel.activateSessionLogPanelIframe();
         }
         break;
      case DEACTIVATED:
      case LAUNCH_PANELS_DEACTIVATED:
         deActivateIframe();
         if (null != sessionLogPanel && sessionLogPanel.isExpanded())
         {
            SessionLogPanel.deactivateSessionLogPanelIframe();
         }
         FacesUtils.refreshPage();
         break;
      }
   }


   /**
    * @param style
    */
   private void changeMouseCursorStyle(String style)
   {
      // InfinityBpm.Core is no longer available in Panama
      // changeMouseCursorStyle() is moved to CommonUtils
      // TODO - adjust code of CommonUtils and use accordingly
      /*
       * PortalApplicationEventScript.getInstance().addEventScript(
       * "InfinityBpm.Core.changeMouseCursorStyle(\"" + style + "\");");
       */
   }

   public String getProfile()
   {
      return profile;
   }

   public void setProfile(String profile)
   {
      this.profile = profile;
   }
}