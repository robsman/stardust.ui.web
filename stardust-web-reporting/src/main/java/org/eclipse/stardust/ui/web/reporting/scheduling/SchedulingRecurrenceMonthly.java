package org.eclipse.stardust.ui.web.reporting.scheduling;

import com.google.gson.JsonObject;

public class SchedulingRecurrenceMonthly extends SchedulingRecurrence
{
   public String generateSchedule(JsonObject json)
   {
      StringBuilder cronExpr = new StringBuilder();

      int recurrenceMonthIntervalCount = 0;

      String monthsRecurrence = json.get("monthlyRecurrenceOptions").getAsJsonObject()
            .get("monthsRecurrence").getAsString();

      if (monthsRecurrence.equals("day"))
      {
         recurrenceMonthIntervalCount = json.get("monthlyRecurrenceOptions")
               .getAsJsonObject().get("month").getAsInt();

         int dayNumber = json.get("monthlyRecurrenceOptions").getAsJsonObject()
               .get("dayNumber").getAsInt();

         cronExpr.append(getSECONDS() + SchedulingUtils.BLANK_SPACE
               + getMINUTES() + SchedulingUtils.BLANK_SPACE
               + getHOURS() + SchedulingUtils.BLANK_SPACE + dayNumber + " 1/"
               + recurrenceMonthIntervalCount + " ? *");
      }
      else if (monthsRecurrence.equals("weekday"))
      {
         recurrenceMonthIntervalCount = json.get("monthlyRecurrenceOptions")
               .getAsJsonObject().get("monthIndex").getAsInt();

         int dayIndex = json.get("monthlyRecurrenceOptions").getAsJsonObject()
               .get("dayIndex").getAsInt();

         int day = json.get("monthlyRecurrenceOptions").getAsJsonObject().get("day")
               .getAsInt();

         String byDay = SchedulingUtils.getDayNameFromIndex(day);

         cronExpr.append(getSECONDS() + SchedulingUtils.BLANK_SPACE
               + getMINUTES() + SchedulingUtils.BLANK_SPACE
               + getHOURS() + SchedulingUtils.BLANK_SPACE + "? 1/"
               + recurrenceMonthIntervalCount + SchedulingUtils.BLANK_SPACE + byDay + "#"
               + dayIndex + SchedulingUtils.BLANK_SPACE + "*");
      }

      return cronExpr.toString();
   }
}
