/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core.orm;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.QueryService;

public class ProviderContext
{
   public static final String DURATION_UNIT_ID = "DurationUnitId";

   private Map<String, Object> contextData
      = new HashMap<String, Object>();

   private long totalCount;
   private QueryService queryService;

   public ProviderContext(QueryService queryService, long totalCount)
   {
      this.totalCount = totalCount;
   }

   public Object getContextData(String key)
   {
      return contextData.get(key);
   }

   public void putContextData(String key, Object value)
   {
      contextData.put(key, value);
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
