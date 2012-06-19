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
 * @author gille
 * @version $Revision: 26585 $
 */
public class ViewEvent extends UiEventBase<ViewEventHandler>
{
   private final View view;
   
   private final ViewEventType type;
   
   private boolean vetoed;
   
   private boolean vetoable;

   public ViewEvent(View view, ViewEventType eventType, boolean vetoable)
   {
      this.view = view;
      this.type = eventType;
      this.vetoable = vetoable;
   }

   public ViewEvent(View view, ViewEventType eventType)
   {
      this(view, eventType, true); // Default is vetoable = true, for backward compatibility
   }

   public View getView()
   {
      return view;
   }

   public ViewEventType getType()
   {
      return type;
   }

   public boolean isVetoed()
   {
      return vetoed;
   }

   public void setVetoed(boolean vetoed)
   {
      this.vetoed = vetoed;
   }
   
   public boolean isVetoable()
   {
      return vetoable;
   }

   @Override
   public void notifyObserver(ViewEventHandler observer)
   {
      observer.handleEvent(this);
   }

   public static enum ViewEventType
   {
      CREATED,
      TO_BE_ACTIVATED,
      ACTIVATED,
      TO_BE_DEACTIVATED,
      DEACTIVATED,
      TO_BE_CLOSED,
      CLOSED,
      TO_BE_FULL_SCREENED,
      FULL_SCREENED,
      TO_BE_RESTORED_TO_NORMAL,
      RESTORED_TO_NORMAL,
      TO_BE_PINNED,
      PINNED,
      LAUNCH_PANELS_ACTIVATED,
      LAUNCH_PANELS_DEACTIVATED,
      POST_OPEN_LIFECYCLE,
      PERSPECTIVE_CHANGED,
      POPPED_IN
   }
}
