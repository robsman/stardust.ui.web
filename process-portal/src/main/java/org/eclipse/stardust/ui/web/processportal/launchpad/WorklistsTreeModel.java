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

import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
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
         
         addAssemblyLineChild(reload);

         Set<ParticipantInfo> participants = ParticipantWorklistCacheManager.getInstance().getWorklistParticipants();
         for (ParticipantInfo participantInfo : participants)
         {
            if (assemblyLineUserObject.isAssemblyLineMode()
                  && assemblyLineUserObject.getAssemblyLineParticipants().contains(participantInfo.getId()))
            {
               continue;
            }
            
            addChild(participantInfo, true);
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param participantWorklist
    * @param isLeaf
    */
   private void addChild(ParticipantInfo participantInfo, boolean isLeaf)
   {
      if (showEmptyWorklist || ParticipantWorklistCacheManager.getInstance().getWorklistCount(participantInfo) > 0)
      {
         DefaultMutableTreeNode child = new DefaultMutableTreeNode();
         WorklistsTreeUserObject childUserObject = new WorklistsTreeUserObject(child);
   
         childUserObject.setModel(participantInfo);
         childUserObject.setLeaf(isLeaf);
   
         child.setUserObject(childUserObject);
         root.add(child);
      }
   }
   
   /**
    * @param reload
    * @return
    */
   private WorklistsTreeAssemblyLineUserObject addAssemblyLineChild(boolean reload)
   {
      DefaultMutableTreeNode child = new DefaultMutableTreeNode();
      if (reload || null == assemblyLineUserObject)
      {
         assemblyLineUserObject = new WorklistsTreeAssemblyLineUserObject(child, worklistsBean);
      }

      if(assemblyLineUserObject.isAssemblyLineMode())
      {
         if (showEmptyWorklist || assemblyLineUserObject.getActivityCount() > 0)
         {
            child.setUserObject(assemblyLineUserObject);
            root.add(child);
         }
      }
      
      return assemblyLineUserObject;
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
