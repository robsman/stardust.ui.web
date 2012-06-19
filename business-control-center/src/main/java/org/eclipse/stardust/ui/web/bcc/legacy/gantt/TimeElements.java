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
package org.eclipse.stardust.ui.web.bcc.legacy.gantt;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;


public class TimeElements implements Serializable
{
   private final static long serialVersionUID = 1l;
   
   private List<TimeElement> elements;

   private final String timeType;
 

   public static final String DAY_TYPE = "day";

   public static final String MINUTE_TYPE = "min";

   public static final String HOUR_TYPE = "hour";

   public static final String WEEK_TYPE = "week";

   public static final String MONTH_TYPE = "month";

   public static final String YEAR_TYPE = "year";

   public static final String DECADE_TYPE = "decade";

   public TimeElements(Calendar start, TimeUnit timeUnit)
   {
      timeType = timeUnit.getTimeType();
       

      int startTime = start.get(timeUnit.getStartTimeUnit());
      int endTime = startTime + timeUnit.getNumberOfElements();

      Calendar time = (Calendar) start.clone();

      this.elements = CollectionUtils.newArrayList();

      for (int i = startTime; i < endTime; i++)
      {
         int j = i <= startTime ? 0 : i - startTime;

         TimeElement timeElement = new TimeElement(time, j * timeUnit.getPixelSpace(),
               start,timeUnit.getHeaderDateFormat(), timeUnit.getDateFormat(), timeType);

         time.add(timeUnit.getNextLowerTimeUnit(), timeUnit.getAmountOfTimeToAdd());

         this.elements.add(timeElement);
      }
   }

   public List<TimeElement> getElements()
   {
      return elements;
   }

   public void setElements(List<TimeElement> elements)
   {
      this.elements = elements;
   }

   public String getTimeType()
   {
      return timeType;
   }

}
