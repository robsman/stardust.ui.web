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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.FileWrapper;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DocumentTypeWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadCallbackHandler.FileUploadEvent;

import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;

/**
 * Assist in uploading a File
 * 
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class CommonFileUploadDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(CommonFileUploadDialog.class);
   private static final String BEAN_NAME = "commonFileUploadDialog";
   private static final String DEFAULT_DOCUMENT_TYPE = "default";
   private FileUploadCallbackHandler callbackHandler;
   private int fileUploadProgress;
   private FileUploadDialogAttributes attributes;

   /**
    * default constructor
    */
   public CommonFileUploadDialog()
   {
      super();
   }

   public static CommonFileUploadDialog getInstance()
   {
      return (CommonFileUploadDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   public void initializeBean()
   {
      attributes = new FileUploadDialogAttributes();
      attributes.title = MessagesViewsCommonBean.getInstance().getString("fileUpload.label");
      fileUploadProgress = 0;
      callbackHandler = null;
   }

   /**
    * gets invoked when icefaces completes the file upload to some temporary location
    * 
    * @param event
    */
   public void uploadFile(ActionEvent event)
   {
      InputFile inputFile = (InputFile) event.getSource();
      FileInfo fileInfo = inputFile.getFileInfo();
      try
      {
         if (fileInfo.isSaved())
         {
            fireCallback(FileUploadEvent.FILE_UPLOADED, fileInfo);
         }
         else
         {
            switch (fileInfo.getStatus())
            {
            case FileInfo.UNSPECIFIED_NAME:
               ExceptionHandler.handleException("commonFile" + getBeanId(), MessagesViewsCommonBean.getInstance()
                     .getString("views.genericRepositoryView.UNSPECIFIED_NAME"));
               break;
            default:
               ExceptionHandler.handleException("commonFile" + getBeanId(), MessagesViewsCommonBean.getInstance()
                     .getString("views.genericRepositoryView.fileUploadError"));
               break;
            }
            fireCallback(FileUploadEvent.UPLOAD_FAILED, null);
         }
      }
      catch (Exception exception)
      {
         ExceptionHandler.handleException(exception);
         fireCallback(FileUploadEvent.UPLOAD_FAILED, null);
      }

   }

   public void continueAction()
   {
      fireCallback(FileUploadEvent.SAVE_CONFIRMED, null);
   }

   /**
    * tracks the progress
    * 
    * @param event
    */
   public void measureProgress(EventObject event)
   {
      InputFile file = (InputFile) event.getSource();
      trace.info("Measure progress " + file.getFileInfo().getPercent());
      fileUploadProgress = file.getFileInfo().getPercent();
   }

   public void toggleDescription()
   {
      attributes.showDescription = !attributes.showDescription;
   }

   public void toggleComment()
   {
      attributes.showComment = !attributes.showComment;
   }

   /**
    * @return
    */

   /**
    * @param eventType
    */
   private void fireCallback(FileUploadEvent eventType, FileInfo fileInfo)
   {
      // if exception occurs on dialog, don't close the popup
      if (!(FileUploadEvent.UPLOAD_FAILED == eventType))
      {
         closePopup();
      }
      
      if (callbackHandler != null)
      {
         FileWrapper fileWrapper = new FileWrapper();
         if (FileUploadEvent.FILE_UPLOADED == eventType)
         {
            fileWrapper.setFileInfo(fileInfo);
            fileWrapper.setDocumentType(attributes.getDocumentType());
            fileWrapper.setOpenDocument(attributes.openDocument);
         }
         fileWrapper.setDescription(attributes.description);
         fileWrapper.setComments(attributes.comments);

         callbackHandler.setFileWrapper(fileWrapper);

         callbackHandler.handleEvent(eventType);
      }
   }

   public int getFileUploadProgress()
   {
      return fileUploadProgress;
   }

   public void setICallbackHandler(FileUploadCallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   @Override
   public String getTitle()
   {
      return attributes.title;
   }

   public static class FileUploadDialogAttributes
   {
      private String title = "";
      private String description = "";
      private String headerMessage = "";
      private String comments = "";
      private boolean viewFileUpload = true;
      // To enable/disable the description and comment section on the dialog
      private boolean viewDescription = true;
      private boolean viewComment = true;
      private boolean viewDocumentType = true;

      // To show/hide the description and comment section on the dialog (page section link
      // action)
      private boolean showDescription;
      private boolean showComment;

      private Map<String, DocumentType> allDocumentTypes;
      private List<SelectItem> allDocumentTypesList;

      // Used in case where document type is disabled
      private DocumentType documentType;
      private String documentTypeName;

      // Used in case where document type is not disabled as displayed as select box
      private String documentTypeId;

      private boolean documentTypeDisabled;

      private boolean enableOpenDocument = true;
      private Boolean openDocument = false;
      private boolean showOpenDocument = true;

      private String message = "";

      public void setOpenDocumentFlag(Boolean openDocument)
      {
         this.openDocument = openDocument;
      }

      public String getDescription()
      {
         return description;
      }

      public void setDescription(String description)
      {
         this.description = description;
      }

      public String getHeaderMessage()
      {
         return headerMessage;
      }

      public void setHeaderMessage(String headerMessage)
      {
         this.headerMessage = headerMessage;
      }

      public String getComments()
      {
         return comments;
      }

      public void setComments(String comments)
      {
         this.comments = comments;
      }

      public boolean isViewFileUpload()
      {
         return viewFileUpload;
      }

      public void setViewFileUpload(boolean viewFileUpload)
      {
         this.viewFileUpload = viewFileUpload;
      }

      public boolean isViewDescription()
      {
         return viewDescription;
      }

      public void setViewDescription(boolean viewDescription)
      {
         this.viewDescription = viewDescription;
      }

      public boolean isViewComment()
      {
         return viewComment;
      }

      public void setViewComment(boolean viewComment)
      {
         this.viewComment = viewComment;
      }

      public boolean isViewDocumentType()
      {
         return viewDocumentType;
      }

      public void setViewDocumentType(boolean viewDocumentType)
      {
         this.viewDocumentType = viewDocumentType;
      }

      public boolean isShowDescription()
      {
         return showDescription;
      }

      public void setShowDescription(boolean showDescription)
      {
         this.showDescription = showDescription;
      }

      public boolean isShowComment()
      {
         return showComment;
      }

      public void setShowComment(boolean showComment)
      {
         this.showComment = showComment;
      }

      /**
       * @return
       */
      public List<SelectItem> getAllDocumentTypesList()
      {
         if (null == allDocumentTypes)
         {
            String displayName;
            String key;

            allDocumentTypes = new HashMap<String, DocumentType>();
            allDocumentTypesList = new ArrayList<SelectItem>();

            Set<DocumentTypeWrapper> docTypes = ModelUtils.getAllActiveDeclaredDocumentTypes();
            for (DocumentTypeWrapper docTypeObj : docTypes)
            {
               key = getDocumentTypeMapKey(docTypeObj.getDocumentType());
               displayName = docTypeObj.getDocumentTypeI18nName();

               allDocumentTypes.put(key, docTypeObj.getDocumentType());
               allDocumentTypesList.add(new SelectItem(key, displayName));
            }

            Collections.sort(allDocumentTypesList, new Comparator<SelectItem>()
            {
               public int compare(SelectItem arg0, SelectItem arg1)
               {
                  return arg0.getLabel().compareTo(arg1.getLabel());
               }
            });

            allDocumentTypesList.add(0, new SelectItem(DEFAULT_DOCUMENT_TYPE, MessagesViewsCommonBean.getInstance()
                  .getString("fileUpload.documentType.default")));
         }

         return allDocumentTypesList;
      }

      /**
       * @param docType
       * @return
       */
      private String getDocumentTypeMapKey(DocumentType docType)
      {
         return docType.getSchemaLocation() + ":" + docType.getDocumentTypeId();
      }

      public Map<String, DocumentType> getAllDocumentTypes()
      {
         return allDocumentTypes;
      }

      public void setAllDocumentTypes(Map<String, DocumentType> allDocumentTypes)
      {
         this.allDocumentTypes = allDocumentTypes;
      }

      public void setDocumentTypeName(String documentTypeName)
      {
         this.documentTypeName = documentTypeName;
      }

      public String getDocumentTypeId()
      {
         return documentTypeId;
      }

      public void setDocumentTypeId(String documentTypeId)
      {
         if (!isDocumentTypeDisabled())
         {
            this.documentTypeId = documentTypeId;
         }
      }

      public boolean isDocumentTypeDisabled()
      {
         return documentTypeDisabled;
      }

      public void setDocumentTypeDisabled(boolean documentTypeDisabled)
      {
         this.documentTypeDisabled = documentTypeDisabled;
      }

      public boolean isEnableOpenDocument()
      {
         return enableOpenDocument;
      }

      public void setEnableOpenDocument(boolean enableOpenDocument)
      {
         this.enableOpenDocument = enableOpenDocument;
      }

      public Boolean getOpenDocument()
      {
         return openDocument;
      }

      public void setOpenDocument(Boolean openDocument)
      {
         if (enableOpenDocument)
         {
            this.openDocument = openDocument;
         }
      }

      public String getMessage()
      {
         return message;
      }

      public void setMessage(String message)
      {
         this.message = message;
      }

      /**
       * @return
       */
      public DocumentType getDocumentType()
      {
         if (!isDocumentTypeDisabled())
         {
            if (null != allDocumentTypes)
            {
               return allDocumentTypes.get(getDocumentTypeId());
            }
            return null;
         }
         else
         {
            return documentType;
         }
      }

      /**
       * @param documentType
       */
      public void setDocumentType(DocumentType documentType)
      {
         this.documentType = documentType;
         setDocumentTypeName(documentType);
         documentTypeDisabled = true;
      }

      /**
       * @return
       */
      public String getDocumentTypeName()
      {
         return documentTypeName;
      }

      /**
       * @param documentType
       */
      private void setDocumentTypeName(DocumentType documentType)
      {
         if (null != documentType)
         {
            Set<DocumentTypeWrapper> docTypes = ModelUtils.getAllDeclaredDocumentTypes();
            for (DocumentTypeWrapper docTypeObj : docTypes)
            {
               if (docTypeObj.getDocumentType().equals(documentType))
               {
                  documentTypeName = docTypeObj.getDocumentTypeI18nName();
                  break;
               }
            }
         }
         else
         {
            documentTypeName = MessagesViewsCommonBean.getInstance().getString("fileUpload.documentType.default");
         }
      }

      public void setTitle(String title)
      {
         this.title = title;
      }

      public boolean isShowOpenDocument()
      {
         return showOpenDocument;
      }

      public void setShowOpenDocument(boolean showOpenDocument)
      {
         this.showOpenDocument = showOpenDocument;
      }
   }

   public void setCallbackHandler(FileUploadCallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   public FileUploadDialogAttributes getAttributes()
   {
      return attributes;
   }

   public static abstract class FileUploadCallbackHandler
   {
      private FileWrapper fileWrapper;

      public static enum FileUploadEvent {
         FILE_UPLOADED, UPLOAD_FAILED, SAVE_CONFIRMED
      }

      abstract public void handleEvent(FileUploadEvent eventType);

      public FileWrapper getFileWrapper()
      {
         return fileWrapper;
      }

      public void setFileWrapper(FileWrapper fileWrapper)
      {
         this.fileWrapper = fileWrapper;
      }
   }

   @Override
   public void initialize()
   {}
}
