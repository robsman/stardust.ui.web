/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.common.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;


/**
 * @author Subodh.Godbole
 *
 */
public class DateUtils
{
	
   private static final int SECS_IN_A_DAY = 86400;
   private static final int SECS_IN_AN_HOUR = 3600;
   private static final int SECS_IN_A_MINUTE = 60;
   private static final int MILLIS_TO_SECS_DIV_FACTOR = 1000;
   private static final String TIME_ZONE_PREFIX = "GMT";
   private static final Logger trace = LogManager.getLogger(DateUtils.class);
   
  
   public DateUtils()
   {
      
   }
  
   
   /**
    * @return
    */
   public static String getDateTimeFormat()
   {
      return MessagePropertiesBean.getInstance().getString(
            "portalFramework.formats.defaultDateTimeFormat");
   }
   
   /**
    * @return
    */
   public static String getDateFormat()
   {
      return MessagePropertiesBean.getInstance().getString(
            "portalFramework.formats.defaultDateFormat");
   }
   
   /**
    * @return
    */
   public static String getTimeFormat()
   {
      return MessagePropertiesBean.getInstance().getString(
            "portalFramework.formats.defaultTimeFormat");
   }
   
   /**
    * @param date
    * @return
    */
   public static String formatDateTime(Date date)
   {
      return format(date, getDateTimeFormat());
   }

   /**
    * @param date
    * @return
    */
   public static String formatDate(Date date)
   {
      return format(date, getDateFormat());
   }

   /**
    * @param date
    * @return
    */
   public static String formatTime(Date date)
   {
      return format(date, getTimeFormat());
   }

   /**
    * This method will also consider client time zone if available
    * 
    * @param date
    * @param format
    * @return
    */
   public static String format(Date date, String format)
   {
      return format(date, format, PortalApplication.getInstance().getTimeZone());
   }
  
   /**
    * @param date
    * @param format
    * @param timeZone
    * @return
    */
   public static String format(Date date, String format, TimeZone timeZone)
   {
      UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
      Locale locale = Locale.getDefault();
      if (view != null)
      {
         locale = view.getLocale();
      }
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, locale);

