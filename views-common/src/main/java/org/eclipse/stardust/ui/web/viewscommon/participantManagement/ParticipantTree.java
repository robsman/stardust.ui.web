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
package org.eclipse.stardust.ui.web.viewscommon.participantManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.DynamicParticipantInfo;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedOrganizationInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserGroupQuery;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.InfoPanelBean;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataFilterOnOff;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.CallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.PanelConfirmation;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.participantManagement.ParticipantUserObject.NODE_TYPE;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantItem;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;


import com.icesoft.faces.component.dragdrop.DndEvent;
import com.icesoft.faces.component.dragdrop.DragEvent;
import com.icesoft.faces.component.tree.Tree;

/**
 * @author anair
 * @version $Revision: $
 */
public class ParticipantTree
{
   private static final long serialVersionUID = 992994543928984490L;
   private static final Logger trace = LogManager.getLogger(ParticipantTree.class);
   private static final int HIGHLIGHT_STYLES_MAX = 12;
   private DefaultTreeModel treeModel;
   private static final String HIGHLIGHT_USERS_ACTION = "highlightUsers"; 
   private static final String SHOW_MODEL_ACTION = "showModel";
   private static final String SEARCH_PARTICIPANT = "SearchParticipant";

   private Map<Long, Department> departmentCache = CollectionUtils.newMap();

   // Used to store list of model tree nodes with model id (key)
   private Map<String, DefaultMutableTreeNode> modelNodesMap;
   
   // Used to store list of top level participant tree nodes per model id (key)
   private Map<String, List<DefaultMutableTreeNode>> topLevelParticipantsByModelMap;
   
   private Map<String, GenericDataFilterOnOff> onOffFilters;
   
   //holds the references to all highlighted participantUserObjects
   private List<ParticipantUserObject> highlightedParticipantUserObjects = new ArrayList<ParticipantUserObject>();
   
   //user highlight - style index
   private int highlightStyleIndex = 1;
   
   //hold the list of highlighted user (unique)
   private Set<String> highlightedUsers = new HashSet<String>();
   
   /**
    * Nodes to be highlighted.
    * These are the participants selected in the user table.
    */
   private Set<User> selectedUsers;
   
   private ParticipantUserObject selectedUserObject;

   private boolean showUserNodes = true;

   private boolean showUserGroupNodes = true;

   private boolean highlightUserFilterEnabled = true;

   private boolean filterPredefniedModelNodes = true;

   private boolean participantTreeUpdated;

   private Map<String, List<String>> referringModels = new HashMap<String, List<String>>();
   
   private InfoPanelBean infoPanelBean;

   public ParticipantTree()
   {
      //super(view);
      modelNodesMap = new LinkedHashMap<String, DefaultMutableTreeNode>();
      topLevelParticipantsByModelMap = new LinkedHashMap<String, List<DefaultMutableTreeNode>>();
   }

   /**
    *
    */
   public void initialize()
   {
      // As per Filter Toolbar creating Data Filters
      createFilterToolbar();
      treeModel = null;
      departmentCache.clear();
      modelNodesMap.clear();
      topLevelParticipantsByModelMap.clear();
      refreshTreeModel();
      highlightAllSelectedUsers();
      selectedUsers = new HashSet<User>();
      infoPanelBean = new InfoPanelBean();
      participantTreeUpdated = false;
   }
   
   public void applyFilter(ActionEvent event)
   {
      UICommand commandObject = (UICommand) event.getComponent();
      Map<String, Object> attributesMap = commandObject.getAttributes();
      String actionName = (String) attributesMap.get("name");

      GenericDataFilterOnOff selectedDataFilter = onOffFilters.get(actionName);

      if (HIGHLIGHT_USERS_ACTION.equals(actionName))
      {
         selectedDataFilter.toggle();
         if (!selectedDataFilter.isOn())
         {
            removeHighlighting();
         }
         else
         {
            highlightAllSelectedUsers();
         }
         
      }
      else if (SHOW_MODEL_ACTION.equals(actionName))
      {
         DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();

         if (selectedDataFilter.isOn())
         {
            hideModelNodes(rootNode);
         }
         else
         {
            showModelNodes(rootNode);
         }
         selectedDataFilter.toggle();
      }
      else if (SEARCH_PARTICIPANT.equals(actionName))
      {
         //TODO: to be implemented later
      }
   }
   
   /**
    * @param event
    */
   public void nodeExpandCollapse(ActionEvent event)
   {
      Tree tree = (Tree) event.getComponent();

      if (tree.getNavigationEventType().equals(Tree.NAVIGATION_EVENT_EXPAND))
      {
         if (tree.getNavigatedNode().isRoot())
         {
            expandRootNode();
         }
         else
         {
            loadChildNodes(tree.getNavigatedNode());
         }
      }
      else if (tree.getNavigationEventType().equals(Tree.NAVIGATION_EVENT_COLLAPSE))
      {
         // NOP
      }
   }

   public void nodeClicked(ActionEvent event)
   {
      ParticipantUserObject userObj = (ParticipantUserObject) event.getComponent().getAttributes().get("userObject");
      if (null != userObj)
      {
         toggleSelection(userObj);
         resetPreviousSelection();
         if (userObj.isSelected())
         {
            selectedUserObject = userObj;
         }
      }
   }

   /**
    * 
    */
   public void resetPreviousSelection()
   {
      if (null != selectedUserObject)
      {
         selectedUserObject.setSelected(false);
         selectedUserObject = null;
      }
   }

   /**
    * 
    */
   private void toggleSelection(ParticipantUserObject userObj)
   {
      if (null != userObj)
      {
         userObj.setSelected(!userObj.isSelected());
      }
   }

   /**
    * 
    */
   private void refreshTreeModel()
   {
      // Create root node
      DefaultMutableTreeNode rootTreeNode = addRootParticipantNode();

      addTopLevelNodes(rootTreeNode);

      treeModel = new DefaultTreeModel(rootTreeNode);
   }

   /**
    * @param root
    */
   private void addTopLevelNodes(DefaultMutableTreeNode root)
   {
      topLevelParticipantsByModelMap.clear();
      
      // Add all top-level Organizations
      addTopLevelOrganizations(root);

      // Add all top-level Roles
      addTopLevelRoles(root);

      // Add all UserGroups
      if (showUserGroupNodes)
      {
         addAllUserGroups(root);
      }

      if (isModelsDisplayed())
      {
         showModelNodes(root);
      }
   }

   /*
    * Methods to expand various types of Participant nodes
    */

   /**
    * @param node
    * @param qualifiedOrganizationInfo
    */
   private void expandOrganizationNode(DefaultMutableTreeNode node, QualifiedOrganizationInfo qualifiedOrganizationInfo)
   {
      // Add all associated Users
      addUsersForParticipant(node, qualifiedOrganizationInfo);

      // Add all sub-Organizations
      addSubOrganizations(node, qualifiedOrganizationInfo);

      // Add all sub-Roles
      addSubRoles(node, qualifiedOrganizationInfo);
   }

