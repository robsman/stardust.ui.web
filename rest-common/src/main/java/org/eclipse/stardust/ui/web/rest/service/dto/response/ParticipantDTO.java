/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.service.dto.response;

import java.util.List;
import java.util.UUID;

import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ModelDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;

/**
 * @author Yogesh.Manware
 *
 */
public class ParticipantDTO extends AbstractDTO implements Comparable<ParticipantDTO>
{
   // exposed properties
   public String id;
   public String qualifiedId;
   public long OID;
   public String name;
   public String type;
   public Boolean onlineStatus;
   public Long runtimeOrganizationOid;
   public String realmId;
   public String uiQualifiedId;
   public String parentDepartmentName;
   public String organization; // used only for departments
   public String description; // used only for departments
   public List<ParticipantDTO> children;
   public String uuid = UUID.randomUUID().toString(); //necessary for tree directive

   public ParticipantDTO()
   {}

   /**
    * @param participant
    */
   public ParticipantDTO(Participant participant)
   {
      this.id = participant.getId();
      this.qualifiedId = participant.getQualifiedId();
      this.OID = ParticipantUtils.getParticipantOID(participant);
      this.name = ParticipantUtils.getParticipantLabel(participant);
      this.type = ParticipantUtils.getParticipantType(participant).name();
      this.description = participant.getDescription();
   }

   /**
    * @param user
    */
   public ParticipantDTO(User user)
   {
      this.id = user.getId();
      this.qualifiedId = user.getQualifiedId();
      this.OID = ParticipantUtils.getParticipantOID(user);
      this.name = ParticipantUtils.getParticipantLabel(user);
      this.type = ParticipantUtils.getParticipantType(user).name();
      this.realmId = user.getRealm().getId();
   }

   /**
    * @param department
    */
   public ParticipantDTO(DepartmentInfo department)
   {
      this.id = department.getId();
      this.OID = department.getOID();
      this.name = ParticipantUtils.getDepartmentLabel(department);
      this.type = ParticipantUtils.ParticipantType.DEPARTMENT.name();
      this.runtimeOrganizationOid = department.getRuntimeOrganizationOID();
      if (department instanceof Department)
      {
         Department dep = (Department) department;
         this.description = dep.getDescription();
         this.organization = ParticipantUtils.getParticipantLabel(dep.getOrganization());
      }

   }
   
   public ParticipantDTO(ModelDTO modelDto)
   {
      this.id = modelDto.id;
      this.qualifiedId = modelDto.id;
      this.OID = modelDto.oid;
      this.name = modelDto.name;
      this.description = modelDto.description;
      this.children = modelDto.children;
   }

   /**
    * @param participant
    */
   public ParticipantDTO(Participant participant, boolean online)
   {
      this(participant);
      this.onlineStatus = online;
   }

   @Override
   public int compareTo(ParticipantDTO otherParticipant)
   {
      return this.name.compareToIgnoreCase(otherParticipant.name);
   }
}
