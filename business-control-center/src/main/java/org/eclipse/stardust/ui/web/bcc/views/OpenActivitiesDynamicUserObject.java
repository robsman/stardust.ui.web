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

public class OpenActivitiesDynamicUserObject extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private Long today;

   private Long yesterday;

   private Double month;

   /**
    * @param today
    * @param yesterday
    * @param month
    */
   public OpenActivitiesDynamicUserObject(Long today, Long yesterday, Double month)
   {
      super();
      this.today = today;
      this.yesterday = yesterday;
      this.month = month;
   }

   /**
    * 
    */
   public OpenActivitiesDynamicUserObject()
   {
   // TODO Auto-generated constructor stub
   }

   public Long getToday()
   {
      return today;
   }

   public void setToday(Long today)
   {
      this.today = today;
   }

   public Long getYesterday()
   {
      return yesterday;
   }

   public void setYesterday(Long yesterday)
   {
      this.yesterday = yesterday;
   }

   public Double getMonth()
   {
      return month;
   }

   public void setMonth(String month)
   {
      month = month;
   }
}
