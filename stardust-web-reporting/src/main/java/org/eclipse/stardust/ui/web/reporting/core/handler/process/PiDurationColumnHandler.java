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
package org.eclipse.stardust.ui.web.reporting.core.handler.process;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.Constants.TimeUnit;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.IFactValueProvider;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;

public class PiDurationColumnHandler extends PiColumnHandler<Long> implements IFactValueProvider<ProcessInstance>
{
   @Override
   public Long provideObjectValue(HandlerContext context, ProcessInstance t)
   {
      TimeUnit du = context.getColumn().getTimeUnit();
      Date startTime = t.getStartTime();
      Date endTime = t.getTerminationTime();
      if(endTime == null || endTime.getTime() == 0)
      {
         endTime = new Date();
      }

      return ReportingUtil.calculateDuration(startTime,
            endTime, du);
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(PiDimensionField.DURATION.getId(),
            DataFieldType.NUMBER);
   }

   @Override
   public void applyFilter(ProcessInstanceQuery query, ReportFilter filter)
   {
      raisUnsupportedFilterException(query, filter);
   }

   @Override
   public Long provideResultSetValue(HandlerContext context, ResultSet rs)
         throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Long provideFactValue(HandlerContext context, ProcessInstance t)
   {
      return provideObjectValue(context, t);
   }
}
