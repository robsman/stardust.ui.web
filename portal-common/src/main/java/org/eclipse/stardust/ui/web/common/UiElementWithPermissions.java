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

import static org.eclipse.stardust.ui.web.common.util.StringUtils.splitUnique;

import java.util.Set;

/**
 * @author Subodh.Godbole
 *
 */
public abstract class UiElementWithPermissions extends UiElement
{
   private static final long serialVersionUID = 1L;

   private String requiredRoles;
   private Set<String> requiredRolesSet;

   private String excludeRoles;
   private Set<String> excludeRolesSet;


   /**
    * @param name
    * @param include
    * @param definedIn
    * @param global
    */
   public UiElementWithPermissions(String name, String include, String definedIn, boolean global)
   {
      super(name, include, definedIn, global);
   }

   /**
    * @return
    */
   public Set<String> getRequiredRolesSet()
   {
      if(requiredRolesSet == null)
      {
         requiredRolesSet = splitUnique(requiredRoles, ",");
      }

      return requiredRolesSet;
   }

   /**
    * @return
    */
   public Set<String> getExcludeRolesSet()
   {
      if(excludeRolesSet == null)
      {
         excludeRolesSet = splitUnique(excludeRoles, ",");
      }

      return excludeRolesSet;
   }

   public String getRequiredRoles()
   {
      return requiredRoles;
   }
   
   public void setRequiredRoles(String requiredRoles)
   {
      this.requiredRoles = requiredRoles;
   }

   public String getExcludeRoles()
   {
      return excludeRoles;
   }

   public void setExcludeRoles(String excludeRoles)
   {
      this.excludeRoles = excludeRoles;
   }
}
