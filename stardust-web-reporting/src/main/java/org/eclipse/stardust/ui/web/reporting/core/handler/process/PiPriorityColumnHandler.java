package org.eclipse.stardust.ui.web.reporting.core.handler.process;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery.Attribute;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter.OperatorType;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;

public class PiPriorityColumnHandler extends PiColumnHandler<Integer>
{
   @Override
   public Integer provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Integer provideObjectValue(HandlerContext context, ProcessInstance t)
   {
      return t.getPriority();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(PiDimensionField.PRIORITY.getId(),
            DataFieldType.NUMBER);
   }

   @Override
   public void applyFilter(ProcessInstanceQuery query, ReportFilter filter, ReportParameter parameter)
   {
      final long priorityValue;
      if(parameter != null)
      {
         priorityValue = parameter.getLongValue();
      }
      else
      {
         priorityValue = JsonUtil.getPrimitiveValueAsLong(filter.getValue());
      }

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
