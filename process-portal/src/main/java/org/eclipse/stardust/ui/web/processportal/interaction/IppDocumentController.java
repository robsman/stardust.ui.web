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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileStorage;
import org.eclipse.stardust.ui.web.viewscommon.views.document.AbstractDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DocumentHandlerBean.InputParameters;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemJCRDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
public class IppDocumentController
{
   private static final long serialVersionUID = -4288981777329121017L;

   private AbstractDocumentContentInfo document;

   private InputParameters documentViewInputParameters = null;

   private View view;

   private List<AbstractDocumentContentInfo> docsTobeDeleted = new ArrayList<AbstractDocumentContentInfo>();

   /**
    * 
    * @param document
    * @param dm
    * @param interaction
    */
   public IppDocumentController(Document document, DataMapping dm, Interaction interaction)
   {
      this(dm, interaction);

      JCRDocument jcrDocument = new JCRDocument(document,
            Direction.IN == dm.getDirection());

      // initialize mimetype
      jcrDocument.getMimeType();
      this.document = jcrDocument;
      documentViewInputParameters.setDocumentContentInfo(jcrDocument);

      // store it so that it can be opened later
      FileStorage fileStorage = FileStorage.getInstance();
      fileStorage.pushFile(jcrDocument.getId(), documentViewInputParameters);
   }

   /**
    * when document is empty
    * 
    * @param dm
    * @param interaction
    */
   public IppDocumentController(DataMapping dm, Interaction interaction)
   {
      documentViewInputParameters = new InputParameters();
      documentViewInputParameters.setDataId(dm.getDataId());
      documentViewInputParameters.setDataPathId(dm.getDataPath());
      documentViewInputParameters.setDisableAutoDownload(false);
      documentViewInputParameters.setProcessInstancOid(interaction.getActivityInstance()
            .getProcessInstance()
            .getOID());
   }

   /**
    * 
    * @return
    */
   public AbstractDocumentContentInfo getDocument()
   {
      return this.document;
   }

   /**
    * 
    * @return
    */
   public InputParameters getDocumentViewerInputParameters()
   {
      return documentViewInputParameters;
   }

   /**
    * 
    * @return
    */
   public View getView()
   {
      return this.view;
   }

   /**
    * 
    * @param nested
    * @return
    */
   public View openDocument(boolean nested)
   {
      if (this.document != null)
      {
         Map<String, Object> params = CollectionUtils.newMap();
         params.put("fileSystemJCRDocumentId", this.document.getId());

         view = PortalApplication.getInstance().openViewById("documentView",
               getViewKey(), params, null, nested);
      }

      return view;
   }

   /**
    * 
    * @return
    */
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

   /**
    * 
    * @return
    */
   public boolean delete()
   {
      if (this.document != null)
      {
         if (isJCRDocument())
         {
            // to be deleted from MAIframeController
            docsTobeDeleted.add(this.document);
            this.document = null;
         }
         else
         {
            // delete file system document
            ((FileSystemDocument) this.document).delete();
         }
         return true;
      }
      return false;
   }

   /**
    * 
    * @return
    */
   public boolean isJCRDocument()
   {
      if (this.document != null && this.document instanceof JCRDocument)
      {
         return true;
      }
      return false;
   }

   /**
    * 
    * @param document
    */
   public void setDocument(AbstractDocumentContentInfo document)
   {
      this.document = document;
      this.documentViewInputParameters.setDocumentContentInfo(document);
   }

   /**
    * 
    * @return
    */
   public Document getJCRDocument()
   {
      if (isJCRDocument())
      {
         return ((JCRDocument) this.document).getDocument();
      }
      return null;
   }

   /**
    * 
    * @return
    */
   public List<AbstractDocumentContentInfo> getDocsTobeDeleted()
   {
      return docsTobeDeleted;
   }
}
