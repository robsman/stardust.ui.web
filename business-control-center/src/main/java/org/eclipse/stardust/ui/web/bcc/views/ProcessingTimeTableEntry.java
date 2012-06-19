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
package org.eclipse.stardust.ui.web.bcc.views;

import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;


/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ProcessingTimeTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   
   private String processDefinitionId;
   private String averageTimeToday;
   private String averageTimeLastWeek;
   private String averageTimeLastMonth;
   private int todatState;
   private int lastWeekState;
   private int lastMonthState;
   
   
   /**
    * @param processDefinitionId
    * @param averageTimeToday
    * @param averageTimeLastWeek
    * @param averageTimeLastMonth
    * @param todatState
    * @param lastWeekState
    * @param lastMonthState
    */
   public ProcessingTimeTableEntry(String processDefinitionId, String averageTimeToday,
         String averageTimeLastWeek, String averageTimeLastMonth, int todatState,
         int lastWeekState, int lastMonthState)
   {
      super();
      this.processDefinitionId = processDefinitionId;
      this.averageTimeToday = averageTimeToday;
      this.averageTimeLastWeek = averageTimeLastWeek;
      this.averageTimeLastMonth = averageTimeLastMonth;
      this.todatState = todatState;
      this.lastWeekState = lastWeekState;
      this.lastMonthState = lastMonthState;
   }

   public String getTodayStatusLabel()
   {
      return getStateLabel(todatState);      
   }

   public String getLastWeekStatusLabel()
   {
      return getStateLabel(lastWeekState);
   }

   public String getLastMonthStatusLabel()
   {
      return getStateLabel(lastMonthState);
   }

   private String getStateLabel(int state)
   {
      String ret = "";
      switch(state)
      {
      case 1:
         ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.critical");
         break;
      case 2:
         ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.warning");
         break;
      case 3:
         ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.normal");
         break;
      }
      return ret;
   }

   public String getProcessDefinitionId()
   {
      return processDefinitionId;
   }

   public String getAverageTimeToday()
   {
      return averageTimeToday;
   }

   public String getAverageTimeLastWeek()
   {
      return averageTimeLastWeek;
   }

   public String getAverageTimeLastMonth()
   {
      return averageTimeLastMonth;
   }

   public int getTodatState()
   {
      return todatState;
   }

   public int getLastWeekState()
   {
      return lastWeekState;
   }

   public int getLastMonthState()
   {
      return lastMonthState;
   }

   public void setProcessDefinitionId(String processDefinitionId)
   {
      this.processDefinitionId = processDefinitionId;
   }

   public void setAverageTimeToday(String averageTimeToday)
   {
      this.averageTimeToday = averageTimeToday;
   }

   public void setAverageTimeLastWeek(String averageTimeLastWeek)
   {
      this.averageTimeLastWeek = averageTimeLastWeek;
   }

   public void setAverageTimeLastMonth(String averageTimeLastMonth)
   {
      this.averageTimeLastMonth = averageTimeLastMonth;
   }

   public void setTodatState(int todatState)
   {
      this.todatState = todatState;
   }

   public void setLastWeekState(int lastWeekState)
   {
      this.lastWeekState = lastWeekState;
   }

   public void setLastMonthState(int lastMonthState)
   {
      this.lastMonthState = lastMonthState;
   }

}
