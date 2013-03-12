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
package org.eclipse.stardust.ui.web.viewscommon.process.history;

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.RuntimeObject;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



/**
 * @author Yogesh.Manware
 *
 */
public class ActivityInstanceHistoryItem extends AbstractProcessHistoryTableEntry
{
   private static final String STATUS_PREFIX = "views.activityTable.statusFilter.";
   private Date lastModificationTime;
   private Date startTime;
   private ProcessInstance scopeProcessInstance;
   private String activityInstanceName;
   private String performer;
   private String state;
   private String type;
   private boolean activityAbortable;

   /**
    * @param scopeProcessInstance
    * @param activityInstance
    * @param children
    */
   public ActivityInstanceHistoryItem(ProcessInstance scopeProcessInstance, ActivityInstance activityInstance,
         List<IProcessHistoryTableEntry> children)
   {
      super(activityInstance, children);
      this.scopeProcessInstance = scopeProcessInstance;
      init(activityInstance);
   }

   /**
    * @param processInstance
    * @param activityInstance
    */
   public ActivityInstanceHistoryItem(ProcessInstance processInstance, ActivityInstance activityInstance)
   {
      this(processInstance, activityInstance, null);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.process.history.AbstractProcessHistoryTableEntry
    * #setRuntimeObject(org.eclipse.stardust.engine.api.runtime.RuntimeObject)
    */
   public void setRuntimeObject(RuntimeObject runtimeObject)
   {
      if (runtimeObject instanceof ActivityInstance || (runtimeObject == null))
      {
         super.setRuntimeObject(runtimeObject);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.process.history.AbstractProcessHistoryTableEntry
    * #runtimeObjectChanged()
    */
   protected void runtimeObjectChanged()
   {
      if (scopeProcessInstance != null)
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
         query.getFilter().add(ProcessInstanceQuery.OID.isEqual(scopeProcessInstance.getOID()));
         query.setPolicy(new ProcessInstanceDetailsPolicy(scopeProcessInstance.getDetailsLevel()));

         ServiceFactory sf = SessionContext.findSessionContext().getServiceFactory();
         QueryService qs = (sf != null) ? sf.getQueryService() : null;

         if (qs != null)
         {
            try
            {
               scopeProcessInstance = qs.findFirstProcessInstance(query);
            }
            catch (RuntimeException e)
            {
               scopeProcessInstance = null;
            }
         }
      }

      init((ActivityInstance) getRuntimeObject());
   }

   /**
    * 
    */
   protected void setMetadata()
   {
      List<IProcessHistoryTableEntry> metadatas = CollectionUtils.newArrayList();
      RuntimeObject ro = getRuntimeObject();

      if (ro instanceof ActivityInstance)
      {
         List<HistoricalEvent> events = ((ActivityInstance) ro).getHistoricalEvents();

         for (HistoricalEvent event : events)
         {
            metadatas.add(new EventHistoryItem(event));
         }
      }

      if (!metadatas.isEmpty())
      {
         List<IProcessHistoryTableEntry> children = getChildren();

         if (children != null)
         {
            metadatas.addAll(children);
         }
         setChildren(metadatas);
      }
   }

   /**
    * @param activityInstance
    */
   private void init(ActivityInstance activityInstance)
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      if (activityInstance != null)
      {
         Activity activity = activityInstance.getActivity();
         type = activity.getImplementationType().getName() + "Activity";
         
         if (ActivityInstanceUtils.isDefaultCaseActivity(activityInstance))
         {
            activityInstanceName = ActivityInstanceUtils.getCaseName(activityInstance);
         }
         else
         {
            activityInstanceName = I18nUtils.getActivityName(activity);
         }        
        
         startTime = activityInstance.getStartTime();
         lastModificationTime = activityInstance.getLastModificationTime();

         // Either the activity is alive
         if (activityInstance.isAssignedToUser())
         {
            UserInfo userInfo = (UserInfo) activityInstance.getCurrentPerformer();
            User user = UserUtils.getUser(userInfo.getId());
            performer = I18nUtils.getUserLabel(user);
         }
         else if (activityInstance.isAssignedToModelParticipant() && activityInstance.getCurrentPerformer()!=null)
         {            
            Participant participant = ParticipantUtils.getParticipant(activityInstance.getCurrentPerformer());
            if(null!=participant)
            {
               performer = I18nUtils.getParticipantName(participant);
            }
         }
         else
         {
            performer = activityInstance.getParticipantPerformerName();
         }
         // Or it is completed
         if (performer == null)
         {          
            UserInfo userInfo = activityInstance.getPerformedBy();
            if (null != userInfo)
            {
               User user = UserUtils.getUser(userInfo.getId());
               performer= I18nUtils.getUserLabel(user);
            }
            else
            {
               performer= activityInstance.getPerformedByName();
            }
            
         } 

         //state = activityInstance.getState().toString();
         state = propsBean.getString(STATUS_PREFIX +  activityInstance.getState().getName().toLowerCase()); 
         activityAbortable = ActivityInstanceUtils.isAbortable(activityInstance);
         setMetadata();
      }
   }

   public Date getLastModificationTime()
   {
      return lastModificationTime;
   }

   public String getName()
   {
      return activityInstanceName;
   }

   public String getPerformer()
   {
      return performer;
   }

   public String getRuntimeObjectType()
   {
      return type;
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public String getState()
   {
      return state;
   }

   public boolean isActivityAbortable()
   {
      return activityAbortable;
   }
}