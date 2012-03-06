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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Yogesh.Manware
 * 
 */
public class BpmPortalErrorMessages
{
   private static final String BUNDLE_NAME = "bpm-portal-errors"; //$NON-NLS-1$

   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

   private BpmPortalErrorMessages()
   {}

   public static String getString(String key)
   {
      try
      {
         return RESOURCE_BUNDLE.getString(key);
      }
      catch (MissingResourceException e)
      {
         return '!' + key + '!';
      }
   }
}
