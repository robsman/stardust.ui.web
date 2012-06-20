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

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.PanelConfirmation;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility.NodeType;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;


/**
 * 
 * @author Yogesh.Manware
 * 
 */
public class ReclassifyDocumentBean extends PanelConfirmation
{

   private boolean moveDocumentToAttachments;
   private static final long serialVersionUID = 1L;
   private DefaultMutableTreeNode sourceNode;
   private DefaultMutableTreeNode targetNode;
   private String msgLine2;
   private NodeType sourceNodeType;
   private NodeType targetNodeType;
   
   private boolean documentTypeChanged;
   
   private String description;
   private String comments;
   
   // To show/hide the description and comment section on the dialog (page section link
   // action)
   private boolean showDescription;
   private boolean showComment;
   

   /**
    * pass reInitialize=true if reset required
    * 
    * @param reInitialize
    * @return
    */
   public static ReclassifyDocumentBean getInstance(Boolean reInitialize)
   {
      ReclassifyDocumentBean reclassifyDocumentBean = (ReclassifyDocumentBean) FacesUtils
            .getBeanFromContext("reclassifyDocumentBean");
      if (reInitialize)
      {
         reclassifyDocumentBean.reset();
      }

      return reclassifyDocumentBean;
   }

   @Override
   public void reset()
   {
      msgLine2 = "";
      moveDocumentToAttachments = false;
      documentTypeChanged = false;
      description = "";
      comments = "";
      super.reset();
   }

   public boolean isMoveDocumentToAttachments()
   {
      return moveDocumentToAttachments;
   }

   public NodeType getClassificationType()
   {
      return sourceNodeType;
   }

   public void initialize(DefaultMutableTreeNode sourceNode, DefaultMutableTreeNode targetNode)
   {
      this.sourceNode = sourceNode;
      this.targetNode = targetNode;

      sourceNodeType = RepositoryUtility.getNodeType(sourceNode);
      targetNodeType = RepositoryUtility.getNodeType(targetNode);

      // check if the source node is specific document or process attachment
      if (null == sourceNodeType)
      {
         return;
      }

      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();

      String sourceDocName = "";
      String targetDocName = "";
      
      Document sourceDocument = null;
      // check if the source node is specific document and has out data mapping
      if (NodeType.DOCUMENT.equals(sourceNodeType))
      {
         TypedDocumentUserObject docUserObject = (TypedDocumentUserObject) sourceNode.getUserObject();
         TypedDocument valueTypedDocument = docUserObject.getTypedDocument();
         sourceDocument = valueTypedDocument.getDocument();
         sourceDocName = valueTypedDocument.getName();
         if (!valueTypedDocument.isOutMappingExist())
         {
            MessageDialog
                  .addErrorMessage(msgBean.getParamString(
                        "views.genericRepositoryView.specificDocument.reclassifyDocument.sourceReadOnlyError",
                        sourceDocName));
            return;
         }
      }
      else if (NodeType.ATTACHMENT.equals(sourceNodeType))
      {
         RepositoryDocumentUserObject docUserObject = (RepositoryDocumentUserObject) sourceNode.getUserObject();
         sourceDocument = docUserObject.getDocument();
         sourceDocName = docUserObject.getName();
      }
      
      // check if target node has out data mapping
      if (NodeType.DOCUMENT.equals(targetNodeType))
      {
         TypedDocumentUserObject targetTypedDocUserObject = (TypedDocumentUserObject) targetNode.getUserObject();
         TypedDocument targetTypedDocument = targetTypedDocUserObject.getTypedDocument();
         
         if (!targetTypedDocument.isOutMappingExist())
         {
            MessageDialog.addErrorMessage(msgBean.getParamString(
                  "views.genericRepositoryView.specificDocument.reclassifyDocument.targetReadOnlyError", sourceDocName,
                  targetTypedDocument.getName()));
            return;
         }

         // check if the target node is already filled
         if (null != targetTypedDocUserObject.getResource())
         {
            // check if process supports process attachments
            boolean supportsProcessAttachment = DMSHelper.existsProcessAttachmentsDataPath(targetTypedDocument
                  .getProcessInstance());
            if (supportsProcessAttachment)
            {
               this.moveDocumentToAttachments = true;
            }
            else
            {
               // display error message
               MessageDialog.addErrorMessage(MessagesViewsCommonBean.getInstance().getParamString(
                     "views.genericRepositoryView.specificDocument.reclassifyDocument.processAttachmentNotSupported",
                     targetTypedDocument.getName()));
               return;
            }
         }
         // check document type
         if (!StringUtils.areEqual(sourceDocument.getDocumentType(), targetTypedDocument.getDocumentType()))
         {
            documentTypeChanged = true;
         }
         
         targetDocName = targetTypedDocument.getName();
      }
      setMessages(targetDocName);
      openPopup();
   }

