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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

/**
 * @author Yogesh.Manware
 * 
 */
public abstract class CallbackHandler implements ICallbackHandler
{
   private Object payload;

   /**
    * @param payload
    */
   public CallbackHandler(Object payload)
   {
      super();
      this.payload = payload;
   }

   /**
    * @return the parameter
    */
   public Object getPayload()
   {
      return payload;
   }
}