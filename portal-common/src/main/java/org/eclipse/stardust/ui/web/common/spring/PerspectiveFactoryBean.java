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
package org.eclipse.stardust.ui.web.common.spring;

import org.eclipse.stardust.ui.web.common.*;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class PerspectiveFactoryBean
      extends HierarchyFactoryBean<PerspectiveDefinition, UiElement>
{

   public void applyElement(UiElement element) throws Exception
   {
      if (element instanceof MenuSection)
      {
         this.parent.addMenuSection((MenuSection) element);
      }
      else if (element instanceof LaunchPanel)
      {
         this.parent.addLaunchPanel((LaunchPanel) element);
      }
      else if (element instanceof ToolbarSection)
      {
         this.parent.addToolbarSection((ToolbarSection) element);
      }
      else if (element instanceof ViewDefinition)
      {
         this.parent.addView((ViewDefinition) element);
      }
      else
      {
         // TODO trace
      }
   }

   public Class<PerspectiveDefinition> getObjectType()
   {
      return PerspectiveDefinition.class;
   }

}
