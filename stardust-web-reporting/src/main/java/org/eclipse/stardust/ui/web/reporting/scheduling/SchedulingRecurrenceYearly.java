package org.eclipse.stardust.ui.web.reporting.scheduling;

import com.google.gson.JsonObject;

public class SchedulingRecurrenceYearly extends SchedulingRecurrence
{
   public String generateSchedule(JsonObject json)
   {
      StringBuilder cronExpr = new StringBuilder();

      int recurrenceYearIntervalCount = json.get("yearlyRecurrenceOptions")
            .getAsJsonObject().get("recurEveryYear").getAsInt();

      String yearlyRecurrence = json.get("yearlyRecurrenceOptions").getAsJsonObject()
            .get("yearlyRecurrence").getAsString();

      if (yearlyRecurrence.equals("weekday"))
      {
         int dayNumber = json.get("yearlyRecurrenceOptions").getAsJsonObject()
               .get("onDay").getAsInt();
         int onMonth = json.get("yearlyRecurrenceOptions").getAsJsonObject()
               .get("onMonth").getAsInt();

         cronExpr.append(getSECONDS() + SchedulingUtils.BLANK_SPACE
               + getMINUTES() + SchedulingUtils.BLANK_SPACE
               + getHOURS() + SchedulingUtils.BLANK_SPACE + dayNumber + " "
               + onMonth + " ? 0/" + recurrenceYearIntervalCount);

      }
      else if (yearlyRecurrence.equals("date"))
      {
         int onTheXDay = json.get("yearlyRecurrenceOptions").getAsJsonObject()
               .get("onTheXDay").getAsInt();
         int onTheXDayName = json.get("yearlyRecurrenceOptions").getAsJsonObject()
               .get("onTheXDayName").getAsInt();
         int onTheMonth = json.get("yearlyRecurrenceOptions").getAsJsonObject()
               .get("onTheMonth").getAsInt();

         String byDay = SchedulingUtils.getDayNameFromIndex(onTheXDayName);

         cronExpr.append(getSECONDS() + SchedulingUtils.BLANK_SPACE
               + getMINUTES() + SchedulingUtils.BLANK_SPACE
               + getHOURS() + SchedulingUtils.BLANK_SPACE + "?"
               + SchedulingUtils.BLANK_SPACE + onTheMonth + SchedulingUtils.BLANK_SPACE
               + byDay + "#" + onTheXDay + SchedulingUtils.BLANK_SPACE + "0/" + recurrenceYearIntervalCount);
      }

      return cronExpr.toString();
   }
}
