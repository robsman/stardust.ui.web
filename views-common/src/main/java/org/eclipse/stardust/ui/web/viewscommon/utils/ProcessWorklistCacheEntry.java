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

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;

/**
 * @author anoop.nair
 */
public class ProcessWorklistCacheEntry
{
   private long count;
   private ActivityInstanceQuery activityInstanceQuery;

   /**
    * @param count
    * @param activityInstanceQuery
    */
   public ProcessWorklistCacheEntry(long count, ActivityInstanceQuery activityInstanceQuery)
   {
      this.count = count;
      this.activityInstanceQuery = activityInstanceQuery;
   }

   /**
    * @return
    */
   public long getCount()
   {
      return count;
   }

   /**
    * @param count
    */
   public void setCount(long count)
   {
      this.count = count;
   }

   /**
    * @return
    */
   public ActivityInstanceQuery getActivityInstanceQuery()
   {
      return activityInstanceQuery;
   }

   /**
    * @param activityInstanceQuery
    */
   public void setActivityInstanceQuery(ActivityInstanceQuery activityInstanceQuery)
   {
      this.activityInstanceQuery = activityInstanceQuery;
   }

   /**
    *
    */
   @Override
   public String toString()
   {
      return String.valueOf(count);
   }
}
