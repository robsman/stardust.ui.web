/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.bcc.jsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.viewscommon.common.ISortHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityInstanceWithPrio;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



public class ProcessActivitiesSearchHandler implements IActivitySearchHandler, ISortHandler
{  
   private ProcessDefinition processDefinition;
   private Activity activity;
   private Integer priority;
   private Set oids;
   private static final ISortHandler sortHandler = new ActivityInstanceWithPrioSortHandler();
   
   public void setQueryData(ProcessDefinition processDefinition, Activity activity, Integer priority, Set oids)
   {
      this.processDefinition = processDefinition;
      this.activity = activity;
      this.priority = priority;
      this.oids = oids;
   }

   public Query createQuery()
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      FilterTerm filter = query.getFilter();
      if(oids != null)
      {
         if(!oids.isEmpty())
         {
            FilterTerm orTerm = filter.addOrTerm();
            for(Iterator iter = oids.iterator(); iter.hasNext();)
            {
               Long oid = (Long)iter.next();
               orTerm.add(ActivityInstanceQuery.OID.isEqual(oid.longValue()));
            }
         }
         else
         {
            filter.add(ActivityInstanceQuery.OID.isNull());
         }
      }
      else
      {
         filter.add(ActivityFilter.forProcess(activity.getQualifiedId(), processDefinition.getQualifiedId(), false));
         filter.add(ActivityStateFilter.ALIVE);
         if(priority != null)
         {
            filter.add(ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY.isEqual(priority.longValue()));
         }
      }
      return query;
   }

   public QueryResult performSearch(Query query)
   {
      try
      {
         WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
         QueryResult<ActivityInstance> ais = facade.getAllActivityInstances((ActivityInstanceQuery)query);
         
         Map<Long, ProcessInstance> processInstances = ProcessInstanceUtils.getProcessInstancesAsMap(ais, true);

         List /*<ActivitInstanceWithPrio>*/ aiList = new ArrayList(); 
         for (Iterator aiIter = ais.iterator(); aiIter.hasNext();)
         {
            ActivityInstance ai = (ActivityInstance) aiIter.next();
            aiList.add(new ActivityInstanceWithPrio(ai, processInstances.get(ai.getProcessInstanceOID())));
         }
         return new UserDefinedQueryResult(query, aiList, ais.hasMore(), new Long(ais.getTotalCount()));
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return null;
   }
   
   public void applySorting(Query query, List sortCriteria)
   {
      sortHandler.applySorting(query, sortCriteria);
   }

   public boolean isSortableColumn(String propertyName)
   {
      return sortHandler.isSortableColumn(propertyName);
   }
}