   /**
    * @param node
    * @param qualifiedOrganizationInfo
    */
   private void expandExplicitlyScopedOrganizationNode(DefaultMutableTreeNode node,
         QualifiedOrganizationInfo qualifiedOrganizationInfo)
   {
      QueryService qs = getQryService();
      List<Department> deptList = qs.findAllDepartments(qualifiedOrganizationInfo.getDepartment(),
            qualifiedOrganizationInfo);

      // Add Default Department
      addDefaultDepartmentNode(node, qualifiedOrganizationInfo);

      // Add all Departments
      for (Department department : deptList)
      {
         departmentCache.put(department.getOID(), department);
         // Add Department node
         addDepartmentNode(node, department);
      }
   }

   /**
    * @param node
    * @param department
    */
   private void expandDepartmentNode(DefaultMutableTreeNode node, Department department)
   {
      QualifiedModelParticipantInfo scopedOrganizationInfo = department.getScopedParticipant(department
            .getOrganization());

      // Add all associated Users
      addUsersForParticipant(node, scopedOrganizationInfo);

      // Add all sub-Organizations
      addSubOrganizations(node, (QualifiedOrganizationInfo) scopedOrganizationInfo);

      // Add all sub-Roles
      addSubRoles(node, (QualifiedOrganizationInfo) scopedOrganizationInfo);
   }

   /**
    * @param node
    * @param qualifiedOrganizationInfo
    */
   private void expandDefaultDepartmentNode(DefaultMutableTreeNode node,
         QualifiedOrganizationInfo qualifiedOrganizationInfo)
   {
      Department department = getDepartment(qualifiedOrganizationInfo.getDepartment());

      // Add all associated Users
      addUsersForParticipant(node, qualifiedOrganizationInfo);

      // Add all sub-Organizations
      @SuppressWarnings("unchecked")
      List<Organization> subOrganizations = ((Organization) ParticipantUtils.getParticipant(qualifiedOrganizationInfo))
            .getAllSubOrganizations();
      for (Organization subOrganization : subOrganizations)
      {
         addDefaultDepartmentNode(node, (QualifiedOrganizationInfo) ParticipantUtils.getScopedParticipant(
               subOrganization, department));
      }

      // Add all sub-Roles
      addSubRoles(node, qualifiedOrganizationInfo);
   }

   /**
    * @param node
    */
   private void addModels(DefaultMutableTreeNode node)
   {
      modelNodesMap.clear();
      DefaultMutableTreeNode modelNode = null;
      int index = 0;

      List<DeployedModel> models = ModelUtils.getActiveModels();
      for (Model model : models)
      {
         if (!filterModelNodes(model))
         {
            // Add the model nodes to the root node starting at position 0
            modelNode = addModelNode(node, model, index++);
            modelNodesMap.put(model.getId(), modelNode);
         }
      }
   }

   /**
    * @param node
    */
   @SuppressWarnings("unchecked")
   private void addTopLevelOrganizations(DefaultMutableTreeNode node)
   {
      DefaultMutableTreeNode orgNode = null;
      List<Organization> topLevelOrganizations = null;
      
      List<DeployedModel> models = ModelUtils.getActiveModels();
      for (Model model : models)
      {
         if (!filterModelNodes(model))
         {
            topLevelOrganizations = model.getAllTopLevelOrganizations();
            for (Organization organization : topLevelOrganizations)
            {
               String modelId = ModelUtils.extractModelId(organization.getQualifiedId());
               if (modelId.equals(model.getId()))
               {
                  orgNode = addParticipantNode(node, organization);
                  addToTopLevelParticipantsMap(model.getId(), orgNode);
               }
            }
         }
      }
   }

