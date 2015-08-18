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
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Map;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;


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

   public long processInstanceRootOID;

   public PriorityDTO priority;

   public Long startTime;

   public String duration;

   public String createUser;

   public Map<String, DescriptorDTO> descriptorValues;

   public Object processDescriptorsList;

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
   
}
