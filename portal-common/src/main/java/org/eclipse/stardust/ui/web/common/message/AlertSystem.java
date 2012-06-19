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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.common.util.SessionRendererHelper;



/**
 * Not Designed for Multi-Server Env
 * @author Subodh.Godbole
 *
 */
public class AlertSystem implements Serializable
{
   private static final long serialVersionUID = 1L;

   // HashTable and Vector is needed for Synchronization
   private static Map<String, Vector<AlertEntry>> alerts = new Hashtable<String, Vector<AlertEntry>>();

   /**
    * @param appAlert
    * @param targetUsers
    */
   public static void addAlert(AlertEntry appAlert, List<User> targetUsers)
   {
      AlertEntry clone;
      if(appAlert != null && targetUsers != null && targetUsers.size() > 0)
      {
         for (User targetUser : targetUsers)
         {
            Vector<AlertEntry> userAlerts = alerts.get(targetUser.getUID());
            if(userAlerts == null)
            {
               alerts.put(targetUser.getUID(), userAlerts = new Vector<AlertEntry>());
            }

            // Get the clone so that each user will have separate copy of the alert 
            clone = appAlert.getClone();
            userAlerts.add(clone);

            Collections.sort(userAlerts, new Comparator<AlertEntry>(){
               public int compare(AlertEntry arg0, AlertEntry arg1)
               {
                  return arg1.getTimeStamp().compareTo(arg0.getTimeStamp());
               }
            });
            
            SessionRendererHelper.render(SessionRendererHelper.getPortalSessionRendererId(targetUser));
         }
      }
   }
   
   /**
    * @param appAlert
    * @param targetUser
    */
   public static void addAlert(AlertEntry appAlert, User targetUser)
   {
      if(appAlert != null && targetUser != null)
      {
         List<User> targetUsers = new ArrayList<User>();
         targetUsers.add(targetUser);
         addAlert(appAlert, targetUsers);
      }
   }

   /**
    * @param user
    * @return
    */
   public static List<AlertEntry> getAlerts(User user)
   {
      if(user != null)
      {
         List<AlertEntry> userAlert = alerts.get(user.getUID());
         if(userAlert == null)
         {
            userAlert = new Vector<AlertEntry>();
         }
         return userAlert;
      }
      else
      {
         return new ArrayList<AlertEntry>();
      }
   }
   
   /**
    * @param alert
    * @param user
    * @return
    */
   public static boolean removeAlert(AlertEntry alert, User user)
   {
      if(user != null)
      {
         List<AlertEntry> userAlerts = alerts.get(user.getUID());
         if(userAlerts != null)
         {
            boolean ret = userAlerts.remove(alert);
            SessionRendererHelper.render(SessionRendererHelper.getPortalSessionRendererId(user));
            return ret;
         }
         else
         {
            return true;
         }
      }
      
      return false;
   }

   /**
    * @param user
    */
   public static void removeAllAlerts(User user)
   {
      if(user != null)
      {
         List<AlertEntry> userAlerts = alerts.get(user.getUID());
         if(userAlerts != null)
         {
            userAlerts.clear();
         }
         SessionRendererHelper.render(SessionRendererHelper.getPortalSessionRendererId(user));
      }
   }
}
