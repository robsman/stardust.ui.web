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
import org.eclipse.stardust.ui.web.reporting.core.Constants;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;

public abstract class AbstractColumnHandlerRegistry<U, V extends Query>
{
   private Map<String, IColumnHandler< ? , U, V>> fixColumnHandler;

   private List<IColumnHandler< ? , U, V>> dynamicColumnHandler;

   public AbstractColumnHandlerRegistry()
   {
      this.fixColumnHandler = new HashMap<String, IColumnHandler<?,U,V>>();
      this.dynamicColumnHandler = new ArrayList<IColumnHandler<?,U,V>>();
      register(Constants.DimensionField.COUNT.getId(), new CountColumnHandler<U, V>());
   }

   public void register(String key, IColumnHandler< ? , U, V> handler)
   {
      fixColumnHandler.put(key, handler);
   }

   protected void register(IColumnHandler< ? , U, V> handler)
   {
      dynamicColumnHandler.add(handler);
   }

   protected IColumnHandler< ? , U, V> getColumnHandler(RequestColumn column)
   {
      String columnId = column.getId();
      IColumnHandler< ? , U, V> columnHandler = fixColumnHandler.get(columnId);
      if (columnHandler == null)
      {
         for (IColumnHandler< ? , U, V> ic : dynamicColumnHandler)
         {
            if (ic.canHandle(column))
            {
               columnHandler = ic;
               break;
            }
         }
      }

      if (columnHandler == null)
      {
         StringBuilder errorMsg = new StringBuilder();
         errorMsg.append(" No column handler found for column ");
         errorMsg.append(column.toString());

         throw new RuntimeException(errorMsg.toString());
      }

      return columnHandler;
   }

   public IFilterHandler<V> getFilterHandler(RequestColumn column)
   {
      return getColumnHandler(column);
   }

   public IMappingHandler<?, U> getMappingHandler(RequestColumn column)
   {
      return getColumnHandler(column);
   }
}
