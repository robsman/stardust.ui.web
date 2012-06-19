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
package org.eclipse.stardust.ui.client.util;

import java.io.Serializable;
import java.util.Calendar;

public class DateRange implements Serializable
{
   private static final long serialVersionUID = 1L;

   private Calendar fromDate;

   private Calendar toDate;

   public Calendar getFromDate()
   {
      return fromDate;
   }

   public void setFromDate(Calendar fromDate)
   {
      this.fromDate = fromDate;
   }

   public Calendar getToDate()
   {
      return toDate;
   }

   public void setToDate(Calendar toDate)
   {
      this.toDate = toDate;
   }
}
