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
package org.eclipse.stardust.ui.web.common.spi.navigation;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * The navigation tree is used to define the application's main navigation structure. The
 * first level of the tree defines Application States To define Utility Navigation items,
 * see ...
 * 
 * @author Pierre Asselin
 */
public interface NavigationProvider extends Serializable
{

   /**
    * Returns the full navigation tree. Only items that the current user is entitled to
    * see should be returned.
    * 
    * @return returns the first level items with all children.
    */
   List<NavigationItem> getNavigationItems();

   /**
    * Returns the children items of the provided parent item's ID.
    * 
    * @param parentItemID
    *           The ID of the parent item. If the parent id is null or not found, return
    *           an unmodifiable empty list
    * @param withChildren
    *           Determines if the returned items should have their children filled.
    * @return returns the children items of the parent node provided
    */
   List<NavigationItem> getNavigationItems(String parentItemID, boolean withChildren);

   /**
    * Returns the <code>ApplicationView</code>s available under the specified node. It is
    * up to the implementer to determine what views Administrative method on the
    * implementor's side (IAF) instead?
    * 
    * @param nodeID
    *           The ID of the node to query. If null, all views should be returned
    *           regardless of whether the user is allowed to see that view.
    * @return A set of application views. Returns an empty unmodifiable set if none is
    *         available.
    */
   Set<ApplicationView> getViewsInNode(String nodeID);

}
