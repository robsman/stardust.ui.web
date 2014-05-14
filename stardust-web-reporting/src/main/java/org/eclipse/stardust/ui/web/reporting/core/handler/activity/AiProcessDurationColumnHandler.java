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
package org.eclipse.stardust.ui.web.reporting.core.handler.activity;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.IFactValueProvider;
import org.eclipse.stardust.ui.web.reporting.core.handler.process.PiDurationColumnHandler;

public class AiProcessDurationColumnHandler extends AiColumnHandler<Long> implements IFactValueProvider<ActivityInstance>
{
   private PiDurationColumnHandler delegate;

   public AiProcessDurationColumnHandler()
   {
      delegate = new PiDurationColumnHandler();
   }

   @Override
   public Long provideObjectValue(HandlerContext context, ActivityInstance t)
   {
      return delegate.provideObjectValue(context, t.getProcessInstance());
   }

   @Override
   public Long provideResultSetValue(HandlerContext context, ResultSet rs)
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
   public void applyFilter(ActivityInstanceQuery query, ReportFilter filter, ReportParameter parameter)
   {
      raisUnsupportedFilterException(query, filter);
   }

   @Override
   public Long provideFactValue(HandlerContext context, ActivityInstance t)
   {
      return delegate.provideFactValue(context, t.getProcessInstance());
   }
}
