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
package org.eclipse.stardust.ui.event;

import org.eclipse.stardust.common.StringKey;

/**
 * @author gille
 * @version $Revision: 26585 $
 */
public class RefreshEvent extends AbstractEvent<RefreshEventObserver>
{
   public static final EventType FULL = EventType.FULL;

   public static final EventType WORKLIST_OUTLINE = EventType.WORKLIST_OUTLINE;

   public static final EventType WORKAREA = EventType.WORKAREA;

   private final EventType type;

   public RefreshEvent(EventType eventType)
   {
      this.type = eventType;
   }

   public EventType getType()
   {
      return type;
   }

   public static final class/*enum*/ EventType extends StringKey
   {
      private static final long serialVersionUID = 1L;

      public static final EventType FULL = new EventType("FULL");
      public static final EventType WORKLIST_OUTLINE = new EventType("WORKLIST_OUTLINE");
      public static final EventType WORKAREA = new EventType("WORKAREA");
      
      private EventType(String id)
      {
         super(id, id);
      }
   }

   @Override
   protected void notifyObserver(RefreshEventObserver observer)
   {
      observer.handleEvent(this);
   }

}
