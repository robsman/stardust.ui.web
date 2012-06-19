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

import java.util.Set;

import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class ViewDefinition extends UiElement
{
   private static final long serialVersionUID = 1L;

   public static final String CLOSING_POLICY_NONE = "";
   public static final String CLOSING_POLICY_DIRECT = "direct";
   public static final String CLOSING_POLICY_RECURSIVE = "recursive";

   private PreferencesDefinition preferences;
   private String controller;
   private String closingPolicy;
   private boolean administrative;

   private String identityParams;
   private Set<String> identityParamsSet;

   public ViewDefinition(String name, String include)
   {
      super(name, include);
   }
   
   @Override
   protected String getMessageCodePrefix()
   {
      return "views.";
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

   public String getClosingPolicy()
   {
      return closingPolicy;
   }

   public void setClosingPolicy(String closingPolicy)
   {
      this.closingPolicy = closingPolicy;
   }

   public boolean isAdministrative()
   {
      return administrative;
   }

   public void setAdministrative(boolean administrative)
   {
      this.administrative = administrative;
   }

   public String getIdentityParams()
   {
      return identityParams;
   }

   public void setIdentityParams(String identityParams)
   {
      this.identityParams = identityParams;
   }

   public Set<String> getIdentityParamsSet()
   {
      if (null == identityParamsSet)
      {
         identityParamsSet = StringUtils.splitUnique(identityParams, ",");
      }

      return identityParamsSet;
   }
}
