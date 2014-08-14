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
   
   public String getXDayOfMonthOrYear(int dayIndex) 
   {
	   String xDayOfMonthOrYear =  "#" + dayIndex;
       if (dayIndex == 5) {
      	 //To handle special case of Last day of every month/year
		 xDayOfMonthOrYear = "L";
	   }
       return xDayOfMonthOrYear;
   }

   public abstract String generateSchedule(JsonObject json);

   public String prcoessSchedule(JsonObject json)
   {
      Date processSchedule = processSchedule(json, false);
      return processSchedule != null ? SchedulingUtils.convertDate(
            processSchedule, SchedulingUtils.CLIENT_DATE_FORMAT) : null;
   }

   @SuppressWarnings("deprecation")
   public Date processSchedule(JsonObject json, boolean daemon)
   {
      Date currentDate = Calendar.getInstance().getTime();

      String startDateStr = json.get("recurrenceRange").getAsJsonObject()
            .get("startDate").getAsString();

      String uIselectedExecutionTime = json.get("executionTime").getAsString();
      String executionTime = SchedulingUtils.getExecutionTime(uIselectedExecutionTime);

      String input = startDateStr + SchedulingUtils.BLANK_SPACE + executionTime;
      // Format of the date defined in the input String
      startDate = SchedulingUtils.getParsedDate(input, "yyyy-MM-dd hh:mm aa");

      setStartTimeString(daemon);
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

      setStartTimeString(false);
      cronExpressionInput = this.generateSchedule(json);
      CronExpression cronExpressionFuture = null;
      try
      {
         cronExpressionFuture = new CronExpression(cronExpressionInput);
      }
      catch (ParseException e)
      {
         trace.error(e);
      }

      // Set Current time to compare with Scheduled Execution time.
      startDate.setHours(0);
      startDate.setMinutes(0);
      startDate.setSeconds(0);

      trace.info("Start Date: " + startDate);

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
            return getNextExecutionDate(cronExpression, currentDate, null);
         }
         else if (startDate.after(currentDate))
         {
            // Future Date
            return getNextExecutionDate(cronExpression, startDate, null);
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
         List<Date> nFutureExecutionDates = generateNFutureExecutionDates(cronExpressionFuture,
               startDate, count);
         trace.info("N Future occurences: " + nFutureExecutionDates.toString());

         Date lastDate = nFutureExecutionDates.get(nFutureExecutionDates.size() - 1);
         if(daemon)
         {
            lastDate.setSeconds(59);
         }
         if (lastDate.before(currentDate))
         {
            trace.info("All Occurences are finished");
            return null;
         }
         for (Date date : nFutureExecutionDates)
         {
            if(daemon)
            {
               date.setSeconds(59);
            }
            if (date.after(currentDate))
            {
               if(daemon)
               {
                  date.setSeconds(0);
               }
               return date;
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
               trace.info("Invalid Dates: Start Date is after End Date");
               return null;
            }
            if (startDate.before(currentDate) && endDate.before(currentDate))
            {// Start Date and End Date are less than current date.
               trace.info("Start Date and End Date are less than current date");
               return null;
            }
            else if (startDate.before(currentDate) && endDate.after(currentDate))
            {
               // Current Running Scenario
               return getNextExecutionDate(cronExpression, currentDate, endDate);
            }
            else if (startDate.after(currentDate))
            {
               // Future Date Scenario
               return getNextExecutionDate(cronExpression, startDate, endDate);
            }
         }
      }
      return null;
   }

   @SuppressWarnings("deprecation")
   private void setStartTimeString(boolean daemon)
   {
      String cronSeconds = "0/1";
      if(!daemon)
      {
         try
         {
            cronSeconds = Integer.toString(startDate.getSeconds());
         }
         catch (Exception e)
         {
            trace.error(e);
         }
      }

      startTime = cronSeconds + SchedulingUtils.BLANK_SPACE
            + startDate.getMinutes() + SchedulingUtils.BLANK_SPACE + startDate.getHours()
            + SchedulingUtils.BLANK_SPACE;
   }

   private Date getNextExecutionDate(CronExpression cronExpression, Date startDate, Date endDate)
   {
      Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(startDate);
      trace.info("Next Execution Date: " + nextValidTimeAfter);
      if (endDate == null)
      {
         return nextValidTimeAfter;
      } else
      {
         return (nextValidTimeAfter.before(endDate)) ?
               nextValidTimeAfter : null;
      }
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