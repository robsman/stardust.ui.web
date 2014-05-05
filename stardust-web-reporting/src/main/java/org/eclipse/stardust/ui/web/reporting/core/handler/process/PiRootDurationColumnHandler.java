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
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.IFactValueProvider;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;

public class PiRootDurationColumnHandler extends PiColumnHandler<Long> implements IFactValueProvider<ProcessInstance>
{
   private PiDurationColumnHandler delegate;

   public PiRootDurationColumnHandler()
   {
      delegate = new PiDurationColumnHandler();
   }

   @Override
   public Long provideObjectValue(HandlerContext context, ProcessInstance t)
   {
      ProcessInstance rootProcessInstance
         = ReportingUtil.findRootProcessInstance(context.getQueryService(), t);
      return delegate.provideObjectValue(context, rootProcessInstance);
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
   public void applyFilter(ProcessInstanceQuery query, ReportFilter filter)
   {
      raisUnsupportedFilterException(query, filter);
   }

   @Override
   public Long provideFactValue(HandlerContext context, ProcessInstance t)
   {
      ProcessInstance rootProcessInstance
         = ReportingUtil.findRootProcessInstance(context.getQueryService(), t);
      return delegate.provideFactValue(context, rootProcessInstance);
   }

}
