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
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;


/**
 * @author gille
 * @version $Revision: 26585 $
 */
public class ActivityEvent extends AbstractEvent<ActivityEventObserver>
{
   public static final EventType STARTED = EventType.STARTED;

   public static final EventType ACTIVATED = EventType.ACTIVATED;

   public static final EventType SUSPENDED = EventType.SUSPENDED;

   public static final EventType COMPLETED = EventType.COMPLETED;

   public static final EventType ABORTED = EventType.ABORTED;

   private final EventType type;

   private final ActivityInstance activityInstance;

   private boolean hasFollowUpActivity;

   public ActivityEvent(EventType eventType, ActivityInstance sourceActivityInstance, boolean hasFollowUpActivity)
   {
      this.type = eventType;
      this.activityInstance = sourceActivityInstance;
      this.hasFollowUpActivity = hasFollowUpActivity;
   }

   public boolean hasFollowUpActivity()
   {
      return hasFollowUpActivity;
   }

   public EventType getType()
   {
      return type;
   }

   public ActivityInstance getActivityInstance()
   {
      return activityInstance;
   }

   public static final class/*enum*/ EventType extends StringKey
   {
      private static final long serialVersionUID = 1L;

      public static final EventType STARTED = new EventType("STARTED");
      public static final EventType ACTIVATED = new EventType("ACTIVATED");
      public static final EventType SUSPENDED = new EventType("SUSPENDED");
      public static final EventType COMPLETED = new EventType("COMPLETED");
      public static final EventType ABORTED = new EventType("ABORTED");
      
      private EventType(String id)
      {
         super(id, id);
      }
   }

   @Override
   protected void notifyObserver(ActivityEventObserver observer)
   {
      observer.handleEvent(this);
   }

   public static ActivityEvent completed(ActivityInstance instance, boolean hasFollowUpActivity)
   {
      return new ActivityEvent(ActivityEvent.COMPLETED, instance, hasFollowUpActivity);
   }

   public static ActivityEvent activated(ActivityInstance instance)
   {
      return new ActivityEvent(ActivityEvent.ACTIVATED, instance, false);
   }

   public static ActivityEvent suspended(ActivityInstance instance)
   {
      return new ActivityEvent(ActivityEvent.SUSPENDED, instance, false);
   }

   public static ActivityEvent aborted(ActivityInstance instance)
   {
      return new ActivityEvent(ActivityEvent.ABORTED, instance, false);
   }
}
