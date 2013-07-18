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
package org.eclipse.stardust.ui.web.common.message;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplicationEventScript;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;

import com.icesoft.faces.context.effects.BlankEffect;
import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Pulsate;


/**
 * @author Subodh.Godbole
 *
 */
public class AlertManager extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   
   private static final Logger trace = LogManager.getLogger(AlertManager.class);

   private Effect alertEffect;

   private boolean newAlert;

   private UserProvider userProvider;
   
   /**
    * 
    */
   public AlertManager()
   {
      firePerspectiveEvents = false;
   }

   @Override
   public void initialize()
   {}

   /**
    * @return
    */
   public boolean isAlertsAvailable()
   {
      return getAlerts().size() > 0;
   }

   /**
    * @return
    */
   public boolean isNewAlert()
   {
      return newAlert;
   }

   /**
    * @return
    */
   public int getAlertsCount()
   {
      return getAlerts().size();
   }

   /**
    * 
    */
   public void toggleAlertsPanel()
   {
      if (isVisible())
      {
         String script = "parent.BridgeUtils.hideAlertNotifications();";
         PortalApplicationEventScript.getInstance().addEventScript(script);
      }
      
      setVisible(!isVisible());
      if (isVisible()) // If panel is visible
      {
         List<AlertEntry> alerts = AlertSystem.getAlerts(userProvider.getUser());
       
         for (AlertEntry alert : alerts)
         {
            alert.setShownToUser(true);
         }
      }
   }

   /**
    * @param event
    */
   public void processAlertAction(ActionEvent event)
   {
      try
      {
         AlertEntry alert = (AlertEntry) event.getComponent().getAttributes().get(
               "alertItem");
         if (alert.getAlertHandler() != null)
         {
            boolean success = alert.getAlertHandler().handleAlert(alert);
            if (success)
            {
               AlertSystem.removeAlert(alert, userProvider.getUser());

               toggleAlertsPanel();
            }
         }
         else
         {
            trace.debug("AlertHandler Not Defined");
         }
      }
      catch (Exception e)
      {
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString("common.unknownError"),
               e);
      }
   }

   /**
    * @return
    */
   public List<AlertEntry> getAlerts()
   {
      List<AlertEntry> newAlerts = new ArrayList<AlertEntry>();
      try
      {
         newAlerts = AlertSystem.getAlerts(userProvider.getUser());

         boolean newAlert = false;
         for (AlertEntry alert : newAlerts)
         {
            if (!alert.isShownToUser())
            {
               newAlert = true;
               break;
            }
         }
         setNewAlert(newAlert);
      }
      catch (Exception e)
      {
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString("common.unknownError"),
               e);
      }
      return newAlerts;
   }

   /**
    * @return
    */
   private void setNewAlert(boolean newAlert)
   {
      this.newAlert = newAlert;
      setAlertEffect(newAlert);
   }

   /**
    * 
    */
   private void setAlertEffect(boolean isNew)
   {
      if (isNew)
      {
         alertEffect = new Pulsate();
      }
      else
      {
         alertEffect = new BlankEffect();
      }
   }

   public Effect getAlertEffect()
   {
      return alertEffect;
   }

   /**
   * @param userProvider the userProvider to set
   */
   public void setUserProvider(UserProvider userProvider)
   {
      this.userProvider = userProvider;
   }
}
