package org.eclipse.stardust.ui.web.reporting.core.filter.activity;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilterMetaData;
import org.eclipse.stardust.ui.web.reporting.core.filter.DimensionBasedFilterApplier;

public class CriticalityFilterApplier extends DimensionBasedFilterApplier<ActivityInstanceQuery>
{
   @Override
   public void apply(ActivityInstanceQuery query, ReportFilter filter)
   {
      ReportFilterMetaData metaData = filter.getMetadata();
      Number rangeFrom = metaData.getRangeFrom();
      Number rangeTo = metaData.getRangeTo();

      FilterCriterion criticalityFilterCriterion
         = ActivityInstanceQuery.CRITICALITY.between(rangeFrom.doubleValue(), rangeTo.doubleValue());
      query.getFilter().and(criticalityFilterCriterion);
   }

   @Override
   protected String getMatchDimension()
   {
      return "criticality";
   }
}
