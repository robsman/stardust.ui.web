package org.eclipse.stardust.ui.mobile.service;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.EvaluateByWorkitemsPolicy;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;

/**
 * @author Shrikant.Gangal
 * 
 */
public class WorklistHelper
{
   /**
    * @author Shrikant.Gangal
    * 
    */
   public static class WorklistCriteria
   {
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
   public static WorklistCriteria getWorkslitCriteria(String sortKey, String rowFrom,
         String pageSize)
   {
      WorklistCriteria criteria = new WorklistCriteria();
      criteria.sortKey = sortKey;
      criteria.rowFrom = SearchHelperUtil.stringToInt(rowFrom, -1);
      criteria.pageSize = SearchHelperUtil.stringToInt(pageSize, -1);

      return criteria;
   }

   /**
    * @param criteria
    * @return
    */
   public static ActivityInstanceQuery buildWorklistQuery(WorklistCriteria criteria)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});
      // TODO - this is used to enhance performace but has a bug 
      // query.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);

      FilterOrTerm or = query.getFilter().addOrTerm();
      or.add(PerformingParticipantFilter.ANY_FOR_USER).add(
            PerformingUserFilter.CURRENT_USER);

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
         else
         {
            query.orderBy(ActivityInstanceQuery.OID, false);
         }
      }
      else
      {
         query.orderBy(ActivityInstanceQuery.OID, false);
      }

      if (criteria.rowFrom > -1 && criteria.pageSize > 0)
      {
         query.setPolicy(new SubsetPolicy(criteria.pageSize, criteria.rowFrom, true));
      }

      return query;
   }
}
