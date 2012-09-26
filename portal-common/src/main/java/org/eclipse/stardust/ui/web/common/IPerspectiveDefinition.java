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
package org.eclipse.stardust.ui.web.common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author robert.sauer
 * @version $Revision: $
 */
public interface IPerspectiveDefinition extends Serializable
{
   public String getName();
   
   public void setName(String name);

   public void setMessages(MessageSource messages);
   
   public String getRequiredRoles();
   
   public void setRequiredRoles(String requiredRoles);
   
   public Set<String> getRequiredRolesSet();

   public String getExcludeRoles();

   public void setExcludeRoles(String excludeRoles);

   public Set<String> getExcludeRolesSet();

   public String getLabel();

   public PreferencesDefinition getPreferences();

   public void setPreferences(PreferencesDefinition preferences);
   
   public boolean isDefaultPerspective();
   
   public void setDefaultPerspective(boolean defaultPerspective);
   
   public boolean addExtension(PerspectiveExtension extension);

   public boolean removeExtension(String extensionName);

   public boolean addMenuSection(MenuSection menuSection);

   public List<MenuSection> getMenuSections();

   public boolean addLaunchPanel(LaunchPanel launchPanel);

   public List<LaunchPanel> getLaunchPanels();

   public boolean addToolbarSection(ToolbarSection toolbarSection);

   public List<ToolbarSection> getToolbarSections();

   public boolean addView(ViewDefinition view);

   public List<ViewDefinition> getViews();

   public LaunchPanel getLaunchPanel(String name);
   
   public MenuSection getMenuSection(String name);

   public ToolbarSection getToolbarSection(String name);

   public ViewDefinition getViewDefinition(String name);
   
   public String getController();
   
   public void setController(String controller);
   
   public Map<String, PerspectiveExtension> getExtensions();
}
