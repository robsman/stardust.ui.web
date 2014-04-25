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
package org.eclipse.stardust.ui.web.reporting.core.filter.activity;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.Constants;
import org.eclipse.stardust.ui.web.reporting.core.filter.DimensionBasedFilterApplier;

public class ActivityRootProcessStartTimestampFilterApplier extends DimensionBasedFilterApplier<ActivityInstanceQuery>
{

   @Override
   protected String getMatchDimension()
   {
      return Constants.AiDimensionField.PROCESS_INSTANCE_ROOT_START_TIMESTAMP.getId();
   }

   @Override
   public void apply(ActivityInstanceQuery query, ReportFilter filter)
   {
      raisUnsupportedFilterException(query, filter);
   }
}
