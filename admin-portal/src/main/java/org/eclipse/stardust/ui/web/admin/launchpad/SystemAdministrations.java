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
package org.eclipse.stardust.ui.web.admin.launchpad;

import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;


/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class SystemAdministrations extends AbstractLaunchPanel implements ResourcePaths
{

   /**
    * @param name
    */
   public SystemAdministrations()
   {
      super(LP_systemAdministrations);
      setExpanded(true);
   }

   @Override
   public void update()
   {}

}
