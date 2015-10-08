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
   public static final String VIEW_NOTES_COLUMNS = "/plugins/processportal/notesTableColumns.xhtml";
   public static final String VIEW_WORKLIST_COLUMNS = "/plugins/processportal/worklistViewColumns.xhtml";

   public final static class Icons
   {
      public static final String getOrganization()
      {
         return "pi pi-organization pi-lg";
      }

      public static final String getRole()
      {
         return "pi pi-role pi-lg";
      }

      public static final String getScopedOrganization()
      {
         return "pi pi-scope-organization pi-lg";
      }

      public static final String getScopedRole()
      {
         return "pi pi-scope-role pi-lg";
      }

      public static final String getUser()
      {
         return "pi pi-user pi-lg";
      }

      public static final String getUserGroup()
      {
         return "pi pi-user-group pi-lg";
      }

      public static final String getAssemblyLine()
      {
         return "pi pi-assembly-line pi-lg";
      }
   }

}
