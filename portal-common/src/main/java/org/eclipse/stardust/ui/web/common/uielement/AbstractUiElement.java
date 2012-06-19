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

import java.io.Serializable;

import org.eclipse.stardust.ui.web.common.message.UiElementMessage;


/**
 * @author Subodh.Godbole
 * 
 */
public abstract class AbstractUiElement implements Serializable
{
   private static final long serialVersionUID = 1L;

   protected String name;

   protected UiElementMessage messages;

   protected boolean disabled;

   /**
    * 
    */
   public AbstractUiElement(String name)
   {
      this.name = name;
   }

   public abstract UiElementMessage createMessages();
   
   /**
    * @return
    */
   public UiElementMessage getMessages()
   {
      if(messages == null)
         messages = createMessages();

      return messages;
   }

   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public boolean isDisabled()
   {
      return disabled;
   }

   public void setDisabled(boolean disabled)
   {
      this.disabled = disabled;
   }
}