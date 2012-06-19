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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.bcc.legacy.ITimeProvider;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;



/**
 * A ProgressStatus instance contains all information needed when rendering the graphical
 * elements of the Gantt Diagram View. These information will be utilized directly from
 * the ganttDiagramTab.jspx JSF page.
 * 
 * @author mueller1
 * 
 */
public class ProgressStatus
{

   private static Logger logger = LogManager.getLogger(ProgressStatus.class);

   // name of the status
   private String status;

   // name of a color referencing a css style
   private String color;

   // name of a color referencing a css style
   private String progressColor = "darkGreyColor";

   // width of the status bar
   private int barWidth;

   // start position of the status bar
   private int barLeft;

   // percentage value
   private double progress;

   // start position of the progress bar
   private double progressLeft;

   // width of the progress bar
   private int progressWidth;

   // width of the duration bar
   private int durationWidth;

   // height of the dependency line
   private int dependencyHeight = 0;

   // width of the dependency line
   private int dependencyWidth = 0;

   private String completed = "background-color: #FFFFFF;";

   private boolean noteIconStatus = false;

   private boolean errorIconStatus = false;

   private boolean descriptorIconStatus = false;

   private boolean completeIconStatus = false;

   private String noteText = null;

   private String errorText = null;

   private String descriptorText = null;

   private String completeText = null;

   private long now = System.currentTimeMillis();

   private boolean progressCalculated = false;

   private TimeUnit timeUnit;

   private final ProcessProgressModel model;

   private final ProcessProgressInstance instance;

   private final long startingPoint;

   private final ModelTreeItem predecessor;

   private ITimeProvider timeProvider;
   
   private ProgressStatus predecessorNode;

   public ProgressStatus(ProcessProgressModel model, ProcessProgressInstance instance,
         long startingPoint, ModelTreeItem predecessor, TimeUnit timeUnit)
   {
      this.model = model;
      this.instance = instance;
      this.startingPoint = startingPoint;
      this.predecessor = predecessor;
      this.timeUnit = timeUnit;
      timeProvider = (ITimeProvider) Reflect.createInstance((PropertyProvider
            .getInstance().getTimeProviderClassName()));
      this.now = timeProvider.getCurrentTime();

      calculateDiagramBars();
   }

   public void calculateDiagramBars()
   {
      long startTime = instance != null ? instance.getStartTime().getTime() : 0;

      // calculate status and color
      this.calculateStatus(model, instance, predecessor);

      if (model.getPlannedStartTime() != null
            && model.getPlannedTerminationTime() != null)
      {
         // calculate barWidth
         this.calculateBarWidth(model.getPlannedStartTime(), model
               .getPlannedTerminationTime());

         // calculate barLeft
         this.calculateBarLeft(model.getPlannedStartTime(), startingPoint);
      }

      // calculate progressLeft
      this.calculateProgressLeft(startTime, startingPoint);

      // calculate progressWidth
      Calendar calculatedStartDate = timeUnit.calculateStartDate(startingPoint);
      this.progressWidth = 0;
      long duration = instance != null ? instance.getDuration() : 0;
      if (instance != null)
      {
         if (instance.getTerminationTime() != null)
         {
            this.progressWidth = new BigDecimal(new Long(duration).doubleValue()).divide(
                  new BigDecimal(1000), BigDecimal.ROUND_HALF_UP).divide(
                  new BigDecimal(60), BigDecimal.ROUND_HALF_UP).intValue();
         }
         else
         {
            BigDecimal nowBD = new BigDecimal(new Long(timeProvider.getCurrentTime()
                  - calculatedStartDate.getTimeInMillis()).doubleValue());
            double nowInTimeUnit = nowBD.divide(new BigDecimal(1000), 1,
                  BigDecimal.ROUND_HALF_UP).divide(new BigDecimal(60), 1,
                  BigDecimal.ROUND_HALF_UP).doubleValue();
            this.progressWidth = timeUnit.calculateSize(nowInTimeUnit)
                  - getProgressLeft();
         }
      }

      if (model.getEstimatedDuration() != 0)
      {
         // calculate progress
         this.calculateProgress(duration, model.getEstimatedDuration());
         progressCalculated = true;
      }

      this.calculateIconStatus(instance);

      // calculate durationWidth
      this.durationWidth = new Long(model.getEstimatedDuration()).intValue() / 1000 / 60;

      if (model.getSuccessorId() != null && !"".equals(model.getSuccessorId()))
      {
         this.dependencyHeight = 0;
//         this.dependencyWidth = 1;
      }

      // completed process instance
      if (instance != null && instance.getTerminationTime() != null)
      {
         this.completed = "background-color: #EEEFF7;";
         // this.durationWidth = 0;
         // this.barWidth = 0;
         // this.progressColor = this.color;
      }
   }

