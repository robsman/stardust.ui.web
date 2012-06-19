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
public class DefaultViewDefinition extends AbstractUiElement
{
   /**
    * @param name
    */
   public DefaultViewDefinition(String name)
   {
      super(name);
   }

   @Override
   public UiElementMessage createMessages()
   {
      return new UiElementMessage(UiElementType.VIEW_DEFINITION, name);
   }
}
