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
import org.eclipse.stardust.ui.web.rest.service.utils.ParticipantManagementUtils.ParticipantType;
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
      if (org.eclipse.stardust.common.StringUtils.isNotEmpty(this.type) && !this.type.equals(otherParticipant.type))
      {
         return Integer.valueOf(ParticipantType.getOrder(this.type)).compareTo(
               Integer.valueOf(ParticipantType.getOrder(otherParticipant.type)));
      }
      else if (org.eclipse.stardust.common.StringUtils.isNotEmpty(otherParticipant.type)
            && !otherParticipant.type.equals(this.type))
      {
         return Integer.valueOf(ParticipantType.getOrder(this.type)).compareTo(
               Integer.valueOf(ParticipantType.getOrder(otherParticipant.type)));
      }

      return this.name.compareTo(otherParticipant.name);
   }
   

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((qualifiedId == null) ? 0 : qualifiedId.hashCode());
      result = prime * result + ((uiQualifiedId == null) ? 0 : uiQualifiedId.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ParticipantDTO other = (ParticipantDTO) obj;

      if (uiQualifiedId == null)
      {
         if (other.uiQualifiedId != null)
            return false;
      }
      else if (uiQualifiedId.equals(other.uiQualifiedId))
         return true;

      if (qualifiedId == null)
      {
         if (other.qualifiedId != null)
            return false;
      }
      else if (qualifiedId.equals(other.qualifiedId))
         return true;

      else if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (id.equals(other.id))
         return true;

      return true;
   }
}
