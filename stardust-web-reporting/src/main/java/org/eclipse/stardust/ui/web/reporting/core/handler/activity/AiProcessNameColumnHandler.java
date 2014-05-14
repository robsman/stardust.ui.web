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
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;

public class AiProcessNameColumnHandler extends AiColumnHandler<String>
{
   @Override
   public String provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException
   {
      return null;
   }

   @Override
   public String provideObjectValue(HandlerContext context, ActivityInstance t)
   {
      return t.getProcessInstance().getProcessName();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(AiDimensionField.PROCESS_NAME.getId(),
            DataFieldType.SHORT_STRING);
   }

   @Override
   public void applyFilter(ActivityInstanceQuery query, ReportFilter filter, ReportParameter parameter)
   {
      raisUnsupportedFilterException(query, filter);
   }
}
