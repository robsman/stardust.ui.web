/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core.handler.process;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessStateFilter;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.Constants.FilterConstants;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;

public class PiStateColumnHandler extends PiColumnHandler<ProcessInstanceState>
{
   private Map<String, ProcessInstanceState[]> allPiStates;

   public PiStateColumnHandler()
   {
      allPiStates = new HashMap<String, ProcessInstanceState[]>();

      List<ProcessInstanceState> availableStates = new ArrayList<ProcessInstanceState>();
      availableStates.add(ProcessInstanceState.Created);
      availableStates.add(ProcessInstanceState.Active);
      availableStates.add(ProcessInstanceState.Completed);
      availableStates.add(ProcessInstanceState.Interrupted);
      availableStates.add(ProcessInstanceState.Aborting);
      availableStates.add(ProcessInstanceState.Aborted);
      for(ProcessInstanceState state: availableStates)
      {
         allPiStates.put(state.getName(), new ProcessInstanceState[] {state});
      }

      allPiStates.put(FilterConstants.ALIVE.getId(), new ProcessInstanceState[] {ProcessInstanceState.Created, ProcessInstanceState.Active});
   }

   @Override
   public ProcessInstanceState provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException
   {
      return null;
   }

   @Override
   public ProcessInstanceState provideObjectValue(HandlerContext context, ProcessInstance t)
   {
      return t.getState();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(PiDimensionField.STATE.getId(),
            DataFieldType.NUMBER);
   }

   @Override
   public void applyFilter(ProcessInstanceQuery query, ReportFilter filter, ReportParameter parameter)
   {
      final List<String> filterStates;
      if(parameter != null)
      {
         filterStates = new ArrayList<String>(parameter.getAllValues());
      }
      else
      {
        filterStates = filter.getListValues();
      }

      if(filterStates.size() > 0)
      {
         Set<ProcessInstanceState> allFilterStates = new HashSet<ProcessInstanceState>();
         for(int i=0; i< filterStates.size(); i++)
         {
            String filterStateName = filterStates.get(i);
            ProcessInstanceState[] mappedStates = allPiStates.get(filterStateName);
            for(ProcessInstanceState pis: mappedStates)
            {
               allFilterStates.add(pis);
            }
         }

         ProcessInstanceState[] criteriaStates = allFilterStates.toArray(new ProcessInstanceState[allFilterStates.size()]);
         query.where(new ProcessStateFilter(criteriaStates));
      }
   }
}
