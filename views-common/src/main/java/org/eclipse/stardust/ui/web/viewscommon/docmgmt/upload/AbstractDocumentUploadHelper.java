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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.AbstractDocumentUploadHelper.DocumentUploadCallbackHandler.DocumentUploadEventType;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadDialogAttributes;

/**
 * Assist in uploading a Document which is to be stored in JCR repository
 * 
 * @author Yogesh.Manware
 * 
 */
public abstract class AbstractDocumentUploadHelper implements Serializable
{
   private static final long serialVersionUID = 1L;
   protected String parentFolderPath;
   private DocumentUploadCallbackHandler callbackHandler;
   protected CommonFileUploadDialog fileUploadHelperDialog;
   protected MessagesViewsCommonBean msgBean;
   protected boolean openDocumentOverride = true;
   protected Map<String, Object> viewParam = new HashMap<String, Object>();
   RepositoryResourceUserObject repositoryResourceUserObject = null;

   public AbstractDocumentUploadHelper()
   {
      super();
      msgBean = MessagesViewsCommonBean.getInstance();
   }

   /**
    * @return
    */
   public FileUploadDialogAttributes getFileUploadDialogAttributes()
   {
      return fileUploadHelperDialog.getAttributes();
   }

   /**
    * initialized fresh document upload dialog
    */
   public void initializeDocumentUploadDialog()
   {
      fileUploadHelperDialog = CommonFileUploadDialog.getInstance();
      fileUploadHelperDialog.initializeBean();

      fileUploadHelperDialog.getAttributes().setOpenDocumentFlag(true);

      fileUploadHelperDialog.setCallbackHandler(new FileUploadCallbackHandler()
      {
         public void handleEvent(FileUploadEvent eventType)
         {
            if (eventType == FileUploadEvent.FILE_UPLOADED)
            {
               try
               {
                  processUploadedFile(getFileWrapper());
               }
               catch (Exception e)
               {
                  ExceptionHandler.handleException(e);
                  informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
               }
            }
            else
            {
               informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
            }
         }
      });
   }

   /**
    * Start Document Upload or Version Upload
    */
   public void uploadFile()
   {
      fileUploadHelperDialog.openPopup();
      informInitiator(DocumentUploadEventType.DIALOG_OPENED, null);
   }

   /**
    * @param existingDocument
    */
   public void initializeVersionUploadDialog(final Document existingDocument)
   {
      fileUploadHelperDialog = CommonFileUploadDialog.getInstance();
      fileUploadHelperDialog.initializeBean();

      getFileUploadDialogAttributes().setTitle(
            msgBean.getString("views.documentView.saveDocumentDialog.uploadNewVersion.label"));

      getFileUploadDialogAttributes().setDocumentType(existingDocument.getDocumentType());

      fileUploadHelperDialog.setCallbackHandler(new FileUploadCallbackHandler()
      {
         public void handleEvent(FileUploadEvent eventType)
         {
            if (eventType == FileUploadEvent.FILE_UPLOADED)
            {
               try
               {
                  FileWrapper fileWrapper = getFileWrapper();

                  if (!checkIfFileWithSameNameExist(existingDocument, fileWrapper.getFileInfo().getFileName()))
                  {
                     saveVersion(existingDocument, getFileWrapper());
                  }
                  else
                  {
                     displayFileAlreadyExistError(fileWrapper.getFileInfo().getFileName());
                     return;
                  }
               }
               catch (Exception e)
               {
                  ExceptionHandler.handleException(e);
                  informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
               }
            }
            else
            {
               informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
            }
         }
      });
   }

   /**
    * @param parentFolder
    * @param fileName
    * @return
    */
   protected boolean handleFileAlreadyExistInFolder(Folder parentFolder, String fileName)
   {
      return false;
   }

