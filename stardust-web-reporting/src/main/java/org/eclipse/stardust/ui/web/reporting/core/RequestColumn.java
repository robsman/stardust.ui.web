/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core;

import org.eclipse.stardust.ui.web.reporting.core.Constants.TimeUnit;


public class RequestColumn extends AbstractColumn
{
   private TimeUnit timeUnit;

   private boolean descriptor;

   private Interval interval;

   public RequestColumn(String id)
   {
      this(id, null);
   }

   public RequestColumn(String id, TimeUnit timeUnit)
   {
      this(id, timeUnit, null);
   }

   public RequestColumn(String id, TimeUnit timeUnit, Interval interval)
   {
      super(id);
      this.timeUnit = timeUnit;
      this.interval = interval;
   }

   public Interval getInterval()
   {
      return interval;
   }

   public void setInterval(Interval interval)
   {
      this.interval = interval;
   }

   public TimeUnit getTimeUnit()
   {
      return timeUnit;
   }

   public void setTimeUnit(TimeUnit timeUnit)
   {
      this.timeUnit = timeUnit;
   }

   public boolean isDescriptor()
   {
      return descriptor;
   }

   public void setDescriptor(boolean descriptor)
   {
      this.descriptor = descriptor;
   }

   @Override
   public String toString()
   {
      StringBuilder s = new StringBuilder();
      s.append("{").append("columnId: ").append(getId());
      s.append(",isDescriptor: "+isDescriptor()).append("}");
      return s.toString();
   }

}
