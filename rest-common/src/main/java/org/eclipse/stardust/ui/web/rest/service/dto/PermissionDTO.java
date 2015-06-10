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
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Set;

import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;

/**
 * 
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class PermissionDTO extends AbstractDTO
{
   public String name = MessagesViewsCommonBean.getInstance().getString("common.unknown");
   public String id = null;
   public Set<ParticipantDTO> allow;
   public Set<ParticipantDTO> deny;
   public Set<PermissionDTO> views;
   public Set<PermissionDTO> launchPanels;

   public PermissionDTO()
   {}

   public PermissionDTO(String id, String name)
   {
      super();
      this.name = name;
      this.id = id;
   }

   /**
    * 
    * @author Yogesh.Manware
    * @version $Revision: $
    */
   public static class ParticipantDTO
   {
      public static ParticipantDTO ALL = new ParticipantDTO("all", MessagesViewsCommonBean.getInstance().getString(
            "views.common.all"));
      public String participantQualifiedId;

      public ParticipantDTO(String qid, String name)
      {
         super();
         this.participantQualifiedId = qid;
         this.name = name;
      }

      public String name;

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((participantQualifiedId == null) ? 0 : participantQualifiedId.hashCode());
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
         if (participantQualifiedId == null)
         {
            if (other.participantQualifiedId != null)
               return false;
         }
         else if (!participantQualifiedId.equals(other.participantQualifiedId))
            return false;
         return true;
      }
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
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
      PermissionDTO other = (PermissionDTO) obj;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      return true;
   }
}