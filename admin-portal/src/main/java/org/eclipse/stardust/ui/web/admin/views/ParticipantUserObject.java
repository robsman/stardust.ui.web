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
package org.eclipse.stardust.ui.web.admin.views;

import java.util.EnumSet;
import java.util.UUID;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.engine.api.model.DynamicParticipantInfo;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedOrganizationInfo;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.user.UserProfileBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantItem;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

import com.icesoft.faces.component.tree.IceUserObject;



public class ParticipantUserObject extends IceUserObject
{
   public static enum NODE_TYPE {
      ROOT, MODEL, ORGANIZATON_SCOPED_EXPLICIT, ORGANIZATON_SCOPED_IMPLICIT, ROLE_SCOPED, ORGANIZATION_UNSCOPED, ROLE_UNSCOPED, USERGROUP, USER, DEPARTMENT, DEPARTMENT_DEFAULT;
   }

   private static final String DEFAULT_DEPARTMENT_SUFFIX = " - Default";

   private static final String ICON_PARTICIPANT_ROOT = "/plugins/views-common/images/icons/world.png";

   private static final String ICON_MODEL = "/plugins/views-common/images/icons/model.gif";

   private static final String ICON_ORGANIZATION_UNSCOPED = "/plugins/views-common/images/icons/chart_organisation.png";

   private static final String ICON_ORGANIZATION_SCOPED_EXPLICIT = "/plugins/views-common/images/icons/organization_scoped.png";

   private static final String ICON_ORGANIZATION_SCOPED_IMPLICIT = "/plugins/views-common/images/icons/organization_scoped.png";

   private static final String ICON_ROLE_UNSCOPED = "/plugins/views-common/images/icons/role.png";

   private static final String ICON_ROLE_SCOPED = "/plugins/views-common/images/icons/role_scoped.png";

   private static final String ICON_ROLE_TEAMLEAD_UNSCOPED = "/plugins/views-common/images/icons/role_teamlead.png";

   private static final String ICON_ROLE_TEAMLEAD_SCOPED = "/plugins/views-common/images/icons/role_teamlead_scoped.png";

   private static final String ICON_DEPARTMENT = "/plugins/views-common/images/icons/group_link.png";

   private static final String ICON_DEPARTMENT_DEFAULT = "/plugins/views-common/images/icons/group_error.png";

   private static final String ICON_USERGROUP = "/plugins/views-common/images/icons/group.png";

   private static final String ICON_USER = "/plugins/views-common/images/icons/user.png";
   
   private static final String HIGHLIGHT_USER_STYLE_PREFIX = "participant-tree-highlight-style-common participant-tree-highlight-style";

   private ParticipantItem participantItem;
   
   private Integer modelOid = null;

   private NODE_TYPE nodeType;

   private UUID uuid;
   
   private String highlightStyle;

   private boolean isTeamLead = false;
   
   private boolean childrenLoaded = false;
   
   private AdminMessagesPropertiesBean propsBean;

   public ParticipantUserObject()
   {
      super(null);
      propsBean = AdminMessagesPropertiesBean.getInstance();
      setText(propsBean.getString("views.participantMgmt.participantTree.participantNode.label"));
      setExpanded(true);
      setNodeType();
      setBranchIcon();
      setUuid(UUID.randomUUID());
   }

   public ParticipantUserObject(DefaultMutableTreeNode wrapper, Model model)
   {
      super(wrapper);
      setText(I18nUtils.getLabel(model, model.getName()));
      setModelOid(model.getModelOID());
      nodeType = NODE_TYPE.MODEL;
      setExpanded(true);
      setBranchIcon();
      setUuid(UUID.randomUUID());
   }

   public ParticipantUserObject(DefaultMutableTreeNode wrapper, QualifiedModelParticipantInfo qualifiedParticipantInfo,
         boolean isDefault)
   {
      super(wrapper);
      
      setText(I18nUtils.getParticipantName(ParticipantUtils.getParticipant(qualifiedParticipantInfo))
            + DEFAULT_DEPARTMENT_SUFFIX);

      participantItem = new ParticipantItem(qualifiedParticipantInfo);
      nodeType = NODE_TYPE.DEPARTMENT_DEFAULT;
      setBranchIcon();
      setUuid(UUID.randomUUID());
   }

