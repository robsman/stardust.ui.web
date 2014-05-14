package org.eclipse.stardust.ui.mobile.service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;

/**
 * @author Shrikant.Gangal
 *
 */
public class ActivitySearchHelper
{
   /**
    * @author Shrikant.Gangal
    *
    */
   public static class ActivitySearchCriteria
   {
      private long startedFromTimestamp;

      private long startedToTimestamp;

      private List<String> processDefinitionIds;

      private List<String> activityIds;

      private List<ActivityInstanceState> states;

      private String sortKey;

      private int rowFrom;

      private int pageSize;
   }

   /**
    * @param startedFromTimestamp
    * @param startedToTimestamp
    * @param processDefinitionIds
    * @param activityIds
    * @param states
    * @param sortKey
    * @param rowFrom
    * @param pageSize
    * @return
    */
   public static ActivitySearchCriteria getActivitySearchCriteria(
         String startedFromTimestamp, String startedToTimestamp,
         String processDefinitionIds, String activityIds, String states, String sortKey,
         String rowFrom, String pageSize)
   {
      ActivitySearchCriteria criteria = new ActivitySearchCriteria();
      criteria.startedFromTimestamp = SearchHelperUtil.stringToLong(startedFromTimestamp, -1);
      criteria.startedToTimestamp = SearchHelperUtil.stringToLong(startedToTimestamp, -1);
      criteria.processDefinitionIds = SearchHelperUtil.csvStringToList(processDefinitionIds);
      criteria.activityIds = SearchHelperUtil.csvStringToList(activityIds);
      criteria.states = csvStringToActivityInstanceStates(states);
      criteria.sortKey = sortKey;
      criteria.rowFrom = SearchHelperUtil.stringToInt(rowFrom, -1);
      criteria.pageSize = SearchHelperUtil.stringToInt(pageSize, -1);

      return criteria;
   }

   /**
    * @param criteria
    * @return
    */
   public static ActivityInstanceQuery buildActivitySearchQuery(
         ActivitySearchCriteria criteria)
   {
      ActivityInstanceState[] aiss = new ActivityInstanceState[criteria.states.size()];
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(criteria.states.toArray(aiss));

      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      FilterAndTerm filter = query.getFilter().addAndTerm();
      if ( -1 != criteria.startedFromTimestamp && -1 != criteria.startedToTimestamp)
      {
         filter.and(ActivityInstanceQuery.START_TIME.between(
               criteria.startedFromTimestamp, criteria.startedToTimestamp));
      }
      else if ( -1 != criteria.startedToTimestamp)
      {
         filter.and(ActivityInstanceQuery.START_TIME.lessOrEqual(criteria.startedToTimestamp));
      }
      else if ( -1 != criteria.startedFromTimestamp)
      {
         filter.and(ActivityInstanceQuery.START_TIME.greaterOrEqual(criteria.startedFromTimestamp));
      }

      FilterOrTerm processIdOrTerm = filter.addOrTerm();
      for (String id : criteria.processDefinitionIds)
      {
         processIdOrTerm.add(new ProcessDefinitionFilter(id, false));
      }

      FilterOrTerm activityIdOrTerm = filter.addOrTerm();
      for (String id : criteria.activityIds)
      {
         activityIdOrTerm.add(ActivityFilter.forAnyProcess(id));
      }

      if (criteria.rowFrom > -1 && criteria.pageSize > 0)
      {
         SubsetPolicy pagePolicy = new SubsetPolicy(criteria.pageSize, criteria.rowFrom,
               true);
         query.setPolicy(pagePolicy);
      }
      
      if (null != criteria.sortKey)
      {
         if (criteria.sortKey.equals("oldest"))
         {
            query.orderBy(ActivityInstanceQuery.START_TIME, true);
         }
         else if (criteria.sortKey.equals("newest"))
         {
            query.orderBy(ActivityInstanceQuery.START_TIME, false);
         }
         else if (criteria.sortKey.equals("criticality"))
         {
            query.orderBy(ActivityInstanceQuery.CRITICALITY, false);
         }
         else if (criteria.sortKey.equals("modified"))
         {
            query.orderBy(ActivityInstanceQuery.LAST_MODIFICATION_TIME, false);
         }
      }
      
      return query;
   }

   /**
    * @param listString
    * @return
    */
   private static List<ActivityInstanceState> csvStringToActivityInstanceStates(
         String listString)
   {
      List<ActivityInstanceState> list = new ArrayList<ActivityInstanceState>();
      try
      {
         StringTokenizer tokenizer = new StringTokenizer(listString, ",");
         while (tokenizer.hasMoreTokens())
         {
            list.add(ActivityInstanceState.getState(Integer.parseInt(tokenizer.nextToken())));
         }
      }
      catch (Exception e)
      {
         // No handling required
      }

      return list;
   }
}
