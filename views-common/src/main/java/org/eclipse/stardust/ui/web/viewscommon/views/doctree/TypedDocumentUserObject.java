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
package org.eclipse.stardust.ui.web.viewscommon.views.doctree;

import java.io.IOException;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.viewscommon.common.DocumentToolTip;
import org.eclipse.stardust.ui.web.viewscommon.common.ToolTip;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.TypedDocumentUploadHelper;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.AbstractDocumentUploadHelper.DocumentUploadCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DefualtResourceDataProvider;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class TypedDocumentUserObject extends RepositoryResourceUserObject
{
   private static final long serialVersionUID = 1L;
   private MIMEType mType = MimeTypesHelper.DEFAULT;
   private boolean sendFileAllowed = false;
   private TypedDocument typedDocument;
   private String label;
   private boolean readable = false;
   private boolean deletable = false;
   private boolean readACL = false;
   private ToolTip documentToolTip;
   private boolean supportsToolTip = false;
   private Boolean detachable;

   /**
    * custom constructor initialing document user object
    *
    * @param defaultMutableTreeNode
    * @param document
    */
   public TypedDocumentUserObject(DefaultMutableTreeNode node, TypedDocument typedDocument)
   {
      super(node, typedDocument.getDocument());
      this.typedDocument = typedDocument;
      node.setAllowsChildren(false);
      setName(typedDocument.getName());
      this.setLeaf(true);
      label = getName();
      setEditable(false);
      initialize();
   }

   /**
    * Initialize
    */
   public void initialize()
   {
      Document document = typedDocument.getDocument();
      setResource(document);
      if (null != document)
      {
         sendFileAllowed = true;
         setEditable(true);
         setCanCreateFile(false);
         setCanUploadFile(false);
         readable = true;
         deletable = true;
         readACL = true;
         if (typedDocument.isOutMappingExist())
         {
            setDeletable(true);
         }
         else
         {
            setDeletable(false);
         }
         this.mType = MimeTypesHelper.detectMimeType(document.getName(), document.getContentType());
         setLeafIcon(this.mType.getCompleteIconPath());
         supportsToolTip = true;
         documentToolTip = new DocumentToolTip(typedDocument.getDocumentType(), document);
      }
      else
      {
         sendFileAllowed = false;
         setEditable(false);
         readable = false;
         deletable = false;
         readACL = false;
         setLeafIcon(ResourcePaths.I_EMPTY_CORE_DOCUMENT);
         if (typedDocument.isOutMappingExist())
         {
            setCanUploadFile(true);
         }
      }
   }

   @Override
   public void download()
   {
      DownloadPopupDialog downloadPopupDialog = DownloadPopupDialog.getCurrent();
      Document document = getDocument();
      OutputResource resource = new OutputResource(new DefualtResourceDataProvider(
            document.getName(), document.getId(), getMType().toString(), getDMS(), true),
            downloadPopupDialog);
      downloadPopupDialog.open(resource);
   }

   @Override
   public void openDocument()
   {
      if (null != getResource())
      {
         Map<String, Object> params = CollectionUtils.newMap();
         params.put("processInstance", typedDocument.getProcessInstance());
         params.put("dataPathId", typedDocument.getDataPath().getId());

         try
         {
            DocumentViewUtil.openDataMappingDocument(typedDocument.getProcessInstance(), typedDocument.getDataDetails()
                  .getId(), new JCRDocument(getDocument().getId()), params);
         }
         catch (ResourceNotFoundException e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      else
      {
         upload();
      }
   }

   @Override
   public void deleteResource()
   {
      typedDocument.setDocument(null);
      try
      {
         // delete resource from repository
         getDMS().removeDocument(getDocument().getId());

         TypedDocumentsUtil.updateTypedDocument(typedDocument, true);
         initialize();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   @Override
   public void detachResource() {
      typedDocument.setDocument(null);
      try
      {
         TypedDocumentsUtil.updateTypedDocument(typedDocument);
         initialize();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      
   };
   
   @Override
   public void renameStart()
   {
      RepositoryUtility.showErrorPopup("views.myDocumentsTreeView.renameDocMessageDialog.renamenotallowed", null, null);
   }

   @Override
   public void refresh()
   {}

   @Override
   public ResourceType getType()
   {
      return ResourceType.DOCUMENT;
   }

   @Override
   public void versionHistory()
   {
      DocumentVersionDialog documentVersionDialog = DocumentVersionDialog.getCurrent();
      documentVersionDialog.open(getDocument());
   }

   @Override
   public RepositoryResourceUserObject createSubfolder()
   {
      // This method should never get invoked
      return null;
   }

   /**
    * Upload new version
    */
   @Override
   public void upload()
   {
      TypedDocumentUploadHelper documentUploadHelper = new TypedDocumentUploadHelper();
      documentUploadHelper.setParentFolderPath(DocumentMgmtUtility.getTypedDocumentsFolderPath(typedDocument
            .getProcessInstance()));
      documentUploadHelper.setTypedDocument(typedDocument);
      DocumentUploadCallbackHandler callbackHandler = new DocumentUploadCallbackHandler()
      {
         public void handleEvent(DocumentUploadEventType eventType)
         {
            if (DocumentUploadEventType.DOCUMENT_CREATED == eventType || DocumentUploadEventType.VERSION_SAVED == eventType)
            {
               saveDocument(getDocument());
            }
         }
      };

      if (null == typedDocument.getDocument())
      {
         documentUploadHelper.initializeDocumentUploadDialog();
         documentUploadHelper.setCallbackHandler(callbackHandler);
         documentUploadHelper.uploadFile();
      }
      else
      {
         documentUploadHelper.initializeVersionUploadDialog(typedDocument.getDocument());
         documentUploadHelper.setCallbackHandler(callbackHandler);
         documentUploadHelper.uploadFile();
      }
   }

   /**
    * save document
    *
    * @throws DocumentManagementServiceException
    * @throws IOException
    */
   private void saveDocument(Document document)
   {
      typedDocument.setDocument(document);
      TypedDocumentsUtil.updateTypedDocument(typedDocument);
      initialize();
   }

   @Override
   public void drop(DefaultMutableTreeNode sourceNode)
   {
      ReclassifyDocumentBean reclassifyDocumentBean = ReclassifyDocumentBean.getInstance(true);
      reclassifyDocumentBean.initialize(sourceNode, this.wrapper);
   }

   @Override
   public RepositoryResourceUserObject createTextDocument(String fileType)
   {
      // This method should never get invoked
      return null;
   }

   @Override
   public void sendFile()
   {
      Map<String, Object> params = CollectionUtils.newTreeMap();
      if (null != typedDocument.getProcessInstance())
      {
         params.put("processInstanceOID", Long.toString(typedDocument.getProcessInstance().getOID()));
      }
      params.put("attachment", getDocument());
      PortalApplication.getInstance().openViewById("correspondenceView", "DocumentID=" + getDocument().getId(), params,
            null, true);
   }

   public Document getDocument()
   {
      return (Document) this.getResource();
   }

   @Override
   public String getLabel()
   {
      return label;
   }

   public MIMEType getMType()
   {
      return mType;
   }

   @Override
   public String getMTypeStr()
   {
      return mType.toString();
   }

   @Override
   public void uploadFolder()
   {
      // This method should never get invoked
   }

   @Override
   public boolean isDraggable()
   {
      return true;
   }

   @Override
   public boolean isSupportsToolTip()
   {
      return supportsToolTip;
   }

   @Override
   public boolean isDownloadable()
   {
      if (readable)
      {
         return super.isReadable();
      }
      else
      {
         return false;
      }
   }

   @Override
   public ToolTip getToolTip()
   {
      return documentToolTip;
   }

   @Override
   public boolean isRefreshable()
   {
      return false;
   }

   @Override
   public boolean isCanCreateNote()
   {
      return false;
   }

   @Override
   public boolean isSendFileAllowed()
   {
      return this.sendFileAllowed;
   }

   @Override
   public boolean isDetachable()
   {
      if (detachable == null)
      {
         detachable = (null != typedDocument.getProcessInstance())? true : false;
      }
      return detachable;
   }
   
   /*
    * (non-Javadoc)
    *
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#createNote()
    */
   public void createNote()
   {}

   @Override
   public boolean isLeafNode()
   {
      return true;
   }

   @Override
   public boolean isReadable()
   {
      if (readable)
      {
         return super.isReadable();
      }
      else
      {
         return false;
      }
   }

   @Override
   public boolean isDeletable()
   {
      if (deletable)
      {
         return super.isDeletable();
      }
      else
      {
         return false;
      }
   }

   @Override
   public boolean isReadACL()
   {
      if (readACL)
      {
         return super.isReadACL();
      }
      else
      {
         return false;
      }
   }

   /**
    * @return
    */
   private static DocumentManagementService getDMS()
   {
      return DocumentMgmtUtility.getDocumentManagementService();
   }

   @Override
   public void rename(String newName)
   {}

   public TypedDocument getTypedDocument()
   {
      return typedDocument;
   }
}