   @Override
   public void apply()
   {
      try
      {
         // if source node is a process attachment and target node is specific document
         if (NodeType.ATTACHMENT.equals(sourceNodeType) && NodeType.DOCUMENT.equals(targetNodeType))
         {
            attachmentToDocument();
         }
         // if source node and target node are specific document
         else if (NodeType.DOCUMENT.equals(sourceNodeType) && NodeType.DOCUMENT.equals(targetNodeType))
         {
            documentToDocument();
         }
         // if source node is specific document and target node is process attachments
         // folder
         else if (NodeType.DOCUMENT.equals(sourceNodeType) && NodeType.ATTACHMENT_FOLDER.equals(targetNodeType))
         {
            documentToAttachment();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
         super.closePopup();
         return;
      }
   }

   public void toggleDescription()
   {
      showDescription = !showDescription;
   }

   public void toggleComment()
   {
      showComment = !showComment;
   }
   
   /**
    * if source node is a process attachment and target node is specific document
    */
   private void attachmentToDocument()
   {
      Document docToBeAttached = null;
      Document docToBeDettached = null;

      TypedDocumentUserObject targetTypedDocUserObject = (TypedDocumentUserObject) targetNode.getUserObject();
      TypedDocument targetTypedDocument = targetTypedDocUserObject.getTypedDocument();
      ProcessInstance pi = targetTypedDocument.getProcessInstance();

      RepositoryDocumentUserObject sourceDocUserObject = (RepositoryDocumentUserObject) sourceNode.getUserObject();
      docToBeDettached = sourceDocUserObject.getDocument();
      
      // attach the existing document to process
      if (isMoveDocumentToAttachments())
      {
         docToBeAttached = targetTypedDocument.getDocument();
      }

      docToBeDettached = createVersion(docToBeDettached, targetTypedDocument.getDocumentType());

      // update process attachments
      updateProcessAttachment(pi, docToBeDettached, docToBeAttached);
      
      // dissociate the existing document from specific document and
      // update current typed documents and re-initialize
      targetTypedDocument.setDocument(docToBeDettached);
      TypedDocumentsUtil.updateTypedDocument(targetTypedDocument);
      targetTypedDocUserObject.initialize();
      super.apply();
   }
   
    /**
    * if source node and target node are Specific Document
    */
   private void documentToDocument()
   {
      Document docToBeAttached = null;
      ProcessInstance pi = null;

      TypedDocumentUserObject targetTypedDocUserObject = (TypedDocumentUserObject) targetNode.getUserObject();
      TypedDocument targetTypedDocument = targetTypedDocUserObject.getTypedDocument();
      pi = targetTypedDocument.getProcessInstance();

      // attach the existing document to process
      if (isMoveDocumentToAttachments())
      {
         docToBeAttached = targetTypedDocUserObject.getDocument();
      }

      // update process attachments
      updateProcessAttachment(pi, null, docToBeAttached);
      
      // update source Specific Document Node
      Document document = null;
      TypedDocumentUserObject sourceDocUserObject = (TypedDocumentUserObject) sourceNode.getUserObject();
      TypedDocument sourceTypedDocument = sourceDocUserObject.getTypedDocument();
      document = sourceTypedDocument.getDocument();
      sourceTypedDocument.setDocument(null);
      TypedDocumentsUtil.updateTypedDocument(sourceDocUserObject.getTypedDocument());
      sourceDocUserObject.initialize();

      // update target Specific Document Node
      document = createVersion(document, targetTypedDocument.getDocumentType());
      
      targetTypedDocument.setDocument(document);
      TypedDocumentsUtil.updateTypedDocument(targetTypedDocument);
      targetTypedDocUserObject.initialize();
      
      super.apply();
   }

   /**
    * if source node is specific document and target node is process attachment
    */
   private void documentToAttachment()
   {
      Document docToBeAttached = null;
      ProcessInstance pi = null;

      TypedDocumentUserObject sourceDocUserObject = (TypedDocumentUserObject) sourceNode.getUserObject();
      TypedDocument sourceTypedDocument = sourceDocUserObject.getTypedDocument();
      pi = sourceTypedDocument.getProcessInstance();

      // attach the specific document to process
      docToBeAttached = sourceTypedDocument.getDocument();

      // update process attachments
      updateProcessAttachment(pi, null, docToBeAttached);
      
      // update source Specific Document Node
      sourceTypedDocument.setDocument(null);
      TypedDocumentsUtil.updateTypedDocument(sourceDocUserObject.getTypedDocument());
      sourceDocUserObject.initialize();
      super.apply();
   }
   
   /**
    * create new version if document type is different
    * 
    * @param docToBeDettached
    * @param dType
    * @return
    */
   private Document createVersion(Document docToBeDettached, DocumentType dType)
   {
      // create new version if document type is different
      if (documentTypeChanged)
      {
         docToBeDettached.getProperties().clear();
         docToBeDettached.setDocumentType(dType);
         docToBeDettached.setDescription(getDescription());

         if (!DocumentMgmtUtility.isDocumentVersioned(docToBeDettached))
         {
            docToBeDettached = DocumentMgmtUtility.getDocumentManagementService().versionDocument(
                  docToBeDettached.getId(), "", null);
         }

         return DocumentMgmtUtility.getDocumentManagementService().updateDocument(docToBeDettached, true,
               getComments(), null, false);
      }
      return docToBeDettached;
   }
   
   /**
    * 
    * @param pi
    * @param docToBeDettached
    * @param docToBeAttached
    */
   private void updateProcessAttachment(ProcessInstance pi, Document docToBeDettached, Document docToBeAttached)
   {
      // update process attachment
      if (null != docToBeDettached)
      {
         DMSHelper.deleteProcessAttachment(pi, docToBeDettached);
      }
      if (null != docToBeAttached)
      {
         DMSHelper.addAndSaveProcessAttachment(pi, docToBeAttached);
      }
   }

  
   /**
    * @param documentDataName
    */
   private void setMessages(String documentDataName)
   {
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();
      String msg = "";
      if (targetNodeType.equals(NodeType.DOCUMENT))
      {
         msg = msgBean.getParamString("views.genericRepositoryView.specificDocument.reclassifyDocument.confirmation",
               documentDataName);
      }
      else if (targetNodeType.equals(NodeType.ATTACHMENT_FOLDER))
      {
         msg = msgBean
               .getParamString("views.genericRepositoryView.specificDocument.reclassifyDocument.processAttachments");
      }

      if (this.moveDocumentToAttachments)
      {
          msgLine2 = msgBean.getParamString("views.genericRepositoryView.specificDocument.reclassifyDocument.warning",
               documentDataName);
      }


      if (this.documentTypeChanged)
      {
         msgLine2 = msgLine2
               + "</br>"
               + msgBean
                     .getString("views.genericRepositoryView.specificDocument.reclassifyDocument.documentTypeChanged");
      }
      
      super.setMessage(msg);
   }
   
   public DefaultMutableTreeNode getSourceNode()
   {
      return sourceNode;
   }

   public String getMsgLine2()
   {
      return msgLine2;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getComments()
   {
      return comments;
   }

   public void setComments(String versionComment)
   {
      this.comments = versionComment;
   }

   public boolean isShowDescription()
   {
      return showDescription;
   }

   public boolean isShowComment()
   {
      return showComment;
   }

   public boolean isDocumentTypeChanged()
   {
      return documentTypeChanged;
   }
}