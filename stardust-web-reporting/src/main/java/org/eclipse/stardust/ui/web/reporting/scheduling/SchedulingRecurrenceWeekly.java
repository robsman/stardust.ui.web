package org.eclipse.stardust.ui.web.reporting.scheduling;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

public class SchedulingRecurrenceWeekly extends SchedulingRecurrence
{
   public String generateSchedule(JsonObject json)
   {
      int recurrenceWeekIntervalCount = json.get("weeklyRecurrenceOptions")
            .getAsJsonObject().get("recurrenceWeekCount").getAsInt();

      List<String> byDay = new ArrayList<String>();

      if (json.get("weeklyRecurrenceOptions").getAsJsonObject().get("mondays")
            .getAsBoolean())
      {
         byDay.add("MON");
      }
      if (json.get("weeklyRecurrenceOptions").getAsJsonObject().get("tuesdays")
            .getAsBoolean())
      {
         byDay.add("TUE");
      }
      if (json.get("weeklyRecurrenceOptions").getAsJsonObject().get("wednesdays")
            .getAsBoolean())
      {
         byDay.add("WED");
      }
      if (json.get("weeklyRecurrenceOptions").getAsJsonObject().get("thursdays")
            .getAsBoolean())
      {
         byDay.add("THU");
      }
      if (json.get("weeklyRecurrenceOptions").getAsJsonObject().get("fridays")
            .getAsBoolean())
      {
         byDay.add("FRI");
      }
      if (json.get("weeklyRecurrenceOptions").getAsJsonObject().get("saturdays")
            .getAsBoolean())
      {
         byDay.add("SAT");
      }
      if (json.get("weeklyRecurrenceOptions").getAsJsonObject().get("sundays")
            .getAsBoolean())
      {
         byDay.add("SUN");
      }

      StringBuilder commaSepValueBuilder = new StringBuilder();
      for (int i = 0; i < byDay.size(); i++)
      {
         // if the value is not the last element of the list then append the comma(,) as
         // well
         commaSepValueBuilder.append(byDay.get(i));
         if (i != byDay.size() - 1)
         {
            commaSepValueBuilder.append(",");
         }
      }

      StringBuilder cronExpr = new StringBuilder();

      cronExpr.append(getStartTime() + "? *"
            + SchedulingUtils.BLANK_SPACE + commaSepValueBuilder.toString()
            + SchedulingUtils.BLANK_SPACE + "*");

      return cronExpr.toString();
   }
}
