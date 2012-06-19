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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.io.Serializable;
import java.util.Date;

public class DateRange implements Serializable
{
   private Date toDateValue;

   private Date fromDateValue;

   public DateRange()
   {}

   public DateRange(Date fromDateValue, Date toDateValue)
   {
      this.fromDateValue = fromDateValue;
      this.toDateValue = toDateValue;
   }

   public void setToDateValue(Date toDateValue)
   {
      this.toDateValue = toDateValue;
   }

   public void setFromDateValue(Date fromDateValue)
   {
      this.fromDateValue = fromDateValue;
   }

   public Date getToDateValue()
   {
      return toDateValue;
   }

   public Date getFromDateValue()
   {
      return fromDateValue;
   }

   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fromDateValue == null) ? 0 : fromDateValue.hashCode());
      result = prime * result + ((toDateValue == null) ? 0 : toDateValue.hashCode());
      return result;
   }

   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final DateRange other = (DateRange) obj;
      if (fromDateValue == null)
      {
         if (other.fromDateValue != null)
            return false;
      }
      else if (!fromDateValue.equals(other.fromDateValue))
         return false;
      if (toDateValue == null)
      {
         if (other.toDateValue != null)
            return false;
      }
      else if (!toDateValue.equals(other.toDateValue))
         return false;
      return true;
   }

}