   /**
    * @param existingDocument
    */
   protected void initializeSaveVersionConfirmationDialog(final Document existingDocument)
   {
      fileUploadHelperDialog = CommonFileUploadDialog.getInstance();
      fileUploadHelperDialog.initializeBean();
      FileUploadDialogAttributes attributes = getFileUploadDialogAttributes();
      attributes.setHeaderMessage(msgBean.getParamString(
            "views.genericRepositoryView.createDocumentVersionConfirmation", existingDocument.getName()));
      attributes.setTitle(msgBean.getString("views.documentView.saveDocumentDialog.saveDocument"));
      attributes.setViewFileUpload(false);
      attributes.setShowOpenDocument(false);
      attributes.setViewDocumentType(false);
      attributes.setViewDescription(true);
      attributes.setViewComment(true);
   }

   /**
    * @param existingDocument
    */
   protected void openDocument(final Document existingDocument)
   {
      if (openDocumentOverride)
      {
         DocumentViewUtil.openJCRDocument(existingDocument.getId(), viewParam);
      }
   }

   /**
    * @param fileWrapper
    */
   private void processUploadedFile(final FileWrapper fileWrapper)
   {
      String fileName = fileWrapper.getFileInfo().getFileName();

      if (validateFileNameAndDisplayMsg(fileName))
      {
         Folder parentFolder = DocumentMgmtUtility.createFolderIfNotExists(parentFolderPath);

         Document existingDocument = DocumentMgmtUtility.getDocument(parentFolder.getPath(), fileName);
         boolean fileAlreadyExist = null != existingDocument;

         if (fileAlreadyExist)
         {
            if (handleFileAlreadyExistInFolder(parentFolder, fileName))
            {
               updateVersion(existingDocument, fileWrapper);
            }
            else
            {
               displayFileAlreadyExistError(fileName);
               return;
            }
         }
         else
         {
            createDocument(parentFolder, fileWrapper);
         }
      }
      else
      {
         informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
      }
   }

   /**
    * @param existingDocument
    * @param fileWrapper
    */
   private void updateVersion(final Document existingDocument, final FileWrapper fileWrapper)
   {
      initializeSaveVersionConfirmationDialog(existingDocument);

      fileUploadHelperDialog.getAttributes().setDescription(fileWrapper.getDescription());
      fileUploadHelperDialog.getAttributes().setComments(fileWrapper.getComments());
      fileUploadHelperDialog.getAttributes().setDocumentType(fileWrapper.getDocumentType());
      if (!StringUtils.areEqual(existingDocument.getDocumentType(), fileWrapper.getDocumentType()))
      {
         fileUploadHelperDialog.getAttributes().setMessage(
               msgBean.getString("views.genericRepositoryView.fileUpload.documentTypeChanged"));
      }

      fileUploadHelperDialog.setCallbackHandler(new FileUploadCallbackHandler()
      {
         public void handleEvent(FileUploadEvent eventType)
         {
            if (eventType == FileUploadEvent.SAVE_CONFIRMED)
            {
               try
               {
                  fileWrapper.setDescription(getFileWrapper().getDescription());
                  fileWrapper.setComments(getFileWrapper().getComments());
                  saveVersion(existingDocument, fileWrapper);

               }
               catch (Exception e)
               {
                  ExceptionHandler.handleException(e);
                  informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
               }
            }
            else
            {
               informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
            }
         }
      });

      fileUploadHelperDialog.openPopup();
   }

   /**
    * @param existingDocument
    * @return
    */
   protected boolean checkModifyPrivilege(Document existingDocument)
   {
      if (!DMSHelper.hasPrivilege(existingDocument.getId(), DmsPrivilege.MODIFY_PRIVILEGE))
      {
         MessageDialog.addErrorMessage(MessagesViewsCommonBean.getInstance().getString(
               "views.genericRepositoryView.permissionDenied"));
         return false;
      }
      else
      {
         return true;
      }
   }

