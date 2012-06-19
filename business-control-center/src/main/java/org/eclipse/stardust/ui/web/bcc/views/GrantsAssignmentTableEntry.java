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
package org.eclipse.stardust.ui.web.bcc.views;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.common.ParticipantDepartmentPair;


/**
 * @author ankita.Patel
 * @version $Revision: $
 */
public class GrantsAssignmentTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   private ParticipantDepartmentPair participantDepartmentPair;

   private boolean userInRole = false;
   
   /**
    * @param participantDepartmentPair
    * @param userInRole
    */
   public GrantsAssignmentTableEntry(ParticipantDepartmentPair participantDepartmentPair, boolean userInRole)
   {
      super();
      this.participantDepartmentPair = participantDepartmentPair;
      this.userInRole = userInRole;
   }

   /**
    * 
    */
   public GrantsAssignmentTableEntry()
   {
   // TODO Auto-generated constructor stub
   }

   public ParticipantDepartmentPair getParticipantDepartmentPair()
   {
      return participantDepartmentPair;
   }

   public void setParticipantDepartmentPair(
         ParticipantDepartmentPair participantDepartmentPair)
   {
      this.participantDepartmentPair = participantDepartmentPair;
   }

   public boolean isUserInRole()
   {
      return userInRole;
   }

   public void setUserInRole(boolean userInRole)
   {
      this.userInRole = userInRole;
   }

   @Override
   public String toString()
   {
      return participantDepartmentPair + ":" + userInRole;
   }
}
