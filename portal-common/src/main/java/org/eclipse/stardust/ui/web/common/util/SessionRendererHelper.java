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
package org.eclipse.stardust.ui.web.common.util;

import org.eclipse.stardust.ui.web.common.spi.user.User;

import com.icesoft.faces.async.render.SessionRenderer;

/**
 * Use this class instead of using com.icesoft.faces.async.render.SessionRenderer directly.
 * @author Yogesh.Manware
 * 
 */
public class SessionRendererHelper
{
   public static final String SESSION_RENDERER_PREFIX = "portal-";

   /**
    * @param id
    */
   public static void addCurrentSession(String id)
   {
      SessionRenderer.addCurrentSession(id);
   }

   /**
    * @param id
    */
   public static void render(String id)
   {
      SessionRenderer.render(id);
   }

   /**
    * @param id
    */
   public static void removeCurrentSession(String id)
   {
      SessionRenderer.removeCurrentSession(id);
   }

   /**
    * @param user
    * @return
    */
   public static String getPortalSessionRendererId(User user)
   {
      return SESSION_RENDERER_PREFIX + user.getUID();
   }
}
