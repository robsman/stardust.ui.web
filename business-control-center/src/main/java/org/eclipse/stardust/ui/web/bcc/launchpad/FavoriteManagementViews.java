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
package org.eclipse.stardust.ui.web.bcc.launchpad;

import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;


/**
 * @author Subodh.Godbole
 * 
 */
public class FavoriteManagementViews extends AbstractLaunchPanel implements ResourcePaths
{
   /**
    * 
    */
   public FavoriteManagementViews()
   {
      super(LP_favoriteManagementViews);
      setExpanded(true);
   }

   @Override
   public void update()
   {}
}
