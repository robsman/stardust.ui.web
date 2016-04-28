/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.dto;

import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;


/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@DTOClass
public class ActivityInstanceDTO extends AbstractDTO
{
   @DTOAttribute("OID")
   public long activityOID;
   
   public String activityName;

   @DTOAttribute("startTime.time")
   public Long startTime;

   @DTOAttribute("lastModificationTime.time")
   public Long lastModification;

   @DTOAttribute("activity")
   public ActivityDTO activity;

   @DTOAttribute("processInstance")
   public ProcessInstanceDTO processInstance;
   
   @DTOAttribute("modelOID")
   public int modelOID;

   public CriticalityDTO criticality;

   public PriorityDTO priority;

   public String duration;

   public String lastPerformer;

   public StatusDTO status;

   public String assignedTo;
   
   public boolean defaultCaseActivity;
   
   public boolean isCaseInstance;
   
   public boolean abortActivity;
   
   public boolean delegable;
   
   public boolean abortProcess;
   
   @DTOAttribute("qualityAssuranceState")
   public String qualityAssuranceState;
   
   public boolean activatable;
   
   public boolean relocationEligible;

   public Map<String, DescriptorDTO> descriptorValues;
   
   public String completedBy;
   public long completedByOid;
   public UserAttributesDTO completedByDetails;
   
   public String participantPerformer;

   public int notesCount;
   
   public BenchmarkDTO benchmark;

   public boolean auxillary;
   
   public Long resubmissionTime;
   
   public String rootProcessName;

   public NotesResultDTO notes;
   
   public ProcessInstanceDTO startingProcessInstance;
   
   public Boolean isChecklistActivity;
   
   public void setQualityAssuranceState(QualityAssuranceState state)
   {
      if (QualityAssuranceState.IS_QUALITY_ASSURANCE.equals(state) || QualityAssuranceState.IS_REVISED.equals(state))
      {
         qualityAssuranceState = state.toString();
      }
   }

}
