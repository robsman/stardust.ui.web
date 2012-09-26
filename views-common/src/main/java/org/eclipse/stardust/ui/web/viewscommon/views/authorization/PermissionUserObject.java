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

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;

import com.icesoft.faces.component.tree.IceUserObject;

/**
 * class is type of IceUserObject for Permission tree table
 * 
 * @author Vikas.Mishra
 * @author Yogesh.Manware
 * 
 */
public class PermissionUserObject extends IceUserObject
{
   private static final long serialVersionUID = -364375780924462409L;
   private static final String ICON_ROLE_UNSCOPED = "/plugins/views-common/images/icons/role.png";
   public static final String ICON_PERMISSION = "/plugins/views-common/images/icons/key.png";
   private static final String ICON_PERMISSION_MODIFIED = "/plugins/views-common/images/icons/key-modified.png";

   private static final String ICON_ALLOW_ACCESS = "/plugins/views-common/images/icons/application-share.png";
   private static final String ICON_DENY_ACCESS = "/plugins/views-common/images/icons/prohibition.png";

   // ~ Instance fields
   // ================================================================================================
   private NODE_TYPE nodeType;
   private String participantId;
   private String permissionId;
   private boolean containsAllParticipants;
   private boolean containsDefaultParticipants;
   private boolean dirty;
   private boolean selected;
   private Participant participant;
   private PERMISSION_TYPE permissionType;

   /**
    * @param wrapper
    * @param permissionId
    * @param permissionLabel
    * @param pType
    */
   public PermissionUserObject(DefaultMutableTreeNode wrapper, String permissionId, String permissionLabel,
         PERMISSION_TYPE pType)
   {
      super(wrapper);
      initializePermissionNode(wrapper, permissionId, permissionLabel, pType);
   }

   /**
    * @param wrapper
    * @param permissionId
    * @param permissionLabel
    * @param pType
    */
   private void initializePermissionNode(DefaultMutableTreeNode wrapper, String permissionId, String permissionLabel,
         PERMISSION_TYPE pType)
   {
      this.permissionType = pType;
      nodeType = NODE_TYPE.PERMISSION;
      String icon = null;
      if (PERMISSION_TYPE.ALLOW == pType)
      {
         this.permissionId = UiPermissionUtils.getPermissionIdAllow(permissionId);
         icon = ICON_ALLOW_ACCESS;
         setText(MessagesViewsCommonBean.getInstance().getString("views.authorizationManagerView.allow"));
      }
      else if (PERMISSION_TYPE.DENY == pType)
      {
         this.permissionId = UiPermissionUtils.getPermissionIdDeny(permissionId);
         icon = ICON_DENY_ACCESS;
         setText(MessagesViewsCommonBean.getInstance().getString("views.authorizationManagerView.deny"));
      }
      else
      {
         this.permissionType = PERMISSION_TYPE.GEN_PERMISSION;
         this.permissionId = permissionId;
         icon = ICON_PERMISSION;
         setText(permissionLabel);
      }

      setLeaf(false);
      setBranchContractedIcon(icon);
      setBranchExpandedIcon(icon);
      setLeafIcon(icon);
   }

   /**
    * 
    * @param wrapper
    * @param participantId
    * @param participantLabel
    * @param permissionId
    *           this is used only for ALL participant node
    */
   public PermissionUserObject(DefaultMutableTreeNode wrapper, String participantId, String participantLabel,
         String permissionId)
   {
      super(wrapper);
      this.permissionId = permissionId;
      this.participantId = participantId;
      setText(participantLabel);
      setLeaf(false);
      nodeType = NODE_TYPE.ROLE_UNSCOPED;
      setBranchContractedIcon(ICON_ROLE_UNSCOPED);
      setBranchExpandedIcon(ICON_ROLE_UNSCOPED);
      setLeafIcon(ICON_ROLE_UNSCOPED);
   }

