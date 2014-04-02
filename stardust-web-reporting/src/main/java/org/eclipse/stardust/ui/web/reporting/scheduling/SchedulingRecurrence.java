package org.eclipse.stardust.ui.web.reporting.scheduling;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.reporting.core.ReportingServicePojo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.CronExpression;

import com.google.gson.JsonObject;

public abstract class SchedulingRecurrence
{
   
   private static final Logger trace = LogManager.getLogger(SchedulingRecurrence.class);
   
   private String SECONDS = "0";

   private String MINUTES = "0";

   private String HOURS = "12";
   
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

   public abstract String generateSchedule(JsonObject json);
   
   public String prcoessSchedule(JsonObject json)
   {
      String cronExpressionInput = this.generateSchedule(json);
      
      //TODO Get the execution Time from JsonObject and set into SECONDS, MINUTES, 
      //and HOURS variables

      String startDateStr = json.get("recurrenceRange").getAsJsonObject()
            .get("startDate").getAsString();

      Date startDate = getParsedDate(startDateStr);

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
         endDate = getParsedDate(endDateStr);
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

      trace.info("Start Date: " + startDate);

      trace.info("End Date: " + endDate);

      trace.info("Constructed Cron Expression: " + cronExpressionInput);
      Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(startDate);
      trace.info("Next Execution Date: " + nextValidTimeAfter);

      nextValidTimeAfter = cronExpression.getNextValidTimeAfter(nextValidTimeAfter);

      trace.info("Next to Next Time: "
            + cronExpression.getNextValidTimeAfter(nextValidTimeAfter));
      // trace.info("Get Time After: " + cronExpression.getTimeAfter(startDate));

      String selectedExecutionTime = json.get("executionTime").getAsString();
      SchedulingUtils.getExecutionTime(selectedExecutionTime);

      return convertDate(nextValidTimeAfter);
   }

   private Date getParsedDate(String startDateStr)
   {
      DateFormat df = new SimpleDateFormat(SchedulingUtils.DATE_FORMAT);

      Date date = null;
      try
      {
         date = df.parse(startDateStr);
      }
      catch (ParseException e)
      {
         trace.error(e);
      }

      return date;

   }

   private String convertDate(Date date)
   {
      SimpleDateFormat sdf = new SimpleDateFormat(SchedulingUtils.DATE_FORMAT);
      String convertedDate = sdf.format(date);
      return convertedDate;
   }
}
