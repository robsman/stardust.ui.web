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

import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Resource;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.ui.web.viewscommon.common.ToolTip;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;

import com.icesoft.faces.component.tree.IceUserObject;
import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Highlight;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public abstract class RepositoryResourceUserObject extends IceUserObject
{
   private static final long serialVersionUID = 5221739219683823073L;

   public enum ResourceType {
      FOLDER, DOCUMENT, VIRTUAL, NOTE, REPOSITORY
   }
   
   private Resource resource;
   private boolean isEditingName;
   private String name;
   private Effect resourceFoundEffect = new Highlight("#fda505");
   private boolean newNodeCreated;
   private Boolean isCanCreateFolder;
   private Boolean isReadable;
   private Boolean isDeletable;
   private Boolean isEditable;
   private Boolean isReadACL;
   private Boolean isModifyACL;
   
   private Boolean isCanCreateFile;
   private Boolean canUploadFile;

   private boolean isMenuPopupApplicable = true;
   private boolean isRepositoryNode = false;
   private boolean repositoryRootNode = false;
   private boolean isVersioningSupported = true;
   private boolean isWriteSupported = true;

   /**
    * custom constructor initialized a user object specific to node
    * 
    * @param defaultMutableTreeNode
    * @param resource
    */
   public RepositoryResourceUserObject(DefaultMutableTreeNode defaultMutableTreeNode, Resource resource)
   {
      super(defaultMutableTreeNode);
      if (null != resource)
      {
         setResource(resource);
         setName(resource.getName());
         setExpanded(false);
      }
      this.resourceFoundEffect.setFired(true);
   }

   /**
    * custom constructor initialized a user object specific to node
    * 
    * @param defaultMutableTreeNode
    * @param resource
    */
   public RepositoryResourceUserObject(DefaultMutableTreeNode defaultMutableTreeNode)
   {
      super(defaultMutableTreeNode);
      this.resourceFoundEffect.setFired(true);
   }

   /**
    * gets invoked when user confirm the rename resource to rename the resource
    */
   public void renameAccept()
   {
      String newName = DocumentMgmtUtility.stripOffSpecialCharacters(getName().trim());
      String msgKey = null;
      
      // Trim the name and set it back for further use
      if (StringUtils.isNotEmpty(getName()))
      {
         setName(getName().trim());
      }

      if (StringUtils.isEmpty(getName()))
      {
         msgKey = "views.common.name.label";
      }
      else if (!DocumentMgmtUtility.validateFileName(getName()))
      {
         msgKey = "views.common.invalidCharater.message";
      }
      else if (getResource() != null)
      {
         String oldName = getResource().getName();
         if (!newName.equals(oldName))
         {
            String path = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringBeforeLast(getResource().getPath(), "/");
            if (DocumentMgmtUtility.isFolderPresent(path, newName))
            {
               msgKey = "views.genericRepositoryView.folderAlreadyPresent";
            }
            if (null != DocumentMgmtUtility.getDocument(path, newName))
            {
               msgKey = "views.genericRepositoryView.fileAlreadyPresent";
            }
         }
      }
      if (msgKey != null)
      {
         //setName(this.resource.getName());
         RepositoryUtility.showErrorPopup(msgKey, this.resource.getName(), null);
      }
      else
      {
         this.rename(newName);
      }
   }

   /**
    * gets invoked when user declines renaming the resource
    */
   public void renameDecline()
   {
      setName(getResource().getName());
      this.setEditingName(false);
   }

   /**
    * triggers the rename resource flow
    */
   public void renameStart()
   {
      this.setEditingName(true);
   }

   /**
    * download file or folder as a zip file
    */
   public abstract void download();

   // for document/Folder Security
   public boolean isCanCreateFolder()
   {
      if (null == isCanCreateFolder)
      {
         isCanCreateFolder = getPrivilege(DmsPrivilege.CREATE_PRIVILEGE);
      }
      return isCanCreateFolder;
   }
   
   public boolean isReadable(){
      if (null == isReadable)
      {
         isReadable = getPrivilege(DmsPrivilege.READ_PRIVILEGE);
      }
      return isReadable;
   }
   public boolean isDeletable(){
      if (null == isDeletable)
      {
         isDeletable = getPrivilege(DmsPrivilege.DELETE_PRIVILEGE);
      }
      return isDeletable;
   }
   public boolean isEditable(){
      if (null == isEditable)
      {
         isEditable = getPrivilege(DmsPrivilege.MODIFY_PRIVILEGE);
      }
      return isEditable;
   }
   public boolean isReadACL(){
      if (null == isReadACL)
      {
         isReadACL = getPrivilege(DmsPrivilege.READ_ACL_PRIVILEGE);
      }
      return isReadACL;   
   }
   public boolean isModifyACL(){
      if (null == isModifyACL)
      {
         isModifyACL = getPrivilege(DmsPrivilege.MODIFY_ACL_PRIVILEGE);
      }
      return isModifyACL;
   }
   
   public boolean isCanCreateFile()
   {
      if (null == isCanCreateFile)
      {
         isCanCreateFile = getPrivilege(DmsPrivilege.CREATE_PRIVILEGE);
      }
      return isCanCreateFile;
   }
   
   public boolean isCanUploadFile()
   {
      if (null == canUploadFile)
      {
         canUploadFile = getPrivilege(DmsPrivilege.CREATE_PRIVILEGE);
      }
      return canUploadFile;
   }

   /**
    * @param dms
    * @return
    */
   private boolean getPrivilege(DmsPrivilege dms){
      if(null != getResource()){
         return DMSHelper.hasPrivilege(getResource().getId(), dms);   
      }else{
         return false;
      }
   }
   
   public Resource getResource()
   {
      return this.resource;
   }

   public void setResource(Resource resource)
   {
      this.resource = resource;
   }

   public String getTypeString()
   {
      return getType().toString();
   }

   public boolean isEditingName()
   {
      return isEditingName;
   }

   public void setEditingName(boolean isEditingName)
   {
      this.isEditingName = isEditingName;
   }

   public Effect getResourceFoundEffect()
   {
      return this.resourceFoundEffect;
   }

   public void setResourceFoundEffect(Effect resourceFoundEffect)
   {
      this.resourceFoundEffect = resourceFoundEffect;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public boolean isNewNodeCreated()
   {
      return newNodeCreated;
   }

   public void setNewNodeCreated(boolean newNodeCreated)
   {
      this.newNodeCreated = newNodeCreated;
   }

   public String getMTypeStr()
   {
      return "";
   }

   public Set<String> getPermissibleMimeTypes()
   {
      return null;
   }

   public void setEditable(boolean editable){
      isEditable = editable; 
      if(isEditable){
         
      }
   }
   
   public void setDeletable(boolean deletable){
      isDeletable = deletable; 
   }
   
   public boolean isMenuPopupApplicable()
   {
      return isMenuPopupApplicable;
   }

   public void setMenuPopupApplicable(boolean isMenuPopupAvailable)
   {
      this.isMenuPopupApplicable = isMenuPopupAvailable;
   }
   
   public void setCanCreateFile(Boolean isCanCreateFile)
   {
      this.isCanCreateFile = isCanCreateFile;
   }

   public void setCanCreateFolder(Boolean canCreateFolder)
   {
      this.isCanCreateFolder = canCreateFolder;
   }
   
   public void setCanUploadFile(Boolean canUploadFile)
   {
      this.canUploadFile = canUploadFile;
   }
   
   public boolean isRepositoryNode()
   {
      return isRepositoryNode;
   }

   public void setRepositoryNode(boolean isRepositoryNode)
   {
      this.isRepositoryNode = isRepositoryNode;
   }

   public boolean isRepositoryRootNode()
   {
      return repositoryRootNode;
   }

   public void setRepositoryRootNode(boolean repositoryRootNode)
   {
      this.repositoryRootNode = repositoryRootNode;
   }
   
   public boolean isVersioningSupported()
   {
      return isVersioningSupported;
   }

   public void setVersioningSupported(boolean isVersioningSupported)
   {
      this.isVersioningSupported = isVersioningSupported;
   }

   public boolean isWriteSupported()
   {
      return isWriteSupported;
   }

   public void setWriteSupported(boolean isWriteSupported)
   {
      this.isWriteSupported = isWriteSupported;
   }
   
   public boolean isDetachable()
   {
      return false;
   }
   
   public void detachResource()
   {
      throw new UnsupportedOperationException();
   }
   
   /**
    * Rename the resource
    * 
    * @param newName
    */
   public abstract void rename(String newName);

   /**
    * creates subfolder/child node
    * 
    * @throws IllegalAccessException
    */
   public abstract RepositoryResourceUserObject createSubfolder();

   /**
    * creates blank text document
    */
   public abstract RepositoryResourceUserObject createTextDocument(String fileType);

   /**
    * refreshes the resource and it expanded children nodes (folders and files) if any
    */
   public abstract void refresh();

   /**
    * Opens the document in editor
    */
   public abstract void openDocument();

   /**
    * deletes the resource
    */
   public abstract void deleteResource();

   /**
    * triggers the file upload operation
    */
   public abstract void upload();

   /**
    * triggers the folder upload operation
    */
   public abstract void uploadFolder();

   /**
    * Send File
    */
   public abstract void sendFile();

   /**
    * Displays Version History
    */
   public abstract void versionHistory();


   /**
    * This method handles the drag and drop operation
    * @param valueNode
    */
   public abstract void drop(DefaultMutableTreeNode valueNode); 

   public abstract ResourceType getType();

   public abstract String getLabel();

   public abstract boolean isSupportsToolTip();

   public abstract boolean isDraggable();

   public abstract boolean isDownloadable();

   public abstract ToolTip getToolTip();

   public abstract boolean isRefreshable();

   public abstract boolean isCanCreateNote();

   public abstract boolean isSendFileAllowed();

   public abstract void createNote();

   public abstract boolean isLeafNode();
}
