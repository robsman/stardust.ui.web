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

import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;


/**
 * @author ankita.Patel
 * @version $Revision: $
 */
public class ParticipantsTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   
   private ModelParticipantInfo modelParticipantInfo;
   private long totalCount;
   private String avgDuration;
   private long exceededDurationCount;
   private Set<Long> allActivityOIDs;
   private Set<Long> exceededActivityOIDs;
   
   /**
    * @param participantId
    * @param totalCount
    * @param avgDuration
    * @param exceededDurationCount
    */
   public ParticipantsTableEntry(ModelParticipantInfo modelParticipantInfo, long totalCount,
         String avgDuration, long exceededDurationCount, Set<Long> allActivityOIDs, Set<Long> exceededActivityOIDs)
   {
      super();
      this.modelParticipantInfo = modelParticipantInfo;
      this.totalCount = totalCount;
      this.avgDuration = avgDuration;
      this.exceededDurationCount = exceededDurationCount;
      this.allActivityOIDs = allActivityOIDs;
      this.exceededActivityOIDs = exceededActivityOIDs;
   }
   
   public void doPriorityAction(ActionEvent event)
   {
      Map param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      Object showtotalCountAI = param.get("totalCountCol");
      Set<Long> oids = null;
      if (showtotalCountAI != null && StringUtils.isNotEmpty(showtotalCountAI.toString()))
      {
         oids = allActivityOIDs;
      }
      else
      {
         oids = exceededActivityOIDs;
      }
      PostponedActivitiesBean postponedActivityBean = (PostponedActivitiesBean) ManagedBeanUtils.getManagedBean(PostponedActivitiesBean.BEAN_ID);
      postponedActivityBean.fetchActivityAndRefresh(oids);
   }
   
   /**
    * 
    */
   public ParticipantsTableEntry()
   {
   // TODO Auto-generated constructor stub
   }
   public ModelParticipantInfo getModelParticipantInfo()
   {
      return modelParticipantInfo;
   }
   public void setModelParticipantInfo(ModelParticipantInfo modelParticipantInfo)
   {
      this.modelParticipantInfo = modelParticipantInfo;
   }
   public long getTotalCount()
   {
      return totalCount;
   }
   public void setTotalCount(long totalCount)
   {
      this.totalCount = totalCount;
   }
   public String getAvgDuration()
   {
      return avgDuration;
   }
   public void setAvgDuration(String avgDuration)
   {
      this.avgDuration = avgDuration;
   }
   public long getExceededDurationCount()
   {
      return exceededDurationCount;
   }
   public void setExceededDurationCount(long exceededDurationCount)
   {
      this.exceededDurationCount = exceededDurationCount;
   }

   public Set<Long> getActivityOIDs()
   {
      return allActivityOIDs;
   }

   public void setActivityOIDs(Set<Long> activityOIDs)
   {
      this.allActivityOIDs = activityOIDs;
   }
   
}
