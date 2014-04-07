package org.eclipse.stardust.ui.web.reporting.core.filter.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.filter.FilterApplier;
import org.eclipse.stardust.ui.web.reporting.core.filter.process.ProcessNameFilterApplier;

public class ActivityNameFilterApplier extends FilterApplier<ActivityInstanceQuery>
{
   public static final String ALL_ACTIVITY_NAMES = "allActivities";

   @Override
   public void apply(ActivityInstanceQuery query, ReportFilter filter)
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

      boolean allProcessesSelected = false;
      List<String> selectedProcesses = filter.getMetadata().getSelectedProcesses();
      if(selectedProcesses != null && selectedProcesses.size() == 1)
      {
         String s = selectedProcesses.get(0);
         if(ProcessNameFilterApplier.ALL_PROCESS_NAMES.equals(s))
         {
            allProcessesSelected = true;
         }
      }

      FilterOrTerm filterOrTerm = query.getFilter().addOrTerm();
      for(String activityName: allFilterValues)
      {
         if(!ALL_ACTIVITY_NAMES.equals(activityName))
         {
            final String activityProcessID;
            final String activityID;
            final FilterCriterion filterCriterion;
            //in case all processed are selected the activty id are not process qualified
            //take care of this
            if(allProcessesSelected)
            {
               activityID = activityName;
               filterCriterion = ActivityFilter.forAnyProcess(activityID);
            }
            //split the full qualified activity name in format
            //FullQualifiedProcessId:FullQualifiedActivityId
            else
            {
               StringTokenizer st = new StringTokenizer(activityName, ":");
               activityProcessID = st.nextToken();
               activityID = st.nextToken();
               filterCriterion = ActivityFilter.forProcess(activityID, activityProcessID);
            }

            filterOrTerm.or(filterCriterion);
         }
      }
   }
}