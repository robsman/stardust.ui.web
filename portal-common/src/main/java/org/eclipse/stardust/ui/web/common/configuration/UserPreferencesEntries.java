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
 * This class holds Module Ids and View Ids 
 * Also the Features Ids. Convention: Module -> M_, View -> V_ , Feature -> F_
 * @author subodh.godbole
 */
public interface UserPreferencesEntries
{
   public static final String M_PORTAL = "ipp-portal-common";
   
   public static final String V_PORTAL_CONFIG = "configuration";
   
   public static final String F_SKIN = "prefs.skin";
   public static final String F_DEFAULT_PERSPECTIVE = "prefs.defaultPerspective";
   public static final String F_TABS_MAX_TABS_DISPLAY = "prefs.maxTabsDisplay";
   public static final String F_PAGINATOR_PAGE_SIZE = "prefs.pageSize";
   public static final String F_PAGINATOR_MAX_PAGES = "prefs.paginatorMaxPages";
   public static final String F_PAGINATOR_FAST_STEP = "prefs.paginatorFastStep";
}
