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

package org.eclipse.stardust.ui.web.modeler.portal;

import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.views.PortalConfiguration;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;

/**
 * 
 * @author Marc.Gille
 */
public class ModelingConfigurationPanel
{
   private String defaultProfile;
   private boolean showTechnologyPreview;
   private UserPreferencesHelper userPreferencesHelper;
   
   /**
    * 
    */
   public ModelingConfigurationPanel()
   {
//      userPreferencesHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_WORKFLOW, PortalConfiguration.getInstance()
//            .getPrefScopesHelper().getSelectedPreferenceScope());
   }

   public String getDefaultProfile()
   {
      System.out.println("Get Default Profile: " + defaultProfile);

      return defaultProfile;
   }

   public void setDefaultProfile(String defaultProfile)
   {
      System.out.println("Set Default Profile: " + defaultProfile);
      
      //userPrefsHelper.setXYX()/.getXYZ()
      
      this.defaultProfile = defaultProfile;
   }

   public boolean isShowTechnologyPreview()
   {
      System.out.println("Is Technology Preview: " + showTechnologyPreview);

      return showTechnologyPreview;
   }

   public void setShowTechnologyPreview(boolean showTechnologyPreview)
   {
      System.out.println("Set Technology Preview: " + showTechnologyPreview);
      
      this.showTechnologyPreview = showTechnologyPreview;
   }
}
