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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter.OperatorType;
import org.eclipse.stardust.ui.web.reporting.core.Interval;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;
import org.eclipse.stardust.ui.web.reporting.core.Constants.TimeUnit;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;

public abstract class AbstractColumnHandler<T, U, V extends Query> implements IColumnHandler<T, U, V>
{
   @Override
   public Object provideGroupingCriteria(HandlerContext context, U t)
   {
      T result = provideObjectValue(context, t);
      if(result instanceof Date)
      {
         Interval interval = context.getColumn().getInterval();
         if(interval != null)
         {
            Date referenceDate = new Date(0);
            Date endDate = (Date) result;

            long unitValue = interval.getUnitValue();
            if(unitValue == 0)
            {
               unitValue = 1;
            }
            Long diff = ReportingUtil.calculateDuration(referenceDate, endDate, interval.getUnit());
            Double intervalCriteriaDouble = new Double(diff) / new Double(unitValue);
            return intervalCriteriaDouble.longValue();
         }
         else
         {
            return ((Date) result).getTime();
         }
      }

      return result;
   }

   protected FilterCriterion getDateFilterCriterion(FilterableAttribute filterAttribute, Date fromDate, Date toDate)
   {
      FilterCriterion fc = null;
      if(fromDate != null && toDate != null)
      {
         if(fromDate.before(toDate))
         {
            fc = ProcessInstanceQuery.START_TIME.between(fromDate.getTime(), toDate.getTime());
         }
         else if(fromDate.equals(toDate))
         {
            fc = ProcessInstanceQuery.START_TIME.isEqual(fromDate.getTime());
         }
      }
      else if(fromDate != null)
      {
         fc = ProcessInstanceQuery.START_TIME.greaterOrEqual(fromDate.getTime());
      }
      else if(toDate != null)
      {
         fc = ProcessInstanceQuery.START_TIME.lessOrEqual(toDate.getTime());
      }

      return fc;
   }

   protected void raisUnsupportedFilterException(V query, ReportFilter filter)
   {
      StringBuffer errorMsg = new StringBuffer();
      errorMsg.append("Filtering for filter "+filter.getDimension());
      errorMsg.append(" and query type: "+query.getClass().getName());
      errorMsg.append(" is not supported native by the engine yet");
      throw new RuntimeException(errorMsg.toString());
   }

   //TODO: add parameter support
   protected void applyDateFilter(V query, FilterableAttribute filterAttribute, ReportFilter dateFilter, ReportParameter parameter)
   {
      boolean hasFromToSyntax = (dateFilter.getMetadata() == null || dateFilter.getMetadata().isFromTo());
      JsonObject jsonObject = dateFilter.getValue().getAsJsonObject();
      final String from;
      if(parameter != null)
      {
         from = parameter.getFirstValue();
      }
      else
      {
         from = jsonObject.getAsJsonPrimitive("from").getAsString();
      }
      Date fromDate = ReportingUtil.parseDate(from);

      final Date toDate;
      if(hasFromToSyntax)
      {
         final String to;
         if(parameter != null && parameter.getValuesSize() > 1)
         {
            to = parameter.getLastValue();
         }
         else
         {
            to = jsonObject.getAsJsonPrimitive("to").getAsString();
         }

         toDate = ReportingUtil.parseDate(to);
      }
      else
      {
         final int durationUnitValue;
         String durationUnitString = jsonObject.getAsJsonPrimitive("durationUnit").getAsString();
         TimeUnit durationUnit = TimeUnit.parse(durationUnitString);

         if(parameter != null && parameter.getValuesSize() > 1)
         {
            String durationValueAsString = parameter.getLastValue();
            durationUnitValue = Integer.parseInt(durationValueAsString);
         }
         else
         {
            durationUnitValue = jsonObject.getAsJsonPrimitive("duration").getAsInt();
         }

         toDate = ReportingUtil.addDuration(fromDate, durationUnit, durationUnitValue);
      }

      FilterCriterion fc = getDateFilterCriterion(filterAttribute, fromDate, toDate);
      if(fc != null)
      {
         query.where(fc);
      }
   }

   protected void applyOidFilter(V query, FilterableAttribute oidAttribute, ReportFilter oidFilter, ReportParameter parameter)
   {
      final List<Long> oids = new ArrayList<Long>();
      String operator = oidFilter.getOperator();
      OperatorType operatorType = OperatorType.valueOf(operator);
      String rawFilterValue = oidFilter.getValue().getAsJsonPrimitive().getAsString();

      if(parameter != null)
      {
         oids.addAll(parameter.getLongValues());
      }
      else
      {
         oids.addAll(ReportingUtil.getCollectionValues(rawFilterValue));
      }

      final long oid = oids.get(0);
      FilterAndTerm andTerm = query.getFilter().addAndTerm();
      switch(operatorType)
      {
         case LE:
            andTerm.and(oidAttribute.lessOrEqual(oid));
            break;
         case GE:
            andTerm.and(oidAttribute.greaterOrEqual(oid));
            break;
         case E:
            andTerm.and(oidAttribute.isEqual(oid));
            break;
         case NE:
            andTerm.and(oidAttribute.notEqual(oid));
            break;
         case I:
            FilterOrTerm orTerm = andTerm.addOrTerm();
            for(Long tmpOid: oids)
            {
               orTerm.or(oidAttribute.isEqual(tmpOid));
            }
            break;
         case NI:
            for(Long tmpOid: oids)
            {
               andTerm.and(oidAttribute.notEqual(tmpOid));
            }
            break;
         default:
            throw new RuntimeException("Unsupported Operator Type: "+operator);
      }
   }

   @Override
   public boolean canHandle(RequestColumn requestColumn)
   {
      return false;
   }

   @Override
   public boolean canFilter(V query, ReportFilter filter)
   {
      return false;
   }
}


