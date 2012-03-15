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
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;


import com.icesoft.faces.component.tree.IceUserObject;


/**
 * class is type of IceUserObject for Permission tree table
 *
 * @author Vikas.Mishra
 *
 */
public class PermissionUserObject extends IceUserObject
{
    private static final long serialVersionUID = -364375780924462409L;
    private static final String ICON_ROLE_UNSCOPED = "/plugins/views-common/images/icons/role.png";
    //private static final String ICON_ROLE_SCOPED = "/plugins/admin-portal/images/icons/role_scoped.png";
    private static final String ICON_PERMISSION = "/plugins/views-common/images/icons/key.png";
    private static final String ICON_PERMISSION_MODIFIED = "/plugins/views-common/images/icons/key-modified.png";

    // ~ Instance fields
    // ================================================================================================
    private final NODE_TYPE nodeType;
    private final String participantId;
    private final String permissionId;
    private boolean containsAllParticipants;
    private boolean containsDefaultParticipants;
    private boolean dirty;
    private boolean selected;
    private final Participant participant;

    // ~ Constructor
    // ================================================================================================
    /**
     * to create root node
     */
    public PermissionUserObject()
    {
        super(null);
        setText("Root node");
        nodeType = NODE_TYPE.ROOT;
        setBranchContractedIcon(ICON_PERMISSION);
        setBranchExpandedIcon(ICON_PERMISSION);
        setLeafIcon(ICON_PERMISSION);
        setLeaf(false);
        setExpanded(true);
        this.permissionId=null;
        this.participant=null;
        this.participantId=null;
    }

    /**
     *
     * @param wrapper
     * @param permissionId
     * @param permissionLabel
     * this is used only for permission node
     */
    public PermissionUserObject(DefaultMutableTreeNode wrapper, String permissionId, String permissionLabel)
    {
        super(wrapper);
        this.permissionId = permissionId;
        setText(permissionLabel);
        setLeaf(false);
        nodeType = NODE_TYPE.PERMISSION;
        setBranchContractedIcon(ICON_PERMISSION);
        setBranchExpandedIcon(ICON_PERMISSION);
        setLeafIcon(ICON_PERMISSION);
        
        
        this.participant=null;
        this.participantId=null;
    }

    /**
     *
     * @param wrapper
     * @param participantId
     * @param participantLabel
     * @param permissionId
     * this is used only for ALL participant node
     */
    public PermissionUserObject(DefaultMutableTreeNode wrapper, String participantId, String participantLabel, String permissionId)
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
        
        this.participant=null;
        
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

    /**
      *
      */
    public String getBranchContractedIcon()
    {
        if (dirty && NODE_TYPE.PERMISSION.equals(nodeType))
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
        if (dirty && NODE_TYPE.PERMISSION.equals(nodeType))
        {
            return ICON_PERMISSION_MODIFIED;
        }

        return super.getBranchExpandedIcon();
    }

    @Override
    public String getIcon()
    {
        if (dirty && NODE_TYPE.PERMISSION.equals(nodeType))
        {
            return ICON_PERMISSION_MODIFIED;
        }

        return super.getIcon();
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
    public static enum NODE_TYPE
    {ORGANIZATION_UNSCOPED, ORGANIZATON_SCOPED_EXPLICIT, ORGANIZATON_SCOPED_IMPLICIT, PERMISSION, ROLE_SCOPED, ROLE_UNSCOPED, ROOT;
    }
}
