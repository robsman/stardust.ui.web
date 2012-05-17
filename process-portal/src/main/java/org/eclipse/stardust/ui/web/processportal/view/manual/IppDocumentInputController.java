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
package org.eclipse.stardust.ui.web.processportal.view.manual;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.common.form.jsf.DocumentInputController;
import org.eclipse.stardust.ui.common.form.jsf.DocumentPath;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.app.View.ViewState;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.event.ViewDataEvent;
import org.eclipse.stardust.ui.web.common.event.ViewDataEventHandler;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.view.ActivityDetailsBean.WorkflowAction;
import org.eclipse.stardust.ui.web.processportal.view.manual.DocumentInputEventHandler.DocumentInputEvent;
import org.eclipse.stardust.ui.web.processportal.view.manual.DocumentInputEventHandler.DocumentInputEvent.DocumentInputEventType;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ConfirmationDialogWithOptionsBean;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.FileWrapper;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.IceComponentUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadDialogAttributes;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemJCRDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;



/**
 * @author Subodh.Godbole
 *
 */
public class IppDocumentInputController extends DocumentInputController implements ViewDataEventHandler
{
   private static final Logger trace = LogManager.getLogger(IppDocumentInputController.class);
   private static final String DELETE_DOCUMENT_OPTIONS_PREFIX = "views.activityPanel.deleteDocument.options.";
   
   private static enum RemoveDocumentOptions {
      MOVE_TO_PROCESS_ATTACHMENTS, DELETE_PERMANENTLY;
   }
   
   private View documentView; 
   private ActivityInstance activityInstance;
   private DataMapping dataMapping;
   private DocumentInputEventHandler handler;
   private boolean openDocument = true;
   private boolean enableOpenDocument = true;
   private Document documentToBeMoved;
   private Document documentToBeDeleted;

   /**
    * @param path
    * @param activityInstance
    * @param dataMapping
    */
   public IppDocumentInputController(DocumentPath path, ActivityInstance activityInstance, DataMapping dataMapping,
         DocumentInputEventHandler handler)
   {
      super(path);
      this.activityInstance = activityInstance;
      this.dataMapping = dataMapping;
      this.handler = handler;

      setDeleteIcon("/plugins/views-common/images/icons/page_white_delete.png");
      setDeleteLabel(MessagesViewsCommonBean.getInstance().getString("views.genericRepositoryView.treeMenuItem.delete"));
   }

   @Override
   public void setValue(Object object)
   {
      super.setValue(object);

      // It's observed that sometimes document is not received as null
      // So check with obvious attributes (id or name), if any of them are empty then consider document as null 
      // Scenario is - 
      // - Open Activity where document is already assigned to Data Mapping.
      // - Delete Document And save the Activity
      // - Now open Activity again - It returns non null document, but actually document should be null 
      if (null != document && (StringUtils.isEmpty(document.getId()) || StringUtils.isEmpty(document.getName())))
      {
         document = null;
      }

      if (null != document)
      {
         setOpenLabel(document.getName());
         MIMEType mimeType = MimeTypesHelper.detectMimeType(document.getName(), document.getContentType());
         setOpenIcon(mimeType.getCompleteIconPath());
      }
      else
      {
         setOpenLabel(MessagesViewsCommonBean.getInstance().getString("views.common.document.noDocument"));
         setOpenIcon(ResourcePaths.I_EMPTY_CORE_DOCUMENT);
      }
   }

