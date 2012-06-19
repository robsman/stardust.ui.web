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
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.Date;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;


public class UserManagerActivityListUserObject extends DefaultRowModel
{
   private boolean select;

   private String name;

   private String activityId;

   private String activityOid;

   private String processOid;

   private String processId;

   private String status;

   private String participantPerformer;

   private String currentUserPerformer;

   private Date startTime;

   private Date lastModification;

   private String descriptors;
   
   private String processInstanceName;

   /**
    * @param select
    * @param name
    * @param descriptors
    * @param activityId
    * @param activityOid
    * @param processOid
    * @param processId
    * @param status
    * @param participantPerformer
    * @param currentUserPerformer
    * @param startTime
    * @param lastModification
    * @param processInstanceName
    */
   public UserManagerActivityListUserObject(boolean select, String name,
         String descriptors, String activityId, String activityOid, String processOid,
         String processId, String status, String participantPerformer,
         String currentUserPerformer, Date startTime, Date lastModification , String processInstanceName)
   {
      this.select = select;
      this.name = name;
      this.descriptors = descriptors;
      this.activityId = activityId;
      this.activityOid = activityOid;
      this.processOid = processOid;
      this.processId = processId;
      this.status = status;
      this.participantPerformer = participantPerformer;
      this.currentUserPerformer = currentUserPerformer;
      this.startTime = startTime;
      this.lastModification = lastModification;
      this.processInstanceName = processInstanceName;
   }

   public boolean isSelect()
   {
      return select;
   }

   public void setSelect(boolean select)
   {
      this.select = select;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getActivityId()
   {
      return activityId;
   }

   public void setActivityId(String activityId)
   {
      this.activityId = activityId;
   }

   public String getActivityOid()
   {
      return activityOid;
   }

   public void setActivityOid(String activityOid)
   {
      this.activityOid = activityOid;
   }

   public String getProcessOid()
   {
      return processOid;
   }

   public void setProcessOid(String processOid)
   {
      this.processOid = processOid;
   }

   public String getProcessId()
   {
      return processId;
   }

   public void setProcessId(String processId)
   {
      this.processId = processId;
   }

   public String getStatus()
   {
      return status;
   }

   public void setStatus(String status)
   {
      this.status = status;
   }

   public String getParticipantPerformer()
   {
      return participantPerformer;
   }

   public void setParticipantPerformer(String participantPerformer)
   {
      this.participantPerformer = participantPerformer;
   }

   public String getCurrentUserPerformer()
   {
      return currentUserPerformer;
   }

   public void setCurrentUserPerformer(String currentUserPerformer)
   {
      this.currentUserPerformer = currentUserPerformer;
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public void setStartTime(Date startTime)
   {
      this.startTime = startTime;
   }

   public Date getLastModification()
   {
      return lastModification;
   }

   public void setLastModification(Date lastModification)
   {
      this.lastModification = lastModification;
   }

   public String getDescriptors()
   {
      return descriptors;
   }

   public void setDescriptors(String descriptors)
   {
      this.descriptors = descriptors;
   }
   
   public String getProcessInstanceName()
   {
      return processInstanceName;
   }

   public void setProcessInstanceName(String processInstanceName)
   {
      this.processInstanceName = processInstanceName;
   }

}
