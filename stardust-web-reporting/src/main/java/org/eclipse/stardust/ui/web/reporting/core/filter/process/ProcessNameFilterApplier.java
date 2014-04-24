package org.eclipse.stardust.ui.web.reporting.core.filter.process;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.Constants;
import org.eclipse.stardust.ui.web.reporting.core.filter.DimensionBasedFilterApplier;

/**
 *
 * @author Holger.Prause
 * @version $Revision: $
 */
public class ProcessNameFilterApplier extends DimensionBasedFilterApplier<ProcessInstanceQuery>
{
   public static final String ALL_PROCESS_NAMES = "allProcesses";

   void apply(Query query, List<String> processNames)
   {
      FilterAndTerm filterContainer = query.getFilter().addAndTerm();
      FilterOrTerm filterOrTerm = filterContainer.addOrTerm();
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
      return Constants.PiDimensionField.PROCESS_NAME.getId();
   }
}