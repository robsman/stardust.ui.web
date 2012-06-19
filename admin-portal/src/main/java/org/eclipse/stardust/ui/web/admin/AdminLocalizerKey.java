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
package org.eclipse.stardust.ui.web.admin;

import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;

public final class AdminLocalizerKey extends LocalizerKey
{
   private static final String ADMINPORTAL_MESSAGE_BUNDLE = "AdminPortalMessages";  

   public static final LocalizerKey KEY_CANNOT_CREATE_REALM = new AdminLocalizerKey("cannotCreateRealm");
   public static final LocalizerKey KEY_ACTIVITY_DETAIL = new AdminLocalizerKey("activityDetails");

   
   private AdminLocalizerKey(String key)
   {
      super(ADMINPORTAL_MESSAGE_BUNDLE, key);
   }
}
