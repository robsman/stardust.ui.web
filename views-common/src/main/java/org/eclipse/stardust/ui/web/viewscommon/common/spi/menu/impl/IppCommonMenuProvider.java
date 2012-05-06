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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.menu.impl;

import java.util.List;

import org.eclipse.stardust.ui.web.common.spi.menu.CommonMenuItem;
import org.eclipse.stardust.ui.web.common.spi.menu.CommonMenuProvider;

/**
 * @author Anoop.Nair
 *
 */
public class IppCommonMenuProvider implements CommonMenuProvider
{
   private static final long serialVersionUID = -3952633893989613865L;
   private static List<CommonMenuItem> commonMenuItems;

   public IppCommonMenuProvider()
   {
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.spi.menu.CommonMenuProvider#getMenuItems()
    */
   public List<CommonMenuItem> getMenuItems()
   {
      return commonMenuItems;
   }

   /**
    * @param menuItems
    */
   public void setMenuItems(List<CommonMenuItem> menuItems)
   {
      commonMenuItems = menuItems;
   }   
}
