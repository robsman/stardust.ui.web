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
package org.eclipse.stardust.ui.web.viewscommon.views.authorization;

import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



/**
 * class is type of DefaultRowModel for participant table
 * 
 * @author Vikas.Mishra
 * 
 */
public class ParticipantTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = -364375780924462409L;

   // ~ Instance fields
   // ================================================================================================
   private boolean isRole;
   private boolean selected;
   private final Participant participant;
   private String type;
   private boolean isOrganization;
   //private boolean selectedRow;


   // ~ Constructor
   // ================================================================================================
   public ParticipantTableEntry(Participant participant)
   {
      this.participant = participant;
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();
      if (participant instanceof Role)
      {
         isRole = true;
         type = msgBean.getString("delegation.role");
      }
      else if (participant instanceof Organization)
      {
         isOrganization = true;
         type = msgBean.getString("delegation.organization");
      }
      else//not supported now
      {
         type = msgBean.getString("delegation.user");
      }
   }

   public String getName()
   {     
      return I18nUtils.getParticipantName(participant);
   }

   public String getType()
   {
      return type;
   }

   public boolean isOrganization()
   {
      return isOrganization;
   }

   public boolean isRole()
   {
      return isRole;
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setOrganization(boolean isOrganization)
   {
      this.isOrganization = isOrganization;
   }

   public void setRole(boolean isRole)
   {
      this.isRole = isRole;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public Participant getParticipant()
   {
      return participant;
   }

   
   
}
