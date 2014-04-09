package org.eclipse.stardust.ui.web.reporting.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportDataSet;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.filter.DescriptorFilterApplier;
import org.eclipse.stardust.ui.web.reporting.core.filter.FilterApplier;
import org.eclipse.stardust.ui.web.reporting.core.filter.activity.ActivityNameFilterApplier;
import org.eclipse.stardust.ui.web.reporting.core.filter.activity.CriticalityFilterApplier;
import org.eclipse.stardust.ui.web.reporting.core.filter.process.PriorityFilterApplier;
import org.eclipse.stardust.ui.web.reporting.core.filter.process.ProcessNameFilterApplier;

public class QueryBuilder
{
   private List<FilterApplier<ActivityInstanceQuery>> aiFilterAppliers;
   private List<FilterApplier<ProcessInstanceQuery>> piFilterAppliers;
   private List<FilterApplier<Query>> genericFilterAppliers;

   public QueryBuilder()
   {
      aiFilterAppliers = new ArrayList<FilterApplier<ActivityInstanceQuery>>();
      aiFilterAppliers.add(new ActivityNameFilterApplier());
      aiFilterAppliers.add(new CriticalityFilterApplier());

      piFilterAppliers = new ArrayList<FilterApplier<ProcessInstanceQuery>>();
      piFilterAppliers.add(new ProcessNameFilterApplier());
      piFilterAppliers.add(new PriorityFilterApplier());

      genericFilterAppliers = new ArrayList<FilterApplier<Query>>();
      genericFilterAppliers.add(new DescriptorFilterApplier());
   }

   public ActivityInstanceQuery buildActivityInstanceQuery(ReportDataSet dataSet)
   {
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();
      prepareQuery(aiQuery, dataSet);
      return aiQuery;
   }

   public ProcessInstanceQuery buildProcessInstanceQuery(ReportDataSet dataSet)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
      prepareQuery(piQuery, dataSet);
      return piQuery;
   }

   private void prepareQuery(Query query, ReportDataSet dataSet)
   {
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      List<ReportFilter> filters = dataSet.getFilters();

      //apply filters - if any present
      if(filters != null && !filters.isEmpty())
      {
         boolean filterFound = false;
         if(query instanceof ActivityInstanceQuery)
         {
            ActivityInstanceQuery aiQuery = (ActivityInstanceQuery) query;
            filterFound = filterFound && applyFilter(aiQuery, aiFilterAppliers, filters);
         }

         else if(query instanceof ProcessInstanceQuery)
         {
            ProcessInstanceQuery piQuery = (ProcessInstanceQuery) query;
            applyFilter(piQuery, piFilterAppliers, filters);
         }

         applyFilter(query, genericFilterAppliers, filters);
      }
   }

   private <T extends Query> boolean applyFilter(T query, List<FilterApplier<T>> appliers, List<ReportFilter> filters)
   {
      for(ReportFilter filter: filters)
      {
         for(FilterApplier<T> applier: appliers)
         {
            if(applier.canApply(query, filter))
            {
               applier.apply(query, filter);
               return true;
            }
         }
      }

      return false;
   }
}
