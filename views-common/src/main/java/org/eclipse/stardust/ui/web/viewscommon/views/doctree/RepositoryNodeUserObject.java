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
package org.eclipse.stardust.ui.web.viewscommon.views.doctree;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.ui.web.viewscommon.common.ToolTip;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class RepositoryNodeUserObject extends RepositoryResourceUserObject
{
   private String label = "";
   private IRepositoryInstanceInfo repositoryInstance;
   private boolean defaultRepository;

   public RepositoryNodeUserObject(DefaultMutableTreeNode defaultMutableTreeNode,
         IRepositoryInstanceInfo repositoryInstance)
   {
      super(defaultMutableTreeNode);
      this.repositoryInstance = repositoryInstance;
      setEditable(false);
      setDeletable(false);
      setRepositoryNode(false);
      setIcon(ResourcePaths.I_REPOSITORY);
      setRepositoryNode(true);
   }

   /**
    * 
    */
   private static final long serialVersionUID = -5094662027401244956L;

   /**
    * @param iconFile
    */
   public void setIcon(String iconFile)
   {
      setBranchContractedIcon(iconFile);
      setBranchExpandedIcon(iconFile);
      setLeafIcon(iconFile);
   }

   public void switchDefaultRepository(ActionEvent event)
   {
      String repositoryId = (String) event.getComponent().getAttributes().get("repositoryId");
      DocumentMgmtUtility.getDocumentManagementService().setDefaultRepository(repositoryId);
      this.setDefaultRepository(true);
      RepositoryUtility.refreshNode(this.wrapper);
   }

   public void unbindRepository(RepositoryNodeUserObject userObject)
   {
      String repositoryId = userObject.getRepositoryInstance().getRepositoryId();
      DocumentMgmtUtility.getDocumentManagementService().unbindRepository(repositoryId);
      userObject.wrapper.removeFromParent();
   }

   @Override
   public void download()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void rename(String newName)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public RepositoryResourceUserObject createSubfolder()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public RepositoryResourceUserObject createTextDocument(String fileType)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void refresh()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void openDocument()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void deleteResource()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void upload()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void uploadFolder()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void sendFile()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void versionHistory()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void drop(DefaultMutableTreeNode valueNode)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public ResourceType getType()
   {
      return ResourceType.REPOSITORY;
   }

   @Override
   public String getLabel()
   {
      return label;
   }

   @Override
   public boolean isSupportsToolTip()
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean isDraggable()
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean isDownloadable()
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public ToolTip getToolTip()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean isRefreshable()
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean isCanCreateNote()
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean isSendFileAllowed()
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void createNote()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public boolean isLeafNode()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public void setLabel(String newLabel)
   {
      label = newLabel;
   }

   public IRepositoryInstanceInfo getRepositoryInstance()
   {
      return repositoryInstance;
   }

   public boolean isDefaultRepository()
   {
      return defaultRepository;
   }

   public void setDefaultRepository(boolean defaultRepository)
   {
      this.defaultRepository = defaultRepository;
   }

}
