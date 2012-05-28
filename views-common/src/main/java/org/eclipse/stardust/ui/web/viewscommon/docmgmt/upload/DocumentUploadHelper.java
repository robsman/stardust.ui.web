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
package org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload;

import org.eclipse.stardust.engine.api.runtime.Document;

/**
 * @author Yogesh.Manware
 *
 */
public class DocumentUploadHelper extends AbstractDocumentUploadHelper
{
   private static final long serialVersionUID = 6884319935030733352L;
   

   @Override
   public void initializeVersionUploadDialog(Document existingDocument)
   {
      super.initializeVersionUploadDialog(existingDocument);
      getFileUploadDialogAttributes().setHeaderMessage(
            msgBean.getParamString("views.documentView.saveDocumentDialog.uploadNewVersion.text",
                  existingDocument.getName()));
   }
}
