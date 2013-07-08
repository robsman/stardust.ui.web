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
               if(entry.getKey().equals(worklistOwner.getId()))
               {
                  participantWorklists.put(new ParticipantInfoWrapper(worklistOwner), new ParticipantWorklistCacheEntry(
                        worklist.getTotalCount(), WorklistUtils.createWorklistQuery(worklistOwner),WorklistUtils.getAllUserAssignedActivities(),worklist.getTotalCountThreshold(),entry.getKey()));
               }
               else
               participantWorklists.put(new ParticipantInfoWrapper(worklistOwner), new ParticipantWorklistCacheEntry(
                     worklist.getTotalCount(), WorklistUtils.createWorklistQuery(worklistOwner),worklist.getTotalCountThreshold(),entry.getKey()));
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
    * @param participantInfo
    * @return
    */
   public long getWorklistCount(ParticipantInfo participantInfo)
   {
      ParticipantWorklistCacheEntry cacheEntry = participantWorklists.get(new ParticipantInfoWrapper(participantInfo));
      if (null != cacheEntry)
      {
         return cacheEntry.getCount();
      }
      
      return 0;
   }
   
   /**
    * @param participantInfo
    * @return
    */
   public long getWorklistCountThreshold(ParticipantInfo participantInfo)
   {
      ParticipantWorklistCacheEntry cacheEntry = participantWorklists.get(new ParticipantInfoWrapper(participantInfo));
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
    * @param participantInfo
    * @return
    */
   public WorklistQuery getWorklistQuery(ParticipantInfo participantInfo)
   {
      WorklistQuery worklistQuery = participantWorklists.get(new ParticipantInfoWrapper(participantInfo)).getWorklistQuery();
      return (WorklistQuery) QueryUtils.getClonedQuery(worklistQuery);
   }
   
   /**
    * @param participantInfo
    * @return
    */
   public ActivityInstanceQuery getActivityInstanceQuery(ParticipantInfo participantInfo)
   {
      ParticipantWorklistCacheEntry pwc=participantWorklists.get(new ParticipantInfoWrapper(participantInfo));
      ActivityInstanceQuery activityInstanceQuery = participantWorklists.get(new ParticipantInfoWrapper(participantInfo)).getActivityInstanceQuery();
      return (ActivityInstanceQuery) QueryUtils.getClonedQuery(activityInstanceQuery);
   }
   
   /**
    * @param participantInfo
    * @param count
    */
   public void setWorklistCount(ParticipantInfo participantInfo, long count)
   {
      ParticipantWorklistCacheEntry worklistCacheEntry = participantWorklists.get(new ParticipantInfoWrapper(participantInfo));
      if (null != worklistCacheEntry)
      {
         worklistCacheEntry.setCount(count);
      }
   }
   
   /**
    * @param participantInfo
    * @param count
    */
   public void setWorklistThresholdCount(ParticipantInfo participantInfo, long count)
   {
      ParticipantWorklistCacheEntry worklistCacheEntry = participantWorklists.get(new ParticipantInfoWrapper(participantInfo));
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
//      printCache("Before handleActivityEvent() = " + event.getType());

      // Act on OLD AI
      if (null != oldAi) // oldAi can be null if AI is ACTIVATED
      {
         ParticipantWorklistCacheEntry oldEntry = participantWorklists.get(new ParticipantInfoWrapper(oldAi
               .getCurrentPerformer()));
         if (null != oldEntry && (oldEntry.getCount() > 0 && oldEntry.getCount() < oldEntry.getTotalCountThreshold()))
         {
            oldEntry.setCount(oldEntry.getCount() - 1);
         }
      }
      
      // Act on NEW AI
      ActivityInstance newAi = event.getActivityInstance();
      if (null != newAi) // Safety Check
      {
         ParticipantWorklistCacheEntry newEntry = participantWorklists.get(new ParticipantInfoWrapper(newAi
               .getCurrentPerformer()));
         if (null != newEntry && newEntry.getCount() < Long.MAX_VALUE)
         {
            newEntry.setCount(newEntry.getCount() + 1);
         }
      }
      
//      printCache("After handleActivityEvent()");
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
      
      /**
       * @param participantInfo
       */
      public ParticipantInfoWrapper(ParticipantInfo participantInfo)
      {
         this.participantInfo = participantInfo;
      }
      
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         String id = (participantInfo == null) ? null : participantInfo.getId();
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
         
         return true;
      }
      
      public ParticipantInfo getParticipantInfo()
      {
         return participantInfo;
      }
   }
}
