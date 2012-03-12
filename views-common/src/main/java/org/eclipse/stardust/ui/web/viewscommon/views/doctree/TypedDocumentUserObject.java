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
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.viewscommon.common.NoteTip;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;


import com.icesoft.faces.component.inputfile.FileInfo;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class TypedDocumentUserObject extends RepositoryResourceUserObject
{
   private static final long serialVersionUID = 1L;
   private MessagesViewsCommonBean propsBean;
   private MIMEType mType = MimeTypesHelper.DEFAULT;
   private boolean sendFileAllowed = false;
   private TypedDocument typedDocument;
   private String label;
   private boolean readable = false;
   private boolean deletable = false;
   private boolean readACL = false;

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
      propsBean = MessagesViewsCommonBean.getInstance();
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
      OutputResource resource = new OutputResource(document.getName(), document.getId(), getMType().toString(),
            downloadPopupDialog, getDMS(), true);
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
            DocumentViewUtil.openDataMappingDocument(typedDocument.getProcessInstance(), typedDocument.getDataPath()
                  .getData(), new JCRDocument(getDocument().getId()), params);
         }
         catch (ResourceNotFoundException e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      else
      {
         MessageDialog.addErrorMessage(propsBean.getString("views.genericRepositoryView.documentNotUploaded"));
      }
   }

   @Override
   public void deleteResource()
   {
      typedDocument.setDocument(null);
      try
      {
         TypedDocumentsUtil.updateTypedDocument(typedDocument);
         // delete resource from repository
         getDMS().removeDocument(getDocument().getId());
         initialize();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

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
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
      fileUploadDialog.initialize();
      fileUploadDialog.setDocumentType(typedDocument.getDocumentType());
      fileUploadDialog.setOpenDocument(true);
      if (null == getDocument())
      {
         fileUploadDialog.setHeaderMessage(propsBean.getParamString(
               "views.genericRepositoryView.specificDocument.uploadFile", getLabel()));
         fileUploadDialog.setTitle(propsBean.getString("common.fileUpload"));
      }
      else
      {
         fileUploadDialog.setHeaderMessage(propsBean.getParamString(
               "views.genericRepositoryView.specificDocument.newVersion", getLabel()));
         fileUploadDialog.setTitle(propsBean.getString("views.documentView.saveDocumentDialog.uploadNewVersion.label"));
      }
      fileUploadDialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType == EventType.APPLY)
            {
               try
               {
                  saveDocument();
               }
               catch (Exception e)
               {
               }
            }
         }
      });
      fileUploadDialog.openPopup();
   }

   /**
    * save document
    * 
    * @throws DocumentManagementServiceException
    * @throws IOException
    */
   public void saveDocument() throws DocumentManagementServiceException, IOException
   {
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
      FileInfo fileInfo = fileUploadDialog.getFileInfo();
      String fileName = fileInfo.getFileName();

      if (!DocumentMgmtUtility.validateFileName(fileName))
      {
         MessageDialog.addInfoMessage(propsBean.getString("views.common.invalidCharater"));
         return;
      }

      String typedDocumentPath = DocumentMgmtUtility.getTypedDocumentsFolderPath(typedDocument.getProcessInstance());

      // CHECK if the file with same name already exist in Specific Documents folder
      Document existingDocument = DocumentMgmtUtility.getDocument(typedDocumentPath, fileName);
      if (null != existingDocument &&  null == getDocument())
      {
         // display error message
         MessageDialog.addErrorMessage(MessagesViewsCommonBean.getInstance().getParamString(
               "views.genericRepositoryView.specificDocument.reclassifyDocument.fileAlreadyExist", fileName));
         return;
      }

      Document document = null;
      if (null == getDocument()) // first version
      {
         Folder typedDocFolder = DocumentMgmtUtility.createFolderIfNotExists(typedDocumentPath);
         try
         {
            document = DocumentMgmtUtility.createDocument(typedDocFolder.getId(), fileInfo,
                  fileUploadDialog.getDescription(), fileUploadDialog.getComments(), fileUploadDialog.getDocumentType());
            typedDocument.setDocument(document);
            TypedDocumentsUtil.updateTypedDocument(typedDocument);
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      else
      {
         document = getDocument();
         document.setName(fileInfo.getFileName());
         document.setContentType(fileUploadDialog.getFileInfo().getContentType());
         document = DocumentMgmtUtility.updateDocument(getDocument(),
               DocumentMgmtUtility.getFileSystemDocumentContent(fileInfo.getPhysicalPath()),
               fileUploadDialog.getDescription(), fileUploadDialog.getComments());
         typedDocument.setDocument(document);
         TypedDocumentsUtil.updateTypedDocument(typedDocument);
      }
      initialize();
      
      if (null != document && fileUploadDialog.isOpenDocument())
      {
         DocumentViewUtil.openJCRDocument(document.getId());
      }
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
      return false;
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
   public NoteTip getNoteTip()
   {
      return null;
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