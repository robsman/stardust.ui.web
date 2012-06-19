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
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.RuntimeObject;


public class WorklistSelectionEvent extends AbstractEvent<WorklistSelectionObserver>
{
   public static final EventType SELECTED = EventType.SELECTED;
   public static final EventType INSTANCE_SELECTED = EventType.INSTANCE_SELECTED;

   private final EventType type;

   private final Participant participant;
   
   private final long activityInstanceOid;
   private final long processInstanceOid;
   
   public WorklistSelectionEvent(Participant participant)
   {
      this.type = EventType.SELECTED;
      this.participant = participant;
      this.activityInstanceOid = 0;
      this.processInstanceOid = 0;
   }
   
   public WorklistSelectionEvent(RuntimeObject runtimeObject)
   {
      this.type = INSTANCE_SELECTED;
      this.participant = null;
      if(runtimeObject instanceof ActivityInstance)
      {
         this.activityInstanceOid = runtimeObject.getOID();
         this.processInstanceOid = ((ActivityInstance)runtimeObject).getProcessInstanceOID();
      }
      else if(runtimeObject instanceof ProcessInstance)
      {
         this.activityInstanceOid = 0;
         this.processInstanceOid = runtimeObject.getOID();
      }
      else
      {
         this.activityInstanceOid = 0;
         this.processInstanceOid = 0;
      }
   }
   
   public EventType getType()
   {
      return type;
   }

   public Participant getParticipant()
   {
      return participant;
   }
   
   public long getActivityInstanceOid()
   {
      return activityInstanceOid;
   }
   
   public long getProcessInstanceOid()
   {
      return processInstanceOid;
   }
   
   public static final class/*enum*/ EventType extends StringKey
   {
      private static final long serialVersionUID = 1L;

      public static final EventType SELECTED = new EventType("SELECTED");
      public static final EventType INSTANCE_SELECTED = new EventType("INSTANCE_SELECTED");
      
      private EventType(String id)
      {
         super(id, id);
      }
   }

   @Override
   protected void notifyObserver(WorklistSelectionObserver observer)
   {
      observer.handleEvent(this);
   }

}
