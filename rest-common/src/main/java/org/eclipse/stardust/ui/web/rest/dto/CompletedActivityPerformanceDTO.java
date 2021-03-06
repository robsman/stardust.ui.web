/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

/**
 * 
 * @author Johnson.Quadras
 *
 */
@DTOClass
public class CompletedActivityPerformanceDTO
{
   public int day;

   public int week;

   public int month;

   public CompletedActivityPerformanceDTO()
   {
      // TODO Auto-generated constructor stub
   }

   /**
    * 
    * @param day
    * @param week
    * @param month
    */
   public CompletedActivityPerformanceDTO(int day, int week, int month)
   {
      super();
      this.day = day;
      this.week = week;
      this.month = month;
   }
}