   public void addDependencyHeight()
   {
      this.dependencyHeight = this.dependencyHeight + 38; // 53;
   }

   public void removeDependencyHeight()
   {
      this.dependencyHeight = this.dependencyHeight - 38; // 53;
   }

   public String getStatus()
   {
      return status;
   }

   public String getColor()
   {
      return color;
   }

   public int getDurationWidth()
   {
      return timeUnit.calculateSize(durationWidth);
   }

   public double getProgress()
   {
      return progress;
   }

   public boolean isProgressCalculated()
   {
      return progressCalculated;
   }

   public String getProgressTextLeft()
   {

      double l = new Double(getProgressLeft()).doubleValue();
      double p = new Double(getProgressWidth()).doubleValue();
      double d = new Double(getDurationWidth()).doubleValue();

      l = l + 10;

      if (p > d)
      {
         l = l + p;
      }
      else
      {
         l = l + d;
      }

      return l + "";
   }

   public int getProgressLeft()
   {
      return timeUnit.calculateSize(progressLeft);
   }

   public int getBarLeft()
   {
      return timeUnit.calculateSize(barLeft);
   }

   public int getBarWidth()
   {
      return timeUnit.calculateSize(barWidth);
   }

   public int getProgressWidth()
   {
      // there is a progress
      if (instance != null)
      {
         progressWidth = Math.max(progressWidth, 1);
      }
      return progressWidth;
   }

   public int getDependencyWidth()
   {
      return predecessorNode != null ? 1 : dependencyWidth;
   }

   public void setDependencyWidth(int dependencyWidth)
   {
      this.dependencyWidth = dependencyWidth;
   }

   private void calculateIconStatus(ProcessProgressInstance instance)
   {
      if (instance != null && instance.getTerminationTime() != null)
      {
         this.completeIconStatus = true;
      }
      else
      {
         this.completeIconStatus = false;
      }

      this.completeText = "";

      if (instance != null && !instance.getDescriptors().isEmpty())
      {
         this.descriptorIconStatus = true;

         List<ProcessDescriptor> processdescList = CommonDescriptorUtils.createProcessDescriptors(
               instance.getProcessInstance(), false);

         StringBuffer buffer = new StringBuffer();
         for (ProcessDescriptor processDescriptors : processdescList)
         {
            if (StringUtils.isNotEmpty(buffer.toString()))
            {
               buffer.append(", ");
            }
            buffer.append(processDescriptors.getKey()).append(" : ").append(processDescriptors.getValue());
         }
         
         this.descriptorText = buffer.toString();

         if (instance.getNote() != null)
         {
            this.noteIconStatus = true;
            this.noteText = instance.getNote();
         }
         else
         {
            this.noteIconStatus = false;
         }

         if (instance.getError() != null)
         {
            this.errorIconStatus = true;
            this.errorText = instance.getError();
         }
         else
         {
            this.errorIconStatus = false;
         }
      }
      else
      {
         this.descriptorIconStatus = false;
         this.noteIconStatus = false;
         this.descriptorIconStatus = false;
      }

   }

