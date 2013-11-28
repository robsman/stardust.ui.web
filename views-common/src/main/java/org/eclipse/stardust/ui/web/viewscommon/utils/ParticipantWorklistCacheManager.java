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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler.MessageDisplayMode;
import org.springframework.beans.factory.InitializingBean;



/**
 * @author subodh.godbole
 *
 */
public class ParticipantWorklistCacheManager implements InitializingBean, Serializable
{
   private static final long serialVersionUID = -4467279164688998487L;
   public static final String BEAN_ID = "ippParticipantWorklistCacheManager";
   public static final Logger trace = LogManager.getLogger(ParticipantWorklistCacheManager.class);

   private Map<ParticipantInfoWrapper, ParticipantWorklistCacheEntry> participantWorklists;
   
   /**
    * @return
    */
   public static ParticipantWorklistCacheManager getInstance()
   {
      return (ParticipantWorklistCacheManager) FacesUtils.getBeanFromContext(BEAN_ID);
   }
   
   /* (non-Javadoc)
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      reset();
   }
   
   /**
    * 
    */
   public void reset()
   {
      participantWorklists = new LinkedHashMap<ParticipantInfoWrapper, ParticipantWorklistCacheEntry>();
      ParticipantInfo worklistOwner = null;
      try
      {
         Map<String, List<Worklist>> worklistMap = WorklistUtils.getWorklist_anyForUser();
         for (Entry<String, List<Worklist>> entry : worklistMap.entrySet())
         {
            for (Worklist worklist : entry.getValue())
            {
               worklistOwner = worklist.getOwner();
               if(entry.getKey().equals(worklistOwner.getQualifiedId()) && (worklistOwner instanceof UserInfo))
               {
                  // Using the userParticipantId i.e entry.getKey() along with
                  // worklistOwner- ParticipantInfo
                  // to distinguish same Role present for Deputy
                  participantWorklists.put(
                        new ParticipantInfoWrapper(worklistOwner, entry.getKey()),
                        new ParticipantWorklistCacheEntry(worklist.getTotalCount(), WorklistUtils
                              .createWorklistQuery(worklistOwner), WorklistUtils.getAllUserAssignedActivities(),
                              worklist.getTotalCountThreshold(), entry.getKey()));
               }
               else
               {
                  // Using the userParticipantId i.e entry.getKey() along with
                  // worklistOwner- ParticipantInfo
                  // to distinguish same Role present for Deputy
                  participantWorklists.put(
                        new ParticipantInfoWrapper(worklistOwner, entry.getKey()),
                        new ParticipantWorklistCacheEntry(worklist.getTotalCount(), WorklistUtils
                              .createWorklistQuery(worklistOwner), worklist.getTotalCountThreshold(), entry.getKey()));
               }
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, "Error occurred while retrieving worklist",
               MessageDisplayMode.CUSTOM_MSG_OPTIONAL);
      }
   }

   /**
    * 
    * @param participantInfo
    * @param userParticipantId - The Id of user (Root Node in participant Tree)
    * @return
    */
   public long getWorklistCount(ParticipantInfo participantInfo, String userParticipantId)
   {
      ParticipantWorklistCacheEntry cacheEntry = participantWorklists.get(new ParticipantInfoWrapper(participantInfo,
            userParticipantId));
      if (null != cacheEntry)
      {
         return cacheEntry.getCount();
      }

      return 0;
   }

   /**
    * 
    * @param participantInfo
    * @param userParticipantId- The Id of user (Root Node in participant Tree)
    * @return
    */
   public long getWorklistCountThreshold(ParticipantInfo participantInfo, String userParticipantId)
   {
      ParticipantWorklistCacheEntry cacheEntry = participantWorklists.get(new ParticipantInfoWrapper(participantInfo,
            userParticipantId));
      if (null != cacheEntry)
      {
         return cacheEntry.getTotalCountThreshold();
      }

      return Long.MAX_VALUE;
   }

   
   /**
    * @return
    */
   public Map<String, Set<ParticipantInfo>> getWorklistParticipants()
   {
      Map<String, Set<ParticipantInfo>> worklistParticipantMap = new LinkedHashMap<String, Set<ParticipantInfo>>();

      for (Entry<ParticipantInfoWrapper, ParticipantWorklistCacheEntry> entry : participantWorklists.entrySet())
      {
         Set<ParticipantInfo> worklistParticipants = worklistParticipantMap.get(entry.getValue().getWorklistOwner());
         if (null == worklistParticipants)
         {
            worklistParticipants = new LinkedHashSet<ParticipantInfo>();
            worklistParticipantMap.put(entry.getValue().getWorklistOwner(), worklistParticipants);
         }
         worklistParticipants.add(entry.getKey().getParticipantInfo());
         //
      }

      return worklistParticipantMap;
   }

   /**
    * ParticipantInfoWrapper uses the participantInfo,UserParticipantId to distingush same
    * role in Dif hierarchy(User-Deputy)
    * 
    * @param participantInfo
    * @param userParticipantId
    *           - The Id of user (Root Node in participant Tree)
    * @return
    */
   public WorklistQuery getWorklistQuery(ParticipantInfo participantInfo, String userParticipantId)
   {
      WorklistQuery worklistQuery = participantWorklists.get(
            new ParticipantInfoWrapper(participantInfo, userParticipantId)).getWorklistQuery();
      return (WorklistQuery) QueryUtils.getClonedQuery(worklistQuery);
   }
   
   /**
    * ParticipantInfoWrapper uses the participantInfo,UserParticipantId to distingush same
    * role in Dif hierarchy(User-Deputy)
    * 
    * @param participantInfo
    * @param userParticipantId
    * @return
    */
   public ActivityInstanceQuery getActivityInstanceQuery(ParticipantInfo participantInfo, String userParticipantId)
   {
      ActivityInstanceQuery activityInstanceQuery = participantWorklists.get(new ParticipantInfoWrapper(participantInfo, userParticipantId)).getActivityInstanceQuery();
      return (ActivityInstanceQuery) QueryUtils.getClonedQuery(activityInstanceQuery);
   }
   
   /**
    * ParticipantInfoWrapper uses the participantInfo,UserParticipantId to distingush same
    * role in Dif hierarchy(User-Deputy)
    * 
    * @param participantInfo
    * @param userParticipantId
    * @param count
    */
   public void setWorklistCount(ParticipantInfo participantInfo, String userParticipantId, long count)
   {
      ParticipantWorklistCacheEntry worklistCacheEntry = participantWorklists.get(new ParticipantInfoWrapper(
            participantInfo, userParticipantId));
      if (null != worklistCacheEntry)
      {
         worklistCacheEntry.setCount(count);
      }
   }
   
   /**
    * ParticipantInfoWrapper uses the participantInfo,UserParticipantId to distingush same
    * role in Dif hierarchy(User-Deputy)
    * 
    * @param participantInfo
    * @param userParticipantId
    * @param count
    */
   public void setWorklistThresholdCount(ParticipantInfo participantInfo, String userParticipantId, long count)
   {
      ParticipantWorklistCacheEntry worklistCacheEntry = participantWorklists.get(new ParticipantInfoWrapper(
            participantInfo, userParticipantId));
      if (null != worklistCacheEntry)
      {
         worklistCacheEntry.setTotalCountThreshold(count);
      }
   }

   /**
    * @param oldAi
    * @param event
    */
   public void handleActivityEvent(ActivityInstance oldAi, ActivityEvent event)
   {
      String userPerformer = null;
      ActivityInstance newAi = event.getActivityInstance();
      // Act on OLD AI
      if (null != oldAi) // oldAi can be null if AI is ACTIVATED
      {
         userPerformer = null != oldAi.getUserPerformer() ? oldAi.getUserPerformer().getQualifiedId() : null;
         if (null == userPerformer && null != newAi)
         {
            userPerformer = null != newAi.getUserPerformer() ? newAi.getUserPerformer().getQualifiedId() : null;
         }
         ParticipantWorklistCacheEntry oldEntry = participantWorklists.get(new ParticipantInfoWrapper(oldAi
               .getCurrentPerformer(), userPerformer));
         if (null != oldEntry && (oldEntry.getCount() > 0 && oldEntry.getCount() < oldEntry.getTotalCountThreshold()))
         {
            oldEntry.setCount(oldEntry.getCount() - 1);
         }
      }
      // Act on NEW AI
      if (null != newAi) // Safety Check
      {
         if (null == userPerformer)
         {
            userPerformer = null != newAi.getUserPerformer() ? newAi.getUserPerformer().getQualifiedId() : null;
         }
         ParticipantWorklistCacheEntry newEntry = participantWorklists.get(new ParticipantInfoWrapper(newAi
               .getCurrentPerformer(), userPerformer));
         if (null != newEntry && newEntry.getCount() < Long.MAX_VALUE)
         {
            newEntry.setCount(newEntry.getCount() + 1);
         }
      }
      
   }
   
   /**
    * Only Added for Development Purpose. Call to this function can be removed later.
    * @param msg
    */
   private void printCache(String msg)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("ParticipantWorklistCacheManager>> " + msg);
         for (Entry<ParticipantInfoWrapper, ParticipantWorklistCacheEntry> entry : participantWorklists.entrySet())
         {
            trace.debug("\t" + entry.getKey().getParticipantInfo() + "=>" + entry.getValue());
         }
      }
   }
   
   /**
    * @author anoop.nair
    *
    */
   private static final class ParticipantInfoWrapper implements Serializable
   {
      private static final long serialVersionUID = -2659357845106111645L;
      private ParticipantInfo participantInfo;
      private String userParticipant;
      
      /**
       * @param participantInfo
       */
      public ParticipantInfoWrapper(ParticipantInfo participantInfo)
      {
         this.participantInfo = participantInfo;
      }
      
      /**
       * For role Participant, User Participant Id is used to distingush when same role is
       * present in Deputy
       * 
       * @param participantInfo
       * @param userParticipant
       */
      public ParticipantInfoWrapper(ParticipantInfo participantInfo, String userParticipant)
      {
         this(participantInfo);
         this.userParticipant = userParticipant;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         String id = null;
         if(userParticipant!=null)
         {
            id = (participantInfo == null) ? null : participantInfo.getId() + userParticipant;
         }
         else
         id = (participantInfo == null) ? null : participantInfo.getId();
         // Note: It is not required that if two objects are unequal according to the
         // equals(java.lang.Object) method, then calling the hashCode method on each of
         // the two objects must produce distinct integer results.
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         
         if (obj == null)
            return false;
         
         if (getClass() != obj.getClass())
            return false;
         
         ParticipantInfoWrapper other = (ParticipantInfoWrapper) obj;
         if (null == participantInfo)
         {
            if (null != other.participantInfo)
            {
               return false;
            }
         }
         else if (!ParticipantUtils.areEqual(participantInfo, other.participantInfo))
         {
            return false;
         }
         else if (userParticipant != null && !(userParticipant.equals(other.userParticipant)))
         {
            return false;
         }
         
         return true;
      }
      
      public ParticipantInfo getParticipantInfo()
      {
         return participantInfo;
      }

      public String getUserParticipant()
      {
         return userParticipant;
      }
      
   }
}
