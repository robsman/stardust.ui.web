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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEvent;
import org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEventObserver;
import org.eclipse.stardust.ui.web.viewscommon.common.event.IppEventController;
import org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEvent;
import org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEventObserver;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.BindRepositoryDialog;

import com.icesoft.faces.component.DisplayEvent;
import com.icesoft.faces.component.dragdrop.DndEvent;
import com.icesoft.faces.component.dragdrop.DropEvent;
import com.icesoft.faces.component.selectinputtext.SelectInputText;
import com.icesoft.faces.component.tree.Tree;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class GenericRepositoryTreeViewBean extends UIComponentBean implements ViewEventHandler, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private static final Logger logger = LogManager.getLogger(GenericRepositoryTreeViewBean.class);

   public static enum RepositoryMode {
      DOCUMENT_REPO, MY_DOCUMENTS, PROCESS_DOCUMENTS, RESOURCE_MANAGEMENT, MY_REPORTS,CASE_DOCUMENTS
   }

   private static final String BEAN_NAME = "genericRepositoryTreeViewBean";
   private DefaultTreeModel model;
   private List<SelectItem> resourceMatchPossibilities = CollectionUtils.newList();
   private RepositoryResourceUserObject selectedUserObject;
   private RepositoryMode repositoryMode = RepositoryMode.DOCUMENT_REPO;
   private ProcessInstance processInstance;
   private String quickSearchQuery;
   private boolean quickSearchApplicable = false;
   private boolean isBackupAllowed = false;
   private RepositoryResourceUserObject editingResource = null;
   
   private IppEventController ippEventController;
   private EventHandler eventHandler;
   private boolean registred;
   boolean skipDocumentEvents = false;
   RepositoryResourceUserObject deleteUserObject;
   RepositoryResourceUserObject detachUserObject;
   RepositoryNodeUserObject repositoryUserObject;
   private String repositoryId;
   private List<ProcessInstance> processInstances;
   private ConfirmationDialog genericRepoConfirmationDialog;

   /**
    * default constructor
    */
   public GenericRepositoryTreeViewBean()
   {
      super(ResourcePaths.VID_REPOSITORY);
      initialize();
   }

   /**
    * Initialize the model
    */
   @Override
   public void initialize()
   {
	  deleteUserObject=null;
	  repositoryUserObject = null;
	  repositoryId = null;
      quickSearchApplicable = false;
      if (RepositoryMode.DOCUMENT_REPO == this.repositoryMode)
      {
         model = RepositoryUtility.createDocumentRepoModel();
         quickSearchApplicable = true;
      }
      else if (RepositoryMode.MY_DOCUMENTS == this.repositoryMode)
      {
         model = RepositoryUtility.createMyDocumentsModel();
      }
      else if (RepositoryMode.PROCESS_DOCUMENTS == this.repositoryMode)
      {
         model = RepositoryUtility.createProcessDocumentsModel(this.processInstance);

         if (null != ippEventController)
         {
            if (!registred)
            {
               eventHandler = new EventHandler();
               ippEventController.registerObserver((NoteEventObserver)eventHandler);
               ippEventController.registerObserver((DocumentEventObserver)eventHandler);
               registred = true;
            }
         }
      }
      else if (RepositoryMode.CASE_DOCUMENTS == this.repositoryMode)
      {
         this.processInstances=ProcessInstanceUtils.findChildren(processInstance); 
         model = RepositoryUtility.createCaseDocumentsModel(this.processInstances,this.processInstance);
         if (null != ippEventController)
         {
            if (!registred)
            {
               eventHandler = new EventHandler();
               ippEventController.registerObserver((NoteEventObserver)eventHandler);
               ippEventController.registerObserver((DocumentEventObserver)eventHandler);
               registred = true;
            }
         }
      }
      else if (RepositoryMode.RESOURCE_MANAGEMENT == this.repositoryMode)
      {
         model = RepositoryUtility.createResourceMgmtModel();
         isBackupAllowed = true;
      }
      else if (RepositoryMode.MY_REPORTS == this.repositoryMode)
      {
        model = RepositoryUtility.createMyReportsModel();
      }
   }

   /**
    * 
    */
   public void destroy()
   {
      if (null != ippEventController && null != eventHandler)
      {
         ippEventController.unregisterObserver((NoteEventObserver)eventHandler);
         ippEventController.unregisterObserver((DocumentEventObserver)eventHandler);
         registred = false;
      }
   }

   /**
    * returns current instance
    * 
    * @return
    */
   public static GenericRepositoryTreeViewBean getInstance()
   {
      return (GenericRepositoryTreeViewBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   public void handleEvent(ViewEvent event)
   {
   // TODO
   }

   /**
    * gets invoked when user expands or collapse the tree node
    * 
    * @param ae
    */
   public void treeEvent(ActionEvent ae)
   {
      if (isEditingModeOff())
      {
         Tree m = (Tree) ae.getSource();
         if (m.getNavigationEventType().equalsIgnoreCase(Tree.NAVIGATION_EVENT_EXPAND))
         {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) m.getNavigatedNode();
            RepositoryResourceUserObject userObj = (RepositoryResourceUserObject) node.getUserObject();
            // check if the tree it needs to be filled
            if (userObj instanceof RepositoryFolderUserObject && node.getAllowsChildren() && node.getChildCount() == 0)
            {
               RepositoryUtility.expandTree(node);
            }
         }
      }
   }

   /**
    * refreshes the root node without collapsing the expanded tree
    */
   public void update()
   {
      if (isEditingModeOff())
      {
         RepositoryUtility.refreshNode(getRootNode());
      }
   }

   /**
    * Handles the intra tree drag-drop operation Copies/Moves the selected document into
    * target folder
    * 
    * @param dropEvent
    */
   public void dropPanelListener(DropEvent dropEvent)
   {
      try
      {
         if (dropEvent.getEventType() == DndEvent.DROPPED)
         {
            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) dropEvent.getTargetDropValue();
            DefaultMutableTreeNode valueNode = (DefaultMutableTreeNode) dropEvent.getTargetDragValue();
            if (null != targetNode && null != valueNode)
            {
               RepositoryResourceUserObject resourceObject = (RepositoryResourceUserObject) targetNode.getUserObject();

               if (!targetNode.equals(valueNode.getParent()))
               {
                  resourceObject.drop(valueNode);
               }
            }
         }
      }
      catch (Exception exception)
      {
         ExceptionHandler.handleException(exception);
      }
   }

   /**
    * @param event
    */
   public void quickSearch(ValueChangeEvent event)
   {
      String newQuery = (String) event.getNewValue();
      SelectInputText autoComplete = (SelectInputText) event.getComponent();

      Set<String> matchPossibilitiesSet = new HashSet<String>();

      if (StringUtils.isEmpty(newQuery))
      {
         this.resourceMatchPossibilities.clear();
         return;
      }

      if (autoComplete.getSelectedItem() != null)
      {
         String path = (String) autoComplete.getSelectedItem().getValue();

         if (logger.isDebugEnabled())
         {
            logger.debug("Got path: " + path);
         }

         DefaultMutableTreeNode node = RepositoryUtility.searchNode(getRootNode(), path, true);

         if (node == null)
         {
            if (logger.isDebugEnabled())
            {
               logger.debug("Nothing found for: " + newQuery);
            }
            return;
         }

         RepositoryResourceUserObject userObject = (RepositoryResourceUserObject) node.getUserObject();
         this.selectedUserObject = userObject;
         userObject.getResourceFoundEffect().setFired(false);
         userObject.setExpanded(false);

         while (!node.isRoot())
         {
            node = (DefaultMutableTreeNode) node.getParent();
            ((RepositoryResourceUserObject) node.getUserObject()).setExpanded(true);
         }
      }
      else
      {
         newQuery = newQuery.toLowerCase();
         newQuery = DocumentMgmtUtility.replaceIllegalXpathSearchChars(newQuery);
         if (logger.isDebugEnabled())
         {
            logger.debug("Searching for: " + newQuery);
         }
         String xPathQuery = "//*[jcr:like(fn:lower-case(@vfs:metaData/vfs:name), '%" + newQuery + "%')]";

         this.resourceMatchPossibilities.clear();

         List<Document> documents = DocumentMgmtUtility.getDocumentManagementService().findDocuments(xPathQuery);
         for (Document document : documents)
         {
            this.resourceMatchPossibilities.add(new SelectItem(document.getPath(), document.getPath()));
            matchPossibilitiesSet.add(document.getPath());

         }
         List<Folder> folders = DocumentMgmtUtility.getDocumentManagementService().findFolders(xPathQuery,
               Folder.LOD_NO_MEMBERS);
         for (Folder folder : folders)
         {
            this.resourceMatchPossibilities.add(new SelectItem(folder.getPath(), folder.getPath()));
            matchPossibilitiesSet.add(folder.getPath());
         }
      }
      resourceMatchPossibilities = getUniquePossibilitiesList(matchPossibilitiesSet);
   }
   
   /**
    * @return
    */
   public String getQuickSearchQuery()
   {
      if (StringUtils.isEmpty(this.quickSearchQuery))
      {
         return this.quickSearchQuery;
      }
      else
      {
         // only return the resource name, without path, in order to reduce the space
         // taken
         String[] ss = this.quickSearchQuery.split("/");
         if (ss.length > 0)
         {
            return ss[ss.length - 1];
         }
         else
         {
            return this.quickSearchQuery;
         }
      }
   }

   public void setQuickSearchQuery(String quickSearchQuery)
   {
      this.quickSearchQuery = quickSearchQuery;
   }

   /**
    * creates model - root node
    * 
    * @return DefaultTreeModel
    */
   public DefaultTreeModel getModel()
   {
      return model;
   }
   
   
   /**
    * @param event
    */
   public void rename(ActionEvent event)
   {
      editingResource = (RepositoryResourceUserObject) event.getComponent().getAttributes().get("userObject");
      rename();
   }

   /**
    * This function handles the create folder operation and also keeps a check on rename
    * operation
    * 
    * @param event
    */
   public void createSubfolder(ActionEvent event)
   {
      RepositoryResourceUserObject folderUserObject = (RepositoryResourceUserObject) event.getComponent()
            .getAttributes().get("userObject");
      editingResource = (RepositoryResourceUserObject) folderUserObject.createSubfolder();
      rename();
   }

   /**
    * This function handles the create new file operation and also keeps a check on rename
    * 
    * @param event
    */
   public void createTextDocument(ActionEvent event)
   {
      RepositoryResourceUserObject folderUserObject = (RepositoryResourceUserObject) event.getComponent()
            .getAttributes().get("userObject");
      String fileType = (String) event.getComponent().getAttributes().get("fileType");
      setSkipDocumentEvents(true);
      editingResource = folderUserObject.createTextDocument(fileType);
      setSkipDocumentEvents(false);
      rename();
   }

   /**
    * @param event
    */
   public void openDocument(ActionEvent event)
   {
      if (isEditingModeOff())
      {
         RepositoryResourceUserObject userObject = (RepositoryResourceUserObject) event.getComponent().getAttributes()
               .get("userObject");
         userObject.openDocument();
      }
   }

   public void openBindRepoDialog(ActionEvent event)
   {
      BindRepositoryDialog dialog = BindRepositoryDialog.getInstance();
      RepositoryVirtualUserObject userObject = (RepositoryVirtualUserObject) event.getComponent().getAttributes()
      .get("userObject");
      dialog.setUserObject(userObject);
      dialog.openPopup();
   }
   
   public void showRepositoryProperties(ActionEvent event)
   {
      BindRepositoryDialog dialog = BindRepositoryDialog.getInstance();
      String repositoryId = (String) event.getComponent().getAttributes().get("repositoryId");
      dialog.setShowProperties(true);
      dialog.setRepositoryId(repositoryId);
      dialog.openPopup();
   }
   
   /**
    * @param event
    */
   public void menuPopupListener(DisplayEvent event)
   {
      isEditingModeOff();
   }

   /**
    * This function helps to complete rename file/folder operation before starting other
    * operations
    * 
    * @return
    */
   public boolean isEditingModeOff()
   {
      if (null != editingResource && editingResource.isEditingName())
      {
         editingResource.renameAccept();
         if (!editingResource.isEditingName())
         {
            editingResource = null;
         }
         else
         {
            return false;
         }
      }
      return true;
   }
   
   
   /**
    * @param event
    */
   private void rename()
   {
      if(null != editingResource){
         editingResource.renameStart();   
      }
   }

   
   /**
    * @param possibilitiesSet
    * @return
    */
   private List<SelectItem> getUniquePossibilitiesList(Set<String> possibilitiesSet)
   {
      List<SelectItem> resourceMatchPossibilities = new ArrayList<SelectItem>();

      HashSet<String> possibHashSet = new HashSet<String>(possibilitiesSet);
      for (String item : possibHashSet)
      {
         resourceMatchPossibilities.add(new SelectItem(item, item));
      }

      return resourceMatchPossibilities;
   }

   private DefaultMutableTreeNode getRootNode()
   {
      return (DefaultMutableTreeNode) this.model.getRoot();
   }


   /**
    * @param skipDocumentEvents
    */
   public void setSkipDocumentEvents(boolean skipDocumentEvents)
   {
      this.skipDocumentEvents = skipDocumentEvents;
   }
   
   public void setResourceMatchPossibilities(List<SelectItem> resourceMatchPossibilities)
   {
      this.resourceMatchPossibilities = resourceMatchPossibilities;
   }

   public List<SelectItem> getResourceMatchPossibilities()
   {
      return resourceMatchPossibilities;
   }

   public RepositoryResourceUserObject getSelectedUserObject()
   {
      return selectedUserObject;
   }

   public void setRepositoryMode(RepositoryMode repositoryMode)
   {
      this.repositoryMode = repositoryMode;
   }

   public void setProcessInstance(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }

   public ProcessInstance getProcessInstance()
   {
      return this.processInstance;
   }

   public boolean isHideRootNode()
   {
      if (RepositoryMode.MY_DOCUMENTS == repositoryMode || RepositoryMode.CASE_DOCUMENTS == repositoryMode)
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   
   
	public void confirmDeleteResource(ActionEvent event) 
	{
      deleteUserObject = (RepositoryResourceUserObject) event.getComponent().getAttributes().get("userObject");
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      genericRepoConfirmationDialog = new ConfirmationDialog(DialogContentType.NONE, DialogActionType.YES_NO, null, DialogStyle.COMPACT, this);
      genericRepoConfirmationDialog.setTitle(propsBean.getString("common.confirmDelete.title"));
      genericRepoConfirmationDialog.setMessage(propsBean.getString("common.confirmDeleteRes.message.label"));
      genericRepoConfirmationDialog.openPopup();
	}

	
	public void confirmDetachResource(ActionEvent event) 
    {
      detachUserObject = (RepositoryResourceUserObject) event.getComponent().getAttributes().get("userObject");
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      genericRepoConfirmationDialog = new ConfirmationDialog(DialogContentType.NONE, DialogActionType.YES_NO, null, DialogStyle.COMPACT, this);
      genericRepoConfirmationDialog.setTitle(propsBean.getString("common.confirmDetach.title"));
      genericRepoConfirmationDialog.setMessage(propsBean.getString("common.confirmDetach.message.label"));
      genericRepoConfirmationDialog.openPopup();
    }
	
   public void confirmYes()
   {
      if (detachUserObject != null)
      {
         detachUserObject.detachResource();
         detachUserObject = null;
      }
      if (deleteUserObject != null)
      {
         deleteUserObject.deleteResource();
         deleteUserObject = null;
      }
      if (repositoryUserObject != null)
      {
         if (repositoryId != null)
         {
            repositoryUserObject.switchDefaultRepository(repositoryId);
            repositoryId = null;
         }
         else
         {
            repositoryUserObject.unbindRepository(repositoryUserObject);
         }
         repositoryUserObject = null;
      }
   }

	public void confirmNo() 
	{
		deleteUserObject = null;
		repositoryUserObject = null;
		repositoryId = null;
		detachUserObject = null;
	}
	
   public void confirmUnbindRepository(ActionEvent event)
   {
      repositoryUserObject = (RepositoryNodeUserObject) event.getComponent().getAttributes().get("userObject");
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      genericRepoConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO, null,
            DialogStyle.COMPACT, this);
      genericRepoConfirmationDialog.setTitle(propsBean.getString("common.confirm"));
      genericRepoConfirmationDialog.setMessage(propsBean.getParamString("common.confirmUnbindRepo.message.label",
            repositoryUserObject.getRepositoryInstance().getRepositoryId()));
      genericRepoConfirmationDialog.openPopup();
   }
   
   public void confirmSwitchDefaultRepo(ActionEvent event)
   {
      repositoryId = (String) event.getComponent().getAttributes().get("repositoryId");
      repositoryUserObject = (RepositoryNodeUserObject) event.getComponent().getAttributes().get("userObject");
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      genericRepoConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO, null,
            DialogStyle.COMPACT, this);
      genericRepoConfirmationDialog.setTitle(propsBean.getString("common.confirm"));
      genericRepoConfirmationDialog.setMessage(propsBean.getParamString("common.confirmSwitchRepo.message.label",
            repositoryUserObject.getRepositoryInstance().getRepositoryId()));
      genericRepoConfirmationDialog.openPopup();
   }

	/**
	 * 
	 */
	 public boolean accept()
     {
        genericRepoConfirmationDialog = null;
        confirmYes();
        return true;
     }

	 /**
	  * 
	  */
     public boolean cancel()
     {
        genericRepoConfirmationDialog = null;
        confirmNo();
        return true;
     }

   public boolean isQuickSearchApplicable()
   {
      return quickSearchApplicable;
   }
   
   public boolean isBackupAllowed()
   {
      return isBackupAllowed;
   }

   public void setIppEventController(IppEventController ippEventController)
   {
      this.ippEventController = ippEventController;
   }

   public List<ProcessInstance> getProcessInstances()
   {
      return processInstances;
   }

   public void setProcessInstances(List<ProcessInstance> processInstances)
   {
      this.processInstances = processInstances;
   }

   public ConfirmationDialog getGenericRepoConfirmationDialog()
   {
      return genericRepoConfirmationDialog;
   }



   /**
    * @author subodh.godbole
    *
    */
   public class EventHandler implements NoteEventObserver, DocumentEventObserver
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEventObserver#handleEvent(org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEvent)
       */
      public void handleEvent(NoteEvent event)
      {
         if (event.getScopeProcessInstanceOid() == processInstance.getScopeProcessInstanceOID())
         {
            DefaultMutableTreeNode treeNode;

            int count = getRootNode().getChildCount();
            for (int i = 0; i < count; i++)
            {
               treeNode = ((DefaultMutableTreeNode) getRootNode().getChildAt(i));
               if (treeNode.getUserObject() instanceof RepositoryVirtualUserObject)
               {
                  // Check if it's a Notes Virtual Node
                  if (((RepositoryVirtualUserObject) treeNode.getUserObject()).isCanCreateNote())
                  {
                     RepositoryUtility.refreshNoteNodes(treeNode, event.getAllNotes(), processInstance);
                     break;
                  }
               }
            }
         }
      }
      
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEventObserver#handleEvent(org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEvent)
       */
      public void handleEvent(DocumentEvent event)
      {
         if (!skipDocumentEvents && event.getProcessInstanceOid() == processInstance.getOID())
         {
            DefaultMutableTreeNode treeNode;
   
            int count = getRootNode().getChildCount();
            for (int i = 0; i < count; i++)
            {
               treeNode = ((DefaultMutableTreeNode) getRootNode().getChildAt(i));
               if (RepositoryUtility.isProcessAttachmentFolderNode((RepositoryResourceUserObject) treeNode
                     .getUserObject()))
               {
                  RepositoryUtility.updateProcessAttachmentNode(treeNode, processInstance);
                  break;
               }
            }
         }
      }
   }
     
}
