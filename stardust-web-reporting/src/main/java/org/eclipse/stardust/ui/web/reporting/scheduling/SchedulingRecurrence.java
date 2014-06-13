package org.eclipse.stardust.ui.web.reporting.scheduling;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.quartz.CronExpression;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public abstract class SchedulingRecurrence
{

   private static final Logger trace = LogManager.getLogger(SchedulingRecurrence.class);

   private String SECONDS = "0";

   private String MINUTES = "0";

   private String HOURS = "12";

   private Date startDate = null;

   private String startTime;

   public String getSECONDS()
   {
      return SECONDS;
   }

   public void setSECONDS(String sECONDS)
   {
      SECONDS = sECONDS;
   }

   public String getMINUTES()
   {
      return MINUTES;
   }

   public void setMINUTES(String mINUTES)
   {
      MINUTES = mINUTES;
   }

   public String getHOURS()
   {
      return HOURS;
   }

   public void setHOURS(String hOURS)
   {
      HOURS = hOURS;
   }

   public Date getStartDate()
   {
      return startDate;
   }

   public void setStartDate(Date startDate)
   {
      this.startDate = startDate;
   }

   public String getStartTime()
   {
      return startTime;
   }

   public abstract String generateSchedule(JsonObject json);

   public String prcoessSchedule(JsonObject json)
   {
      Date currentDate = Calendar.getInstance().getTime();

      String startDateStr = json.get("recurrenceRange").getAsJsonObject()
            .get("startDate").getAsString();

      String uIselectedExecutionTime = json.get("executionTime").getAsString();
      String executionTime = SchedulingUtils.getExecutionTime(uIselectedExecutionTime);

      String input = startDateStr + SchedulingUtils.BLANK_SPACE + executionTime;
      // Format of the date defined in the input String
      startDate = SchedulingUtils.getParsedDate(input, "yyyy-MM-dd hh:mm aa");

      startTime = startDate.getSeconds() + SchedulingUtils.BLANK_SPACE
            + startDate.getMinutes() + SchedulingUtils.BLANK_SPACE + startDate.getHours()
            + SchedulingUtils.BLANK_SPACE;

      // Set Current time to compare with Scheduled Execution time.
      startDate.setHours(0);
      startDate.setMinutes(0);
      startDate.setSeconds(0);

      trace.info("Start Date: " + startDate);

      String cronExpressionInput = this.generateSchedule(json);

      trace.info("CronExpression: " + cronExpressionInput.toString());
      CronExpression cronExpression = null;

      try
      {
         cronExpression = new CronExpression(cronExpressionInput);
      }
      catch (ParseException e)
      {
         trace.error(e);
      }

      String endMode = json.get("recurrenceRange").getAsJsonObject().get("endMode")
            .getAsString();
      Date endDate = null;
      int count = 0; // stop after n occurrences

      // Logic to determine End Date
      if (endMode.equals(SchedulingUtils.EndMode.NOEND.getEndMode()))
      {
         trace.info("No End Date is selected");
         if (startDate.before(currentDate))
         { // Past Date
            return getNextExecutionDate(cronExpression, currentDate);
         }
         else if (startDate.after(currentDate))
         {
            // Future Date
            return getNextExecutionDate(cronExpression, startDate);
         }
      }
      else if (endMode.equals(SchedulingUtils.EndMode.ENDAFTERNOOCCURENCES.getEndMode()))
      {
         int occurences = json.get("recurrenceRange").getAsJsonObject().get("occurences")
               .getAsInt();
         count = occurences;

         if (count <= 0)
         {
            return null;
         }

         // Generate n Future Execution Dates
         List<Date> nFutureExecutionDates = generateNFutureExecutionDates(cronExpression,
               startDate, count);
         trace.info("N Future occurences: " + nFutureExecutionDates.toString());
         if (nFutureExecutionDates.get(nFutureExecutionDates.size() - 1).before(
               currentDate))
         {
            trace.info("All Occurences are finished");
            return null;
         }
         for (Date date : nFutureExecutionDates)
         {
            if (date.after(currentDate))
            {
               return SchedulingUtils.convertDate(date,
                     SchedulingUtils.CLIENT_DATE_FORMAT);
            }
         }
      }
      else if (endMode.equals(SchedulingUtils.EndMode.ENDBYDATE.getEndMode()))
      {
         String endDateStr = json.get("recurrenceRange").getAsJsonObject().get("endDate")
               .getAsString();
         endDate = SchedulingUtils.getParsedDate(endDateStr,
               SchedulingUtils.CLIENT_DATE_FORMAT);
         endDate.setHours(23);
         endDate.setMinutes(59);
         endDate.setSeconds(59);

         trace.info("End Date: " + endDate);

         if (endDate != null)
         {
            if (startDate.after(endDate))
            {
               trace.info("Start Date is after End Date");
               return null;
            }
            if (startDate.before(currentDate) && endDate.before(currentDate))
            {// Start Date and End Date are less than current date.
               trace.info("Start Date and End Date are less than current date");
               return null;
            }
            else if (startDate.before(currentDate) && endDate.after(currentDate))
            {
               Date nextValidTimeAfter = cronExpression
                     .getNextValidTimeAfter(currentDate);
               trace.info("Next Execution Date: " + nextValidTimeAfter);
               
               return (nextValidTimeAfter.before(endDate)) ? SchedulingUtils.convertDate(
                     nextValidTimeAfter, SchedulingUtils.CLIENT_DATE_FORMAT) : null;
            }
         }
      }
      return null;
   }

   private String getNextExecutionDate(CronExpression cronExpression, Date startDate)
   {
      Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(startDate);
      trace.info("Next Execution Date: " + nextValidTimeAfter);
      return SchedulingUtils.convertDate(nextValidTimeAfter,
            SchedulingUtils.CLIENT_DATE_FORMAT);
   }

   private List<Date> generateNFutureExecutionDates(CronExpression cronExpression,
         Date startDate, int count)
   {
      List<Date> nFutureExecutionDates = new ArrayList<Date>(count);
      for (int i = 0; i < count; i++)
      {
         Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(startDate);
         nFutureExecutionDates.add(nextValidTimeAfter);
         startDate = nextValidTimeAfter;
      }
      return nFutureExecutionDates;
   }

}
