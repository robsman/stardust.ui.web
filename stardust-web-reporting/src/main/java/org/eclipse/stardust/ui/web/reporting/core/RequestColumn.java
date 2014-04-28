/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core;

import org.eclipse.stardust.ui.web.reporting.core.Constants.DurationUnit;


public class RequestColumn extends AbstractColumn
{
   private DurationUnit durationUnit;
   private boolean descriptor;

   public RequestColumn(String id)
   {
      this(id, null);
   }

   public RequestColumn(String id, DurationUnit durationUnit)
   {
      super(id);
      this.durationUnit = durationUnit;
   }

   public DurationUnit getDurationUnit()
   {
      return durationUnit;
   }

   public void setDurationUnit(DurationUnit durationUnit)
   {
      this.durationUnit = durationUnit;
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
