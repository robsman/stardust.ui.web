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
package org.eclipse.stardust.ui.web.bcc.legacy.gantt;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeElement implements Serializable
{
   private final static long serialVersionUID = 1l;
   
   private static final String NON_BREAKING_SPACE = "\u00A0";

   private Date time;

   private Date date;

   private int left;

   private final DateFormat dateFormat;

   private final DateFormat headerDateformat;

   public TimeElement(Calendar date, int left, Calendar start,
         DateFormat headerDateformat, DateFormat dateFormat, String type)
   {
      this.headerDateformat = headerDateformat;
      this.time = date.getTime();
      this.dateFormat = dateFormat;

      int date_hour = date.get(Calendar.HOUR_OF_DAY);
      int date_previous_hour = date_hour - 1;
      int start_hour = start.get(Calendar.HOUR_OF_DAY);
      int date_day = date.get(Calendar.DAY_OF_MONTH);
      int start_day = start.get(Calendar.DAY_OF_MONTH);
      int date_month = date.get(Calendar.MONTH);
      int start_month = start.get(Calendar.MONTH);
      int date_year = date.get(Calendar.YEAR);
      int start_year = start.get(Calendar.YEAR);

      boolean newDay = ((date_hour == 0 && !(TimeElements.WEEK_TYPE.equals(type)
            || TimeElements.MONTH_TYPE.equals(type)
            || TimeElements.YEAR_TYPE.equals(type) || TimeElements.DECADE_TYPE
            .equals(type))) || (date_previous_hour == start_hour && date_day == start_day
            && date_month == start_month && date_year == start_year));
      boolean newYear = (TimeElements.YEAR_TYPE.equals(type) && ((date_month == Calendar.JANUARY) || ((date_hour == start_hour
            && date_day == start_day && date_month == start_month))));
      boolean newMonth = (TimeElements.MONTH_TYPE.equals(type) && ((isNewMonth(date_day)) || ((date_hour == start_hour
            && date_day == start_day && date_month == start_month))
            && date_year == start_year));
      boolean newSecond = (TimeElements.MINUTE_TYPE.equals(type) && (date
            .get(Calendar.SECOND) == 0));
      boolean newMinute = (TimeElements.HOUR_TYPE.equals(type) && (date
            .get(Calendar.MINUTE) == 0));
      
      if (newDay || newYear || newMonth || newSecond || newMinute)
      {
         this.date = date.getTime();
      }
      this.left = left;
   }

   private boolean isNewMonth(int day)
   {
      return day > 0 && day < 8;
   }

   public String getTime()
   {
      StringBuffer result = new StringBuffer();
      String formattedTime = dateFormat.format(time);
      String[] split = formattedTime.split(" ");
      for (int i = 0; i < split.length; i++)
      {
         if (i < split.length - 1)
         {
            result.append(split[i] + NON_BREAKING_SPACE);
         }
         else
         {
            result.append(split[i]);
         }
      }
      formattedTime = result.toString();
      return result.toString();
   }

   public void setTime(Date time)
   {
      this.time = time;
   }

   public int getLeft()
   {
      return left;
   }

   public void setLeft(int left)
   {
      this.left = left;
   }

   public String getDate()
   {
      return date != null ? headerDateformat.format(date) : null;
   }

   public void setDate(Date date)
   {
      this.date = date;
   }

}
