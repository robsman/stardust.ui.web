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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.Date;

public class DateUtils
{

   public static final int SECONDS_PER_MINUTE = 60;
   public static final int MINUTES_PER_HOUR = 60;

   public static final int SECONDS_PER_HOUR = MINUTES_PER_HOUR * SECONDS_PER_MINUTE;

   public static final int SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;

   public static final int MILLISECONDS_PER_DAY = 1000 * SECONDS_PER_DAY;
   
   public static final int MILLISECONDS_PER_MINUTE = 1000 * SECONDS_PER_MINUTE;
   
   public static final int MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR;

   public static String formatDurationAsString(double durationInDays)
   {
      return formatDurationAsString(durationInDays, MINUTES_PER_DAY);
   }
   
   public static String formatDurationAsString(double durationInDays, int minutesPerDay)
   {
      StringBuffer _durationString = new StringBuffer(15);

      Double _durationInSec = new Double(durationInDays * SECONDS_PER_DAY);

      int _rest = _durationInSec.intValue();
      
      // append days
      if ((_rest / SECONDS_PER_DAY) < 10)
      {
         _durationString.append('0');
      }
      _durationString.append(_rest / SECONDS_PER_DAY);
      _rest = _rest % SECONDS_PER_DAY;
      _durationString.append("d ");

      // append hours
      if ((_rest / SECONDS_PER_HOUR) < 10)
      {
         _durationString.append('0');
      }

      _durationString.append(_rest / SECONDS_PER_HOUR);
      _rest = _rest % SECONDS_PER_HOUR;
      _durationString.append("h ");

      // append minutes
      if ((_rest / SECONDS_PER_MINUTE) < 10)
      {
         _durationString.append('0');
      }
      _durationString.append(_rest / SECONDS_PER_MINUTE);
      _rest = _rest % SECONDS_PER_MINUTE;
      _durationString.append("m ");

      if ((_rest < 10))
      {
         _durationString.append('0');
      }
      _durationString.append(_rest);
      _durationString.append('s');

      return _durationString.toString();
   }
   
   /**
    * @param startDate
    * @param endDate
    * @return
    */
   public static boolean validateDateRange(Date startDate, Date endDate)
   {
      if (null != startDate && null != endDate)
      {
         if (endDate.before(startDate))
         {
            return false;
         }
      }
      return true;
   }
}
