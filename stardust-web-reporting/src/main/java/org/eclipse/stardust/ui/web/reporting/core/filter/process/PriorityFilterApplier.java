package org.eclipse.stardust.ui.web.reporting.core.filter.process;

import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery.Attribute;
import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter.OperatorType;
import org.eclipse.stardust.ui.web.reporting.core.filter.FilterApplier;

public class PriorityFilterApplier extends FilterApplier<ProcessInstanceQuery>
{

   @Override
   public void apply(ProcessInstanceQuery query, ReportFilter filter)
   {
      long priorityValue = JsonUtil.getPrimitiveValueAsLong(filter.getValue());

      String operator = filter.getOperator();
      OperatorType operatorType = OperatorType.valueOf(operator);

      Attribute priorityAttribute = ProcessInstanceQuery.PRIORITY;
      final FilterCriterion filterCriterion;
      switch(operatorType)
      {
         case E:
            filterCriterion = priorityAttribute.isEqual(priorityValue);
            break;
         case LE:
            filterCriterion = priorityAttribute.lessOrEqual(priorityValue);
            break;
         case GE:
            filterCriterion = priorityAttribute.greaterOrEqual(priorityValue);
            break;
         case NE:
            filterCriterion = priorityAttribute.notEqual(priorityValue);
            break;

         default:
            throw new RuntimeException("Unsupported Operator Type: "+operator);
      }

      query.where(filterCriterion);
   }

}
