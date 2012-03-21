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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.viewscommon.common.ToolTip;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileUploadHelper;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.I18nFolderUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileUploadHelper.FUNCTION_TYPE;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileUploadHelper.FileUploadEvent;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;


import com.icesoft.faces.component.inputfile.FileInfo;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RepositoryFolderUserObject extends RepositoryResourceUserObject
{
   private static final long serialVersionUID = 1L;
   private MessagesViewsCommonBean propsBean;
   private String label = "";
   private Set<String> permissibleMimeTypes = new HashSet<String>();

   /**
    * custom constructor to initialize Folder user object
    * 
    * @param defaultMutableTreeNode
    * @param folder
    */
   public RepositoryFolderUserObject(DefaultMutableTreeNode defaultMutableTreeNode, Folder folder)
   {
      super(defaultMutableTreeNode, folder);

      String iconFile;
      propsBean = MessagesViewsCommonBean.getInstance();
      if (folder.getPath().equals(RepositoryUtility.CORRESPONDENCE_FOLDER))
      {
         iconFile = ResourcePaths.I_FOLDER_CORRESPONDANCE;
         permissibleMimeTypes.add("text/html");
      }
      else
      {
         iconFile = ResourcePaths.I_FOLDER;
      }
      setIcon(iconFile);
      defaultMutableTreeNode.setAllowsChildren(true);
      this.setLeaf(false);
      if (getFolder().getPath().equals("/"))
      {
         this.setEditable(false);
         this.setDeletable(false);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#rename
    * (java.lang.String)
    */
   public void rename(String newName)
   {
      if (!DocumentMgmtUtility.validateFileName(newName))
      {
         MessageDialog.addErrorMessage(propsBean.getString("views.common.invalidCharater.message"));
      }
      else
      {
         String oldName = getFolder().getName();
         try
         {
            getFolder().setName(newName);
            //update folder and resource
            setResource(DocumentMgmtUtility.getDocumentManagementService().updateFolder(getFolder()));
            this.setEditingName(false);
         }
         catch (Exception e)
         {
            getFolder().setName(oldName);
            this.setName(oldName);
            DocumentMgmtUtility.verifyExistenceOfFolderAndShowMessage(getFolder().getId(), "", e);
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#deleteResource()
    */
   public void deleteResource()
   {
      try
      {
         DocumentMgmtUtility.getDocumentManagementService().removeFolder(getFolder().getId(), true);
         this.wrapper.removeFromParent();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#createSubfolder()
    */
   public RepositoryResourceUserObject createSubfolder()
   {
      if(DocumentMgmtUtility.verifyExistenceOfFolderAndShowMessage(getFolder().getId(), "", null)){
         // create default folder
         DefaultMutableTreeNode node = RepositoryUtility.createSubfolder(this.wrapper, DocumentMgmtUtility
               .getNewFolderName());
         return (RepositoryResourceUserObject)node.getUserObject();
      }else{
         return null;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#createTextDocument()
    */
   public RepositoryResourceUserObject createTextDocument(String fileType)
   {
      try
      {
         if (RepositoryUtility.isFileTypePermissible(this.wrapper, fileType))
         {
            DefaultMutableTreeNode subNode = RepositoryUtility.createBlankDocument(this.wrapper, fileType);
            RepositoryResourceUserObject userObject = (RepositoryResourceUserObject) subNode.getUserObject();
            afterUpload(userObject);
            // userObject.renameStart();
            return (RepositoryResourceUserObject)subNode.getUserObject();
         }
      }
      catch (Exception e)
      {
         DocumentMgmtUtility.verifyExistenceOfFolderAndShowMessage(getFolder().getId(), "", e);
      }
      return null;
   }

   
   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#upload
    * ()
    */
   public void upload()
   {
      RepositoryUtility.expandTree(this.wrapper);
      this.wrapper.setAllowsChildren(true);
      // upload file
      FileUploadHelper fileUploadHelper = new FileUploadHelper(FUNCTION_TYPE.UPLOAD_ON_FOLDER, getFolder().getPath());
      fileUploadHelper.setHeaderMsg(propsBean.getParamString("common.uploadIntoFolder", getLabel()));
      ParametricCallbackHandler callbackHandler = new ParametricCallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (EventType.APPLY.equals(eventType))
            {
               handleFileUploadEvents(getParameters());
            }
         }
      };
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("userObject", this);
      callbackHandler.setParameters(params);
      fileUploadHelper.setCallbackHandler(callbackHandler);
      fileUploadHelper.uploadDocument();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#drop
    * (javax.swing.tree.DefaultMutableTreeNode)
    */
   public void drop(DefaultMutableTreeNode valueNode)
   {
      RepositoryResourceUserObject docUserObject = (RepositoryResourceUserObject) valueNode.getUserObject();
      Document draggedDocument = null;

      if (valueNode.getUserObject() instanceof RepositoryDocumentUserObject
            || valueNode.getUserObject() instanceof TypedDocumentUserObject)
      {
         draggedDocument = (Document) docUserObject.getResource();
      }
      else
      {
         return;
      }
      try
      {

         if (RepositoryUtility.isFileTypePermissible(this.wrapper, draggedDocument.getContentType()))
         {
            if (!valueNode.getParent().equals(this.wrapper))
            {
               Document existingDocument = DocumentMgmtUtility.getDocument(getResource().getPath(),
                     draggedDocument.getName());
               if (null != existingDocument)
               {
                  FileUploadHelper fileUploadHelper = new FileUploadHelper(FUNCTION_TYPE.DROP_ON_FOLDER, getResource()
                        .getPath());
                  Map<String, Object> params = new HashMap<String, Object>();
                  params.put("valueNode", valueNode);
                  ParametricCallbackHandler callbackHandler = new ParametricCallbackHandler()
                  {
                     public void handleEvent(EventType eventType)
                     {
                        if (EventType.APPLY.equals(eventType))
                        {
                           postDrop(getParameters());
                        }
                     }
                  };
                  callbackHandler.setParameters(params);
                  fileUploadHelper.setCallbackHandler(callbackHandler);
                  fileUploadHelper.updateVersion(existingDocument, draggedDocument);
               }
               else
               {
                  if (this.wrapper.isNodeRelated(valueNode))
                  {
                     RepositoryUtility.moveDocument(this.wrapper, valueNode);
                  }
                  else
                  {
                     RepositoryUtility.copyDocument(this.wrapper, valueNode);
                  }

                  afterUpload(docUserObject);
               }
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   private void postDrop(Map<String, Object> params)
   {
      DefaultMutableTreeNode valueNode = (DefaultMutableTreeNode) params.get("valueNode");
      RepositoryDocumentUserObject docUserObject = (RepositoryDocumentUserObject) valueNode.getUserObject();
      if (this.wrapper.isNodeRelated(valueNode))
      {
         docUserObject.deleteResource();
      }
      this.refresh();
      afterUpload(docUserObject);
   }
   
   /**
    * File Upload Dialog Event Handler
    * 
    * @param parameters
    */
   private void handleFileUploadEvents(Map<String, Object> parameters)
  {
      if (null != parameters)
      {
         // after file upload
         if (parameters.get(FileUploadHelper.EVENT).equals(FileUploadEvent.FILE_UPLOADED))
         {
            Document document = (Document) parameters.get(FileUploadHelper.DOCUMENT);
            DefaultMutableTreeNode subNode = RepositoryUtility.createDocumentNode(document);
            RepositoryDocumentUserObject userObject = (RepositoryDocumentUserObject) subNode.getUserObject();
            userObject.setNewNodeCreated(true);
            this.wrapper.add(subNode);
            // update process attachments
            afterUpload(userObject);
         }
         // after version upload
         if (parameters.get(FileUploadHelper.EVENT).equals(FileUploadEvent.VERSION_UPLOADED))
         {
            CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
            FileInfo fileInfo = fileUploadDialog.getFileInfo();
            DefaultMutableTreeNode childNode = RepositoryUtility.findNode(this.wrapper, fileInfo.getFileName(), true);
            RepositoryDocumentUserObject documentUserObject = (RepositoryDocumentUserObject) childNode.getUserObject();
            documentUserObject.refresh();
            //Process attachment update
            afterUpload(documentUserObject);
         }
         if (parameters.get(FileUploadHelper.EVENT).equals(FileUploadEvent.UPLOAD_ABORTED))
         {
            this.refresh();
         }
      }
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#refresh
    * ()
    */
   public void refresh()
   {
      RepositoryUtility.refreshNode(this.wrapper);
   }
   
   /**
    * @param iconFile
    */
   public void setIcon(String iconFile)
   {
      setBranchContractedIcon(iconFile);
      setBranchExpandedIcon(iconFile);
      setLeafIcon(iconFile);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#getLabel
    * ()
    */
   public String getLabel()
   {
      String label = this.label;
      if ("/".equals(getFolder().getPath()))
      {
         label = I18nFolderUtils.getLabel(I18nFolderUtils.ROOT);
      }
      else if (label == "")
      {
         label = I18nFolderUtils.getLabel(getFolder().getPath());
      }
      return label;
   }

   public void setLabel(String newLabel)
   {
      label = newLabel;
   }
  
   /**
    * placeholder to perform additional activity by subclass
    * 
    * @param document
    */
   public void afterUpload(RepositoryResourceUserObject document)
   {}

   /*
    * upload configuration (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#uploadFolder()
    */

   public void uploadFolder()
   {}


   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#openDocument()
    */
   public void openDocument()
   {
   // This method should never get invoked
   }

   /**
    * @return
    */
   public Folder getFolder()
   {
      return (Folder) this.getResource();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#versionHistory()
    */
   public void versionHistory()
   {
   // This method is not applicable for folders
   }

   public ToolTip getToolTip()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#isDownloadable()
    */
   public boolean isDownloadable()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#isDraggable()
    */
   public boolean isDraggable()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#isSupportsToolTip()
    */
   public boolean isSupportsToolTip()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#isRefreshable()
    */
   @Override
   public boolean isRefreshable()
   {
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#isCanCreateNote()
    */
   @Override
   public boolean isCanCreateNote()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#isSendFileAllowed()
    */
   @Override
   public boolean isSendFileAllowed()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#sendFile()
    */
   public void sendFile()
   {}

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#getType()
    */
   public ResourceType getType()
   {
      return ResourceType.FOLDER;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#createNote()
    */
   public void createNote()
   {}

   @Override
   public boolean isLeafNode()
   {
      return false;
   }

   @Override
   public Set<String> getPermissibleMimeTypes()
   {
      if (CollectionUtils.isNotEmpty(permissibleMimeTypes))
      {
         return this.permissibleMimeTypes;
      }
      else
      {
         return super.getPermissibleMimeTypes();
      }
   }

   @Override
   public void download()
   {}
}