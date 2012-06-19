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
public class PerspectiveExtensionFactoryBean
      extends HierarchyFactoryBean<PerspectiveExtension, UiExtension<?>>
{

   public void applyElement(UiExtension<?> element) throws Exception
   {
      if (MenuExtension.class.isInstance(element))
      {
         this.parent.addMenuExtension(MenuExtension.class.cast(element));
      }
      else if (LaunchpadExtension.class.isInstance(element))
      {
         this.parent.addLaunchpadExtension(LaunchpadExtension.class.cast(element));
      }
      else if (ToolbarExtension.class.isInstance(element))
      {
         this.parent.addToolbarExtension(ToolbarExtension.class.cast(element));
      }
      else if (ViewsExtension.class.isInstance(element))
      {
         this.parent.addViewsExtension(ViewsExtension.class.cast(element));
      }
      else
      {
         // TODO trace
      }
   }

   public Class<PerspectiveExtension> getObjectType()
   {
      return PerspectiveExtension.class;
   }

}
