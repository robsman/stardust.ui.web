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

import java.util.Map;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent.PerspectiveEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

import com.icesoft.faces.context.effects.JavascriptContext;


/**
 * Class used to allow the dynamic opening and closing of modal panelPopups That means the
 * visibility status is tracked, as well as supporting methods for button clicks on the
 * page.
 * 
 * This should be used for "modal popups".
 * 
 * @author Ankita.Patel
 * @version $Revision: $
 */
public abstract class PopupUIComponentBean extends UIComponentBean
{
   private static final long serialVersionUID = 1L;

   private boolean visible = false;
   private String title;
   private boolean popupAutoCenter = true;

   protected boolean firePerspectiveEvents = true;
  
   protected boolean fromlaunchPanels;

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
	  setFromlaunchPanels(false);
      View focusView = PortalApplication.getInstance().getFocusView();
      firePerspectiveEvent(PerspectiveEventType.LAUNCH_PANELS_ACTIVATED);
      if ((null != focusView) && !PortalUiController.getInstance().broadcastVetoableViewEvent(focusView, ViewEventType.TO_BE_ACTIVATED))
      {
         // TODO trace
      }

      visible = false;
      FacesUtils.clearFacesTreeValues();

      // FOR PANAMA
      String popupScript = "parent.BridgeUtils.Dialog.close();";
      PortalApplication.getInstance().addEventScript(popupScript);

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
      Map requestParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      if ("true".equals(requestParams.get("fromlaunchPanels")))
      {
    	  setFromlaunchPanels(true);
      }
      View focusView = PortalApplication.getInstance().getFocusView();
      firePerspectiveEvent(PerspectiveEventType.LAUNCH_PANELS_DEACTIVATED);
      if ((null != focusView) && !PortalUiController.getInstance().broadcastVetoableViewEvent(focusView, ViewEventType.TO_BE_DEACTIVATED))
      {
         // TODO trace
      }
     
      addPopupCenteringScript();
      visible = true;

      // FOR PANAMA
      String popupScript = "parent.BridgeUtils.Dialog.open(" + fromlaunchPanels + ");";
      PortalApplication.getInstance().addEventScript(popupScript);

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
         PortalApplication.getInstance().addEventScript(positionPopupScript);
      }
   }

   /**
    * @param event
    */
   private void firePerspectiveEvent(PerspectiveEventType event)
   {
      if (firePerspectiveEvents)
      {         
         PortalApplication.getInstance().getPortalUiController().broadcastNonVetoablePerspectiveEvent(event);
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

   public boolean isFromlaunchPanels()
   {
      return fromlaunchPanels;
   }

   public void setFromlaunchPanels(boolean fromlaunchPanels)
   {
      this.fromlaunchPanels = fromlaunchPanels;
   }
}
