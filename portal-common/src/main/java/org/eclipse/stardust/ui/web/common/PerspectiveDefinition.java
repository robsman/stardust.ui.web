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

import static org.eclipse.stardust.ui.web.common.PerspectiveUtils.mergeExtensions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.stardust.ui.web.common.impl.HierarchicalMessageSource;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class PerspectiveDefinition implements IPerspectiveDefinition, Serializable, MessageSourceProvider
{
   private static final long serialVersionUID = 1L;

   private String name;

   private MessageSource messages;
   
   private String requiredRoles;
   
   private Set<String> requiredRolesSet;
   
   private String excludeRoles;

   private Set<String> excludeRolesSet;

   private SortedMap<String, PerspectiveExtension> extensions = new TreeMap<String, PerspectiveExtension>();

   private MenuDefinition defaultMenu = new MenuDefinition();

   private List<MenuSection> menuSections;

   private LaunchpadDefinition defaultLaunchpad = new LaunchpadDefinition();

   private List<LaunchPanel> launchPanels;

   private ToolbarDefinition defaultToolbar = new ToolbarDefinition();

   private List<ToolbarSection> toolbarSections;

   private ViewDefinitions defaultViews = new ViewDefinitions();

   private List<ViewDefinition> views;

   private PreferencesDefinition preferences;
   
   private boolean defaultPerspective;
   
   private String controller;

   public PerspectiveDefinition()
   {
      defaultMenu.setMessagesProvider(this);
      defaultLaunchpad.setMessagesProvider(this);
      defaultToolbar.setMessagesProvider(this);
      defaultViews.setMessagesProvider(this);
   }

   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }

   public MessageSource getMessages()
   {
      return messages;
   }
   
   public void setMessages(MessageSource messages)
   {
      this.messages = messages;
   }

   public String getRequiredRoles()
   {
      return requiredRoles;
   }
   
   public void setRequiredRoles(String requiredRoles)
   {
      this.requiredRoles = requiredRoles;
   }
   
   public Set<String> getRequiredRolesSet()
   {
      if(requiredRolesSet == null)
      {
         requiredRolesSet = StringUtils.splitUnique(requiredRoles, ",");
      }

      return requiredRolesSet;
   }

   public String getExcludeRoles()
   {
      return excludeRoles;
   }

   public void setExcludeRoles(String excludeRoles)
   {
      this.excludeRoles = excludeRoles;
   }

   public Set<String> getExcludeRolesSet()
   {
      if(excludeRolesSet == null)
      {
         excludeRolesSet = StringUtils.splitUnique(excludeRoles, ",");
      }

      return excludeRolesSet;
   }

   public boolean isDefaultPerspective()
   {
      return defaultPerspective;
   }

   public void setDefaultPerspective(boolean defaultPerspective)
   {
      this.defaultPerspective = defaultPerspective;
   }

   public String getLabel()
   {
      String key = "perspectives." + getName() + ".label";
      return (getMessages() != null) ? getMessages().getMessage(key, null) : getName();
   }

   public PreferencesDefinition getPreferences()
   {
      return preferences;
   }

   public void setPreferences(PreferencesDefinition preferences)
   {
      this.preferences = preferences;
   }

   public String getController()
   {
      return controller;
   }

   public void setController(String controller)
   {
      this.controller = controller;
   }

   public boolean addExtension(PerspectiveExtension extension)
   {
      if (!extensions.containsKey(extension.getName())
            && (extension.getTargetPerspectiveSet().contains("*") 
                  || extension.getTargetPerspectiveSet().contains(getName())))
      {
         extensions.put(extension.getName(), extension);

         if ((null != messages)
               && (extension.getMessages() instanceof HierarchicalMessageSource))
         {
            ((HierarchicalMessageSource) extension.getMessages())
                  .setParentMessageSource(messages);
         }

         menuSections = null;
         launchPanels = null;
         toolbarSections = null;

         return true;
      }
      return false;
   }

   public boolean removeExtension(String extensionName)
   {
      if (extensions.containsKey(extensionName))
      {
         extensions.remove(extensionName);

         menuSections = null;
         launchPanels = null;
         toolbarSections = null;

         return true;
      }
      return false;
   }

   public boolean addMenuSection(MenuSection menuSection)
   {
      return this.defaultMenu.addElement(menuSection);
   }

   public List<MenuSection> getMenuSections()
   {
      if (null == menuSections)
      {
         this.menuSections = new ArrayList<MenuSection>(defaultMenu.getElements());

         for (PerspectiveExtension extension : extensions.values())
         {
            mergeExtensions(menuSections, extension.getMenuExtensions());
         }
      }

      return menuSections;
   }

   public boolean addLaunchPanel(LaunchPanel launchPanel)
   {
      return this.defaultLaunchpad.addElement(launchPanel);
   }

   public List<LaunchPanel> getLaunchPanels()
   {
      if (null == launchPanels)
      {
         this.launchPanels = new ArrayList<LaunchPanel>(defaultLaunchpad.getElements());

         for (PerspectiveExtension extension : extensions.values())
         {
            mergeExtensions(launchPanels, extension.getLaunchpadExtensions());
         }
      }

      return launchPanels;
   }

   public boolean addToolbarSection(ToolbarSection toolbarSection)
   {
      return this.defaultToolbar.addElement(toolbarSection);
   }

   public List<ToolbarSection> getToolbarSections()
   {
      if (null == toolbarSections)
      {
         this.toolbarSections = new ArrayList<ToolbarSection>(defaultToolbar
               .getElements());

         for (PerspectiveExtension extension : extensions.values())
         {
            mergeExtensions(toolbarSections, extension.getToolbarExtensions());
         }
      }

      return toolbarSections;
   }

   public boolean addView(ViewDefinition view)
   {
      return this.defaultViews.addElement(view);
   }

   public List<ViewDefinition> getViews()
   {
      if (null == views)
      {
         this.views = new ArrayList<ViewDefinition>(defaultViews.getElements());

         for (PerspectiveExtension extension : extensions.values())
         {
            mergeExtensions(views, extension.getViewsExtensions());
         }
      }

      return views;
   }

   /**
    * @param name
    * @return
    */
   public LaunchPanel getLaunchPanel(String name)
   {
      return (LaunchPanel)getUiElement(getLaunchPanels(), name);
   }
   
   /**
    * @param name
    * @return
    */
   public MenuSection getMenuSection(String name)
   {
      return (MenuSection)getUiElement(getMenuSections(), name);
   }
   
   /**
    * @param name
    * @return
    */
   public ToolbarSection getToolbarSection(String name)
   {
      return (ToolbarSection)getUiElement(getToolbarSections(), name);
   }

   /**
    * @param name
    * @return
    */
   public ViewDefinition getViewDefinition(String name)
   {
      return (ViewDefinition)getUiElement(getViews(), name);
   }

   /**
    * @param list
    * @param name
    * @return
    */
   private UiElement getUiElement(List<? extends UiElement> list, String name)
   {
      for (UiElement uiElement : list)
      {
         if(name.equals(uiElement.getName()))
            return uiElement;
      }

      return null;
   }
}
