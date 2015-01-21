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
import org.eclipse.stardust.ui.web.rest.service.DelegationComponent;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;

/**
 * @author Yogesh.Manware
 *
 */

public class ParticipantSearchResponseDTO
{
   private static final String BASE_IMAGE_PATH = "/plugins/views-common/images/icons/";
   // exposed properties
   private String id;
   private String qualifiedId;
   private long OID;
   private String name;
   private String type;
   private boolean onlineStatus = false;
   private String icon;

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
    * @param department
    */
   public ParticipantSearchResponseDTO(DepartmentInfo department)
   {
      this.id = department.getId();
      this.OID = department.getOID();
      this.name = ParticipantUtils.getDepartmentLabel(department);
      this.type = DelegationComponent.ParticipantType.Department.name().toUpperCase();
      this.icon = determineIconPath(null);
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
