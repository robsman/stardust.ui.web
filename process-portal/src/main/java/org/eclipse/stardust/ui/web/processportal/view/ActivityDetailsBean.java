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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.ContextData;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.ui.client.common.ClientContext;
import org.eclipse.stardust.ui.common.form.jsf.DocumentInputController;
import org.eclipse.stardust.ui.common.form.jsf.JsfStructureContainer;
import org.eclipse.stardust.ui.common.form.preferences.FormGenerationPreferences;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.event.ActivityEventObserver;
import org.eclipse.stardust.ui.web.common.ToolbarSection;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.app.View.ViewState;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.message.MessageDialogHandler;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.common.util.SessionRendererHelper;
import org.eclipse.stardust.ui.web.processportal.EventController;
import org.eclipse.stardust.ui.web.processportal.PollingProperties;
import org.eclipse.stardust.ui.web.processportal.PollingProperties.Activator;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.common.PPUtils;
import org.eclipse.stardust.ui.web.processportal.common.PPUtils.CompletionOptions;
import org.eclipse.stardust.ui.web.processportal.common.WorkflowActivityCompletionLog;
import org.eclipse.stardust.ui.web.processportal.interaction.iframe.ExternalWebAppInteractionController;
import org.eclipse.stardust.ui.web.processportal.interaction.iframe.FaceletPanelInteractionController;
import org.eclipse.stardust.ui.web.processportal.interaction.iframe.JspPanelInteractionController;
import org.eclipse.stardust.ui.web.processportal.interaction.iframe.ManualActivityIframeInteractionController;
import org.eclipse.stardust.ui.web.processportal.launchpad.WorklistsBean;
import org.eclipse.stardust.ui.web.processportal.view.manual.DocumentInputEventHandler;
import org.eclipse.stardust.ui.web.processportal.view.manual.IppDocumentInputController;
import org.eclipse.stardust.ui.web.processportal.view.manual.ManualActivityForm;
import org.eclipse.stardust.ui.web.processportal.view.manual.ModelUtils;
import org.eclipse.stardust.ui.web.processportal.views.qualityassurance.QualityAssuranceActivityBean;
import org.eclipse.stardust.ui.web.processportal.views.qualityassurance.QualityAssuranceActivityBean.QAAction;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.NoteTip;
import org.eclipse.stardust.ui.web.viewscommon.common.activity.QualityAssuranceCodesBean;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalErrorClass;
import org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEvent;
import org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEvent.EventMode;
import org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEventObserver;
import org.eclipse.stardust.ui.web.viewscommon.common.event.IppEventController;
import org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEvent;
import org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEventObserver;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.AbortActivityBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.JoinProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.SwitchProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.JsfBackingBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.QualityAssuranceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpiUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;
import org.eclipse.stardust.ui.web.viewscommon.views.casemanagement.AttachToCaseDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.views.casemanagement.CreateCaseDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.TypedDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DocumentHandlerBean;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;
import org.springframework.beans.factory.DisposableBean;

import com.icesoft.faces.context.effects.JavascriptContext;

/**
 * @author roland.stamm
 * 
 */
