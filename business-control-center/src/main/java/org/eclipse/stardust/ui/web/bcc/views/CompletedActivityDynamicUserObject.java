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
package org.eclipse.stardust.ui.web.bcc.views;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;

public class CompletedActivityDynamicUserObject extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private String processId;

   private String day;

   private String week;

   private String month;

   /**
    * @param processId
    * @param day
    * @param week
    * @param month
    */
   public CompletedActivityDynamicUserObject(String processId, String day, String week,
         String month)
   {
      super();
      this.processId = processId;
      this.day = day;
      this.week = week;
      this.month = month;
   }

   public String getDay()
   {
      return day;
   }

   public void setDay(String day)
   {
      this.day = day;
   }

   public String getWeek()
   {
      return week;
   }

   public void setWeek(String week)
   {
      this.week = week;
   }

   public String getMonth()
   {
      return month;
   }

   public void setMonth(String month)
   {
      this.month = month;
   }

   public String getProcessId()
   {
      return processId;
   }

   public void setProcessId(String processId)
   {
      this.processId = processId;
   }

}
