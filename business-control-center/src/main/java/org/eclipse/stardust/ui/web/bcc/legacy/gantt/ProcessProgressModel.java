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
package org.eclipse.stardust.ui.web.bcc.legacy.gantt;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;


/**
 * An instance of this class represents a model element being part of the model tree used
 * for the Gantt Diagram View.
 * 
 * @author mueller1
 * 
 */
public class ProcessProgressModel
{
   private String id;

   private String businessId;

   private String businessValue;

   private String name;

   private long estimatedDuration = 0;

   private Date plannedStartTime = null;

   private Date plannedTerminationTime = null;

   private int thresholdInPercentage = 0;

   private boolean ignorable = false;

   private String successorId = null;

   private String predecessorId = null;

   private long elementOid = 0;

   public ProcessProgressModel(ProcessDefinition pDefType, Date referenceDate,
         String businessId)
   {
      this.id = pDefType.getQualifiedId();
      this.name = I18nUtils.getProcessName(pDefType);
      this.elementOid = pDefType.getRuntimeElementOID();

      if (!this.id.equals(businessId))
      {
         this.businessValue = businessId;
      }

      if (!this.id.equals(businessId))
      {
         this.name += " - " + businessId;
      }

      this.businessId = businessId;

      if (!this.id.equals(this.businessId))
      {
         this.businessId = this.businessId.replace(' ', '_');
         this.businessId = this.id + "." + this.businessId;
      }

      estimatedDuration = PropertyProvider.getInstance().hasConfigParam(this.businessId,
            PropertyProvider.ESTIMATED_DURATION_PROPERTY)
            ? getEstimatedDurationFromConfig()
            : getEstimatedDurationFromModel(pDefType);

      thresholdInPercentage = PropertyProvider.getInstance().hasConfigParam(
            this.businessId, PropertyProvider.THRESHOLD_PROPERTY)
            ? getOverdueThresholdFromConfig()
            : getOverdueThresholdFromModel(pDefType);

      if (PropertyProvider.getInstance().isConfigurationExistent(this.businessId))
      {

         String plannedStartTime = PropertyProvider.getInstance().getProperty(
               this.businessId, PropertyProvider.PLANNED_START_TIME_PROPERTY);

         if (plannedStartTime != null && !"".equals(plannedStartTime))
         {
            int pstH = new Integer(plannedStartTime.substring(0, 2)).intValue();
            int pstMin = new Integer(plannedStartTime.substring(2)).intValue();

            Calendar pstC = Calendar.getInstance();
            pstC.setTime(referenceDate);
            pstC.set(Calendar.HOUR_OF_DAY, pstH);
            pstC.set(Calendar.MINUTE, pstMin);
            pstC.set(Calendar.SECOND, 0);
            this.plannedStartTime = pstC.getTime();
            pstC = null;
         }
//         else
//         {
//            throw new RuntimeException("No planned start time defined for item : "
//                  + this.businessId);
//         }

         String plannedTerminationTime = PropertyProvider.getInstance().getProperty(
               this.businessId, PropertyProvider.PLANNED_TERMINATION_TIME_PROPERTY);

         if (plannedTerminationTime != null && !"".equals(plannedTerminationTime))
         {
            int pttH = new Integer(plannedTerminationTime.substring(0, 2)).intValue();
            int pttMin = new Integer(plannedTerminationTime.substring(2)).intValue();
            Calendar pttC = Calendar.getInstance();
            pttC.setTime(referenceDate);
            pttC.set(Calendar.HOUR_OF_DAY, pttH);
            pttC.set(Calendar.MINUTE, pttMin);
            pttC.set(Calendar.SECOND, 0);
            this.plannedTerminationTime = pttC.getTime();
            pttC = null;

         }
//         else
//         {
//            throw new RuntimeException("No planned termination time defined for item : "
//                  + this.businessId);
//         }

         this.successorId = PropertyProvider.getInstance().getProperty(this.businessId,
               PropertyProvider.SUCCESSOR_PROPERTY);
         this.predecessorId = PropertyProvider.getInstance().getProperty(this.businessId,
               PropertyProvider.PREDECESSOR_PROPERTY);

      }
      else if (!PropertyProvider.getInstance().getAllProcessDefinitionIDs().contains(
            businessId))
      {
        // this.ignorable = true;
      }
   }

   private long getEstimatedDurationFromModel(ProcessDefinition pDefType)
   {
      Period period = new Period((String) pDefType
            .getAttribute(PredefinedConstants.PWH_TARGET_EXECUTION_TIME));
      return period != null ? periodToSeconds(period) : 0;
   }

   private long getEstimatedDurationFromConfig()
   {
      String estimatedDuration = PropertyProvider.getInstance().getProperty(
            this.businessId, PropertyProvider.ESTIMATED_DURATION_PROPERTY);
      return new Long(estimatedDuration).longValue() * 1000;
   }

   private int getOverdueThresholdFromModel(ProcessDefinition pDefType)
   {
      String overdueThreshold = (String) pDefType
            .getAttribute(PredefinedConstants.PWH_OVERDUE_THRESHOLD);
      return StringUtils.isEmpty(overdueThreshold) ? 0 : new Integer(overdueThreshold)
            .intValue();
   }

   private int getOverdueThresholdFromConfig()
   {
      String threshold = PropertyProvider.getInstance().getProperty(this.businessId,
            PropertyProvider.THRESHOLD_PROPERTY);
      return new Integer(threshold).intValue();
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Name: ").append(this.name).append("\n");

      buffer.append("Planned Start Time: ").append(this.plannedStartTime).append("\n");
      buffer.append("Planned End Time: ").append(this.plannedTerminationTime)
            .append("\n");
      buffer.append("Estimated Duration: ").append(this.estimatedDuration).append("\n");

      buffer.append("Threshold: ").append(this.thresholdInPercentage).append("\n");

      return buffer.toString();
   }

   public long getEstimatedDuration()
   {
      return estimatedDuration;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public String getNameAsKey()
   {
      // return name.replace(' ', '_');
      return this.getBusinessId();
   }

   public Date getPlannedStartTime()
   {
      return plannedStartTime;
   }

   public Date getPlannedTerminationTime()
   {
      return plannedTerminationTime;
   }

   public int getThresholdInPercentage()
   {
      return thresholdInPercentage;
   }

   public boolean isIgnorable()
   {
      return ignorable;
   }

   public String getSuccessorId()
   {
      return successorId;
   }

   public String getPredecessorId()
   {
      return predecessorId;
   }

   public String getBusinessId()
   {
      return businessId;
   }

   public String getBusinessValue()
   {
      return businessValue;
   }

   public long getElementOid()
   {
      return elementOid;
   }

   public static long periodToSeconds(Period period)
   {
      long result = 0;
      if (period != null)
      {
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(0);
         cal.add(Calendar.SECOND, period.get(Period.SECONDS));
         cal.add(Calendar.MINUTE, period.get(Period.MINUTES));
         cal.add(Calendar.HOUR_OF_DAY, period.get(Period.HOURS));
         cal.add(Calendar.DAY_OF_YEAR, period.get(Period.DAYS));
         cal.add(Calendar.MONTH, period.get(Period.MONTHS));
         cal.add(Calendar.YEAR, period.get(Period.YEARS));
         result = cal.getTimeInMillis();
      }
      return result;
   }
}
