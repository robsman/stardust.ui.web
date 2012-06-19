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

import org.eclipse.stardust.ui.web.common.ToolbarButton;
import org.eclipse.stardust.ui.web.common.ToolbarSection;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class ToolbarSectionFactoryBean extends HierarchyFactoryBean<ToolbarSection, ToolbarButton>
{

   public void applyElement(ToolbarButton button) throws Exception
   {
      this.parent.addToolbarButton(button);
   }

   public Class<ToolbarSection> getObjectType()
   {
      return ToolbarSection.class;
   }

}
