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
package org.eclipse.stardust.ui.web.viewscommon.helper.activityTable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ModelParticipantDetails;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ResubmissionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



public class ActivityInstanceWithPrio implements Serializable
{
   private final static long serialVersionUID = 1l;

   protected final static Logger trace = LogManager.getLogger(ActivityInstanceWithPrio.class);

   private ActivityInstance activityInstance;

   private int prio;

   private int prioDb;

   private Participant participantPerformer;

   private boolean resubmissionActivity;

   private int noteCount;
   private boolean modifyProcessInstance;

   /**
    * @param activityInstance
    */
   public ActivityInstanceWithPrio(ActivityInstance activityInstance)
   {
      this(activityInstance, ProcessInstanceUtils.getProcessInstance(activityInstance.getProcessInstanceOID()));
   }

   /**
    * @param activityInstance
    * @param pi
    */
   public ActivityInstanceWithPrio(ActivityInstance activityInstance, ProcessInstance processInstance)
   {
      this.activityInstance = activityInstance;
      if (activityInstance != null)
      {
         resubmissionActivity = ResubmissionUtils.isResubmissionActivity(activityInstance);

         try
         {
            prioDb = prio = processInstance.getPriority();

            noteCount = ProcessInstanceUtils.getNotes(processInstance).size();
            modifyProcessInstance = AuthorizationUtils.hasPIModifyPermission(processInstance);
         }
         catch (Exception e)
         {
            trace.error("Cannot get details for Activity '" + activityInstance.getActivity().getId() + "' with OID "
                  + activityInstance.getOID());
         }
      }
      setParticipant();
   }

   private void setParticipant()
   {
      Activity activity = getActivityInstance().getActivity();
      ModelParticipant performer = activity.getDefaultPerformer();
      if (performer != null)
      {
         participantPerformer = performer;
         if (performer instanceof ConditionalPerformer)
         {
            Participant p = ((ConditionalPerformer) performer).getResolvedPerformer();
            if (p != null && !(p instanceof User))
            {
               participantPerformer = p;
            }
            else
            {
               participantPerformer = null;
            }
         }
      }
   }

   public ProcessInstances getAllProcessInstances(ProcessInstanceQuery query)
   {
      try
      {
         return ServiceFactoryUtils.getQueryService().getAllProcessInstances(query);
      }
      catch (Exception e)
      {
      }
      return null;
   }

   public ActivityInstance getActivityInstance()
   {
      return activityInstance;
   }

   public void setActivityInstance(ActivityInstance ai)
   {
      this.activityInstance = ai;
      participantPerformer = null;
      setParticipant();
   }

   public long getActivityOID()
   {
      return activityInstance.getOID();
   }

   public String getActivityName()
   {
      return activityInstance.getActivity().getName();
   }

   public long getProcessOID()
   {
      return activityInstance.getProcessInstanceOID();
   }

   public String getProcessId()
   {
      return activityInstance.getProcessDefinitionId();
   }

   public Date getStartTime()
   {
      return getActivityInstance().getStartTime();
   }

   public String getDuration()
   {
      long timeInMillis = Calendar.getInstance().getTimeInMillis();
      if (activityInstance.getState() == ActivityInstanceState.Completed
            || activityInstance.getState() == ActivityInstanceState.Aborted)
      {
         timeInMillis = activityInstance.getLastModificationTime().getTime();
      }
      return DateUtils.formatDurationInHumanReadableFormat(timeInMillis - activityInstance.getStartTime().getTime());
   }

   public String getDefaultPerformerName()
   {
      return participantPerformer != null ? I18nUtils.getParticipantName(participantPerformer) : null;
   }

   public String getDefaultPerformerId()
   {
      return participantPerformer != null ? participantPerformer.getId() : null;
   }

   public String getDefaultPerformerDesc()
   {
      String defaultDesc = participantPerformer instanceof ModelParticipantDetails
            ? ((ModelParticipantDetails) participantPerformer).getDescription()
            : null;
      return I18nUtils.getParticipantDescription(participantPerformer, defaultDesc);
   }

   public String getCurrentPerformerName()
   {
      return ActivityInstanceUtils.getAssignedToLabel(activityInstance);
   }

   public String getPerformedByName()
   {

      UserInfo userInfo = activityInstance.getPerformedBy();
      if (null != userInfo)
      {
         User user = ServiceFactoryUtils.getUserService().getUser(userInfo.getId());
         return I18nUtils.getUserLabel(user);
      }
      else
      {
         return activityInstance.getPerformedByName();
      }
   }

   public String getStateName()
   {
      return getActivityInstance().getState().getName();
   }

   public int getPriority()
   {
      return prio;
   }

   public int getOriginalPriority()
   {
      return prioDb;
   }

   public void setPriority(int prio)
   {
      this.prio = prio;
   }

   public boolean isPriorityChanged()
   {
      return prio != prioDb;
   }

   public void applyChanges()
   {
      prioDb = prio;
   }

   public boolean isResubmissionActivity()
   {
      return resubmissionActivity;
   }

   public int getNoteCount()
   {
      return noteCount;
   }

   public boolean isModifyProcessInstance()
   {
      return modifyProcessInstance;
   }
}
