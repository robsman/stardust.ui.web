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
package org.eclipse.stardust.ui.web.viewscommon.security;

import java.security.Principal;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrincipal;

import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

public class Participant implements Comparable<Participant>
{
   private static final String POSTFIX_OPEN = "[";
   private static final String POSTFIX_CLOSE = "]";

   private Principal principal;
   private String id;
   private String name;
   private boolean selected;

   /**
    * @param modelParticipantInfo
    */
   public Participant(ModelParticipantInfo modelParticipantInfo)
   {
      initialize(modelParticipantInfo);
   }

   /**
    * @param modelParticipantInfo
    */
   private void initialize(ModelParticipantInfo modelParticipantInfo)
   {
      String modelId = ModelUtils.extractModelId(modelParticipantInfo.getQualifiedId());
      principal = new DmsPrincipal(modelParticipantInfo, modelId);
      id = principal.getName();
      if (modelParticipantInfo instanceof org.eclipse.stardust.engine.api.model.Participant)
      {
         name = I18nUtils.getParticipantName((org.eclipse.stardust.engine.api.model.Participant) modelParticipantInfo);
      }
      else
      // scoped roles / departments
      {
         name = I18nUtils.getParticipantName(ParticipantUtils.getParticipant(modelParticipantInfo));
         if (null != modelParticipantInfo.getDepartment())
         {
            name += POSTFIX_OPEN + modelParticipantInfo.getDepartment().getName() + POSTFIX_CLOSE;
         }
      }

      if (StringUtils.isEmpty(name))
      {
         name = principal.getName();
      }
   }

   /**
    * @param userInfo
    * @param realmId
    */
   public Participant(UserInfo userInfo, String realmId)
   {
      principal = new DmsPrincipal(userInfo, realmId);
      id = principal.getName();
      User user = UserUtils.getUser(userInfo.getId());
      name = I18nUtils.getUserLabel(user);
   }

   /**
    * @param userGroupInfo
    */
   public Participant(UserGroupInfo userGroupInfo)
   {
      principal = new DmsPrincipal(userGroupInfo);
      id = principal.getName();
      name = I18nUtils.getUserGroupLabel(userGroupInfo);
   }

   /**
    * @param principal
    * @param participantInfo
    */
   public Participant(Principal principal, ModelParticipantInfo participantInfo)
   {
      if (null != participantInfo)
      {
         initialize(participantInfo);
      }
      else
      {
         this.principal = principal;
         id = principal.getName();
         name = getDisplayName(principal.getName());
         if (StringUtils.isEmpty(name))
         {
            name = id;
         }
      }
   }

   /**
    * @param name
    */
   public Participant(String name)
   {
      this.id = name;
      this.name = getDisplayName(name);
      this.principal = new DmsPrincipal(name);
   }

   /**
    * @param name
    * @return
    */
   private String getDisplayName(String name)
   {
      String displayName = null;
      if (CommonProperties.EVERYONE.equals(name))
      {
         displayName = MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.securityDialog.everyone");
      }
      else if (CommonProperties.ADMINISTRATOR.equals(name) || CommonProperties.ADMINISTRATORS.equals(name))
      {
         this.id = CommonProperties.ADMINISTRATOR;
         displayName = MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.securityDialog.administrators");
      }
      return displayName;
   }

   public String getId()
   {
      return id;
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   public int compareTo(Participant participant)
   {
      return this.getName().compareTo(participant.getName());
   }

   public Principal getPrincipal()
   {
      return principal;
   }

   public String getName()
   {
      return name;
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
      Participant other = (Participant) obj;
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
