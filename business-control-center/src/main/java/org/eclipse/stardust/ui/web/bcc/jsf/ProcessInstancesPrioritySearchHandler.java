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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.viewscommon.common.ISortHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.processTable.ProcessInstanceTableEntry;



public class ProcessInstancesPrioritySearchHandler implements IProcessInstancesPrioritySearchHandler, ISortHandler, Serializable
{
   private static final long serialVersionUID = 1L;
   private IQueryExtender queryExtender;
   private ProcessDefinition processDefinition;
   private Set<ProcessDefinition> processDefinitions;
   private Integer priority;
   private Set oids;
   private static final ISortHandler sortHandler = new ProcessInstanceWithPrioSortHandler();

   public void setQueryData(ProcessDefinition processDefinition, Integer priority, Set oids)
   {
      this.processDefinition = processDefinition;
      this.priority = priority;
      this.oids = oids;
   }

   public void setQueryData(Set<ProcessDefinition> processDefinitions, Integer priority)
   {
      this.oids = null;
      this.processDefinitions = processDefinitions;
      this.priority = priority;
   }
   
   public Query createQuery()
   {
      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive();
      FilterAndTerm filter = query.getFilter();
      if(oids != null)
      {
         if(!oids.isEmpty())
         {
            Iterator pIter = oids.iterator();
            FilterTerm orTerm = filter.addOrTerm();
            while(pIter.hasNext())
            {
               Long oid = (Long) pIter.next();
               orTerm.add(ProcessInstanceQuery.OID.isEqual(oid.longValue()));
            }
         }
         else
         {
            filter.add(ProcessInstanceQuery.OID.isNull());
         }
      }
      else
      {
         if(processDefinition != null)
         {
            filter.add(new ProcessDefinitionFilter(processDefinition.getQualifiedId(), false));         
         }
         else if(CollectionUtils.isNotEmpty(processDefinitions))
         {
            FilterTerm orTerm = filter.addOrTerm();
            for (ProcessDefinition pd : processDefinitions)
            {
               orTerm.add(new ProcessDefinitionFilter(pd.getQualifiedId(), false));
            }
         }
         if(priority != null)
         {
            filter.add(ProcessInstanceQuery.PRIORITY.isEqual(priority.intValue()));
         }
         else
         {
            // find all process instances which have a priority
            filter.add(ProcessInstanceQuery.PRIORITY.between(
                  ProcessInstancePriority.LOW,  ProcessInstancePriority.HIGH));
         }
      }
      
      if(queryExtender != null)
      {
         queryExtender.extendQuery(query);
      }
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      return query;
   }

   public QueryResult<ProcessInstanceTableEntry> performSearch(Query query)
   {
      List<ProcessInstanceTableEntry> extendedInfo = CollectionUtils.newArrayList();
      ProcessInstances instances = null;
      QueryResult<ProcessInstanceTableEntry> result = null;
      try
      {
         WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
         instances = facade.getAllProcessInstances((ProcessInstanceQuery) query);
         if (instances != null)
         {
            for (ProcessInstance pi : instances)
            {
               extendedInfo.add(new ProcessInstanceTableEntry(pi));
            }
            result = new RawQueryResult<ProcessInstanceTableEntry>((List) instances, instances.getSubsetPolicy(),
                  instances.hasMore(), Long.valueOf((instances.getTotalCount())));

         }
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return result;
   }

  /* public void attachQueryExtender(IQueryExtender queryExtender)
   {
      this.queryExtender = queryExtender;
   }*/
   
   public void applySorting(Query query, List sortCriteria)
   {
      sortHandler.applySorting(query, sortCriteria);
   }

   public boolean isSortableColumn(String propertyName)
   {
      return sortHandler.isSortableColumn(propertyName);
   }

   public void attachQueryExtender(IQueryExtender queryExtender)
   {
      this.queryExtender = queryExtender;
      
   }
}
