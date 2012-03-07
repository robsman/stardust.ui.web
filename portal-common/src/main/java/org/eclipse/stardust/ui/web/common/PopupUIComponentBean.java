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
package org.eclipse.stardust.ui.web.common;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

import com.icesoft.faces.context.effects.JavascriptContext;


/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public abstract class PopupUIComponentBean extends UIComponentBean
{
   private static final long serialVersionUID = 1L;

   private boolean visible = false;
   private String title;
   private boolean popupAutoCenter = true;
  
   /**
    * 
    */
   public PopupUIComponentBean()
   {
   }
   
   /**
    * @param name
    */
   public PopupUIComponentBean(String name)
   {
      super(name);
   }

   /**
    * Ideally these needs to be abstract.
    * Keeping it as it is for backwards compatibility.
    */
   public void apply(){}
   public void reset(){}
   
   /**
    * 
    */
   public void closePopup()
   {
      View focusView = PortalApplication.getInstance().getFocusView();
      if ((null != focusView) && !PortalUiController.getInstance().broadcastVetoableViewEvent(focusView, ViewEventType.TO_BE_ACTIVATED))
      {
         // TODO trace
      }

      visible = false;
      FacesUtils.clearFacesTreeValues();

      if ((null != focusView) && !PortalUiController.getInstance().broadcastVetoableViewEvent(focusView, ViewEventType.ACTIVATED))
      {
         // TODO trace
      }
   }

   /**
    * 
    */
   public void openPopup()
   {
      View focusView = PortalApplication.getInstance().getFocusView();
      if ((null != focusView) && !PortalUiController.getInstance().broadcastVetoableViewEvent(focusView, ViewEventType.TO_BE_DEACTIVATED))
      {
         // TODO trace
      }
     
      addPopupCenteringScript();
      visible = true;

      if ((null != focusView) && !PortalUiController.getInstance().broadcastVetoableViewEvent(focusView, ViewEventType.DEACTIVATED))
      {
         // TODO trace
      }
   }

   /**
    * 
    */
   public void addPopupCenteringScript(boolean condition, String divId)
   {
      if (condition)
      {
         String positionPopupScript = "InfinityBpm.Core.positionMessageDialog('" + divId + "');";
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), positionPopupScript);
         PortalApplication.getInstance().addEventScript(positionPopupScript);
      }
   }

   /**
    * 
    */
   public void addPopupCenteringScript()
   {
      addPopupCenteringScript(popupAutoCenter, getBeanId());
   }

   public boolean isVisible()
   {
      return visible;
   }

   public void setVisible(boolean visible)
   {
      this.visible = visible;
   }
   
   public String getTitle()
   {
      return title;
   }
   
   public void setTitle(String title)
   {
      this.title = title;
   }
   
   public void setPopupAutoCenter(boolean popupAutoCenter)
   {
      this.popupAutoCenter = popupAutoCenter;
   }
}
