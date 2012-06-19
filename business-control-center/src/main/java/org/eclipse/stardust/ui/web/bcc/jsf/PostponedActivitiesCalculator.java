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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics.Participation;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics.PostponedActivities;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics.PostponedActivityDetails;
import org.eclipse.stardust.ui.web.common.util.DateUtils;



public class PostponedActivitiesCalculator
{
   private final PostponedActivities pActivities;
   private final Map/*<Long, ModelParticipantCalculation>*/ calulations;
   
   private static class ModelParticipantCalculation
   {
      private long runtimeOid;
      private long totalCount;
      private long exceededCount;
      private long duration;
      
      private final long currentDate;
      
      public ModelParticipantCalculation(long runtimeOid)
      {
         this.runtimeOid = runtimeOid;
         currentDate = new Date().getTime();
      }
      
      private void addDuration(List details)
      {
         Iterator dIter = details.iterator();
         while (dIter.hasNext())
         {
            PostponedActivityDetails detail = (PostponedActivityDetails) dIter.next();
            duration += (currentDate - detail.aiStart.getTime());
         }
      }
      
      public void addParticipation(Participation contrib)
      {
         if(contrib != null && contrib.performerOid == runtimeOid)
         {
            int count = contrib.highPriorityCritical.size() +
               contrib.normalPriorityCritical.size() +
               contrib.lowPriorityCritical.size();
            exceededCount += count;
            
            count = contrib.highPriority.size() +
               contrib.normalPriority.size() + contrib.lowPriority.size();
            if(count > 0)
            {
               addDuration(contrib.highPriority);
               addDuration(contrib.normalPriority);
               addDuration(contrib.lowPriority);
            }
            totalCount += count;
         }
      }
   }
   
   public PostponedActivitiesCalculator(PostponedActivities pActivities)
   {
      this.pActivities = pActivities;
      calulations = new HashMap();
      performCalculation();
   }
   
   private void performCalculation()
   {
      Iterator mIter = pActivities.participationsPerProcess.values().iterator();
      while (mIter.hasNext())
      {
         List participations = (List)mIter.next();
         Iterator pIter = participations.iterator();
         while(pIter.hasNext())
         {
            Participation contrib = (Participation) pIter.next();
            if(CompareHelper.areEqual(contrib.performerKind, 
                  PerformerType.ModelParticipant))
            {
               Long performerOid = new Long(contrib.performerOid);
               ModelParticipantCalculation mCalc = (ModelParticipantCalculation)
                  calulations.get(performerOid);
               if(mCalc == null)
               {
                  mCalc = new ModelParticipantCalculation(performerOid.longValue());
                  calulations.put(performerOid, mCalc);
               }
               mCalc.addParticipation(contrib);
            }
         }
      }
   }
   
   private ModelParticipantCalculation getCalculation(ModelParticipantInfo modelParticipantInfo)
   {
      long runtimeOid = modelParticipantInfo.getRuntimeElementOID();
      return (ModelParticipantCalculation) calulations.get(new Long(runtimeOid));
   }
   
   public Long getTotalCount(ModelParticipantInfo modelParticipantInfo)
   {
      ModelParticipantCalculation mCalc = getCalculation(modelParticipantInfo);
      return mCalc != null ? new Long(mCalc.totalCount) : null;
   }
   
   public String getAvgDuration(ModelParticipantInfo modelParticipantInfo)
   {
      ModelParticipantCalculation mCalc = getCalculation(modelParticipantInfo);
      return mCalc != null ? DateUtils
				.formatDurationInHumanReadableFormat(mCalc.duration
						/ mCalc.totalCount) : null;
   }
   
   public Long getExceededDurationCount(ModelParticipantInfo modelParticipantInfo)
   {
      ModelParticipantCalculation mCalc = getCalculation(modelParticipantInfo);
      return mCalc != null ? new Long(mCalc.exceededCount) : null;
   }

}