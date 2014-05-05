package org.eclipse.stardust.ui.web.reporting.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.ibm.icu.util.Calendar;

import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.reporting.core.Constants.TimeUnit;

public class ReportingUtil
{
   public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd hh:mm:ss:SSS";

   public static String formatDate(Date d, TimeUnit t)
   {
      if(d != null)
      {
         SimpleDateFormat sdf = new SimpleDateFormat(t.getDateFormat());
         return sdf.format(d);
      }

      return null;
   }

   public static Date parseDate(String s)
   {
      if(StringUtils.isNotEmpty(s))
      {
         SimpleDateFormat f = new SimpleDateFormat(ReportingUtil.DEFAULT_DATE_FORMAT);
         try
         {
            return f.parse(s);
         }
         catch (ParseException e)
         {
            throw new RuntimeException("Could not parse date from string: "+s);
         }
      }

      return null;
   }

   public static ProcessInstance findRootProcessInstance(QueryService queryService, ProcessInstance pi)
   {
      long rootPiOid = pi.getRootProcessInstanceOID();
      if(pi.getOID() == rootPiOid)
      {
         return pi;
      }
      else
      {
         ProcessInstanceQuery rootPiQuery = ProcessInstanceQuery.findAll();
         ProcessInstanceFilter rootPiFilter = new ProcessInstanceFilter(rootPiOid);
         rootPiQuery.where(rootPiFilter);

         ProcessInstance rootPI = queryService.findFirstProcessInstance(rootPiQuery);
         return rootPI;
      }
   }

   public static List<Long> getCollectionValues(String rawFilterValue)
   {
      List<Long> values = new ArrayList<Long>();
      String[] tokens = rawFilterValue.split(",");
      for(String token: tokens)
      {
         values.add(getLongValue(token));
      }

      return values;
   }

   public static Long getLongValue(String rawFilterValue)
   {
      String longToParse = rawFilterValue.replace(" ", "");
      longToParse = rawFilterValue.trim();
      return Long.parseLong(longToParse);
   }

   private static Date normalize(Date date, TimeUnit unit)
   {
      Calendar c = Calendar.getInstance();
      c.setTime(date);

      switch(unit)
      {
         case SECOND:
            break;
         case MINUTE:
            c.set(Calendar.SECOND, 1);
            break;
         case HOUR:
            c.set(Calendar.MINUTE, 1);
            c.set(Calendar.SECOND, 1);
            break;
         case DAY:
            c.set(Calendar.HOUR_OF_DAY, 1);
            c.set(Calendar.MINUTE, 1);
            c.set(Calendar.SECOND, 1);
            break;
         case WEEK:
            c.set(Calendar.DAY_OF_WEEK, 1);
            c.set(Calendar.HOUR_OF_DAY, 1);
            c.set(Calendar.MINUTE, 1);
            c.set(Calendar.SECOND, 1);
            break;
         case MONTH:
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 1);
            c.set(Calendar.MINUTE, 1);
            c.set(Calendar.SECOND, 1);
            break;
         case YEAR:
            c.set(Calendar.MONTH, 1);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 1);
            c.set(Calendar.MINUTE, 1);
            c.set(Calendar.SECOND, 1);
            break;
         default:
            throw new RuntimeException("Unsupported duration unit: "+unit);
      }

      return c.getTime();
   }

   public static long calculateDuration(Date startDate, Date endDate, TimeUnit unit)
   {
      startDate = normalize(startDate, unit);
      endDate = normalize(endDate, unit);

      Calendar startCalendar = Calendar.getInstance();
      startCalendar.setTime(startDate);
      return startCalendar.fieldDifference(endDate, unit.getCalendarField());
   }

   /**
    * Converts the key of a duration unit into the equivalent in seconds.
    *
    * @param unit
    * @return
    */
   public static long convertDurationUnit(String unit)
   {
      if (unit.equals("m"))
      {
         return 1000 * 60;
      }
      else if (unit.equals("h"))
      {
         return 1000 * 60 * 60;
      }
      else if (unit.equals("d"))
      {
         return 1000 * 60 * 60 * 24;
      }
      else if (unit.equals("w"))
      {
         return 1000 * 60 * 60 * 24 * 7;
      }
      else if (unit.equals("M"))
      {
         return 1000 * 60 * 60 * 24 * 30; // TODO Consider calendar?
      }
      else if (unit.equals("Y"))
      {
         return 1000 * 60 * 60 * 24 * 30 * 256; // TODO Consider calendar?
      }

      throw new IllegalArgumentException("Duration unit \"" + unit + "\" is not supported.");
   }
}
