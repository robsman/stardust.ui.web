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

package org.eclipse.stardust.ui.web.processportal.interaction;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileStorage;
import org.eclipse.stardust.ui.web.viewscommon.views.document.AbstractDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemJCRDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DocumentHandlerBean.InputParameters;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
public class IppDocumentController implements DocumentController
{
   private static final long serialVersionUID = -4288981777329121017L;

   private AbstractDocumentContentInfo document;

   private InputParameters documentViewInputParameters = null;

   private View view;

   public IppDocumentController(Document document, DataMapping dm, Interaction interaction)
   {
      JCRDocument jcrDocument = new JCRDocument(document,
            Direction.IN == dm.getDirection());

      // initialize mimetype
      jcrDocument.getMimeType();

      this.document = jcrDocument;

      documentViewInputParameters = new InputParameters();
      documentViewInputParameters.setDocumentContentInfo(jcrDocument);
      documentViewInputParameters.setDataId(dm.getDataId());
      documentViewInputParameters.setDataPathId(dm.getDataPath());
      documentViewInputParameters.setDisableAutoDownload(false);
      documentViewInputParameters.setProcessInstancOid(interaction.getActivityInstance()
            .getProcessInstance()
            .getOID());

      // store it so that it can be opened later
      FileStorage fileStorage = FileStorage.getInstance();
      fileStorage.pushFile(jcrDocument.getId(), documentViewInputParameters);
   }

   public AbstractDocumentContentInfo getDocument()
   {
      return this.document;
   }

   public InputParameters getDocumentViewerInputParameters()
   {
      return documentViewInputParameters;
   }

   public View getView()
   {
      return this.view;
   }

   public View openDocument(boolean nested)
   {
      Map<String, Object> params = CollectionUtils.newMap();
      params.put("fileSystemJCRDocumentId", this.document.getId());

      View view = PortalApplication.getInstance().openViewById("documentView",
            getViewKey(), params, null, nested);

      return view;
   }

   public String getViewKey()
   {
      StringBuffer viewKey = new StringBuffer();

      if (document instanceof FileSystemJCRDocument)
      {
         viewKey.append("documentOID=").append(document.getId().hashCode());
      }
      else
      {
         viewKey.append("documentOID=").append(document.getId());
      }

      return viewKey.toString();
   }
}
