package org.eclipse.stardust.ui.mobile.service;

import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.WorklistQuery;

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
   public static WorklistCriteria getWorkslitCriteria(String sortKey, String rowFrom, String pageSize)
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
   public static WorklistQuery buildWorklistQuery(WorklistCriteria criteria)
   {
      WorklistQuery query = WorklistQuery.findCompleteWorklist();

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
            query.orderBy(WorklistQuery.START_TIME, true);
         }
         else if (criteria.sortKey.equals("newest"))
         {
            query.orderBy(WorklistQuery.START_TIME, false);
         }
         else if (criteria.sortKey.equals("criticality"))
         {
            query.orderBy(WorklistQuery.ACTIVITY_INSTANCE_CRITICALITY, false);
         }
         else if (criteria.sortKey.equals("modified"))
         {
            query.orderBy(WorklistQuery.LAST_MODIFICATION_TIME, false);
         }
         else
         {
            query.orderBy(WorklistQuery.ACTIVITY_INSTANCE_OID, false);
         }
      }
      else
      {
         query.orderBy(WorklistQuery.ACTIVITY_INSTANCE_OID, false);
      }

      return query;
   }
}
