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

import org.eclipse.stardust.engine.api.runtime.Document;


/**
 * @author subodh.godbole
 *
 */
public class DocumentEvent
{
   public static enum EventType
   {
      CREATED,
      EDITED,
      DELETED
   }

   public static enum EventMode
   {
      PROCESS_ATTACHMENTS,
      PROCESS_DOCUMENTS,
      OTHER
   }

   private long processInstanceOid;
   private Document currentDocument;
   private List<Document> allProcessAttachments;
   private EventType eventType;
   private EventMode eventMode;
   
   /**
    * @param eventType
    * @param eventMode
    * @param processInstanceOid
    * @param currentDocument
    * @param allProcessAttachments
    */
   public DocumentEvent(EventType eventType, EventMode eventMode, long processInstanceOid, Document currentDocument,
         List<Document> allProcessAttachments)
   {
      this.eventType = eventType;
      this.processInstanceOid = processInstanceOid;
      this.currentDocument = currentDocument;
      this.allProcessAttachments = allProcessAttachments;
      this.eventMode = EventMode.PROCESS_ATTACHMENTS;
   }

   public DocumentEvent(EventType eventType, long processOid)
   {
      this.eventType = eventType;
      this.processInstanceOid = processOid;
      this.eventMode = EventMode.PROCESS_DOCUMENTS;
   }   
   
   /**
    * @param eventType
    * @param eventMode
    * @param currentDocument
    */
   public DocumentEvent(EventType eventType, Document currentDocument)
   {
      this.eventType = eventType;
      this.eventMode = EventMode.OTHER;
      this.currentDocument = currentDocument;
   }

   public long getProcessInstanceOid()
   {
      return processInstanceOid;
   }

   public Document getCurrentDocument()
   {
      return currentDocument;
   }

   public List<Document> getAllProcessAttachments()
   {
      return allProcessAttachments;
   }

   public EventType getEventType()
   {
      return eventType;
   }

   public EventMode getEventMode()
   {
      return eventMode;
   }
}
