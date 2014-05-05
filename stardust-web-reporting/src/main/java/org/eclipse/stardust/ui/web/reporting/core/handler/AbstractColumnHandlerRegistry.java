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
package org.eclipse.stardust.ui.web.reporting.core.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;
import org.eclipse.stardust.ui.web.reporting.core.aggregation.IGroupingValueProvider;

public abstract class AbstractColumnHandlerRegistry<U, V extends Query>
{
   private Map<String, IColumnHandler< ? , U, V>> fixColumnHandler;

   private List<IColumnHandler< ? , U, V>> dynamicHandler;

   public AbstractColumnHandlerRegistry()
   {
      this.fixColumnHandler = new HashMap<String, IColumnHandler< ? , U, V>>();
      this.dynamicHandler = new ArrayList<IColumnHandler< ? , U, V>>();
   }

   public void register(String key, IColumnHandler< ? , U, V> handler)
   {
      fixColumnHandler.put(key, handler);
   }

   public void register(IColumnHandler< ? , U, V> handler)
   {
      dynamicHandler.add(handler);
   }

   public IColumnHandler< ? , U, V> getColumnHandler(RequestColumn column)
   {
      String columnId = column.getId();
      IColumnHandler< ? , U, V> columnHandler = fixColumnHandler.get(columnId);
      if (columnHandler != null)
      {
         return columnHandler;
      }
      else
      {
         for (IColumnHandler< ? , U, V> dh : dynamicHandler)
         {
            if (dh.canHandle(column))
            {
               return columnHandler;
            }
         }
      }

      StringBuilder errorMsg = new StringBuilder();
      errorMsg.append(" No column handler found for column ");
      errorMsg.append(column.toString());
      throw new RuntimeException(errorMsg.toString());
   }

   public IFilterHandler<V> getFilterHandler(V query, RequestColumn column,
         ReportFilter filter)
   {
      String columnId = column.getId();
      IColumnHandler< ? , U, V> columnHandler = fixColumnHandler.get(columnId);
      if (columnHandler != null)
      {
         return columnHandler;
      }
      else
      {
         for (IColumnHandler< ? , U, V> dh : dynamicHandler)
         {
            if (dh.canFilter(query, filter))
            {
               return dh;
            }
         }
      }

      StringBuilder errorMsg = new StringBuilder();
      errorMsg.append(" No column handler found for column ");
      errorMsg.append(column.toString());
      throw new RuntimeException(errorMsg.toString());
   }

   public IPropertyValueProvider< ? , U> getPropertyValueProvider(RequestColumn column)
   {
      return getColumnHandler(column);
   }

   public IGroupingValueProvider<U> getGroupingValueProvider(RequestColumn column)
   {
      return getColumnHandler(column);
   }

   @SuppressWarnings("unchecked")
   public IFactValueProvider<U> getFactValueProvider(RequestColumn column)
   {
      IColumnHandler< ? , U, V> columnHandler = getColumnHandler(column);
      if (columnHandler instanceof IFactValueProvider)
      {
         return (IFactValueProvider<U>) columnHandler;
      }
      else
      {
         StringBuilder errorMsg = new StringBuilder();
         errorMsg.append(" Requested column ");
         errorMsg.append(column.getId());
         errorMsg.append(" is not fact column!");
         throw new RuntimeException(errorMsg.toString());
      }
   }
}
