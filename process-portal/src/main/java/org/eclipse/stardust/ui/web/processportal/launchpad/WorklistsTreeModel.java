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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.ParticipantLabel;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;



/**
 * @author roland.stamm
 * 
 */
public class WorklistsTreeModel extends DefaultTreeModel
{

   private static final long serialVersionUID = 2628088975263277461L;

   private WorklistsTreeRoot root = null;
   private WorklistsTreeAssemblyLineUserObject assemblyLineUserObject;

   private boolean showEmptyWorklist;
   private WorklistsBean worklistsBean;
   private List<String> expandedUserObjects;
   
   /**
    * @param root
    * @param worklistsBean
    */
   public WorklistsTreeModel(WorklistsTreeRoot root, WorklistsBean worklistsBean)
   {
      super(root);
      this.root = root;
      this.worklistsBean = worklistsBean;
   }

   /**
    * @param root
    * @param asksAllowsChildren
    * @param worklistsBean
    */
   public WorklistsTreeModel(WorklistsTreeRoot root, boolean asksAllowsChildren, WorklistsBean worklistsBean)
   {
      super(root, asksAllowsChildren);
      this.root = root;
      this.worklistsBean = worklistsBean;
   }

   /**
    * @param root
    * @param showEmptyWorklist
    * @param text
    */
   public WorklistsTreeModel(WorklistsTreeRoot root, boolean showEmptyWorklist, String text, WorklistsBean worklistsBean)
   {
      this(root, worklistsBean);
      this.showEmptyWorklist = showEmptyWorklist;
   }

   /**
    * 
    */
   public void clear()
   {
      cacheCurrentNodeState();
      root.removeAllChildren();
   }
   
