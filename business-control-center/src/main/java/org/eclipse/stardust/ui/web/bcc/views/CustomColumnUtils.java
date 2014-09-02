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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.core.query.statistics.api.CalendarUnit;
import org.eclipse.stardust.engine.core.query.statistics.api.DateRange;
import org.eclipse.stardust.engine.core.query.statistics.api.Duration;
import org.eclipse.stardust.engine.core.query.statistics.api.RelativePastDateRange;

import com.google.gson.JsonObject;

public class CustomColumnUtils
{
   public final static String CUSTOM_COL_PREFIX = "customColumns.";
   public final static String CUSTOM_COL_COST_SUFFIX = "Costs";
   public final static String CUSTOM_COL_TIME_SUFFIX = "averageTime";
   public final static String CUSTOM_COL_STATUS_SUFFIX = "Status";
   public final static int DAY_TYPE = 1;
   public final static int WEEK_TYPE = 2;
   public final static int MONTH_TYPE = 3;
   public final static int YEAR_TYPE = 4;
   
   /**
    * 
    * @param startableProcesses
    * @return
    */
   public static List<SelectItem> populateDateList(Map<Integer, List<SelectItem>> dayTypeMapping)
   {
      List<SelectItem> items = new ArrayList<SelectItem>();
      for (Integer i = 0; i <= 31; i++)
      {
         items.add(new SelectItem(i.toString(), i.toString()));
      }
      dayTypeMapping.put(DAY_TYPE, items);
      dayTypeMapping.put(WEEK_TYPE, items.subList(0, 6));
      dayTypeMapping.put(MONTH_TYPE, items.subList(0, 13));
      dayTypeMapping.put(YEAR_TYPE, items);
      return items;
   }

   /**
    * 
    * @return
    */
   public static List<SelectItem> populateDurationList()
   {
      List<SelectItem> items = new ArrayList<SelectItem>();
      items.add(new SelectItem(DAY_TYPE, "Days"));
      items.add(new SelectItem(WEEK_TYPE, "Weeks"));
      items.add(new SelectItem(MONTH_TYPE, "Months"));
      items.add(new SelectItem(YEAR_TYPE, "Year"));
      return items;
   }
   
   /**
    * 
    * @param columnId
    * @param columnTitle
    * @param startNumOfDay
    * @param startDateType
    * @param durationNumOfDays
    * @param durationDateType
    * @param columnDefinition
    * @return
    */
   public static JsonObject updateCustomColumnJson(String columnId, String columnTitle, Integer startNumOfDay,
         Integer startDateType, Integer durationNumOfDays, Integer durationDateType, JsonObject columnDefinition,
         Map<String, DateRange> customColumnDateRange)
   {
      if (null == columnDefinition)
      {
         columnDefinition = new JsonObject();
      }
      if (null != columnId)
      {
         columnDefinition.addProperty("columnId", columnId);
      }
      columnDefinition.addProperty("columnTitle", columnTitle);
      columnDefinition.addProperty("startNumOfDays", startNumOfDay);
      columnDefinition.addProperty("startDateType", startDateType);
      columnDefinition.addProperty("durationNumOfDays", durationNumOfDays);
      columnDefinition.addProperty("durationDateType", durationDateType);
      updateCustomColumnDateRange(columnDefinition, customColumnDateRange);
      return columnDefinition;
   }

   public static void updateCustomColumnDateRange(JsonObject columnDefinition, Map<String, DateRange> customColumnDateRange)
   {
      String columnId = columnDefinition.get("columnId").getAsString();
      Integer startNumOfDay = columnDefinition.get("startNumOfDays").getAsInt();
      Integer startDateType = columnDefinition.get("startDateType").getAsInt();
      Integer durationNumOfDays = columnDefinition.get("durationNumOfDays").getAsInt();
      Integer durationDateType = columnDefinition.get("durationDateType").getAsInt();

      CalendarUnit startDtType = getDateType(startDateType);
      CalendarUnit endDtType = getDateType(durationDateType);
      Duration startDuration = getDuration(startDtType, startNumOfDay);
      Duration endDuration = getDuration(endDtType, durationNumOfDays);
      DateRange range = new RelativePastDateRange(startDuration, startDtType, endDuration, endDtType);
      // Store the date range required for UserStatisticsQuery and calcuations at
      // CostPerProcess
      customColumnDateRange.put(columnId, range);
   }

   public static CalendarUnit getDateType(Integer dateType)
   {
      if (dateType == DAY_TYPE)
      {
         return CalendarUnit.DAY;
      }
      else if (dateType == WEEK_TYPE)
      {
         return CalendarUnit.WEEK;
      }
      else if (dateType == MONTH_TYPE)
      {
         return CalendarUnit.MONTH;
      }
      else
      {
         return CalendarUnit.YEAR;
      }
   }

   public static Duration getDuration(CalendarUnit dateType, int day)
   {
      if (dateType.equals(CalendarUnit.DAY))
      {
         return Duration.days(day);
      }
      else if (dateType.equals(CalendarUnit.WEEK))
      {
         return Duration.weeks(day);
      }
      else if (dateType.equals(CalendarUnit.MONTH))
      {
         return Duration.months(day);
      }
      else
      {
         return Duration.years(day);
      }
   }
}
