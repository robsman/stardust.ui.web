/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core;

import org.eclipse.stardust.ui.web.reporting.core.Constants.TimeUnit;


public class RequestColumn extends AbstractColumn
{
   private TimeUnit timeUnit;

   private boolean descriptor;

   private boolean computed;

   private String computationFormula;

   private Interval interval;

   public RequestColumn(String id)
   {
      this(id, null, null);
   }

   public RequestColumn(String id, TimeUnit timeUnit)
   {
      this(id, timeUnit, null);
   }

   public RequestColumn(String id, Interval interval)
   {
      this(id, null, interval);
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

   public boolean isComputed()
   {
      return computed;
   }

   public void setComputed(boolean computed)
   {
      this.computed = computed;
   }

   public String getComputationFormula()
   {
      return computationFormula;
   }

   public void setComputationFormula(String computationFormula)
   {
      this.computationFormula = computationFormula;
   }

   @Override
   public String toString()
   {
      return "RequestColumn [timeUnit=" + timeUnit + ", descriptor=" + descriptor
            + ", computed=" + computed + ", computationFormula=" + computationFormula
            + ", interval=" + interval + "]";
   }
}
