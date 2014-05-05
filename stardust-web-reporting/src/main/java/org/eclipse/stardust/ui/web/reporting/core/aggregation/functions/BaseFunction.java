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
import org.eclipse.stardust.ui.web.reporting.core.aggregation.IGroupFunction;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.IFactValueProvider;

public abstract class BaseFunction<T> implements IGroupFunction<T>
{
   private RequestColumn factColumn;
   private IFactValueProvider<T> factProvider;
   private QueryService queryService;

   public BaseFunction(QueryService queryService, RequestColumn factColumn, IFactValueProvider<T> factProvider)
   {
      this.queryService = queryService;
      this.factColumn = factColumn;
      this.factProvider = factProvider;
   }

   public RequestColumn getFactColumn()
   {
      return factColumn;
   }

   public IFactValueProvider<T> getFactProvider()
   {
      return factProvider;
   }

   protected HandlerContext getProviderContext()
   {
      HandlerContext context = new HandlerContext(queryService, 0);
      context.setColumn(factColumn);
      return context;
   }

   protected Number getFactValue(T fact)
   {
      Number factValue = factProvider.provideFactValue(getProviderContext(), fact);
      if(factValue != null)
      {
         return factValue;
      }

      return 0;
   }
}
