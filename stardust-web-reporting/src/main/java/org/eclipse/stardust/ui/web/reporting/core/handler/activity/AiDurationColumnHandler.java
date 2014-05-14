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
import org.eclipse.stardust.ui.web.reporting.core.Constants.AiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.Constants.TimeUnit;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.IFactValueProvider;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;

public class AiDurationColumnHandler extends AiColumnHandler<Long> implements IFactValueProvider<ActivityInstance>
{
   @Override
   public Long provideResultSetValue(HandlerContext context, ResultSet rs)
         throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Long provideObjectValue(HandlerContext context, ActivityInstance t)
   {
      TimeUnit du = context.getColumn().getTimeUnit();
      return ReportingUtil.calculateDuration(t.getStartTime(),
            t.getLastModificationTime(), du);
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(AiDimensionField.DURATION.getId(),
            DataFieldType.NUMBER);
   }

   @Override
   public void applyFilter(ActivityInstanceQuery query, ReportFilter filter, ReportParameter parameter)
   {
      raisUnsupportedFilterException(query, filter);
   }

   @Override
   public Long provideFactValue(HandlerContext context, ActivityInstance t)
   {
      return provideObjectValue(context, t);
   }
}
