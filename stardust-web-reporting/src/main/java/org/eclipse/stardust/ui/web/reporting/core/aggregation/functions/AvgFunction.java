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
package org.eclipse.stardust.ui.web.reporting.core.aggregation.functions;

import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;
import org.eclipse.stardust.ui.web.reporting.core.aggregation.ValueGroup;
import org.eclipse.stardust.ui.web.reporting.core.handler.IFactValueProvider;

public class AvgFunction <T> extends BaseFunction<T>
{
   public AvgFunction(QueryService queryService, RequestColumn factColumn, IFactValueProvider<T> factProvider)
   {
      super(queryService, factColumn, factProvider);
   }

   @Override
   public Number apply(ValueGroup<T> group)
   {
      if(group.getSize() > 0)
      {
         double totalSum = 0.0;
         for(T value: group.values)
         {
            Number fact = getFactValue(value);
            totalSum += fact.doubleValue();
         }

         return (totalSum / group.getSize());
      }

      return 0.0;
   }
}
