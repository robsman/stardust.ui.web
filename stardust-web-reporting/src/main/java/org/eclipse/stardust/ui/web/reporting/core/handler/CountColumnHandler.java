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

import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.Constants;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;

public class CountColumnHandler<U, V extends Query> extends AbstractColumnHandler<Long, U, V>
{
   @Override
   public Long provideResultSetValue(HandlerContext context, ResultSet rs)
         throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Long provideObjectValue(HandlerContext context, U t)
   {
      return context.getTotalCount();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(Constants.DimensionField.COUNT.getId(), DataFieldType.NUMBER);
   }

   @Override
   public void applyFilter(V query, ReportFilter filter)
   {
      raisUnsupportedFilterException(query, filter);
   }

}
