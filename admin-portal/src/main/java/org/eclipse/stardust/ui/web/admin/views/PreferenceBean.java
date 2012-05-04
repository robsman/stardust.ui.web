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

import org.eclipse.stardust.ui.web.viewscommon.user.UserAutocompleteMultiSelector;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class PreferenceBean
{

   private String scope;
   private String moduleId;
   private String preferenceId;
   private String preferenceName;
   private String preferenceValue;
   private UserAutocompleteMultiSelector userSelector;

   public PreferenceBean()
   {
      super();
      userSelector = new UserAutocompleteMultiSelector(false, true);
      userSelector.setShowOnlineIndicator(false);
   }

   public PreferenceBean(String scope, String moduleId, String preferenceId, String preferenceName,
         String preferenceValue, UserAutocompleteMultiSelector userSelector)
   {
      super();
      this.scope = scope;
      this.moduleId = moduleId;
      this.preferenceId = preferenceId;
      this.preferenceName = preferenceName;
      this.preferenceValue = preferenceValue;
      this.userSelector = userSelector;
   }

   public String getScope()
   {
      return scope;
   }

   public void setScope(String scope)
   {
      this.scope = scope;
   }

   public String getModuleId()
   {
      return moduleId;
   }

   public void setModuleId(String moduleId)
   {
      this.moduleId = moduleId;
   }

   public String getPreferenceId()
   {
      return preferenceId;
   }

   public void setPreferenceId(String preferenceId)
   {
      this.preferenceId = preferenceId;
   }

   public String getPreferenceName()
   {
      return preferenceName;
   }

   public void setPreferenceName(String preferenceName)
   {
      this.preferenceName = preferenceName;
   }

   public String getPreferenceValue()
   {
      return preferenceValue;
   }

   public void setPreferenceValue(String preferenceValue)
   {
      this.preferenceValue = preferenceValue;
   }

   public UserAutocompleteMultiSelector getUserSelector()
   {
      return userSelector;
   }

}
