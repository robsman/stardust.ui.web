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
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.IFactValueProvider;
import org.eclipse.stardust.ui.web.reporting.core.handler.process.PiRootDurationColumnHandler;

public class AiRootProcessDurationColumnHandler extends AiColumnHandler<Long> implements IFactValueProvider<ActivityInstance>
{
   private PiRootDurationColumnHandler delegate;

   public AiRootProcessDurationColumnHandler()
   {
      delegate = new PiRootDurationColumnHandler();
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
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void applyFilter(ActivityInstanceQuery query, ReportFilter filter)
   {
      raisUnsupportedFilterException(query, filter);

   }

   @Override
   public Long provideFactValue(HandlerContext context, ActivityInstance t)
   {
      return delegate.provideObjectValue(context, t.getProcessInstance());
   }
}
