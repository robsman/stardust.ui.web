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
package org.eclipse.stardust.ui.web.processportal.view.manual;

import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;

/**
 * @author Subodh.Godbole
 *
 */
public interface DocumentInputEventHandler
{
   void handleEvent(DocumentInputEvent documentInputEvent);

   /**
    * @author Subodh.Godbole
    *
    */
   public class DocumentInputEvent
   {
      public static enum DocumentInputEventType
      {
         TO_BE_UPLOADED,
         UPLOADED,
         TO_BE_VIEWED,
         VIEWED,
         TO_BE_DELETED,
         DELETED
      }

      private final IppDocumentInputController documentInputController;
      private final DocumentInputEventType type;
      private final IDocumentContentInfo newDocument;
      private boolean vetoed;

      /**
       * @param dcumentContentInfo
       * @param type
       */
      public DocumentInputEvent(IppDocumentInputController documentInputController, DocumentInputEventType type,
            IDocumentContentInfo newDocument)
      {
         this.documentInputController = documentInputController;
         this.type = type;
         this.newDocument = newDocument;
      }

      public IppDocumentInputController getDocumentInputController()
      {
         return documentInputController;
      }

      public DocumentInputEventType getType()
      {
         return type;
      }

      public IDocumentContentInfo getNewDocument()
      {
         return newDocument;
      }

      public boolean isVetoed()
      {
         return vetoed;
      }

      public void setVetoed(boolean vetoed)
      {
         this.vetoed = vetoed;
      }
   }
}
