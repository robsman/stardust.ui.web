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
package org.eclipse.stardust.ui.web.processportal.common;

/**
 * Contains image resource links
 * 
 * @author roland.stamm
 * 
 */
public final class Resources
{

   private static final String BASE_PATH = "/plugins/processportal/images/icons/";
   
   public static final String VIEW_NOTES_COLUMNS = "/plugins/processportal/notesTableColumns.xhtml";
   public static final String VIEW_WORKLIST_COLUMNS = "/plugins/processportal/worklistViewColumns.xhtml";

   public final static class Icons
   {

      public static final String getOrganization()
      {
         return BASE_PATH + "organization.png";
      }

      public static final String getRole()
      {
         return BASE_PATH + "role.png";
      }

      public static final String getScopedOrganization()
      {
         return BASE_PATH + "department.png";
      }

      public static final String getScopedRole()
      {
         return BASE_PATH + "role_scoped.png";
      }

      public static final String getUser()
      {
         return BASE_PATH + "user.png";
      }

      public static final String getUserGroup()
      {
         return BASE_PATH + "user-group.png";
      }

      public static final String getAssemblyLine()
      {
         return BASE_PATH + "lightning.png";
      }
   }

}
