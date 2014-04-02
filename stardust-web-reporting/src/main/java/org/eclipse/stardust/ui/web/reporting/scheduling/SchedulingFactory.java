package org.eclipse.stardust.ui.web.reporting.scheduling;

import com.google.gson.JsonObject;

public class SchedulingFactory
{
   public static SchedulingRecurrence getSchedular(JsonObject json)
   {
      SchedulingRecurrence schedulingRecurrence = null;

      String recurrenceInterval = json.get("recurrenceInterval").getAsString();

      if (recurrenceInterval.equals(SchedulingUtils.RecurrencePattern.DAILY.getFrequency()))
      {
         schedulingRecurrence = new SchedulingRecurrenceDaily();
      }
      else if (recurrenceInterval.equals(SchedulingUtils.RecurrencePattern.WEEKLY.getFrequency()))
      {
         schedulingRecurrence = new SchedulingRecurrenceWeekly();
      }
      else if (recurrenceInterval.equals(SchedulingUtils.RecurrencePattern.MONTHLY.getFrequency()))
      {
         schedulingRecurrence = new SchedulingRecurrenceMonthly();
      }
      else if (recurrenceInterval.equals(SchedulingUtils.RecurrencePattern.YEARLY.getFrequency()))
      {
         schedulingRecurrence = new SchedulingRecurrenceYearly();
      }

      return schedulingRecurrence;
   }
}
