/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.rest.service.dto.ModelDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DepartmentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
public interface ParticipantService
{
   
   /**
    * @param participantQidIn
    * @return
    */
   public List<ModelDTO> getParticipantTree(boolean lazyLoad);
   
   /**
    * @param participantQidIn
    * @return
    */
   public List<ParticipantDTO> getParticipant(String participantQidIn);

   /**
    * @param departmentDTO
    * @return
    */
   public List<ParticipantDTO> createModifyDepartment(DepartmentDTO departmentDTO);

   /**
    * @param departmentQualifiedId
    * @return
    */
   public boolean deleteDepartment(String departmentQualifiedId);

   /**
    * @param participants
    * @param usersToBeAdded
    * @param usersToBeRemoved
    * @return
    */
   public Map<String, List<ParticipantDTO>> modifyParticipant(HashSet<String> participants,
         HashSet<String> usersToBeAdded, HashSet<String> usersToBeRemoved, boolean lazyLoad);
}