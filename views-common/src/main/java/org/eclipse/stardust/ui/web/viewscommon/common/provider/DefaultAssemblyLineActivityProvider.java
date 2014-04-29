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
package org.eclipse.stardust.ui.web.viewscommon.common.provider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ParticipantWorklist;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserWorklist;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractProcessExecutionPortal;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.provider.IAssemblyLineActivityProvider;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * Default implementation of assembly line contract.
 * <p>
 * The order of the next pending assembly line activity is not predictable
 * but you get at first the activities from the user worklist and after
 * that from the participant worklist.
 * </p> 
 * @see org.eclipse.stardust.ui.web.viewscommon.common.provider.IAssemblyLineActivityProvider
 * @author rsauer
 * @version $Revision$
 */
public class DefaultAssemblyLineActivityProvider implements IAssemblyLineActivityProvider
{
   private Map<String, List<Department>> departmentsCache = new HashMap<String, List<Department>>();
   
   protected WorklistQuery createWorklistQuery(Set participantIds, boolean outline)
   {
      Map<String, Organization> organizationMap = CollectionUtils.newHashMap();
      WorklistQuery query = new WorklistQuery();
      query.setUserContribution(SubsetPolicy.UNRESTRICTED);
      for (Iterator i = participantIds.iterator(); i.hasNext();)
      {
         String participantId = (String) i.next();
         ModelParticipant participant = (ModelParticipant) ModelCache.findModelCache().getParticipant(participantId);
         if(!participant.isDepartmentScoped())
         {
            // For non-department scoped Participant use the participantId for filter
            query.setParticipantContribution(PerformingParticipantFilter.forParticipant(participant, false),
                  outline ? new SubsetPolicy(0, true) : null);
         }
         else
         {
            List<Department> deptList;
            
            if(!updateOrganizationMap(participant,organizationMap))
            {
               continue; // If query already contains filter for Org/Role, loop out
            }
            // Loop over each Org. to get Department for setting filter on Scoped
            // Participant of Dept.
            for (Entry<String, Organization> entry : organizationMap.entrySet())
            {
               Organization org = entry.getValue();
               if (departmentsCache.containsKey(org.getQualifiedId()))
               {
                  deptList = departmentsCache.get(org.getQualifiedId());
               }
               else
               {
                  // Populate Department cache
                  QueryService qs = ServiceFactoryUtils.getQueryService();
                  deptList = qs.findAllDepartments(org.getDepartment(), org);
                  departmentsCache.put(org.getQualifiedId(), deptList);
               }

               for (Department department : deptList)
               {
                  ParticipantInfo participantInfo = department.getScopedParticipant(participant);
                  if (null != participantInfo)
                  {
                     query.setParticipantContribution(PerformingParticipantFilter
                           .forParticipant(participantInfo, false), outline ? new SubsetPolicy(0, true) : null);
                  }
               }
            }
         }
      }
      FilterTerm filter = query.getFilter().addAndTerm();
      long caseActivityOID = ModelCache.findModelCache().getDefaultCaseActivity().getRuntimeElementOID();
      filter.add(WorklistQuery.ACTIVITY_OID.notEqual(caseActivityOID));
      return query;
   }

   /**
    * Updates the original Org Map, return true if map is updated, return false, if Org.
    * Map already contains key
    * 
    * @param participant
    * @param organizationMap
    * @return
    */
   private boolean updateOrganizationMap(Participant participant, Map<String, Organization> organizationMap)
   {
      if (participant instanceof Organization)
      {
         if (!organizationMap.containsKey(participant.getQualifiedId()))
         {
            // Create a map for unique Org, used for fetching dept.
            organizationMap.put(participant.getQualifiedId(), (Organization) participant);
            return true;
         }
      }
      else if (participant instanceof Role)
      {
         Role role = (Role) participant;
         List<Organization> worksForOrganizations = role.getClientOrganizations();

         if ((worksForOrganizations != null) && (worksForOrganizations.size() > 0))
         {
            String orgQualifierId = worksForOrganizations.get(0).getQualifiedId();
            if (!organizationMap.containsKey(orgQualifierId))
            {
               organizationMap.put(orgQualifierId, worksForOrganizations.get(0));
               return true;
            }
         }
      }
      // If Org. Map was not updated return false
      return false;
   }
   
   private boolean isAssemblyLineActivity(Set assemblyLineParticipants, ActivityInstance ai)
   {
      ModelParticipant modelParticipant = null ;
      
      if (QualityAssuranceState.IS_QUALITY_ASSURANCE.equals(ai.getQualityAssuranceState()))
      {
         modelParticipant = ai.getActivity().getQualityAssurancePerformer();
      }
      else
      {
         modelParticipant = ai.getActivity().getDefaultPerformer();
      }
      
      if(modelParticipant != null)
      {
         String aiParticipantId = modelParticipant.getId();
         if(modelParticipant instanceof ConditionalPerformer)
         {
            ConditionalPerformer cp = (ConditionalPerformer)modelParticipant;
            Participant rp = cp.getResolvedPerformer();
            if (rp instanceof ModelParticipant)
            {
               aiParticipantId = rp.getId();
            }
         }

         if(assemblyLineParticipants != null)
         {
            for (Iterator patIter = assemblyLineParticipants.iterator(); patIter.hasNext();)
            {
               String participantId = (String) patIter.next();
               if(CompareHelper.areEqual(participantId, aiParticipantId))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }
   
   public ActivityInstance getNextAssemblyLineActivity(
         AbstractProcessExecutionPortal portal, Set participantIds) throws PortalException
   {
      ActivityInstance result = null;

      WorkflowService ws = portal.getWorkflowService();
      if(ws != null)
      {
         Worklist wl = ws.getWorklist(
               createWorklistQuery(participantIds, false));
   
         for (Iterator i = wl.getCumulatedItems().iterator(); i.hasNext() && result == null;)
         {
            ActivityInstance ai = (ActivityInstance) i.next();
            if(isAssemblyLineActivity(participantIds, ai))
            {
               try
               {
                  // lock activity
                  ws.activate(ai.getOID());
      
                  result = ai;
                  break;
               }
               catch (ConcurrencyException ce)
               {
                  continue;
               }
            }
         }
      }
      return result;
   }

   public long getAssemblyLineActivityCount(AbstractProcessExecutionPortal portal,
         Set participantIds) throws PortalException
   {
      WorkflowService ws = portal.getWorkflowService();
      Worklist worklist = ws != null ? ws.getWorklist(
            createWorklistQuery(participantIds, true)) : null;
      long activityCount = 0;
      if(worklist != null)
      {
         if(worklist instanceof UserWorklist)
         {
            for(Iterator aiIter = worklist.iterator(); aiIter.hasNext();)
            {
               ActivityInstance ai = (ActivityInstance)aiIter.next();
               if(isAssemblyLineActivity(participantIds, ai))
               {
                  activityCount += 1;
               }
            }
         }
         Iterator worklistIter = worklist.getSubWorklists();
         while (worklistIter.hasNext())
         {
            worklist = (Worklist) worklistIter.next();
            if (worklist instanceof ParticipantWorklist)
            {
               activityCount += worklist.getTotalCount();
            }
         }
      }
      return activityCount;
   }

   public Map<String, List<Department>> getDepartmentsCache()
   {
      return departmentsCache;
   }
}