public class ActivityDetailsBean extends UIComponentBean
      implements ActivityEventObserver, NoteEventObserver, DocumentEventObserver, ViewEventHandler, DisposableBean,
      Activator, MessageDialogHandler
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(ActivityDetailsBean.class);

   public static final String ACTIVITY_PANEL_VIEW_ID = "activityPanel";
   public static final String SESSION_RENDERER_PREFIX = "ActivityPanel-SR-";

   private static final String MAPPED_DOC_WARN_INCLUDE = "/plugins/processportal/toolbar/workflowMappedDocMsgDialogInclude.xhtml";

   public static enum WorkflowAction
   {
      COMPLETE,
      SUSPEND,
      SAVE,
      SUSPEND_TO_USER_WORKLIST,
      SAVE_TO_USER_WORKLIST,
      SUSPEND_TO_DEFAULT_PERFORMER,
      SAVE_TO_DEFAULT_PERFORMER
   }

   private String title;

   private ActivityInstance activityInstance;

   private ProcessInstance processInstance;

   private ProcessInstance scopeProcessInstance;

   private View thisView;

   private EventController eventController;
   private IppEventController ippEventController;

   private Interaction interaction;

   private ManualActivityForm activityForm;

   private boolean loadSuccessful;
   
   private String loadUnsuccessfulMsg;

   private boolean supportsWeb;

   private boolean supportsProcessAttachments;

   private List<Note> notes;

   private List<NoteTip> displayNotes;

   private List<Document> processAttachments;

   private List<DocumentInfo> displayProcessAttachments;

   private List<DocumentInfo> displayProcessDocuments;

   private String processAttachmentsFolderPath;
   
   private boolean autoOperationsPerformed;
   
   private boolean assemblyLineActivity;
   
   private boolean assemblyLinePushService;
   
   private boolean showAssemblyLinePushServiceControls;

   private WorklistsBean worklistsBean;

   private boolean skipViewEvents;
   
   private boolean processAttachmentsPopupOpened = false;
   private boolean notesPopupOpened = false;
   private boolean linkedProcessPopupOpened = false;
   private boolean switchProcessPopupOpened = false;
   private boolean suspendActivityPopupOpened = false;
   private boolean saveActivityPopupOpened = false;
   private boolean casePopupOpened = false;
   private boolean supportsProcessDocuments = false;
   private QualityAssuranceCodesBean qaCodeIframeBean;
   
   private boolean hasCreateCasePermission;
   private boolean hasJoinProcessPermission;
   private boolean hasSwitchProcessPermission;
   private boolean hasSpawnProcessPermission;
   private ConfirmationDialog mappedDocumentConfirmationDialog;

   private boolean singleDocumentCase;
   private View singleDocumentView;
   private DataMapping singleDocumentDatgaMapping;
   private DocumentHandlerBean documentHandlerBean;
   List<ToolbarSection> documentViewToolbars;
   
   private WorkflowAction selectedAction = WorkflowAction.SUSPEND;

   private boolean qualityAssuranceActionInProgress;
   private QAAction qualityAssuranceAction;
   
   // Kind of constant, loaded from  properties
   // Temporary to support both modes for some time
   private static boolean HTML_BASED;

   public static IActivityInteractionController getInteractionController(Activity activity)
   {
      IActivityInteractionController interactionController = ActivityInstanceUtils
            .getInteractionController(activity);

      if (SpiUtils.DEFAULT_JSF_ACTIVITY_CONTROLLER == interactionController)
      {
         interactionController = new FaceletPanelInteractionController();
      }
      else if (SpiUtils.DEFAULT_EXTERNAL_WEB_APP_CONTROLLER == interactionController)
      {
         interactionController = new ExternalWebAppInteractionController();
      }
      else if (SpiUtils.DEFAULT_JSP_ACTIVITY_CONTROLLER == interactionController)
      {
         interactionController = new JspPanelInteractionController();
      }
      else if (SpiUtils.DEFAULT_MANUAL_ACTIVITY_CONTROLLER == interactionController)
      {
         if (isHTMLBased(activity))
         {
            interactionController = new ManualActivityIframeInteractionController();
         }
      }

      return interactionController;
   }
   
   /**
    * @return
    */
   public static boolean isHTMLBased(Activity activity)
   {
      if (HTML_BASED && !isSingleDocumentCase(activity))
      {
         return true;
      }

      return false;
   }

   /**
    * @param activity
    * @return
    */
   @SuppressWarnings("unchecked")
   public static boolean isSingleDocumentCase(Activity activity)
   {
      List<Object> allMappings = activity.getApplicationContext("default").getAllDataMappings();
      Model model = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(activity.getModelOID());

      Set<String> docDataMappingIds = new HashSet<String>(); 
      for (Object obj : allMappings)
      {
         DataMapping dm = (DataMapping)obj;
         if (ModelUtils.isDocumentType(model, dm))
         {
            docDataMappingIds.add(dm.getId());
         }
         else
         {
            return false;
         }
      }

      return docDataMappingIds.size() == 1;
   }
   
   /**
     * 
     */
   public ActivityDetailsBean()
   {
      super("activityPanel");
      this.title = "";
      HTML_BASED = Parameters.instance().getBoolean("ManualActivity.Mode.HTML", true);
      trace.debug("Manual Activity HTML Mode = " + HTML_BASED);
   }

   /**
    * @return
    */
   public static ActivityDetailsBean getCurrentInstance()
   {
      return (ActivityDetailsBean) FacesUtils.getBeanFromContext("activityDetailsBean");

   }

   @Override
   public void initialize()
   {
      // NOP
   }

   public Interaction getInteraction()
   {
      return interaction;
   }

   public void destroy()
   {
      if (null != eventController)
      {
         eventController.unregisterObserver(this);
      }
      
      if (null != ippEventController)
      {
         ippEventController.unregisterObserver((NoteEventObserver)this);
         ippEventController.unregisterObserver((DocumentEventObserver)this);
      }
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         if (skipViewEvents)
         {
            return;
         }

         if (null != eventController)
         {
            eventController.registerObserver(this);
         }
         
         if (null != ippEventController)
         {
            ippEventController.registerObserver((NoteEventObserver)this);
            ippEventController.registerObserver((DocumentEventObserver)this);
         }

         // bind to activity instance
         Map<String, Object> viewParams = event.getView().getViewParams();
         setActivityInstance((ActivityInstance) viewParams.get(ActivityInstance.class
               .getName()));
         
         if(viewParams.get("assemblyLineActivity") != null)
         {
            assemblyLineActivity = (Boolean)viewParams.get("assemblyLineActivity");
            worklistsBean = (WorklistsBean) viewParams.get("worklistsBean");

            if (null == viewParams.get("pushService"))
            {
               assemblyLinePushService = Parameters.instance().getBoolean(ProcessPortalConstants.ASSEMBLY_LINE_PUSH_SERVICE, false);
            }
            else
            {
               assemblyLinePushService = (Boolean)viewParams.get("pushService");
            }

            showAssemblyLinePushServiceControls = assemblyLinePushService;
         }

         if (null == activityInstance)
         {
            Object aiOid = viewParams.get("oid");
            if (aiOid instanceof String)
            {
               aiOid = Long.parseLong((String) aiOid);
            }

            if (aiOid instanceof Number)
            {
               try
               {
                  setActivityInstance(ActivityInstanceUtils.getActivityInstance(((Number) aiOid)
                        .longValue()));
               }
               catch (ObjectNotFoundException onfe)
               {
                  ExceptionHandler.handleException(onfe,
                        MessagePropertiesBean.getInstance().getString("views.activityPanel.unknownActivity"));
               }
               if (null != activityInstance)
               {
                  // be sure the AI is properly activated by this user
                  setActivityInstance(ActivityInstanceUtils.activate(activityInstance));
               }
            }
         }

         if (null == activityInstance)
         {
            // no activity instance means there should be no view
            event.setVetoed(true);
            return;
         }

         this.thisView = event.getView();
         if (!viewParams.containsKey("activityName"))
         {
            viewParams.put("activityName", I18nUtils.getActivityName(activityInstance
                  .getActivity()));
         }

         // reset potentially stale interaction
         InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils
               .getManagedBean(FacesContext.getCurrentInstance(),
                     InteractionRegistry.BEAN_ID);
         if ((null != registry) && (null != activityInstance))
         {
            Interaction interaction = registry.getInteraction(Interaction
                  .getInteractionId(activityInstance));
            if (null != interaction)
            {
               registry.unregisterInteraction(interaction.getId());
            }
         }
         
         qaCodeIframeBean = new QualityAssuranceCodesBean(activityInstance);
         qaCodeIframeBean.initializeIframeDisplay();
         String iconPath = QualityAssuranceUtils.getIconfor(activityInstance);
         if (StringUtils.isNotEmpty(iconPath))
         {
            thisView.setIcon(iconPath);
            // FOR PANAMA
            PortalApplication.getInstance().updateViewIconClass(thisView);
         }
         
         hasCreateCasePermission = AuthorizationUtils.canCreateCase();
         hasJoinProcessPermission = AuthorizationUtils.hasAbortAndJoinProcessInstancePermission();
         hasSwitchProcessPermission = AuthorizationUtils.hasAbortAndStartProcessInstancePermission();
         hasSpawnProcessPermission = AuthorizationUtils.hasSpawnProcessPermission();
        
         fireEventForViewEventAwareInteractionController(activityInstance, event);
      }
      else if (ViewEventType.TO_BE_ACTIVATED == event.getType())
      {
         if (skipViewEvents)
         {
            return;
         }

         if (isCurrentActivityInstance(event.getView()))
         {
            boolean update = !loadSuccessful;
            if (ViewState.INACTIVE == event.getView().getViewState())
            {
               // do not activate when coming from created state, but activate if view get
               // back into focus
               trace.info("Activating activity: " + activityInstance);
               ActivityInstance ai = ActivityInstanceUtils.getActivityInstance(activityInstance
                     .getOID());
               
               // State is Application & Current Performer is Same
               if (ActivityInstanceState.Application == ai.getState()
                     && ai.getCurrentPerformer().getId().equals(activityInstance.getCurrentPerformer().getId()))
               {
                  // Valid
               }
               else // Activity may be manipulated externally
               {
                  update = false;
                  loadSuccessful = false;
                  
                  if (ai.getCurrentPerformer() != null)
                  {
                     loadUnsuccessfulMsg = MessagePropertiesBean.getInstance().getParamString(
                           "views.activityPanel.notValid1", ai.getState().toString(),
                           ai.getCurrentPerformer().toString());
                  }
                  else
                  {
                     loadUnsuccessfulMsg = MessagePropertiesBean.getInstance().getParamString(
                           "views.activityPanel.notValid2", ai.getState().toString());
                  }
               }
            }

            // refresh UI
            if (update)
            {
               update(true);
            }

            if (isLoadSuccessful())
            {
               fireEventForViewEventAwareInteractionController(activityInstance, event);
            }
         }
      }
      else if (ViewEventType.POST_OPEN_LIFECYCLE == event.getType())
      {
         if (skipViewEvents)
         {
            return;
         }

         if (isLoadSuccessful())
         {
            String description = I18nUtils.getDescriptionAsHtml(activityInstance.getActivity(), activityInstance
                  .getActivity().getDescription());
            if (StringUtils.isNotEmpty(description))
            {
               String tooltip = MessagesViewsCommonBean.getInstance().getParamString(
                     "views.processInstanceDetailsView.tooltip", thisView.getTooltip(), description);
               thisView.setTooltip(tooltip);
            }
            
            performAutoOperations(event.getView());
            
            if (isLoadSuccessful())
            {
               fireEventForViewEventAwareInteractionController(activityInstance, event);
            }
         }
      }
      else if (ViewEventType.TO_BE_DEACTIVATED == event.getType())
      {
         if (skipViewEvents)
         {
            return;
         }

         if (null != activityInstance)
         {
            fireEventForViewEventAwareInteractionController(activityInstance, event);
         }
      }
      else if (ViewEventType.TO_BE_CLOSED == event.getType())
      {
         if (skipViewEvents)
         {
            return;
         }

         if (null != activityInstance)
         {
            ActivityInstance ai = ActivityInstanceUtils.getActivityInstance(activityInstance.getOID());
            if (ai != null)
            {
               if (isCurrentActivityInstance(event.getView())
                     && (ActivityInstanceState.Application == ai.getState()))
               {
                  suspendCurrentActivity(false, false, true);
               }
            }
         }
      }
      else if (ViewEventType.CLOSED == event.getType())
      {
         if (null != activityInstance)
         {
            if (!skipViewEvents)
            {
               if (ViewState.ACTIVE == event.getView().getViewState()
                     && ActivityInstanceState.Application == activityInstance.getState())
               {
                  suspendCurrentActivity(true, false, false);
               }
            }

            undoAutoOperations(event.getView());

            fireEventForViewEventAwareInteractionController(activityInstance, event);
         }

         if (null != eventController)
         {
            eventController.unregisterObserver(this);
         }

         if (null != ippEventController)
         {
            ippEventController.unregisterObserver((NoteEventObserver)this);
            ippEventController.unregisterObserver((DocumentEventObserver)this);
         }

         ManualActivityForm currentActivityForm = getCurrentManualActivityForm();
         if(null != currentActivityForm)
         {
            currentActivityForm.destroy(selectedAction.name());
         }
      }
      else if (ViewEventType.TO_BE_FULL_SCREENED == event.getType())
      {
         if (isCurrentActivityInstance(event.getView())
               && (ViewState.ACTIVE == event.getView().getViewState())
               && (ActivityInstanceState.Application == activityInstance.getState()))
         {
            // veto if panel uses iframe, as such a request needs to be processed two
            // phased and is only safely handled from the toolbar for the time being
         }
      }
      else if (ViewEventType.LAUNCH_PANELS_ACTIVATED == event.getType()
            || ViewEventType.LAUNCH_PANELS_DEACTIVATED == event.getType()
            || ViewEventType.FULL_SCREENED == event.getType()
            || ViewEventType.RESTORED_TO_NORMAL == event.getType()
            || ViewEventType.PINNED == event.getType()
            || ViewEventType.PERSPECTIVE_CHANGED == event.getType())
      {
         fireEventForViewEventAwareInteractionController(activityInstance, event);
      }

      if (singleDocumentCase && isLoadSuccessful())
      {
         documentHandlerBean.handleEvent(new ViewEvent(singleDocumentView, event.getType()));
         boolean docViewLoadSuccessful = documentHandlerBean.isLoadSuccessful();
         if (!docViewLoadSuccessful)
         {
            update(false);
         }
      }
      
      handleIframePopups(event);

      // Propagate Event to JSF Bean
      if (isLoadSuccessful())
      {
         try
         {
            Object beanObject = getBackingBeanForJsfActivity();
            if (null != beanObject && beanObject instanceof ViewEventHandler)
            {
               ReflectionUtils.invokeMethod(beanObject, "handleEvent", new Object[]{event});
            }
         }
         catch (Exception e)
         {
            trace.error("Could not propagate ViewEvent to JSF Backing Bean", e);
         }
      }
   }

   /**
    * @param event
    */
   private void handleIframePopups(ViewEvent event)
   {
      switch (event.getType())
      {
      case CREATED:
         break;
      case DEACTIVATED:
      case FULL_SCREENED:
      case RESTORED_TO_NORMAL:
      case PINNED:
      case LAUNCH_PANELS_ACTIVATED:
      case LAUNCH_PANELS_DEACTIVATED:
         closeProcessAttachmentsIframePopup();
         closeNotesIframePopup();
         closeSwitchProcessIframePopup();
         closeLinkedProcessIframePopup();
         closeCaseIframePopup();
         qaCodeIframeBean.closeQualityAssuranceCodesIframePopup();
         closeSuspendActivityIframePopup();
         closeSaveActivityIframePopup();
         break;
      case CLOSED:
         closeProcessAttachmentsIframePopup();
         closeNotesIframePopup();
         closeSwitchProcessIframePopup();
         closeLinkedProcessIframePopup();
         closeCaseIframePopup();
         qaCodeIframeBean.closeQualityAssuranceCodesIframePopup();
         closeSuspendActivityIframePopup();
         closeSaveActivityIframePopup();
         break;
      }
   }

   /**
    * @param view
    */
   private void performAutoOperations(View view)
   {
      if(!autoOperationsPerformed)
      {
         skipViewEvents = true;
         PortalApplication portalApplication = PortalApplication.getInstance();

         List<View> autoOpenViews = new ArrayList<View>();

         if (ActivityPanelConfigurationBean.isAutoCloseRelatedViews())
         {            
            view.getDefinition().setClosingPolicy(ViewDefinition.CLOSING_POLICY_RECURSIVE);
         }
         else
         {
            view.getDefinition().setClosingPolicy(ViewDefinition.CLOSING_POLICY_NONE);
         }

         // Every time when new view is to be opened the current focus view should be this view

         if (ActivityPanelConfigurationBean.isAutoDisplayDocuments())
         {
            autoOpenViews.addAll(openProcessAttachments(view));
         }

         if (ActivityPanelConfigurationBean.isAutoDisplayMappedDocuments())
         {
            if(null != activityForm && !singleDocumentCase)
            {
               List<DocumentInputController> mappedDocs = activityForm.getDisplayedMappedDocuments(false, true);
               for (DocumentInputController docInputCtrl : mappedDocs)
               {
                  PortalApplication.getInstance().setFocusView(thisView);
                  docInputCtrl.openDocument();
               }
            }
         }

         if (ActivityPanelConfigurationBean.isAutoDisplayNotes())
         {
            PortalApplication.getInstance().setFocusView(view);
            autoOpenViews.add(openNotesView(null));
         }

         if (ActivityPanelConfigurationBean.isAutoDisplayProcessDetails())
         {
            PortalApplication.getInstance().setFocusView(view);
            autoOpenViews.add(ProcessInstanceUtils.openProcessContextExplorer(processInstance));
         }

         if (ActivityPanelConfigurationBean.isAutoMinimizeLaunchPanels())
         {
            PortalApplication.getInstance().deactivateLaunchPanels();
         }

         if (ActivityPanelConfigurationBean.isAutoMaximizeView())
         {
            PortalApplication.getInstance().activateFullScreenMode();
         }

         // At the end the focus view should be this view
         PortalApplication.getInstance().setFocusView(view);
         // Auto - operations(Notes, documents) change the currentView icon,label on UI,
         // reset for AI active view
         PortalApplication.getInstance().updateViewIconClass(view);
         PortalApplication.getInstance().updateViewTitle(view);
         
         /*
          * If any message pop-up is set to be opened up, then fire a de-activate view
          * event. This is needed for IFRAME based activities (JSF activities, external
          * web applications, etc) in particular, to avoid activation of IFRAMES - which
          * would otherwise overlay the modal pop-up, rendering the portal unusable.
          */
         if (MessageDialog.getInstance().isVisible())
         {
            fireEventForViewEventAwareInteractionController(activityInstance, new ViewEvent(thisView,
                  ViewEvent.ViewEventType.TO_BE_DEACTIVATED));
         }
         
         // Check if UI was already pinned. This case will arise when user completes the
         // activity in pin mode and next activity is also assigned to the same user
         boolean alreadyPinned = false;
         String pinViewMode = view.getParamValue("pinViewMode");
         if (StringUtils.isNotEmpty(pinViewMode))
         {
            alreadyPinned = true;
            if (PortalApplication.PIN_VIEW_MODE_VERTICAL.equals(pinViewMode))
            {
               portalApplication.openPinViewVertical();
            }
            else
            {
               portalApplication.openPinViewHorizontal();
            }
         }

         // Check if more Views are open. If Yes then try to Pin the View
         if (!alreadyPinned && 1 < portalApplication.getOpenViewsSize())
         {
            if (ActivityPanelConfigurationBean.isAutoPinActivityView())
            {
               if (ActivityPanelConfigurationBean.PIN_ACTIVITY_VIEW_VERTICALLY.equals(ActivityPanelConfigurationBean
                     .isAutoPinActivityViewType()))
               {
                  portalApplication.openPinViewVertical();
               }
               else
               {
                  portalApplication.openPinViewHorizontal();
               }
            }
         }
         
         // Lookahead Functionality Part II
         for (View autoOpenView : autoOpenViews)
         {
            if (null != autoOpenView && null != autoOpenView.getOpenerView())
            {
               // This this views opener is Activity View then change opener to current view
               // This check prevents changing opener if view is opened from some other view
               // E.g. Process Details opened from Worklist
               if (ACTIVITY_PANEL_VIEW_ID.equals(autoOpenView.getOpenerView().getDefinition().getName()))
               {
                  autoOpenView.setOpenerView(view);
               }
            }
         }

         autoOperationsPerformed = true;
         skipViewEvents = false;
      }
   }

   /**
    * @param view
    */
   private void undoAutoOperations(View view)
   {
      if (ActivityPanelConfigurationBean.isAutoCloseRelatedViews())
      {
         PortalApplication.getInstance().activateLaunchPanels();
         PortalApplication.getInstance().deactivateFullScreenMode();
      }

      // Close the document, because it belongs to activity data and needs to close with activity
      // This is irrespective of "close related views flag"
      ManualActivityForm currentActivityForm = getCurrentManualActivityForm();
      if(null != currentActivityForm)
      {
         List<DocumentInputController> mappedDocs = currentActivityForm.getDisplayedMappedDocuments(true, true);
         for (DocumentInputController docInputCtrl : mappedDocs)
         {
            docInputCtrl.closeDocument();
         }
      }
   }

   /**
    * @return
    */
   private ManualActivityForm getCurrentManualActivityForm()
   {
      if(null != activityForm)
      {
         return activityForm;
      }
      else // Try for JSF Activity
      {
         try
         {
            Object beanObject = getBackingBeanForJsfActivity();
            if (null != beanObject && null != ReflectionUtils.getMethod(beanObject, "getActivityForm", (Object[]) null))
            {
               return (ManualActivityForm)ReflectionUtils.invokeMethod(beanObject, "getActivityForm");
            }
         }
         catch (Exception e)
         {
            trace.error("Error in working with JSF Backing Bean", e);
         }
      }

      return null;
   }

   /**
    * @return
    */
   private Object getBackingBeanForJsfActivity()
   {
      Activity activity = activityInstance.getActivity();
      IActivityInteractionController interactionController = getInteractionController(activity);
      if (null != interactionController)
      {
         String contextId = interactionController.getContextId(activityInstance);
         if (PredefinedConstants.JSF_CONTEXT.equals(contextId))
         {
            ApplicationContext applicationContext = activity.getApplicationContext(PredefinedConstants.JSF_CONTEXT);
            if (null != applicationContext)
            {
               return JsfBackingBeanUtils.getBackingBean(applicationContext);
            }
         }
      }
      return null;
   }

   /**
    * @return
    */
   private boolean isJSFActivity()
   {
      Activity activity = activityInstance.getActivity();
      IActivityInteractionController interactionController = getInteractionController(activity);
      if (null != interactionController)
      {
         String contextId = interactionController.getContextId(activityInstance);
         if (PredefinedConstants.JSF_CONTEXT.equals(contextId))
         {
            return true;
         }
      }
      
      return false;
   }

   /**
    * @return
    */
   private void renderJsfActivitySession()
   {
      try
      {
         Object beanObject = getBackingBeanForJsfActivity();
         if (null != beanObject)
         {
            String sessionRendererId = (String)ReflectionUtils.invokeMethod(beanObject, "getSessionRendererId");
            if (StringUtils.isNotEmpty(sessionRendererId))
            {
               SessionRendererHelper.render(sessionRendererId);
            }
         }
      }
      catch (Exception e)
      {
         trace.error("Error in rendering JSF Activity session", e);
      }
   }

   /**
    * @param thisView
    * @return
    */
   private List<View> openProcessAttachments(View thisView)
   {
      List<View> docViews = new ArrayList<View>();

      if (!CollectionUtils.isEmpty(processAttachments))
      {
         String docDisplayType = ActivityPanelConfigurationBean.getAutoDisplayDocumentsType();

         Map<String, Object> params = CollectionUtils.newMap();
         params.put("processInstance", processInstance);

         if (ActivityPanelConfigurationBean.DISPLAY_DOCUMENTS_OLDEST.equals(docDisplayType))
         {
            params.put("displayMode", ActivityPanelConfigurationBean.getAutoDocumentsDisplayMode());

            PortalApplication.getInstance().setFocusView(thisView);
            docViews.add(DocumentViewUtil.openJCRDocument(processAttachments.get(0).getId(), params));
         }
         else if (ActivityPanelConfigurationBean.DISPLAY_DOCUMENTS_ALL.equals(docDisplayType))
         {
            int attachmentsCount = processAttachments.size();
            for (int i = 0; i < attachmentsCount - 1; i++)
            {
               PortalApplication.getInstance().setFocusView(thisView);
               docViews.add(DocumentViewUtil.openJCRDocument(processAttachments.get(i), params));
            }

            params.put("displayMode", ActivityPanelConfigurationBean.getAutoDocumentsDisplayMode());

            PortalApplication.getInstance().setFocusView(thisView);
            docViews.add(DocumentViewUtil.openJCRDocument(processAttachments.get(attachmentsCount - 1), params));
         }
      }
      
      return docViews;
   }


   /**
    * @param activityInstance
    * @param event
    */
   private void fireEventForViewEventAwareInteractionController(
         ActivityInstance activityInstance, ViewEvent event)
   {
      IActivityInteractionController interactionController = getInteractionController(activityInstance
            .getActivity());
      if (interactionController instanceof ViewEventAwareInteractionController)
      {
         // Some actions work with handleEvent and some with addEventScript
         // Hence for now do both!

         ViewEventAwareInteractionController veaic = ((ViewEventAwareInteractionController) interactionController);
         //veaic.handleEvent(activityInstance, event);
         PortalApplication.getInstance().addEventScript(
               veaic.getEventScript(activityInstance, event));
      }
   }

   private boolean isCurrentActivityInstance(View view)
   {
      Object aiOid = view.getViewParams().get("oid");

      if (aiOid instanceof String)
      {
         aiOid = Long.parseLong((String) aiOid);
      }

      return (aiOid instanceof Long)
            && isCurrentActivityInstance(((Long) aiOid).longValue());
   }

   private boolean isCurrentActivityInstance(long aiOid)
   {
      return (null != activityInstance) && (aiOid == activityInstance.getOID());
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.event.ActivityEventObserver#handleEvent(org.eclipse.stardust.ui.event.ActivityEvent)
    */
   public void handleEvent(ActivityEvent event)
   {
      if ((null != event.getActivityInstance())
            && isCurrentActivityInstance(event.getActivityInstance().getOID()))
      {
         if (event.getType().equals(ActivityEvent.ACTIVATED))
         {
            update(false);
         }
         else if (event.getType().equals(ActivityEvent.COMPLETED)
               || event.getType().equals(ActivityEvent.SUSPENDED)
               || event.getType().equals(ActivityEvent.ABORTED))
         {
            // in current scenario the ActivityPanel is closed on
            // (complete,suspend,abort), so this will never be visible.
            // If the panel was not closed however, this code would update the bean to
            // reflect the state of the Activity.
            clear();
            setTitle("ActivityInstance (OID=" + event.getActivityInstance().getOID() + ") " + event.getType().getName()); //$NON-NLS-N$
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEventObserver#handleEvent(org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEvent)
    */
   public void handleEvent(NoteEvent event)
   {
      if (scopeProcessInstance.getOID() == event.getScopeProcessInstanceOid())
      {
         refreshNotes(event.getAllNotes());
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEventObserver#handleEvent(org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEvent)
    */
   public void handleEvent(DocumentEvent documentEvent)
   {
      if (EventMode.PROCESS_ATTACHMENTS == documentEvent.getEventMode()
            && processInstance.getOID() == documentEvent.getProcessInstanceOid())
      {
         // For Any EventType - Refresh Full List
         // Using list from event does not work refreshProcessAttachments(event.getAllProcessAttachments());
         // So load from service call
         fetchProcessAttachments();
         
      }
      if (EventMode.PROCESS_DOCUMENTS.equals(documentEvent.getEventMode())
            && processInstance.getOID() == documentEvent.getProcessInstanceOid())
      {
         fetchProcessDocuments();

         // Work with Document Data Mappings
         ManualActivityForm currentActivityForm = getCurrentManualActivityForm();
         if(null != currentActivityForm)
         {
            List<DocumentInputController> docs = currentActivityForm.getDisplayedMappedDocuments(false, false);
            for (DocumentInputController documentInputController : docs)
            {
               if (documentInputController instanceof IppDocumentInputController)
               {
                  IppDocumentInputController ippDocumentInputController = (IppDocumentInputController) documentInputController;
                  if (documentEvent.getDataId().equals(ippDocumentInputController.getDataMapping().getDataId()))
                  {
                     ippDocumentInputController.setValue(documentEvent.getCurrentDocument());
                     if (ippDocumentInputController.isDocumentViewerOpened())
                     {
                        ippDocumentInputController.closeDocument();
                     }
                     break;
                  }
               }
            }

            renderJsfActivitySession();
         }
      }
   }

   /**
    * @param event
    */
   public void openNotes(ActionEvent event)
   {
      Map<String, Object> params = CollectionUtils.newTreeMap();
      Date noteTimestamp = (Date) event.getComponent().getAttributes().get("noteTimestamp");
      Integer noteIndex = (Integer) event.getComponent().getAttributes().get("noteIndex");
      params.put("noteTimestamp", noteTimestamp);
      params.put("noteIndex", noteIndex);
      openNotesView(params);
      closeNotesIframePopupSelf();
   }

   /**
    * @param params
    * @return
    */
   public View openNotesView(Map<String, Object> params)
   {
      if (null != activityInstance)
      {
         return ProcessInstanceUtils.openNotes(activityInstance.getProcessInstance(), params);
      }
      return null;
   }

   /**
    * @param event
    */
   public void openDelegateDialog(ActionEvent event)
   {
      if (null != activityInstance)
      {
         ActivityInstance ai = activityInstance;
         IActivityInteractionController interactionController = getInteractionController(ai
               .getActivity());

         retrieveOutDataMapping(interactionController, false, new ParametricCallbackHandler()
         {
            @SuppressWarnings({"unchecked", "rawtypes"})
            public void handleEvent(EventType eventType)
            {
               if (EventType.APPLY == eventType)
               {
                  openDelegateDialogContinue((Map)getParameters());
               }
            }
         });
      }
   }

   /**
    * @param outData
    */
   private void openDelegateDialogContinue(Map<String, Serializable>outData)
   {
      ActivityInstance ai = activityInstance;
      IActivityInteractionController interactionController = getInteractionController(ai.getActivity());

      String context = interactionController.getContextId(ai);

      ActivityInstanceUtils.openDelegateDialog(activityInstance, outData, context, new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType.equals(EventType.APPLY))
            {
               ActivityDetailsBean.this.interaction = null;
               skipViewEvents = true;
               PortalApplication.getInstance().closeView(thisView, false);
               // When view close is auto-operation, sync view is required to update focus view
               PortalApplication.getInstance().addEventScript("parent.BridgeUtils.View.syncActiveView();");
               skipViewEvents = false;
            }
            else if (eventType.equals(EventType.CANCEL))
            {
               ActivityInstance ai = ActivityInstanceUtils.getActivityInstance(activityInstance.getOID());
               if (ai != null && ActivityInstanceState.Application != ai.getState())
               {
                  setActivityInstance(ActivityInstanceUtils.activate(ai));
                  update(false);
               }
            }
         }
      });
   }

   /**
    * @param event
    */
   public void abortAction(ActionEvent event)
   {
      abortCurrentActivity();
   }

   /**
    * 
    */
   public void abortCurrentActivity()
   {
      if (activityInstance != null)
      {
         AbortActivityBean abortActivity = AbortActivityBean.getInstance();
         abortActivity.setCallbackHandler(new ICallbackHandler()
         {
            public void handleEvent(EventType eventType)
            {
               if (eventType.equals(EventType.APPLY))
               {
                  skipViewEvents = true;
                  PortalApplication.getInstance().closeView(thisView, true);
                  skipViewEvents = false;
               }
            }
         });
         abortActivity.abortActivity(activityInstance);
      }
   }

   /**
    * @param event
    */
   public void resubmitAction(ActionEvent event)
   {
      resubmitCurrentActivity();
   }

   /**
    * 
    */
   private void resubmitCurrentActivity()
   {
      if (activityInstance != null)
      {
         ResubmissionBean resubmissionBean = ResubmissionBean.getCurrent();
         resubmissionBean.setActivityInstance(activityInstance);
         resubmissionBean.setCallbackHandler(new ICallbackHandler()
         {
            public void handleEvent(EventType eventType)
            {
               if (eventType == EventType.APPLY)
               {
                  skipViewEvents = true;
                  PortalApplication.getInstance().closeView(thisView, true);
                  skipViewEvents = false;
               }
            }
         });
         resubmissionBean.openPopup();
      }
   }

   /**
    * @param event
    */
   public void suspendAction(ActionEvent event)
   {
      processActivityInstance(WorkflowAction.SUSPEND);
   }

   /**
    * @param event
    */
   public void suspendToUserWorklistAction(ActionEvent event)
   {
      closeSuspendActivityIframePopup();
      processActivityInstance(WorkflowAction.SUSPEND_TO_USER_WORKLIST);
   }

   /**
    * @param event
    */
   public void suspendToDefaultPerformerAction(ActionEvent event)
   {
      closeSuspendActivityIframePopup();
      processActivityInstance(WorkflowAction.SUSPEND_TO_DEFAULT_PERFORMER);
   }

   /**
    * 
    */
   public void suspendCurrentActivity()
   {
      suspendCurrentActivity(false, true, true);
   }

   /**
    * @param event
    */
   public void saveAction(ActionEvent event)
   {
      showMappedDocumentWarningAndProcessActivity(WorkflowAction.SAVE);
   }

   /**
    * @param event
    */
   public void saveToUserWorklistAction(ActionEvent event)
   {
      closeSaveActivityIframePopup();
      showMappedDocumentWarningAndProcessActivity(WorkflowAction.SAVE_TO_USER_WORKLIST);
      renderSession();
   }

   /**
    * @param event
    */
   public void saveToDefaultPerformerAction(ActionEvent event)
   {
      closeSaveActivityIframePopup();
      showMappedDocumentWarningAndProcessActivity(WorkflowAction.SAVE_TO_DEFAULT_PERFORMER);
      renderSession();
   }

   public void suspendAndSaveCurrentActivity()
   {
      suspendAndSaveCurrentActivity(false, true, true);
   }

   public void suspendCurrentActivity(boolean keepOwnership, boolean closeView,
         boolean suspendToParticipant)
   {
      if (null != activityInstance)
      {
         ActivityInstance ai = activityInstance;

         IActivityInteractionController interactionController = getInteractionController(ai
               .getActivity());
         if (null != interactionController)
         {
            if (interactionController.closePanel(ai, ClosePanelScenario.SUSPEND))
            {
               // close synchronously
               if (keepOwnership)
               {
                  ActivityInstance newAi = ActivityInstanceUtils.suspendToUserWorklist(ai, null, null);
                  if (!closeView) // No need to set the AI if view is closing
                  {
                     setActivityInstance(newAi);
                  }
               }
               else if (suspendToParticipant)
               {
                  this.activityInstance = ActivityInstanceUtils.suspend(ai, null);
               }
               else
               {
                  ActivityInstance newAi = ActivityInstanceUtils.suspendToDefaultPerformer(ai, null,
                        null);
                  if (!closeView) // No need to set the AI if view is closing
                  {
                     setActivityInstance(newAi);
                  }
               }

               if (closeView)
               {
                  skipViewEvents = true;
                  // TODO move to controller?
                  PortalApplication.getInstance().closeView(thisView, true);
                  // When view close is auto-operation, sync view is required to update focus view
                  PortalApplication.getInstance().addEventScript("parent.BridgeUtils.View.syncActiveView();");
                  skipViewEvents = false;
               }
            }
         }
         else
         {
            throw new PublicException(
                  ProcessPortalErrorClass.UNKNOWN_APP_CONTEXT_FOR_METHOD_INVOCATION);
         }
      }
   }

   public void suspendAndSaveCurrentActivity(final boolean keepOwnership, final boolean closeView,
         final boolean suspendToParticipant)
   {
      if (null != activityInstance)
      {
         ActivityInstance ai = activityInstance;

         IActivityInteractionController interactionController = getInteractionController(ai
               .getActivity());
         if (null != interactionController)
         {
            if (interactionController.closePanel(ai, ClosePanelScenario.SUSPEND_AND_SAVE))
            {
               // close synchronously
               retrieveOutDataMapping(interactionController, true, new ParametricCallbackHandler()
               {
                  @SuppressWarnings({"unchecked", "rawtypes"})
                  public void handleEvent(EventType eventType)
                  {
                     if (EventType.APPLY == eventType)
                     {
                        suspendAndSaveCurrentActivityContinue(keepOwnership, closeView, suspendToParticipant,
                              (Map) getParameters());
                     }
                  }
               });
            }
         }
         else
         {
            throw new PublicException(
                  ProcessPortalErrorClass.UNKNOWN_APP_CONTEXT_FOR_METHOD_INVOCATION);
         }
      }
   }

   /**
    * @param keepOwnership
    * @param closeView
    * @param suspendToParticipant
    * @param outData
    */
   private void suspendAndSaveCurrentActivityContinue(boolean keepOwnership, boolean closeView,
         boolean suspendToParticipant, Map<String, Serializable> outData)
   {
      ActivityInstance ai = activityInstance;
      IActivityInteractionController interactionController = getInteractionController(ai.getActivity());

      if (keepOwnership)
      {
         ActivityInstance newAi = ActivityInstanceUtils.suspendToUserWorklist(ai,
               interactionController.getContextId(ai), outData);
         if (!closeView) // No need to set the AI if view is closing
         {
            setActivityInstance(newAi);
         }
      }
      else if (suspendToParticipant)
      {
         this.activityInstance = ActivityInstanceUtils.suspend(ai, new ContextData(
               interactionController.getContextId(ai), outData));
      }
      else
      {
         ActivityInstance newAi = ActivityInstanceUtils.suspendToDefaultPerformer(ai,
               interactionController.getContextId(ai), outData);
         if (!closeView) // No need to set the AI if view is closing
         {
            setActivityInstance(newAi);
         }
      }

      if (closeView)
      {
         skipViewEvents = true;
         // TODO move to controller?
         PortalApplication.getInstance().closeView(thisView, true);
         PortalApplication.getInstance().addEventScript("parent.BridgeUtils.View.syncActiveView();");
         skipViewEvents = false;
      }
   }

   public void completeQualityAssurancePass()
   {
      completeQualityAssuranceActivity(QAAction.PASS);
   }

   public void completeQualityAssuranceFail()
   {
      completeQualityAssuranceActivity(QAAction.FAIL);
   }

   private void completeQualityAssuranceActivity(final QAAction action)
   {
      ActivityInstance ai = activityInstance;
      IActivityInteractionController interactionController = getInteractionController(ai
            .getActivity());

      if (null != interactionController)
      {
         qualityAssuranceActionInProgress = true;
         qualityAssuranceAction = action;
         if (interactionController.closePanel(ai, ClosePanelScenario.COMPLETE))
         {
            qualityAssuranceActionInProgress = false;
            qualityAssuranceAction = null;
            retrieveOutDataMapping(interactionController, false, new ParametricCallbackHandler()
            {
               public void handleEvent(EventType eventType)
               {
                  if (EventType.APPLY == eventType)
                  {
                     QualityAssuranceActivityBean.openDialog(action, getActivityInstance(), thisView, getParameters());
                  }
               }
            });
         }
      }
   }
   
   public boolean isSaveActivityPopupOpened()
   {
      return saveActivityPopupOpened;
   }

   /**
    *
    */
   public void toggleSaveActivityIframePopup()
   {
      if (saveActivityPopupOpened)
      {
         closeSaveActivityIframePopup();
      }
      else
      {
         if (isProcessAttachmentsPopupOpened())
         {
            closeProcessAttachmentsIframePopup();
         }
         if (isNotesPopupOpened())
         {
            closeNotesIframePopup();
         }
         if (isLinkedProcessPopupOpened())
         {
            closeLinkedProcessIframePopup();
         }
         if (isSwitchProcessPopupOpened())
         {
            closeSwitchProcessIframePopup();
         }
         if(isCasePopupOpened())
         {
            closeCaseIframePopup();
         }
         if (qaCodeIframeBean.isQualityAssuranceCodesPopupOpened())
         {
            qaCodeIframeBean.closeQualityAssuranceCodesIframePopup();
         }
         if (isSuspendActivityPopupOpened())
         {
            closeSuspendActivityIframePopup();
         }
         openSaveActivityIframePopup();
      }
   }

   /**
    * closes the popup from portal application like completing activity
    */
   public void closeSaveActivityIframePopup()
   {
      if (saveActivityPopupOpened)
      {
         String iFrameId = getSaveActivityIframePopupId();
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";

         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

         saveActivityPopupOpened = false;
      }
   }

   /**
    *
    */
   public void openSaveActivityIframePopup()
   {
      String iFrameId = getSaveActivityIframePopupId();
      String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
      + "/plugins/processportal/toolbar/suspendAndSaveIframePopup.iface?random=" + System.currentTimeMillis() + "'";

      String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + iFrameId + ", " + url + ", "
            + getSwitchProcessIframePopupArgs() + ");";
      PortalApplication.getInstance().addEventScript(script);
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

      saveActivityPopupOpened = true;
   }

   /**
    * @return
    */
   public String getSaveActivityIframePopupArgs()
   {
      String advanceArgs =
         "{anchorId:'ippSaveActivityAnchor', width:100, height:30, maxWidth:500, maxHeight:550, " +
         "openOnRight:false, anchorXAdjustment:10, anchorYAdjustment:5, zIndex:200, border:'1px solid black', noUnloadWarning: 'true'}";
      return advanceArgs;
   }

   /**
    * @return
    */
   public String getSaveActivityIframePopupId()
   {
      try
      {
         String iFrameId = "'SAS" + getActivityInstance().getOID() + "'";
         return iFrameId;
      }
      catch (Exception e)
      {
         return "''"; // Consume Exception
      }
   }

   public boolean isSuspendActivityPopupOpened()
   {
      return suspendActivityPopupOpened;
   }

   /**
    *
    */
   public void toggleSuspendActivityIframePopup()
   {
      if (suspendActivityPopupOpened)
      {
         closeSuspendActivityIframePopup();
      }
      else
      {
         if (isProcessAttachmentsPopupOpened())
         {
            closeProcessAttachmentsIframePopup();
         }
         if (isNotesPopupOpened())
         {
            closeNotesIframePopup();
         }
         if (isLinkedProcessPopupOpened())
         {
            closeLinkedProcessIframePopup();
         }
         if (isSwitchProcessPopupOpened())
         {
            closeSwitchProcessIframePopup();
         }
         if(isCasePopupOpened())
         {
            closeCaseIframePopup();
         }
         if (qaCodeIframeBean.isQualityAssuranceCodesPopupOpened())
         {
            qaCodeIframeBean.closeQualityAssuranceCodesIframePopup();
         }
         if (isSaveActivityPopupOpened())
         {
            closeSaveActivityIframePopup();
         }
         openSuspendActivityIframePopup();
      }
   }

   /**
    * closes the popup from portal application like completing activity
    */
   public void closeSuspendActivityIframePopup()
   {
      if (suspendActivityPopupOpened)
      {
         String iFrameId = getSuspendActivityIframePopupId();
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";

         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

         suspendActivityPopupOpened = false;
      }
   }

   /**
    *
    */
   public void openSuspendActivityIframePopup()
   {
      String iFrameId = getSuspendActivityIframePopupId();
      String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
      + "/plugins/processportal/toolbar/suspendActivityIframePopup.iface?random=" + System.currentTimeMillis() + "'";

      String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + iFrameId + ", " + url + ", "
            + getSwitchProcessIframePopupArgs() + ");";
      PortalApplication.getInstance().addEventScript(script);
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

      suspendActivityPopupOpened = true;
   }

   /**
    * @return
    */
   public String getSuspendActivityIframePopupArgs()
   {
      String advanceArgs =
         "{anchorId:'ippSuspendAnchor', width:100, height:30, maxWidth:500, maxHeight:550, " +
         "openOnRight:false, anchorXAdjustment:10, anchorYAdjustment:5, zIndex:200, border:'1px solid black', noUnloadWarning: 'true'}";
      return advanceArgs;
   }

   /**
    * @return
    */
   public String getSuspendActivityIframePopupId()
   {
      try
      {
         String iFrameId = "'SA" + getActivityInstance().getOID() + "'";
         return iFrameId;
      }
      catch (Exception e)
      {
         return "''"; // Consume Exception
      }
   }

   /**
    * 
    */
   public void toggleQualityAssuranceCodesIframePopup()
   {
      if (qaCodeIframeBean.isQualityAssuranceCodesPopupOpened())
      {
         qaCodeIframeBean.closeQualityAssuranceCodesIframePopup();
      }
      else
      {
         if (isProcessAttachmentsPopupOpened())
         {
            closeProcessAttachmentsIframePopup();
         }
         if (isNotesPopupOpened())
         {
            closeNotesIframePopup();
         }
         if (isLinkedProcessPopupOpened())
         {
            closeLinkedProcessIframePopup();
         }
         if (isSwitchProcessPopupOpened())
         {
            closeSwitchProcessIframePopup();
         }
         if(isCasePopupOpened())
         {
            closeCaseIframePopup();
         }
         if (isSuspendActivityPopupOpened())
         {
            closeSuspendActivityIframePopup();
         }
         if (isSaveActivityPopupOpened())
         {
            closeSaveActivityIframePopup();
         }
         qaCodeIframeBean.openQualityAssuranceCodesIframePopup();
      }
   }

   /**
    * @param event
    */
   public void completeAction(ActionEvent event)
   {
      showMappedDocumentWarningAndProcessActivity(WorkflowAction.COMPLETE);
   }

   public void completeCurrentActivity()
   {
      if (activityInstance != null)
      {
         ActivityInstance ai = activityInstance;

         IActivityInteractionController interactionController = getInteractionController(ai
               .getActivity());
         if (null != interactionController)
         {
            if (interactionController.closePanel(ai, ClosePanelScenario.COMPLETE))
            {
               // close synchronously
               retrieveOutDataMapping(interactionController, false, new ParametricCallbackHandler()
               {
                  @SuppressWarnings({"rawtypes", "unchecked"})
                  public void handleEvent(EventType eventType)
                  {
                     if (EventType.APPLY == eventType)
                     {
                        if (qualityAssuranceActionInProgress)
                        {
                           qualityAssuranceActionInProgress = false;
                           QualityAssuranceActivityBean.openDialog(qualityAssuranceAction, getActivityInstance(), thisView, getParameters());
                           qualityAssuranceAction = null;
                        }
                        else
                        {
                           completeCurrentActivityContinue((Map)getParameters());
                        }
                     }
                  }
               });
            }
         }
         else
         {
            throw new PublicException(
                  ProcessPortalErrorClass.UNKNOWN_APP_CONTEXT_FOR_METHOD_INVOCATION);
         }
      }
   }
   
   /**
    * @param outData
    */
   private void completeCurrentActivityContinue(Map<String, Serializable> outData)
   {
      ActivityInstance ai = activityInstance;
      IActivityInteractionController interactionController = getInteractionController(ai.getActivity());

      WorkflowActivityCompletionLog completionLog = PPUtils.complete(interactionController.getContextId(ai), outData,
            CompletionOptions.ACTIVATE_NEXT, ai);

      if (completionLog.isCloseViewAndProceed())
      {
         this.interaction = null;
         ActivityInstance nextActivityObject = null;

         if (completionLog.isSuccess())
         {
            this.activityInstance = completionLog.getCompletedActivity();
            nextActivityObject = completionLog.getNextActivity();
            if (null == nextActivityObject
                  && !QualityAssuranceState.QUALITY_ASSURANCE_TRIGGERED.equals(activityInstance
                        .getQualityAssuranceState())
                  && !QualityAssuranceState.IS_REVISED.equals(activityInstance.getQualityAssuranceState()))
            {
               nextActivityObject = PollingProperties.getInstance().poll(this);
            }
   
            // Lookahead Functionality Part I
            // Set Close Related Views to None because another related activity would be started
            if (null != nextActivityObject)
            {
               thisView.getDefinition().setClosingPolicy(ViewDefinition.CLOSING_POLICY_NONE);
            }
         }

         Map<String, Object> params = getPinViewStatusParam();
         
         skipViewEvents = true;
         int viewIndex = PortalApplication.getInstance().getViewIndex(getThisView());
         if (completionLog.isDelayViewClose())
         {
            //Current view will close on Popup Close
            MessageDialog.getInstance().setCallbackHandler(this);
         }
         else
         {
            PortalApplication.getInstance().closeView(thisView, true);
            if(null == nextActivityObject)
            {
               PortalApplication.getInstance().addEventScript("parent.BridgeUtils.View.syncActiveView();");               
            }
         }

         if (completionLog.isSuccess())
         {
            
            if (null != nextActivityObject)
            {
               if (null != worklistsBean) // Means Assembly Line Mode is OFF  
               {
                  params.put("assemblyLineActivity", worklistsBean.isAssemblyLineActivity(nextActivityObject
                        .getActivity()));
                  params.put("pushService", assemblyLinePushService);
                  params.put("worklistsBean", worklistsBean);
               }
               ActivityInstanceUtils.openActivity(nextActivityObject, params);
               FacesUtils.refreshPage();
            }
            else if(assemblyLineActivity && assemblyLinePushService)
            {
               worklistsBean.openNextAssemblyLineActivity(params);
               FacesUtils.refreshPage();
            }
         }
         skipViewEvents = false;
      }
   }

   /**
    * @param action
    * @return
    */
   private boolean processActivityInstance(WorkflowAction action)
   {
      try
      {
         selectedAction = action;
         
         Map<String, Object> params;

         switch (action)
         {
         case COMPLETE:
            completeCurrentActivity();
            break;
         case SUSPEND:
            params = getPinViewStatusParam();

            suspendCurrentActivity();

            if(assemblyLineActivity && assemblyLinePushService)
            {
               worklistsBean.openNextAssemblyLineActivity(params);
            }
            break;
         case SAVE:
            params = getPinViewStatusParam();

            suspendAndSaveCurrentActivity();

            if(assemblyLineActivity && assemblyLinePushService)
            {
               worklistsBean.openNextAssemblyLineActivity(params);
            }
            break;
         case SUSPEND_TO_USER_WORKLIST:
            params = getPinViewStatusParam();

            suspendCurrentActivity(true, true, false);

            if(assemblyLineActivity && assemblyLinePushService)
            {
               worklistsBean.openNextAssemblyLineActivity(params);
            }
            break;
         case SAVE_TO_USER_WORKLIST:
            params = getPinViewStatusParam();

            suspendAndSaveCurrentActivity(true, true, false);

            if(assemblyLineActivity && assemblyLinePushService)
            {
               worklistsBean.openNextAssemblyLineActivity(params);
            }
            break;
         case SUSPEND_TO_DEFAULT_PERFORMER:
            params = getPinViewStatusParam();

            suspendCurrentActivity(false, true, false);

            if(assemblyLineActivity && assemblyLinePushService)
            {
               worklistsBean.openNextAssemblyLineActivity(params);
            }
            break;
         case SAVE_TO_DEFAULT_PERFORMER:
            params = getPinViewStatusParam();

            suspendAndSaveCurrentActivity(false, true, false);

            if(assemblyLineActivity && assemblyLinePushService)
            {
               worklistsBean.openNextAssemblyLineActivity(params);
            }
            break;
         default:
            // No need to I18N, as this will be detected at implementation level
            throw new RuntimeException("Not Supported");
         }

         return true;
      }
      catch (Exception e)
      {
         trace.error("Unable to Process Activity");
         ExceptionHandler.handleException(e);
         return false;
      }
   }

   /**
    * @param action
    */
   private void showMappedDocumentWarningAndProcessActivity(WorkflowAction action)
   {
      try
      {
         if (ActivityPanelConfigurationBean.isAutoShowMappedDocumentWarning() && null != activityForm
               && activityForm.getDisplayedMappedDocuments(true, true).size() > 0)
         {
            mappedDocumentConfirmationDialog = new MappedDocumentsConfirmationDialog(action, DialogContentType.WARNING,
                  DialogActionType.CONTINUE_CANCEL, MAPPED_DOC_WARN_INCLUDE);

            mappedDocumentConfirmationDialog.openPopup();
         }
         else
         {
            processActivityInstance(action);
         }
      }
      catch (Exception e)
      {
         trace.error("Unable to Process Activity = " + action);
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param interactionController
    * @param releaseInteraction
    * @param mainCallback
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   private void retrieveOutDataMapping(IActivityInteractionController interactionController,
         final boolean releaseInteraction, final ParametricCallbackHandler mainCallback)
   {
      boolean dataAvailable = true;
      Map<String, Serializable> outDataValues = null;

      ActivityInstance ai = activityInstance;
      String contextId = interactionController.getContextId(ai);

      if (PredefinedConstants.DEFAULT_CONTEXT.equals(contextId) && !isHTMLBased(ai.getActivity()))
      {
         if (singleDocumentCase)
         {
            // If not 'savable' means only IN Data Mapping, So no Out Data
            if (documentHandlerBean.isSavable())
            {
               dataAvailable = false;
               documentHandlerBean.save(new ICallbackHandler()
               {
                  public void handleEvent(EventType eventType)
                  {
                     if (EventType.APPLY == eventType)
                     {
                        Document document = ((JCRDocument) documentHandlerBean.getDocumentContentInfo()).getDocument();
   
                        // Even Annotations needs to be cleared
                        document.setDocumentAnnotations(null);
                        
                        Map<String, Serializable> outDataValues = new HashMap<String, Serializable>();
                        outDataValues.put(singleDocumentDatgaMapping.getId(), document);
                        retrieveOutDataMappingContinue(releaseInteraction, mainCallback, outDataValues);
                     }
                  }
               });
            }
         }
         else if (null != activityForm)
         {
            outDataValues = (Map)activityForm.retrieveData();
         }
      }
      else
      {
         outDataValues = interactionController.getOutDataValues(ai);
      }

      if (dataAvailable)
      {
         Map<String, Serializable> ret = new HashMap<String, Serializable>();
         if (!CollectionUtils.isEmpty(outDataValues))
         {
            ret.putAll(outDataValues);
         }
         retrieveOutDataMappingContinue(releaseInteraction, mainCallback, ret);
      }
   }

   /**
    * @param releaseInteraction
    * @param mainCallback
    * @param outValues
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   private void retrieveOutDataMappingContinue(boolean releaseInteraction, ParametricCallbackHandler mainCallback,
         Map<String, Serializable> outValues)
   {
      if (releaseInteraction)
      {
         this.interaction = null;
      }

      mainCallback.setParameters((Map)outValues);
      mainCallback.handleEvent(EventType.APPLY);
   }

   /**
    * 
    */
   public void clear()
   {
      this.title = "";
   }

   /**
    * @param ai
    */
   private void update(boolean fromViewEvent)
   {
      try
      {
         Activity activity = activityInstance.getActivity();
         clear();

         this.title = activity.getName() + " (OID " + activityInstance.getOID() + ")";

         this.supportsWeb = ActivityInstanceUtils.isSupportsWeb(activity);

         if (supportsWeb)
         {
            IActivityInteractionController interactionController = getInteractionController(activity);
            if (null != interactionController)
            {
               String contextId = interactionController.getContextId(activityInstance);
               
               if (PredefinedConstants.DEFAULT_CONTEXT.equals(contextId) && !isHTMLBased(activity))
               {
                  FormGenerationPreferences formPref = new FormGenerationPreferences(
                        ActivityPanelConfigurationBean.getAutoNoOfColumnsInColumnLayout(),
                        ActivityPanelConfigurationBean.getAutoNoOfColumnsInTable());
                  
                  if (!fromViewEvent && null != activityForm)
                  {
                     activityForm.destroy(selectedAction.name()); // If already exists destroy it first before creating again
                     activityForm = null;
                  }

                  activityForm = new ManualActivityForm(formPref, "activityDetailsBean.activityForm", activityInstance,
                        ServiceFactoryUtils.getWorkflowService(), activityInstance.getActivity().getApplicationContext(
                              "default"), new DocumentInputEventHandler()
                              {
                                 /* (non-Javadoc)
                                  * @see org.eclipse.stardust.ui.web.processportal.view.manual.DocumentInputEventHandler#handleEvent(org.eclipse.stardust.ui.web.processportal.view.manual.DocumentInputEventHandler.DocumentInputEvent)
                                  */
                                 public void handleEvent(DocumentInputEvent documentInputEvent)
                                 {
                                    switch (documentInputEvent.getType())
                                    {
                                    case UPLOADED:
                                       refreshActivityPanelForSingleDocument();
                                       break;
                                    case TO_BE_VIEWED:
                                       if (singleDocumentCase)
                                       {
                                          documentInputEvent.setVetoed(true);
                                       }
                                    }
                                 }
                              });
                  activityForm.setData();
                  processSingleDocumentMappingCase(fromViewEvent);
               }
               else
               {
                  WorkflowService ws = ClientContext.getClientContext()
                        .getServiceFactory().getWorkflowService();

                  Map<String, Serializable> inData = (null != ws) //
                        ? ws.getInDataValues(activityInstance.getOID(), contextId, null)
                        : Collections.<String, Serializable>emptyMap();

                  interactionController.initializePanel(activityInstance, inData);
               }
            }
            else
            {
               trace.info("Did not find an interaction controller for the current activity instance.");
            }

            fetchNotes();
            fetchDMSData();

            loadSuccessful = true;
            loadUnsuccessfulMsg = "";
         }
      }
      catch (Exception e)
      {
         loadSuccessful = false;
         loadUnsuccessfulMsg = "";
         ExceptionHandler.handleException(e);
      }
   }

   public void refreshActivityPanelForSingleDocument()
   {
      if (null != activityForm)
      {
         IppDocumentInputController docController = activityForm.getIfSingleDocument();
         if (null != docController && null != docController.getValue())
         {
            processSingleDocumentMappingCase(true);
            documentHandlerBean.handleEvent(new ViewEvent(singleDocumentView, ViewEventType.TO_BE_ACTIVATED));
            documentHandlerBean.handleEvent(new ViewEvent(singleDocumentView, ViewEventType.ACTIVATED));
            documentHandlerBean.handleEvent(new ViewEvent(singleDocumentView, ViewEventType.POST_OPEN_LIFECYCLE));
         }
      }
   }
   
   
   /**
    * @param fromViewEvent
    */
   private void processSingleDocumentMappingCase(boolean fromViewEvent)
   {
      if (null != activityForm)
      {
         IppDocumentInputController docController = activityForm.getIfSingleDocument();
         
         if (null != docController && null != docController.getValue())
         {
            if (!docController.isReadable())
            {
               singleDocumentCase = false;
               return;
            }
            
            singleDocumentDatgaMapping = docController.getDataMapping();
   
            IDocumentContentInfo documentContentInfo = docController.getDocumentContentInfo();
   
            Map<String, Object> docViewParams = new HashMap<String, Object>();
            docViewParams.put("documentInfo", documentContentInfo);
            docViewParams.put("processInstance", processInstance);
            docViewParams.put("dataPathId", singleDocumentDatgaMapping.getDataPath());
            docViewParams.put("dataId", singleDocumentDatgaMapping.getDataId());
            docViewParams.put("baseFormBinding", "activityDetailsBean.documentHandlerBean");
            docViewParams.put("disableSaveAction", true);
            docViewParams.put("embededView", true);
   
            ViewDefinition definition = PortalUiController.getInstance().getViewDefinition("documentView");
            singleDocumentView = new View(definition, "dummy.xhtml");
            singleDocumentView.getViewParams().putAll(docViewParams);
            documentViewToolbars = PortalUiController.getToolbarSections(singleDocumentView);
   
            // fromViewEvent = True means View is just created, so create the Bean
            if (fromViewEvent)
            {
               if (null == documentHandlerBean)
               {
                  // DocumentHandlerBean.getInstance() is not working so create new instance and push it to Tab scope
                  documentHandlerBean = new DocumentHandlerBean();
                  thisView.getCurrentTabScope().remove("documentHandlerBean"); // Remove if exists
                  thisView.getCurrentTabScope().put("documentHandlerBean", documentHandlerBean);
               }
   
               documentHandlerBean.handleEvent(new ViewEvent(singleDocumentView, ViewEventType.CREATED));
   
               // Check if Document is already open in Separate Viewer
               for (View openView : PortalApplication.getInstance().getOpenViews())
               {
                  if ("documentView".equals(openView.getDefinition().getName()))
                  {
                     try
                     {
                        DocumentHandlerBean docHandlerBean = (DocumentHandlerBean) openView.getCurrentTabScope()
                              .get("documentHandlerBean");
                        IDocumentContentInfo docInfo = docHandlerBean.getDocumentContentInfo();
                        if (docInfo.getId().equals(documentContentInfo.getId()))
                        {
                           PortalApplication.getInstance().closeView(openView, true);
                           MessageDialog.addInfoMessage(getMessages().getString("message.documentViewClosed"));
                           break;
                        }
                     }
                     catch (Exception e)
                     {
                        // TODO
                     }
                  }
               }
            }
            else
            {
               documentHandlerBean.initializeBean();
            }
            
            activityForm = null;
            singleDocumentCase = true;
         }
         else
         {
            singleDocumentCase = false;
         }
      }
   }

   /**
    * @return
    */
   public UIComponent getRootGrid()
   {
      if (null != activityForm)
      {
         return ((JsfStructureContainer)activityForm.getRootContainer()).getRootGrid();
      }
      else
      {
         return null;
      }
   }

   /**
    * @return
    */
   public String getFormId()
   {
      return null != activityForm ? activityForm.getFormId() : "";
   }

   /**
    * @param title
    */
   private void setTitle(String title)
   {
      this.title = title;
   }

   public int getNotesCount()
   {
      if (notes != null)
      {
         return notes.size();
      }

      return 0;
   }

   /**
    * 
    */
   private void fetchNotes()
   {
      if (activityInstance != null)
      {
         refreshNotes(ProcessInstanceUtils.getNotes(scopeProcessInstance));
      }
   }
   
   /**
    * @param notes
    */
   private void refreshNotes(List<Note> notes)
   {
      this.notes = notes;
      displayNotes = new ArrayList<NoteTip>();
      
      for (Note note : notes)
      {
         displayNotes.add(new NoteTip(note, null));
      }

      Collections.sort(displayNotes, new Comparator<NoteTip>()
      {
         public int compare(NoteTip arg0, NoteTip arg1)
         {
            return arg1.getTimeStampAsDate().compareTo(arg0.getTimeStampAsDate());
         }
      });
   }

   private void fetchDMSData()
   {
      fetchProcessAttachments();
      fetchProcessDocuments();
   }

   /**
    * 
    */
   public void fetchProcessAttachments()
   {
      if (activityInstance != null)
      {
         processAttachmentsFolderPath = DMSHelper.getProcessAttachmentsFolderPath(processInstance);

         supportsProcessAttachments = DMSHelper.existsProcessAttachmentsDataPath(processInstance);
         if (supportsProcessAttachments)
         {
            processAttachments = DocumentMgmtUtility.getProcesInstanceDocuments(processInstance);
            refreshProcessAttachments(processAttachments);
         }
      }
   }

   /**
    * @param attachments
    */
   private void refreshProcessAttachments(List<Document> attachments)
   {
      processAttachments = attachments;
      if (processAttachments == null)
      {
         processAttachments = new ArrayList<Document>();
      }

      displayProcessAttachments = new ArrayList<DocumentInfo>();
      for (Document processAttachment : processAttachments)
      {
         displayProcessAttachments.add(new DocumentInfo(
               getDocumentIcon(processAttachment.getName(), processAttachment.getContentType()),
               processAttachment));
      }
   }

   public void fetchProcessDocuments()
   {
      if (activityInstance != null)
      {
         displayProcessDocuments = new ArrayList<DocumentInfo>();

         List<TypedDocument> typedDocuments = TypedDocumentsUtil.getTypeDocuments(processInstance);
         if (CollectionUtils.isNotEmpty(typedDocuments))
         {
            supportsProcessDocuments = true;
         }
         String icon;
         Document document;
         for (TypedDocument typedDocument : typedDocuments)
         {
            document = typedDocument.getDocument();
            if (null != document)
            {
               icon = getDocumentIcon(document.getName(), document.getContentType());
            }
            else
            {
               icon = ResourcePaths.I_EMPTY_CORE_DOCUMENT;
            }
            displayProcessDocuments.add(new DocumentInfo(icon, typedDocument));
         }
      }
   }

   /**
    * @return
    */
   public boolean isProcessAttachmentsPopupOpened()
   {
      return processAttachmentsPopupOpened;
   }
   
   /**
    * 
    */
   public void toggleProcessAttachmentsIframePopup()
   {
      if (processAttachmentsPopupOpened)
      {
         closeProcessAttachmentsIframePopup();
      }
      else
      {
         if (isNotesPopupOpened())
         {
            closeNotesIframePopup();
         }
         if (isLinkedProcessPopupOpened())
         {
            closeLinkedProcessIframePopup();
         }
         if (isSwitchProcessPopupOpened())
         {
            closeSwitchProcessIframePopup();
         }
         if(isCasePopupOpened())
         {
            closeCaseIframePopup();
         }
         if (qaCodeIframeBean.isQualityAssuranceCodesPopupOpened())
         {
            qaCodeIframeBean.closeQualityAssuranceCodesIframePopup();
         }
         if (isSuspendActivityPopupOpened())
         {
            closeSuspendActivityIframePopup();
         }
         if (isSaveActivityPopupOpened())
         {
            closeSaveActivityIframePopup();
         }
         openProcessAttachmentsIframePopup();
      }
   }
   
   /**
    * @return
    */
   public String getProcessAttachmentsIframePopupId()
   {
      try
      {
         String iFrameId = "'PA" + getActivityInstance().getOID() + "'";
         return iFrameId;
      }
      catch(Exception e)
      {
         return "''"; // Consume Exception
      }
   }

   /**
    * @return
    */
   public String getProcessAttachmentsIframePopupArgs()
   {
      String advanceArgs =
         "{anchorId:'ippProcessAttachmentsAnchor', width:100, height:30, maxWidth:500, maxHeight:550, " +
         "openOnRight:false, anchorXAdjustment:30, anchorYAdjustment:2, zIndex:200, border:'1px solid black', noUnloadWarning: 'true'}";
      return advanceArgs;
   }

   /**
    * 
    */
   public void openProcessAttachmentsIframePopup()
   {
      String iFrameId = getProcessAttachmentsIframePopupId();
      String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
      + "/plugins/processportal/toolbar/processAttachmentsIframePopup.iface?random=" + System.currentTimeMillis() + "'";

      String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + iFrameId + ", " + url + ", "
            + getProcessAttachmentsIframePopupArgs() + ");";
      PortalApplication.getInstance().addEventScript(script);
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
      
      processAttachmentsPopupOpened = true;
   }

   /**
    * closes the popup from portal application like completing activity  
    */
   public void closeProcessAttachmentsIframePopup()
   {
      if (processAttachmentsPopupOpened)
      {
         String iFrameId = getProcessAttachmentsIframePopupId();
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";
   
         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
         
         processAttachmentsPopupOpened = false;
      }
   }
   
   /**
    * closes the popup in case any menu option from the popup is selected
    */
   public void closeProcessAttachmentsIframePopupSelf()
   {
      if (processAttachmentsPopupOpened)
      {
         String iFrameId = getProcessAttachmentsIframePopupId();
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";
   
         PortalApplication.getInstance().addEventScript(script);
         
         processAttachmentsPopupOpened = false;
      }
   }
   
   /**
    * @return
    */
   public boolean isNotesPopupOpened()
   {
      return notesPopupOpened;
   }
   
   /**
    * 
    */
   public void toggleNotesIframePopup()
   {
      if (notesPopupOpened)
      {
         closeNotesIframePopup();
      }
      else
      {
         if (isProcessAttachmentsPopupOpened())
         {
            closeProcessAttachmentsIframePopup();
         }
         if (isLinkedProcessPopupOpened())
         {
            closeLinkedProcessIframePopup();
         }
         if (isSwitchProcessPopupOpened())
         {
            closeSwitchProcessIframePopup();
         }
         if(isCasePopupOpened())
         {
            closeCaseIframePopup();
         }
         if (qaCodeIframeBean.isQualityAssuranceCodesPopupOpened())
         {
            qaCodeIframeBean.closeQualityAssuranceCodesIframePopup();
         }
         if (isSuspendActivityPopupOpened())
         {
            closeSuspendActivityIframePopup();
         }
         if (isSaveActivityPopupOpened())
         {
            closeSaveActivityIframePopup();
         }
         openNotesIframePopup();
      }
   }
   
   /**
    * @return
    */
   public String getNotesIframePopupId()
   {
      try
      {
         String iFrameId = "'NT" + getActivityInstance().getOID() + "'";
         return iFrameId;
      }
      catch (Exception e)
      {
         return "''"; // Consume Exception
      }
   }

   /**
    * @return
    */
   public String getNotesIframePopupArgs()
   {
      String advanceArgs =
         "{anchorId:'ippNotesAnchor', width:100, height:30, maxWidth:500, maxHeight:550, " +
         "openOnRight:false, anchorXAdjustment:30, anchorYAdjustment:2, zIndex:200, border:'1px solid black', noUnloadWarning: 'true'}";
      return advanceArgs;
   }

   /**
    * 
    */
   public void openNotesIframePopup()
   {
      String iFrameId = getNotesIframePopupId();
      String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
      + "/plugins/processportal/toolbar/notesIframePopup.iface?random=" + System.currentTimeMillis() + "'";

      String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + iFrameId + ", " + url + ", "
            + getNotesIframePopupArgs() + ");";
      PortalApplication.getInstance().addEventScript(script);
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
      
      notesPopupOpened = true;
   }
   
   /**
    * closes the popup from portal application like completing activity  
    */
   public void closeNotesIframePopup()
   {
      if (notesPopupOpened)
      {
         String iFrameId = getNotesIframePopupId();
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";
   
         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
         
         notesPopupOpened = false;
      }
   }
   
   /**
    * closes the popup in case any menu option from the popup is selected
    */
   public void closeNotesIframePopupSelf()
   {
      if (notesPopupOpened)
      {
         String iFrameId = getNotesIframePopupId();
         String script = "parent.ippPortalMain.InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";
   
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
         
         notesPopupOpened = false;
      }
   }
   
   /**
    * @return
    */
   public boolean isLinkedProcessPopupOpened()
   {
      return linkedProcessPopupOpened;
   }

   /**
    * 
    */
   public void toggleLinkedProcessIframePopup()
   {
      if (linkedProcessPopupOpened)
      {
         closeLinkedProcessIframePopup();
      }
      else
      {
         if (isNotesPopupOpened())
         {
            closeNotesIframePopup();
         }
         if (isProcessAttachmentsPopupOpened())
         {
            closeProcessAttachmentsIframePopup();
         }
         if (isSwitchProcessPopupOpened())
         {
            closeSwitchProcessIframePopup();
         }
         if(isCasePopupOpened())
         {
            closeCaseIframePopup();
         }
         if (qaCodeIframeBean.isQualityAssuranceCodesPopupOpened())
         {
            qaCodeIframeBean.closeQualityAssuranceCodesIframePopup();
         }
         if (isSuspendActivityPopupOpened())
         {
            closeSuspendActivityIframePopup();
         }
         if (isSaveActivityPopupOpened())
         {
            closeSaveActivityIframePopup();
         }
         openLinkedProcessIframePopup();
      }
   }

   /**
    * @return
    */
   public String getLinkedProcessIframePopupId()
   {
      try
      {
         String iFrameId = "'PL" + getActivityInstance().getOID() + "'";
         return iFrameId;
      }
      catch (Exception e)
      {
         return "''"; // Consume Exception
      }
   }

   /**
    * @return
    */
   public String getLinkedProcessIframePopupArgs()
   {
      String advanceArgs = "{anchorId:'ippLinkedProcessAnchor', width:100, height:30, maxWidth:500, maxHeight:550, "
            + "openOnRight:false, anchorXAdjustment:15, anchorYAdjustment:2, zIndex:200, border:'1px solid black', noUnloadWarning: 'true'}";
      return advanceArgs;
   }

   /**
    * 
    */
   public void openLinkedProcessIframePopup()
   {
      String iFrameId = getLinkedProcessIframePopupId();
      String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
            + "/plugins/views-common/common/linkedProcessIframePopup.iface?random=" + System.currentTimeMillis() + "'";

      String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + iFrameId + ", " + url + ", "
            + getLinkedProcessIframePopupArgs() + ");";
      PortalApplication.getInstance().addEventScript(script);
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

      linkedProcessPopupOpened = true;
   }

   /**
    * closes the popup from portal application like completing activity
    */
   public void closeLinkedProcessIframePopup()
   {
      if (linkedProcessPopupOpened)
      {
         String iFrameId = getLinkedProcessIframePopupId();
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";

         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

         linkedProcessPopupOpened = false;
      }
   }

   /**
    * closes the popup in case any menu option from the popup is selected
    */
   public void closeLinkedProcessIframePopupSelf()
   {
      if (linkedProcessPopupOpened)
      {
         String iFrameId = getLinkedProcessIframePopupId();
         String script = "parent.ippPortalMain.InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";

         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

         linkedProcessPopupOpened = false;
      }
   }

   public void openLinkedProcess(ActionEvent event)
   {

      if (null != processInstance)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put("processInstanceOID", Long.toString(processInstance.getOID()));
         PortalApplication.getInstance().openViewById("correspondenceView", "DocumentID=" + processInstance.getOID(),
               params, null, true);
      }
   }

   public boolean isSwitchProcessPopupOpened()
   {
      return switchProcessPopupOpened;
   }

   /**
    * 
    */
   public void toggleSwitchProcessIframePopup()
   {
      if (switchProcessPopupOpened)
      {
         closeSwitchProcessIframePopup();
      }
      else
      {
         if (isProcessAttachmentsPopupOpened())
         {
            closeProcessAttachmentsIframePopup();
         }
         if (isNotesPopupOpened())
         {
            closeNotesIframePopup();
         }
         if (isLinkedProcessPopupOpened())
         {
            closeLinkedProcessIframePopup();
         }
         if(isCasePopupOpened())
         {
            closeCaseIframePopup();
         }
         if (qaCodeIframeBean.isQualityAssuranceCodesPopupOpened())
         {
            qaCodeIframeBean.closeQualityAssuranceCodesIframePopup();
         }
         if (isSuspendActivityPopupOpened())
         {
            closeSuspendActivityIframePopup();
         }
         if (isSaveActivityPopupOpened())
         {
            closeSaveActivityIframePopup();
         }
         openSwitchProcessIframePopup();
      }
   }
   
   /**
    * closes the popup from portal application like completing activity
    */
   public void closeSwitchProcessIframePopup()
   {
      if (switchProcessPopupOpened)
      {
         String iFrameId = getSwitchProcessIframePopupId();
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";

         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

         switchProcessPopupOpened = false;
      }
   }
   
   /**
    * 
    */
   public void openSwitchProcessIframePopup()
   {
      String iFrameId = getSwitchProcessIframePopupId();
      String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
      + "/plugins/views-common/common/abortIframePopup.iface?random=" + System.currentTimeMillis() + "'";

      String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + iFrameId + ", " + url + ", "
            + getSwitchProcessIframePopupArgs() + ");";
      PortalApplication.getInstance().addEventScript(script);
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
      
      switchProcessPopupOpened = true;
   }
   
   /**
    * @return
    */
   public String getSwitchProcessIframePopupArgs()
   {
      String advanceArgs =
         "{anchorId:'ippSwitchAnchor', width:100, height:30, maxWidth:500, maxHeight:550, " +
         "openOnRight:false, anchorXAdjustment:10, anchorYAdjustment:5, zIndex:200, border:'1px solid black', noUnloadWarning: 'true'}";
      return advanceArgs;
   }
   
   /**
    * @return
    */
   public String getSwitchProcessIframePopupId()
   {
      try
      {
         String iFrameId = "'SW" + getActivityInstance().getOID() + "'";
         return iFrameId;
      }
      catch (Exception e)
      {
         return "''"; // Consume Exception
      }
   }
   
   /**
    * 
    * @return
    */
   public boolean isCasePopupOpened()
   {
      return casePopupOpened;
   }

   /**
    * 
    */
   public void toggleCaseIframePopup()
   {
      if (casePopupOpened)
      {
         closeCaseIframePopup();
      }
      else
      {
         if (isProcessAttachmentsPopupOpened())
         {
            closeProcessAttachmentsIframePopup();
         }
         if (isNotesPopupOpened())
         {
            closeNotesIframePopup();
         }
         if (isLinkedProcessPopupOpened())
         {
            closeLinkedProcessIframePopup();
         }
         if (isSwitchProcessPopupOpened())
         {
            closeSwitchProcessIframePopup();
         }
         if (qaCodeIframeBean.isQualityAssuranceCodesPopupOpened())
         {
            qaCodeIframeBean.closeQualityAssuranceCodesIframePopup();
         }
         if (isSuspendActivityPopupOpened())
         {
            closeSuspendActivityIframePopup();
         }
         if (isSaveActivityPopupOpened())
         {
            closeSaveActivityIframePopup();
         }
         openCaseIframePopup();
      }
   }
   
   /**
    * closes the popup from portal application like completing activity
    */
   public void closeCaseIframePopup()
   {
      if (casePopupOpened)
      {
         String iFrameId = getCaseIframePopupId();
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";

         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

         casePopupOpened = false;
      }
   }
   
   /**
    * 
    */
   public void openCaseIframePopup()
   {
      String iFrameId = getCaseIframePopupId();
      String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
      + "/plugins/views-common/common/caseIframePopup.iface?random=" + System.currentTimeMillis() + "'";

      String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + iFrameId + ", " + url + ", "
            + getCaseIframePopupArgs() + ");";
      PortalApplication.getInstance().addEventScript(script);
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
      
      casePopupOpened = true;
   }
   
   /**
    * @return
    */
   public String getCaseIframePopupArgs()
   {
      String advanceArgs =
         "{anchorId:'ippCaseAnchor', width:100, height:30, maxWidth:500, maxHeight:550, " +
         "openOnRight:false, anchorXAdjustment:13, anchorYAdjustment:5, zIndex:200, border:'1px solid black', noUnloadWarning: 'true'}";
      return advanceArgs;
   }
   
   /**
    * @return
    */
   public String getCaseIframePopupId()
   {
      try
      {
         String iFrameId = "'SW" + getActivityInstance().getOID() + "'";
         return iFrameId;
      }
      catch (Exception e)
      {
         return "''"; // Consume Exception
      }
   }
   
   
   /**
    * 
    */
   public void openCreateCase(ActionEvent event)
   {
      List<ProcessInstance> selectedProcesses = CollectionUtils.newArrayList();
      selectedProcesses.add(processInstance);
      CreateCaseDialogBean createCaseDialog = CreateCaseDialogBean.getInstance();
      createCaseDialog.setSourceProcessInstances(selectedProcesses);
      createCaseDialog.openPopup();
      closeCaseIframePopup();

   }
   
   /**
    * Method isHasCreateCasePermission.
    * @return boolean
    */
   public boolean isEnableCreateCase()
   {
      return hasCreateCasePermission;
   }

   /**
    * 
    */
   public void openAttachToCase(ActionEvent event)
   {
      List<ProcessInstance> selectedProcesses = CollectionUtils.newArrayList();
      selectedProcesses.add(processInstance);
      AttachToCaseDialogBean attachToCaseDialog = AttachToCaseDialogBean.getInstance();
      attachToCaseDialog.setSourceProcessInstances(selectedProcesses);
      attachToCaseDialog.openPopup();
      closeCaseIframePopup();
   }
   
   /**
    * action listener to open Switch process dialog
    */
   public void openSwitchProcess(ActionEvent event)
   {
      SwitchProcessDialogBean dialog = SwitchProcessDialogBean.getInstance();
      List<ProcessInstance> sourceList = CollectionUtils.newArrayList();
      sourceList.add(getActivityInstance().getProcessInstance());
      dialog.setSourceProcessInstances(sourceList);
      dialog.openPopup();
      closeSwitchProcessIframePopup();

      dialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType.equals(EventType.APPLY))
            {
               skipViewEvents = true;
               PortalApplication.getInstance().closeView(thisView, true);
               skipViewEvents = false;
            }
         }
      });
   }
   
   /**
    * action listener to open Join process dialog
    */
   public void openJoinProcess(ActionEvent event)
   {
      JoinProcessDialogBean dialog=JoinProcessDialogBean.getInstance();    
      dialog.setSourceProcessInstance(getActivityInstance().getProcessInstance());
      dialog.openPopup();
      closeSwitchProcessIframePopup();

      dialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType.equals(EventType.APPLY))
            {
               skipViewEvents = true;
               PortalApplication.getInstance().closeView(thisView, true);
               skipViewEvents = false;
            }
         }
      });

   }
   
   public boolean isEnableJoinProcess()
   {
      return hasJoinProcessPermission;
   }

   public boolean isEnableSwitchProcess()
   {
      return hasSwitchProcessPermission;
   } 
   
   public boolean isEnableSpawnProcess()
   {
      return hasSpawnProcessPermission;
   }
   
   
   /**
    * 
    */
   public void renderSession()
   {
      if (!isJSFActivity() && thisView.getViewState() != ViewState.CLOSED)
      {
         PortalApplication.getInstance().renderPortalSession();
      }
   }

   public static String getDocumentIcon(String fileName, String contentType)
   {
      return "/plugins/views-common/images/icons/mime-types/"
            + MimeTypesHelper.detectMimeType(fileName, contentType).getIconPath();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.processportal.PollingProperties.Activator#activateNext()
    */
   public ActivityInstance activateNext()
   {
      ActivityInstance nextAI = null;
      if (null != activityInstance)
      {
         nextAI = ServiceFactoryUtils.getServiceFactory().getWorkflowService()
               .activateNextActivityInstance(activityInstance.getOID());
      }
      return nextAI;
   }

   /*public void createDocument()
   {
      DMSHelper.ensureFolderExists(SessionContext.findSessionContext()
            .getServiceFactory().getDocumentManagementService(),
            processAttachmentsFolderPath);

      DocumentHandle document = FocusDocumentObject.getFocusDocumentObject()
            .createDocument();

      if (document != null)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put("documentID", document.getName());
         params.put("documentOID", document.getId());
         params.put("processInstance", processInstance);
         PortalApplication.getInstance().openViewById("documentPanelView",
               "documentOID=" + document.getId(), params, null, true);
      }
   }*/

   /**
    * @return
    */
   public String getProcessAttachmentsFolderId()
   {
      return processAttachmentsFolderPath;
   }

   public boolean isAbortable()
   {
      if (null != activityInstance)
      {
         return ActivityInstanceUtils.isAbortable(activityInstance);
      }
      return false;
   }
   
   public boolean isCaseEnabled()
   {
      if (processInstance.isCaseProcessInstance())
      {
         return false;
      }
      else if (processInstance.getOID() != processInstance.getRootProcessInstanceOID())
      {
         return false;
      }
      return true;
   }

   /**
    * 
    * @return
    */
   public boolean isAbortableProcess()
   {
      if (null != activityInstance)
      {
         return ProcessInstanceUtils.isAbortable(activityInstance.getProcessInstance());
      }
      return false;
   }
   
   public boolean isResubmitable()
   {
      if (null != activityInstance)
      {
         return ResubmissionBean.isResubmissionActivity(activityInstance);
      }
      return false;
   }
   
   public boolean isDelegable()
   {
      if (null != activityInstance && !isAssemblyLineActivity())
      {
         return ActivityInstanceUtils.isDelegable(activityInstance);
      }
      return false;
   }

   private void setActivityInstance(ActivityInstance ai)
   {
      this.activityInstance = ai;
      if (this.activityInstance != null)
      {
         this.processInstance = ProcessInstanceUtils.getProcessInstance(ai);
         
         if (this.processInstance.getOID() == processInstance.getScopeProcessInstanceOID())
         {
            this.scopeProcessInstance = this.processInstance;
         }
         else
         {
            this.scopeProcessInstance = ProcessInstanceUtils.getProcessInstance(processInstance
                  .getScopeProcessInstanceOID());
         }
      }
   }
   
   /**
    * Returns the current Pinning Mode Status in params, So that same can be passed to openViewById()
    * @return
    */
   private Map<String, Object> getPinViewStatusParam()
   {
      Map<String, Object> params = CollectionUtils.newTreeMap();

      // Get Current Pin View Status
      PortalApplication portalApp = PortalApplication.getInstance();
      if (portalApp.isPinViewOpened() && portalApp.getPinView() == thisView)
      {
         params.put("pinViewMode", portalApp.getPinViewOrientation());;
      }
      
      return params;
   }

   public void toggleAssemblyLinePushService()
   {
      setAssemblyLinePushService(!isAssemblyLinePushService());
   }
   
   public ActivityInstance getActivityInstance()
   {
      return activityInstance;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public String getTitle()
   {
      return title;
   }

   public void setEventController(EventController eventController)
   {
      this.eventController = eventController;
   }

   public void setIppEventController(IppEventController ippEventController)
   {
      this.ippEventController = ippEventController;
   }

   public boolean isLoadSuccessful()
   {
      return loadSuccessful;
   }

   public String getLoadUnsuccessfulMsg()
   {
      return loadUnsuccessfulMsg;
   }

   public boolean isSupportsWeb()
   {
      return supportsWeb;
   }

   public boolean isSupportsProcessAttachments()
   {
      return supportsProcessAttachments;
   }

   public List<NoteTip> getDisplayNotes()
   {
      return displayNotes;
   }

   public List<Document> getProcessAttachments()
   {
      return processAttachments;
   }

   public List<DocumentInfo> getDisplayProcessAttachments()
   {
      return displayProcessAttachments;
   }

   public List<DocumentInfo> getDisplayProcessDocuments()
   {
      return displayProcessDocuments;
   }

   public boolean isAssemblyLineActivity()
   {
      return assemblyLineActivity;
   }

   public boolean isAssemblyLinePushService()
   {
      return assemblyLinePushService;
   }

   public void setAssemblyLinePushService(boolean assemblyLinePushService)
   {
      this.assemblyLinePushService = assemblyLinePushService;
   }
   
   public boolean isShowAssemblyLinePushServiceControls()
   {
      return showAssemblyLinePushServiceControls;
   }

   public boolean isSupportsProcessDocuments()
   {
      return supportsProcessDocuments;
   }

   public ManualActivityForm getActivityForm()
   {
      return activityForm;
   }

   public QualityAssuranceCodesBean getQaCodeIframeBean()
   {
      return qaCodeIframeBean;
   }

   public ConfirmationDialog getMappedDocumentConfirmationDialog()
   {
      return mappedDocumentConfirmationDialog;
   }

   public boolean isSingleDocumentCase()
   {
      return singleDocumentCase;
   }

   public List<ToolbarSection> getDocumentViewToolbars()
   {
      return documentViewToolbars;
   }

   public DocumentHandlerBean getDocumentHandlerBean()
   {
      return documentHandlerBean;
   }

   public View getThisView()
   {
      return thisView;
   }
   /**
    * @author subodh.godbole
    *
    */
   public class MappedDocumentsConfirmationDialog extends ConfirmationDialog implements ConfirmationDialogHandler
   {
      private static final long serialVersionUID = 1L;

      private boolean doNotShowMsgAgain;
      private WorkflowAction action;

      /**
       * @param action
       * @param contentType
       * @param actionType
       * @param includePath
       */
      public MappedDocumentsConfirmationDialog(WorkflowAction action, DialogContentType contentType, DialogActionType actionType, String includePath)
      {
         super(contentType, actionType, null);
         setIncludePath(includePath);
         setHandler(this);
         
         this.action = action;
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.viewscommon.dialogs.ConfirmationDialogHandler#accept()
       */
      public boolean accept()
      {
         if (doNotShowMsgAgain)
         {
            ActivityPanelConfigurationBean.setAutoShowMappedDocumentWarning(false);
         }

         mappedDocumentConfirmationDialog = null;
         return processActivityInstance(action);
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.viewscommon.dialogs.CommonPopupDialogHandler#cancel()
       */
      public boolean cancel()
      {
         mappedDocumentConfirmationDialog = null;
         return true;
      }

      public boolean isDoNotShowMsgAgain()
      {
         return doNotShowMsgAgain;
      }

      public void setDoNotShowMsgAgain(boolean doNotShowMsgAgain)
      {
         this.doNotShowMsgAgain = doNotShowMsgAgain;
      }
   }
   
   public boolean accept()
   {
      PortalApplication.getInstance().closeView(thisView, true);
      return true;
   }

}