package org.eclipse.stardust.ui.web.reporting.core.handler.process;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;

/**
 *
 * @author Holger.Prause
 * @version $Revision: $
 */
public class PiNameColumnHandler extends PiColumnHandler<String>
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
   public void applyFilter(ProcessInstanceQuery query, ReportFilter filter, ReportParameter parameter)
   {
      List<String> allFilterValues = new ArrayList<String>();
      if(parameter != null)
      {
         allFilterValues.addAll(parameter.getAllValues());
      }
      else
      {
         if(filter.isSingleValue())
         {
            allFilterValues.add(filter.getSingleValue());
         }

         if(filter.isListValue())
         {
            allFilterValues.addAll(filter.getListValues());
         }
      }

      apply(query, allFilterValues);
   }

   @Override
   public String provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException
   {
      return null;
   }

   @Override
   public String provideObjectValue(HandlerContext context, ProcessInstance t)
   {
      return t.getProcessName();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(PiDimensionField.PROCESS_NAME.getId(),
            DataFieldType.SHORT_STRING);
   }
}