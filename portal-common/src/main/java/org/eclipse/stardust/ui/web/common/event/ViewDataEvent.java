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
package org.eclipse.stardust.ui.web.common.event;

import org.eclipse.stardust.ui.web.common.app.View;

/**
 * @author Subodh.Godbole
 *
 */
public class ViewDataEvent
{
   public static enum ViewDataEventType
   {
      DATA_MODIFIED
   }

   private final View view;
   private final ViewDataEventType type;
   private final Object payload;

   /**
    * @param view
    * @param type
    * @param payload
    */
   public ViewDataEvent(View view, ViewDataEventType type, Object payload)
   {
      super();
      this.view = view;
      this.type = type;
      this.payload = payload;
   }

   public View getView()
   {
      return view;
   }

   public ViewDataEventType getType()
   {
      return type;
   }

   public Object getPayload()
   {
      return payload;
   }
}
