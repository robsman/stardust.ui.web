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
package org.eclipse.stardust.ui.web.viewscommon.views.correspondence;

import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.categorytree.GenericCategory;
import org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTree;
import org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTreeUserObject;
import org.eclipse.stardust.ui.web.common.categorytree.GenericItem;
import org.eclipse.stardust.ui.web.common.categorytree.IGenericCategoryTreeUserObjectCallback;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.I18nFolderUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.correspondence.CorrespondenceAttachmentsHandler.AddPolicy;



/**
 * @author Subodh.Godbole
 *
 */
public class CorrespondenceAttachments extends PopupUIComponentBean implements IGenericCategoryTreeUserObjectCallback
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(CorrespondenceAttachments.class);

   private static final String PROCESS_ATATCHMENT_NODE = "ProcessAttach";
   
   public static final String ADD_TEMPLATE_TOP = "TOP";
   public static final String ADD_TEMPLATE_BOTTOM = "BOTTOM";

   private MessagesViewsCommonBean msgBean;

   private boolean showProcessDocuments;
   
   private String addTemplatePosition = ADD_TEMPLATE_BOTTOM;
   
   private GenericCategoryTree tree;

   private ProcessInstance processInstance;

   private CorrespondenceAttachmentsHandler correspondenceAttachmentsHandler;
   
   /**
    * 
    */
   public CorrespondenceAttachments(CorrespondenceAttachmentsHandler correspondenceAttachmentsHandler)
   {
      this.correspondenceAttachmentsHandler = correspondenceAttachmentsHandler;
      tree = new GenericCategoryTree("treeRoot", "ROOT", this);
      msgBean = MessagesViewsCommonBean.getInstance();
      tree.refreshTreeModel();
   }

   @Override
   public void initialize()
   {
      tree = new GenericCategoryTree("treeRoot", "ROOT", this);

      GenericCategory rootCategory = tree.getRootCategory();
      addMyDocuments(rootCategory);
      addCommonDocuments(rootCategory);
      addProcessDocuments(rootCategory);
      
      tree.refreshTreeModel();
   }

   @Override
   public void openPopup()
   {
      initialize();
      super.openPopup();
      correspondenceAttachmentsHandler.popupOpened();
   }

   @Override
   public void closePopup()
   {
      super.closePopup();
      correspondenceAttachmentsHandler.popupClosed();      
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.categorytree.IGenericCategoryTreeUserObjectCallback#itemClicked(org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTreeUserObject)
    */
   public void itemClicked(GenericCategoryTreeUserObject treeUserobject)
   {
      try
      {
         DocumentWrapper documentWrapper = (DocumentWrapper)treeUserobject.getItem().getItemObject();
         Document document = documentWrapper.getDocument();
   
         if (isShowProcessDocuments())
         {
            correspondenceAttachmentsHandler.addAttachment(document);
         }
         else
         {
            correspondenceAttachmentsHandler.addTemplate(document, ADD_TEMPLATE_TOP.equals(addTemplatePosition)
                  ? AddPolicy.AT_TOP
                  : AddPolicy.AT_BOTTOM);
         }
         documentWrapper.fireClickEffect();
      }
      catch(Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.categorytree.IGenericCategoryTreeUserObjectCallback#expanded(org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTreeUserObject)
    */
   public void expanded(GenericCategoryTreeUserObject treeUserobject)
   {
      try
      {
         GenericCategory category = treeUserobject.getCategory();
         
         if (category.getSubCategories().size() == 0 && category.getItems().size() == 0)
         {
            if (category.getCategoryObject() instanceof Folder)
            {
               addDocumentsAndFolders(category);
               tree.refreshTreeModel();
            }
            else 
            {
               if (PROCESS_ATATCHMENT_NODE.equals(category.getId()))
               {
                  fetchProcessAttachments(category);
                  tree.refreshTreeModel();
               }
            }
         }
      }
      catch(Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.categorytree.IGenericCategoryTreeUserObjectCallback#collapsed(org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTreeUserObject)
    */
   public void collapsed(GenericCategoryTreeUserObject treeUserobject)
   {
      // NOP
   }

   /**
    * @param parentCategory
    */
   private void addMyDocuments(GenericCategory parentCategory)
   {
      GenericCategory myDocCat = parentCategory.addSubCategory("MyDocs", msgBean
            .getString("views.correspondenceView.attachments.tree.myDocuments"), DMSUtils.getMyDocumentsFolder());
      myDocCat.setIcon(DMSUtils.I_FOLDER_MY_DOCS);
      addDocumentsAndFolders(myDocCat);
   }
   
   /**
    * @param parentCategory
    */
   private void addCommonDocuments(GenericCategory parentCategory)
   {
      addDocumentsAndFolders(parentCategory.addSubCategory("CommonDocs", msgBean
            .getString("views.correspondenceView.attachments.tree.commonDocuments"), DMSUtils.getCommonDocumentsFolder()));
   }

   /**
    * @param parentCategory
    */
   private void addProcessDocuments(GenericCategory parentCategory)
   {
      if(!isShowProcessDocuments())
         return;

      GenericCategory procAttachCat = parentCategory.addSubCategory("ProcessDocs",
            msgBean.getString("views.correspondenceView.attachments.tree.processDocuments")).addSubCategory(
            PROCESS_ATATCHMENT_NODE, msgBean.getString("views.correspondenceView.attachments.tree.processAttachments"));
      procAttachCat.setIcon(DMSUtils.I_FOLDER_PROCESS_ATTACH);
   }

   /**
    * @param parentCategory
    */
   private void fetchProcessAttachments(GenericCategory parentCategory)
   {
      if(processInstance != null)
      {
         List<Document> documents = DMSHelper.fetchProcessAttachments(processInstance);
         for (Document document : documents)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Adding Document = " + document.getId() + ":" + document.getName());
            }
            GenericItem documentItem = parentCategory.addItem(document.getId(), document.getName(),
                  new DocumentWrapper(document, true));
            documentItem.setIcon(DMSUtils.getDocumentIcon(document));
         }
         
         parentCategory.setExpanded(true);
      }
      else
      {
         MessageDialog.addWarningMessage("Process Instance is not set");
      }
   }
   
   /**
    * @param parentCategory
    */
   @SuppressWarnings("unchecked")
   private void addDocumentsAndFolders(GenericCategory parentCategory)
   {
      Object catObject = parentCategory.getCategoryObject();
      if (catObject instanceof Folder)
      {
         Folder thisFolder = (Folder)catObject;
         thisFolder = DMSUtils.getFolder(thisFolder.getId());

         List<Folder> folders = thisFolder.getFolders();
         for (Folder folder : folders)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Adding Folder = " + folder.getId() + ":" + folder.getName());
            }
            GenericCategory folderCat = parentCategory.addSubCategory(folder.getId(),
                  I18nFolderUtils.getLabel(folder.getPath()), folder);
            
            if (DMSUtils.F_CORRESPONDENCE_FOLDER.equals(folder.getPath()))
            {
               folderCat.setIcon(DMSUtils.I_FOLDER_CORRESPONDENCE);
            }
         }

         boolean selectable;
         List<Document> documents = thisFolder.getDocuments();
         for (Document document : documents)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Adding Document = " + document.getId() + ":" + document.getName());
            }
            selectable = isShowProcessDocuments() ? true : correspondenceAttachmentsHandler.isDocumentTemplate(document);
            GenericItem documentItem = parentCategory.addItem(document.getId(), document.getName(),
                  new DocumentWrapper(document, selectable));
            documentItem.setIcon(DMSUtils.getDocumentIcon(document));
         }
      }
      
      parentCategory.setExpanded(true);
   }

   public boolean isShowProcessDocuments()
   {
      return showProcessDocuments;
   }

   public void setShowProcessDocuments(boolean showProcessDocuments)
   {
      this.showProcessDocuments = showProcessDocuments;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public void setProcessInstance(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }
   
   public GenericCategoryTree getTree()
   {
      return tree;
   }

   public String getAddTemplatePosition()
   {
      return addTemplatePosition;
   }

   public void setAddTemplatePosition(String addTemplatePosition)
   {
      this.addTemplatePosition = addTemplatePosition;
   }
}
