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
package org.eclipse.stardust.ui.web.admin.views;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class PreferenceManagerTableEntry extends DefaultRowModel
{

   /**
    * 
    */
   private String scope;
   private String moduleId;
   private String preferenceId;
   private String preferenceName;
   private String preferenceValue;
   private boolean selected;

   public PreferenceManagerTableEntry(String scope, String moduleId, String preferenceId, String preferenceName,
         String preferenceValue, String partitionId, String userName)
   {
      super();
      this.scope = scope + getPreferenceScope(partitionId, userName);
      this.moduleId = moduleId;
      this.preferenceId = preferenceId;
      this.preferenceName = preferenceName;
      this.preferenceValue = preferenceValue;
   }

   public String getScope()
   {
      return scope;
   }

   public String getModuleId()
   {
      return moduleId;
   }

   public String getPreferenceId()
   {
      return preferenceId;
   }

   public String getPreferenceName()
   {
      return preferenceName;
   }

   public String getPreferenceValue()
   {
      return preferenceValue;
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   /**
    * Utility method which returns the User Full Name or Partition Id for preference
    * 
    * @param partitionId
    * @param userName
    * @return
    */
   private String getPreferenceScope(String partitionId, String userName)
   {
      if (null != userName)
      {
         return "(" + userName + ")";
      }
      else
      {
         return " (" + partitionId + ")";
      }

   }

}
