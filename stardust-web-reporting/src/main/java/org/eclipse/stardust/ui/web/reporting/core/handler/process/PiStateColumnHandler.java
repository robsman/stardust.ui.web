/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core.handler.process;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessStateFilter;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;

public class PiStateColumnHandler extends PiColumnHandler<Integer>
{
   private Map<String, ProcessInstanceState> allPiStates;

   public PiStateColumnHandler()
   {
      allPiStates = new HashMap<String, ProcessInstanceState>();
      for(ProcessInstanceState state: ProcessInstanceState.getAllStates())
      {
         allPiStates.put(state.getName(), state);
      }
   }

   @Override
   public Integer provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException
   {
      return null;
   }

   @Override
   public Integer provideObjectValue(HandlerContext context, ProcessInstance t)
   {
      return t.getState().getValue();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(PiDimensionField.STATE.getId(),
            DataFieldType.NUMBER);
   }

   @Override
   public void applyFilter(ProcessInstanceQuery query, ReportFilter filter)
   {
      List<String> filterValues = filter.getListValues();
      if(filterValues.size() > 0)
      {
         ProcessInstanceState[] filterStates = new ProcessInstanceState[filterValues.size()];
         for(int i=0; i< filterValues.size(); i++)
         {
            String filterStateName = filterValues.get(i);
            ProcessInstanceState filterState = allPiStates.get(filterStateName);
            filterStates[i] = filterState;
         }

         query.where(new ProcessStateFilter(filterStates));
      }
   }
}
