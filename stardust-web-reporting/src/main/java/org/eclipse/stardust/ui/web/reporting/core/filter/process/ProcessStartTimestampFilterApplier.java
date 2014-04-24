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
package org.eclipse.stardust.ui.web.reporting.core.filter.process;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.Constants;
import org.eclipse.stardust.ui.web.reporting.core.filter.DimensionBasedFilterApplier;

public class ProcessStartTimestampFilterApplier extends DimensionBasedFilterApplier<ProcessInstanceQuery>
{
   @Override
   protected String getMatchDimension()
   {
      return Constants.PiDimensionField.START_TIMESTAMP.getId();
   }

   @Override
   public void apply(ProcessInstanceQuery query, ReportFilter filter)
   {
      applyDateFilter(query, ProcessInstanceQuery.START_TIME, filter);
   }
}
