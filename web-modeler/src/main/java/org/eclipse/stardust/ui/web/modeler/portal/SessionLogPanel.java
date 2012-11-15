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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.icesoft.faces.context.effects.JavascriptContext;

import org.eclipse.stardust.ui.web.common.ResourcePaths;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;

/**
 * @author Shrikant.Gangal
 *
 */
@Component
@Scope("session")
public class SessionLogPanel extends AbstractLaunchPanel implements ResourcePaths
{
   private static final long serialVersionUID = -6011422465888235475L;

   /**
    *
    */
   public SessionLogPanel()
   {
      super("sessionLogPanel");
   }

   @Override
   public void update()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void toggle()
   {
      super.toggle();
      if (isExpanded())
      {
         activateSessionLogPanelIframe();
      }
      else
      {
         deactivateSessionLogPanelIframe();
      }
   }

   /**
    *
    */
   public void repositionPanelIframe()
   {
      if (isExpanded())
      {
         activateSessionLogPanelIframe();
      }
   }

   /**
    *
    */
   protected static void activateSessionLogPanelIframe()
   {
      String activateSessionLogPanelIframeJS = "InfinityBpm.ProcessPortal.createOrActivateContentFrame('sessionLogPanelFrame', '../bpm-modeler/launchpad/sessionLogPanel.html', {anchorId:'sessionLogPanelAnchor', width:280, height:400, maxWidth:350, maxHeight:1000, anchorYAdjustment:10, zIndex:200, noUnloadWarning: 'true'});";

      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
            activateSessionLogPanelIframeJS);
      PortalApplication.getInstance().addEventScript(activateSessionLogPanelIframeJS);
   }

   /**
    *
    */
   protected static void deactivateSessionLogPanelIframe()
   {
      String deactivateSessionLogPanelPanelIframeJS = "InfinityBpm.ProcessPortal.deactivateContentFrame('sessionLogPanelFrame');";

      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
            deactivateSessionLogPanelPanelIframeJS);
      PortalApplication.getInstance().addEventScript(
            deactivateSessionLogPanelPanelIframeJS);
   }
}