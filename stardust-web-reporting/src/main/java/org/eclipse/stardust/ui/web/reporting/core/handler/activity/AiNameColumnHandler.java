package org.eclipse.stardust.ui.web.reporting.core.handler.activity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.Constants.AiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.ReportParameter;
import org.eclipse.stardust.ui.web.reporting.core.handler.HandlerContext;
import org.eclipse.stardust.ui.web.reporting.core.handler.process.PiNameColumnHandler;

public class AiNameColumnHandler extends AiColumnHandler<String>
{
   public static final String ALL_ACTIVITY_NAMES = "allActivities";

   @Override
   public String provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String provideObjectValue(HandlerContext context, ActivityInstance t)
   {
      return t.getActivity().getName();
   }

   @Override
   public DataField provideDataField(HandlerContext context)
   {
      return new DataField(AiDimensionField.ACTIVITY_NAME.getId(),
            DataFieldType.SHORT_STRING);
   }

   @Override
   public void applyFilter(ActivityInstanceQuery query, ReportFilter filter, ReportParameter parameter)
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

      boolean allProcessesSelected = false;
      List<String> selectedProcesses = filter.getMetadata().getSelectedProcesses();
      if(selectedProcesses != null && selectedProcesses.size() == 1)
      {
         String s = selectedProcesses.get(0);
         if(PiNameColumnHandler.ALL_PROCESS_NAMES.equals(s))
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
            //in case all processes are selected the activty id are not process qualified
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