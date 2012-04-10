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
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DocumentTypeWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;

import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class CommonFileUploadDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "commonFileUploadDialog";
   private static final Logger trace = LogManager.getLogger(CommonFileUploadDialog.class);
   private static final String DEFAULT_DOCUMENT_TYPE = "default";
   
   private Folder targetFolder;
   private int fileUploadProgress;
   private ICallbackHandler iCallbackHandler;
   private FileInfo fileInfo;
   private String description;
   private String headerMessage;
   private String comments;
   private boolean viewFileUpload;
   // To enable/disable the description and comment section on the dialog
   private boolean viewDescription;
   private boolean viewComment;
   private boolean viewDocumentType;

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
   
   private boolean enableOpenDocument;
   private Boolean openDocument;
   
   private String message;

   /**
    * default constructor
    */
   public CommonFileUploadDialog()
   {
      super();
      setTitle(MessagesViewsCommonBean.getInstance().getString("fileUpload.label"));
      fileUploadProgress = 0;
   }

   /**
    * @return fileUploadAdminDialog object
    */
   public static CommonFileUploadDialog getCurrent()
   {
      return (CommonFileUploadDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   @Override
   public void initialize()
   {
      //FacesUtils.refreshPage();
      setTitle(MessagesViewsCommonBean.getInstance().getString("fileUpload.label"));
      fileUploadProgress = 0;
      description = "";
      comments = "";
      headerMessage = "";
      message = "";

      targetFolder = null;
      iCallbackHandler = null;
      fileInfo = null;

      viewFileUpload = true;
      viewDocumentType = true;
      viewDescription = true;
      viewComment = true;
      enableOpenDocument = true;
      
      showDescription = false;
      showComment = false;
      
      allDocumentTypes = null;
      allDocumentTypesList = null;
      documentTypeId = null;
      documentTypeDisabled = false;
      
      openDocument = false;
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
      this.fileInfo = fileInfo;
      try
      {
         if (fileInfo.isSaved())
         {
            fireCallback(EventType.APPLY);
         }
         else
         {
            switch (fileInfo.getStatus())
            {
            case FileInfo.UNSPECIFIED_NAME:
               ExceptionHandler.handleException("commonFile" + getBeanId(),
                     MessagesViewsCommonBean.getInstance().getString("views.genericRepositoryView.UNSPECIFIED_NAME"));
               break;
            default:
               ExceptionHandler.handleException("commonFile" + getBeanId(),
                     MessagesViewsCommonBean.getInstance().getString("views.genericRepositoryView.fileUploadError"));
               break;
            }

            // fireCallback(EventType.CANCEL);
         }
      }
      catch (Exception x)
      {
         ExceptionHandler.handleException(x);
         fireCallback(EventType.CANCEL);
      }

   }

   public void continueAction()
   {
      fireCallback(EventType.APPLY);
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
      showDescription = !showDescription;
   }

   public void toggleComment()
   {
      showComment = !showComment;
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

   /**
    * @param docType
    * @return
    */
   private String getDocumentTypeMapKey(DocumentType docType)
   {
      return docType.getSchemaLocation() + ":" + docType.getDocumentTypeId();
   }

   /**
    * @param eventType
    */
   private void fireCallback(EventType eventType)
   {
      closePopup();
      if (iCallbackHandler != null)
      {
         iCallbackHandler.handleEvent(eventType);
      }
   }

   public int getFileUploadProgress()
   {
      return fileUploadProgress;
   }

   public Folder getTargetFolder()
   {
      return targetFolder;
   }

   public FileInfo getFileInfo()
   {
      return this.fileInfo;
   }

   public ICallbackHandler getICallbackHandler()
   {
      return iCallbackHandler;
   }

   public void setICallbackHandler(ICallbackHandler callbackHandler)
   {
      iCallbackHandler = callbackHandler;
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

   public void setViewFileUpload(boolean fileUpload)
   {
      this.viewFileUpload = fileUpload;
   }

   public boolean isViewDescription()
   {
      return viewDescription;
   }

   public void setViewDescription(boolean viewDescription)
   {
      this.viewDescription = viewDescription;
   }

   public boolean isShowDescription()
   {
      return showDescription;
   }

   public boolean isShowComment()
   {
      return showComment;
   }

   public boolean isViewComment()
   {
      return viewComment;
   }

   public void setViewComment(boolean viewComment)
   {
      this.viewComment = viewComment;
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

   public Boolean getOpenDocument()
   {
      return openDocument;
   }

   /**
    * is invoked only from icefaces form
    * @param openDocument
    */
   public void setOpenDocument(Boolean openDocument)
   {
      this.openDocument = openDocument;
      if (enableOpenDocument)
      {
         this.openDocument = openDocument;
      }
   }
   
   public void setOpenDocumentFlag(Boolean openDocument)
   {
      this.openDocument = openDocument;
   }

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   public boolean isViewDocumentType()
   {
      return viewDocumentType;
   }

   public void setViewDocumentType(boolean viewDocumentType)
   {
      this.viewDocumentType = viewDocumentType;
   }

   public boolean isEnableOpenDocument()
   {
      return enableOpenDocument;
   }

   public void setEnableOpenDocument(boolean enableOpenDocument)
   {
      this.enableOpenDocument = enableOpenDocument;
   }
}
