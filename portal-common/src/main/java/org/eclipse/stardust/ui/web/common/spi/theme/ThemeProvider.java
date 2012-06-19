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
package org.eclipse.stardust.ui.web.common.spi.theme;

import java.io.Serializable;
import java.util.List;

/**
 * Provides the application with the theme to be applied for the current user.
 * 
 * @author Pierre Asselin
 */
public interface ThemeProvider extends Serializable
{
   /**
    * Returns all available Themes
    * @return
    */
   List<Theme> getThemes();

   /**
    * Loads the selected Theme to make it current theme
    * @param themeId
    */
   void loadTheme(String themeId);

   /**
    * 
    * @return Returns a list of paths to the stylesheets for the current theme or an
    *         unmodifiable empty list of there are none.
    */
   List<String> getStyleSheets();
}