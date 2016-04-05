/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.viewscommon.security.Participant;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class ResourcePolicyDTO extends AbstractDTO
{
   public ParticipantDTO participant;
   
   public String participantQualifiedId; // convenient field, used only in PUT

   // allow, deny, empty
   public String read;

   public String modify;

   public String create; //used only for folders

   public String delete;

   public String readAcl;

   public String modifyAcl;

   public static class ParticipantDTO extends AbstractDTO
   {
      public String name;
      public String qualifiedId;

      public ParticipantDTO()
      {}

      public ParticipantDTO(Participant participant)
      {
         this.name = participant.getName();
         this.qualifiedId = participant.getPrincipal().getName();
      }
   }
}
