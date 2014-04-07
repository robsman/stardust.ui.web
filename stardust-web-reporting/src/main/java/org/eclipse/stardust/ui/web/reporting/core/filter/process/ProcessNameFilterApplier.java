package org.eclipse.stardust.ui.web.reporting.core.filter.process;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.filter.FilterApplier;

public class ProcessNameFilterApplier extends FilterApplier<ProcessInstanceQuery>
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
}