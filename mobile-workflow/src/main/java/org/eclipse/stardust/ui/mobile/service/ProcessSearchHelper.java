package org.eclipse.stardust.ui.mobile.service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;

/**
 * @author Shrikant.Gangal
 *
 */
public class ProcessSearchHelper
{
   /**
    * @author Shrikant.Gangal
    *
    */
   public static class ProcessSearchCriteria
   {
      private long startedFromTimestamp;

      private long startedToTimestamp;

      private List<String> processDefinitionIds;

      private List<ProcessInstanceState> states;

      private String sortKey;

      private int rowFrom;

      private int pageSize;
   }

   /**
    * @param startedFromTimestamp
    * @param startedToTimestamp
    * @param processDefinitionIds
    * @param states
    * @param sortKey
    * @param rowFrom
    * @param pageSize
    * @return
    */
   public static ProcessSearchCriteria getProcessSearchCriteria(
         String startedFromTimestamp, String startedToTimestamp,
         String processDefinitionIds, String states, String sortKey, String rowFrom,
         String pageSize)
   {
      ProcessSearchCriteria criteria = new ProcessSearchCriteria();
      criteria.startedFromTimestamp = SearchHelperUtil.stringToLong(startedFromTimestamp, -1);
      criteria.startedToTimestamp = SearchHelperUtil.stringToLong(startedToTimestamp, -1);
      criteria.processDefinitionIds = SearchHelperUtil.csvStringToList(processDefinitionIds);
      criteria.states = csvStringToProcessInsstanceStates(states);
      criteria.sortKey = sortKey;
      criteria.rowFrom = SearchHelperUtil.stringToInt(rowFrom, -1);
      criteria.pageSize = SearchHelperUtil.stringToInt(pageSize, -1);

      return criteria;
   }

   /**
    * @param criteria
    * @return
    */
   public static ProcessInstanceQuery buildProcessSearchQuery(
         ProcessSearchCriteria criteria)
   {
      ProcessInstanceState[] piss = new ProcessInstanceState[criteria.states.size()];
      ProcessInstanceQuery query = ProcessInstanceQuery.findInState(criteria.states.toArray(piss));

      FilterAndTerm filter = query.getFilter().addAndTerm();

      if ( -1 != criteria.startedFromTimestamp && -1 != criteria.startedToTimestamp)
      {
         filter.and(ProcessInstanceQuery.START_TIME.between(
               criteria.startedFromTimestamp, criteria.startedToTimestamp));
      }
      else if ( -1 != criteria.startedToTimestamp)
      {
         filter.and(ProcessInstanceQuery.START_TIME.lessOrEqual(criteria.startedToTimestamp));
      }
      else if ( -1 != criteria.startedFromTimestamp)
      {
         filter.and(ProcessInstanceQuery.START_TIME.greaterOrEqual(criteria.startedFromTimestamp));
      }

      if (criteria.processDefinitionIds.size() > 0)
      {
         FilterOrTerm orTerm = filter.addOrTerm();
         for (String id : criteria.processDefinitionIds)
         {
            orTerm.add(new ProcessDefinitionFilter(id, false));
         }
      }

      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);

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
            query.orderBy(ProcessInstanceQuery.START_TIME, true);
         }
         else if (criteria.sortKey.equals("newest"))
         {
            query.orderBy(ProcessInstanceQuery.START_TIME, false);
         }
         else if (criteria.sortKey.equals("priority"))
         {
            query.orderBy(ProcessInstanceQuery.PRIORITY, false);
         }
         else if (criteria.sortKey.equals("modified"))
         {
            // TODO - no query attribute to sort on last modified time
            // for process
            // query.orderBy(ProcessInstanceQuery., false);
         }
      }

      return query;
   }

   /**
    * @param listString
    * @return
    */
   private static List<ProcessInstanceState> csvStringToProcessInsstanceStates(
         String listString)
   {
      List<ProcessInstanceState> list = new ArrayList<ProcessInstanceState>();
      try
      {
         StringTokenizer tokenizer = new StringTokenizer(listString, ",");
         while (tokenizer.hasMoreTokens())
         {
            list.add(ProcessInstanceState.getState(Integer.parseInt(tokenizer.nextToken())));
         }
      }
      catch (Exception e)
      {
         // No handling required
      }

      return list;
   }
}
