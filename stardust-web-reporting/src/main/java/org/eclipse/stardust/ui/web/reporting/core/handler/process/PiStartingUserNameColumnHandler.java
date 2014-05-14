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

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;

public class PiStartingUserNameColumnHandler extends PiColumnHandler<String>
{
   @Override
   public String provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException
   {
      return null;
   }

   @Override
   public String provideObjectValue(HandlerContext context, ProcessInstance t)
   {
      User startingUser = t.getStartingUser();
      if(startingUser != null)
      {
         return startingUser.getName();
      }

      return null;
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(PiDimensionField.STARTING_USER_NAME.getId(),
            DataFieldType.SHORT_STRING);
   }

   @Override
   public void applyFilter(ProcessInstanceQuery query, ReportFilter filter, ReportParameter parameter)
   {
      throw new RuntimeException("awaiting change request for passing user oid");
   }
}
