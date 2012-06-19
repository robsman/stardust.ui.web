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

/**
 * @author ankita.Patel
 * @version $Revision: $
 */
public class CompletedActivityStatisticsTableEntry extends DefaultRowModel
{

   private static final long serialVersionUID = 1L;
   private String processDefinitionId;
   private long countToday;

   private long countWeek;

   private long countMonth;

   /**
    * 
    */
   public CompletedActivityStatisticsTableEntry()
   {
   // TODO Auto-generated constructor stub
   }

   public CompletedActivityStatisticsTableEntry(String processDefinitionId,
         long countToday, long countWeek, long countMonth)
   {
      super();
      this.processDefinitionId = processDefinitionId;
      this.countToday = countToday;
      this.countWeek = countWeek;
      this.countMonth = countMonth;
   }

   public String getProcessDefinitionId()
   {
      return processDefinitionId;
   }

   public void setProcessDefinitionId(String processDefinitionId)
   {
      this.processDefinitionId = processDefinitionId;
   }

   public long getCountToday()
   {
      return countToday;
   }

   public void setCountToday(long countToday)
   {
      this.countToday = countToday;
   }

   public long getCountWeek()
   {
      return countWeek;
   }

   public void setCountWeek(long countWeek)
   {
      this.countWeek = countWeek;
   }

   public long getCountMonth()
   {
      return countMonth;
   }

   public void setCountMonth(long countMonth)
   {
      this.countMonth = countMonth;
   }

}
