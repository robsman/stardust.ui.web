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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.script.*;

import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;

public abstract class AbstractComputedColumnHandler<U, V extends Query> extends AbstractColumnHandler<Object, U, V>
{
   protected abstract Map<String, IPropertyValueProvider< ? , U>> getScriptingContextInfo(U t);

   @Override
   public Object provideObjectValue(HandlerContext context, U t)
   {
      ScriptEngine engine = context.getEngine();
      String expression = context.getColumn().getComputationFormula();
      ScriptContext scriptingContext = new SimpleScriptContext();
      Bindings scope = scriptingContext.getBindings(ScriptContext.ENGINE_SCOPE);

      Map< String , IPropertyValueProvider< ? , U>> scriptingContextInfo
               = getScriptingContextInfo(t);
      for(String scriptingContextId: scriptingContextInfo.keySet())
      {
         IPropertyValueProvider< ? , U> scriptingValueProvider
            = scriptingContextInfo.get(scriptingContextId);
         Object providerValue = scriptingValueProvider.provideObjectValue(context, t);
         scope.put(scriptingContextId, providerValue);
      }

      try
      {
         return engine.eval(expression, scriptingContext);
      }
      catch (ScriptException e)
      {
         return null;
      }
   }

   @Override
   public Object provideResultSetValue(HandlerContext context, ResultSet rs)
         throws SQLException
   {
      return null;
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return null;
   }

   @Override
   public void applyFilter(V query, ReportFilter filter)
   {
      raisUnsupportedFilterException(query, filter);
   }

   @Override
   public boolean canHandle(RequestColumn requestColumn)
   {
      return requestColumn.isComputed();
   }
}
