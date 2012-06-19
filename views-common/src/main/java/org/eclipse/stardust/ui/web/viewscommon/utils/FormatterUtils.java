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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import org.eclipse.stardust.engine.api.runtime.User;


/**
 * Utility class for common formatter.
 * TODO - This class can be deleted as all it currently does is formats user name
 * All instances of FormatterUtils#getUserLabel(user) can be replaced with UserUtils.getUserDisplayLabel(user).
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class FormatterUtils
{
    private FormatterUtils()
    {
    }

   public static String getUserLabel(User user)
   {
      return UserUtils.getUserDisplayLabel(user);
   }
}
