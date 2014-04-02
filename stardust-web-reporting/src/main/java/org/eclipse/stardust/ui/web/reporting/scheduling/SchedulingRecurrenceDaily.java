package org.eclipse.stardust.ui.web.reporting.scheduling;

import com.google.gson.JsonObject;

public class SchedulingRecurrenceDaily extends SchedulingRecurrence
{
   public String generateSchedule(JsonObject json)
   {
      StringBuilder cronExpr = new StringBuilder();

      String daysRecurrence = json.get("dailyRecurrenceOptions").getAsJsonObject()
            .get("daysRecurrence").getAsString();

      if (daysRecurrence.equals("interval"))
      {
         int daysIntervalCount = json.get("dailyRecurrenceOptions").getAsJsonObject()
               .get("daysIntervalCount").getAsInt();
         cronExpr.append(getSECONDS() + SchedulingUtils.BLANK_SPACE
               + getMINUTES() + SchedulingUtils.BLANK_SPACE
               + getHOURS() + SchedulingUtils.BLANK_SPACE + "1/"
               + daysIntervalCount + SchedulingUtils.BLANK_SPACE + "* ? *");
      }
      else if (daysRecurrence.equals("weekdays"))
      {
         String byDay = "MON-FRI";
         cronExpr.append(getSECONDS() + SchedulingUtils.BLANK_SPACE
               + getMINUTES() + SchedulingUtils.BLANK_SPACE
               + getHOURS() + SchedulingUtils.BLANK_SPACE + "? * " + byDay
               + SchedulingUtils.BLANK_SPACE + "*");
      }

      return cronExpr.toString();
   }

}
