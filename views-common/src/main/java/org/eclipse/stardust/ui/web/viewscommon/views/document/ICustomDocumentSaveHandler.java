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

import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;

public interface ICustomDocumentSaveHandler
{
   static enum CustomDialogPosition
   {
      REPLACE,
      ADD_BEFORE,
      ADD_AFTER
   }
   
   IDocumentContentInfo save() throws ResourceNotFoundException;
   
   boolean isModified();
   
   boolean usesCustomSaveDialog();
   
   void setCustomSaveDialogOptions();
   
   void setDescriptionChanged(boolean changed);
   
   CustomDialogPosition getDialogPosition();

   String getCustomDialogURL();
}
