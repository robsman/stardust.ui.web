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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.eclipse.stardust.ui.web.common.util.FacesUtils;

public class TimeUnit
{
   private final int numberOfElements;

   private final int pixelSpace;

   private final int startTimeUnit;

   private final int nextLowerTimeUnit;

   private final int amountOfTimeToAdd;

   public static final TimeUnit MINUTE = new TimeUnit(Calendar.MINUTE, Calendar.SECOND,
         6, 100, 10, TimeElements.MINUTE_TYPE)
   {
      public int calculateSize(double value)
      {
         return (int) (value * 1000);
      }

      public Calendar calculateStartDate(long date)
      {
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(date);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         return cal;
      }

      public DateFormat getDateFormat()
      {
         return DateFormat.getTimeInstance(DateFormat.MEDIUM, getLocale());
      }
   };

   public static final TimeUnit HOUR = new TimeUnit(Calendar.HOUR_OF_DAY,
         Calendar.MINUTE, 6, 100, 10, TimeElements.HOUR_TYPE)
   {
      public int calculateSize(double value)
      {
         return (int) (value * 10);
      }

      public Calendar calculateStartDate(long date)
      {
         return calculateDefaultStartDate(date);
      }

   };

   public static final TimeUnit DAY = new TimeUnit(Calendar.HOUR_OF_DAY, Calendar.HOUR,
         24, 60, 1, TimeElements.DAY_TYPE)
   {
      // public int calculateSize(double value)
      // {
      // BigDecimal valueBD = new BigDecimal(value);
      // BigDecimal number = new BigDecimal(1.3);
      // BigDecimal result = valueBD.divide(number, 2, BigDecimal.ROUND_HALF_UP);
      // return result.intValue();
      // }

      public Calendar calculateStartDate(long date)
      {
         return calculateDefaultStartDate(date);
      }

   };

   public static final TimeUnit WEEK = new TimeUnit(Calendar.DAY_OF_WEEK, Calendar.HOUR,
         7, 96, 24, TimeElements.WEEK_TYPE)
   {
      public int calculateSize(double value)
      {
         return (int) (value / 15);
      }

      public Calendar calculateStartDate(long date)
      {
         return calculateDayStartDate(date);
      }

      public DateFormat getDateFormat()
      {
         return DateFormat.getDateInstance(DateFormat.SHORT, getLocale());
      }
   };

   public static final TimeUnit MONTH = new TimeUnit(Calendar.WEEK_OF_YEAR,
         Calendar.HOUR, 5, 168, 24 * 7, TimeElements.MONTH_TYPE)
   {
      public int calculateSize(double value)
      {
         return (int) (value / 60);
      }

      public Calendar calculateStartDate(long date)
      {
         return calculateDayStartDate(date);
      }

      public DateFormat getDateFormat()
      {
         return DateFormat.getDateInstance(DateFormat.SHORT, getLocale());
      }

      public DateFormat getHeaderDateFormat()
      {
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMMM", getLocale());
         return simpleDateFormat;
      }
   };

   public static final TimeUnit YEAR = new TimeUnit(Calendar.MONTH, Calendar.MONTH, 12,
         60, 1, TimeElements.YEAR_TYPE)
   {
      public int calculateSize(double value)
      {
         BigDecimal hourOfMonth = new BigDecimal(startDate
               .getActualMaximum(Calendar.DAY_OF_MONTH) * 24);
         BigDecimal pixelSpace = new BigDecimal(getPixelSpace());
         BigDecimal number = pixelSpace.divide(hourOfMonth, 4, BigDecimal.ROUND_HALF_UP);
         BigDecimal minutes = new BigDecimal(60);
         BigDecimal size = minutes.divide(number, 4, BigDecimal.ROUND_HALF_UP);
         return new BigDecimal(value).divide(size, 4, BigDecimal.ROUND_HALF_UP)
               .intValue();
      }

      public Calendar calculateStartDate(long date)
      {
         Calendar cal = calculateDayStartDate(date);
         cal.set(Calendar.DAY_OF_MONTH, 1);
         startDate = Calendar.getInstance();
         startDate.setTimeInMillis(cal.getTimeInMillis());
         return cal;
      }

      public int getPixelSpace()
      {
         int space = super.getPixelSpace();
         if (startDate != null)
         {
            space = startDate.getActualMaximum(Calendar.DAY_OF_MONTH) * 2;
         }
         return space;
      }

      public DateFormat getDateFormat()
      {
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM", getLocale());
         return simpleDateFormat;
      }

      public DateFormat getHeaderDateFormat()
      {
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy", getLocale());
         return simpleDateFormat;
      }
   };