   public ParticipantUserObject(DefaultMutableTreeNode wrapper, QualifiedModelParticipantInfo qualifiedParticipantInfo)
   {
      super(wrapper);

      setText(I18nUtils.getParticipantName(ParticipantUtils.getParticipant(qualifiedParticipantInfo)));

      participantItem = new ParticipantItem(qualifiedParticipantInfo);
      setNodeType();
      setBranchIcon();
      setUuid(UUID.randomUUID());
   }

   public ParticipantUserObject(DefaultMutableTreeNode wrapper, DynamicParticipantInfo dynamicParticipantInfo)
   {
      super(wrapper);

      // TODO
      if (dynamicParticipantInfo instanceof Participant)
      {
         setText(I18nUtils.getParticipantName((Participant) dynamicParticipantInfo));
      }
      else
      {
         setText(dynamicParticipantInfo.getName());
      }

      participantItem = new ParticipantItem(dynamicParticipantInfo);
      setNodeType();
      setBranchIcon();
      setUuid(UUID.randomUUID());
   }

   public ParticipantUserObject(DefaultMutableTreeNode wrapper, Department department)
   {
      super(wrapper);

      setText(department.getName());
      participantItem = new ParticipantItem(department);
      setNodeType();
      setBranchIcon();
      setUuid(UUID.randomUUID());
   }

