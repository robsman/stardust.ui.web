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
package org.eclipse.stardust.ui.web.processportal.view;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.viewscommon.common.AbstractProcessExecutionPortal.GENERIC_PANEL;

import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.LinkDirection;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.processportal.interaction.iframe.FaceletPanelInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.NoteTip;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.LinkedProcessBean;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.AbstractDocumentUploadHelper;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.DocumentUploadHelper;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.TypedDocumentUploadHelper;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.AbstractDocumentUploadHelper.DocumentUploadCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.AbstractDocumentUploadHelper.DocumentUploadCallbackHandler.DocumentUploadEventType;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadDialogAttributes;

import com.icesoft.faces.context.effects.JavascriptContext;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class ActivityPanelController extends UIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(ActivityPanelController.class);
   private ActivityDetailsBean activityDetailsBean;
   private boolean openProcessDocuments;
   private MessagesViewsCommonBean propsBean;
   private LinkedProcessBean linkedProcess;
   private DocumentInfo typedDocumentInfo;
   
   public ActivityPanelController(ActivityDetailsBean activityDetailsBean)
   {
      super("activityPanel");
      this.activityDetailsBean = activityDetailsBean;
      propsBean = MessagesViewsCommonBean.getInstance();
   }

   public Interaction getInteraction()
   {
      return activityDetailsBean.getInteraction();
   }

   public String getPanelIntegrationStrategy()
   {
      ActivityInstance ai = activityDetailsBean.getActivityInstance();

      PanelIntegrationStrategy result = null;
      if (null != ai)
      {
         IActivityInteractionController interactionController = ActivityDetailsBean
               .getInteractionController(ai.getActivity());
         if (null != interactionController)
         {
            result = interactionController.getPanelIntegrationStrategy(ai);
         }
      }

      return (null != result) ? result.getId() : PanelIntegrationStrategy.UNKNOWN.getId();
   }

   public String getIframePanelUrl()
   {
      ActivityInstance ai = activityDetailsBean.getActivityInstance();
      if (null != ai)
      {
         // give the interaction handler a chance to provide a customized panel URI
         IActivityInteractionController interactionController = ActivityDetailsBean
               .getInteractionController(ai.getActivity());
         if (interactionController instanceof FaceletPanelInteractionController)
         {
            return ((FaceletPanelInteractionController) interactionController)
                  .provideIframePanelUri(ai, activityDetailsBean.getThisView());
         }
      }

      return getPanelUrl();
   }

   public String getPanelUrl()
   {
      String url = null;

      ActivityInstance ai = activityDetailsBean.getActivityInstance();
      if (null != ai)
      {
         // give the interaction handler a chance to provide a customized panel URI
         IActivityInteractionController interactionController = ActivityDetailsBean
               .getInteractionController(ai.getActivity());
         if (null != interactionController)
         {
            String customizedUri = interactionController.providePanelUri(ai);

            if (!StringUtils.isEmpty(customizedUri))
            {
               if (GENERIC_PANEL.equals(customizedUri))
               {
                  // translate from generic string to portal specific variant
                  url = "manual";
               }
               else
               {
                  url = customizedUri;
               }
            }
         }
         else
         {
            trace.info("Did not find an interaction controller for the current activity instance.");
         }
      }

      return !StringUtils.isEmpty(url) ? url : "";
   }

   public String getRemoteControlActivityStateChangeCommandId()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      JavascriptContext.addJavascriptCall(facesContext,
            "InfinityBpm.ProcessPortal.enableRemoteControlApi();");

      return null;
   }

   public void setRemoteControlActivityStateChangeCommandId(String commandId)
   {
      if (!isEmpty(commandId))
      {
         trace
               .info("Handling externally triggered close of activity panel: "
                     + commandId);

         ClosePanelScenario scenario = (ClosePanelScenario) ClosePanelScenario.getKey(
               ClosePanelScenario.class, commandId);

         if (ClosePanelScenario.COMPLETE == scenario)
         {
            activityDetailsBean.completeCurrentActivity();
         }
         else if (ClosePanelScenario.QA_PASS == scenario)
         {
            activityDetailsBean.completeQualityAssurancePass();
         }
         else if (ClosePanelScenario.QA_FAIL == scenario)
         {
            activityDetailsBean.completeQualityAssuranceFail();
         }
         else if (ClosePanelScenario.SUSPEND_AND_SAVE == scenario)
         {
            activityDetailsBean.suspendAndSaveCurrentActivity();
         }
         else if (ClosePanelScenario.SUSPEND == scenario)
         {
            activityDetailsBean.suspendCurrentActivity();
         }
         else if (ClosePanelScenario.ABORT == scenario)
         {
            activityDetailsBean.abortCurrentActivity();
         }
         else
         {
            trace.warn("Externally triggered command NOT handled: " + commandId);
         }
      }
   }

   @Override
   public void initialize()
   {
   // TODO Auto-generated method stub
   }

   public boolean isSupportsProcessAttachments()
   {
      return activityDetailsBean.isSupportsProcessAttachments();
   }

   public boolean isSupportsProcessDocuments()
   {
      return activityDetailsBean.isSupportsProcessDocuments();
   }

   
   public boolean isLoadSuccessful()
   {
      return activityDetailsBean.isLoadSuccessful();
   }

   public boolean isSupportsWeb()
   {
      return activityDetailsBean.isSupportsWeb();
   }

   /**
    * Open new Correspondence View
    */
   public void openCorrespondence()
   {
      ProcessInstance processInstance = activityDetailsBean.getProcessInstance();
      if (null != processInstance)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put("processInstanceOID", Long.toString(processInstance.getOID()));
         PortalApplication.getInstance().openViewById("correspondenceView",
               "DocumentID=" + processInstance.getOID(), params, null, true);
      }
   }

   /**
    * Open new Chat View
    */
   public void openChat()
   {
      ProcessInstance processInstance = activityDetailsBean.getProcessInstance();
      if (null != processInstance)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put("processInstanceOID", Long.toString(processInstance.getOID()));
         PortalApplication.getInstance().openViewById("chatView",
               "processInstanceOID=" + processInstance.getOID(), params, null, true);
      }
   }

   public void openProcessDocumentsPopup()
   {
      ProcessInstance processInstance = activityDetailsBean.getProcessInstance();
      if (null != processInstance)
      {
         Map<String, Object> params = PortalApplication.getInstance().getFocusView()
               .getViewParams();
         params.put("processInstanceOID", Long.toString(processInstance.getOID()));
         setOpenProcessDocuments(true);
      }
   }

   public void closeProcessDocumentsPopup()
   {
      setOpenProcessDocuments(false);
   }

   public boolean isOpenProcessDocuments()
   {
      return openProcessDocuments;
   }

   public void setOpenProcessDocuments(boolean openProcessDocuments)
   {
      this.openProcessDocuments = openProcessDocuments;
   }

   public List<DocumentInfo> getProcessAttachments()
   {
      return activityDetailsBean.getDisplayProcessAttachments();
   }
   
   public List<DocumentInfo> getProcessDocuments()
   {
      return activityDetailsBean.getDisplayProcessDocuments();
   }

   /**
    * timeout
    */
   public void openDocument(ActionEvent event)
   {
      DocumentInfo docInfo = (DocumentInfo) event.getComponent().getAttributes().get("documentInfo");

      if (StringUtils.isNotEmpty(docInfo.getId()))
      {
         Map<String, Object> params = CollectionUtils.newMap();
         params.put("processInstance", activityDetailsBean.getProcessInstance());
         params.put("documentName", docInfo.getName());
         DocumentViewUtil.openJCRDocument(docInfo.getId(), params);

         activityDetailsBean.closeProcessAttachmentsIframePopupSelf();
         activityDetailsBean.renderSession();
      }
      else
      {
         typedDocumentInfo = docInfo;
         uploadTypedDocument();
      }
   }

   /**
    * upload a process attachment
    */
   public void uploadProcessAttachment()
   {
      typedDocumentInfo = null;
      DocumentUploadHelper documentUploadHelper = new DocumentUploadHelper();
      documentUploadHelper.initializeDocumentUploadDialog();

      documentUploadHelper.setParentFolderPath(DocumentMgmtUtility.getProcessAttachmentsFolderPath(activityDetailsBean
            .getProcessInstance()));
      documentUploadHelper.getFileUploadDialogAttributes().setHeaderMessage(
            propsBean.getParamString("common.uploadIntoFolder",
                  propsBean.getString("views.processInstanceDetailsView.processDocumentTree.processAttachment")));
      startFileUpload(documentUploadHelper);
      documentUploadHelper.uploadFile();
   }

   private void uploadTypedDocument()
   {
      TypedDocumentUploadHelper uploadHelper = new TypedDocumentUploadHelper();
      uploadHelper.setTypedDocument(typedDocumentInfo.getTypedDocument());
      uploadHelper.initializeDocumentUploadDialog();
      
      uploadHelper.setParentFolderPath(DocumentMgmtUtility.getTypedDocumentsFolderPath(activityDetailsBean
            .getProcessInstance()));

      FileUploadDialogAttributes attributes = uploadHelper.getFileUploadDialogAttributes();
      attributes.setHeaderMessage(propsBean.getParamString("views.genericRepositoryView.specificDocument.uploadFile",
            typedDocumentInfo.getName()));

      if (null != activityDetailsBean.getActivityForm()
            && activityDetailsBean.getActivityForm().getIfSingleDocument() != null)
      {
         attributes.setEnableOpenDocument(false);
         uploadHelper.setOpenDocumentOverride(false);
      }
      attributes.setDocumentType(typedDocumentInfo.getTypedDocument().getDocumentType());
      startFileUpload(uploadHelper);
   }

   /**
    * @param uploadHelper
    */
   private void startFileUpload(AbstractDocumentUploadHelper uploadHelper)
   {
      uploadHelper.setViewParam("processInstance", activityDetailsBean.getActivityInstance().getProcessInstance());
      uploadHelper.setCallbackHandler(new DocumentUploadCallbackHandler()
      {
         public void handleEvent(DocumentUploadEventType eventType)
         {
            handleFileUploadEvents(getDocument(), eventType);
         }
      });
      uploadHelper.uploadFile();
   }

   /**
    * timeout
    */
   public void createDocument()
   {
      //FacesContext context = FacesContext.getCurrentInstance();
      ProcessInstance processInstance = activityDetailsBean.getProcessInstance();
      Folder processAttFolder = RepositoryUtility.getProcessAttachmentsFolder(processInstance);
      Document document = DocumentMgmtUtility.createBlankDocument(processAttFolder.getId(), "text/html",
            null);
      
      //update document in process instance
      DMSHelper.addAndSaveProcessAttachment(processInstance, document);
      DocumentViewUtil.openJCRDocument(document.getId());

      activityDetailsBean.closeProcessAttachmentsIframePopupSelf();
      activityDetailsBean.renderSession();
   }
   
   /**
    * File Upload Dialog Event Handler
    * 
    * @param parameters
    */
   private void handleFileUploadEvents(Document document, DocumentUploadEventType eventType)
   {
      if (DocumentUploadEventType.DIALOG_OPENED == eventType)
      {
         activityDetailsBean.closeProcessAttachmentsIframePopupSelf();
         activityDetailsBean.renderSession();
      }
      if (DocumentUploadEventType.DOCUMENT_CREATED == eventType)
      {
         if (null == typedDocumentInfo) // process attachment
         {
            DMSHelper.addAndSaveProcessAttachment(activityDetailsBean.getProcessInstance(), document);
         }
         else
         // typed document
         {
            typedDocumentInfo.getTypedDocument().setDocument(document);
            TypedDocumentsUtil.updateTypedDocument(typedDocumentInfo.getTypedDocument());
         }
         activityDetailsBean.refreshActivityPanelForSingleDocument();
      }
      // after version upload
      if (DocumentUploadEventType.VERSION_SAVED == eventType)
      {
         DMSHelper.updateProcessAttachment(activityDetailsBean.getProcessInstance(), document);
      }
   }
   
   public String getProcessAttachmentsFolderId()
   {
      return activityDetailsBean.getProcessAttachmentsFolderId();
   }

  /* public void createDocument()
   {
      activityDetailsBean.createDocument();
   }
   */
   public List<NoteTip> getNotes()
   {
      return activityDetailsBean.getDisplayNotes(); 
   }

   public boolean isProcessAttachmentPresent()
   {
      List<DocumentInfo> processAttachments = getProcessAttachments();
      if (processAttachments != null)
      {
         return !processAttachments.isEmpty();
      }
      return false;
   }

   public boolean isProcessDocumentPresent()
   {
      List<DocumentInfo> processDocuments = getProcessDocuments();
      if (processDocuments != null)
      {
         return !processDocuments.isEmpty();
      }
      return false;
   }
   
   /**
    * @return
    */
   public String getProcessAttachmentsIcon()
   {
      if (isSupportsProcessAttachments() && getProcessAttachments().size() > 0)
      {
         return ResourcePaths.I_PROCESS_ATTACHMENT_FILLED;
      }

      if (isSupportsProcessDocuments() && specificDocumentExist())
      {
         return ResourcePaths.I_PROCESS_ATTACHMENT_FILLED;
      }

      return ResourcePaths.I_PROCESS_ATTACHMENT_BLANK;
   }
   

   public String getSeparatorStyle()
   {
      if (isSupportsProcessAttachments() && getProcessAttachments().size() > 0 && isSupportsProcessDocuments())
      {
         return "border-bottom: 1px solid #CCCCCC;";
      }

      return "";
   }

   
   private boolean specificDocumentExist()
   {
      List<DocumentInfo> documents = getProcessDocuments();
      for (DocumentInfo documentInfo : documents)
      {
         if (StringUtils.isNotEmpty(documentInfo.getId()))
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * @return
    */
   public String getNotesIcon()
   {
      if (activityDetailsBean.getNotesCount() > 0)
      {
         return ResourcePaths.I_NOTES_FILLED;
      }
      else
      {
         return ResourcePaths.I_NOTES_BLANK;
      }
   }
   
   /**
    * Open the ProcessInstanceDetailView with the SwitchedFrom process
    */
   public void openProcessDetail()
   {
      linkedProcess.openProcessDetial();
      // Close the current Iframe
      activityDetailsBean.closeLinkedProcessIframePopup();
      activityDetailsBean.renderSession();

   }
   /**
    * 
    */
   public void toggleProcessAttachmentsIframePopup()
   {
      activityDetailsBean.toggleProcessAttachmentsIframePopup();
   }
   
   /**
    * 
    */
   public void toggleNotesIframePopup()
   {
      activityDetailsBean.toggleNotesIframePopup();
   }
   
   public void toggleSwitchProcessIframePopup()
   {
      activityDetailsBean.toggleSwitchProcessIframePopup();
   }

   /**
    * Method will find the linked process and open the IFrame
    */
   public void toggleLinkedProcessIframePopup()
   {
      if (!activityDetailsBean.isLinkedProcessPopupOpened())
      {
         ProcessInstance processInstance = activityDetailsBean.getProcessInstance();
         ProcessInstance fromProcessLink = ProcessInstanceUtils.getLinkInfo(processInstance, LinkDirection.TO,
               PredefinedProcessInstanceLinkTypes.SWITCH);
         ProcessInstance joinProcessLink = ProcessInstanceUtils.getLinkInfo(processInstance, LinkDirection.TO,
        		 PredefinedProcessInstanceLinkTypes.JOIN);
         linkedProcess = LinkedProcessBean.getCurrent();
         linkedProcess.setFromLinkedProcess(fromProcessLink);
         linkedProcess.setJoinLinkedProcess(joinProcessLink);
         if (null != fromProcessLink)
         {
            ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(
                  fromProcessLink.getModelOID(), fromProcessLink.getProcessID());
            linkedProcess.setFromProcessName(propsBean.getParamString("views.linkedProcess.processname",
                  I18nUtils.getProcessName(processDefinition), Long.valueOf(fromProcessLink.getOID()).toString()));
         }
         if (null != joinProcessLink)
         {
            ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(
                  joinProcessLink.getModelOID(), joinProcessLink.getProcessID());
            linkedProcess.setJoinProcessName(propsBean.getParamString("views.linkedProcess.processname",
                  I18nUtils.getProcessName(processDefinition), Long.valueOf(joinProcessLink.getOID()).toString()));
         }
      }
      activityDetailsBean.toggleLinkedProcessIframePopup();

   }
   
   /**
    * 
    * @return
    */
   public LinkedProcessBean getLinkedProcess()
   {
      return linkedProcess;
   }
   
   
}
