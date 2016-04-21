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

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;


/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@DTOClass
public class ProcessInstanceDTO extends AbstractDTO
{
   @DTOAttribute("OID")
   public long oid;

   @DTOAttribute("processName")
   public String processName;

   public String qualifiedId;

   public long processInstanceRootOID;
   
   public long parentProcessInstanceOID; //set only when requested with 'withHierarchyInfo'

   public PriorityDTO priority;

   public Long startTime;

   public String duration;

   public String createUser;

   public Map<String, DescriptorDTO> descriptorValues;

   public Map<String, DescriptorDTO> processDescriptorsValues;

   public Long endTime;

   public String startingUser;

   public Object enableTerminate;

   public Object status;
   
   public boolean enableRecover;

   public boolean checkSelection;

   public boolean modifyProcessInstance;

   public int notesCount;

   public boolean caseInstance;

   public String caseOwner;

   public PriorityDTO oldPriority;
   
   public BenchmarkDTO benchmark;

   public boolean auxillary;
   
   public boolean supportsProcessAttachments;
   
   public String rootProcessName;

   public List<ActivityInstanceDTO> activityInstances;

   public List<DocumentDTO> attachments;
   
   public List<HistoricalDataDTO> historicalData;
}
