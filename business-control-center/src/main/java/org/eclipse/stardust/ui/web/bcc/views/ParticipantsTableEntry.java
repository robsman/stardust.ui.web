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

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;


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
   
   /**
    * @param participantId
    * @param totalCount
    * @param avgDuration
    * @param exceededDurationCount
    */
   public ParticipantsTableEntry(ModelParticipantInfo modelParticipantInfo, long totalCount,
         String avgDuration, long exceededDurationCount)
   {
      super();
      this.modelParticipantInfo = modelParticipantInfo;
      this.totalCount = totalCount;
      this.avgDuration = avgDuration;
      this.exceededDurationCount = exceededDurationCount;
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
  
}
