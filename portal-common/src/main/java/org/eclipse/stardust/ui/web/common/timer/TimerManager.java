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
package org.eclipse.stardust.ui.web.common.timer;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.ui.web.common.app.PortalApplicationEventScript;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

import com.icesoft.faces.context.effects.JavascriptContext;


/**
 * Supports multiple Timers
 *
 * @author Subodh.Godbole
 *
 */
public class TimerManager
{
   private static final Logger trace = LogManager.getLogger(TimerManager.class);

   private Map<String, TimerEventHandler> handlers = new HashMap<String, TimerEventHandler>();

   /**
    * @return
    */
   public static TimerManager getInstance()
   {
      return (TimerManager)FacesUtils.getBeanFromContext("timerManager");
   }

   /**
    * @param id
    * @param interval
    * @param handler
    */
   public void startTimer(String id, long interval, TimerEventHandler handler)
   {
      handlers.put(id, handler);
      String timerScript = "parent.CommonUtils.startTimer('" + id + "', " + interval + ");";
      PortalApplicationEventScript.getInstance().addEventScript(timerScript);

      if (trace.isDebugEnabled())
      {
         trace.debug("Timer Started with interval = " + interval);
      }
   }

   /**
    * @param id
    */
   public void stopTimer(String id)
   {
      handlers.remove(id);
      String timerScript = "parent.CommonUtils.stopTimer('" + id + "');";
      PortalApplicationEventScript.getInstance().addEventScript(timerScript);

      if (trace.isDebugEnabled())
      {
         trace.debug("Timer Stopped");
      }
   }

   /**
    * @param event
    */
   public void processTimerEvent(ValueChangeEvent event)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Processing Timer Event");
      }

      try
      {
         String newValue = (String)event.getNewValue();
         String timerId = newValue.split(":")[0];
         if (handlers.containsKey(timerId))
         {
            handlers.get(timerId).handleEvent();
         }
      }
      catch (Exception e)
      {
         trace.error("Error in Processing Timer Event", e);
      }
   }
}
