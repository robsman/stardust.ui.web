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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.navigation.impl;

import java.util.List;
import java.util.Set;

import org.eclipse.stardust.ui.web.common.spi.navigation.ApplicationView;
import org.eclipse.stardust.ui.web.common.spi.navigation.NavigationItem;
import org.eclipse.stardust.ui.web.common.spi.navigation.NavigationProvider;


/**
 * @author Subodh.Godbole
 *
 */
public class DefaultNavigationProvider implements NavigationProvider
{
   private static final long serialVersionUID = 1L;

   public List<NavigationItem> getNavigationItems()
   {
      return null;
   }

   public List<NavigationItem> getNavigationItems(String parentItemID,
         boolean withChildren)
   {
      return null;
   }

   public Set<ApplicationView> getViewsInNode(String nodeID)
   {
      return null;
   }
}
