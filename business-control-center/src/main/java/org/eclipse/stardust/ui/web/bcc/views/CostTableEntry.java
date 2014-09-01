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

import java.util.Map;

import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;


/**
 * @author Giridhara.G
 * @version
 */
public class CostTableEntry extends DefaultRowModel
{
   private final static String CUSTOM_COL_STATUS_SUFFIX = "Status";
   
   private String processDefinition;

   private String todayCosts;

   private String lastWeekCosts;

   private String lastMonthCosts;

   private int todayState;

   private int lastWeekState;

   private int lastMonthState;
   
   private Map<String, Object> customColumns = CollectionUtils.newHashMap();

   /**
    * @param processDefinition
    * @param todayCosts
    * @param lastWeekCosts
    * @param lastMonthCosts
    * @param todayState
    * @param lastWeekState
    * @param lastMonthState
    */
   public CostTableEntry(String processDefinition, String todayCosts,
         String lastWeekCosts, String lastMonthCosts, int todayState, int lastWeekState,
         int lastMonthState, String currencyCode, Map<String, Object> customColumns)
   {
      super();
      this.processDefinition = processDefinition;
      this.todayCosts = todayCosts +" "+ currencyCode;
      this.lastWeekCosts = lastWeekCosts +" "+ currencyCode;
      this.lastMonthCosts = lastMonthCosts +" "+ currencyCode;
      this.todayState = todayState;
      this.lastWeekState = lastWeekState;
      this.lastMonthState = lastMonthState;
      if(!CollectionUtils.isEmpty(customColumns))
      {
         this.customColumns = CollectionUtils.newHashMap();
         for(Map.Entry<String, Object> cols : customColumns.entrySet())
         {
            String key = cols.getKey();
            Object val = null;
            if(key.contains(CUSTOM_COL_STATUS_SUFFIX))
            {
               val = cols.getValue();
            }
            else
            {
               val = cols.getValue().toString() + " "+ currencyCode;   
            }
            this.customColumns.put(key, val);
         }         
      }
      else
      {
         this.customColumns = CollectionUtils.newHashMap();
      }
   }

   public String getTodayStatusLabel()
   {
      return getStateLabel(todayState);
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

   public String getProcessDefinition()
   {
      return processDefinition;
   }

   public void setProcessDefinition(String processDefinition)
   {
      this.processDefinition = processDefinition;
   }

   public String getTodayCosts()
   {
      return todayCosts;
   }

   public void setTodayCosts(String todayCosts)
   {
      this.todayCosts = todayCosts;
   }

   public String getLastWeekCosts()
   {
      return lastWeekCosts;
   }

   public void setLastWeekCosts(String lastWeekCosts)
   {
      this.lastWeekCosts = lastWeekCosts;
   }

   public String getLastMonthCosts()
   {
      return lastMonthCosts;
   }

   public void setLastMonthCosts(String lastMonthCosts)
   {
      this.lastMonthCosts = lastMonthCosts;
   }

   public int getTodayState()
   {
      return todayState;
   }

   public void setTodayState(int todayState)
   {
      this.todayState = todayState;
   }

   public int getLastWeekState()
   {
      return lastWeekState;
   }

   public void setLastWeekState(int lastWeekState)
   {
      this.lastWeekState = lastWeekState;
   }

   public int getLastMonthState()
   {
      return lastMonthState;
   }

   public void setLastMonthState(int lastMonthState)
   {
      this.lastMonthState = lastMonthState;
   }

   public Map<String, Object> getCustomColumns()
   {
      return customColumns;
   }

   public void setCustomColumns(Map<String, Object> customColumns)
   {
      this.customColumns = customColumns;
   }
   
}
