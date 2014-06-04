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
package org.eclipse.stardust.ui.web.common.configuration;

import java.io.Serializable;

import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceProvider;
import org.eclipse.stardust.ui.web.html5.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class UserPreferenceBean implements Serializable
{

   private static final long serialVersionUID = -2977908229268476133L;

   private static final String BEAN_ID = "ippUserPreferenceBean";
   
   private PreferenceProvider preferenceProvider;

   /**
    * @return
    */
   public static UserPreferenceBean getInstance()
   {
      return (UserPreferenceBean) ManagedBeanUtils.getManagedBean(BEAN_ID);
   }

   public PreferenceProvider getPreferenceProvider()
   {
      return preferenceProvider;
   }

   public void setPreferenceProvider(PreferenceProvider preferenceProvider)
   {
      this.preferenceProvider = preferenceProvider;
   }
}
