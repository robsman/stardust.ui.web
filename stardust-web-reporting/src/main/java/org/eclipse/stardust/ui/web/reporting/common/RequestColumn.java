/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.common;

import org.eclipse.stardust.ui.web.reporting.core.Constants.DurationUnit;

public class RequestColumn extends AbstractColumn
{
   private DurationUnit durationUnit;

   public RequestColumn(String id, DurationUnit durationUnit)
   {
      super(id);
      this.durationUnit = durationUnit;
   }

   public DurationUnit getDurationUnit()
   {
      return durationUnit;
   }
}
