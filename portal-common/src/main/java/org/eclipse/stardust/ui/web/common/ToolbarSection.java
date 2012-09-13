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
package org.eclipse.stardust.ui.web.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class ToolbarSection extends UiElement
{
   private Set<String> requiredView;

   private List<ToolbarButton> buttons = new ArrayList<ToolbarButton>();

   public ToolbarSection(String name, String include, String definedIn, boolean global)
   {
      super(name, include, definedIn, global);
   }

   @Override
   protected String getMessageCodePrefix()
   {
      return "toolbars.";
   }  
   
   public Set<String> getRequiredView()
   {
      return requiredView;
   }

   public void setRequiredView(Set<String> requiredView)
   {
      this.requiredView = Collections.unmodifiableSet(requiredView);
   }

   public void addToolbarButton(ToolbarButton button)
   {
      buttons.add(button);
   }

   public List<ToolbarButton> getButtons()
   {
      return buttons;
   }

}
