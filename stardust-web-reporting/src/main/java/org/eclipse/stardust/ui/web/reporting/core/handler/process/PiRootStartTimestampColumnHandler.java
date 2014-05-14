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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;

public class PiRootStartTimestampColumnHandler extends PiColumnHandler<Date>
{
   @Override
   public Date provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException
   {
      return null;
   }

   @Override
   public Date provideObjectValue(HandlerContext context, ProcessInstance t)
   {
      ProcessInstance pi = ReportingUtil.findRootProcessInstance(context.getQueryService(), t);
      return pi.getStartTime();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(PiDimensionField.ROOT_START_TIMESTAMP.getId(),
            DataFieldType.NUMBER);
   }

   @Override
   public void applyFilter(ProcessInstanceQuery query, ReportFilter filter, ReportParameter parameter)
   {
      raisUnsupportedFilterException(query, filter);
   }
}