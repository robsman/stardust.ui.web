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

import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.query.ParticipantWorklist;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserWorklist;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractProcessExecutionPortal;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;


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

   private WorklistQuery createWorklistQuery(AbstractProcessExecutionPortal portal, 
         Set participantIds, boolean outline)
   {
      WorklistQuery query = new WorklistQuery();
      query.setUserContribution(SubsetPolicy.UNRESTRICTED);
      for (Iterator i = participantIds.iterator(); i.hasNext();)
      {
         String participantId = (String) i.next();
         query.setParticipantContribution(
               PerformingParticipantFilter.forModelParticipant(participantId, false),
               outline ? new SubsetPolicy(0, true) : null);
      }
      return query;
   }
   
   private boolean isAssemblyLineActivity(Set assemblyLineParticipants, ActivityInstance ai)
   {
      ModelParticipant modelParticipant = ai.getActivity().getDefaultPerformer();
      
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
               createWorklistQuery(portal, participantIds, false));
   
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
            createWorklistQuery(portal, participantIds, true)) : null;
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
}
