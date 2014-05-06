/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core.handler;

import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;

public class HandlerContext
{
   private long totalCount;
   private QueryService queryService;
   private RequestColumn column;

   public HandlerContext(QueryService queryService, long totalCount)
   {
      this.totalCount = totalCount;
      this.queryService = queryService;
   }

   public RequestColumn getColumn()
   {
      return column;
   }

   public void setColumn(RequestColumn column)
   {
      this.column = column;
   }

   public long getTotalCount()
   {
      return totalCount;
   }

   public QueryService getQueryService()
   {
      return queryService;
   }
}
