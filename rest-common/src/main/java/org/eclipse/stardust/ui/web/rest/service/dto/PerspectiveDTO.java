/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.LaunchPanel;
import org.eclipse.stardust.ui.web.common.PreferencePage;
import org.eclipse.stardust.ui.web.common.PreferencesDefinition;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.InitializingDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@DTOClass
public class PerspectiveDTO extends AbstractDTO implements InitializingDTO
{
   @DTOAttribute("name")
   public String name;

   @DTOAttribute("label")
   public String label;

   @DTOAttribute("defaultPerspective")
   public Boolean defaultPerspective;

   @DTOAttribute("launchPanels")
   public List<LaunchPanelDTO> launchPanels;

   public Integer iceFacesLaunchPanelCount;

   public Boolean active;
   public String helpUrl;

   public String iconClass;
   public String iconImge;
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.rest.service.dto.builder.InitializingDTO#afterPropertiesSet(java.lang.Object)
    */
   public void afterAttributesSet(Object sourceInstance)
   {
      IPerspectiveDefinition perspective = (IPerspectiveDefinition)sourceInstance;

      int count = 0;
      for (LaunchPanel launchPanel : perspective.getLaunchPanels())
      {
         if (!launchPanel.getInclude().endsWith(".html"))
         {
            count++;
         }
      }
      iceFacesLaunchPanelCount = count;

      String icon = null;
      if (null != perspective.getPreferences())
      {
         PreferencePage helpPreference = perspective.getPreferences().getPreference(PreferencesDefinition.PREF_HELP_DOCUMENTATION);
         helpUrl = helpPreference.getInclude();

         PreferencePage iconPreference = perspective.getPreferences().getPreference(PreferencesDefinition.PREF_ICON);
         if (null != iconPreference)
         {
            icon = iconPreference.getInclude();
         }
      }

      if (StringUtils.isEmpty(icon))
      {
         icon = "pi pi-perspective-default"; // Default Icon
      }

      if (icon.contains("/") && icon.contains(".")) // It's an image URL
      {
         iconImge = icon;
         iconClass = perspective.getName() + " " + PortalApplication.deriveIconClass(icon);
      }
      else
      {
         iconClass = perspective.getName() + " " + icon;
      }
   }
}
