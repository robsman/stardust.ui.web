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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

public interface IDocumentEventListener
{
   enum DocumentEventType
   {
      DETAILS_PANEL_COLLAPSED,
      DETAILS_PANEL_EXPANDED,
      TO_BE_POPPED_OUT,
      POPPED_OUT,
      TO_BE_POPPED_IN,
      POPPED_IN,
      SHOW_PREVIOUS_VERSION_TO_BE_INVOKED,
      SHOW_PREVIOUS_VERSION_INVOKED,
      SHOW_NEXT_VERSION_TO_BE_INVOKED,
      SHOW_NEXT_VERSION_INVOKED,
      REFRESH_VIWER_TO_BE_INVOKED,
      REFRESH_VIWER_INVOKED
   }
   
   void handleEvent(DocumentEventType documentEventType);
}
