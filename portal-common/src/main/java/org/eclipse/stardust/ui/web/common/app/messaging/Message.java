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
package org.eclipse.stardust.ui.web.common.app.messaging;

import com.google.gson.JsonObject;

/**
 * @author Subodh.Godbole
 *
 */
public class Message
{
   private String type;
   private JsonObject data;

   /**
    * @param messageType
    * @param data
    */
   public Message(String type, JsonObject data)
   {
      super();
      this.type = type;
      this.data = data;
   }

   public String getType()
   {
      return type;
   }

   public JsonObject getData()
   {
      return data;
   }
}
