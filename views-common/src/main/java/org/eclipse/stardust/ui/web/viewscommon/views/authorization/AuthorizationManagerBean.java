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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.RuntimePermissions;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.authorization.PermissionUserObject.NODE_TYPE;

import com.icesoft.faces.component.dragdrop.DndEvent;
import com.icesoft.faces.component.dragdrop.DragEvent;
import com.icesoft.faces.component.tree.IceUserObject;


/**
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class AuthorizationManagerBean extends PopupUIComponentBean
      implements ViewEventHandler, ICallbackHandler, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private static final String PERMISSION_KEY = "permission.model.";
   private static final String ALL_PARTICIPANT = "All";
   private static final ModelParticipantComparator MODEL_PARTICIPANT_COMPARATOR = new ModelParticipantComparator();
   private final AdministrationService administrationService;
   private DefaultTreeModel permissionTreeModel;
   private final ParticipantHelper participantHelper;
   private final PermissionPickList permissionPickList;
   private final SelectItem[] viewSelection = new SelectItem[2];
   private RuntimePermissions runtimePermissions;
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

         int count = root.getChildCount();

         for (int i = 0; i < count; i++)
         {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            PermissionUserObject userObject = (PermissionUserObject) node.getUserObject();

            if (userObject.isSelected())
            {
               boolean isAdded = addUniqueParticipants(userObject.getPermissionId(), selectedParticipants);

               if (isAdded)
               {
                  userObject.setDirty(isAdded);
                  modifiedPermissions.add(userObject.getPermissionId());
               }
            }
         }
      }

      buildPermissionTree();
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
            runtimePermissions.setGrants(userObject.getPermissionId(), null);
            userObject.setContainsDefaultParticipants(true);
            userObject.setDirty(true);
            modifiedPermissions.add(userObject.getPermissionId());
         }

         buildPermissionTree();
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
         }

         buildPermissionTree();
      }
      else if (CONTEXT_ACTION.ADD_ALL_PARTICIPANT.name().equals(actionType))
      {
         nodeUserObject.setSelected(true);
         selectedPermissions.add(nodeUserObject.getPermissionId());

         List<PermissionUserObject> selectedPermissions = findSelectedNodes(NODE_TYPE.PERMISSION);

         for (PermissionUserObject userObject : selectedPermissions)
         {
            runtimePermissions.setAllGrant(userObject.getPermissionId());
            userObject.setSelected(false);
            modifiedPermissions.add(userObject.getPermissionId());
            nodeUserObject.setContainsAllParticipants(true);
         }

         buildPermissionTree();
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
               runtimePermissions.setAllGrant(permissionId);
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

         buildPermissionTree();
         pasteActionEnable = false;
      }
   }

   /**
    * method to listen drag events
    * 
    * @param dragEvent
    */
   public void dragObjectListener(DragEvent dragEvent)
   {
      try
      {
         Object dragObject = dragEvent.getTargetDragValue();
         Object dropObject = dragEvent.getTargetDropValue();

         if (dragEvent.getEventType() == DndEvent.DROPPED)
         {
         }
      }
      catch (Exception exception)
      {
         MessageDialog.addErrorMessage(exception);
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
    * org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(com.sungard.framework
    * .ui.event.ViewEvent)
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
         runtimePermissions = administrationService.getGlobalPermissions();
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
            administrationService.setGlobalPermissions(runtimePermissions);
            runtimePermissions = administrationService.getGlobalPermissions();
            modifiedPermissions.clear();
            buildPermissionTree();

            // update();
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
         runtimePermissions = administrationService.getGlobalPermissions();
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
    * @return
    */
   private static DefaultMutableTreeNode addPermissionNode(DefaultMutableTreeNode parentNode, String permissionId,
         String permissionLabel)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      PermissionUserObject branchObject = new PermissionUserObject(childNode, permissionId, permissionLabel);
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
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      PermissionUserObject branchObject = new PermissionUserObject();
      childNode.setUserObject(branchObject);

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
      if (runtimePermissions.hasAllGrant(permissionId))
      {
         return false;
      }
      else
      {

         boolean addSuccess = false;
         Set<ModelParticipantInfo> participants = runtimePermissions.getGrants(permissionId);

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

         runtimePermissions.setGrants(permissionId, participants);
         return addSuccess;
      }
   }

   /**
    * method to build whole Permission Tree
    */
   private void buildPermissionTree()
   {
      try
      {
         // Create root node
         DefaultMutableTreeNode rootTreeNode = addRootNode();

         List<String> permissionIds = new ArrayList<String>(runtimePermissions.getAllPermissionIds());       
         
         Collections.sort(permissionIds); // sort in natural order

         for (String permissionId : permissionIds)
         {
            // Add Permission Node
            DefaultMutableTreeNode treeNode = addPermissionNode(rootTreeNode, permissionId,
                  (getMessages().getString(PERMISSION_KEY + permissionId)));

            PermissionUserObject permissionObject = ((PermissionUserObject) treeNode.getUserObject());

            Set<ModelParticipantInfo> grants = runtimePermissions.getGrants(permissionId);

            // check is contains default participants
            if (runtimePermissions.isDefaultGrant(permissionId))
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
               if (runtimePermissions.hasAllGrant(permissionId))
               {
                  DefaultMutableTreeNode paricipantNode = addAllParticipantNode(treeNode, permissionId);
                  PermissionUserObject paricipantObject = ((PermissionUserObject) paricipantNode.getUserObject());

                  paricipantObject.setLeaf(true);
                  permissionObject.setContainsAllParticipants(true);
               }
               else
               {
                  ((IceUserObject) treeNode.getUserObject()).setLeaf(true);
               }
            }
            else
            {
               List<ModelParticipantInfo> grantList = new ArrayList<ModelParticipantInfo>(grants);
               Collections.sort(grantList, MODEL_PARTICIPANT_COMPARATOR);

               // add Participant node
               for (ModelParticipantInfo info : grantList)
               {
                  DefaultMutableTreeNode paricipantNode = addParticipantNode(treeNode, info, permissionId);
                  ((IceUserObject) paricipantNode.getUserObject()).setLeaf(true);
               }
            }
         }

         permissionTreeModel = new DefaultTreeModel(rootTreeNode);
      }
      catch (Exception exception)
      {
         MessageDialog.addErrorMessage(exception);
      }
   }

   /*
     *
     */
   private List<PermissionUserObject> findSelectedNodes(NODE_TYPE type)
   {
      List<PermissionUserObject> selectedNodes = new ArrayList<PermissionUserObject>();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) permissionTreeModel.getRoot();
      int permissionNodesCount = root.getChildCount();

      if (PermissionUserObject.NODE_TYPE.PERMISSION.equals(type))
      {
         for (int i = 0; i < permissionNodesCount; i++)
         {
            DefaultMutableTreeNode permissionNode = (DefaultMutableTreeNode) root.getChildAt(i);
            PermissionUserObject userObject = (PermissionUserObject) permissionNode.getUserObject();

            if (userObject.isSelected())
            {
               selectedNodes.add(userObject);
            }
         }
      }
      else
      {
         for (int i = 0; i < permissionNodesCount; i++)
         {
            DefaultMutableTreeNode permissionNode = (DefaultMutableTreeNode) root.getChildAt(i);
            int participantCount = permissionNode.getChildCount();

            for (int j = 0; j < participantCount; j++)
            {
               DefaultMutableTreeNode participantNode = (DefaultMutableTreeNode) permissionNode.getChildAt(j);
               PermissionUserObject userObject = (PermissionUserObject) participantNode.getUserObject();

               if (userObject.isSelected())
               {
                  selectedNodes.add(userObject);
               }
            }
         }
      }

      return selectedNodes;
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
         runtimePermissions.setGrants(permissionId, null);
         removeSuccess = true;
      }
      else
      {
         Set<ModelParticipantInfo> paricipants = runtimePermissions.getGrants(permissionId);

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
            runtimePermissions.setGrants(permissionId, paricipants.isEmpty() ? null : paricipants);
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

