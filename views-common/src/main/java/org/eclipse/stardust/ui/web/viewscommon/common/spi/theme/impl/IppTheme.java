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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.theme.impl;

import org.eclipse.stardust.ui.web.common.spi.theme.Theme;

/**
 * @author Subodh.Godbole
 *
 */
public class IppTheme implements Theme
{
   private static final long serialVersionUID = 1L;

   private String themeId;
   private String themeName;
   
   public IppTheme(String themeId, String themeName)
   {
      super();
      this.themeId = themeId;
      this.themeName = themeName;
   }

   public String getThemeId()
   {
      return themeId;
   }

   public String getThemeName()
   {
      return themeName;
   }
}
