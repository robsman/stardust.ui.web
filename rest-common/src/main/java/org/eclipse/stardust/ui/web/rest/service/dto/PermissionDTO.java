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

import java.util.List;

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
   public List<ParticipantDTO> allow;
   public List<ParticipantDTO> deny;
   public List<PermissionDTO> views;
   public List<PermissionDTO> launchPanels;

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
   }
}