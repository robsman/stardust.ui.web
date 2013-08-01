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
package org.eclipse.stardust.ui.web.common.app;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import com.icesoft.faces.context.effects.JavascriptContext;

public class PerspectiveMenuIframeHandler implements Serializable
{

   private static final long serialVersionUID = -4826900826213359272L;
   private static final String IFRAME_ID = "'PERSPECTIVE_MENU'";

   private boolean open = false;

   public PerspectiveMenuIframeHandler()
   {
      String script = "parent.EventHub.events.subscribe('PERSPECTIVE_CHANGED', function() {InfinityBpm.ProcessPortal.closeContentFrame("
            + IFRAME_ID + ");});";
      PortalApplicationEventScript.getInstance().addEventScript(script);
   }
   
   public void togglePopup()
   {
      if (isOpen())
      {
         closeIframePopup();
      }
      else
      {
         PortalApplication.getInstance().cleanEventScripts();
         openPopup();
      }
   }

   /**
    * Close Common menu popup
    */
   public void closeIframePopup()
   {
      if (isOpen())
      {
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + IFRAME_ID + ");";

         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
         PortalApplicationEventScript.getInstance().addEventScript(script);
      }
      open = false;
   }

   /**
    * Open Common menu popup
    */
   public void openPopup()
   {
      if (!isOpen())
      {
         String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
               + "/plugins/common/perspectiveMenuIframePopup.iface?random=" + System.currentTimeMillis() + "'";
         String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + IFRAME_ID + ", " + url + ","
               + getPopupArgs() + ");";

         PortalApplication.getInstance().addEventScript(script);
         open = true;
      }
   }

   /**
    * @return
    */
   public String getPopupArgs()
   {
      // For Panama, appended the parentIframe for Anchor name i.e parentIframe:AnchorName
      String advanceArgs = "{anchorId:'modelerLaunchPanels:ippPerspectiveMenuAnchor', width:100, height:110,"
            + "openOnRight:true, anchorXAdjustment:-3, anchorYAdjustment:27, zIndex:1000, border:'none', noUnloadWarning: 'true'}";
      return advanceArgs;
   }

   public boolean isOpen()
   {
      return open;
   }

   public String getIframeId()
   {
      return IFRAME_ID;
   }

}
