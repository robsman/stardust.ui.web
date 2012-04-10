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
package org.eclipse.stardust.ui.web.viewscommon.docmgmt;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.ProcessAttachmentUserObject;

import com.icesoft.faces.component.inputfile.FileInfo;

/**
 * Helps to upload a file and create version if the file already exists into JCR.
 * 
 * @author Yogesh.Manware
 * 
 */
public class FileUploadHelper
{
   public static enum FileUploadEvent {
      DIALOG_OPENED, FILE_UPLOADED, VERSION_UPLOADED, UPLOAD_ABORTED
   }
   public static final String EVENT = "event";
   public static final String DOCUMENT = "document";

   public enum FUNCTION_TYPE {
      UPLOAD_ON_FOLDER, DROP_ON_FOLDER
   }

   private FUNCTION_TYPE functionType = FUNCTION_TYPE.UPLOAD_ON_FOLDER;
   private IParametricCallbackHandler callbackHandler;
   private String parentFolderPath;
   private String headerMsg;
   private Document existingDocument;
   private Document draggedDocument;

   public FileUploadHelper(FUNCTION_TYPE functionType, String parentFolderPath)
   {
      super();
      this.parentFolderPath = parentFolderPath;
      this.functionType = functionType; 
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#upload
    * ()
    */
   public void uploadDocument()
   {
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();
      try
      {
         CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
         fileUploadDialog.initialize();
         fileUploadDialog.setTitle(msgBean.getString("common.fileUpload"));
         fileUploadDialog.setHeaderMessage(headerMsg);
         fileUploadDialog.setOpenDocumentFlag(true);
         fileUploadDialog.setICallbackHandler(new ICallbackHandler()
         {
            public void handleEvent(EventType eventType)
            {
               if (eventType == EventType.APPLY)
               {
                  try
                  {
                     uploadFile();
                  }
                  catch (Exception e)
                  {
                  }
               }
            }
         });
         fileUploadDialog.openPopup();
         informInitiator(FileUploadEvent.DIALOG_OPENED, null);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * upload file
    */
   private void uploadFile()
   {
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();

      try
      {
         Folder parentFolder = DocumentMgmtUtility.createFolderIfNotExists(parentFolderPath);
         CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
         FileInfo fileInfo = fileUploadDialog.getFileInfo();
         String fileName = fileInfo.getFileName();

         if (DocumentMgmtUtility.verifyExistenceOfFolderAndShowMessage(parentFolder.getId(), "", null))
         {
            if (!DocumentMgmtUtility.validateFileName(fileName))
            {
               MessageDialog.addInfoMessage(msgBean.getString("views.common.invalidCharater"));
               return;
            }
            this.existingDocument = DocumentMgmtUtility.getDocument(parentFolder.getPath(), fileName);
            if (null != this.existingDocument)
            {
               if (isVersionPermissible(parentFolder, fileName))
               {
                  updateVersion(existingDocument, null);
               }
               else
               {
                  // display error message, this is only in case if the parent folder is process attachments
                  MessageDialog.addErrorMessage(MessagesViewsCommonBean.getInstance().getParamString(
                        "views.genericRepositoryView.specificDocument.reclassifyDocument.fileAlreadyExist", fileName));
                  informInitiator(FileUploadEvent.UPLOAD_ABORTED, null);
                  return;
               }
            }
            else
            {
               try
               {
                  Document document = DocumentMgmtUtility.createDocument(parentFolder.getId(), 
                        fileInfo, fileUploadDialog.getDescription(), fileUploadDialog.getComments(), fileUploadDialog.getDocumentType());

                  informInitiator(FileUploadEvent.FILE_UPLOADED, document);
                  
                  if (fileUploadDialog.getOpenDocument())
                  {
                     Map<String, Object> viewParam = null;
                     if (null != callbackHandler && callbackHandler instanceof IParametricCallbackHandler)
                     {
                        viewParam = ((IParametricCallbackHandler) callbackHandler).getParameters();
                     }
                     DocumentViewUtil.openJCRDocument(document.getId(), viewParam);
                  }
               }
               catch (Exception e)
               {
                  ExceptionHandler.handleException(e);
               }
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }


   /**
    * This method can be invoked directly while drag drop operation
    * 
    * @param existingDocument
    * @param draggedDocument
    */
   public void updateVersion(Document existingDocument, Document draggedDocument)
   {
      this.existingDocument = existingDocument;
      this.draggedDocument = draggedDocument;
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
      String fileDescription = "";
      String versionComments = "";
      DocumentType newDocType = null;
      
      if (FUNCTION_TYPE.UPLOAD_ON_FOLDER.equals(functionType))
      {
         // Get already entered Description and Comments
         fileDescription = fileUploadDialog.getDescription();
         versionComments = fileUploadDialog.getComments();
         newDocType = fileUploadDialog.getDocumentType();
      }
      else if (null != draggedDocument)
      {
         newDocType = draggedDocument.getDocumentType();
      }
      
      //fileUploadDialog.initialize();
      
      fileUploadDialog.setHeaderMessage(msgBean.getParamString(
            "views.genericRepositoryView.createDocumentVersionConfirmation", this.existingDocument.getName()));
      fileUploadDialog.setTitle(msgBean.getString("views.documentView.saveDocumentDialog.saveDocument"));
      fileUploadDialog.setViewFileUpload(false);
      fileUploadDialog.setViewDocumentType(false);
      fileUploadDialog.setViewDescription(true);
      fileUploadDialog.setViewComment(true);
      
      // fileUploadDialog.setDocumentType(existingDocument.getDocumentType());
      if (!StringUtils.areEqual(existingDocument.getDocumentType(), newDocType))
      {
         if (FUNCTION_TYPE.UPLOAD_ON_FOLDER.equals(functionType))
         {
            fileUploadDialog
                  .setMessage(msgBean.getString("views.genericRepositoryView.fileUpload.documentTypeChanged"));
            newDocType = existingDocument.getDocumentType();
         }
         else
         {
            fileUploadDialog.setMessage(msgBean
                  .getString("views.genericRepositoryView.fileDragged.documentTypeChanged"));
         }
      }

      fileUploadDialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType == EventType.APPLY)
            {
               try
               {
                  saveVersion();
               }
               catch (Exception e)
               {
               }
            }
         }
      });
      fileUploadDialog.openPopup();
      if (FUNCTION_TYPE.UPLOAD_ON_FOLDER.equals(functionType))
      {
         // Need to set it after openPopup() as it's clearing the fields in that method
         fileUploadDialog.setDescription(fileDescription);
         fileUploadDialog.setComments(versionComments);
      }
      if (null != newDocType)
      {
         fileUploadDialog.setDocumentType(newDocType);
      }
   }

   /**
    * If user tries to upload some document from right click - process attachments folder
    * returns true if document version can be created, if process attachment
    * folder contains a document which is actually not attached to the process, then do
    * not allow to create a version
    * 
    * @param parentFolder
    * @param fileName
    * @return
    */
   private boolean isVersionPermissible(Folder parentFolder, String fileName)
   {
      Map<String, Object> params = callbackHandler.getParameters();
      Object userObject = params.get("userObject");
      boolean allowVersion = true;
      if (null != userObject)
      {
         if (userObject instanceof ProcessAttachmentUserObject)
         {  
            allowVersion = false;
            ProcessAttachmentUserObject attachmentUserObject = (ProcessAttachmentUserObject) userObject;
            List<Document> attachmentsList = DMSHelper.fetchProcessAttachments(attachmentUserObject
                  .getProcessInstance());
            for (Document document : attachmentsList)
            {
               if (document.getPath().equals(parentFolder.getPath() + File.pathSeparator + fileName))
               {
                  allowVersion = true;
                  break;
               }
            }
         }
      }
      return allowVersion;
   }
 
   /**
    * @throws IOException
    * @throws DocumentManagementServiceException
    * 
    */
   private void saveVersion() throws DocumentManagementServiceException, IOException
   {
      if (DMSHelper.hasPrivilege(existingDocument.getId(), DmsPrivilege.MODIFY_PRIVILEGE))
      {
         CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();

         // if document type is different then reset properties
         if (!StringUtils.areEqual(existingDocument.getDocumentType(), fileUploadDialog.getDocumentType()))
         {
            existingDocument.setDocumentType(fileUploadDialog.getDocumentType());
            existingDocument.getProperties().clear();
         }
         
         // if document is uploaded
         if (FUNCTION_TYPE.UPLOAD_ON_FOLDER.equals(functionType))
         {
            FileInfo fileInfo = fileUploadDialog.getFileInfo();
            existingDocument = DocumentMgmtUtility.getDocumentManagementService().getDocument(
                  parentFolderPath + "/" + fileInfo.getFileName());
            existingDocument = DocumentMgmtUtility.updateDocument(existingDocument,
                  DocumentMgmtUtility.getFileSystemDocumentContent(fileInfo.getPhysicalPath()),
                  fileUploadDialog.getDescription(), fileUploadDialog.getComments());
         }// if document is drag-dropped
         else
         {
            existingDocument = DocumentMgmtUtility.updateDocument(existingDocument, DocumentMgmtUtility
                  .getDocumentManagementService().retrieveDocumentContent(draggedDocument.getId()), fileUploadDialog
                  .getDescription(), fileUploadDialog.getComments());
         }
         informInitiator(FileUploadEvent.VERSION_UPLOADED, existingDocument);
         
         if (fileUploadDialog.getOpenDocument())
         {
            Map<String, Object> viewParam = null;
            if (null != callbackHandler && callbackHandler instanceof IParametricCallbackHandler)
            {
               viewParam = ((IParametricCallbackHandler) callbackHandler).getParameters();
            }
            DocumentViewUtil.openJCRDocument(existingDocument, viewParam);
         }
      }

      else
      {
         MessageDialog.addErrorMessage(MessagesViewsCommonBean.getInstance().getString(
               "views.genericRepositoryView.permissionDenied"));
      }
   }

   /**
    * InformInitiator about the event
    * 
    * @param event
    */
   private void informInitiator(FileUploadEvent event, Document document)
   {
      if (null != callbackHandler)
      {
         Map<String, Object> parameters = callbackHandler.getParameters();
         if (null == parameters)
         {
            parameters = new HashMap<String, Object>();
         }
         parameters.put(EVENT, event);
         if (null != document)
         {
            parameters.put(DOCUMENT, document);
         }
         callbackHandler.setParameters(parameters);
         callbackHandler.handleEvent(EventType.APPLY);
      }
   }

   /**
    * @param parametricCallbackHandler
    *           the callbackHandler to set
    */
   public void setCallbackHandler(ParametricCallbackHandler parametricCallbackHandler)
   {
      this.callbackHandler = parametricCallbackHandler;
   }

   /**
    * @param headerMsg
    *           the headerMsg to set
    */
   public void setHeaderMsg(String headerMsg)
   {
      this.headerMsg = headerMsg;
   }
}
