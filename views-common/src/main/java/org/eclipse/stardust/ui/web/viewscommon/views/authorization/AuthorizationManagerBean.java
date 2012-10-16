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
package org.eclipse.stardust.ui.web.viewscommon.views.authorization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.RuntimePermissionsDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.LaunchPanel;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.UiElement;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.authorization.PermissionUserObject.NODE_TYPE;
import org.eclipse.stardust.ui.web.viewscommon.views.authorization.PermissionUserObject.PERMISSION_TYPE;

import com.icesoft.faces.component.tree.IceUserObject;

/**
 * @author Vikas.Mishra
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class AuthorizationManagerBean extends PopupUIComponentBean
      implements ViewEventHandler, ICallbackHandler, ConfirmationDialogHandler
{

   private static final long serialVersionUID = 1L;

   private static final String PERMISSION_KEY = "permission.model.";
   private static final String ALL_PARTICIPANT = "All";
   private static final String L_PANELS = "launchPanels";
   private static final String VIEWS = "views";
   private static final String GLOBAL_EXTNS = "globalExtensions";
   
   private static final ModelParticipantComparator MODEL_PARTICIPANT_COMPARATOR = new ModelParticipantComparator();
   private final AdministrationService administrationService;
   private DefaultTreeModel permissionTreeModel;
   private final ParticipantHelper participantHelper;
   private final PermissionPickList permissionPickList;
   private final SelectItem[] viewSelection = new SelectItem[2];
   private PermissionsDetails permissions;
   private final Set<String> modifiedPermissions = new HashSet<String>();
   private final Set<String> selectedPermissions = new HashSet<String>();
   private String selectedView;
   private boolean pasteActionEnable;
   private boolean showPermissionView = true;
   private ConfirmationDialog authMngrConfirmationDialog;

   /**
    * Constructor
    */
   public AuthorizationManagerBean()
   {
      super(ResourcePaths.V_AUTHORIZATION_MANAGER_VIEW);
      participantHelper = new ParticipantHelper();
      permissionPickList = new PermissionPickList();
      viewSelection[0] = new SelectItem(VIEW_TYPE.PERMISSION.name(), getMessages().getString("permissions.label"));
      viewSelection[1] = new SelectItem(VIEW_TYPE.PARTICIPANT.name(), getMessages().getString("participant.label"));

      SessionContext sessionContext = SessionContext.findSessionContext();
      administrationService = sessionContext.getServiceFactory().getAdministrationService();
   }

   /**
    * method to add participants on selected permission nodes
    * 
    * @param event
    */
   public void addParticipants(ActionEvent event)
   {
      List<Participant> selectedParticipants = participantHelper.getAddedParticipants();

      if (!selectedParticipants.isEmpty())
      {
         DefaultMutableTreeNode root = (DefaultMutableTreeNode) permissionTreeModel.getRoot();
         updatePermissions(root, selectedParticipants);
      }
   }

   /**
    * @param treeNode
    * @param selectedParticipants
    */
   private void updatePermissions(DefaultMutableTreeNode treeNode, List<Participant> selectedParticipants)
   {
      int count = treeNode.getChildCount();
      for (int i = 0; i < count; i++)
      {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeNode.getChildAt(i);
         PermissionUserObject userObject = (PermissionUserObject) node.getUserObject();
         if (NODE_TYPE.ABSOLUTE == userObject.getNodeType())
         {
            updatePermissions(node, selectedParticipants);
         }
         else
         {
            if (userObject.isSelected())
            {
               boolean isAdded = addUniqueParticipants(userObject.getPermissionId(), selectedParticipants);

               if (isAdded)
               {
                  userObject.setDirty(isAdded);
                  updateParticipantNodes(node);
                  modifiedPermissions.add(userObject.getPermissionId());
               }
            }
         }
      }
   }

   /**
    * popup dialog Yes action
    */
   public void confirmCancel()
   {
      showPermissionView = VIEW_TYPE.PERMISSION.name().equals(selectedView) ? false : true;

      if (showPermissionView)
      {
         reset();
      }
      else
      {
         permissionPickList.reset();
      }
      showPermissionView = !showPermissionView;
   }

   /**
    * popup dialog cancel action
    */
   public void confirmYes()
   {
      showPermissionView = VIEW_TYPE.PERMISSION.name().equals(selectedView) ? false : true;

      if (showPermissionView)
      {
         save();
      }
      else
      {
         permissionPickList.save();
      }
      showPermissionView = !showPermissionView;

   }

   /**
    * method to listen context menu action.
    * 
    * @param event
    */
   public void contextMenuAction(ActionEvent event)
   {
      String actionType = FacesUtils.getRequestParameter("action");
      PermissionUserObject nodeUserObject = (PermissionUserObject) event.getComponent().getAttributes().get("node");
      nodeUserObject.setExpanded(true);
      nodeUserObject.setSelected(true);

      if (CONTEXT_ACTION.RESTORE_DEFAULT_PARTICIPANT.name().equals(actionType))
      {
         selectedPermissions.add(nodeUserObject.getPermissionId());
         List<PermissionUserObject> selectedPermissions = findSelectedNodes(NODE_TYPE.PERMISSION);

         for (PermissionUserObject userObject : selectedPermissions)
         {
            permissions.setGrants(userObject.getPermissionId(), null);
            userObject.setContainsDefaultParticipants(true);
            userObject.setDirty(true);
            modifiedPermissions.add(userObject.getPermissionId());
            updateParticipantNodes(userObject.getWrapper());
         }
      }
      else if (CONTEXT_ACTION.REMOVE_PARTICIPANT.name().equals(actionType))
      {
         nodeUserObject.setSelected(true);
         selectedPermissions.add(nodeUserObject.getPermissionId());

         List<PermissionUserObject> selectedParticipants = findSelectedNodes(NODE_TYPE.ROLE_UNSCOPED);

         for (PermissionUserObject participant : selectedParticipants)
         {
            boolean removed = removeParticipant(participant.getPermissionId(), participant.getParticipantId());
            participant.setDirty(removed);
            modifiedPermissions.add(participant.getPermissionId());
            updateParticipantNodes((DefaultMutableTreeNode) participant.getWrapper().getParent());
         }

      }
      else if (CONTEXT_ACTION.ADD_ALL_PARTICIPANT.name().equals(actionType))
      {
         nodeUserObject.setSelected(true);
         selectedPermissions.add(nodeUserObject.getPermissionId());

         List<PermissionUserObject> selectedPermissions = findSelectedNodes(NODE_TYPE.PERMISSION);

         for (PermissionUserObject userObject : selectedPermissions)
         {
            permissions.setAllGrant(userObject.getPermissionId());
            userObject.setSelected(false);
            modifiedPermissions.add(userObject.getPermissionId());
            nodeUserObject.setContainsAllParticipants(true);
            updateParticipantNodes((DefaultMutableTreeNode) userObject.getWrapper());
         }
      }
      else if (CONTEXT_ACTION.COPY.name().equals(actionType))
      {
         nodeUserObject.setSelected(true);
         pasteActionEnable = true;
      }
      else if (CONTEXT_ACTION.PASTE.name().equals(actionType))
      {
         selectedPermissions.add(nodeUserObject.getPermissionId());
         nodeUserObject.setExpanded(true);

         List<Participant> participants = CollectionUtils.newArrayList();
         boolean isAllSelected = false;
         List<PermissionUserObject> selectedParticipants = findSelectedNodes(NODE_TYPE.ROLE_UNSCOPED);

         for (PermissionUserObject userObject : selectedParticipants)
         {
            if (!isAllSelected && userObject.getParticipantId().equals(ALL_PARTICIPANT))
            {
               isAllSelected = true;
            }

            participants.add(userObject.getParticipant());
            userObject.setSelected(false);

         }

         if (isAllSelected)
         {
            for (String permissionId : selectedPermissions)
            {
               permissions.setAllGrant(permissionId);
               modifiedPermissions.add(permissionId);
            }
         }
         else
         {
            for (String permissionId : selectedPermissions)
            {
               boolean isAdded = addUniqueParticipants(permissionId, participants);

               if (isAdded)
               {
                  modifiedPermissions.add(permissionId);
               }
            }
         }

         List<PermissionUserObject> selectedPermissions = findSelectedNodes(NODE_TYPE.PERMISSION);

         for (PermissionUserObject userObject : selectedPermissions)
         {
            updateParticipantNodes((DefaultMutableTreeNode) userObject.getWrapper());
         }

         pasteActionEnable = false;
      }
   }

   public ParticipantHelper getParticipantHelper()
   {
      return participantHelper;
   }

   public PermissionPickList getPermissionPickList()
   {
      return permissionPickList;
   }

   public DefaultTreeModel getPermissionTreeModel()
   {
      return permissionTreeModel;
   }

   public String getSelectedView()
   {
      return selectedView;
   }

   /**
    * Gets the option items for model export selection .
    * 
    * @return array of model export items
    */
   public SelectItem[] getViewSelection()
   {
      return viewSelection;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(com.sungard
    * .framework .ui.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         selectedView = VIEW_TYPE.PERMISSION.name();
         participantHelper.createTable();
         permissionPickList.setShow(false);
         selectedPermissions.clear();

         reset();
         resetPermissions();

         initialize();

      }
      else if (ViewEventType.CLOSED == event.getType())
      {
         showPermissionView = true;
         selectedPermissions.clear();
         modifiedPermissions.clear();
         participantHelper.reset();
         permissionPickList.reset();
      }
   }

   private void resetPermissions()
   {
      // UI permissions
      permissions = new PermissionsDetails(UiPermissionUtils.getAllPermissions(administrationService, true));

      // general Permissions
      RuntimePermissionsDetails runtimePermissionsDetails = (RuntimePermissionsDetails) administrationService
            .getGlobalPermissions();
      permissions.setGeneralPermission(runtimePermissionsDetails);
   }

   public void handleEvent(EventType eventType)
   {
      if (EventType.APPLY.equals(eventType))
      {
         save();
      }
      else
      {
         update();
      }

      permissionPickList.setDirty(false);
   }

   /**
    * method to initialize
    */
   @Override
   public void initialize()
   {
      participantHelper.refresh();
      buildPermissionTree();
   }

   public boolean isDirty()
   {
      // return dirty;
      return !modifiedPermissions.isEmpty() || permissionPickList.isDirty();
   }

   public boolean isPasteActionEnable()
   {
      return pasteActionEnable;
   }

   public boolean isShowParticipantView()
   {
      return !showPermissionView;
   }

   public boolean isShowPermissionView()
   {
      return showPermissionView;
   }

   /**
    * @param event
    */
   public void nodeExpandCollapse(ActionEvent event)
   {
      // do nothings
   }

   public void permissionNodeSelected(ActionEvent event)
   {
      PermissionUserObject nodeUserObject = (PermissionUserObject) event.getComponent().getAttributes().get("node");
      nodeUserObject.setSelected(!nodeUserObject.isSelected());
      nodeUserObject.setExpanded(nodeUserObject.isSelected());

      if (nodeUserObject.getNodeType().equals(NODE_TYPE.PERMISSION))
      {
         if (nodeUserObject.isSelected())
         {
            selectedPermissions.add(nodeUserObject.getPermissionId());
         }
         else
         {
            selectedPermissions.remove(nodeUserObject.getPermissionId());
         }
      }
   }

   /**
    * method to reset bean variables states
    */
   public void reset()
   {
      permissionTreeModel = null;
      selectedPermissions.clear();
      modifiedPermissions.clear();
      super.reset();
   }

   /**
    * method to save added/removed participants
    */
   public void save()
   {
      if (!showPermissionView)
      {
         permissionPickList.save();
      }
      else
      {
         try
         {
            administrationService.setGlobalPermissions(permissions.getGeneralPermission());
            UiPermissionUtils.savePreferences(administrationService, permissions.getUIPermissionMap());
            resetPermissions();
            modifiedPermissions.clear();
            resetDirtyNodes();
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
   }

   public void setPasteActionEnable(boolean pasteActionEnable)
   {
      this.pasteActionEnable = pasteActionEnable;
   }

   public void setPermissionTreeModel(DefaultTreeModel permissionTreeModel)
   {
      this.permissionTreeModel = permissionTreeModel;
   }

   public void setSelectedView(String selectedView)
   {
      this.selectedView = selectedView;
   }

   public ConfirmationDialog getAuthMngrConfirmationDialog()
   {
      return authMngrConfirmationDialog;
   }

   /**
    * Updates the changes
    */
   public void update()
   {
      if (showPermissionView)
      {
         selectedPermissions.clear();
         modifiedPermissions.clear();

         resetPermissions();

         initialize();
      }
      else
      {
         permissionPickList.refresh();
      }
   }

   /**
    * 
    * @param event
    */
   public void viewSelectionListener(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();

         return;
      }
      else
      {
         String oldView = event.getOldValue().toString();
         String newView = event.getNewValue().toString();

         if (isDirty() && !newView.equals(oldView))
         {
            // selectedView=oldView;
            // Confirmation dialog to save details before switching
            authMngrConfirmationDialog = new ConfirmationDialog(DialogContentType.NONE, DialogActionType.YES_NO, null,
                  DialogStyle.COMPACT, this);
            authMngrConfirmationDialog.setTitle(getMessages().getString("confirmSavePermissions.title"));
            authMngrConfirmationDialog.setMessage(getMessages().getString("confirmSavePermissionsDescription"));
            authMngrConfirmationDialog.openPopup();
         }
         else
         {
            showPermissionView = VIEW_TYPE.PERMISSION.name().equals(newView) ? true : false;

            if (showPermissionView)
            {
               update();
            }
            else
            {
               permissionPickList.reset();
               permissionPickList.initialize();
            }
         }
      }
   }

   /**
    * 
    * @param parentNode
    * @param permissionId
    * @return
    */
   private DefaultMutableTreeNode addAllParticipantNode(DefaultMutableTreeNode parentNode, String permissionId)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();

      PermissionUserObject branchObject = new PermissionUserObject(childNode, ALL_PARTICIPANT, getMessages().getString(
            "participant.all"), permissionId);
      childNode.setUserObject(branchObject);

      if (parentNode != null)
      {
         parentNode.add(childNode);
      }

      return childNode;
   }

   /**
    * @return
    */
   private static DefaultMutableTreeNode addParticipantNode(DefaultMutableTreeNode parentNode,
         ModelParticipantInfo modelInfo, String permissionId)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      PermissionUserObject branchObject = new PermissionUserObject(childNode, modelInfo, permissionId);
      childNode.setUserObject(branchObject);

      if (parentNode != null)
      {
         parentNode.add(childNode);
      }

      return childNode;
   }

   /**
    * @param parentNode
    * @param permissionId
    * @param permissionLabel
    * @return
    */
   private static DefaultMutableTreeNode addPermissionNode(DefaultMutableTreeNode parentNode, String permissionId,
         String permissionLabel)
   {
      return addPermissionNode(parentNode, permissionId, permissionLabel, null);
   }

   /**
    * @param parentNode
    * @param permissionId
    * @param pType
    * @return
    */
   private static DefaultMutableTreeNode addPermissionNode(DefaultMutableTreeNode parentNode, String permissionId,
         PERMISSION_TYPE pType)
   {
      return addPermissionNode(parentNode, permissionId, null, pType);
   }

   /**
    * @param parentNode
    * @param permissionId
    * @param permissionLabel
    * @param pType
    * @return
    */
   private static DefaultMutableTreeNode addPermissionNode(DefaultMutableTreeNode parentNode, String permissionId,
         String permissionLabel, PERMISSION_TYPE pType)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      PermissionUserObject branchObject = new PermissionUserObject(childNode, permissionId, permissionLabel, pType);
      childNode.setUserObject(branchObject);

      if (parentNode != null)
      {
         parentNode.add(childNode);
      }
      return childNode;
   }

   /**
    * @return
    */
   private static DefaultMutableTreeNode addRootNode()
   {
      return addAbsoluteNode(null, "", null);
   }

   /**
    * @param parentNode
    * @param label
    * @param icon
    * @return
    */
   private static DefaultMutableTreeNode addAbsoluteNode(DefaultMutableTreeNode parentNode, String label, String icon)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      PermissionUserObject branchObject = new PermissionUserObject(childNode, label);

      childNode.setUserObject(branchObject);
      if (null == parentNode) // Root Node
      {
         branchObject.setExpanded(true);
      }
      else
      {
         parentNode.add(childNode);
      }
      if (StringUtils.isNotEmpty(icon))
      {
         branchObject.setBranchContractedIcon(icon);
         branchObject.setBranchExpandedIcon(icon);
         branchObject.setLeafIcon(icon);
      }
      return childNode;
   }

   /**
    * 
    * @param permissionId
    * @param selectedParticipants
    * @return
    */
   private boolean addUniqueParticipants(String permissionId, List<Participant> selectedParticipants)
   {
      if (permissions.hasAllGrant(permissionId))
      {
         return false;
      }
      else
      {

         boolean addSuccess = false;
         Set<ModelParticipantInfo> participants = permissions.getGrants(permissionId);

         for (Participant selectedParticipant : selectedParticipants)
         {
            boolean isExist = false;

            for (ModelParticipantInfo info : participants)
            {
               if (info instanceof QualifiedModelParticipantInfo)
               {
                  if (((QualifiedModelParticipantInfo) info).getQualifiedId().equals(
                        selectedParticipant.getQualifiedId()))
                  {
                     isExist = true;
                     break;
                  }
               }
               else
               {
                  if (info.getId().equals(selectedParticipant.getId()))
                  {
                     isExist = true;
                     break;
                  }
               }
            }

            if (!isExist)
            {
               participants.add((ModelParticipantInfo) selectedParticipant);
               addSuccess = true;
            }

         }

         permissions.setGrants(permissionId, participants);
         return addSuccess;
      }
   }

   private void buildPermissionTree()
   {
      DefaultMutableTreeNode rootTreeNode = addRootNode();
      buildGeneralPermissionTree(rootTreeNode);
      buildUiPermissionTree(rootTreeNode);
      permissionTreeModel = new DefaultTreeModel(rootTreeNode);

   }

   /**
    * @param rootTreeNode
    * @return
    */
   private DefaultMutableTreeNode buildUiPermissionTree(DefaultMutableTreeNode rootTreeNode)
   {
      DefaultMutableTreeNode uiTreeNode = addAbsoluteNode(rootTreeNode, getMessages().getString("uiParticipant"),
            UiPermissionUtils.ICON_UI_PERM);

      Map<String, IPerspectiveDefinition> perspectives = PortalUiController.getInstance().getPerspectives();

      // global elements
      Map<String, Map<String, Set<UiElement>>> globalElements = new HashMap<String, Map<String, Set<UiElement>>>();

      for (Entry<String, IPerspectiveDefinition> perspEntry : perspectives.entrySet())
      {
         IPerspectiveDefinition perspective = perspEntry.getValue();
         // add perspective node
         DefaultMutableTreeNode perspNode = addAbsoluteNode(uiTreeNode, perspective.getLabel(),
               PermissionUserObject.ICON_PERMISSION);

         // add permissions for perspective
         DefaultMutableTreeNode perspAccessNode = addPermissionNode(perspNode, perspEntry.getKey(),
               PERMISSION_TYPE.ALLOW);

         updateParticipantNodes(perspAccessNode);

         // add launch panels
         DefaultMutableTreeNode launchPanelsNode = addAbsoluteNode(perspNode, getMessages().getString(L_PANELS),
               UiPermissionUtils.ICON_LAUNCH_PANEL);

         List<LaunchPanel> launchPanels = perspective.getLaunchPanels();
         for (LaunchPanel launchPanel : launchPanels)
         {
            if (launchPanel.isGlobal())
            {
               addGlobalElement(globalElements, launchPanel, UiPermissionUtils.LAUNCH_PANEL);
            }
            else
            {
               // add launch panel node
               DefaultMutableTreeNode launchPanelNode = addAbsoluteNode(launchPanelsNode,
                     getUiElementLabel(launchPanel), null);

               // add launch panel permissions
               DefaultMutableTreeNode lPanelAccessNode = addPermissionNode(launchPanelNode, launchPanel.getName(),
                     PERMISSION_TYPE.ALLOW);
               updateParticipantNodes(lPanelAccessNode);
            }
         }

         // add view definitions
         DefaultMutableTreeNode viewsNode = addAbsoluteNode(perspNode, getMessages().getString(VIEWS),
               UiPermissionUtils.ICON_VIEW);

         List<ViewDefinition> viewDefinitions = perspective.getViews();
         for (ViewDefinition viewDefinition : viewDefinitions)
         {
            if (viewDefinition.isGlobal())
            {
               addGlobalElement(globalElements, viewDefinition, UiPermissionUtils.VIEW);
            }
            else
            {
               // add view node
               DefaultMutableTreeNode viewNode = addAbsoluteNode(viewsNode, getUiElementLabel(viewDefinition), null);

               // add view permissions
               DefaultMutableTreeNode viewAccessNode = addPermissionNode(viewNode, viewDefinition.getName(),
                     PERMISSION_TYPE.ALLOW);
               updateParticipantNodes(viewAccessNode);
            }
         }
      }

      // add global views
      DefaultMutableTreeNode globalExtNode = addAbsoluteNode(uiTreeNode, getMessages().getString(GLOBAL_EXTNS),
            UiPermissionUtils.ICON_GLOBAL_EXT);

      for (Entry<String, Map<String, Set<UiElement>>> entry : globalElements.entrySet())
      {
         // add extension node
         DefaultMutableTreeNode extensionNode = addAbsoluteNode(globalExtNode,
               UiPermissionUtils.getPermisionLabel(entry.getKey()), UiPermissionUtils.ICON_GLOBAL_EXT);

         Map<String, Set<UiElement>> elementPermissions = entry.getValue();

         // add launch panels and views if available
         for (Entry<String, Set<UiElement>> elementsEntry : elementPermissions.entrySet())
         {
            // add Launch Panel / View
            String icon = null;
            String label = elementsEntry.getKey();

            if (elementsEntry.getKey().equals(UiPermissionUtils.LAUNCH_PANEL))
            {
               icon = UiPermissionUtils.ICON_LAUNCH_PANEL;
               label = getMessages().getString(L_PANELS);
            }
            else
            {
               icon = UiPermissionUtils.ICON_VIEW;
               label = getMessages().getString(VIEWS);
            }

            DefaultMutableTreeNode elementTypeNode = addAbsoluteNode(extensionNode, label, icon);

            // add views
            Set<UiElement> elements = elementsEntry.getValue();
            for (UiElement uiElement : elements)
            {
               DefaultMutableTreeNode elementNode = addAbsoluteNode(elementTypeNode, getUiElementLabel(uiElement), null);
               DefaultMutableTreeNode accessNode = addPermissionNode(elementNode, uiElement.getName(),
                     PERMISSION_TYPE.ALLOW);
               updateParticipantNodes(accessNode);
            }
         }
      }
      return uiTreeNode;
   }

   /**
    * @param uiElementDefs
    * @param uiElement
    * @param elementType
    */
   private void addGlobalElement(Map<String, Map<String, Set<UiElement>>> uiElementDefs, UiElement uiElement,
         String elementType)
   {
      // global launch panels and views
      String extension = uiElement.getDefinedIn();

      // add extension
      if (!uiElementDefs.containsKey(extension))
      {
         uiElementDefs.put(extension, new HashMap<String, Set<UiElement>>());
      }

      Map<String, Set<UiElement>> extensionMap = uiElementDefs.get(extension);

      // add Launch panel or views map
      if (!extensionMap.containsKey(elementType))
      {
         extensionMap.put(elementType, new HashSet<UiElement>());
      }

      // add actual view definition or launch panel definition
      extensionMap.get(elementType).add(uiElement);
   }

   /**
    * @param vd
    * @return
    */
   private static String getUiElementLabel(UiElement vd)
   {
      return UiPermissionUtils.getUiElementLabel(vd);
   }

   /**
    * method to build whole Permission Tree
    */
   private DefaultMutableTreeNode buildGeneralPermissionTree(DefaultMutableTreeNode rootTreeNode)
   {
      // Create root node
      DefaultMutableTreeNode generalPermTreeNode = addAbsoluteNode(rootTreeNode, "General Permissions",
            UiPermissionUtils.ICON_GENERAL_PERM);

      try
      {
         List<String> permissionIds = new ArrayList<String>(permissions.getGeneralPermission().getAllPermissionIds());

         Collections.sort(permissionIds); // sort in natural order

         for (String permissionId : permissionIds)
         {
            // Add Permission Node

            DefaultMutableTreeNode treeNode = addPermissionNode(generalPermTreeNode, permissionId,
                  (getMessages().getString(PERMISSION_KEY + permissionId)));

            updateParticipantNodes(treeNode);
         }
      }
      catch (Exception exception)
      {
         ExceptionHandler.handleException(exception);
      }

      return generalPermTreeNode;
   }

   /**
    * @param permissionNode
    */
   private void updateParticipantNodes(DefaultMutableTreeNode permissionNode)
   {
      PermissionUserObject permissionObject = ((PermissionUserObject) permissionNode.getUserObject());
      String permissionId = permissionObject.getPermissionId();

      Set<ModelParticipantInfo> grants = permissions.getGrants(permissionObject.getPermissionId());
      permissionNode.removeAllChildren();
      
      if (permissions.hasAllGrant(permissionId))
      {
         permissionObject.setContainsAllParticipants(true);
      }
      else
      {
         permissionObject.setContainsAllParticipants(false);
      }
      
      // check is contains default participants
      if (permissions.isDefaultGrant(permissionId))
      {
         permissionObject.setContainsDefaultParticipants(true);
      }

      // check permission node is selected
      if (selectedPermissions.contains(permissionId))
      {
         permissionObject.setSelected(true);
         permissionObject.setExpanded(true);
      }

      // check permission node is modified
      if (modifiedPermissions.contains(permissionId))
      {
         permissionObject.setDirty(true);
      }

      if ((grants == null) || grants.isEmpty())
      {
         if (permissions.hasAllGrant(permissionId))
         {
            DefaultMutableTreeNode paricipantNode = addAllParticipantNode(permissionNode, permissionId);
            PermissionUserObject paricipantObject = ((PermissionUserObject) paricipantNode.getUserObject());

            paricipantObject.setLeaf(true);
            permissionObject.setContainsAllParticipants(true);
         }
         else
         {
            ((IceUserObject) permissionNode.getUserObject()).setLeaf(true);
         }
      }
      else
      {
         List<ModelParticipantInfo> grantList = new ArrayList<ModelParticipantInfo>(grants);
         Collections.sort(grantList, MODEL_PARTICIPANT_COMPARATOR);

         // add Participant node
         for (ModelParticipantInfo info : grantList)
         {
            DefaultMutableTreeNode paricipantNode = addParticipantNode(permissionNode, info, permissionId);
            ((IceUserObject) paricipantNode.getUserObject()).setLeaf(true);
         }
      }
   }

   /**
    * resets dirty nodes with latest information
    */
   private void resetDirtyNodes()
   {
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) permissionTreeModel.getRoot();
      resetDirtyNodes_(root, false);
   }

   /**
    * @param treeNode
    * @param parentDirty
    */
   private void resetDirtyNodes_(DefaultMutableTreeNode treeNode, boolean parentDirty)
   {
      int permissionNodesCount = treeNode.getChildCount();
      for (int i = 0; i < permissionNodesCount; i++)
      {
         DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) treeNode.getChildAt(i);
         PermissionUserObject userObject = (PermissionUserObject) childNode.getUserObject();

         if (NODE_TYPE.ABSOLUTE == userObject.getNodeType())
         {
            if (userObject.isDirty())
            {
               userObject.forceSetDirty(false);
               resetDirtyNodes_(childNode, true);
            }
            else
            {
               resetDirtyNodes_(childNode, false);
            }
         }
         else
         {
            if (NODE_TYPE.PERMISSION == userObject.getNodeType())
            {
               if (userObject.isDirty() || parentDirty)
               {
                  userObject.forceSetDirty(false);
                  updateParticipantNodes(childNode);
               }
            }
         }
      }
   }

   /**
    * @param type
    * @return
    */
   private List<PermissionUserObject> findSelectedNodes(NODE_TYPE type)
   {
      List<PermissionUserObject> selectedNodes = new ArrayList<PermissionUserObject>();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) permissionTreeModel.getRoot();
      findSelectedNodes_(selectedNodes, type, root);
      return selectedNodes;
   }

   /**
    * @param selectedNodes
    * @param type
    * @param treeNode
    */
   private void findSelectedNodes_(List<PermissionUserObject> selectedNodes, NODE_TYPE type,
         DefaultMutableTreeNode treeNode)
   {
      int permissionNodesCount = treeNode.getChildCount();
      for (int i = 0; i < permissionNodesCount; i++)
      {
         DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) treeNode.getChildAt(i);
         PermissionUserObject userObject = (PermissionUserObject) childNode.getUserObject();

         if (NODE_TYPE.ABSOLUTE == userObject.getNodeType())
         {
            findSelectedNodes_(selectedNodes, type, childNode);
         }
         else
         {
            if (NODE_TYPE.PERMISSION != type && NODE_TYPE.PERMISSION == userObject.getNodeType())
            {
               findSelectedNodes_(selectedNodes, type, childNode);
            }
            else
            {
               DefaultMutableTreeNode permissionNode = (DefaultMutableTreeNode) treeNode.getChildAt(i);
               PermissionUserObject userObject1 = (PermissionUserObject) permissionNode.getUserObject();

               if (userObject1.isSelected())
               {
                  selectedNodes.add(userObject1);
               }
            }
         }
      }
   }

   /**
    * method to remove partcipant
    * 
    * @param permissionId
    * @param participantId
    */
   private boolean removeParticipant(String permissionId, String participantId)
   {
      boolean removeSuccess = false;

      if (ALL_PARTICIPANT.equals(participantId))
      {
         permissions.setGrants(permissionId, new HashSet<ModelParticipantInfo>());
         removeSuccess = true;
      }
      else
      {
         Set<ModelParticipantInfo> paricipants = permissions.getGrants(permissionId);

         for (Iterator<ModelParticipantInfo> itr = paricipants.iterator(); itr.hasNext();)
         {
            ModelParticipantInfo info = itr.next();

            if (info.getId().equals(participantId))
            {
               itr.remove();
               removeSuccess = true;

               break;
            }
         }

         if (removeSuccess)
         {
            permissions.setGrants(permissionId, paricipants.isEmpty() ? null : paricipants);
         }
      }

      return removeSuccess;
   }

   /**
    * 
    */
   public boolean accept()
   {
      authMngrConfirmationDialog = null;
      confirmYes();
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      authMngrConfirmationDialog = null;
      confirmCancel();
      return true;
   }

   /**
     *
     */
   public static enum CONTEXT_ACTION {
      ADD_ALL_PARTICIPANT, ADD_PARTICIPANT, COPY, PASTE, REMOVE_PARTICIPANT, RESTORE_DEFAULT_PARTICIPANT;
   }

   /**
     *
     */
   public static enum VIEW_TYPE {
      PARTICIPANT, PERMISSION;
   }

}

/**
 * Participant Comparator to compare by names
 */
class ModelParticipantComparator implements Comparator<ModelParticipantInfo>
{
   public int compare(ModelParticipantInfo part1, ModelParticipantInfo part2)
   {
      return part1.getName().compareTo(part2.getName());
   }
}