   /**
    * @param reload
    */
   public void update(boolean reload)
   {
      try
      {
         clear();
         if (reload)
         {
            ParticipantWorklistCacheManager.getInstance().reset();
         }
         Map<String,Set<ParticipantInfo>> participantMap = ParticipantWorklistCacheManager.getInstance().getWorklistParticipants();
         
         for (Entry<String, Set<ParticipantInfo>> entry : participantMap.entrySet())
         {
            DefaultMutableTreeNode tempRootNode = null;
            DefaultMutableTreeNode assemblyLineNode = null;
            Set<ParticipantInfo> participants = entry.getValue();
            if (null == assemblyLineNode)
            {
               assemblyLineNode = initAssemblyLineChild(reload);
            }
            for (ParticipantInfo participantInfo : participants)
            {
               boolean assemblyNodeCreated = false;
               if (participantInfo.getQualifiedId().equals(entry.getKey()) && (participantInfo instanceof UserInfo))
               {
                  tempRootNode = addChild(participantInfo, false, root);
               }
               
               if (assemblyLineUserObject.isAssemblyLineMode()
                     && assemblyLineUserObject.getAssemblyLineParticipants().contains(participantInfo.getQualifiedId()))
               {
                  if (!assemblyNodeCreated)
                  {
                     assemblyLineNode = addAssemblyLineChild(reload, tempRootNode, assemblyLineNode);
                  }
                  assemblyNodeCreated = true;
                  continue;
               }
               DefaultMutableTreeNode childNode = addChild(participantInfo, true, tempRootNode);
               if(null == childNode)
               {
                  continue;
               }
               if(entry.getKey().equals(participantInfo.getQualifiedId()) && (participantInfo instanceof UserInfo))
               {
                  ((WorklistsTreeUserObject)childNode.getUserObject()).setText(MessagePropertiesBean.getInstance().getString("launchPanels.worklists.personalWorklist")+ " :");
               }

            }
            
            if (tempRootNode != null)
            {   
               WorklistsTreeUserObject rootUserObject = (WorklistsTreeUserObject) tempRootNode.getUserObject();
               ParticipantLabel label = ModelHelper.getParticipantLabel(rootUserObject.getParticipantInfo());
               rootUserObject.setText(label.getWrappedLabel() + ": ");
            }
         }
         
         restoreNodeState();
         
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
	/**
	 * Append the child to specified Root Node
	 * 
	 * @param participantInfo
	 * @param isLeaf
	 *            - For Current User/deputy User -isLeaf is false, else true
	 * @param rootNode
	 * @return
	 */
   private DefaultMutableTreeNode addChild(ParticipantInfo participantInfo, boolean isLeaf,
         DefaultMutableTreeNode rootNode)
   {
      String userParticipantId =null;
      if(isLeaf)
      {
         userParticipantId =((WorklistsTreeUserObject) rootNode.getUserObject()).getParticipantInfo().getQualifiedId();   
      }
      else
      {
         userParticipantId = participantInfo.getQualifiedId();
      }
      if (showEmptyWorklist
            || (ParticipantWorklistCacheManager.getInstance().getWorklistCount(participantInfo, userParticipantId) > 0 || !isLeaf))
      {
         DefaultMutableTreeNode child = new DefaultMutableTreeNode();
         WorklistsTreeUserObject childUserObject = new WorklistsTreeUserObject(child);
         childUserObject.setModel(participantInfo);
         childUserObject.setLeaf(isLeaf);
         childUserObject.setUserParticipantId(userParticipantId);
         child.setUserObject(childUserObject);
         rootNode.add(child);
         return child;
      }
      return null;
   }
   
   /**
    * Append the child to specified Root Node
    * @param reload
    * @param rootNode
    * @param child
    * @return
    */
   private DefaultMutableTreeNode addAssemblyLineChild(boolean reload,DefaultMutableTreeNode rootNode,DefaultMutableTreeNode child)
   {
     
      if(assemblyLineUserObject.isAssemblyLineMode())
      {
         if (showEmptyWorklist || Long.valueOf(assemblyLineUserObject.getActivityCount()) > 0)
         {
            child.setUserObject(assemblyLineUserObject);
            rootNode.add(child);
         }
         return child;
      }
      
      return null;
   }
   
   /**
    * 
    * @param reload
    * @return
    */
   private DefaultMutableTreeNode initAssemblyLineChild(boolean reload)
   {
      DefaultMutableTreeNode child = new DefaultMutableTreeNode();
      if (reload || null == assemblyLineUserObject)
      {
         assemblyLineUserObject = new WorklistsTreeAssemblyLineUserObject(child, worklistsBean);
      }
      return child;
   }

   /**
    * Cache the current Node state while Refresh/Show all worklist -before clean
    */
   private void cacheCurrentNodeState()
   {
      expandedUserObjects = CollectionUtils.newArrayList();
      int permissionNodesCount = root.getChildCount();
      for (int i = 0; i < permissionNodesCount; i++)
      {
         DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) root.getChildAt(i);
         WorklistsTreeUserObject userObject = (WorklistsTreeUserObject) childNode.getUserObject();
         if (userObject.isExpanded())
         {
            expandedUserObjects.add(userObject.getParticipantInfo().getQualifiedId());
         }
      }
   }

   /**
    * 
    */
   private void restoreNodeState()
   {
    int permissionNodesCount = root.getChildCount();
      for (int i = 0; i < permissionNodesCount; i++)
      {
         DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) root.getChildAt(i);
         WorklistsTreeUserObject userObject = (WorklistsTreeUserObject)childNode.getUserObject();
         if(userObject.getParticipantInfo()!=null && expandedUserObjects.contains(userObject.getParticipantInfo().getQualifiedId()))
          {
            userObject.setExpanded(true);
          }
      }
   }

   public boolean isEmpty()
   {
      return root.getChildCount() < 1;
   }

   public boolean isShowEmptyWorklist()
   {
      return showEmptyWorklist;
   }

   public void setShowEmptyWorklist(boolean showEmptyWorklist)
   {
      this.showEmptyWorklist = showEmptyWorklist;
   }

   public WorklistsTreeAssemblyLineUserObject getAssemblyLineUserObject()
   {
      return assemblyLineUserObject;
   }
}
