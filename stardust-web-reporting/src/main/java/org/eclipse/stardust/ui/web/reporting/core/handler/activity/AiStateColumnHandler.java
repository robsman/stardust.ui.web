/*******************************************************************************
* Copyright (c) 2014 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Holger.Prause (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.ui.web.reporting.core.handler.activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.Constants.AiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.Constants.FilterConstants;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;

public class AiStateColumnHandler extends AiColumnHandler<ActivityInstanceState>
{
   private Map<String, ActivityInstanceState[]> allAiStates;

   public AiStateColumnHandler()
   {
      allAiStates
         = new HashMap<String, ActivityInstanceState[]>();
      List<ActivityInstanceState> availableStates
         = new ArrayList<ActivityInstanceState>();
      availableStates.add(ActivityInstanceState.Created);
      availableStates.add(ActivityInstanceState.Application);
      availableStates.add(ActivityInstanceState.Completed);
      availableStates.add(ActivityInstanceState.Interrupted);
      availableStates.add(ActivityInstanceState.Suspended);
      availableStates.add(ActivityInstanceState.Aborted);
      availableStates.add(ActivityInstanceState.Aborting);
      availableStates.add(ActivityInstanceState.Hibernated);
      for(ActivityInstanceState state: availableStates)
      {
         allAiStates.put(state.getName(), new ActivityInstanceState[] {state});
      }

      allAiStates.put(FilterConstants.ALIVE.getId(), new ActivityInstanceState[]{ActivityInstanceState.Created, ActivityInstanceState.Application});
   }

   @Override
   public ActivityInstanceState provideResultSetValue(HandlerContext context, ResultSet rs)
         throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ActivityInstanceState provideObjectValue(HandlerContext context, ActivityInstance t)
   {
      return t.getState();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(AiDimensionField.STATE.getId(),
            DataFieldType.NUMBER);
   }

   @Override
   public void applyFilter(ActivityInstanceQuery query, ReportFilter filter)
   {
      List<String> filterValues = filter.getListValues();
      if(filterValues.size() > 0)
      {
         Set<ActivityInstanceState> allFilterStates = new HashSet<ActivityInstanceState>();
         for(int i=0; i< filterValues.size(); i++)
         {
            String filterStateKey = filterValues.get(i);
            ActivityInstanceState[] mappedStates = allAiStates.get(filterStateKey);
            for(ActivityInstanceState ais: mappedStates)
            {
               allFilterStates.add(ais);
            }
         }

         ActivityInstanceState[] criteriaStates
            = allFilterStates.toArray(new ActivityInstanceState[allFilterStates.size()]);
         query.where(new ActivityStateFilter(criteriaStates));
      }
   }
}
