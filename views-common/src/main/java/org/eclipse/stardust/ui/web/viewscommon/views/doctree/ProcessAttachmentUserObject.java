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
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class ProcessAttachmentUserObject extends RepositoryFolderUserObject
{
   private static final long serialVersionUID = 1L;
   private ProcessInstance processInstance;

   /**
    * constructor - sets default properties
    * 
    * @param defaultMutableTreeNode
    * @param folder
    * @param processInstance
    */
   public ProcessAttachmentUserObject(DefaultMutableTreeNode defaultMutableTreeNode, Folder folder,
         ProcessInstance processInstance)
   {
      super(defaultMutableTreeNode, folder);
      this.setExpanded(true);
      this.setLabel(MessagesViewsCommonBean.getInstance().getString(
            "views.processInstanceDetailsView.processDocumentTree.processAttachment"));
      this.setEditable(false);
      this.setDeletable(false);
      this.processInstance = processInstance;
      setIcon(ResourcePaths.I_PROCESS_ATTACHMENT);
   }

   /**
    * This method updates the process attachment mapping for newly added document
    */
   @Override
   public void afterUpload(RepositoryResourceUserObject userObject)
   {
      if (userObject.getResource() instanceof Document)
      {
         Document document = (Document)userObject.getResource();

         // It's observed that a refresh is required after addAndSaveProcessAttachment()
         // Otherwise ClassCaseException occurs in kernel, when a new version is uploaded
         // java.lang.ClassCastException: java.util.ArrayList
         // at org.eclipse.stardust.engine.extensions.dms.data.DmsResourceBean.getProperties(DmsResourceBean.java:98)
         // Refresh Document in Tree
         try
         {
            userObject.setResource(DocumentMgmtUtility.getDocument(document.getId()));
         }
         catch (Exception e)
         {
            // NOP. This will never occur
         }
         // if process attachment does not exist, add it or if exist then update it
         if (!DMSHelper.addAndSaveProcessAttachment(this.processInstance, document))
         {
            DMSHelper.updateProcessAttachment(this.processInstance, document);
         }
      }
   }
   
   public void drop(DefaultMutableTreeNode valueNode)
   {
      RepositoryResourceUserObject userObject = (RepositoryResourceUserObject) valueNode.getUserObject();

      if (userObject instanceof TypedDocumentUserObject)
      {
         ReclassifyDocumentBean reclassifyDocumentBean = ReclassifyDocumentBean.getInstance(true);
         reclassifyDocumentBean.initialize(valueNode, this.wrapper);
      }
      else
      {
         super.drop(valueNode);
      }
   }

   /**
    * returns the associated process instance
    * 
    * @return
    */
   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   @Override
   public void sendFile()
   {}

   @Override
   public void refresh()
   {
      RepositoryUtility.updateProcessAttachmentNode(this.wrapper, this.processInstance);
   }

   @Override
   public boolean isCanCreateFolder()
   {
      return false;
   } 
}