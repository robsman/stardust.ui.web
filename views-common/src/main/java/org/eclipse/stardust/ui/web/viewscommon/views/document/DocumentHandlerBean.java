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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.ui.common.form.FormGenerator;
import org.eclipse.stardust.ui.common.form.jsf.DocumentForm;
import org.eclipse.stardust.ui.common.form.jsf.DocumentObject;
import org.eclipse.stardust.ui.common.form.jsf.ILabelProvider;
import org.eclipse.stardust.ui.common.form.jsf.JsfFormGenerator;
import org.eclipse.stardust.ui.common.form.jsf.JsfStructureContainer;
import org.eclipse.stardust.ui.common.form.preferences.FormGenerationPreferences;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewDataEvent;
import org.eclipse.stardust.ui.web.common.event.ViewDataEvent.ViewDataEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DocumentTypeWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.FileSaveDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.FileSaveDialog.FileSaveCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.OutputResource;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentEventListener.DocumentEventType;
import org.eclipse.stardust.ui.web.viewscommon.views.document.helper.CorrespondenceMetaData;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DocumentHandlerBean extends UIComponentBean implements ViewEventHandler
{
   private static final Logger trace = LogManager.getLogger(DocumentHandlerBean.class);

   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "documentHandlerBean";
   private static final String TIFF_IMAGE="image/tiff";
   private IDocumentContentInfo documentContentInfo;
   private CorrespondenceMetaData correspondencInfo;
   private boolean correspondencInfoAvailble = false;
   private String description;
   private String inputDescription;
   private IDocumentViewer contentHandler;
   private MessagesViewsCommonBean propsBean = null;
   private View thisView;
   private OutputResource fileOutputResource = null;
   private ProcessInstance processInstance;
   private boolean detailsPanelExpanded;
   private DocumentForm documentForm;
   private String documentTypeName;
   private boolean propertiesPanelExpanded = true;
   private boolean metaDataPanelExpanded = true;
   private String dataPathId;
   private String dataId;
   private String baseFormBinding = BEAN_NAME;
   private boolean disableSaveAction;
   private boolean embededView;
   private boolean loadSuccessful = false;
   private String loadUnsuccessfulMsg;

   /**
    * default constructor
    */
   public DocumentHandlerBean()
   {
      super("documentView");
   }
   
   public static DocumentHandlerBean getInstance()
   {
      return (DocumentHandlerBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(com.sungard.framework
    * .ui.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      propsBean = MessagesViewsCommonBean.getInstance();
      loadUnsuccessfulMsg = propsBean.getString("views.documentView.fetchError");
      
      if (ViewEventType.CREATED == event.getType())
      {
         loadSuccessful = false;
         thisView = event.getView();
        
         if (!retrieveAndSetInputParameters(event))
         {
            return;
         }

         // Check if this document is already open in any Activity Panel
         if (!embededView)
         {
            for (View openView : PortalApplication.getInstance().getOpenViews())
            {
               if ("activityPanel".equals(openView.getDefinition().getName()))
               {
                  try
                  {
                     Object activityDetailsBean = openView.getCurrentTabScope().get("activityDetailsBean");
                     DocumentHandlerBean docHandlerBean = (DocumentHandlerBean) ReflectionUtils.invokeGetterMethod(
                           activityDetailsBean, "documentHandlerBean");
                     if (null != docHandlerBean) // Single Document Case
                     {
                        IDocumentContentInfo docInfo = docHandlerBean.getDocumentContentInfo();
                        if (docInfo.getId().equals(this.getDocumentContentInfo().getId()))
                        {
                           event.setVetoed(true);
                           MessageDialog.addInfoMessage(getMessages().getString("message.alreadyOpenInActivityPanel"));
                           return;
                        }
                     }
                  }
                  catch (Exception e)
                  {
                     // TODO
                  }
               }
            }
         }

         if (!initializeBean())
         {
            event.setVetoed(true);
            return;
         }
         
         // pop-out the document
         String displayMode = thisView.getParamValue("displayMode");
         if (RepositoryUtility.DOCUMENT_DISPLAY_MODE_NEWBROWSER.equals(displayMode))
         {
            popOutDocument();
         }
         
         loadSuccessful = true;
      }
      else if (ViewEventType.ACTIVATED == event.getType())
      {
         loadSuccessful = false;
         // check if the document still exist
         if (documentContentInfo instanceof JCRDocument)
         {
            try
            {
               DocumentMgmtUtility.getDocument(documentContentInfo.getId());
            }
            catch (ResourceNotFoundException exception)
            {
               fireDocumentDeletedEvent();
               loadUnsuccessfulMsg = propsBean.getString("views.documentView.documentNotFoundError");
               return;
            }
         }
         
         //refresh if different document is selected (but with same view-key)
         IDocumentContentInfo documentInfoParam = (IDocumentContentInfo) event.getView().getViewParams()
               .get("documentInfo");
         if (null != documentInfoParam && !documentInfoParam.getId().equals(documentContentInfo.getId()))
         {
            documentContentInfo = documentInfoParam;
            thisView = event.getView();
            if (!initializeBean())
            {
               return;
            }
         }
         
         ExternalDocumentViewerBean externalDocumentViewer = ExternalDocumentViewerBean.getInstance();
         if (externalDocumentViewer.isOpened()
               && !externalDocumentViewer.getDocumentId().equals(documentContentInfo.getId()))
         {
            externalDocumentViewer.openDocument(this, thisView);
         }
         
         loadSuccessful = true;
      }
      else if (ViewEventType.TO_BE_CLOSED == event.getType())
      {
         loadSuccessful = false;
         ExternalDocumentViewerBean externalDocumentViewer = ExternalDocumentViewerBean.getInstance();
         if (externalDocumentViewer.isOpened())
         {
            externalDocumentViewer.closePopupDocument();
         }
         if (null != contentHandler)
         {
            contentHandler.closeDocument();
         }
         loadSuccessful = true;
      }

      if (contentHandler instanceof ViewEventHandler && loadSuccessful)
      {
         ((ViewEventHandler) contentHandler).handleEvent(event);
      }
   }

   /**
    * @param viewEvent
    */
   public boolean retrieveAndSetInputParameters(ViewEvent viewEvent)
   {
      dataPathId = (String) thisView.getViewParams().get("dataPathId");
      dataId = (String) thisView.getViewParams().get("dataId");
      baseFormBinding = (String) thisView.getViewParams().get("baseFormBinding");
      if (StringUtils.isEmpty(baseFormBinding))
      {
         baseFormBinding = BEAN_NAME;
      }

      documentContentInfo = (IDocumentContentInfo) thisView.getViewParams().get("documentInfo");

      if (null == documentContentInfo && null != thisView.getViewParams().get("documentId"))
      {
         String documentId = (String) thisView.getViewParams().get("documentId");
         
         try
         {
            documentContentInfo = new JCRDocument(DocumentMgmtUtility.getDocument(documentId));
            thisView.getViewParams().put("documentInfo", documentContentInfo);
         }
         catch (ResourceNotFoundException e)
         {
            viewEvent.setVetoed(true);
            ExceptionHandler.handleException(e);
            return false;
         }
      }

      processInstance = (ProcessInstance) thisView.getViewParams().get("processInstance");

      if (null == processInstance)
      {
         Object processInsOIdObj = thisView.getViewParams().get("processInstanceOId");
         Long processInstanceOid;
         if (null != processInsOIdObj)
         {
            if (processInsOIdObj instanceof Number)
            {
               processInstanceOid = ((Number) processInsOIdObj).longValue();
            }
            else
            {
               processInstanceOid = Long.valueOf((String) processInsOIdObj);
            }
            processInstance = ProcessInstanceUtils.getProcessInstance(processInstanceOid);
         }
      }
      Object embededViewObj = thisView.getViewParams().get("embededView");

      if (null != embededViewObj)
      {
         if (embededViewObj instanceof Boolean)
         {
            embededView = (Boolean) embededViewObj;
         }
         else
         {
            embededView = Boolean.valueOf((String) embededViewObj);
         }
      }

      Object disableSaveActionObj = thisView.getViewParams().get("disableSaveAction");

      if (null != disableSaveActionObj)
      {
         if (disableSaveActionObj instanceof Boolean)
         {
            disableSaveAction = (Boolean) disableSaveActionObj;
         }
         else
         {
            disableSaveAction = Boolean.valueOf((String) disableSaveActionObj);
         }
      }
      return true;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public boolean initializeBean()
   {
      DocumentHandlersRegistryBean documentHandlersRegistryBean = DocumentHandlersRegistryBean.getInstance();
      try
      {
         contentHandler = documentHandlersRegistryBean.getContentHandler(documentContentInfo, thisView);

         if (CollectionUtils.isNotEmpty(documentContentInfo.getProperties()))
         {
            correspondencInfo = new CorrespondenceMetaData(documentContentInfo.getProperties());
            correspondencInfoAvailble = correspondencInfo.isCorrespondencInfoAvailble();
         }
         else
         {
            correspondencInfoAvailble = false;
         }

         description = documentContentInfo.getDescription();
         if (null == this.description)
         {
            description = "";
         }
         inputDescription = description;
         
         String documentVersion = "";
         if (documentContentInfo.isSupportsVersioning())
         {
            documentVersion = " (" + documentContentInfo.getVersionTracker().getCurrentVersionNo() + ")";
         }

         thisView.getViewParams().put("documentVersion", documentVersion);
         thisView.setIcon(documentContentInfo.getIcon());
         thisView.getViewParams().put("documentName", documentContentInfo.getName());
         thisView.resolveLabelAndDescription();
         
         // update popup content if it is already in open state
         ExternalDocumentViewerBean externalDocumentViewer = ExternalDocumentViewerBean.getInstance();
         if (externalDocumentViewer.isOpened())
         {
            externalDocumentViewer.openDocument(this, thisView);
         }

         // Handle Meta Data
         // Read Preferences for Form Generation
         Integer noOfColumnsInColumnLayout;
         Integer noOfColumnsInTable;
         try
         {
            // Using Reflection because this configuration exists in process-portal
            noOfColumnsInColumnLayout = ReflectionUtils.invokeStaticMethod(
                  "org.eclipse.stardust.ui.web.processportal.view.ActivityPanelConfigurationBean",
                  "getAutoNoOfColumnsInColumnLayout()", new Object[0]);
            noOfColumnsInTable = ReflectionUtils.invokeStaticMethod(
                  "org.eclipse.stardust.ui.web.processportal.view.ActivityPanelConfigurationBean",
                  "getAutoNoOfColumnsInTable()", new Object[0]);
         }
         catch (Exception e)
         {
            trace.error("Unable to read preferences for 'NoOfColumnsInColumnLayout' / 'NoOfColumnsInTable'"
                  + " from ActivityPanelConfigurationBean.java. Using 3 & 5 respectively as drfault", e);
            noOfColumnsInColumnLayout = 3;
            noOfColumnsInTable = 5;
         }

         // Render Meta Data
         FormGenerationPreferences generationPreferences = new FormGenerationPreferences(noOfColumnsInColumnLayout,
               noOfColumnsInTable);
         DeployedModel model = ModelUtils.getModelForDocumentType(documentContentInfo.getDocumentType());

         if (null != model && StringUtils.isEmpty(dataId))
         {
            List<Data> dataList = DocumentTypeUtils.getDataUsingDocumentType(model,
                  documentContentInfo.getDocumentType());
            if (CollectionUtils.isNotEmpty(dataList))
            {
               dataId = dataList.get(0).getId();
            }
         }         
         
         ILabelProvider labelProvider = null;
         FormGenerator formGenerator;
         if (null != model && StringUtils.isNotEmpty(dataId))
         {
            labelProvider = new DocumentMetaDataLabelProvider(model.getData(dataId), model);
            formGenerator = new JsfFormGenerator(generationPreferences, baseFormBinding + ".documentForm", labelProvider);
         }
         else
         {
            formGenerator = new JsfFormGenerator(generationPreferences, baseFormBinding + ".documentForm");
         }

         documentForm = new DocumentForm(new DocumentObject(documentContentInfo.getDocumentType(),
               documentContentInfo.getProperties(), !documentContentInfo.isMetaDataEditable()), model, formGenerator,
               labelProvider);

         documentTypeName = null;
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, propsBean.getString("views.documentView.error.initializationFailed"));
         return false;
      }
      return true;
   }

   public OutputResource getFileOutputResource()
   {
      if (null == fileOutputResource)
      {
         if (documentContentInfo instanceof JCRDocument)
         {
            fileOutputResource = new OutputResource(documentContentInfo.getName(), documentContentInfo.getId(),
                  documentContentInfo.getMimeType().getType(), null,
                  DocumentMgmtUtility.getDocumentManagementService(), true);
         }
     }
      return fileOutputResource;
   }
   
   /**
    * Save document action handler
    */
   public void save()
   {
      save(null);
   }


   /**
    * @param callback
    */
   public void save(ICallbackHandler callback)
   {
      if (isSavable() && isModified())
      {
         try
         {
            FileSaveDialog fileSaveDialog = FileSaveDialog.getInstance();
            fileSaveDialog.initialize();
            if (documentContentInfo instanceof FileSystemJCRDocument)
            {
               fileSaveDialog.setComments(documentContentInfo.getComments());
            }
            fileSaveDialog.setCallbackHandler(new DCCallBackHandler(false, callback));
            if (contentHandler instanceof ICustomDocumentSaveHandler
                  && ((ICustomDocumentSaveHandler) contentHandler).usesCustomSaveDialog())
            {
               fileSaveDialog.setCustomDialog(true);
               fileSaveDialog
                     .setCustomDialogPosition(((ICustomDocumentSaveHandler) contentHandler).getDialogPosition());
               fileSaveDialog.setCustomDialogSource(((ICustomDocumentSaveHandler) contentHandler).getCustomDialogURL());
               ((ICustomDocumentSaveHandler) contentHandler).setCustomSaveDialogOptions();
            }
            fileSaveDialog.openPopup();
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
            callback.handleEvent(EventType.CANCEL);
         }
      }
      else
      {
         if (null == callback)
         {
            MessageDialog.addWarningMessage(propsBean.getString("views.documentView.saveDocumentDialog.notSaved"));
         }
         else
         {
            callback.handleEvent(EventType.APPLY);
         }
      }
   }
   
   /**
    * @param e
    */
   public void detailsPanelCollapseListener(ActionEvent event)
   {
      //TODO - determine whether it's a collapse or expand event and call respective methods accordingly if needed.
      //For TIFF viewer this differentiation is not needed.
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.DETAILS_PANEL_COLLAPSED);
      }
   }
   
   /**
    * 
    */
   public void togglePropertiesPanel()
   {
      setPropertiesPanelExpanded(!isPropertiesPanelExpanded());
      detailsPanelCollapseListener(null);
   }

   /**
    * 
    */
   public void toggleMetaDataPanel()
   {
      setMetaDataPanelExpanded(!isMetaDataPanelExpanded());
      detailsPanelCollapseListener(null);
   }

   /**
    * revert version
    */
   public void revertToVersion()
   {
      try
      {
         // get the version revision id and save it with new version using kernel API
         FileSaveDialog fileSaveDialog = FileSaveDialog.getInstance();
         fileSaveDialog.initialize();
         fileSaveDialog.setTitle(MessagesViewsCommonBean.getInstance().getString(
               "views.documentView.saveDocumentDialog.saveDocument"));
         fileSaveDialog.setHeaderMessage(propsBean.getString("views.documentView.saveDocumentDialog.revert"));
         fileSaveDialog.setCallbackHandler(new DCCallBackHandler(true));
         //check if the document type matches
         IDocumentContentInfo latestDocInfo = getVersionTracker().getLatestVersion();
         if (!StringUtils.areEqual(getDocumentContentInfo().getDocumentType(), latestDocInfo.getDocumentType()))
         {
            fileSaveDialog.setMessage(propsBean.getParamString(
                  "views.documentView.saveDocumentDialog.documentTypeWarning", getDocumentTypeName()));
         }
         
         fileSaveDialog.openPopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * 
    */
   public void popOutDocument()
   {
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.POPPED_OUT);
      }
      ExternalDocumentViewerBean externalDocumentViewer = ExternalDocumentViewerBean.getInstance();
      externalDocumentViewer.openDocument(this, thisView);
   }

   /**
    * 
    */
   public void popInDocument()
   {
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.POPPED_IN);
      }
      ExternalDocumentViewerBean externalDocumentViewer = ExternalDocumentViewerBean.getInstance();
      externalDocumentViewer.closePopupDocument();
   }

   /**
    * @return
    */
   public boolean isPoppedOut()
   {
      boolean poppedOut = false;

      ExternalDocumentViewerBean externalDocumentViewer = ExternalDocumentViewerBean.getInstance();
      if (externalDocumentViewer.isOpened())
      {
         poppedOut = true;
      }
      return poppedOut;
   }

   /**
    * Save document contents
    * 
    * @param force
    * @throws ResourceNotFoundException 
    */
   private void saveDocumentContents(boolean force, String comments) throws ResourceNotFoundException 
   {
      if (force || isModified())
      {
         if (!(force && !documentContentInfo.getVersionTracker().isLatestVersion()) && contentHandler instanceof ICustomDocumentSaveHandler)
         {
            documentContentInfo = ((ICustomDocumentSaveHandler) contentHandler).save();
         }
         
         if (isDescriptionChanged())
         {
            documentContentInfo.setDescription(inputDescription);
         }
         // If Viewer get content from Repository else get it from Editor
         byte[] contentByte = null;
         if (contentHandler instanceof IDocumentEditor)
         {
            contentByte = contentHandler.getContent().getBytes();
         }
         else //for viewer and unsupported documents, retrieve content
         {
            contentByte = documentContentInfo.retrieveContent();
         }

         documentContentInfo.setComments(comments);

         // Add Meta Data
         // TODO look at following code later
         Map<String, Object> metaData = documentForm.retrieveMetaData();
         if (null != metaData)
         {
            documentContentInfo.getProperties().putAll(metaData);
         }

         /*
          * In case of TIFF documents the view / tiff-iframe needs to be refreshed with the latest
          * document id if the document being saved is a FileSystemJCRDocument.
          * The refreshViewer flag is set if the document being save
          * 
          * TODO - review
         */
         boolean refreshViewer = false;
         if (documentContentInfo instanceof FileSystemJCRDocument)
         {
            refreshViewer = true;
         }
         
         beforeSave(refreshViewer);
         
         documentContentInfo = documentContentInfo.save(contentByte);

         postSave(refreshViewer);
      }
      else
      {
         MessageDialog.addWarningMessage(propsBean.getString("views.documentView.saveDocumentDialog.notSaved"));
      }
   }

   /**
    * This is needed for TIFF viewer when the document being saved is a FileSystemJCRDocument.
    * The iframe with the older document id is removed.
    * 
    * Viewer will be refreshed with the latest saved version in the postSave() method.
    */
   private void beforeSave(boolean refreshViewer)
   {
      if(refreshViewer)
      {
         fireRefreshViewerToBeInvoked();
      }
   }
   
   /**
    * Updates the ProcessAttachments and reinitialize the data
    * @throws ResourceNotFoundException 
    */
   private void postSave(boolean refreshViewer) throws ResourceNotFoundException
   {
      if (null != processInstance && documentContentInfo instanceof JCRDocument)
      {
         Document docToBeUpdated = ((JCRDocument) documentContentInfo).getDocument();
         // update specific document
         if (org.eclipse.stardust.common.StringUtils.isNotEmpty(dataPathId))
         {
            TypedDocumentsUtil.updateTypedDocument(processInstance.getOID(), dataPathId, dataId, docToBeUpdated);
         }
         // update process attachment
         else
         {
            DMSHelper.updateProcessAttachment(processInstance, docToBeUpdated);
            // Following code is required if the document needs to be saved again in the
            // same view Above call make some changes in document properties which causes
            // classcast arraylist exception at document.getProperties() in next iteration
            documentContentInfo = documentContentInfo.reset();
         }
      }
      
      //update view parameter - this is used to check if refresh is required while handling "Activated" event
      thisView.getViewParams().put("documentInfo", documentContentInfo);

      PortalApplication.getInstance().broadcastViewDataEvent(
            new ViewDataEvent(thisView, ViewDataEventType.DATA_MODIFIED, documentContentInfo));

      initializeBean();
      
      if(refreshViewer)
      {
         fireRefreshViewerInvoked();
      }
   }
   
   public boolean isDescriptionChanged()
   {
      return !description.equals(inputDescription);
   }

   /**
    * @return
    */
   private boolean isModified()
   {
      if ((contentHandler instanceof ICustomDocumentSaveHandler && ((ICustomDocumentSaveHandler) contentHandler).isModified())
            || contentHandler instanceof IDocumentEditor
            || isDescriptionChanged() || (null != documentForm && documentForm.isMetaDataAvailable()))
      {
         return true;
      }
      
      return false;
   }

   public boolean isSavable()
   {
      return documentContentInfo.isContentEditable();
   }

   public boolean isDisableSaveAction()
   {
      return disableSaveAction;
   }

   public String getInputDescription()
   {
      return inputDescription;
   }

   public void setInputDescription(String inputDescription)
   {
      this.inputDescription = inputDescription;
      if (isDescriptionChanged() && (contentHandler instanceof ICustomDocumentSaveHandler))
      {
         ((ICustomDocumentSaveHandler) contentHandler).setDescriptionChanged(true);
      }
   }

   public void showPreviousVersion()
   {
      try
      {
         fireShowPreviousVersionToBeInvoked();
         documentContentInfo = documentContentInfo.getVersionTracker().shiftToPreviousVersion();
         initializeBean();
         fireShowPreviousVersionInvoked();
         
         if (isMetaDataAvailable())
         {
            FacesUtils.refreshPage(); // This is needed otherwise Meta Data is not rendered correctly
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   public void showNextVersion()
   {
      try
      {
         fireShowNextVersionToBeInvoked();
         documentContentInfo = documentContentInfo.getVersionTracker().shiftToNextVersion();
         initializeBean();
         fireShowNextVersionInvoked();

         if (isMetaDataAvailable())
         {
            FacesUtils.refreshPage(); // This is needed otherwise Meta Data is not rendered correctly
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   /**
    * This method is called when Upload New Version is called from Document Viewer
    * toolbar.
    */
   public void uploadNewVersion()
   {
      final CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
      fileUploadDialog.initialize();
      fileUploadDialog.setTitle(propsBean.getString("views.documentView.saveDocumentDialog.uploadNewVersion.label"));
      fileUploadDialog.setHeaderMessage(propsBean.getParamString(
            "views.documentView.saveDocumentDialog.uploadNewVersion.text", documentContentInfo.getName()));
      fileUploadDialog.setDocumentType(documentContentInfo.getDocumentType());
      // In Document viewer document is already open, so no need to set Open Document
      fileUploadDialog.setEnableOpenDocument(false);
      fileUploadDialog.setOpenDocumentFlag(true);
      fileUploadDialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType == EventType.APPLY)
            {
               try
               {
                  JCRDocument dmsDocument = (JCRDocument) documentContentInfo;
                  Document document = dmsDocument.getDocument();
                  String origFileMimeType = documentContentInfo.getMimeType().getType();

                  // Close the current IFrame if available
                  fireShowNextVersionToBeInvoked();
                  // Remove current document from sessionMap, required for creating new
                  // object with same documentId with new metadata
                  SessionSharedObjectsMap sessionMap = SessionSharedObjectsMap.getCurrent();
                  sessionMap.removeObject(document.getId());

                  documentContentInfo.setComments(fileUploadDialog.getComments());
                  documentContentInfo.setAnnotations(null);
                  documentContentInfo.setDescription(fileUploadDialog.getDescription());
                  documentContentInfo = documentContentInfo.saveFile(fileUploadDialog.getFileInfo().getPhysicalPath());

                  String newFileMimeType = documentContentInfo.getMimeType().getType();
                  postSave(false);
                  // Activiate the Iframe with dealy if available
                  fireRefreshViewerWithDelayInvoked();
               }
               catch (Exception e)
               {
                  DocumentMgmtUtility.verifyExistanceOfDocumentAndShowMessage(documentContentInfo.getId(), "", e);
               }
            }
         }
      });
      fileUploadDialog.openPopup();
   }

   /**
    * This method is called when document is modified in the background and the
    * viewer needs to be refreshed with the latest document.
    *  
    * @param document
    */
   public void refreshViewer()
   {
      fireRefreshViewerToBeInvoked();
      try
      {
         documentContentInfo = this.documentContentInfo.reset();
         thisView.getViewParams().put("documentInfo", documentContentInfo);
      }
      catch (ResourceNotFoundException e)
      {
         ExceptionHandler.handleException(e);
      }
      
      initializeBean();
      fireRefreshViewerInvoked();
   }

   public IDocumentContentInfo getDocumentContentInfo()
   {
      return documentContentInfo;
   }

   /**
    * @return
    */
   public String getDocumentTypeName()
   {
      if (null == documentTypeName)
      {
         if (null != documentContentInfo.getDocumentType())
         {
            DocumentTypeWrapper documentTypeObj = new DocumentTypeWrapper(documentContentInfo.getDocumentType());
            documentTypeName = documentTypeObj.getDocumentTypeI18nName();
         }
         else
         {
            documentTypeName = "";
         }
      }
      return documentTypeName;
   }

   public String getDescription()
   {
      return description;
   }

   public IVersionTracker getVersionTracker()
   {
      return documentContentInfo.getVersionTracker();
   }

   public boolean isShowPrev()
   {
      if (null == getVersionTracker())
      {
         return false;
      }
      return getVersionTracker().hasPreviousVersion();
   }

   public IDocumentViewer getContentHandler()
   {
      return contentHandler;
   }

   public boolean isShowNext()
   {
      if (null == getVersionTracker())
      {
         return false;
      }
      return getVersionTracker().hasNextVersion();
   }

   public boolean isRevertible()
   {
      if (null == getVersionTracker())
      {
         return false;
      }
      return !getVersionTracker().isLatestVersion() && documentContentInfo.isModifyPrivilege();
   }

   public CorrespondenceMetaData getCorrespondencInfo()
   {
      return correspondencInfo;
   }

   public String getUserLabel()
   {
      return documentContentInfo.getAuthor();
   }

   public View getThisView()
   {
      return thisView;
   }

   public String getLabel()
   {
      return getThisView().getViewParams().get("documentName") + " " + thisView.getParamValue("documentVersion");
   }
   
   private void fireShowPreviousVersionToBeInvoked()
   {
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.SHOW_PREVIOUS_VERSION_TO_BE_INVOKED);
      }
   }
   
   private void fireShowPreviousVersionInvoked()
   {
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.SHOW_PREVIOUS_VERSION_INVOKED);
      }
   }
   
   private void fireShowNextVersionToBeInvoked()
   {
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.SHOW_NEXT_VERSION_TO_BE_INVOKED);
      }
   }
   
   private void fireShowNextVersionInvoked()
   {
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.SHOW_NEXT_VERSION_INVOKED);
      }
   }
   
   private void fireDocumentDeletedEvent()
   {
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.DOCUMENT_DELETED);
      }
   }
   
   private void fireRefreshViewerToBeInvoked()
   {
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.REFRESH_VIWER_TO_BE_INVOKED);
      }
   }
   
   private void fireRefreshViewerInvoked()
   {
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.REFRESH_VIWER_INVOKED);
      }
   }
   
   private void fireRefreshViewerWithDelayInvoked()
   {
      if (contentHandler instanceof IDocumentEventListener)
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.REFRESH_VIWER_WITH_DELAY_INVOKED);
      }
   }

   public boolean isDetailsPanelExpanded()
   {
      return detailsPanelExpanded;
   }

   public void setDetailsPanelExpanded(boolean detailsPanelExpanded)
   {
      this.detailsPanelExpanded = detailsPanelExpanded;
   }

   public DocumentForm getDocumentForm()
   {
      return documentForm;
   }
   
   public boolean isMetaDataAvailable()
   {
      return (null != documentForm && documentForm.isMetaDataAvailable()) ? true : false;
   }

   public UIComponent getMetaDataRootGrid()
   {
      return ((JsfStructureContainer)documentForm.getRootContainer()).getRootGrid();
   }

   public boolean isPropertiesPanelExpanded()
   {
      return propertiesPanelExpanded;
   }

   public void setPropertiesPanelExpanded(boolean propertiesPanelExpanded)
   {
      this.propertiesPanelExpanded = propertiesPanelExpanded;
   }

   public boolean isMetaDataPanelExpanded()
   {
      return metaDataPanelExpanded;
   }

   public void setMetaDataPanelExpanded(boolean metaDataPanelExpanded)
   {
      this.metaDataPanelExpanded = metaDataPanelExpanded;
   }

   /**
    * @author Yogesh.Manware
    * @version $Revision: $
    */
   class DCCallBackHandler extends FileSaveCallbackHandler
   {
      private boolean force;
      private String comments;
      private ICallbackHandler callback;
      
      public DCCallBackHandler(boolean force)
      {
         this(force, null);
      }

      public DCCallBackHandler(boolean force, ICallbackHandler callback)
      {
         this.force = force;
         this.callback = callback;
      }      

      public void handleEvent(EventType eventType)
      {
         if (eventType == EventType.APPLY)
         {
            try
            {
               saveDocumentContents(force, getComments());
               if (null != callback)
               {
                  callback.handleEvent(EventType.APPLY);
               }
            }
            catch (ResourceNotFoundException exception)
            {
               ExceptionHandler.handleException(exception);
               fireDocumentDeletedEvent();
               loadSuccessful = false;
               return;
            }
            catch (Exception e)
            {
               RepositoryUtility.showErrorPopup("views.genericRepositoryView.saveFile.error", null, e);
               fireDocumentDeletedEvent();
               loadSuccessful = false;
               return;
            }

            if (null != callback)
            {
               callback.handleEvent(EventType.CANCEL);
            }
         }
      }

      public String getComments()
      {
         return comments;
      }

      public void setComments(String comments)
      {
         this.comments = comments;
      }

   }

   public boolean isCorrespondencInfoAvailble()
   {
      return correspondencInfoAvailble;
   }

   @Override
   public void initialize()
   {
      // TODO Auto-generated method stub
      
   }

   public boolean isLoadSuccessful()
   {
      return loadSuccessful;
   }

   public String getLoadUnsuccessfulMsg()
   {
      return loadUnsuccessfulMsg;
   }
}