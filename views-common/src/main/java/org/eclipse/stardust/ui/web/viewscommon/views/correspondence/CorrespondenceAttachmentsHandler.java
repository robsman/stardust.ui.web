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
package org.eclipse.stardust.ui.web.viewscommon.views.correspondence;

import org.eclipse.stardust.engine.api.runtime.Document;

/**
 * @author subodh.godbole
 *
 */
public interface CorrespondenceAttachmentsHandler
{
   public enum AddPolicy
   {
      AT_TOP,
      AT_BOTTOM
   }

   void popupOpened();
   void popupClosed();
   
   boolean addAttachment(Document document);
   boolean addTemplate(Document document, AddPolicy addPolicy);
   boolean isDocumentTemplate(Document document);
}
