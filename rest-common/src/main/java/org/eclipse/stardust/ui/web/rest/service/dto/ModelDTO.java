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

import java.util.List;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantDTO;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class ModelDTO extends AbstractDTO
{

   @DTOAttribute("modelOID")
   public Integer oid;

   @DTOAttribute("id")
   public String id;

   @DTOAttribute("name")
   public String name;

   @DTOAttribute("description")
   public String description;

   public List<ProcessDefinitionDTO> processDefinitions;
   
   public List<ParticipantDTO> allTopLevelOrganizations;
   
   public List<ParticipantDTO> allTopLevelRoles;
   
   public List<DataDTO> data;
}
