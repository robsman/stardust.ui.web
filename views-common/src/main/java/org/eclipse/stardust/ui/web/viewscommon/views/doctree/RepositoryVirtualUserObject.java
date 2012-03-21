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

import java.io.File;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.viewscommon.common.NoteTip;
import org.eclipse.stardust.ui.web.viewscommon.common.ToolTip;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.CallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.PanelConfirmation;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RepositoryVirtualUserObject extends RepositoryResourceUserObject
{
   private static final long serialVersionUID = 8988270189849140756L;
   private String label = "";
   private boolean canCreateNotes = false;
   private MessagesViewsCommonBean propsBean;
   private ProcessInstance processInstance;
   private String resourcePath;
   private boolean leafNode = false;
 

/**
    * constructor - set default properties
    * 
    * @param defaultMutableTreeNode
    */
   public RepositoryVirtualUserObject(DefaultMutableTreeNode defaultMutableTreeNode)
   {
      super(defaultMutableTreeNode, null);
      propsBean = MessagesViewsCommonBean.getInstance();
      this.setLeaf(false);
   }
   
   
   /**
    * download file or folder as a zip file
    */
   @Override
   public void download()
   {
      if(null != resourcePath){
         Folder resource = DocumentMgmtUtility.getFolder(resourcePath);
         DownloadPopupDialog downloadPopupDialog = DownloadPopupDialog.getCurrent();
         OutputResource outputResource = new OutputResource(resource.getName(), resource.getId(), this.getMTypeStr(),
               downloadPopupDialog, DocumentMgmtUtility.getDocumentManagementService(), false);
         downloadPopupDialog.open(outputResource);   
      }
   }


   public void setLabel(String label)
   {
      this.label = label;
   }

   public void setCanCreateNotes(boolean canCreateNotes)
   {
      this.canCreateNotes = canCreateNotes;
   }

   @Override
   public RepositoryResourceUserObject createSubfolder()
   {
      return null;
   }

   @Override
   public RepositoryResourceUserObject createTextDocument(String fileType)
   {
      return null;
   }

   @Override
   public void deleteResource()
   {}

   @Override
   public String getLabel()
   {
      return label;
   }

   @Override
   public ResourceType getType()
   {
      return ResourceType.VIRTUAL;
   }

   @Override
   public void openDocument()
   {
      if (null != resourcePath)
      {
         DocumentViewUtil.openFileSystemDocument(resourcePath, getLabel(), false);
      }
   }

   @Override
   public void refresh()
   {}

   @Override
   public void rename(String newName)
   {}

   @Override
   public void upload()
   {}

   public void uploadFolder()
   {
      // create callback handler
      CallbackHandler callbackHandler = new CallbackHandler(null)
      {
         public void handleEvent(EventType eventType)
         {
            try
            {
               updateConfiguration();
            }
            catch (Exception e)
            {
               ExceptionHandler.handleException(e);
            }
         }
      };
      // create panelpopup
      PanelConfirmation panelConfirmation = PanelConfirmation.getInstance(true);
      panelConfirmation.setCallbackHandler(callbackHandler);
      panelConfirmation.setMessage(MessagesViewsCommonBean.getInstance().getString(
            "views.genericRepositoryView.replaceFoldersConfirmation"));
      panelConfirmation.openPopup();
   }
   
   /*
    * upload configuration (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#uploadFolder()
    */
   private void updateConfiguration()
   {
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
      fileUploadDialog.initialize();
      fileUploadDialog.setHeaderMessage(propsBean.getParamString("common.uploadIntoFolder", getLabel()));
      fileUploadDialog.setTitle(propsBean.getString("common.fileUpload"));
      fileUploadDialog.setViewDescription(false);
      fileUploadDialog.setViewComment(false);
      fileUploadDialog.setViewDocumentType(false);
      fileUploadDialog.setEnableOpenDocument(false);
      fileUploadDialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType == EventType.APPLY)
            {
               CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
               try
               {
                  loadConf(fileUploadDialog.getFileInfo().getFile());
               }
               catch (Exception e)
               {
               }
            }
         }
      });
      fileUploadDialog.openPopup();
   }

   @Override
   public void versionHistory()
   {}

   public void setIcon(String iconFile)
   {
      setBranchContractedIcon(iconFile);
      setBranchExpandedIcon(iconFile);
      setLeafIcon(iconFile);
   }

   public ToolTip getToolTip()
   {
      return null;
   }
   
   @Override
   public boolean isDownloadable()
   {
      return false;
   }

   @Override
   public boolean isDraggable()
   {
      return false;
   }

   @Override
   public boolean isSupportsToolTip()
   {
      return false;
   }

   @Override
   public boolean isRefreshable()
   {
      return false;
   }

   @Override
   public boolean isCanCreateNote()
   {
      return this.canCreateNotes;
   }

   @Override
   public boolean isSendFileAllowed()
   {
      return false;
   }

   @Override
   public void sendFile()
   {}

   @Override
   public void drop(DefaultMutableTreeNode valueNode)
   {}

   /**
    * @param processInstance
    */
   public void setProcessInstance(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }

   @Override
   public void createNote()
   {
      if (null != processInstance)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put("createNote", "true");
         ProcessInstanceUtils.openNotes(processInstance, params);
      }
   }

   @Override
   public boolean isLeafNode()
   {
      return this.leafNode;
   }
   
   public void setLeafNode(boolean leafNode)
   {
      this.leafNode = leafNode;
   }

   
   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }
   
   public void setResourcePath(String resourcePath)
   {
      this.resourcePath = resourcePath;
   }

   /**
    * loads old configuration
    * 
    * @throws Exception
    * 
    */
   private void loadConf(File uploadedFile) throws Exception
   {
      if ("zip".equalsIgnoreCase(StringUtils.substringAfterLast(uploadedFile.getName(), ".")))
      {
         DocumentMgmtUtility.loadFromZipFile(resourcePath, uploadedFile);
         if (this.wrapper.isRoot())
         {
            GenericRepositoryTreeViewBean tree = GenericRepositoryTreeViewBean.getInstance();
            tree.initialize();
         }
         else
         {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.wrapper.getParent();
            int index = parentNode.getIndex(this.wrapper);
            parentNode.remove(index);
            ((RepositoryFolderUserObject) parentNode.getUserObject()).refresh();
         }
      }
      else
      {
         MessageDialog.addErrorMessage(propsBean.getString("views.genericRepositoryView.fileUploadError"));
      }
   }
}