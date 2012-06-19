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

/**
 * @author Subodh.Godbole
 *
 */
public interface NavigationItem extends Serializable
{
   /**
    * 
    * @return The ID of this item
    */
   String getID();

   /**
    * Note: even if this returns an empty list, it doesn't mean the item doesn't have
    * children. To make that determination, <code>@see isLeaf()</code>
    * 
    * @return
    */
   List<NavigationItem> getChildren();

   /**
    * 
    * @return true if the node is a leaf
    */
   boolean isLeaf();
   // Action getAction(); // openView, method invocation
}