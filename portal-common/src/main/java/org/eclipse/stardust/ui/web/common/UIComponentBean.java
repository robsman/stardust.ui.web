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

import java.io.Serializable;

import org.eclipse.stardust.ui.web.common.message.UiElementMessage;
import org.eclipse.stardust.ui.web.common.uielement.DefaultViewDefinition;


/**
 * @author subodh.godbole
 * 
 */
public abstract class UIComponentBean implements Serializable
{
   private static final long serialVersionUID = 1L;

   private DefaultViewDefinition viewDef;

   // ************* PROTECTED METHODS **********************

   /**
    * Provided for backward compatibility
    */
   public UIComponentBean()
   {}

   /**
    * @param viewName
    */
   public UIComponentBean(String viewName)
   {
      viewDef = new DefaultViewDefinition(viewName);
   }

   /**
    * Initializes UI Component
    */
   public abstract void initialize();

   /**
    * Adapter Method to get the
    * 
    * @return
    */
   public UiElementMessage getMessages()
   {
      if (viewDef == null)
         throw new IllegalAccessError(
               "View Definition is not initialized to read messages");

      return viewDef.getMessages();
   }
}