   private void calculateStatus(ProcessProgressModel model,
         ProcessProgressInstance instance, ModelTreeItem predecessor)
   {

      if (instance != null)
      {
         logger.info("Calculate progress status for instance with id : " + model.getId());
      }

      this.status = "scheduled";
      this.color = "scheduled";

      // running
      if (instance != null)
      {
         this.status = "running";
         this.color = "running";
      }

      if (model.getPlannedTerminationTime() != null)
      {
         // running late
         if (this.isProcessRunningLate(model, instance))
         {
            this.status = "running late";
            this.color = "runningLate";
         }

         // running critical
         if (this.isProcessRunningCritical(model, instance))
         {
            this.status = "running critical";
            this.color = "runningCritical";
         }
      }

      if (instance != null && instance.getTerminationTime() != null)
      {
         this.color = this.color + "Completed";
      }

      this.color = this.color + "Color";

      if (predecessor != null && predecessor.getProgressStatus() != null)
      {
         this.status = predecessor.getProgressStatus().getStatus();
         this.color = predecessor.getProgressStatus().getColor();
      }
   }

   // private void calculateStatusGP3(ProcessProgressModel model,
   // ProcessProgressInstance instance) {
   //
   // // scheduled
   // if (!this.isPlannedStartTimeExceeded(model, instance)
   // && instance == null) {
   // this.status = "scheduled";
   // this.color = "scheduledColor";
   // }
   //
   // // waiting
   // if (this.isPlannedStartTimeExceeded(model, instance)
   // && instance == null) {
   // this.status = "waiting";
   // this.color = "waitingColor";
   // }
   //
   // // running
   // if (this.isPlannedStartTimeExceeded(model, instance)
   // && instance != null
   // && !this.isProcessRunningLate(model, instance)) {
   // this.status = "running";
   // this.color = "runningColor";
   // }
   //
   // // completed
   // if (instance != null && instance.getTerminationTime() != null
   // && !this.isPlannedTerminationTimeExceeded(model, instance)) {
   // this.status = "completed";
   // this.color = "completedColor";
   // }
   //
   // // running late
   // if (this.isProcessRunningLate(model, instance)) {
   // this.status = "running late";
   // this.color = "runningLateColor";
   // }
   //
   // // running early
   // if (this.isProcessRunningCritical(model, instance) && instance != null
   // && !isProcessRunningLate(model, instance)) {
   // this.status = "running early";
   // this.color = "runningEarlyColor";
   // }
   // }

   private void calculateBarWidth(Date plannedStartTime, Date plannedTerminationTime)
   {
      Calendar start = Calendar.getInstance();
      start.setTimeInMillis(plannedStartTime.getTime());

      Calendar end = Calendar.getInstance();
      end.setTimeInMillis(plannedTerminationTime.getTime());

      long d = new Long(end.getTimeInMillis() - start.getTimeInMillis()).longValue();
      d = d / 1000 / 60;

      this.barWidth = new Long(d).intValue();

   }

   private void calculateBarLeft(Date plannedStartTime, long startingPoint)
   {

      // Calendar c = Calendar.getInstance();
      // c.setTimeInMillis(startingPoint);
      //
      // c.set(Calendar.MINUTE, 0);
      // c.set(Calendar.SECOND, 0);
      // c.set(Calendar.MILLISECOND, 0);

      Calendar calculatedStartDate = timeUnit.calculateStartDate(startingPoint);
      long difference = plannedStartTime.getTime()
            - calculatedStartDate.getTimeInMillis();

      this.barLeft = new Long(difference / 1000 / 60).intValue();

   }

   private void calculateProgress(long duration, long plannedDuration)
   {
      BigDecimal currentDuration = new BigDecimal(new Long(duration).doubleValue());
      BigDecimal estimatedDuration = new BigDecimal(new Long(plannedDuration)
            .doubleValue());
      BigDecimal currentProgress = currentDuration.divide(estimatedDuration, 4,
            BigDecimal.ROUND_HALF_UP);
      this.progress = currentProgress.doubleValue();
   }

