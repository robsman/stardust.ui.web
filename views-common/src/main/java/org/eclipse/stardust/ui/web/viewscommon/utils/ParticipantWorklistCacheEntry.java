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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.WorklistQuery;

/**
 * @author subodh.godbole
 *
 */
public class ParticipantWorklistCacheEntry implements Serializable
{
   private static final long serialVersionUID = 7571234851632442390L;
   private long count;
   private long totalCountThreshold;
   private WorklistQuery worklistQuery;
   private ActivityInstanceQuery activityInstanceQuery;
   private String worklistOwner;

   /**
    * @param count
    * @param worklistQuery
    */
   public ParticipantWorklistCacheEntry(long count, WorklistQuery worklistQuery)
   {
      this.count = count;
      this.worklistQuery = worklistQuery;
   }
   
   /**
    * @param count
    * @param worklistQuery
    */
   public ParticipantWorklistCacheEntry(long count, WorklistQuery worklistQuery, long totalCountThreshold,String worklistOwner)
   {
      this(count,worklistQuery);
      this.totalCountThreshold = totalCountThreshold;
      this.worklistOwner = worklistOwner;
   }
   
   /**
    * 
    * @param count
    * @param worklistQuery
    * @param activityQuery
    * @param totalCountThreshold
    * @param worklistOwner
    */
   public ParticipantWorklistCacheEntry(long count, WorklistQuery worklistQuery, ActivityInstanceQuery activityQuery,
         long totalCountThreshold, String worklistOwner)
   {
      this(count, worklistQuery);
      this.totalCountThreshold = totalCountThreshold;
      this.worklistOwner = worklistOwner;
      this.activityInstanceQuery = activityQuery;
   }

   public long getCount()
   {
      return count;
   }

   public void setCount(long count)
   {
      this.count = count;
   }

   public long getTotalCountThreshold()
   {
      return totalCountThreshold;
   }

   public void setTotalCountThreshold(long totalCountThreshold)
   {
      this.totalCountThreshold = totalCountThreshold;
   }

   public WorklistQuery getWorklistQuery()
   {
      return worklistQuery;
   }

   public void setWorklistQuery(WorklistQuery worklistQuery)
   {
      this.worklistQuery = worklistQuery;
   }
   
   public ActivityInstanceQuery getActivityInstanceQuery()
   {
      return activityInstanceQuery;
   }

   public void setActivityInstanceQuery(ActivityInstanceQuery activityInstanceQuery)
   {
      this.activityInstanceQuery = activityInstanceQuery;
   }

   public String getWorklistOwner()
   {
      return worklistOwner;
   }

   @Override
   public String toString()
   {
      return String.valueOf(count);
   }
}
