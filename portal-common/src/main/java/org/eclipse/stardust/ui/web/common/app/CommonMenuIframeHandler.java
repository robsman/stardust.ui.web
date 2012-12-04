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
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.ui.web.common.spi.menu.CommonMenuItem;
import org.eclipse.stardust.ui.web.common.spi.menu.CommonMenuProvider;

import com.icesoft.faces.context.effects.JavascriptContext;

/**
 * @author Yogesh.Manware
 * 
 */
public class CommonMenuIframeHandler implements Serializable
{
   private static final long serialVersionUID = -2343197564129624329L;
   private static final String COMMOM_MENU_IFRAME_ID = "'COMMON_MENU'";
   private CommonMenuProvider commonMenuProvider;
   private boolean commonMenuPopupOpened = false;

   public CommonMenuIframeHandler(CommonMenuProvider commonMenuProvider)
   {
      this.commonMenuProvider = commonMenuProvider;
   }

   public void toggleCommonMenuIframePopup()
   {
      if (isCommonMenuPopupOpened())
      {
         closeCommonMenuIframePopup();
      }
      else
      {
         PortalApplication.getInstance().cleanEventScripts();
         openCommonMenuIframePopup();
      }
   }

   /**
    * Close Common menu popup
    */
   public void closeCommonMenuIframePopup()
   {
      if (isCommonMenuPopupOpened())
      {
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + COMMOM_MENU_IFRAME_ID + ");";

         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

         commonMenuPopupOpened = false;
      }
   }

   /**
    * Open Common menu popup
    */
   public void openCommonMenuIframePopup()
   {
      if (!isCommonMenuPopupOpened())
      {
         String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
               + "/plugins/common/views/toolbar/commonMenuIframePopup.iface?random=" + System.currentTimeMillis() + "'";
         String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + COMMOM_MENU_IFRAME_ID + ", " + url
               + "," + getCommonMenuIframePopupArgs() + ");";

         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
         commonMenuPopupOpened = true;
      }
   }

   /**
    * @return
    */
   public String getCommonMenuIframePopupArgs()
   {
      String advanceArgs = "{anchorId:'ippCommonMenuAnchor', width:100, height:100, maxWidth:500, maxHeight:550, "
            + "openOnRight:true, anchorXAdjustment:30, anchorYAdjustment:55, zIndex:200, border:'1px solid black', noUnloadWarning: 'true'}";
      return advanceArgs;
   }
   
   public List<CommonMenuItem> getCommonMenuLinks()
   {
      return commonMenuProvider.getMenuItems();
   }

   public void commonMenuCloseListener(ValueChangeEvent event)
   {
      commonMenuPopupOpened = false;
   }

   public boolean isCommonMenuPopupOpened()
   {
      return commonMenuPopupOpened;
   }

   public String getCommonMenuIframeId()
   {
      return COMMOM_MENU_IFRAME_ID;
   }

}
