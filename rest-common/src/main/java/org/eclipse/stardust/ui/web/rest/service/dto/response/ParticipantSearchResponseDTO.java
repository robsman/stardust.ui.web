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

import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.service.ParticipantSearchComponent;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;

/**
 * @author Yogesh.Manware
 *
 */
//TODO : rename it to ParticipantDTO for better reuse
public class ParticipantSearchResponseDTO extends AbstractDTO
{
   private static final String BASE_IMAGE_PATH = "/plugins/views-common/images/icons/";
   // exposed properties
   public String id;
   public String qualifiedId;
   public long OID;
   public String name;
   public String type;
   public boolean onlineStatus = false;
   public String icon;
   public Long runtimeOrganizationOid;
   public String realmId;
   
   public ParticipantSearchResponseDTO()
   {
   }

   /**
    * @param participant
    */
   public ParticipantSearchResponseDTO(Participant participant)
   {
      this.id = participant.getId();
      this.qualifiedId = participant.getQualifiedId();
      this.OID = ParticipantUtils.getParticipantOID(participant);
      this.name = ParticipantUtils.getParticipantLabel(participant);
      this.type = ParticipantUtils.getParticipantType(participant).name();
      this.icon = determineIconPath(participant);
   }
   
   /**
    * @param user
    */
   public ParticipantSearchResponseDTO(User user)
   {
      this.id = user.getId();
      this.qualifiedId = user.getQualifiedId();
      this.OID = ParticipantUtils.getParticipantOID(user);
      this.name = ParticipantUtils.getParticipantLabel(user);
      this.type = ParticipantUtils.getParticipantType(user).name();
      this.realmId = user.getRealm().getId();
      this.icon = determineIconPath(user);
   }

   /**
    * @param department
    */
   public ParticipantSearchResponseDTO(DepartmentInfo department)
   {
      this.id = department.getId();
      this.OID = department.getOID();
      this.name = ParticipantUtils.getDepartmentLabel(department);
      this.type = ParticipantSearchComponent.PerformerTypeUI.Department.name().toUpperCase();
      this.icon = determineIconPath(null);
      this.runtimeOrganizationOid = department.getRuntimeOrganizationOID();
   }

   /**
    * @param participant
    */
   public ParticipantSearchResponseDTO(Participant participant, boolean online)
   {
      this(participant);
      this.onlineStatus = online;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.user.IParticipantWrapper#getIcon()
    */
   private String determineIconPath(Participant participant)
   {
      if (null != participant)
      {
         if (participant instanceof User)
         {
            return MyPicturePreferenceUtils.getUsersImageURI((User) participant);
         }
         else if (participant instanceof Role)
         {
            return BASE_IMAGE_PATH + "role.png";
         }
         else
         {
            return BASE_IMAGE_PATH + "chart_organisation.png";
         }
      }
      else
      {
         return BASE_IMAGE_PATH + "group_link.png";
      }
   }
}
