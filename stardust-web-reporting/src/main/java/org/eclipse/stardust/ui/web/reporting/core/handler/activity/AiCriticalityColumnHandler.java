package org.eclipse.stardust.ui.web.reporting.core.handler.activity;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterableAttribute;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter.OperatorType;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.Constants.AiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;

public class AiCriticalityColumnHandler extends AiColumnHandler<Double>
{
   @Override
   public Double provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException
   {
      return null;
   }

   @Override
   public Double provideObjectValue(HandlerContext context, ActivityInstance t)
   {
      return t.getCriticality();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(AiDimensionField.CRITICALITY.getId(),
            DataFieldType.NUMBER);
   }

   @Override
   public void applyFilter(ActivityInstanceQuery query, ReportFilter filter, ReportParameter parameter)
   {
      String operator = filter.getOperator();
      OperatorType operatorType = OperatorType.valueOf(operator);

      final Number number;
      if(parameter != null)
      {
         number = parameter.getDoubleValue();
      }
      else
      {
         JsonPrimitive jsonPrimitive = filter.getValue().getAsJsonPrimitive();
         number = jsonPrimitive.getAsNumber();
      }

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
}