   @Override
   public void viewDocument()
   {
      if (null != document)
      {
         if (!fireEvent(DocumentInputEventType.TO_BE_VIEWED, null))
         {
            unregisterHandler();
   
            IDocumentContentInfo docInfo = (document instanceof RawDocument) ? getFileSystemDocument() : getJCRDocument();
            Map<String, Object> params = CollectionUtils.newMap();
            params.put("processInstance", activityInstance.getProcessInstance());
            params.put("dataPathId", dataMapping.getDataPath());
            params.put("dataId", dataMapping.getDataId());
            documentView = DocumentViewUtil.openDataMappingDocument(activityInstance.getProcessInstance(),
                  dataMapping.getDataId(), docInfo, params);
            PortalApplication.getInstance().registerViewDataEventHandler(documentView, this);
            refreshPortalSession();
            
            fireEvent(DocumentInputEventType.VIEWED, null);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.common.form.jsf.DocumentInputController#saveDocument()
    */
   public boolean saveDocument()
   {
      // Save Document only if it's Raw/Unsaved Document
      if (document instanceof RawDocument)
      {
         FileSystemJCRDocument fsDoc = getFileSystemDocument();
         JCRDocument jcrDoc = (JCRDocument)fsDoc.save(fsDoc.retrieveContent());
         setValue(jcrDoc.getDocument());

         return true;
      }
      
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.common.form.jsf.DocumentInputController#closeDocument()
    */
   public void closeDocument()
   {
      if (isDocumentViewerOpened())
      {
         PortalApplication.getInstance().closeView(documentView);
         unregisterHandler();
         refreshPortalSession();
      }
   }

   @Override
   protected void uploadDocument()
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();

      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getInstance();
      fileUploadDialog.initializeBean();
      FileUploadDialogAttributes attributes = fileUploadDialog.getAttributes();
      
      attributes.setDocumentType(getPath().getDocumentType());
      attributes.setHeaderMessage(propsBean.getParamString(
            "views.genericRepositoryView.specificDocument.uploadFile", label));
      attributes.setOpenDocumentFlag(openDocument);
      attributes.setEnableOpenDocument(enableOpenDocument);

      fileUploadDialog.setCallbackHandler(new FileUploadCallbackHandler()
      {
         public void handleEvent(FileUploadEvent eventType)
         {
            if (eventType == FileUploadEvent.FILE_UPLOADED)
            {
               try
               {
                  FileWrapper fileWrapper = getFileWrapper();
                  RawDocument rawDocument = new RawDocument(fileWrapper.getFileInfo());
                  rawDocument.setDescription(fileWrapper.getDescription());
                  rawDocument.setComments(fileWrapper.getComments());
                  rawDocument.setDocumentType(fileWrapper.getDocumentType());

                  if (!fireEvent(DocumentInputEventType.TO_BE_UPLOADED, getFileSystemDocument(rawDocument)))
                  {
                     setValue(rawDocument);
                     fireEvent(DocumentInputEventType.UPLOADED, null);
                     if (fileWrapper.isOpenDocument())
                     {
                        viewDocument();
                     }
                  }
                  refreshPortalSession();
               }
               catch (Exception e)
               {
               }
            }
         }
      });
      fileUploadDialog.openPopup();
      refreshPortalSession();
   }

   /**
    * Display confirmation dialog only in case of JCR document, else just update activity panel
    * 
    * @author Yogesh.Manware
    * 
    */
   @Override
   public void deleteDocument()
   {
      // check if the document is JCR document
      if (!(document instanceof RawDocument))
      {
         MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();

         ConfirmationDialogWithOptionsBean confirmationDialog = ConfirmationDialogWithOptionsBean.getInstance();
         confirmationDialog.setContentType(DialogContentType.WARNING);
         confirmationDialog.setActionType(DialogActionType.OK_CANCEL);

         confirmationDialog.setTitle(MessagesViewsCommonBean.getInstance().getString("common.confirm"));

         // If process supports process attachments
         if (DMSHelper.existsProcessAttachmentsDataPath(activityInstance.getProcessInstance()))
         {
            confirmationDialog.setMessage(propsBean.getString("views.activityPanel.deleteDocument.message"));
            confirmationDialog.setIncludePath(ResourcePaths.V_CONFIRMATION_DIALOG_OPTIONS);

            // set available options
            String[] keys = {RemoveDocumentOptions.MOVE_TO_PROCESS_ATTACHMENTS.name(), RemoveDocumentOptions.DELETE_PERMANENTLY.name()};
            confirmationDialog.setOptions(IceComponentUtil.buildSelectItemArray(DELETE_DOCUMENT_OPTIONS_PREFIX, keys,
                  MessagePropertiesBean.getInstance()));
            confirmationDialog.setSelectedOption(DELETE_DOCUMENT_OPTIONS_PREFIX
                  + RemoveDocumentOptions.MOVE_TO_PROCESS_ATTACHMENTS.name());

            confirmationDialog.setHandler(new ConfirmationDialogHandler()
            {
               public boolean cancel()
               {
                  return true;
               }

               public boolean accept()
               {
                  if (ConfirmationDialogWithOptionsBean.getInstance().getSelectedOption()
                        .contains(RemoveDocumentOptions.MOVE_TO_PROCESS_ATTACHMENTS.name()))
                  {
                     handleDocumentToBeMoved(document);
                  }
                  else
                  {
                     handleDocumentToBeDeleted(document);
                  }
                  updateActivityPanel();
                  return true;
               }
            });
         }
         // If process does not supports process attachments
         else
         {
            confirmationDialog.setMessage(MessagesViewsCommonBean.getInstance().getString(
                  "common.confirmDeleteRes.message.label"));
            confirmationDialog.setActionType(DialogActionType.YES_NO);

            confirmationDialog.setHandler(new ConfirmationDialogHandler()
            {
               public boolean cancel()
               {
                  return true;
               }

               public boolean accept()
               {
                  handleDocumentToBeDeleted(document);
                  updateActivityPanel();
                  return true;
               }
            });
         }

         confirmationDialog.openPopup();
      }
      else
      {
         updateActivityPanel();
      }
   }

   /**
    * @param document
    */
   private void handleDocumentToBeDeleted(Document document)
   {
      if (null != documentToBeMoved || null != documentToBeDeleted)
      {
         DocumentMgmtUtility.getDocumentManagementService().removeDocument(documentToBeDeleted.getId());
      }
      else
      {
         documentToBeDeleted = document;
      }
   }

   /**
    * @param documentMoved
    */
   private void handleDocumentToBeMoved(Document documentMoved)
   {
      if (null != documentToBeMoved || null != documentToBeDeleted)
      {
         DMSHelper.addAndSaveProcessAttachment(activityInstance.getProcessInstance(), documentMoved, true);
      }
      else
      {
         documentToBeMoved = documentMoved;
      }
   }

   /**
    * @param action
    */
   private void processJCRDocuments(String action)
   {
      if (WorkflowAction.COMPLETE.name().equals(action) || WorkflowAction.SAVE.name().equals(action))
      {
         if (null != documentToBeDeleted)
         {
            DocumentMgmtUtility.getDocumentManagementService().removeDocument(documentToBeDeleted.getId());
         }

         if (null != documentToBeMoved)
         {
            DMSHelper.addAndSaveProcessAttachment(activityInstance.getProcessInstance(), documentToBeMoved, true);
         }
      }
   }

   private void updateActivityPanel()
   {
      if (!fireEvent(DocumentInputEventType.TO_BE_DELETED, null))
      {
         setValue(null);
         closeDocument();

         fireEvent(DocumentInputEventType.DELETED, null);
      }
   }

   @Override
   public void destroy(String action)
   {
      processJCRDocuments(action);
      super.destroy(action);
      unregisterHandler();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.event.ViewDataEventHandler#handleEvent(org.eclipse.stardust.ui.web.common.event.ViewDataEvent)
    */
   public void handleEvent(ViewDataEvent event)
   {
      switch (event.getType())
      {
      case DATA_MODIFIED:
         Object payload = event.getPayload();
         if (payload instanceof JCRDocument)
         {
            setValue(((JCRDocument)payload).getDocument());
         }
         else
         {
            // Ideally, this should not happen
            trace.error("Received Document is not instanceof JCRDocument");
         }
         break;
      }
   }

   /**
    * @return
    */
   public boolean isDocumentViewerOpened()
   {
      return (null != documentView && ViewState.CLOSED != documentView.getViewState()) ? true : false;
   }

   /**
    * @return
    */
   public IDocumentContentInfo getDocumentContentInfo()
   {
      if (document instanceof RawDocument)
      {
         return getFileSystemDocument();
      }
      else
      {
         return getJCRDocument();
      }
   }

   /**
    * @param doc
    * @return
    */
   private FileSystemJCRDocument getFileSystemDocument(Document doc)
   {
      if (doc instanceof RawDocument)
      {
         RawDocument rawDocument = (RawDocument) doc;
         String parentFolder = DocumentMgmtUtility.getTypedDocumentsFolderPath(activityInstance.getProcessInstance());
         return new FileSystemJCRDocument(rawDocument.getFileInfo().getPhysicalPath(), doc.getDocumentType(),
               parentFolder, rawDocument.getDescription(), rawDocument.getComments());
      }

      return null;
   }

   /**
    * @return
    */
   private FileSystemJCRDocument getFileSystemDocument()
   {
      return getFileSystemDocument(document);
   }

   /**
    * @return
    */
   private JCRDocument getJCRDocument()
   {
      try
      {
         document = DocumentMgmtUtility.getDocument(document.getId());
      }
      catch (ResourceNotFoundException e)
      {
         // should never occur
      }
      return new JCRDocument(document, getPath().isReadonly());
   }

   /**
    * 
    */
   private void unregisterHandler()
   {
      if (null != documentView)
      {
         PortalApplication.getInstance().unregisterViewDataEventHandler(documentView, this);
      }
   }

   /**
    * 
    */
   private void refreshPortalSession()
   {
      if (ActivityInstanceUtils.isIframeBased(activityInstance))
      {
         PortalApplication.getInstance().renderPortalSession();
      }
   }

   /**
    * @param eventType
    * @param newDocument
    * @return
    */
   private boolean fireEvent(DocumentInputEventType eventType, IDocumentContentInfo newDocument)
   {
      if (null != handler)
      {
         DocumentInputEvent event = new DocumentInputEvent(this, eventType, newDocument);
         handler.handleEvent(event);
         return event.isVetoed();
      }

      return false;
   }

   public DataMapping getDataMapping()
   {
      return dataMapping;
   }

   public void setOpenDocument(boolean openDocument)
   {
      this.openDocument = openDocument;
   }

   public void setEnableOpenDocument(boolean enableOpenDocument)
   {
      this.enableOpenDocument = enableOpenDocument;
   }
}
