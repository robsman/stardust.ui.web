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

import java.util.Date;
import java.util.List;

import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter.OperatorType;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;

public abstract class AbstractColumnHandler<T, U, V extends Query> implements IColumnHandler<T, U, V>
{


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

   protected void applyDateFilter(V query, FilterableAttribute filterAttribute, ReportFilter dateFilter)
   {
      JsonObject jsonObject = dateFilter.getValue().getAsJsonObject();
      String from = jsonObject.getAsJsonPrimitive("from").getAsString();
      String to = jsonObject.getAsJsonPrimitive("to").getAsString();

      Date fromDate = JsonUtil.parseDate(from);
      Date toDate = JsonUtil.parseDate(to);

      FilterCriterion fc = getDateFilterCriterion(filterAttribute, fromDate, toDate);
      if(fc != null)
      {
         query.where(fc);
      }
   }

   protected void applyOidFilter(V query, FilterableAttribute oidAttribute, ReportFilter oidFilter)
   {
      String rawFilterValue = oidFilter.getValue().getAsJsonPrimitive().getAsString();

      final long oid;
      final List<Long> oids;

      String operator = oidFilter.getOperator();
      OperatorType operatorType = OperatorType.valueOf(operator);

      FilterAndTerm andTerm = query.getFilter().addAndTerm();
      switch(operatorType)
      {
         case LE:
            oid = ReportingUtil.getLongValue(rawFilterValue);
            andTerm.and(oidAttribute.lessOrEqual(oid));
            break;
         case GE:
            oid = ReportingUtil.getLongValue(rawFilterValue);
            andTerm.and(oidAttribute.greaterOrEqual(oid));
            break;
         case E:
            oid = ReportingUtil.getLongValue(rawFilterValue);
            andTerm.and(oidAttribute.isEqual(oid));
            break;
         case NE:
            oid = ReportingUtil.getLongValue(rawFilterValue);
            andTerm.and(oidAttribute.notEqual(oid));
            break;
         case I:
            FilterOrTerm orTerm = andTerm.addOrTerm();
            oids = ReportingUtil.getCollectionValues(rawFilterValue);
            for(Long tmpOid: oids)
            {
               orTerm.or(oidAttribute.isEqual(tmpOid));
            }
            break;
         case NI:
            oids = ReportingUtil.getCollectionValues(rawFilterValue);
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
      return true;
   }
}


