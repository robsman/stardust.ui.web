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
import java.util.Date;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.Constants.AiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;

public class AiLastModificationTimeStampColumnHandler extends AiColumnHandler<Date>
{
   @Override
   public Date provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Date provideObjectValue(HandlerContext context, ActivityInstance t)
   {
      return t.getLastModificationTime();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(AiDimensionField.LAST_MODIFICATION_TIMESTAMP
            .getId(), DataFieldType.NUMBER);
   }

   @Override
   public void applyFilter(ActivityInstanceQuery query, ReportFilter filter, ReportParameter parameter)
   {
      applyDateFilter(query, ActivityInstanceQuery.LAST_MODIFICATION_TIME, filter, parameter);
   }
}
