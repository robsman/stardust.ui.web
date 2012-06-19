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
package org.eclipse.stardust.ui.web.viewscommon.participantspanel;

/**
 * @author Subodh.Godbole
 *
 */
public class ParticipantsTableEntryDyna
{
   private String projectName;
   private String role;
   private String manager;
   
   public ParticipantsTableEntryDyna(String projectName, String role, String manager)
   {
      this.projectName = projectName;
      this.role = role;
      this.manager = manager;
   }
   
   public String getProjectName()
   {
      return projectName;
   }
   public void setProjectName(String projectName)
   {
      this.projectName = projectName;
   }
   public String getRole()
   {
      return role;
   }
   public void setRole(String role)
   {
      this.role = role;
   }
   public String getManager()
   {
      return manager;
   }
   public void setManager(String manager)
   {
      this.manager = manager;
   }

   @Override
   public String toString()
   {
      return projectName + ":" + role + ":" + manager;
   }
}
