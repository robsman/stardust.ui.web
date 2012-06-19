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
package org.eclipse.stardust.ui.web.viewscommon.common.event;

import java.util.List;

import org.eclipse.stardust.engine.api.dto.Note;


/**
 * @author subodh.godbole
 *
 */
public class NoteEvent
{
   public static enum EventType
   {
      ADDED
   }

   private long scopeProcessInstanceOid;
   private Note currentNote;
   private List<Note> allNotes;
   private EventType eventType;

   /**
    * @param scopeProcessInstanceOid
    * @param currentNote
    * @param allNotes
    */
   public NoteEvent(long scopeProcessInstanceOid, Note currentNote, List<Note> allNotes)
   {
      super();
      this.scopeProcessInstanceOid = scopeProcessInstanceOid;
      this.currentNote = currentNote;
      this.allNotes = allNotes;
      eventType = EventType.ADDED;
   }

   public long getScopeProcessInstanceOid()
   {
      return scopeProcessInstanceOid;
   }

   public Note getCurrentNote()
   {
      return currentNote;
   }

   public List<Note> getAllNotes()
   {
      return allNotes;
   }

   public EventType getEventType()
   {
      return eventType;
   }
}
