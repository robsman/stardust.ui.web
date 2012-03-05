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

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.viewscommon.common.NoteTip;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RepositoryDocumentUserObject extends RepositoryResourceUserObject
{
   private static final long serialVersionUID = 1296615449302077105L;
   private static final String DOCUMENT_TEMPLATE_PROPERTY = "DocumentTemplate";
   private static final String CORRESPONDENCE_TEMPLATE_TEMPLATE = "CorrespondenceTemplate";
   private boolean opened;
   private MIMEType mType = MimeTypesHelper.DEFAULT;
   private boolean sendFileAllowed = false;
   private MessagesViewsCommonBean propsBean;

   /**
    * custom constructor initialing document user object
    * 
    * @param defaultMutableTreeNode
    * @param document
    */
   public RepositoryDocumentUserObject(DefaultMutableTreeNode defaultMutableTreeNode, Document document)
   {
      super(defaultMutableTreeNode, document);
      propsBean = MessagesViewsCommonBean.getInstance();
      this.mType = MimeTypesHelper.detectMimeType(document.getName(), document.getContentType());
      Map<String, Serializable> properties = document.getProperties();

      if (null != properties && properties.containsKey(DOCUMENT_TEMPLATE_PROPERTY)
            && properties.get(DOCUMENT_TEMPLATE_PROPERTY).equals(CORRESPONDENCE_TEMPLATE_TEMPLATE))
      {
         setLeafIcon(ResourcePaths.I_DOCUMENT);
      }
      else
      {
         setLeafIcon(ResourcePaths.I_DOCUMENT_PATH + this.mType.getIconPath());
      }
      
      defaultMutableTreeNode.setAllowsChildren(false);
      this.setLeaf(true);
      this.setCanUploadFile(false);
      
      if (null != properties)
      {
         Map<String, Object> correspondenceMetaData = (Map<String, Object>) properties
               .get(CommonProperties.FAX_EMAIL_MESSAGE_INFO);
         if ((null != correspondenceMetaData && !correspondenceMetaData.isEmpty())
               || properties.containsKey("chatProtocolInfo"))
         {
            setEditable(false);
         }
      }
   }

   @Override
   public void rename(String newName)
   {
      try
      {
         if (DocumentMgmtUtility.isDocumentExtensionChanged(newName, getDocument().getName()))
         {
            RenameDocumentExtensionDialog.getCurrent().open(this);
         }
         else
         {
            saveRename();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e,
               propsBean.getString("views.genericRepositoryView.renameNotPermitted"));
      }
   }

   @Override
   public void download()
   {
      DownloadPopupDialog downloadPopupDialog = DownloadPopupDialog.getCurrent();
      Document document = getDocument();
      OutputResource resource = new OutputResource(document.getName(), document.getId(), getMType().toString(),
            downloadPopupDialog, getDMS(), true);
      downloadPopupDialog.open(resource);
   }

   @Override
   public void openDocument()
   {
      if (isReadable())
      {
         Map<String, Object> params = CollectionUtils.newMap();
         params.put("processInstance", getProcessInstance());
         //should fetch latest document
         DocumentViewUtil.openJCRDocument(getDocument().getId(), params);
      }
      else
      {
         MessageDialog.addErrorMessage(propsBean.getString("views.genericRepositoryView.permissionDenied"));
      }
   }
   
   

   @Override
   public void deleteResource()
   {
      try
      {
         // delete resource from process instance if applicable
         ProcessInstance processInstance = getProcessInstance();
         if (null != processInstance)
         {
            DMSHelper.deleteProcessAttachment(processInstance, getDocument());
         }
         // delete resource from repository
         getDMS().removeDocument(getDocument().getId());
         this.wrapper.removeFromParent();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e,
               propsBean.getString("views.genericRepositoryView.processAttachmntDltErr"));
      }
   }

   @Override
   public void refresh()
   {
      RepositoryUtility.refreshNode(this.wrapper);
   }

   @Override
   public ResourceType getType()
   {
      return ResourceType.DOCUMENT;
   }

   @Override
   public void versionHistory()
   {
      DocumentVersionDialog documentVersionDialog = DocumentVersionDialog.getCurrent();
      documentVersionDialog.open(getDocument());
   }

   @Override
   public RepositoryResourceUserObject createSubfolder()
   {
      // This method should never get invoked
      return null;
   }

   /**
    * Upload new version
    */
   @Override
   public void upload()
   {
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
      fileUploadDialog.initialize();
      fileUploadDialog.setTitle(propsBean.getString("views.documentView.saveDocumentDialog.uploadNewVersion.label"));
      fileUploadDialog.setHeaderMessage(propsBean.getParamString(
            "views.documentView.saveDocumentDialog.uploadNewVersion.text", getDocument().getName()));
      fileUploadDialog.setDocumentType(getDocument().getDocumentType());
      fileUploadDialog.setOpenDocument(true);
      fileUploadDialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType == EventType.APPLY)
            {
               try
               {
                  saveVersion();
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
   public RepositoryResourceUserObject createTextDocument(String fileType)
   {
      // This method should never get invoked
      return null;
   }

   /**
    * also gets invoked when file extension is changed
    */
   public void saveRename()
   {
      Document document = getDocument();
      String oldName = document.getName();

      try
      {
         document.setName(this.getName());
         document = getDMS().updateDocument(document, false, "", false);
         updateprocessInstance(document);
         this.setText(this.getName());
      }
      catch(Exception e)
      {
         document.setName(oldName);
         this.setName(oldName);
         DocumentMgmtUtility.verifyExistanceOfDocumentAndShowMessage(document.getId(), "", e);         
      }
      this.setEditingName(false);
      RepositoryUtility.refreshNode(this.wrapper);
   }

   @Override
   public void sendFile()
   {
      Map<String, Object> params = CollectionUtils.newTreeMap();
      ProcessInstance processInstance = getProcessInstance();
      if (null != processInstance)
      {
         params.put("processInstanceOID", Long.toString(processInstance.getOID()));
      }
      params.put("attachment", getDocument());
      PortalApplication.getInstance().openViewById("correspondenceView", "DocumentID=" + getDocument().getId(),
            params, null, true);
   }

   public Document getDocument()
   {
      return (Document) this.getResource();
   }

   @Override
   public String getLabel()
   {
      return getDocument().getName();
   }

   public boolean isOpened()
   {
      return opened;
   }

   public void setOpened(boolean opened)
   {
      this.opened = opened;
   }

   public MIMEType getMType()
   {
      return mType;
   }

   @Override
   public String getMTypeStr()
   {
      return mType.toString();
   }

   @Override
   public void uploadFolder()
   {
   // This method should never get invoked
   }

   @Override
   public boolean isDraggable()
   {
      return true;
   }

   @Override
   public boolean isSupportsToolTip()
   {
      return false;
   }

   @Override
   public boolean isDownloadable()
   {
      if (isReadable())
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   @Override
   public NoteTip getNoteTip()
   {
      return null;
   }

   @Override
   public boolean isCanCreateFile()
   {
      return false;
   }

   @Override
   public boolean isRefreshable()
   {
      return true;
   }

   @Override
   public boolean isCanCreateNote()
   {
      return false;
   }

   public void setSendFileAllowed(boolean sfa)
   {
      this.sendFileAllowed = sfa;
   }

   @Override
   public boolean isSendFileAllowed()
   {
      return this.sendFileAllowed;
   }

   @Override
   public void drop(DefaultMutableTreeNode valueNode)
   {}

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#createNote()
    */
   public void createNote()
   {}

   /**
    * save document
    * 
    * @throws DocumentManagementServiceException
    * @throws IOException
    */
   public void saveVersion() throws DocumentManagementServiceException, IOException
   {
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
      String fileName = fileUploadDialog.getFileInfo().getFileName();
      try
      {
         Document document = getDocument();
         document.setName(fileName);
         document.setContentType(fileUploadDialog.getFileInfo().getContentType());
         document = DocumentMgmtUtility.updateDocument(document,
               DocumentMgmtUtility.getFileSystemDocumentContent(fileUploadDialog.getFileInfo().getPhysicalPath()),
               fileUploadDialog.getDescription(), fileUploadDialog.getComments());
         updateprocessInstance(document);
         RepositoryUtility.refreshNode(this.wrapper);
         if (fileUploadDialog.isOpenDocument())
         {
            DocumentViewUtil.openJCRDocument(document);
         }
      }
      catch (Exception e)
      {
         try
         {
            this.setResource(DocumentMgmtUtility.getDocument(getResource().getId()));
         }
         catch (ResourceNotFoundException e1)
         {
         }
         DocumentMgmtUtility.verifyExistanceOfDocumentAndShowMessage(getDocument().getId(), "", e);
      }
   }

   /**
    * Update document into process instance
    * 
    * @param document
    */
   private void updateprocessInstance(Document document)
   {
      ProcessInstance processInstance = getProcessInstance();
      if (null != processInstance)
      {
         DMSHelper.updateProcessAttachment(processInstance, document);
      }
   }

   /**
    * @return
    */
   private ProcessInstance getProcessInstance()
   {
      DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.wrapper.getParent();
      if (parentNode.getUserObject() instanceof ProcessAttachmentUserObject)
      {
         return ((ProcessAttachmentUserObject) parentNode.getUserObject()).getProcessInstance();
      }
      else
         return null;
   }

   private static DocumentManagementService getDMS()
   {
      return DocumentMgmtUtility.getDocumentManagementService();
   }

   @Override
   public boolean isLeafNode()
   {
      return true;
   }
}