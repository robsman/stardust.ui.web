package org.eclipse.stardust.ui.web.reporting.scheduling;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SchedulingUtils
{
   public static String BLANK_SPACE = " ";
   public static String DATE_FORMAT = "yyyy-MM-dd";

   public enum RecurrencePattern
   {
      DAILY("daily"), WEEKLY("weekly"), MONTHLY("monthly"), YEARLY("yearly");

      private String frequency;

      public String getFrequency()
      {
         return frequency;
      }

      private RecurrencePattern(String freq)
      {
         frequency = freq;
      }

   }

   public enum EndMode
   {
      NOEND("noEnd"), ENDAFTERNOOCCURENCES("endAfterNOcurrences"), ENDBYDATE("endByDate");

      private String endMode;

      private EndMode(String s)
      {
         endMode = s;
      }

      public String getEndMode()
      {
         return endMode;
      }

   }

   public static String getDayNameFromIndex(int dayIndex)
   {
      String dayName = null;

      /*
       * For UI, java.util.date 0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 =
       * Thursday, 5 = Friday, 6 = Saturday
       */

      /*
       * For Quartz, 1-7 or SUN-SAT
       */

      if (dayIndex == 1)
      {
         dayName = "MON";
      }
      else if (dayIndex == 2)
      {
         dayName = "TUE";
      }
      else if (dayIndex == 3)
      {
         dayName = "WED";
      }
      else if (dayIndex == 4)
      {
         dayName = "THU";
      }
      else if (dayIndex == 5)
      {
         dayName = "FRI";
      }
      else if (dayIndex == 6)
      {
         dayName = "SAT";
      }
      else if (dayIndex == 0)
      {
         dayName = "SUN";
      }
      return dayName;
   }

   /**
    * This function has dummy implementation , more meaningful implementation would be
    * provided by the usage of daemon/engine.
    * 
    * @param selectedExecutionTime
    * @return
    */
   public static String getExecutionTime(String selectedExecutionTime)
   {
      Map<String, String> map = new HashMap<String, String>();
      map.put("01", "12:00 AM");
      map.put("02", "12:30 AM");
      map.put("03", "01:00 AM");
      map.put("04", "01:30 AM");
      map.put("05", "02:00 AM");
      map.put("06", "02:30 AM");
      map.put("07", "03:00 AM");
      map.put("08", "03:30 AM");
      map.put("09", "04:00 AM");
      map.put("10", "04:30 AM");
      map.put("11", "05:00 AM");
      map.put("12", "05:30 AM");
      map.put("13", "06:00 AM");
      map.put("14", "06:30 AM");
      map.put("15", "07:00 AM");
      map.put("16", "07:30 AM");
      map.put("17", "08:00 AM");
      map.put("18", "08:30 AM");
      map.put("19", "09:00 AM");
      map.put("20", "09:30 AM");
      map.put("21", "10:00 AM");
      map.put("22", "10:30 AM");
      map.put("23", "11:00 AM");
      map.put("24", "11:30 AM");
      map.put("25", "12:00 PM");
      map.put("26", "12:30 PM");
      map.put("27", "01:00 PM");
      map.put("28", "01:30 PM");
      map.put("29", "02:00 PM");
      map.put("30", "02:30 PM");
      map.put("31", "03:00 PM");
      map.put("32", "03:30 PM");
      map.put("33", "04:00 PM");
      map.put("34", "04:30 PM");
      map.put("35", "05:00 PM");
      map.put("36", "05:30 PM");
      map.put("37", "06:00 PM");
      map.put("38", "06:30 PM");
      map.put("39", "07:00 PM");
      map.put("40", "07:30 PM");
      map.put("41", "08:00 PM");
      map.put("42", "08:30 PM");
      map.put("43", "09:00 PM");
      map.put("44", "09:30 PM");
      map.put("45", "10:00 PM");
      map.put("46", "10:30 PM");
      map.put("47", "11:00 PM");
      map.put("48", "11:30 PM");

      String string = map.get(selectedExecutionTime);
      return string;

   }

}
