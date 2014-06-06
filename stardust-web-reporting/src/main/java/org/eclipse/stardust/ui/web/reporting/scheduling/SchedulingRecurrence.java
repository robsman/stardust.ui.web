package org.eclipse.stardust.ui.web.reporting.scheduling;

import java.text.ParseException;
import java.util.Date;

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
      
      //Set Current time to compare with Scheduled Execution time.
      startDate.setHours(new Date().getHours());
      startDate.setMinutes(new Date().getMinutes());
      startDate.setSeconds(new Date().getSeconds());
      
      trace.info("Start Date: " + startDate);

      String cronExpressionInput = this.generateSchedule(json);

      String endMode = json.get("recurrenceRange").getAsJsonObject().get("endMode")
            .getAsString();
      Date endDate = null;
      int count = 0; // stop after n occurences

      // Logic to determine End Date
      if (endMode.equals(SchedulingUtils.EndMode.NOEND.getEndMode()))
      {
         count = 2;

      }
      else if (endMode.equals(SchedulingUtils.EndMode.ENDAFTERNOOCCURENCES.getEndMode()))
      {
         int occurences = json.get("recurrenceRange").getAsJsonObject().get("occurences")
               .getAsInt();
         count = occurences;
      }
      else if (endMode.equals(SchedulingUtils.EndMode.ENDBYDATE.getEndMode()))
      {

         String endDateStr = json.get("recurrenceRange").getAsJsonObject().get("endDate")
               .getAsString();
         endDate = SchedulingUtils.getParsedDate(endDateStr,
               SchedulingUtils.CLIENT_DATE_FORMAT);
      }

      if (endDate != null)
      {
         /*
          * RRule rRule = null; try { rRule = new RRule(ical.toString()); } catch
          * (ParseException e) { trace.error(e); } rRule.setUntil(new
          * DateValueImpl(endDate.getYear(), endDate.getMonthOfYear(),
          * endDate.getDayOfMonth())); ical = rRule.toIcal();
          */

      }
      else if (endDate == null && count != 0)
      {
         // TODO
         // cronExpressionInput = cronExpressionInput + ";COUNT=" + count;
      }

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

      trace.info("End Date: " + endDate);

      trace.info("Constructed Cron Expression: " + cronExpressionInput);
      Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(startDate);
      trace.info("Next Execution Date: " + nextValidTimeAfter);

      trace.info("Next to Next Time: "
            + cronExpression.getNextValidTimeAfter(nextValidTimeAfter));
      // trace.info("Get Time After: " + cronExpression.getTimeAfter(startDate));

      return SchedulingUtils.convertDate(nextValidTimeAfter,
            SchedulingUtils.CLIENT_DATE_FORMAT);
   }

}
