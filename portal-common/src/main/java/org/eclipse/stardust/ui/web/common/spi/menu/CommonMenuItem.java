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
package org.eclipse.stardust.ui.web.common.spi.menu;

import java.io.Serializable;

/**
 * @author Anoop.Nair
 *
 */
public interface CommonMenuItem extends Serializable
{
   /**
    * @return the Id for the menu item 
    */
   public String getId();

   /**
    * @return the title for the menu item
    */
   public String getTitle();
   
   /**
    * @return the URL for the menu item
    */
   public String getURL();
   
   /**
    * @return the icon for the menu item
    */
   public String getIconPath();
   
   /**
    * @return true if the menu item has changed since the last retrieval, false otherwise
    */
   public boolean isChanged();
}
