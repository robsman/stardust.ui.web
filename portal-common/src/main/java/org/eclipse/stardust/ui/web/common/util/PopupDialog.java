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
package org.eclipse.stardust.ui.web.common.util;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.app.PortalApplicationEventScript;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;

import com.icesoft.faces.context.effects.JavascriptContext;



/**
 * Class used to allow the dynamic opening and closing of panelPopups That means the
 * visibility status is tracked, as well as supporting methods for button clicks on the
 * page
 */
public abstract class PopupDialog implements Serializable
{
   private static final long serialVersionUID = 1L;
   private boolean popupAutoCenter = true;

   protected String id;
   protected boolean visible = false;
   protected String title;
   
   protected boolean fireViewEvents = true;

   public abstract void apply();
   public abstract void reset();
   
   /**
    * @param title
    */
   public PopupDialog(String title)
   {
      this.title = title;
   }

   /**
    * @deprecated Refactor to an abstract method, and make sure all concrete classes use
    *             proper DI to obtain the UI controller. For an example see
    *             {@link org.eclipse.stardust.ui.web.common.message.MessageDialog}.
    */
   @Deprecated
   protected PortalUiController getPortalUiController()
   {
      return PortalUiController.getInstance();
   }
   
   /**
    * 
    */
   public void closePopup()
   {
      // TODO remove duplicate code, see CRNT-16380
      PortalUiController portalUiController = null;
      View focusView = null;
      if(fireViewEvents)
      {
         portalUiController = getPortalUiController();

         focusView = portalUiController.getFocusView();
         if ((null != focusView) && !portalUiController.broadcastVetoableViewEvent(focusView, ViewEventType.TO_BE_ACTIVATED))
         {
            // TODO trace
         }
      }

      visible = false;

      if(fireViewEvents)
      {
         if ((null != focusView) && !portalUiController.broadcastVetoableViewEvent(focusView, ViewEventType.ACTIVATED))
         {
            // TODO trace
         }
      }
   }

   /**
    * 
    */
   public void openPopup()
   {
      PortalUiController portalUiController = null;
      View focusView = null;
      if(fireViewEvents)
      {
         portalUiController = getPortalUiController();

         // TODO remove duplicate code, see CRNT-16380
         focusView = portalUiController.getFocusView();
         if ((null != focusView) && !portalUiController.broadcastVetoableViewEvent(focusView, ViewEventType.TO_BE_DEACTIVATED))
         {
            // TODO trace
         }
      }

      addPopupCenteringScript();
      visible = true;

      if(fireViewEvents)
      {
         if ((null != focusView) && !portalUiController.broadcastVetoableViewEvent(focusView, ViewEventType.DEACTIVATED))
         {
            // TODO trace
         }
      }
   }
   
   private void addPopupCenteringScript()
   {
      if (popupAutoCenter)
      {
         String positionPopupScript = "InfinityBpm.Core.positionMessageDialog('" + getBeanId() + "');";
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), positionPopupScript);
         PortalApplicationEventScript.getInstance().addEventScript(positionPopupScript);
      }
   }

   public String getId()
   {
      if(StringUtils.isEmpty(id))
         id = "Id" + hashCode();

      return id;
   }
   
   /**
    * A duplicate method for getId.
    * Added to maintain consistency with UIComponentBean.getBeanId()
    * 
    * @return
    */
   public final String getBeanId()
   {

      return getId();
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

   public boolean isFireViewEvents()
   {
      return fireViewEvents;
   }

   public void setFireViewEvents(boolean fireViewEvents)
   {
      this.fireViewEvents = fireViewEvents;
   }
   
   public void setPopupAutoCenter(boolean popupAutoCenter)
   {
      this.popupAutoCenter = popupAutoCenter;
   }
}