   public void createUser(ActionEvent event)
   {
      UserProfileBean userProfileBean = UserProfileBean.getInstance();
      userProfileBean.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            UserProfileBean userProfileBean = UserProfileBean.getInstance();
            User newUser = userProfileBean.getUser();
            if (null != newUser)
            {
               switch (nodeType)
               {
               case ORGANIZATION_UNSCOPED:
               case ORGANIZATON_SCOPED_IMPLICIT:
               case DEPARTMENT_DEFAULT:
               case ROLE_UNSCOPED:
               case ROLE_SCOPED:
               {
                  newUser.addGrant(getQualifiedModelParticipantInfo());
                  break;
               }
               case USERGROUP:
               {
                  newUser.joinGroup(getDynamicParticipantInfo().getId());
                  break;
               }
               case DEPARTMENT:
               {
                  Department department = getDepartment();
                  QualifiedModelParticipantInfo qualifiedParticipantInfo = department.getScopedParticipant(department
                        .getOrganization());
                  newUser.addGrant(qualifiedParticipantInfo);
                  break;
               }
               }
               ServiceFactoryUtils.getUserService().modifyUser(newUser);
               ParticipantManagementBean.getInstance().refreshUserManagementTable();
               refreshParticipantTree();
            }
         }
      });
      userProfileBean.openCreateUserDialog();
   }
   
   private void refreshParticipantTree()
   {
      ParticipantTree.getInstance().refreshParticipantNode(this.wrapper,  EnumSet.of(NODE_TYPE.USER));
   }
   
   private void setBranchIcon()
   {
      boolean isLeaf = false;

      switch (nodeType)
      {
      case ROOT:
         setBranchContractedIcon(ICON_PARTICIPANT_ROOT);
         setBranchExpandedIcon(ICON_PARTICIPANT_ROOT);
         setLeafIcon(ICON_PARTICIPANT_ROOT);
         isLeaf = false;
         break;

      case MODEL:
         setBranchContractedIcon(ICON_MODEL);
         setBranchExpandedIcon(ICON_MODEL);
         setLeafIcon(ICON_MODEL);
         isLeaf = false;
         break;

      case ORGANIZATON_SCOPED_EXPLICIT:
         setBranchContractedIcon(ICON_ORGANIZATION_SCOPED_EXPLICIT);
         setBranchExpandedIcon(ICON_ORGANIZATION_SCOPED_EXPLICIT);
         setLeafIcon(ICON_ORGANIZATION_SCOPED_EXPLICIT);
         isLeaf = false;
         break;

      case ORGANIZATON_SCOPED_IMPLICIT:
         setBranchContractedIcon(ICON_ORGANIZATION_SCOPED_IMPLICIT);
         setBranchExpandedIcon(ICON_ORGANIZATION_SCOPED_IMPLICIT);
         setLeafIcon(ICON_ORGANIZATION_SCOPED_IMPLICIT);
         isLeaf = false;
         break;

      case ROLE_SCOPED:
         if (isReferencesTeamLeadRole())
         {
            setBranchContractedIcon(ICON_ROLE_TEAMLEAD_SCOPED);
            setBranchExpandedIcon(ICON_ROLE_TEAMLEAD_SCOPED);
            setLeafIcon(ICON_ROLE_TEAMLEAD_SCOPED);
         }
         else
         {
            setBranchContractedIcon(ICON_ROLE_SCOPED);
            setBranchExpandedIcon(ICON_ROLE_SCOPED);
            setLeafIcon(ICON_ROLE_SCOPED);
         }
         isLeaf = false;
         break;

      case ORGANIZATION_UNSCOPED:
         setBranchContractedIcon(ICON_ORGANIZATION_UNSCOPED);
         setBranchExpandedIcon(ICON_ORGANIZATION_UNSCOPED);
         setLeafIcon(ICON_ORGANIZATION_UNSCOPED);
         isLeaf = false;
         break;

      case ROLE_UNSCOPED:
         if (isReferencesTeamLeadRole())
         {
            setBranchContractedIcon(ICON_ROLE_TEAMLEAD_UNSCOPED);
            setBranchExpandedIcon(ICON_ROLE_TEAMLEAD_UNSCOPED);
            setLeafIcon(ICON_ROLE_TEAMLEAD_UNSCOPED);
         }
         else
         {
            setBranchContractedIcon(ICON_ROLE_UNSCOPED);
            setBranchExpandedIcon(ICON_ROLE_UNSCOPED);
            setLeafIcon(ICON_ROLE_UNSCOPED);
         }
         isLeaf = false;
         break;

      case USERGROUP:
         setBranchContractedIcon(ICON_USERGROUP);
         setBranchExpandedIcon(ICON_USERGROUP);
         setLeafIcon(ICON_USERGROUP);
         isLeaf = false;
         break;

      case USER:
         setLeafIcon(ICON_USER);
         isLeaf = true;
         break;

      case DEPARTMENT:
         setBranchContractedIcon(ICON_DEPARTMENT);
         setBranchExpandedIcon(ICON_DEPARTMENT);
         setLeafIcon(ICON_DEPARTMENT);
         isLeaf = false;
         break;

      case DEPARTMENT_DEFAULT:
         setBranchContractedIcon(ICON_DEPARTMENT_DEFAULT);
         setBranchExpandedIcon(ICON_DEPARTMENT_DEFAULT);
         setLeafIcon(ICON_DEPARTMENT_DEFAULT);
         isLeaf = false;
         break;
      }

      setLeaf(isLeaf);
   }

   void setNodeType()
   {
      QualifiedModelParticipantInfo modelParticipantInfo = getQualifiedModelParticipantInfo();
      DynamicParticipantInfo dynamicParticipantInfo = getDynamicParticipantInfo();
      Department department = getDepartment();
      
      if ((null == modelParticipantInfo) && (null == department) && (null == dynamicParticipantInfo))
      {
         nodeType = (null == modelOid) ? NODE_TYPE.ROOT : NODE_TYPE.MODEL;
      }
      else if ((modelParticipantInfo instanceof OrganizationInfo) && modelParticipantInfo.definesDepartmentScope())
      {
         nodeType = NODE_TYPE.ORGANIZATON_SCOPED_EXPLICIT;
      }
      else if ((modelParticipantInfo instanceof OrganizationInfo) && modelParticipantInfo.isDepartmentScoped()
            && !modelParticipantInfo.definesDepartmentScope())
      {
         nodeType = NODE_TYPE.ORGANIZATON_SCOPED_IMPLICIT;
      }
      else if ((modelParticipantInfo instanceof RoleInfo) && modelParticipantInfo.isDepartmentScoped())
      {
         nodeType = NODE_TYPE.ROLE_SCOPED;
      }
      else if (modelParticipantInfo instanceof OrganizationInfo)
      {
         nodeType = NODE_TYPE.ORGANIZATION_UNSCOPED;
      }
      else if (modelParticipantInfo instanceof RoleInfo)
      {
         nodeType = NODE_TYPE.ROLE_UNSCOPED;
      }
      else if (dynamicParticipantInfo instanceof UserGroup)
      {
         nodeType = NODE_TYPE.USERGROUP;
      }
      else if (dynamicParticipantInfo instanceof User)
      {
         nodeType = NODE_TYPE.USER;
      }
      else if (department != null)
      {
         nodeType = NODE_TYPE.DEPARTMENT;
      }
   }

   public NODE_TYPE getNodeType()
   {
      return nodeType;
   }

   public void setUuid(UUID uuid)
   {
      this.uuid = uuid;
   }

   public UUID getUuid()
   {
      return uuid;
   }

   public boolean isTeamLead()
   {
      return isTeamLead;
   }

   public void setTeamLead(boolean isTeamLead)
   {
      this.isTeamLead = isTeamLead;
   }

   public void setModelOid(Integer modelOid)
   {
      this.modelOid = modelOid;
   }

   public Integer getModelOid()
   {
      return modelOid;
   }

   public QualifiedModelParticipantInfo getQualifiedModelParticipantInfo()
   {
      return (null != participantItem) ? participantItem.getQualifiedModelParticipantInfo() : null;
   }

   public DynamicParticipantInfo getDynamicParticipantInfo()
   {
      return (null != participantItem) ? participantItem.getDynamicParticipantInfo() : null;
   }

   public Department getDepartment()
   {
      return (null != participantItem) ? participantItem.getDepartment() : null;
   }

   public QualifiedOrganizationInfo getScopedOrganization()
   {
      QualifiedOrganizationInfo qualifiedOrganizationInfo = null;
      if (isReferencesScopedOrganization())
      {
         qualifiedOrganizationInfo = (QualifiedOrganizationInfo) getQualifiedModelParticipantInfo();
      }

      return qualifiedOrganizationInfo;
   }

   public UserGroup getUserGroup()
   {
      UserGroup userGroup = null;
      if (isReferencesUserGroup())
      {
         userGroup = (UserGroup) getDynamicParticipantInfo();
      }

      return userGroup;
   }

   public User getUser()
   {
      User user = null;
      if (isReferencesUser())
      {
         user = (User) getDynamicParticipantInfo();
      }

      return user;
   }

   public boolean isReferencesRoot()
   {
      return (nodeType == NODE_TYPE.ROOT);
   }

   public boolean isReferencesModel()
   {
      return (nodeType == NODE_TYPE.MODEL);
   }

   public boolean isReferencesUnscopedOrganization()
   {
      return (nodeType == NODE_TYPE.ORGANIZATION_UNSCOPED);
   }

   public boolean isReferencesScopedOrganization()
   {
      return (nodeType == NODE_TYPE.ORGANIZATON_SCOPED_EXPLICIT);
   }

   public boolean isReferencesImplicitlyScopedOrganization()
   {
      return (nodeType == NODE_TYPE.ORGANIZATON_SCOPED_IMPLICIT);
   }

   public boolean isReferencesScopedRole()
   {
      return (nodeType == NODE_TYPE.ROLE_SCOPED);
   }

   public boolean isReferencesUnscopedRole()
   {
      return (nodeType == NODE_TYPE.ROLE_UNSCOPED);
   }

   public boolean isReferencesTeamLeadRole()
   {
      return isTeamLead;
   }

   public boolean isReferencesUserGroup()
   {
      return (nodeType == NODE_TYPE.USERGROUP);
   }

   public boolean isReferencesUser()
   {
      return (nodeType == NODE_TYPE.USER);
   }

   public boolean isReferencesDepartment()
   {
      return (nodeType == NODE_TYPE.DEPARTMENT);
   }

   public boolean isReferencesDefaultDepartment()
   {
      return (nodeType == NODE_TYPE.DEPARTMENT_DEFAULT);
   }
   
   public boolean isChildrenLoaded()
   {
      return childrenLoaded;
   }

   public void setChildrenLoaded(boolean childrenLoaded)
   {
      this.childrenLoaded = childrenLoaded;
   }

   public String getHighlightStyleClass()
   {
      return highlightStyle;
   }

   public void setHighlightStyleClass(int highlightStyleIndex)
   {
      this.highlightStyle = HIGHLIGHT_USER_STYLE_PREFIX + highlightStyleIndex;
   }

   /**
    * @return the participantItem
    */
   public ParticipantItem getParticipantItem()
   {
      return participantItem;
   }
}