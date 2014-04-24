package org.eclipse.stardust.ui.web.reporting.core.util;

import java.util.Date;

import com.ibm.icu.util.Calendar;

import org.eclipse.stardust.ui.web.reporting.core.Constants.DurationUnit;

public class ReportingUtil
{
   public static long calculateDuration(Date startDate, Date endDate, DurationUnit unit)
   {
      if(startDate == null || startDate.getTime() == 0)
      {
         startDate = new Date();
      }

      if(endDate == null || endDate.getTime() == 0)
      {
         endDate = new Date();
      }

      Calendar startCalendar = Calendar.getInstance();
      startCalendar.setTime(startDate);

      final int calendarField;
      switch(unit)
      {
         case SECOND:
            calendarField = Calendar.SECOND;
            break;
         case MINUTE:
            calendarField = Calendar.MINUTE;
            break;
         case HOUR:
            calendarField = Calendar.HOUR;
            break;
         case DAY:
            calendarField = Calendar.DAY_OF_YEAR;
            break;
         case WEEK:
            calendarField = Calendar.WEEK_OF_YEAR;
            break;
         case YEAR:
            calendarField = Calendar.YEAR;
            break;
         default:
            throw new RuntimeException("Unsupported duration unit: "+unit);
      }

      return startCalendar.fieldDifference(endDate, calendarField);
   }

   /**
    * Converts the key of a duration unit into the equivalent in seconds.
    *
    * @param unit
    * @return
    */
   public static long convertDurationUnit(String unit)
   {
      if (unit.equals("m"))
      {
         return 1000 * 60;
      }
      else if (unit.equals("h"))
      {
         return 1000 * 60 * 60;
      }
      else if (unit.equals("d"))
      {
         return 1000 * 60 * 60 * 24;
      }
      else if (unit.equals("w"))
      {
         return 1000 * 60 * 60 * 24 * 7;
      }
      else if (unit.equals("M"))
      {
         return 1000 * 60 * 60 * 24 * 30; // TODO Consider calendar?
      }
      else if (unit.equals("Y"))
      {
         return 1000 * 60 * 60 * 24 * 30 * 256; // TODO Consider calendar?
      }

      throw new IllegalArgumentException("Duration unit \"" + unit + "\" is not supported.");
   }
}