   /**
    * 
    * @param wrapper
    * @param qualifiedParticipantInfo
    * @param permissionId
    */
   public PermissionUserObject(DefaultMutableTreeNode wrapper, ModelParticipantInfo participantInfo, String permissionId)
   {
      super(wrapper);
      this.permissionId = permissionId;

      if (participantInfo instanceof QualifiedModelParticipantInfo)
      {
         QualifiedModelParticipantInfo qualifiedParticipantInfo = (QualifiedModelParticipantInfo) participantInfo;
         String modelId = ModelUtils.extractModelId(qualifiedParticipantInfo.getQualifiedId());
         Model model = ModelCache.findModelCache().getActiveModel(modelId);
         participant = model.getParticipant(participantInfo.getId());
         setText(I18nUtils.getParticipantName(participant));
         this.participantId = participantInfo.getId();
      }
      else
      {

         participant = ModelCache.findModelCache().getParticipant(participantInfo.getId());
         setText(I18nUtils.getParticipantName(participant));
         this.participantId = participantInfo.getId();
      }

      nodeType = NODE_TYPE.ROLE_UNSCOPED;
      setBranchContractedIcon(ICON_ROLE_UNSCOPED);
      setBranchExpandedIcon(ICON_ROLE_UNSCOPED);
      setLeafIcon(ICON_ROLE_UNSCOPED);
   }

   public PermissionUserObject(DefaultMutableTreeNode wrapper, String label)
   {
      super(wrapper);
      setText(label);
      this.nodeType = NODE_TYPE.ABSOLUTE;

      setBranchContractedIcon(ICON_PERMISSION);
      setBranchExpandedIcon(ICON_PERMISSION);
      setLeafIcon(ICON_PERMISSION);
   }

   /**
      *
      */
   public String getBranchContractedIcon()
   {
      if (dirty)
      {
         return ICON_PERMISSION_MODIFIED;
      }
      return super.getBranchContractedIcon();
   }

   /**
     *
     */
   public String getBranchExpandedIcon()
   {
      if (dirty)
      {
         return ICON_PERMISSION_MODIFIED;
      }
      return super.getBranchExpandedIcon();
   }

   @Override
   public String getIcon()
   {
      if (dirty)
      {
         return ICON_PERMISSION_MODIFIED;
      }
      return super.getIcon();
   }

   private boolean isGeneralPermission()
   {
      if (NODE_TYPE.PERMISSION == nodeType && PERMISSION_TYPE.GEN_PERMISSION == permissionType)
      {
         return true;
      }
      return false;
   }

   /**
     *
     */
   public String getLeafIcon()
   {
      if (dirty && NODE_TYPE.PERMISSION.equals(nodeType))
      {
         return ICON_PERMISSION_MODIFIED;
      }

      return super.getLeafIcon();
   }

   /**
    * 
    * @return
    */
   public NODE_TYPE getNodeType()
   {
      return nodeType;
   }

   public String getParticipantId()
   {
      return participantId;
   }

   public String getPermissionId()
   {
      return permissionId;
   }

   public boolean isContainsAllParticipants()
   {
      return containsAllParticipants;
   }

   public boolean isContainsDefaultParticipants()
   {
      return containsDefaultParticipants;
   }

   public boolean isDirty()
   {
      return dirty;
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setContainsAllParticipants(boolean containsAllParticipants)
   {
      this.containsAllParticipants = containsAllParticipants;
   }

   public void setContainsDefaultParticipants(boolean containsDefaultParticipants)
   {
      this.containsDefaultParticipants = containsDefaultParticipants;
   }

   public void setDirty(boolean dirty)
   {
      if (NODE_TYPE.PERMISSION == nodeType
            && (PERMISSION_TYPE.ALLOW == permissionType || PERMISSION_TYPE.DENY == permissionType))
      {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) getWrapper().getParent();
         PermissionUserObject userObject = (PermissionUserObject) node.getUserObject();
         userObject.forceSetDirty(true);
      }
      else if (isGeneralPermission())
      {
         this.dirty = dirty;
      }
   }

   public void forceSetDirty(boolean dirty)
   {
      this.dirty = dirty;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   public Participant getParticipant()
   {
      return participant;
   }

   /**
      *
      */
   public static enum NODE_TYPE {
      ORGANIZATION_UNSCOPED, ORGANIZATON_SCOPED_EXPLICIT, ORGANIZATON_SCOPED_IMPLICIT, PERMISSION, ROLE_SCOPED, ROLE_UNSCOPED, ROOT, ABSOLUTE;
   }

   public static enum PERMISSION_TYPE {
      ALLOW, DENY, GEN_PERMISSION
   }
}
