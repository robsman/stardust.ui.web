/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Map;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;


/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@DTOClass
public class ActivityInstanceDTO extends AbstractDTO
{
   @DTOAttribute("OID")
   public long oid;

   @DTOAttribute("startTime.time")
   public Long start;

   @DTOAttribute("lastModificationTime.time")
   public Long lastModification;

   @DTOAttribute("activity")
   public ActivityDTO activity;

   @DTOAttribute("processInstance")
   public ProcessInstanceDTO processInstance;

   public CriticalityDTO criticality;

   @DTOAttribute("processInstance.priority")
   public String priority;

   public String duration;

   public String lastPerformer;

   public StatusDTO status;

   public String assignedTo;
   
   public boolean defaultCaseActivity;
   
   public boolean abortActivity;
   
   public boolean delegable;

   public Map<String, ProcessDescriptor> descriptors;

   public void setPriority(Integer priority) {
      this.priority = ProcessInstanceUtils.getPriorityValue(priority);
   }

}
