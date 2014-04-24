package org.eclipse.stardust.ui.web.reporting.core.filter;

import java.util.Date;

import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterableAttribute;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;

public abstract class FilterApplier<T extends Query>
{
   public abstract boolean canApply(T query, ReportFilter filter);
   public abstract void apply(T query, ReportFilter filter);

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

   protected void applyDateFilter(T query, FilterableAttribute filterAttribute, ReportFilter dateFilter)
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
}