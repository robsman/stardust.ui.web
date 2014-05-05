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

public abstract class CompareFunction<T> extends BaseFunction<T>
{
   public CompareFunction(QueryService queryService, RequestColumn factColumn, IFactValueProvider<T> factProvider)
   {
      super(queryService, factColumn, factProvider);
   }

   @Override
   public Number apply(ValueGroup<T> group)
   {
      Long referenceValue = null;
      for(T groupValue: group.getValues())
      {
         Number factValue = getFactValue(groupValue);
         if(factValue != null)
         {
            long currentFactValue = factValue.longValue();
            if(referenceValue == null)
            {
               referenceValue = currentFactValue;
            }
            else if (matches(referenceValue, currentFactValue))
            {
               referenceValue = currentFactValue;
            }
         }
      }

      if(referenceValue == null)
      {
         referenceValue = 0L;
      }

      return referenceValue;
   }

   protected abstract boolean matches(long referenceValue, long currentValue);
}
