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

/**
 * @author Subodh.Godbole
 *
 */
public interface ConfigurationConstants
{
   int DEFAULT_MAX_TAB_DISPLAY = 6;
   int DEFAULT_PAGE_SIZE = 8;
   int DEFAULT_MAX_PAGES = 4;
   int DEFAULT_FAST_STEP = 3;
   
   int LOWER_LIMIT_MAX_TAB_DISPLAY = 1;
   int LOWER_LIMIT_PAGE_SIZE = 1;
   int LOWER_LIMIT_MAX_PAGES = 1;
   int LOWER_LIMIT_FAST_STEP = 0;
   
   int UPPER_LIMIT_MAX_TAB_DISPLAY = 9;
   int UPPER_LIMIT_PAGE_SIZE = 50;
   int UPPER_LIMIT_MAX_PAGES = 25;
   int UPPER_LIMIT_FAST_STEP = 25;
}
