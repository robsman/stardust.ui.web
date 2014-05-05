package org.eclipse.stardust.ui.web.reporting.core;

import java.util.List;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportDataSet;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.handler.AbstractColumnHandlerRegistry;
import org.eclipse.stardust.ui.web.reporting.core.handler.IFilterHandler;
import org.eclipse.stardust.ui.web.reporting.core.handler.activity.AiColumnHandlerRegistry;
import org.eclipse.stardust.ui.web.reporting.core.handler.process.PiColumnHandlerRegistry;

public class QueryBuilder
{
   private AiColumnHandlerRegistry aiHandlerRegistry;
   private PiColumnHandlerRegistry piHandlerRegistry;


   public QueryBuilder()
   {
      aiHandlerRegistry = new AiColumnHandlerRegistry();
      piHandlerRegistry = new PiColumnHandlerRegistry();
   }

   public ActivityInstanceQuery buildActivityInstanceQuery(ReportDataSet dataSet)
   {
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();
      applyFilters(aiQuery, aiHandlerRegistry, dataSet.getFilters());
      return aiQuery;
   }

   public ProcessInstanceQuery buildProcessInstanceQuery(ReportDataSet dataSet)
   {
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
      applyFilters(piQuery, piHandlerRegistry, dataSet.getFilters());
      return piQuery;
   }


   private <T extends Query> void applyFilters(T query, AbstractColumnHandlerRegistry<?, T> handlerRegistry, List<ReportFilter> filters)
   {
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      for(ReportFilter filter: filters)
      {
         RequestColumn columnKey = new RequestColumn(filter.getDimension());
         columnKey.setDescriptor(filter.isDescriptor());

         IFilterHandler<T> filterHandler = handlerRegistry.getFilterHandler(query, columnKey, filter);
         filterHandler.applyFilter(query, filter);
      }
   }
}