   private void calculateProgressLeft(long startTime, long startingPoint)
   {
      if (startTime == 0)
      {
         this.progressLeft = this.barLeft;
      }
      else
      {

         // Calendar c = Calendar.getInstance();
         // c.setTimeInMillis(startingPoint);
         //
         // c.set(Calendar.MINUTE, 0);
         // c.set(Calendar.SECOND, 0);
         // c.set(Calendar.MILLISECOND, 0);

         Calendar calculatedStartDate = timeUnit.calculateStartDate(startingPoint);
         long difference = startTime - calculatedStartDate.getTimeInMillis();

         progressLeft = new BigDecimal(difference).divide(new BigDecimal(1000), 2,
               BigDecimal.ROUND_HALF_UP).divide(new BigDecimal(60), 2,
               BigDecimal.ROUND_HALF_UP).doubleValue();
      }
   }

   private boolean isPlannedStartTimeExceeded(ProcessProgressModel model,
         ProcessProgressInstance instance)
   {
      if (instance == null)
      {
         return model.getPlannedStartTime().before(new Date(this.now));
      }
      else
      {
         return model.getPlannedStartTime().before(instance.getStartTime());
      }
   }

   private boolean isPlannedTerminationTimeExceeded(ProcessProgressModel model,
         ProcessProgressInstance instance)
   {
      if (instance == null)
      {
         return model.getPlannedTerminationTime().before(new Date(this.now));
      }
      else
      {
         return instance.getTerminationTime() != null ? model.getPlannedTerminationTime()
               .before(instance.getTerminationTime()) : model.getPlannedTerminationTime()
               .before(new Date(this.now));
      }
   }

   private boolean isProcessRunningLate(ProcessProgressModel model,
         ProcessProgressInstance instance)
   {
      boolean runningLate = false;

      long start = instance != null ? instance.getStartTime().getTime() : this.now;
      long end = model.getPlannedTerminationTime().getTime();

      if ((start + model.getEstimatedDuration()) > end)
      {
         runningLate = true;
      }

      long currentDuration = instance != null ? instance.getDuration() : 0;

      if (currentDuration > model.getEstimatedDuration())
      {
         runningLate = true;
      }

      return runningLate;
   }

   private boolean isProcessRunningCritical(ProcessProgressModel model,
         ProcessProgressInstance instance)
   {

      boolean runningCritcal = false;

      long start = instance != null ? instance.getStartTime().getTime() : this.now;
      long end = model.getPlannedTerminationTime().getTime();

      if (start > end)
      {
         runningCritcal = true;
      }

      long currentDuration = instance != null ? instance.getDuration() : 0;
      long durationThreshold = model.getEstimatedDuration() / 100;
      durationThreshold = durationThreshold * (100 + model.getThresholdInPercentage());

      if (currentDuration > durationThreshold)
      {
         runningCritcal = true;
      }

      return runningCritcal;
   }

   public int getDependencyHeight()
   {
      return predecessorNode != null ? predecessorNode.getDependencyHeight() : dependencyHeight;
   }

   public void setDependencyHeight(int dependencyHeight)
   {
      this.dependencyHeight = dependencyHeight;
   }

   public String getCompleted()
   {
      return completed;
   }

   public void setCompleted(String completed)
   {
      this.completed = completed;
   }

   public String getProgressColor()
   {
      return progressColor;
   }

   public void setProgressColor(String progressColor)
   {
      this.progressColor = progressColor;
   }

   public String getNoteText()
   {
      return noteText;
   }

   public String getErrorText()
   {
      return errorText;
   }

   public String getDescriptorText()
   {
      return descriptorText;
   }

   public String getCompleteText()
   {
      return completeText;
   }

   public boolean isNoteIconStatus()
   {
      return noteIconStatus;
   }

   public boolean isErrorIconStatus()
   {
      return errorIconStatus;
   }

   public boolean isDescriptorIconStatus()
   {
      return descriptorIconStatus;
   }

   public boolean isCompleteIconStatus()
   {
      return completeIconStatus;
   }

   public TimeUnit getTimeUnit()
   {
      return timeUnit;
   }

   public void setTimeUnit(TimeUnit timeUnit)
   {
      this.timeUnit = timeUnit;
   }
   
   public void setPredecessorNode(ProgressStatus predecessorNode)
   {
      this.predecessorNode = predecessorNode;
   }

}
