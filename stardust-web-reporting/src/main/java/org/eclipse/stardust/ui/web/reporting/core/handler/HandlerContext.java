/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core.handler;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;

public class HandlerContext
{
   private long totalCount;
   private QueryService queryService;
   private RequestColumn column;
   private ScriptEngine engine;

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

   public ScriptEngine getEngine()
   {
      if(engine == null)
      {
         ScriptEngineManager manager = new ScriptEngineManager();
         engine = manager.getEngineByName("JavaScript");
      }

      return engine;
   }
}
