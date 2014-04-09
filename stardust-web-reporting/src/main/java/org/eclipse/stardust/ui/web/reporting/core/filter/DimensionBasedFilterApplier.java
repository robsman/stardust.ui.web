package org.eclipse.stardust.ui.web.reporting.core.filter;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;

public abstract class DimensionBasedFilterApplier<T extends Query> extends FilterApplier<T>
{
   @Override
   public boolean canApply(T query, ReportFilter filter)
   {
      String dimension = filter.getDimension();
      String matchDimension = getMatchDimension();

      if(CompareHelper.areEqual(dimension, matchDimension))
      {
         return true;
      }

      return false;
   }

   protected abstract String getMatchDimension();
}
