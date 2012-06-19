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
package org.eclipse.stardust.ui.client.event;

import org.eclipse.stardust.ui.client.event.AbstractEvent;

public class StatusEvent extends AbstractEvent<StatusEventObserver>
{
   private StatusEventType type;
   private Object source;

   private StatusEvent(StatusEventType type, Object source)
   {
      this.type = type;
      this.source = source;
   }

   public StatusEventType getType()
   {
      return type;
   }

   public Object getSource()
   {
      return source;
   }

   @Override
   protected void notifyObserver(StatusEventObserver observer)
   {
      observer.handleEvent(this);
   }
   
   public static StatusEvent closed(Object source)
   {
      return new StatusEvent(StatusEventType.closed, source);
   }
   
   public static StatusEvent updated(Object source)
   {
      return new StatusEvent(StatusEventType.updated, source);
   }
}