      if (timeZone != null)
      {
         simpleDateFormat.setTimeZone(timeZone);
      }
      return simpleDateFormat.format(date);
   }

   /**
    * @param date
    * @param format
    * @param locale
    * @param timezone
    * @return
    */
   public static String format(Date date, String format, Locale locale, TimeZone timezone)
   {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, locale);
      simpleDateFormat.setTimeZone(timezone);
      return simpleDateFormat.format(date);
   }

   /**
    * @param date
    * @return
    */
   public static Date parseDateTime(String date)
   {
      UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
      Locale locale = Locale.getDefault();
      if (view != null)
      {
         locale = view.getLocale();
      }

      return parseDateTime(date, getDateTimeFormat(), locale, PortalApplication.getInstance().getTimeZone());
   }

   /**
    * @param date
    * @param format
    * @param locale
    * @param timezone
    * @return
    */
   public static Date parseDateTime(String date, String format, Locale locale, TimeZone timezone)
   {
      if (StringUtils.isNotEmpty(date))
      {
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, locale);
         simpleDateFormat.setTimeZone(timezone);
         try
         {
            return simpleDateFormat.parse(date);
         }
         catch (ParseException e)
         {
            // Ignore
         }
      }
      
      return null;
   }
   
   /**
    * TODO: Can use tools like Pretty Time Instead if permissible 
    * @param timeStamp
    * @return
    */
   public static String getHumanReadableTimeStamp(Date timeStamp)
   {
      MessagePropertiesBean msgProps = MessagePropertiesBean.getInstance();

      long diffTime = System.currentTimeMillis() - timeStamp.getTime();
      diffTime = diffTime / 1000; // Now diffTime is in Sec

      StringBuffer str = new StringBuffer("");

      if(diffTime < 0)
      {
         str.append(msgProps.getString("portalFramework.humanDate.FUTURE"));
      }
      else
      {   
         if(diffTime <= 10)
         {
            str.append(msgProps.getString("portalFramework.humanDate.SECONDS"));
         }
         else if(diffTime < 60) // 1 Min
         {
            str.append(diffTime + " " + msgProps.getString("portalFramework.humanDate.SECONDS"));
         }
         else if(diffTime < (2*60)) // 2 Minutes
         {
            str.append("1 " + msgProps.getString("portalFramework.humanDate.MINUTE"));
         }
         else if(diffTime < (60*60)) // 1 Hour
         {
            str.append(((int)diffTime/60) + " " + msgProps.getString("portalFramework.humanDate.MINUTES"));
         }
         else if(diffTime < (2*60*60)) // 2 Hours
         {
            str.append("1 " + msgProps.getString("portalFramework.humanDate.HOUR"));
         }
         else if(diffTime < (24*60*60)) // 24 Hours
         {
            str.append(((int)diffTime/(60*60)) + " " + msgProps.getString("portalFramework.humanDate.HOURS"));
         }
         else if(diffTime < (48*60*60)) // 48 Hours
         {
            str.append("1 " + msgProps.getString("portalFramework.humanDate.DAY"));
         }
         else if(diffTime < (168*60*60)) // 168 Hours
         {
            str.append(((int)diffTime/(24*60*60)) + " " + msgProps.getString("portalFramework.humanDate.DAYS"));
         }
         else if(diffTime < (336 *60*60)) // 336 Hours
         {
            str.append("1 " + msgProps.getString("portalFramework.humanDate.WEEK"));
         }
         else // 168 hours = 1 week
         {
            str.append(((int)diffTime/(168*60*60)) + " " + msgProps.getString("portalFramework.humanDate.WEEKS"));
         }
         
         str.append(" " + msgProps.getString("portalFramework.humanDate.AGO"));
      }

      str.append(str.length() > 0 ? "" : msgProps.getString("portalFramework.humanDate.NOW"));
      
      return str.toString(); 
   }
   /**
    * Converts a <code>Date</code> object to GMT format and return time .
    *
    * @param date   A <code>Date</code> object to be converted 
    * @return A long representing the date after converting to GMT format. 
    */     
   
   public static long convertToGmt( Date date )
   {
      TimeZone tz = TimeZone.getDefault();
      Date ret = new Date( date.getTime() - tz.getRawOffset() );

      // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
      if ( tz.inDaylightTime( ret ))
      {
         Date dstDate = new Date( ret.getTime() - tz.getDSTSavings() );

         // check to make sure we have not crossed back into standard time
         // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
         if ( tz.inDaylightTime( dstDate ))
         {
            ret = dstDate;
         }
      }

      return ret.getTime();
   }
   
   /**
    * Formats the duration in the "dd days hh hours mm minutes ss seconds" format
    * 
    * @param durationInMillis
    * @return
    */
   public static String formatDurationInHumanReadableFormat(long durationInMillis)
   {
      return formatDurationInHumanReadableFormat(durationInMillis, SECS_IN_A_DAY);
   }
   
   /**
    * Formats the duration in the "dd days hh hours mm minutes ss seconds" format
    * 
    * @param durationInMillis
    * @return
    */
   public static String formatDurationInHumanReadableFormat(long durationInMillis, int secsInADay)
   {
      long durationInSecs = durationInMillis / MILLIS_TO_SECS_DIV_FACTOR;
      NumberFormat numFormat = NumberFormat.getInstance();
      numFormat.setMinimumIntegerDigits(2);
      String days = String.valueOf(numFormat.format(durationInSecs / secsInADay));
      durationInSecs = durationInSecs % 86400;
      String hrs = String.valueOf(numFormat.format(durationInSecs / SECS_IN_AN_HOUR));
      durationInSecs = durationInSecs % 3600;
      String mins = String.valueOf(numFormat.format(durationInSecs / SECS_IN_A_MINUTE));
      String secs = String.valueOf(numFormat.format(durationInSecs % SECS_IN_A_MINUTE));
      
      return MessagePropertiesBean.getInstance().getParamString("portalFramework.humanReadable.duration", days, hrs, mins, secs);
   }
   
   /**
    * Returns the Client's timeZone
    * @author Yogesh.Manware
    * @return
    */
   public static TimeZone getClientTimeZone(Object clientTimeZoneOffsetObj)
   {
      if (null != clientTimeZoneOffsetObj && StringUtils.isNotEmpty((String) clientTimeZoneOffsetObj))
      {
         int clientTimeZoneOffset = Integer.valueOf((String) clientTimeZoneOffsetObj);

         StringBuffer clientTimeZoneId = new StringBuffer(TIME_ZONE_PREFIX);
         if (clientTimeZoneOffset < 0)
         {
            clientTimeZoneId.append("+");
         }
         else if (clientTimeZoneOffset > 0)
         {
            clientTimeZoneId.append("-");
         }

         clientTimeZoneOffset = Math.abs(clientTimeZoneOffset);
         int clienthours = (clientTimeZoneOffset / SECS_IN_A_MINUTE);
         int clientMinutes = (clientTimeZoneOffset % SECS_IN_A_MINUTE);

         NumberFormat formatter = new DecimalFormat("00");
         String clienthoursStr = formatter.format(clienthours);
         String clientMinutesStr = formatter.format(clientMinutes);
         clientTimeZoneId.append(clienthoursStr).append(":").append(clientMinutesStr);

         trace.info("Client TimeZone ID: " + clientTimeZoneId);

         return TimeZone.getTimeZone(clientTimeZoneId.toString());
      }
      return null;
   }


   /**
    * Date format pattern for bean 
    * @return
    */
   
   public String getDateFormatter()
   {
      return getDateFormat();
   }
   /**
    * DateTime format pattern for bean 
    * @return
    */
   public String getDateTimeFormatter()
   {
      return getDateTimeFormat();
   }
}