   /**
    * @param node
    */
   @SuppressWarnings("unchecked")
   private void addTopLevelRoles(DefaultMutableTreeNode node)
   {
      DefaultMutableTreeNode roleNode = null;
      List<Role> topLevelRoles = null;
      boolean adminRoleAdded = false;
      List<DeployedModel> models = ModelUtils.getActiveModels();
      for (Model model : models)
      {
         topLevelRoles = model.getAllTopLevelRoles();
         for (Role role : topLevelRoles)
         {
            // We need to only add the first occurrence of the "Administrator" role 
            if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(role.getId()))
            {
               if (!adminRoleAdded)
               {
                  adminRoleAdded = true;
               }
               else
               {
                  // If "Administrator" role has already been added, skip this element
                  continue;
               }
            }

            if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(role.getId()) || !filterModelNodes(model))
            {
               String modelId = !PredefinedConstants.ADMINISTRATOR_ROLE.equals(role.getId()) ? ModelUtils
                     .extractModelId(role.getQualifiedId()) : null;
               if((modelId == null) || (modelId.equals(model.getId())))
               {
                  roleNode = addParticipantNode(node, role);
                  // Add non-Administrator top-level role nodes to model-participant Map
                  if (!PredefinedConstants.ADMINISTRATOR_ROLE.equals(role.getId()))
                  {
                     addToTopLevelParticipantsMap(model.getId(), roleNode);
                  }
               }
            }
         }
      }
   }

   /**
    * @param node
    */
   private void addAllUserGroups(DefaultMutableTreeNode node)
   {
      UserGroupQuery userGroupQuery = UserGroupQuery.findAll();
      List<UserGroup> allUserGroups = getQryService().getAllUserGroups(userGroupQuery);
      for (UserGroup userGroup : allUserGroups)
      {
         addParticipantNode(node, userGroup);
      }
   }

   /**
    * @param node
    * @param qualifiedOrganizationInfo
    */
   private void addSubOrganizations(DefaultMutableTreeNode node, QualifiedOrganizationInfo qualifiedOrganizationInfo)
   {
      Department department = getDepartment(qualifiedOrganizationInfo.getDepartment());
      Organization organization = (Organization) ParticipantUtils.getParticipant(qualifiedOrganizationInfo);

      @SuppressWarnings("unchecked")
      List<Organization> subOrganizations = organization.getAllSubOrganizations();
      for (Organization subOrganization : subOrganizations)
      {
         addParticipantNode(node, ParticipantUtils.getScopedParticipant(subOrganization, department));
      }
   }

   /**
    * @param node
    * @param qualifiedOrganizationInfo
    */
   private void addSubRoles(DefaultMutableTreeNode node, QualifiedOrganizationInfo qualifiedOrganizationInfo)
   {
      Department department = getDepartment(qualifiedOrganizationInfo.getDepartment());
      Organization organization = (Organization) ParticipantUtils.getParticipant(qualifiedOrganizationInfo);

      @SuppressWarnings("unchecked")
      List<Role> subRoles = organization.getAllSubRoles();
      for (Role subRole : subRoles)
      {
         addParticipantNode(node, ParticipantUtils.getScopedParticipant(subRole, department));
      }
   }

   /**
    * @param node
    * @param participantInfo
    */
   private void addUsersForParticipant(DefaultMutableTreeNode node, ParticipantInfo participantInfo)
   {
      if (showUserNodes)
      {
         UserQuery userQuery = UserQuery.findAll();
         userQuery.getFilter().add(ParticipantAssociationFilter.forParticipant(participantInfo, false));
         UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Full);
         userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
         userQuery.setPolicy(userPolicy);
         Users allUsers = getQryService().getAllUsers(userQuery);
         int index = 0;
         for (User user : allUsers)
         {
            addParticipantNode(node, user, index++);
         }
      }
   }
   /**
    * @param modelId
    * @param node
    */
   private void addToTopLevelParticipantsMap(String modelId, DefaultMutableTreeNode node)
   {
      List<DefaultMutableTreeNode> topLevelParticipantNodes = topLevelParticipantsByModelMap.get(modelId);
      if (null == topLevelParticipantNodes)
      {
         topLevelParticipantNodes = CollectionUtils.newList();
      }
      topLevelParticipantNodes.add(node);
      topLevelParticipantsByModelMap.put(modelId, topLevelParticipantNodes);
   }

   /*
    * Methods to add Tree nodes of different types
    */

   /**
    * @return
    */
   private DefaultMutableTreeNode addRootParticipantNode()
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      ParticipantUserObject branchObject = new ParticipantUserObject(this);

      childNode.setUserObject(branchObject);

      return childNode;
   }

   /**
    * @param parentNode
    * @param model
    * @return
    */
   private DefaultMutableTreeNode addModelNode(DefaultMutableTreeNode parentNode, Model model, int index)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      ParticipantUserObject branchObject = new ParticipantUserObject(childNode, model, this);

      childNode.setUserObject(branchObject);
      if (parentNode != null)
      {
         parentNode.insert(childNode, index);
      }

      return childNode;
   }

   /**
    * @param parentNode
    * @param qualifiedParticipantInfo
    * @return
    */
   private DefaultMutableTreeNode addParticipantNode(DefaultMutableTreeNode parentNode,
         QualifiedModelParticipantInfo qualifiedParticipantInfo)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      ParticipantUserObject branchObject = new ParticipantUserObject(childNode, qualifiedParticipantInfo, this);

      childNode.setUserObject(branchObject);
      if (parentNode != null)
      {
         parentNode.add(childNode);
      }

      return childNode;
   }

   
   /**
    * @param parentNode
    * @param dynamicParticipantInfo
    * @return
    */
   private DefaultMutableTreeNode addParticipantNode(DefaultMutableTreeNode parentNode,
         DynamicParticipantInfo dynamicParticipantInfo)
   {
      return addParticipantNode(parentNode, dynamicParticipantInfo, null);
   }
   
   
   /**
    *  helps to organize the child user nodes before other type of nodes
    * @param parentNode
    * @param dynamicParticipantInfo
    * @param childIndex
    * @return
    */
   private DefaultMutableTreeNode addParticipantNode(DefaultMutableTreeNode parentNode,
         DynamicParticipantInfo dynamicParticipantInfo, Integer childIndex)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      ParticipantUserObject branchObject = new ParticipantUserObject(childNode, dynamicParticipantInfo, this);

      childNode.setUserObject(branchObject);
      if (parentNode != null)
      {
         if (null != childIndex)
         {
            parentNode.insert(childNode, childIndex);
         }
         else
         {
            parentNode.add(childNode);
         }
      }
      return childNode;
   }

   /**
    * @param parentNode
    * @param department
    * @return
    */
   private DefaultMutableTreeNode addDepartmentNode(DefaultMutableTreeNode parentNode, Department department)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      ParticipantUserObject branchObject = new ParticipantUserObject(childNode, department, this);

      childNode.setUserObject(branchObject);
      if (parentNode != null)
      {
         parentNode.add(childNode);
      }

      return childNode;
   }

   /**
    * @param parentNode
    * @param qualifiedOrganizationInfo
    * @return
    */
   private DefaultMutableTreeNode addDefaultDepartmentNode(DefaultMutableTreeNode parentNode,
         QualifiedOrganizationInfo qualifiedOrganizationInfo)
   {
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
      ParticipantUserObject branchObject = new ParticipantUserObject(childNode, qualifiedOrganizationInfo, true, this);

      childNode.setUserObject(branchObject);
      if (parentNode != null)
      {
         parentNode.add(childNode);
      }

      return childNode;
   }

   /**
    * @param treeNode
    */
   private void removeParticipantNode(DefaultMutableTreeNode treeNode)
   {
      treeNode.removeFromParent();
   }

   /**
    * Utility method to find Tree node by UUID
    * 
    * @param uuid
    * @return
    */
   private DefaultMutableTreeNode findTreeNodeByUuid(UUID uuid)
   {
      DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
      DefaultMutableTreeNode node;
      ParticipantUserObject participantUserObject;

      @SuppressWarnings("unchecked")
      Enumeration<DefaultMutableTreeNode> nodes = rootNode.depthFirstEnumeration();
      while (nodes.hasMoreElements())
      {
         node = nodes.nextElement();
         participantUserObject = (ParticipantUserObject) node.getUserObject();
         if (participantUserObject.getUuid().equals(uuid))
         {
            return node;
         }
      }
      return null;
   }


   /**
    * Methods to add Users to Participants (via Drag-n-Drop)
    * @param dragEvent
    */
   public void dragObjectListener(DragEvent dragEvent)
   {
      try
      {
         Object dragObject = dragEvent.getTargetDragValue();
         Object dropObject = dragEvent.getTargetDropValue();

         if (trace.isDebugEnabled())
         {
            trace.debug("DragEvent: " + DragEvent.getEventName(dragEvent.getEventType()) + " TargetDragValue: "
                  + dragObject + " TargetDropValue: " + dropObject + " ClientId: " + dragEvent.getTargetClientId());
         }

         if (dragEvent.getEventType() == DndEvent.DROPPED)
         {
            if ((dragObject instanceof User) && (dropObject instanceof ParticipantUserObject))
            {
               addUserToParticipant(dragEvent);
            }
            else if (!(dragObject instanceof User))
            {
               trace.debug("Only User objects can be dropped onto the Participant Tree");
            }
            else if (!(dropObject instanceof ParticipantUserObject))
            {
               trace.debug("Users can be dropped on Participant nodes only");
            }
         }

      }
      catch (Exception exception)
      {
         ExceptionHandler.handleException(exception);
      }
   }

   public void notifyParticipantTreeUpdate()
   {
      // If alert message is alread visible, no need to recreate the notify message
      if (!participantTreeUpdated)
      {
         infoPanelBean.setNotificationMsg(MessagesViewsCommonBean.getInstance().getString(
               "views.participantTree.toolbar.highlightUsers.alertMsg"));
         participantTreeUpdated = true;
      }
   }
   /**
    * @param dragEvent
    */
   private void addUserToParticipant(DragEvent dragEvent)
   {
      Object dragObject = dragEvent.getTargetDragValue();
      Object dropObject = dragEvent.getTargetDropValue();
      boolean userGrantsChanged = false;
      if (!(dragObject instanceof User) || !(dropObject instanceof ParticipantUserObject))
      {
         trace.debug("Invalid drag / drop object");
         return;
      }

      User user = (User) dragObject;
      ParticipantUserObject participantUserObject = (ParticipantUserObject) dropObject;
      DefaultMutableTreeNode participantNode = participantUserObject.getWrapper();

      switch (participantUserObject.getNodeType())
      {
      case ORGANIZATION_UNSCOPED:
      case ORGANIZATON_SCOPED_IMPLICIT:
      case DEPARTMENT_DEFAULT:
      case ROLE_UNSCOPED:
      case ROLE_SCOPED:
         addUserToModelParticipant(user, participantUserObject.getQualifiedModelParticipantInfo());
         userGrantsChanged = true;
         break;

      case USERGROUP:
         addUserToUserGroup(user, participantUserObject.getUserGroup());
         userGrantsChanged = true;
         break;

      case DEPARTMENT:
         Department department = participantUserObject.getDepartment();
         QualifiedModelParticipantInfo qualifiedParticipantInfo = department.getScopedParticipant(department
               .getOrganization());
         addUserToModelParticipant(user, qualifiedParticipantInfo);
         userGrantsChanged = true;
         break;

      default:
         trace.debug("Invalid DropTarget");
         return;
      }
      participantUserObject.setExpanded(true);
      refreshParticipantNode(participantNode, NODE_TYPE.USER);
      if(userGrantsChanged && UserUtils.isLoggedInUser(user))
      {
         notifyParticipantTreeUpdate();
      }
   }

   /**
    * @param user
    * @param qualifiedParticipantInfo
    */
   private void addUserToModelParticipant(User user, QualifiedModelParticipantInfo qualifiedParticipantInfo)
   {
      UserService userService = getUserService();

      User userToModify = userService.getUser(user.getOID());
      userToModify.addGrant(qualifiedParticipantInfo);
      userService.modifyUser(userToModify);
   }

   /**
    * @param user
    * @param userGroup
    */
   private void addUserToUserGroup(User user, UserGroup userGroup)
   {
      UserService userService = getUserService();

      User updatedUser = userService.getUser(user.getOID());
      updatedUser.joinGroup(userGroup.getId());
      userService.modifyUser(updatedUser);
   }

   /*
    * Methods to remove Users from Participants (via right click Context Menu)
    */

   /**
    * 
    */
   public void removeUserFromParticipant()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      String uuid = (String) context.getExternalContext().getRequestParameterMap().get("uuid");
      DefaultMutableTreeNode participantNode = findTreeNodeByUuid(UUID.fromString(uuid));
      ParticipantUserObject participantUserObject = null;
      if (participantNode != null)
      {
         participantUserObject = (ParticipantUserObject) participantNode.getUserObject();
      }
      else
      {
         // TODO: Add some logging here
         return;
      }
      User user = (User) participantUserObject.getDynamicParticipantInfo();

      TreeNode node = participantNode.getParent();
      if (node instanceof DefaultMutableTreeNode)
      {
         DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) participantNode.getParent();
         if (parentNode.getUserObject() instanceof ParticipantUserObject)
         {
            ParticipantUserObject parentParticipantUserObject = (ParticipantUserObject) parentNode.getUserObject();

            switch (parentParticipantUserObject.getNodeType())
            {
            case ORGANIZATION_UNSCOPED:
            case ORGANIZATON_SCOPED_IMPLICIT:
            case DEPARTMENT_DEFAULT:
            case ROLE_UNSCOPED:
            case ROLE_SCOPED:
               removeUserFromModelParticipant(user, parentParticipantUserObject.getQualifiedModelParticipantInfo());
               break;

            case USERGROUP:
               removeUserFromUserGroup(user, parentParticipantUserObject.getUserGroup());
               break;

            case DEPARTMENT:
               Department department = parentParticipantUserObject.getDepartment();
               removeUserFromModelParticipant(user, department.getScopedParticipant(department.getOrganization()));
               break;
            }
            refreshParticipantNode(parentNode, NODE_TYPE.USER);
            // If user is currently logged in User, notify to re-login
            if (UserUtils.isLoggedInUser(user))
            {
               notifyParticipantTreeUpdate();
            }
         }
      }
   }

   /**
    * @param user
    * @param qualifiedParticipantInfo
    */
   private void removeUserFromModelParticipant(User user, QualifiedModelParticipantInfo qualifiedParticipantInfo)
   {
      UserService userService = getUserService();
      User userToModify = userService.getUser(user.getOID());
      userToModify.removeGrant(qualifiedParticipantInfo);
      userService.modifyUser(userToModify);
   }

   /**
    * @param user
    * @param userGroup
    */
   private void removeUserFromUserGroup(User user, UserGroup userGroup)
   {
      UserService userService = getUserService();

      User userToModify = userService.getUser(user.getOID());
      userToModify.leaveGroup(userGroup.getId());
      userService.modifyUser(userToModify);
   }

   /**
    * Refresh participant tree without collapsing nodes
    */
   public void refresh()
   {
      refreshParticipantNode((DefaultMutableTreeNode) treeModel.getRoot(),
            EnumSet.of(NODE_TYPE.USER, NODE_TYPE.DEPARTMENT, NODE_TYPE.DEPARTMENT_DEFAULT));
   }

   /**
    * @param node
    */
   public void refreshParticipantNode(DefaultMutableTreeNode node, NODE_TYPE nodeType)
   {
      refreshParticipantNode(node, EnumSet.of(nodeType));
   }

   /**
    * @param node
    * @param nodeTypes
    */
   public void refreshParticipantNode(DefaultMutableTreeNode node, Set<NODE_TYPE> nodeTypes)
   {
      List<ParticipantUserObject> expandedNodeUserObjects = new ArrayList<ParticipantUserObject>();
      populateExpandedNodeList(node, expandedNodeUserObjects);
      refreshParticipantNodeRecursively(node, nodeTypes, expandedNodeUserObjects);
      highlightAllSelectedUsers();
   }

   
   /**
    * @param ae
    */
   public void deleteDepartment(ActionEvent ae)
   {
      UUID uuid = (UUID) ((UIComponent) ae.getSource()).getAttributes().get("uuid");
      if (null != uuid)
      {
         // create callback handler
         CallbackHandler callbackHandler = new CallbackHandler(uuid)
         {
            public void handleEvent(EventType eventType)
            {
               try
               {
                  deleteDepartment(getPayload());
               }
               catch (PortalException e)
               {
                  ExceptionHandler.handleException(e);
               }
            }
         };
         // create panelpopup
         PanelConfirmation panelConfirmation = PanelConfirmation.getInstance(true);
         panelConfirmation.setCallbackHandler(callbackHandler);
         panelConfirmation.setMessage(MessagesViewsCommonBean.getInstance().getString(
               "views.participantTree.confirmDepartmentDelete.msg.title"));
         panelConfirmation.setTitle(MessagesViewsCommonBean.getInstance().getString(
               "views.participantTree.confirmDepartmentDelete.title"));
         panelConfirmation.openPopup();
      }
   }
   
   
   /**
    * Deletes selected Department
    * 
    * @param ae
    * @throws PortalException
    */
   private void deleteDepartment(Object uuidObj) throws PortalException
   {
      UUID uuid = (UUID) uuidObj;
      DefaultMutableTreeNode participantNode = findTreeNodeByUuid(uuid);
      ParticipantUserObject participantUserObject = null;
      if (participantNode != null)
      {
         participantUserObject = (ParticipantUserObject) participantNode.getUserObject();
      }

      if ((participantUserObject != null) && (participantUserObject.isReferencesDepartment()))
      {
         long deleteDepartmentOID = participantUserObject.getDepartment().getOID();
         DefaultMutableTreeNode deleteDepartmentNode = participantNode;

         AdministrationService service = getAdministrationService();

         if (deleteDepartmentOID >= 0)
         {
            try
            {
               service.removeDepartment(deleteDepartmentOID);
               departmentCache.remove(deleteDepartmentOID);
               refreshParticipantNode((DefaultMutableTreeNode) deleteDepartmentNode.getParent(), EnumSet.of(
                     NODE_TYPE.DEPARTMENT, NODE_TYPE.DEPARTMENT_DEFAULT));
            }
            catch (InvalidArgumentException aex)
            {
               ExceptionHandler.handleException(
                     aex,
                     MessagesViewsCommonBean.getInstance().getString(
                           "views.participantTree.deleteDepartment.error.inUse"));
            }
            catch (Exception ex)
            {
               ExceptionHandler.handleException(
                     ex,
                     MessagesViewsCommonBean.getInstance().getString(
                           "views.participantTree.deleteDepartment.error.generic"));
            }
         }
      }
   }

   /**
    * @param root
    */
   private void showModelNodes(DefaultMutableTreeNode root)
   {
      addModels(root);

      DefaultMutableTreeNode modelNode = null;
      List<DefaultMutableTreeNode> topLevelParticipantNodes = null;

      for (Map.Entry<String, DefaultMutableTreeNode> modelEntry : modelNodesMap.entrySet())
      {
         modelNode = modelEntry.getValue();
         topLevelParticipantNodes = topLevelParticipantsByModelMap.get(modelEntry.getKey());
         if (!CollectionUtils.isEmpty(topLevelParticipantNodes))
         {
            for (DefaultMutableTreeNode defaultMutableTreeNode : topLevelParticipantNodes)
            {
               modelNode.insert(defaultMutableTreeNode, modelNode.getChildCount());
            }
         }
      }
   }

   /**
    * @param root
    */
   private void hideModelNodes(DefaultMutableTreeNode root)
   {
      int index = 0;
      for (DefaultMutableTreeNode modelNode : modelNodesMap.values())
      {
         @SuppressWarnings("unchecked")
         List<DefaultMutableTreeNode> participantNodes = Collections.list(modelNode.children());
         for (DefaultMutableTreeNode defaultMutableTreeNode : participantNodes)
         {
            // Add the model nodes to the root node starting at position 0
            root.insert(defaultMutableTreeNode, index++);
         }
         removeParticipantNode(modelNode);
      }
      modelNodesMap.clear();
   }

   /**
    * @param rootNode
    * @param nodeTypes
    * @return
    */
   private List<DefaultMutableTreeNode> getChildNodes(DefaultMutableTreeNode rootNode, Set<NODE_TYPE> nodeTypes)
   {
      @SuppressWarnings("unchecked")
      Enumeration<DefaultMutableTreeNode> rootChildren = rootNode.children();
      
      DefaultMutableTreeNode node;
      ParticipantUserObject participantUserObject;
      List<DefaultMutableTreeNode> nodes = CollectionUtils.newList();

      while (rootChildren.hasMoreElements())
      {
         node = rootChildren.nextElement();
         participantUserObject = (ParticipantUserObject) node.getUserObject();

         for (NODE_TYPE nodeType : nodeTypes)
         {
            if (nodeType == participantUserObject.getNodeType())
            {
               nodes.add(node);
            }
         }
      }

      return nodes;
   }

   /**
    * @param node
    */
   private void loadChildNodes(DefaultMutableTreeNode node)
   {
      ParticipantUserObject participantUserObject = (ParticipantUserObject) node.getUserObject();
      if ((null != participantUserObject) && (!participantUserObject.isChildrenLoaded()))
      {
         QualifiedModelParticipantInfo modelParticipantInfo = participantUserObject.getQualifiedModelParticipantInfo();
         DynamicParticipantInfo dynamicParticipantInfo = participantUserObject.getDynamicParticipantInfo();

         switch (participantUserObject.getNodeType())
         {
         case ORGANIZATION_UNSCOPED:
         case ORGANIZATON_SCOPED_IMPLICIT:
            expandOrganizationNode(node, (QualifiedOrganizationInfo) modelParticipantInfo);
            break;

         case ORGANIZATON_SCOPED_EXPLICIT:
            expandExplicitlyScopedOrganizationNode(node, (QualifiedOrganizationInfo) modelParticipantInfo);
            break;

         case ROLE_SCOPED:
         case ROLE_UNSCOPED:
            addUsersForParticipant(node, modelParticipantInfo);
            break;

         case USERGROUP:
            addUsersForParticipant(node, dynamicParticipantInfo);
            break;

         case DEPARTMENT:
            expandDepartmentNode(node, participantUserObject.getDepartment());
            break;

         case DEPARTMENT_DEFAULT:
            expandDefaultDepartmentNode(node, (QualifiedOrganizationInfo) modelParticipantInfo);
            break;

         default:
            if (trace.isDebugEnabled())
            {
               trace.debug("Not supported to expand: " + participantUserObject.getNodeType());
            }
            break;
         }

         participantUserObject.setChildrenLoaded(true);
      }
   }

   /**
    * @param departmentInfo
    * @return
    */
   private Department getDepartment(DepartmentInfo departmentInfo)
   {
      Department department = null;

      if (departmentInfo != null)
      {
         department = departmentCache.get(departmentInfo.getOID());
         if (null == department)
         {
            department = getAdministrationService().getDepartment(departmentInfo.getOID());
         }
      }

      return department;
   }

   /*
    * Utility methods to get Service objects
    */

   /**
    * @return
    */
   private QueryService getQryService()
   {
      return SessionContext.findSessionContext().getServiceFactory().getQueryService();
   }

   /**
    * @return
    */
   private UserService getUserService()
   {
      return SessionContext.findSessionContext().getServiceFactory().getUserService();
   }

   /**
    * @return
    */
   private AdministrationService getAdministrationService()
   {
      return SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
   }

   /*
    * Default getter / setter methods
    */

   public DefaultTreeModel getModel()
   {
      return treeModel;
   }

   public boolean isModelsDisplayed()
   {
      return onOffFilters.get(SHOW_MODEL_ACTION).isOn();
   }

   /**
    * Highlight all selected users. Takes a fresh copy of users before highlighting.
    */
   private void highlightAllSelectedUsers()
   {
      if (isHighlightUsersOn())
      {
         Set<User> latestUsers = new HashSet<User>();
         UserService userService = ServiceFactoryUtils.getUserService();
         for (User user : selectedUsers)
         {
            latestUsers.add(userService.getUser(user.getOID()));
         }
         selectedUsers = latestUsers;
         removeHighlighting();
         highlightUsers(selectedUsers);
      }
   }
   
   /**
    * Highlights the selected users without taking fresh copies of users.
    * 
    */
   public void highlightSelectedUsers()
   {
      if (isHighlightUsersOn())
      {
         removeHighlighting();
         highlightUsers(selectedUsers);
      }
   }

   /**
    * Expand root node if it is not expanded
    */
   private void expandRootNode()
   {
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
      ParticipantUserObject participantUserObject = (ParticipantUserObject) root.getUserObject();
      if (participantUserObject.isChildrenLoaded() && !participantUserObject.isExpanded())
      {
         participantUserObject.setExpanded(true);
      }
      else
      {
         loadChildNodes(root);
      }
   }
   
   /**
    * highlight or remove highlighting of user, this method is invoked on change user
    * selection event
    * 
    * @param userEntry
    */
   public void highlightSelectedUser(User user, boolean selected)
   {
      try
      {
         updateSelectedUsersSet(user, selected);
         if (isHighlightUsersOn())
         {
            if (selected)
            {
               Set<User> users = CollectionUtils.newHashSet();
               UserService userService = ServiceFactoryUtils.getUserService();
               User usr = userService.getUser(user.getOID());
               users.add(usr);// latest latest user to get latest roles
               highlightUsers(users);
            }
            else
            {
               if (highlightedUsers.contains(user.getAccount()))
               {
                  removeHighlightingForSelectedUser(user);
               }
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   private void updateSelectedUsersSet(User user, boolean selected)
   {
      if (selected)
      {
         selectedUsers.add(user);
      }
      else
      {
         selectedUsers.remove(user);
      }
   }
   
   
   /**
    * create filters or commands on top of the tree
    */
   private void createFilterToolbar()
   {

      onOffFilters = new LinkedHashMap<String, GenericDataFilterOnOff>();

      //TODO: Do not delete following lines as they will be needed in future...
      /*onOffFilters.put(SEARCH_PARTICIPANT, new GenericDataFilterOnOff(SEARCH_PARTICIPANT, getMessages().getString(
            "participantTree.toolbar.searchParticipants.title"), getMessages().getString(
            "participantTree.toolbar.searchParticipants.title"), true, false,
            "/plugins/views-common/images/icons/find.png"));*/

      onOffFilters.put(SHOW_MODEL_ACTION, new GenericDataFilterOnOff(SHOW_MODEL_ACTION, MessagesViewsCommonBean.getInstance().getString(
            "views.participantTree.filters.model.title"), MessagesViewsCommonBean.getInstance().getString("views.participantTree.filters.model.off.title"),
            true, false, "/plugins/views-common/images/icons/model.gif"));

      if (highlightUserFilterEnabled)
      {
         onOffFilters.put(HIGHLIGHT_USERS_ACTION, new GenericDataFilterOnOff(HIGHLIGHT_USERS_ACTION, MessagesViewsCommonBean.getInstance()
               .getString("views.participantTree.toolbar.highlightUsers.title"), MessagesViewsCommonBean.getInstance().getString(
               "views.participantTree.toolbar.highlightUsers.off.title"), true, false,
               "/plugins/views-common/images/icons/flashlight-shine.png"));
      }
   }
 
   /**
    * Highlights all occurrences of the provided users in the tree.
    * 
    * @param users
    */
   private void highlightUsers(Set<User> users)
   {
      if (CollectionUtils.isEmpty(users))
      {
         return;
      }
      else
      {
         expandRootNode();
      }
      
      for (User user : users)
      {
         if (isUserHighlighted(user))
         {
            continue;
         }
 
         // Highlight user grants
         List<Grant> allGrants = user.getAllGrants();
         for (Grant grant : allGrants)
         {
            highlightUserGrant(user, grant, highlightStyleIndex);
         }
         
         // Highlight group memberships
         List<UserGroup> allGroups = user.getAllGroups();
         for (UserGroup userGroup : allGroups)
         {
            highlightUserGroup(user, userGroup, highlightStyleIndex);
         }

         highlightStyleIndex = (highlightStyleIndex >= HIGHLIGHT_STYLES_MAX)
         ? highlightStyleIndex = 1
         : ++highlightStyleIndex;  

      }
   }


   /**
    * Highlights the user under the particular grant.
    * 
    * @param user
    * @param grant
    * @param highlightStyleIndex
    */
   private void highlightUserGrant(User user, Grant grant, int highlightStyleIndex)
   {
      String participantId = grant.getQualifiedId();
      Department department = grant.getDepartment();
      String modelId = ModelUtils.extractModelId(grant.getQualifiedId());

      List<ParticipantItem> participantItemPath = UserUtils.getParticipantPath(participantId, department);
      highlightNode(user, participantItemPath, modelId, highlightStyleIndex, false);

      // find referring models
      if (!referringModels.containsKey(modelId))
      {
         putReferringModels(modelId, ModelUtils.findReferringModels(modelId));
      }
      List<String> modelIds = referringModels.get(modelId);

      for (String referringModelId : modelIds)
      {
         highlightNode(user, participantItemPath, referringModelId, highlightStyleIndex, true);
      }
   }
   
   /**
    * @param modelId
    * @param models
    */
   private void putReferringModels(String modelId, List<DeployedModel> models)
   {
      List<String> modelIds = new ArrayList<String>();
      for (DeployedModel deployedModel : models)
      {
         modelIds.add(deployedModel.getId());
      }
      referringModels.put(modelId, modelIds);
   }
   
   /**
    * Highlights the user under the particular user group.
    * 
    * @param user
    * @param userGroup
    * @param highlightStyleIndex
    */
   private void highlightUserGroup(User user, UserGroup userGroup, int highlightStyleIndex)
   {
      List<ParticipantItem> participantItemPath = CollectionUtils.newList();
      participantItemPath.add(new ParticipantItem(userGroup));
      highlightNode(user, participantItemPath, null, highlightStyleIndex, false);
   }
   
   /**
    * Highlights the {@link User} that can be reached by traversing the
    * {@link ParticipantItem} path. Expands each node along the way.
    * 
    * @param user
    * @param participantItemPath
    * @param modelId
    * @param highlightStyleIndex
    * @param allowMultiple
    */
   private void highlightNode(User user, List<ParticipantItem> participantItemPath, String modelId,
         int highlightStyleIndex, boolean allowMultiple)
   {
      DefaultMutableTreeNode searchRoot = null;

      // Use the tree root to start the search if:
      // 1. Model nodes are being displayed OR
      // 2. modelId is null (e.g. Usergroups or Administrator Role)
      if (!isModelsDisplayed() || null == modelId)
      {
         searchRoot = (DefaultMutableTreeNode) treeModel.getRoot();
      }
      else
      {
         searchRoot = modelNodesMap.get(modelId);
      }

      if (null != searchRoot)
      {
         // expand model node
         ParticipantUserObject modelNodeUserObject = (ParticipantUserObject) searchRoot.getUserObject();
         modelNodeUserObject.setExpanded(true);

         for (ParticipantItem participantItem : participantItemPath)
         {
            Set<DefaultMutableTreeNode> searchNodes = searchNodeChildren(searchRoot, participantItem, allowMultiple);
            for (DefaultMutableTreeNode searchNode : searchNodes)
            {
               searchRoot = searchNode;
               loadChildNodes(searchNode);

               ParticipantUserObject participantUserObject = (ParticipantUserObject) searchNode.getUserObject();
               participantUserObject.setExpanded(true);
               // Locate and highlight the user
               DefaultMutableTreeNode searchNodeUser = searchNodeChildren(searchRoot, new ParticipantItem(user));

               if (null != searchNodeUser)
               {
                  ParticipantUserObject userObject = (ParticipantUserObject) searchNodeUser.getUserObject();
                  userObject.setHighlightStyleClass(highlightStyleIndex);
                  highlightedParticipantUserObjects.add(userObject);
                  highlightedUsers.add(userObject.getUser().getAccount());
               }
            }
         }
      }
   }
   
   /**
    * @param parent
    * @param participantItem
    * @return
    */
   private DefaultMutableTreeNode searchNodeChildren(DefaultMutableTreeNode parent, ParticipantItem participantItem)
   {
      Set<DefaultMutableTreeNode> searchNodes = searchNodeChildren(parent, participantItem, false);
      if (CollectionUtils.isNotEmpty(searchNodes))
      {
         return searchNodes.iterator().next();
      }
      return null;
   }   
   
   /**
    *  Returns the matching child node under the parent for the provided, in case of referred roles nodes can be more than one
    * {@link ParticipantItem}.
    * @param parent
    * @param participantItem
    * @param allowMultiple
    * @return
    */
   private Set<DefaultMutableTreeNode> searchNodeChildren(DefaultMutableTreeNode parent, ParticipantItem participantItem, boolean allowMultiple)
   {
      Set<DefaultMutableTreeNode> resultNode = new HashSet<DefaultMutableTreeNode>();
      DefaultMutableTreeNode node = null;
      ParticipantItem other = null;

      if (null != parent && null != participantItem)
      {
         // Iterate over all of parent node's children
         @SuppressWarnings("unchecked")
         Enumeration<DefaultMutableTreeNode> childNodes = parent.children();
         while (childNodes.hasMoreElements())
         {
            node = childNodes.nextElement();
            if (null != node.getUserObject() && node.getUserObject() instanceof ParticipantUserObject)
            {
               // Create a ParticipantItem object from the ParticipantUserObject
               ParticipantUserObject participantUserObject = (ParticipantUserObject) node.getUserObject();
               if (null != participantUserObject.getQualifiedModelParticipantInfo())
               {
                  other = new ParticipantItem(participantUserObject.getQualifiedModelParticipantInfo());
               }
               else if (null != participantUserObject.getDynamicParticipantInfo())
               {
                  other = new ParticipantItem(participantUserObject.getDynamicParticipantInfo());
               }
               else if (null != participantUserObject.getDepartment())
               {
                  other = new ParticipantItem(participantUserObject.getDepartment());
               }

               // Compare the two objects
               if (participantItem.equals(other))
               {
                  resultNode.add(node);
                  if (!allowMultiple)
                  {
                     break;
                  }
               }
            }
         }
      }

      return resultNode;
   }

   /**
    * remove user highlighting
    */
   private void removeHighlighting()
   {
      for (ParticipantUserObject participantUserObject : highlightedParticipantUserObjects)
      {
         participantUserObject.setHighlightStyleClass(0);
      }
      resetHighlightUser();
   }
   
   /**
    * remove user highlighting
    * @param userDetailsTableEntry
    */
   private void removeHighlightingForSelectedUser(User user)
   {
      if (highlightedUsers.contains(user.getAccount()))
      {
         boolean marker = false;
         for (Iterator<ParticipantUserObject> iterator = highlightedParticipantUserObjects.iterator(); iterator
               .hasNext();)
         {
            ParticipantUserObject participantUserObject = (ParticipantUserObject) iterator.next();
            if (participantUserObject.getUser().getAccount().equals(user.getAccount()))
            {
               participantUserObject.setHighlightStyleClass(0);
               iterator.remove();
               marker = true;
            }
         }
         if (marker)
         {
            highlightStyleIndex = (highlightStyleIndex > 1) ? --highlightStyleIndex : highlightStyleIndex;
            highlightedUsers.remove(user.getAccount());
         }
      }
   }

   /**
    * refreshes the given participant node recursively without collapsing the sub nodes
    * 
    * @param participantNode
    * @param nodeTypes
    */
   private void refreshParticipantNodeRecursively(DefaultMutableTreeNode participantNode, Set<NODE_TYPE> nodeTypes,
         List<ParticipantUserObject> expandedNodeUserObjects)
   {
      ParticipantUserObject participantUserObject = (ParticipantUserObject) participantNode.getUserObject();
      if (participantUserObject.isExpanded() && !NODE_TYPE.USER.equals(participantUserObject.getNodeType()))
      {
         // if children are not loaded
         if (!participantUserObject.isChildrenLoaded())
         {
            loadChildNodes(participantNode);
         }
         // if it is a Root or Model node
         else if ((NODE_TYPE.MODEL.equals(participantUserObject.getNodeType()) || NODE_TYPE.ROOT
               .equals(participantUserObject.getNodeType())))
         {
            participantUserObject.setExpanded(true);

         }
         // if children are already loaded then just refresh those
         else
         {
            refreshParticipantNodeControlled(participantNode, nodeTypes);
         }
         int childCount = participantNode.getChildCount();
         for (int i = 0; i < childCount; i++)
         {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) participantNode.getChildAt(i);
            ParticipantUserObject childUserObject = (ParticipantUserObject) childNode.getUserObject();
            if (!childUserObject.isExpanded())
            {
               childUserObject.setExpanded(isExpanded(expandedNodeUserObjects, childUserObject));
            }
            refreshParticipantNodeRecursively(childNode, nodeTypes, expandedNodeUserObjects);
         }
      }
   }
   

   /**
    * @param participantNode
    * @param nodeTypes
    */
   private void refreshParticipantNodeControlled(DefaultMutableTreeNode participantNode, Set<NODE_TYPE> nodeTypes)
   {
      List<DefaultMutableTreeNode> allChildNodes = getChildNodes(participantNode, nodeTypes);

      ParticipantUserObject parentUserObject = (ParticipantUserObject) participantNode.getUserObject();

      // Remove the nodes
      for (DefaultMutableTreeNode childNode : allChildNodes)
      {
         removeParticipantNode(childNode);
      }

      for (NODE_TYPE nodeType : nodeTypes)
      {
         switch (nodeType)
         {
         case USER:
            // Re-add the User nodes
            if (parentUserObject != null)
            {
               if (parentUserObject.getNodeType() == NODE_TYPE.DEPARTMENT)
               {
                  Department dpt = parentUserObject.getDepartment();
                  Organization org = dpt.getOrganization();
                  QualifiedModelParticipantInfo modelParticipantInfo = dpt.getScopedParticipant(org);
                  addUsersForParticipant(participantNode, modelParticipantInfo);
               }
               else
               {
                  if (parentUserObject.getQualifiedModelParticipantInfo() != null)
                  {
                     addUsersForParticipant(participantNode, parentUserObject.getQualifiedModelParticipantInfo());
                  }
                  else if (parentUserObject.getDynamicParticipantInfo() != null)
                  {
                     addUsersForParticipant(participantNode, parentUserObject.getDynamicParticipantInfo());
                  }
               }
               // If parent node is not expanded, expand it
               parentUserObject.setExpanded(true);
            }
            break;

         case DEPARTMENT:
            // Re-add the Department nodes
            ParticipantUserObject participantUserObject = (ParticipantUserObject) participantNode.getUserObject();
            QualifiedOrganizationInfo organizationInfo = participantUserObject.getScopedOrganization();
            if (null != organizationInfo)
            {
               expandExplicitlyScopedOrganizationNode(participantNode, organizationInfo);
            }
            // If parent node is not expanded, expand it
            participantUserObject.setExpanded(true);
            break;
         }
      }
   }

   /**
    * returns true if the node was previously expanded
    * 
    * @param expandedNodeUserObjects
    * @param currentUserObject
    * @return
    */
   private boolean isExpanded(List<ParticipantUserObject> expandedNodeUserObjects,
         ParticipantUserObject currentUserObject)
   {
      for (ParticipantUserObject expandedUserObject : expandedNodeUserObjects)
      {
         if (expandedUserObject.getNodeType().equals(currentUserObject.getNodeType()))
         {
            switch (currentUserObject.getNodeType())
            {
            case ORGANIZATION_UNSCOPED:
            case ORGANIZATON_SCOPED_IMPLICIT:
            case ORGANIZATON_SCOPED_EXPLICIT:
            case ROLE_UNSCOPED:
            case ROLE_SCOPED:
            case USERGROUP:
            case DEPARTMENT:
            case DEPARTMENT_DEFAULT:
               
               ParticipantItem currentParticipantItem = currentUserObject.getParticipantItem();
               ParticipantItem expandedParticipantItem = expandedUserObject.getParticipantItem();
               
               if (null != currentParticipantItem && null != expandedParticipantItem
                     && currentParticipantItem.equals(expandedParticipantItem))
               {
                  return expandedUserObject.isExpanded();
               }
               break;

            case ROOT:
               return expandedUserObject.isExpanded();

            case MODEL:
               if (expandedUserObject.getModelOid() == currentUserObject.getModelOid())
               {
                  return expandedUserObject.isExpanded();
               }
               break;

            case USER:
               return false;

            default:
               if (trace.isDebugEnabled())
               {
                  trace.debug("Not supported to expand: " + currentUserObject.getNodeType());
               }
               break;
            }
         }
      }
      return false;
   }

   /**
    * Populates the expanded Nodes List
    * 
    * @param participantNode
    * @param expandedFolders
    */
   private static void populateExpandedNodeList(DefaultMutableTreeNode participantNode,
         List<ParticipantUserObject> expandedNodeUserObjects)
   {
      ParticipantUserObject participantUserObject = (ParticipantUserObject) participantNode.getUserObject();
      if (participantUserObject.isExpanded())
      {
         expandedNodeUserObjects.add(participantUserObject);
         int chileCount = participantNode.getChildCount();
         DefaultMutableTreeNode tempNode;
         for (int i = 0; i < chileCount; i++)
         {
            tempNode = (DefaultMutableTreeNode) participantNode.getChildAt(i);
            populateExpandedNodeList(tempNode, expandedNodeUserObjects);
         }
      }
   }
   
   /**
    * @param user
    * @return
    */
   private boolean isUserHighlighted(User user)
   {
      return highlightedUsers.contains(user.getAccount());
   }

   /**
    * 
    */
   private void resetHighlightUser()
   {
      highlightedParticipantUserObjects.clear();
      highlightedUsers.clear();
      highlightStyleIndex = 1;
   }

   /**
    * @param model
    * @return
    */
   private boolean filterModelNodes(Model model)
   {
      if (filterPredefniedModelNodes && PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
      {
         return true;
      }

      return false;
   }

   public List<GenericDataFilterOnOff> getOnOffFilters()
   {
      return CollectionUtils.newArrayList(onOffFilters.values());
   }

   public boolean isHighlightUsersOn()
   {
      if (null != onOffFilters.get(HIGHLIGHT_USERS_ACTION))
      {
         return onOffFilters.get(HIGHLIGHT_USERS_ACTION).isOn();
      }

      return false;
   }

   public Set<User> getSelectedUsers()
   {
      return selectedUsers;
   }

   public void setSelectedUsers(Set<User> selectedUsers)
   {
      this.selectedUsers = selectedUsers;
   }

   public ParticipantUserObject getSelectedUserObject()
   {
      return selectedUserObject;
   }

   public boolean isShowUserNodes()
   {
      return showUserNodes;
   }

   public void setShowUserNodes(boolean showUserNodes)
   {
      this.showUserNodes = showUserNodes;
   }

   public boolean isShowUserGroupNodes()
   {
      return showUserGroupNodes;
   }

   public void setShowUserGroupNodes(boolean showUserGroupNodes)
   {
      this.showUserGroupNodes = showUserGroupNodes;
   }

   public boolean isHighlightUserFilterEnabled()
   {
      return highlightUserFilterEnabled;
   }

   public void setHighlightUserFilterEnabled(boolean highlightUserFilterEnabled)
   {
      this.highlightUserFilterEnabled = highlightUserFilterEnabled;
   }

   public boolean isFilterPredefniedModelNodes()
   {
      return filterPredefniedModelNodes;
   }

   public void setFilterPredefniedModelNodes(boolean filterPredefniedModelNodes)
   {
      this.filterPredefniedModelNodes = filterPredefniedModelNodes;
   }

   public void setParticipantTreeUpdated(boolean participantTreeUpdated)
   {
      this.participantTreeUpdated = participantTreeUpdated;
   }

   public InfoPanelBean getInfoPanelBean()
   {
      return infoPanelBean;
   }

}
