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
package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class DefaultNames
{
   private static final String BUNDLE_NAME = "bpm-modeler-messages"; //$NON-NLS-1$

   private DefaultNames()
   {}

   public static String getString(String key)
   {
      try
      {
         return ResourceBundle.getBundle(BUNDLE_NAME).getString(key);
      }
      catch (MissingResourceException e)
      {
         return '!' + key + '!';
      }
   }
}