   /**
    * @param existingDocument
    * @param fileWrapper
    * @throws DocumentManagementServiceException
    * @throws IOException
    */
   private void saveVersion(Document existingDocument, final FileWrapper fileWrapper)
         throws DocumentManagementServiceException, IOException
   {
      if (checkModifyPrivilege(existingDocument))
      {
         // create version if not required
         if (!DocumentMgmtUtility.isDocumentVersioned(existingDocument))
         {
            DocumentMgmtUtility.getDocumentManagementService().versionDocument(existingDocument.getId(),
                  CommonProperties.ZERO);
         }

         existingDocument.setName(fileWrapper.getFileInfo().getFileName());
         existingDocument.setOwner(DocumentMgmtUtility.getUser().getAccount());
         existingDocument.setContentType(fileWrapper.getFileInfo().getContentType());
         existingDocument.setDocumentAnnotations(null);

         if (!StringUtils.areEqual(existingDocument.getDocumentType(), fileWrapper.getDocumentType()))
         {
            existingDocument.getProperties().clear();
         }

         Document updatedDocument = DocumentMgmtUtility.updateDocument(existingDocument,
               DocumentMgmtUtility.getFileSystemDocumentContent(fileWrapper.getFileInfo().getPhysicalPath()),
               fileWrapper.getDescription(), fileWrapper.getComments());

         informInitiator(DocumentUploadEventType.VERSION_SAVED, updatedDocument);

         if (fileWrapper.isOpenDocument())
         {
            openDocument(updatedDocument);
         }
      }
      else
      {
         informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
      }
   }

   /**
    * @param parentFolder
    * @param fileWrapper
    */
   private void createDocument(Folder parentFolder, FileWrapper fileWrapper)
   {
      try
      {
         Document updatedDocument = DocumentMgmtUtility.createDocument(parentFolder.getId(), fileWrapper.getFileInfo(),
               fileWrapper.getDescription(), fileWrapper.getComments(), fileWrapper.getDocumentType());

         informInitiator(DocumentUploadEventType.DOCUMENT_CREATED, updatedDocument);
         openDocument(updatedDocument);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
         informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
      }
   }

   /**
    * @param fileName
    */
   private void displayFileAlreadyExistError(String fileName)
   {
      MessageDialog.addErrorMessage(MessagesViewsCommonBean.getInstance().getParamString(
            "views.genericRepositoryView.specificDocument.reclassifyDocument.fileAlreadyExist", fileName));
      informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
   }

   /**
    * @param existingDocument
    * @param fileName
    * @return
    */
   private boolean checkIfFileWithSameNameExist(final Document existingDocument, final String fileName)
   {
      if (!existingDocument.getName().equals(fileName))
      {
         String parentFolderPath = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringBeforeLast(
               existingDocument.getPath(), File.pathSeparator);
         Document document = DocumentMgmtUtility.getDocument(parentFolderPath, fileName);
         if (null != document)
         {
            return true;
         }
      }
      return false;
   }

   /**
    * @param fileName
    * @return
    */
   private boolean validateFileNameAndDisplayMsg(String fileName)
   {
      if (!DocumentMgmtUtility.validateFileName(fileName))
      {
         MessageDialog.addInfoMessage(msgBean.getString("views.common.invalidCharater.error"));
         return false;
      }
      return true;
   }

   /**
    * @param event
    * @param document
    */
   void informInitiator(DocumentUploadEventType event, Document document)
   {
      if (null != callbackHandler)
      {
         callbackHandler.setDocument(document);
         callbackHandler.handleEvent(event);
      }
   }

   public void setParentFolderPath(String path)
   {
      this.parentFolderPath = path;
   }

   public void setOpenDocumentOverride(boolean openDocumentOverride)
   {
      this.openDocumentOverride = openDocumentOverride;
   }

   public void setViewParam(String viewParamName, Object viewParam)
   {
      this.viewParam.put(viewParamName, viewParam);
   }

   public void setCallbackHandler(DocumentUploadCallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }
   
   /**
    * @param repositoryResourceUserObject
    */
   public void setRepositoryResourceUserObject(RepositoryResourceUserObject repositoryResourceUserObject)
   {
      this.repositoryResourceUserObject = repositoryResourceUserObject;
   }
   
   /**
    * @author Yogesh.Manware
    * 
    */
   public static abstract class DocumentUploadCallbackHandler
   {
      private Document document;

      public static enum DocumentUploadEventType {
         DIALOG_OPENED, DOCUMENT_CREATED, UPLOAD_FAILED, VERSION_SAVED
      }

      abstract public void handleEvent(DocumentUploadEventType eventType);

      public Document getDocument()
      {
         return document;
      }

      public void setDocument(Document document)
      {
         this.document = document;
      }
   }
}
