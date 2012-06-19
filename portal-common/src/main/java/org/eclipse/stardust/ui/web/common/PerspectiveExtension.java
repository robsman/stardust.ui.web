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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class PerspectiveExtension implements Serializable, MessageSourceProvider
{
   private static final long serialVersionUID = 1L;

   private String name;

   private String targetPerspective;
   
   private Set<String> targetPerspectiveSet;

   private MessageSource messages;
   
   private String requiredRoles;
   
   private Set<String> requiredRolesSet;
   
   private String excludeRoles;

   private Set<String> excludeRolesSet;

   private List<MenuExtension> menuExtensions = new ArrayList<MenuExtension>();

   private List<LaunchpadExtension> launchpadExtensions = new ArrayList<LaunchpadExtension>();

   private List<ToolbarExtension> toolbarExtensions = new ArrayList<ToolbarExtension>();

   private List<ViewsExtension> viewsExtensions = new ArrayList<ViewsExtension>();

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getTargetPerspective()
   {
      return targetPerspective;
   }

   public void setTargetPerspective(String targetPerspective)
   {
      this.targetPerspective = targetPerspective;
   }

   public Set<String> getTargetPerspectiveSet()
   {
      if(null == targetPerspectiveSet)
      {
         targetPerspectiveSet = StringUtils.splitUnique(targetPerspective, ",");
      }

      return targetPerspectiveSet;
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

   public boolean addMenuExtension(MenuExtension extensions)
   {
      extensions.setMessagesProvider(this);
      return menuExtensions.add(extensions);
   }

   public List<MenuExtension> getMenuExtensions()
   {
      return menuExtensions;
   }

   public boolean addLaunchpadExtension(LaunchpadExtension extensions)
   {
      extensions.setMessagesProvider(this);
      return launchpadExtensions.add(extensions);
   }

   public List<LaunchpadExtension> getLaunchpadExtensions()
   {
      return launchpadExtensions;
   }

   public boolean addToolbarExtension(ToolbarExtension extensions)
   {
      extensions.setMessagesProvider(this);
      return toolbarExtensions.add(extensions);
   }

   public List<ToolbarExtension> getToolbarExtensions()
   {
      return toolbarExtensions;
   }

   public boolean addViewsExtension(ViewsExtension extensions)
   {
      extensions.setMessagesProvider(this);
      return viewsExtensions.add(extensions);
   }

   public List<ViewsExtension> getViewsExtensions()
   {
      return viewsExtensions;
   }

}
