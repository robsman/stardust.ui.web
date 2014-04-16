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
         cronExpr.append(getStartTime() + getStartDate().getDate() + "/"
               + daysIntervalCount + SchedulingUtils.BLANK_SPACE + "* ? *");
      }
      else if (daysRecurrence.equals("weekdays"))
      {
         String byDay = "MON-FRI";
         cronExpr.append(getStartTime() + "? * " + byDay + SchedulingUtils.BLANK_SPACE
               + "*");
      }

      return cronExpr.toString();
   }

}
