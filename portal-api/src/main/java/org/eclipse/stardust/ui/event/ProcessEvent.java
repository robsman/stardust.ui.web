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
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;


/**
 * @author gille
 * @version $Revision: 26585 $
 */
public class ProcessEvent extends AbstractEvent<ProcessEventObserver>
{
   public static final EventType STARTED = EventType.STARTED;

   public static final EventType COMPLETED = EventType.COMPLETED;

   private final EventType type;

   private final ProcessInstance processInstance;

   public ProcessEvent(EventType eventType, ProcessInstance processInstance)
   {
      this.type = eventType;
      this.processInstance = processInstance;
   }

   public EventType getType()
   {
      return type;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public static final class/*enum*/ EventType extends StringKey
   {
      private static final long serialVersionUID = 1L;

      public static final EventType STARTED = new EventType("STARTED");
      public static final EventType COMPLETED = new EventType("COMPLETED");
      
      private EventType(String id)
      {
         super(id, id);
      }
   }

   @Override
   protected void notifyObserver(ProcessEventObserver observer)
   {
      observer.handleEvent(this);
   }
}
