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
package org.eclipse.stardust.ui.web.common.uielement;

import org.eclipse.stardust.ui.web.common.UiElementType;
import org.eclipse.stardust.ui.web.common.message.UiElementMessage;


/**
 * @author Subodh.Godbole
 * 
 */
public abstract class AbstractLaunchPanel extends AbstractUiElement
{
   private boolean expanded;

   /**
    * 
    */
   public AbstractLaunchPanel(String name)
   {
      super(name);
   }

   @Override
   public UiElementMessage createMessages()
   {
      return new UiElementMessage(UiElementType.LAUNCH_PANEL, name);
   }

   public void expand()
   {
      setExpanded(true);
   }

   public void collapse()
   {
      setExpanded(false);
   }

   public void toggle()
   {
      setExpanded(!isExpanded());
   }

   public boolean isExpanded()
   {
      return expanded;
   }

   public void setExpanded(boolean expanded)
   {
      if (!disabled || !expanded)
      {
         this.expanded = expanded;
      }
   }

   @Override
   public void setDisabled(boolean disabled)
   {
      super.setDisabled(disabled);
      if (this.disabled)
      {
         setExpanded(false);
      }
   }
   
   public abstract void update();
}