/*******************************************************************************
* Copyright (c) 2014 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Holger.Prause (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.ui.web.reporting.core.aggregation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.AbstractQueryResult;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.reporting.core.GroupColumn;
import org.eclipse.stardust.ui.web.reporting.core.handler.AbstractColumnHandlerRegistry;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.IColumnHandler;

public class ValueAggregator<T>
{
   private AbstractQueryResult<T> results;
   private AbstractColumnHandlerRegistry<T, ? extends Query> handlerRegistry;
   private Map<AbstractGroupKey<T>, ValueGroup<T>> aggregationMap;
   private QueryService queryService;
   private List<GroupColumn> groupColumns;

   public ValueAggregator(
         QueryService queryService,
         AbstractQueryResult<T> results,
         List<GroupColumn> groupColumns,
         AbstractColumnHandlerRegistry<T, ? extends Query> handlerRegistry)
   {
      this.queryService = queryService;
      this.groupColumns = groupColumns;
      this.handlerRegistry = handlerRegistry;
      this.results = results;
      this.aggregationMap = new HashMap<AbstractGroupKey<T>, ValueGroup<T>>();
   }

   public Map<AbstractGroupKey<T>, ValueGroup<T>> aggregate()
   {
      HandlerContext ctx = new HandlerContext(queryService, results.size());
      for(T result: results)
      {
         AbstractGroupKey<T> groupKey = getGroupKey(result);
         for(GroupColumn gc: groupColumns)
         {
            IColumnHandler< ? , T, ? extends Query>
               columnHandler = handlerRegistry.getColumnHandler(gc);
            //set context data
            ctx.setColumn(gc);
            groupKey.addCriteria(columnHandler.provideGroupingCriteria(ctx, result));
         }

         ValueGroup<T> valueGroup = aggregationMap.get(groupKey);
         if(valueGroup == null)
         {
            valueGroup = new ValueGroup<T>();
            aggregationMap.put(groupKey, valueGroup);
         }

         valueGroup.add(result);
      }

      return aggregationMap;
   }

   private AbstractGroupKey<T> getGroupKey(T criteriaEntity)
   {
      if(this.groupColumns.isEmpty())
      {
         return new IdentityGroupKey<T>(criteriaEntity);
      }

      return new ValueGroupKey<T>(criteriaEntity);
   }
}
