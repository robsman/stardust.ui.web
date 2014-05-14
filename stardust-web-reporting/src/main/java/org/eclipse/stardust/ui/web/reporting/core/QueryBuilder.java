package org.eclipse.stardust.ui.web.reporting.core;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
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
   private static final Logger trace = LogManager.getLogger(QueryBuilder.class);

   private AiColumnHandlerRegistry aiHandlerRegistry;
   private PiColumnHandlerRegistry piHandlerRegistry;
   private Map<String, ReportParameter> parameterMap;

   public QueryBuilder(Map<String, ReportParameter> parameterMap)
   {
      this.parameterMap = parameterMap;
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
         String dimension = filter.getDimension();
         RequestColumn columnKey = new RequestColumn(dimension);
         columnKey.setDescriptor(filter.isDescriptor());

         IFilterHandler<T> filterHandler
            = handlerRegistry.getFilterHandler(query, columnKey, filter);
         ReportParameter rp = parameterMap.get(dimension);
         if(rp != null && filter.isParameterizable() == false)
         {
            StringBuffer warnMsg = new StringBuffer();
            warnMsg.append("Parameter for column").append("'").append(rp.getId()).append("'");
            warnMsg.append(" was provided but the matching filter does not support parameters");
            trace.warn(warnMsg.toString());

            //filter does not support parameter - set it to null
            rp = null;
         }

         filterHandler.applyFilter(query, filter, rp);
      }
   }
}
