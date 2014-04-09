package org.eclipse.stardust.ui.web.reporting.core.filter.process;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.filter.DimensionBasedFilterApplier;

public class ProcessNameFilterApplier extends DimensionBasedFilterApplier<ProcessInstanceQuery>
{
   public static final String ALL_PROCESS_NAMES = "allProcesses";

   void apply(Query query, List<String> processNames)
   {
      FilterOrTerm filterOrTerm = query.getFilter().addOrTerm();
      for(String processName: processNames)
      {
         if(!ALL_PROCESS_NAMES.equals(processName))
         {
            ProcessDefinitionFilter processDefinitionFilter
               = new ProcessDefinitionFilter(processName);
            filterOrTerm.add(processDefinitionFilter);
         }
      }
   }

   @Override
   public void apply(ProcessInstanceQuery query, ReportFilter filter)
   {
      List<String> allFilterValues = new ArrayList<String>();
      if(filter.isSingleValue())
      {
         allFilterValues.add(filter.getSingleValue());
      }

      if(filter.isListValue())
      {
         allFilterValues.addAll(filter.getListValues());
      }

      apply(query, allFilterValues);
   }

   @Override
   protected String getMatchDimension()
   {
      return "processName";
   }
}