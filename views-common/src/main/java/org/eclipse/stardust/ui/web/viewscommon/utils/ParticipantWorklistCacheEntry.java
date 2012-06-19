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

import org.eclipse.stardust.engine.api.query.WorklistQuery;

/**
 * @author subodh.godbole
 *
 */
public class ParticipantWorklistCacheEntry
{
   private long count;
   private WorklistQuery worklistQuery;

   /**
    * @param count
    * @param worklistQuery
    */
   public ParticipantWorklistCacheEntry(long count, WorklistQuery worklistQuery)
   {
      this.count = count;
      this.worklistQuery = worklistQuery;
   }

   public long getCount()
   {
      return count;
   }

   public void setCount(long count)
   {
      this.count = count;
   }

   public WorklistQuery getWorklistQuery()
   {
      return worklistQuery;
   }

   public void setWorklistQuery(WorklistQuery worklistQuery)
   {
      this.worklistQuery = worklistQuery;
   }

   @Override
   public String toString()
   {
      return String.valueOf(count);
   }
}
