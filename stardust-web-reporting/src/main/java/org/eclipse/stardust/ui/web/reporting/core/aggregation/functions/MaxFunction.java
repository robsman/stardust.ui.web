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
import org.eclipse.stardust.ui.web.reporting.core.handler.IFactValueProvider;

public class MaxFunction<T> extends CompareFunction<T>
{

   public MaxFunction(QueryService queryService, RequestColumn factColumn,
         IFactValueProvider<T> factProvider)
   {
      super(queryService, factColumn, factProvider);
   }

   @Override
   protected boolean matches(long referenceValue, long currentValue)
   {
      return currentValue > referenceValue;
   }

}
