/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core.filter.process;

import java.util.*;

import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.Constants;
import org.eclipse.stardust.ui.web.reporting.core.filter.DimensionBasedFilterApplier;

public class ProcessStateFilterApplier extends DimensionBasedFilterApplier<ProcessInstanceQuery>
{
   private Map<String, ProcessInstanceState> allPiStates;

   public ProcessStateFilterApplier()
   {
      allPiStates = new HashMap<String, ProcessInstanceState>();
      for(ProcessInstanceState state: ProcessInstanceState.getAllStates())
      {
         allPiStates.put(state.getName(), state);
      }
   }

   @Override
   protected String getMatchDimension()
   {
      return Constants.PiDimensionField.STATE.getId();
   }

   @Override
   public void apply(ProcessInstanceQuery query, ReportFilter filter)
   {
      List<String> filterValues = filter.getListValues();
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
