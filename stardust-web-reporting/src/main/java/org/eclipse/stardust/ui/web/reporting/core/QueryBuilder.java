package org.eclipse.stardust.ui.web.reporting.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportDataSet;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.filter.FilterApplier;
import org.eclipse.stardust.ui.web.reporting.core.filter.activity.ActivityNameFilterApplier;
import org.eclipse.stardust.ui.web.reporting.core.filter.process.PriorityFilterApplier;
import org.eclipse.stardust.ui.web.reporting.core.filter.process.ProcessNameFilterApplier;

public class QueryBuilder
{
   private Map<String, FilterApplier<ActivityInstanceQuery>> aiFilterAppliers;
   private Map<String, FilterApplier<ProcessInstanceQuery>> piFilterAppliers;

   public QueryBuilder()
   {
      aiFilterAppliers = new HashMap<String, FilterApplier<ActivityInstanceQuery>>();
      piFilterAppliers = new HashMap<String, FilterApplier<ProcessInstanceQuery>>();

      aiFilterAppliers.put("activityName", new ActivityNameFilterApplier());
      piFilterAppliers.put("processName", new ProcessNameFilterApplier());
      piFilterAppliers.put("priority", new PriorityFilterApplier());
   }

   public ActivityInstanceQuery buildActivityInstanceQuery(ReportDataSet dataSet)
   {
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();
      prepareQuery(aiQuery);
      applyFilter(aiQuery, aiFilterAppliers, dataSet.getFilters());
      return aiQuery;
   }

   public ProcessInstanceQuery buildProcessInstanceQuery(ReportDataSet dataSet)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
      prepareQuery(piQuery);
      applyFilter(piQuery, piFilterAppliers, dataSet.getFilters());
      return piQuery;
   }

   private void prepareQuery(Query query)
   {
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
   }

   private <T extends Query> void applyFilter(T query, Map<String, FilterApplier<T>> appliers, List<ReportFilter> filters)
   {
      for(ReportFilter filter: filters)
      {
         String column = filter.getDimension();
         FilterApplier<T> applier = appliers.get(column);

         // TODO: throw exception when not filter applier can be found for column type
         if(applier != null)
         {
            applier.apply(query, filter);
         }
      }
   }
}