   public static final TimeUnit DECADE = new TimeUnit(Calendar.YEAR, Calendar.YEAR, 10,
         80, 1, TimeElements.DECADE_TYPE)
   {
      public int calculateSize(double value)
      {
         BigDecimal hoursOfYear = new BigDecimal(startDate
               .getActualMaximum(Calendar.DAY_OF_YEAR) * 24);
         BigDecimal pixelSpace = new BigDecimal(getPixelSpace());
         BigDecimal number = pixelSpace.divide(hoursOfYear, 4, BigDecimal.ROUND_HALF_UP);
         BigDecimal minutes = new BigDecimal(60);
         BigDecimal size = minutes.divide(number, 4, BigDecimal.ROUND_HALF_UP);
         return new BigDecimal(value).divide(size, 4, BigDecimal.ROUND_HALF_UP).intValue();
      }

      public Calendar calculateStartDate(long date)
      {
         Calendar cal = calculateDayStartDate(date);
         cal.set(Calendar.DAY_OF_YEAR, 1);
         startDate = Calendar.getInstance();
         startDate.setTimeInMillis(cal.getTimeInMillis());
         return cal;
      }

      public int getPixelSpace()
      {
         int space = super.getPixelSpace();
         if (startDate != null)
         {
            space = startDate.getActualMaximum(Calendar.DAY_OF_YEAR) / 5;
         }
         return space;
      }

      public DateFormat getDateFormat()
      {
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy", getLocale());
         return simpleDateFormat;
      }
   };

   private final String type;

   private static Calendar startDate;

   private TimeUnit(int startTimeUnit, int nextLowerTimeUnit, int numberOfElements,
         int pixelSpace, int amountOfTimeToAdd, String type)
   {
      this.startTimeUnit = startTimeUnit;
      this.nextLowerTimeUnit = nextLowerTimeUnit;
      this.numberOfElements = numberOfElements;
      this.pixelSpace = pixelSpace;
      this.amountOfTimeToAdd = amountOfTimeToAdd;
      this.type = type;
   }

   public int getStartTimeUnit()
   {
      return startTimeUnit;
   }

   public int getNextLowerTimeUnit()
   {
      return nextLowerTimeUnit;
   }

   public int getNumberOfElements()
   {
      return numberOfElements;
   }

   public int getPixelSpace()
   {
      return pixelSpace;
   }

   public int getAmountOfTimeToAdd()
   {
      return amountOfTimeToAdd;
   }

   public String getTimeType()
   {
      return type;
   }

   public int calculateSize(double progressLeft)
   {
      return (int) progressLeft;
   }

   public Calendar calculateStartDate(long date)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(date);
      return cal;
   }

   private static Calendar calculateDefaultStartDate(long date)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(date);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      return cal;
   }

   private static Calendar calculateDayStartDate(long date)
   {
      Calendar cal = calculateDefaultStartDate(date);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      return cal;
   }

   public DateFormat getDateFormat()
   {
      return DateFormat.getTimeInstance(DateFormat.SHORT, getLocale());
   }

   public DateFormat getHeaderDateFormat()
   {
      return DateFormat.getDateInstance(DateFormat.SHORT, getLocale());
   }

   private static Locale getLocale()
   {
      return FacesUtils.getLocaleFromRequest();
   }
}
