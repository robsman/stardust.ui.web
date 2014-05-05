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
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.IFactValueProvider;

public class StdDevFunction<T> extends BaseFunction<T>
{
   public StdDevFunction(QueryService queryService, RequestColumn factColumn, IFactValueProvider<T> factProvider)
   {
      super(queryService, factColumn, factProvider);
   }

   @Override
   public Double apply(ValueGroup<T> group)
   {
      HandlerContext providerContext = getProviderContext();
      RequestColumn factColumn = getFactColumn();
      IFactValueProvider<T> factProvider = getFactProvider();
      AvgFunction<T> avgFunction = new AvgFunction<T>(providerContext.getQueryService(), factColumn, factProvider);

      if(group.getSize() > 1)
      {
         double avg = avgFunction.apply(group).doubleValue();
         double squareDiffSum = 0;
         for(T value : group.values)
         {
            double factValue = getFactValue(value).doubleValue();
            double diff = factValue - avg;
            double squareDiff = Math.pow(diff, 2);
            squareDiffSum += squareDiff;
         }
         double result = squareDiffSum / (group.values.size() - 1);
         double standardDeviation = Math.sqrt(result);
         return new Double(standardDeviation);
      }

      return 0.0;
   }
}