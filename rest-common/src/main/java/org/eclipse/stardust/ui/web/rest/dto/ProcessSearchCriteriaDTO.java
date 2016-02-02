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
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.dto;

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

import com.google.gson.JsonObject;

@DTOClass
public class ProcessSearchCriteriaDTO extends AbstractDTO
{
   public Long filterObject;
   
   public Date procStartFrom;

   public Date procStartTo;

   public Date procEndFrom;

   public Date procEndTo;

   public String procSearchHierarchySelected;

   public List<ProcessDefinitionDTO> procSrchProcessSelected;

   public String procSrchStateSelected;

   public String processSrchPrioritySelected;

   public String processSrchCaseOwner;

   public String processSrchRootProcessOID;

   public String processSrchProcessOID;

   public List<ActivityDTO> activitySrchSelected;

   public Date actStartFrom;

   public Date actStartTo;

   public Date actModifyFrom;

   public Date actModifyTo;

   public String activitySrchStateSelected;

   public String activitySrchCriticalitySelected;

   public String activitySrchActivityOID;

   public String activitySrchPerformer;
   
   public JsonObject descriptors;
   
   public boolean showAuxiliaryProcess;    
   
   public boolean showInteractiveActivities;
   
   public boolean showNonInteractiveActivities;
   
   public boolean showAuxiliaryActivities;
}
