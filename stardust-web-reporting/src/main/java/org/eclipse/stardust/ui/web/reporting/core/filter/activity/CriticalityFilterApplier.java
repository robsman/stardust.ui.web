package org.eclipse.stardust.ui.web.reporting.core.filter.activity;

import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterableAttribute;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter.OperatorType;
import org.eclipse.stardust.ui.web.reporting.core.Constants;
import org.eclipse.stardust.ui.web.reporting.core.filter.DimensionBasedFilterApplier;

public class CriticalityFilterApplier extends DimensionBasedFilterApplier<ActivityInstanceQuery>
{
   @Override
   public void apply(ActivityInstanceQuery query, ReportFilter filter)
   {
      String operator = filter.getOperator();
      OperatorType operatorType = OperatorType.valueOf(operator);

      JsonPrimitive jsonPrimitive = filter.getValue().getAsJsonPrimitive();
      Number number = jsonPrimitive.getAsNumber();

      FilterCriterion filterCriterion;
      FilterableAttribute criticalityAttribute = ActivityInstanceQuery.CRITICALITY;
      switch(operatorType)
      {
         case E:
            filterCriterion = criticalityAttribute.isEqual(number.doubleValue());
            break;
         case LE:
            filterCriterion = criticalityAttribute.lessOrEqual(number.doubleValue());
            break;
         case GE:
            filterCriterion = criticalityAttribute.greaterOrEqual(number.doubleValue());
            break;
         case NE:
            filterCriterion = criticalityAttribute.notEqual(number.doubleValue());
            break;

         default:
            throw new RuntimeException("Unsupported Operator Type: "+operator);
      }
      query.getFilter().and(filterCriterion);
   }

   @Override
   protected String getMatchDimension()
   {
      return Constants.AiDimensionField.CRITICALITY.getId();
   }
}
