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

import java.util.Set;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

/**
 * 
 * @author Johnson.Quadras
 *
 */
@DTOClass
public class PostponedActivitiesStatsDTO
{
   public long totalCount;
   
   public String avgDuration;
   
   public long exceededDurationCount;
   
   public Set<Long> allActivityOIDs;
   
   public Set<Long> exceededActivityOIDs;

   /**
    * 
    */
   public PostponedActivitiesStatsDTO()
   {
      // TODO Auto-generated constructor stub
   }

   /**
    * 
    */
   public PostponedActivitiesStatsDTO(long totalCount, String avgDuration, long exceededDurationCount,
         Set<Long> allActivityOIDs, Set<Long> exceededActivityOIDs)
   {
      super();
      this.totalCount = totalCount;
      this.avgDuration = avgDuration;
      this.exceededDurationCount = exceededDurationCount;
      this.allActivityOIDs = allActivityOIDs;
      this.exceededActivityOIDs = exceededActivityOIDs;
   }
   
}
