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

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class ToolbarButton implements Serializable
{
   private static final long serialVersionUID = 1L;

   private final String handler;

   private final String icon;

   private final String disabledIcon;

   public ToolbarButton(String handler, String icon, String disabledIcon)
   {
      this.handler = handler;
      this.icon = icon;
      this.disabledIcon = disabledIcon;
   }

   public String getHandler()
   {
      return handler;
   }

   public String getIcon()
   {
      return icon;
   }

   public String getDisabledIcon()
   {
      return disabledIcon;
   }
}
