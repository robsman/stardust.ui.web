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
package org.eclipse.stardust.ui.web.viewscommon.docmgmt;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.core.RepositoryCache;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.NoteUserObject;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.ProcessAttachmentUserObject;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryDocumentUserObject;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryFolderProxyUserObject;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryFolderUserObject;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryNodeUserObject;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryVirtualUserObject;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.TypedDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.TypedDocumentUserObject;

/**
 * contains UI dependent utility methods for document management
 * 
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RepositoryUtility
{
   public static final String CORRESPONDENCE_FOLDER = "/documents/correspondence-templates";
   public static final String DOCUMENT_DISPLAY_MODE_PORTAL = "PORTAL";
   public static final String DOCUMENT_DISPLAY_MODE_NEWBROWSER = "NEWBROWSER";
   
   private static final String ARTIFACTS_SKINS = "/artifacts/skins";
   private static final String ARTIFACTS_BUNDLES = "/artifacts/bundles";
   private static final String ARTIFACTS_CONTENT = "/artifacts/content";
   private static final String POSTFIX_OPEN = " (";
   private static final String POSTFIX_CLOSE = ")";
   
   public static enum NodeType {
      DOCUMENT, ATTACHMENT, ATTACHMENT_FOLDER
   }

   /**
    * creates the model for Document Repository View
    * 
    * @return
    */
   public static DefaultTreeModel createDocumentRepoModel()
   {
      DefaultMutableTreeNode repositoryRootNode = createVirtualNode(MessagesViewsCommonBean.getInstance().getString("views.genericRepositoryView.repositories.label"), ResourcePaths.I_REPOSITORY_ROOT, null);
      List<IRepositoryInstanceInfo> repositoryInstanceInfos = DocumentMgmtUtility.getDocumentManagementService()
      .getRepositoryInstanceInfos();
      String defaultRepository = DocumentMgmtUtility.getDocumentManagementService().getDefaultRepository();
      for (IRepositoryInstanceInfo repoInstanceInfo : repositoryInstanceInfos)
      {
         DefaultMutableTreeNode repositoryNode = createRepository(repositoryRootNode, repoInstanceInfo);
         RepositoryNodeUserObject repositoryNodeUserObject = (RepositoryNodeUserObject) repositoryNode.getUserObject();
         if(defaultRepository.equals(repoInstanceInfo.getRepositoryId()))
         {
            repositoryNodeUserObject.setDefaultRepository(true);
            repositoryNodeUserObject.setLabel(getRepositoryLabel(repositoryNodeUserObject, defaultRepository));
         }
      }
      // Boolean flag to control Menu options for Repository Root node
      ((RepositoryVirtualUserObject)repositoryRootNode.getUserObject()).setRepositoryRootNode(true);
      return new DefaultTreeModel(repositoryRootNode);
   }

   /**
    * creates the Model for My Documents Tree view
    * 
    * @return
    */
   public static DefaultTreeModel createMyDocumentsModel()
   {
      DefaultMutableTreeNode personalDocNode = getPersonalDocNode();
      DefaultMutableTreeNode commonDocNode = getCommonDocNode();

      SessionContext sessionCtx = SessionContext.findSessionContext();

      if (( !DMSHelper.isSecurityEnabled() || (sessionCtx.isSessionInitialized() && sessionCtx.getUser()
            .isAdministrator())))
      {
         if (null == DocumentMgmtUtility.getFolder(CORRESPONDENCE_FOLDER))
         {
            DocumentMgmtUtility.createFolder(DocumentMgmtUtility.DOCUMENTS,
                  CommonProperties.CORRESPONDENCE_TEMPLATES_AND_PARAGRAPHS);
         }
         if (null == DocumentMgmtUtility.getFolder(CommonProperties.COMMON_STAMPS_FOLDER))
         {
            createCommonStampsFolder();
         }
      }
      DefaultMutableTreeNode virtualNode = createVirtualNode(null, null, null);
      virtualNode.add(personalDocNode);
      virtualNode.add(commonDocNode);
      ((RepositoryVirtualUserObject) virtualNode.getUserObject()).setExpanded(true);
      return new DefaultTreeModel(virtualNode);
   }

   /**
    * create model for process document tree view
    * 
    * @param processInstance
    * @return
    */
   public static DefaultTreeModel createProcessDocumentsModel(ProcessInstance processInstance)
   {
      // create Process Document Node
      StringBuffer processDocPath = new StringBuffer(
            I18nFolderUtils.getLabel(I18nFolderUtils.PROCESS_DOCUMENTS_V)).append(" [").append(
            I18nUtils.getProcessName(ProcessDefinitionUtils.getProcessDefinition(processInstance.getProcessID())))
            .append(" #").append(processInstance.getOID()).append("]");
      
      DefaultMutableTreeNode processDocumentsNode = createVirtualNode(processDocPath.toString(),
            ResourcePaths.I_PROCESS, null);
      RepositoryVirtualUserObject virtualObject = (RepositoryVirtualUserObject) processDocumentsNode.getUserObject();
      virtualObject.setMenuPopupApplicable(false);
      virtualObject.setExpanded(true);

      // create Typed Documents Node
      List<TypedDocument> typedDocuments = TypedDocumentsUtil.getTypeDocuments(processInstance);
      if (CollectionUtils.isNotEmpty(typedDocuments))
      {
         DefaultMutableTreeNode typedDocumentsNode = createVirtualNode(
               I18nFolderUtils.getLabel(I18nFolderUtils.SPECIFIC_DOCUMENTS_V), ResourcePaths.I_CORE_DOCUMENTS,
               processInstance);
         virtualObject = (RepositoryVirtualUserObject) typedDocumentsNode.getUserObject();
         updateProcessDocumentNode(typedDocumentsNode, typedDocuments);
         virtualObject.setMenuPopupApplicable(false);
         processDocumentsNode.add(typedDocumentsNode);
      }

      // check if the process attachment supported for the process instance
      if (DMSHelper.existsProcessAttachmentsDataPath(processInstance))
      {
         processDocumentsNode.add(createProcessAttachmentsNode(processInstance));
      }

      // create Noted Node
      List<Note> notesList = ProcessInstanceUtils.getNotes(processInstance);
      DefaultMutableTreeNode processNoteNode = createVirtualNode(I18nFolderUtils.getLabel(I18nFolderUtils.NOTES_V),
            ResourcePaths.I_NOTES, processInstance);
      
      ((RepositoryVirtualUserObject) processNoteNode.getUserObject()).setCanCreateNotes(true);
      refreshNoteNodes(processNoteNode, notesList, processInstance);
      processDocumentsNode.add(processNoteNode);

      return new DefaultTreeModel(processDocumentsNode);
   }
   
   public static DefaultTreeModel createCaseDocumentsModel(List<ProcessInstance> processInstances, ProcessInstance pi)
   {
      String resourcePath = null;
      DefaultMutableTreeNode subProcessDocumentsNode = null;
      DefaultMutableTreeNode parentNode = createVirtualNode(null, null, null);
      subProcessDocumentsNode = createTreeNode(pi, ResourcePaths.I_CASE, true);
      parentNode.add(subProcessDocumentsNode);
      for (ProcessInstance processInstance : processInstances)
      {
         if (processInstance.getOID() != pi.getOID())
         {
            resourcePath = processInstance.isCaseProcessInstance() ? ResourcePaths.I_CASE : ResourcePaths.I_PROCESS;
            subProcessDocumentsNode = createTreeNode(processInstance, resourcePath,
                  processInstance.isCaseProcessInstance());
            parentNode.add(subProcessDocumentsNode);
         }
      }
      return new DefaultTreeModel(parentNode);
   }
   
   
   /**
    * @param valueNode
    * @return
    */
   public static NodeType getNodeType(DefaultMutableTreeNode valueNode)
   {
      NodeType nodeType = null;
      RepositoryResourceUserObject userObject = (RepositoryResourceUserObject) valueNode.getUserObject();
      if (userObject instanceof TypedDocumentUserObject)
      {
         nodeType = NodeType.DOCUMENT;
      }
      else if (isProcessAttachmentFolderNode(userObject))
      {
         nodeType = NodeType.ATTACHMENT_FOLDER;
      }
      if (null == nodeType)
      {
         DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) valueNode.getParent();
         RepositoryResourceUserObject parentUserObject = (RepositoryResourceUserObject) parentNode.getUserObject();
         if (parentUserObject instanceof ProcessAttachmentUserObject)
         {
            nodeType = NodeType.ATTACHMENT;
         }
         if (isProcessAttachmentFolderNode(parentUserObject))
         {
            nodeType = NodeType.ATTACHMENT;
         }
      }
      return nodeType;
   }
   
   /**
    * @param userObject
    * @return
    */
   public static boolean isProcessAttachmentFolderNode(RepositoryResourceUserObject userObject)
   {
      if (userObject instanceof ProcessAttachmentUserObject)
      {
         return true;
      }
      else if (userObject instanceof RepositoryFolderProxyUserObject)
      {
         RepositoryFolderProxyUserObject folderProxyUserObject = (RepositoryFolderProxyUserObject) userObject;
         ProcessInstance processInstance = folderProxyUserObject.getProcessInstance();
         if (null != processInstance
               && DocumentMgmtUtility.getProcessAttachmentsFolderPath(processInstance).equals(
                     folderProxyUserObject.getResourceId()))
         {
            return true;
         }
      }
      return false;
   }
   
   private static DefaultMutableTreeNode createTreeNode(ProcessInstance processInstance, String resourcePath,
         boolean caseInstance)
   {
      StringBuffer processDocPath = null;
      RepositoryVirtualUserObject virtualObject = null;
      List<Note> notesList;
      DefaultMutableTreeNode processNoteNode = null;
      processDocPath = new StringBuffer(ProcessInstanceUtils.getProcessLabel(processInstance));

      DefaultMutableTreeNode subProcessDocumentsNode = createVirtualNode(processDocPath.toString(), resourcePath, null);
      virtualObject = (RepositoryVirtualUserObject) subProcessDocumentsNode.getUserObject();
      virtualObject.setMenuPopupApplicable(false);
      virtualObject.setExpanded(true);
      // create Typed Documents Node
      if (!caseInstance)
      {
         List<TypedDocument> typedDocuments = TypedDocumentsUtil.getTypeDocuments(processInstance);
         if (CollectionUtils.isNotEmpty(typedDocuments))
         {
            DefaultMutableTreeNode typedDocumentsNode = createVirtualNode(
                  I18nFolderUtils.getLabel(I18nFolderUtils.SPECIFIC_DOCUMENTS_V), ResourcePaths.I_CORE_DOCUMENTS,
                  processInstance);
            virtualObject = (RepositoryVirtualUserObject) typedDocumentsNode.getUserObject();
            updateProcessDocumentNode(typedDocumentsNode, typedDocuments);
            virtualObject.setMenuPopupApplicable(false);
            subProcessDocumentsNode.add(typedDocumentsNode);
         }
      }
      // check if the process attachment supported for the process instance
      if (DMSHelper.existsProcessAttachmentsDataPath(processInstance))
      {
         subProcessDocumentsNode.add(createProcessAttachmentsNode(processInstance));
      }

      // create Noted Node
      notesList = ProcessInstanceUtils.getNotes(processInstance);
      processNoteNode = createVirtualNode(I18nFolderUtils.getLabel(I18nFolderUtils.NOTES_V), ResourcePaths.I_NOTES,
            processInstance);

      ((RepositoryVirtualUserObject) processNoteNode.getUserObject()).setCanCreateNotes(true);
      refreshNoteNodes(processNoteNode, notesList, processInstance);
      subProcessDocumentsNode.add(processNoteNode);
      return subProcessDocumentsNode;
   }
   
    /**
    * create My Reports nodes
    * 
    * @return
    */
   public static DefaultTreeModel createMyReportsModel()
   {
      // create Reports Node
      DefaultMutableTreeNode reportsNode = createVirtualNode(I18nFolderUtils.getLabel(I18nFolderUtils.REPORT_MANAGER_REPORTS),
            ResourcePaths.I_FOLDER, null);
      //Predefined Reports
      //reportsNode.add(createPredefinedReportNode());
      //Report Definitions
      DefaultMutableTreeNode reportDefinitions = createVirtualNode(
            I18nFolderUtils.getLabel(I18nFolderUtils.MY_REPORT_DESIGNS_V),
            ResourcePaths.I_FOLDER, null);
      ((RepositoryResourceUserObject)reportDefinitions.getUserObject()).setExpanded(false);
      reportsNode.add(reportDefinitions);
      
      //Private Report Definitions
      DefaultMutableTreeNode privateReportDefinitionsNode = createPrivateReportDefinitionsNode();
      reportDefinitions.add(privateReportDefinitionsNode);
      
      //Public Report Definitions
      DefaultMutableTreeNode publicReportDefinitionsNode = createPublicReportDefinitionsNode();
      reportDefinitions.add(publicReportDefinitionsNode);
      
      List<DefaultMutableTreeNode> roleOrgReportDefinitionsNode = createRoleOrgReportDefinitionsNode();
      for (DefaultMutableTreeNode defaultMutableTreeNode : roleOrgReportDefinitionsNode)
      {
         reportDefinitions.add(defaultMutableTreeNode);
      }
      
      //Saved Reports
      DefaultMutableTreeNode savedReportsNode = createVirtualNode(
            I18nFolderUtils.getLabel(I18nFolderUtils.MY_SAVED_REPORTS_V),
            ResourcePaths.I_FOLDER, null);
      ((RepositoryResourceUserObject)savedReportsNode.getUserObject()).setExpanded(false);
      reportsNode.add(savedReportsNode);

      // Private Saved Reports
      DefaultMutableTreeNode privateSavedReports = createSavedReportsNode(
            DocumentMgmtUtility.getPrivateSavedReportsPath(),
            I18nFolderUtils.PRIVATE_SAVED_REPORTS);
      savedReportsNode.add(privateSavedReports);

      populateFolderContents(privateSavedReports);

      DefaultMutableTreeNode privateSavedReportsAdHocNode = createSavedReportsNode(
            DocumentMgmtUtility.getPrivateSavedReportsAdHocPath(),
            I18nFolderUtils.PRIVATE_SAVED_REPORTS + I18nFolderUtils.AD_HOC);
      privateSavedReports.add(privateSavedReportsAdHocNode);

      // Public Saved Reports
      DefaultMutableTreeNode publicSavedReports = createSavedReportsNode(
            DocumentMgmtUtility.getPublicSavedReportsPath(),
            I18nFolderUtils.PUBLIC_SAVED_REPORTS);
      savedReportsNode.add(publicSavedReports);
      
      populateFolderContents(publicSavedReports);

      DefaultMutableTreeNode publicSavedReportsAdHocNode = createSavedReportsNode(
            DocumentMgmtUtility.getPublicSavedReportsAdHocPath(),
            I18nFolderUtils.PUBLIC_SAVED_REPORTS + I18nFolderUtils.AD_HOC);
      publicSavedReports.add(publicSavedReportsAdHocNode);
      
      List<DefaultMutableTreeNode> roleOrgSavedReportsNode = createRoleOrgSavedReportsNode(false);
      
      List<DefaultMutableTreeNode> roleOrgSavedReportsAdHocNode = createRoleOrgSavedReportsNode(true);
      
      for (int i = 0; i < roleOrgSavedReportsNode.size(); i++)
      {
         populateFolderContents(roleOrgSavedReportsNode.get(i));
         roleOrgSavedReportsNode.get(i).add(roleOrgSavedReportsAdHocNode.get(i));
         savedReportsNode.add(roleOrgSavedReportsNode.get(i));
      }
      
      return new DefaultTreeModel(reportsNode);
   }

   /**
    * creates the Model for Resource Management Tree View
    * 
    * @return
    */
   public static DefaultTreeModel createResourceMgmtModel()
   {
      DefaultMutableTreeNode artifactsRootNode = createVirtualNode(
            I18nFolderUtils.getLabel(I18nFolderUtils.ARTIFACTS_V), ResourcePaths.I_FOLDER, null);
      RepositoryVirtualUserObject virtualUserObject = (RepositoryVirtualUserObject) artifactsRootNode.getUserObject();
      virtualUserObject.setResourcePath("/artifacts");
      artifactsRootNode.add(createFolderNode(DocumentMgmtUtility.createFolderIfNotExists(ARTIFACTS_SKINS)));
      artifactsRootNode.add(createFolderNode(DocumentMgmtUtility.createFolderIfNotExists(ARTIFACTS_BUNDLES)));
      artifactsRootNode.add(createFolderNode(DocumentMgmtUtility.createFolderIfNotExists(ARTIFACTS_CONTENT)));
      return new DefaultTreeModel(artifactsRootNode);
   }
   
   
   /**
    * updates process attachment documents
    * 
    * @param node
    * @param processInstance
    */
   public static void updateProcessAttachmentNode(DefaultMutableTreeNode node, ProcessInstance processInstance)
   {
      if (null != node)
      {
         List<Document> documentsList = DocumentMgmtUtility.getProcesInstanceDocuments(processInstance);
         refreshProcessAttachments(node, processInstance, documentsList);
      }
   }

   /**
    * @param node
    * @param processInstance
    * @param documentsList
    */
   public static void refreshProcessAttachments(DefaultMutableTreeNode node, ProcessInstance processInstance,
         List<Document> documentsList)
   {
      node.removeAllChildren();
      int size = documentsList.size();
      RepositoryDocumentUserObject docUserObject;
      DefaultMutableTreeNode docNode;
      if (!node.getAllowsChildren() && size > 0)
      {
         node.setAllowsChildren(true);
      }
      for (int n = 0; n < size; ++n)
      {
         docNode = createDocumentNode(documentsList.get(n));
         docUserObject = (RepositoryDocumentUserObject) docNode.getUserObject();
         docUserObject.setSendFileAllowed(true);
         node.add(docNode);
      }
   }
   
   /**
    * Expands the document tree when user clicks on the plus sign beside particular folder
    * It refreshes the current node as well as creates new children under the node
    * 
    * @param node
    */
   public static void expandTree(DefaultMutableTreeNode node)
   {
      // refresh the selected node
      RepositoryFolderUserObject folderUserObject = (RepositoryFolderUserObject) node.getUserObject();
      
      if (folderUserObject instanceof ProcessAttachmentUserObject)
      {
         ((ProcessAttachmentUserObject) folderUserObject).refresh();
         return;
      }
      
      boolean versionSupported = true;
      boolean writeSupported = true;
      IRepositoryInstanceInfo repositoryInstance = (IRepositoryInstanceInfo) RepositoryCache.findRepositoryCache().getObject(
            RepositoryIdUtils.extractRepositoryId(folderUserObject.getResource()));
      if (repositoryInstance != null)
      {
         versionSupported = repositoryInstance.isVersioningSupported();
         writeSupported = repositoryInstance.isWriteSupported();
      }
      
      Folder refreshedFolder = getUpdatedFolder(node);
      folderUserObject.setResource(refreshedFolder);
      // Remove existing children if any
      node.removeAllChildren();

      int folderCount = refreshedFolder.getFolderCount();
      int documentCount = refreshedFolder.getDocumentCount();

      // create new folders nodes
      for (int i = 0; i < folderCount; i++)
      {
         DefaultMutableTreeNode folderNode= createFolderNode((Folder) refreshedFolder.getFolders().get(i));
         ((RepositoryFolderUserObject)folderNode.getUserObject()).setWriteSupported(writeSupported);
         node.add(folderNode);
      }
      // create new documents nodes
      for (int i = 0; i < documentCount; i++)
      {
         DefaultMutableTreeNode documentNode = createDocumentNode((Document) refreshedFolder.getDocuments().get(i));
         ((RepositoryDocumentUserObject)documentNode.getUserObject()).setVersioningSupported(versionSupported);
         ((RepositoryDocumentUserObject)documentNode.getUserObject()).setWriteSupported(writeSupported);
         node.add(documentNode);
      }
      folderUserObject.setExpanded(true);
   }
   
   /**
    * Refreshes the selected node (both folder and documents) without collapsing the
    * already expanded tree. it refreshes all the expanded or unexpanded nodes but does
    * not refresh children of unexpanded nodes.
    * 
    * @param node
    */
   public static void refreshNode(DefaultMutableTreeNode node)
   {
      if (node.getUserObject() instanceof RepositoryFolderProxyUserObject)
      {
         RepositoryFolderProxyUserObject folderProxyUserObject = (RepositoryFolderProxyUserObject) node.getUserObject();
         String resourceId = folderProxyUserObject.getResourceId();
         if (StringUtils.isNotEmpty(resourceId) && null != DocumentMgmtUtility.getFolder(resourceId))
         {
            node = replaceProxyNode(node);
         }
      }
      else if(node.getUserObject() instanceof RepositoryNodeUserObject)
      {
         RepositoryNodeUserObject repositoryUserObject = (RepositoryNodeUserObject) node.getUserObject();
         DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
         Enumeration<DefaultMutableTreeNode> en =parent.breadthFirstEnumeration();
         while (en.hasMoreElements())
         {
            DefaultMutableTreeNode node1 = en.nextElement();
            if (node1.getLevel() == 0)
            {
               continue;
            }
            else if (node1.getLevel() == 2)
            {
               break;
            }
            RepositoryNodeUserObject userObj = (RepositoryNodeUserObject) node1.getUserObject();
            if (repositoryUserObject.getLabel().equals(userObj.getLabel()))
            {
               continue;
            }
            else if (repositoryUserObject.isDefaultRepository() && userObj.isDefaultRepository())
            {
               // Reset status and label of previous default repo
               userObj.setDefaultRepository(false);
               userObj.setIcon(ResourcePaths.I_REPOSITORY);
               userObj.setLabel(getRepositoryLabel(userObj, null));
               
               repositoryUserObject.setLabel(getRepositoryLabel(repositoryUserObject, null));
               
            }
         }
         return;
      }
      
      List<String> expandedFolders = new ArrayList<String>();
      populateExpandedFolderList(node, expandedFolders);
      if(expandedFolders.size()>0 || node.getUserObject() instanceof RepositoryDocumentUserObject){
         refreshExpandedNodes(node, expandedFolders);   
      }
   }

   /**
    * returns latest folder
    * 
    * @param node
    * @return latest folder
    */
   public static Folder getUpdatedFolder(DefaultMutableTreeNode node)
   {
      return DocumentMgmtUtility.getDocumentManagementService().getFolder(
            (((RepositoryResourceUserObject) node.getUserObject()).getResource()).getId());
   }

   /**
    * creates blank document with default name under node provided as argument
    * 
    * @param parentNode
    */
   public static DefaultMutableTreeNode createBlankDocument(DefaultMutableTreeNode parentNode, String contentType)
   {
      expandTree(parentNode);
      RepositoryFolderUserObject parentUserObject = (RepositoryFolderUserObject) parentNode.getUserObject();
      parentNode.setAllowsChildren(true);
      Document document = DocumentMgmtUtility.createBlankDocument(parentUserObject.getFolder().getId(), contentType,
            null);
      DefaultMutableTreeNode subNode = createDocumentNode(document);
      ((RepositoryDocumentUserObject)subNode.getUserObject()).setNewNodeCreated(true);
      parentNode.add(subNode);
      return subNode;
   }
   
   /**
    * This function creates a copy of document
    * 
    * @param parentNode
    * @param doc
    */
   public static Document copyDocument(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode valueNode)
   {
      expandTree(parentNode);
      RepositoryFolderUserObject userObject = (RepositoryFolderUserObject) parentNode.getUserObject();
      parentNode.setAllowsChildren(true);
      Document newDocument = DocumentMgmtUtility.createDocumentCopy(getUpdatedDocument(valueNode), userObject
            .getFolder().getPath());
      DefaultMutableTreeNode subNode = createDocumentNode(newDocument);
      ((RepositoryDocumentUserObject)subNode.getUserObject()).setNewNodeCreated(true);
      parentNode.add(subNode);
      return newDocument;
   }

   /**
    * This function MOVES document from one folder to other in the same tree
    * 
    * @param targetNode
    * @param valueNode
    */
   public static Document moveDocument(DefaultMutableTreeNode targetNode, DefaultMutableTreeNode valueNode)
   {
      Document document = copyDocument(targetNode, valueNode);
      ((RepositoryResourceUserObject) valueNode.getUserObject()).deleteResource();
      return document;
   }

   /**
    * creates new sub folder and corresponding new subNode also attaches the subNode to
    * its parent node
    * 
    * @param parentNode
    * @param folderName
    * @return subfolder
    */
   public static DefaultMutableTreeNode createSubfolder(DefaultMutableTreeNode parentNode, String folderName)
   {
      expandTree(parentNode);
      parentNode.setAllowsChildren(true);
      RepositoryFolderUserObject parentUserObject = (RepositoryFolderUserObject) parentNode.getUserObject();
      DefaultMutableTreeNode subNode = createFolderNode(DocumentMgmtUtility.createFolder(
            RepositoryIdUtils.addRepositoryId(parentUserObject.getResource().getPath(), parentUserObject.getResource().getRepositoryId()), folderName));
      parentNode.add(subNode);
      RepositoryFolderUserObject repositoryFolderUserObject = (RepositoryFolderUserObject) subNode.getUserObject();
      //repositoryFolderUserObject.renameStart();
      repositoryFolderUserObject.setNewNodeCreated(true);
      return subNode;
   }
   
   /**
    * 
    * @param rootNode
    * @param repositoryInstanceInfo
    * @return
    */
   public static DefaultMutableTreeNode createRepository(DefaultMutableTreeNode rootNode, IRepositoryInstanceInfo repositoryInstanceInfo)
   {
      DefaultMutableTreeNode repositoryNode = new DefaultMutableTreeNode();
      RepositoryNodeUserObject repositoryNodeUserObject = new RepositoryNodeUserObject(repositoryNode, repositoryInstanceInfo);
      repositoryNode.setUserObject(repositoryNodeUserObject);
      repositoryNodeUserObject.setLabel(getRepositoryLabel(repositoryNodeUserObject, null));

      // Fetch the folders for the Repository
      String folderId = RepositoryIdUtils.addRepositoryId("/", repositoryInstanceInfo.getRepositoryId());
      Folder rootFolder = DocumentMgmtUtility.getDocumentManagementService().getFolder(folderId);
      // Create Folder Node
      DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode();
      RepositoryFolderUserObject repositoryFolderUserObject = new RepositoryFolderUserObject(folderNode, rootFolder);
      folderNode.setUserObject(repositoryFolderUserObject);
      repositoryNode.add(folderNode);
      rootNode.add(repositoryNode);
      return repositoryNode;
   }
   
   /**
    * searches a node for provided path
    * 
    * @param rootNode
    * @param resourcePath
    * @param forceReload
    * @return search result
    */
   public static DefaultMutableTreeNode searchNode(DefaultMutableTreeNode rootNode, String resourcePath,
         boolean forceReload)
   {
      DefaultMutableTreeNode node = rootNode;
      DefaultMutableTreeNode searchNode = null;
      String[] path = resourcePath.split("/");
      String defaultRepository = DocumentMgmtUtility.getDocumentManagementService().getDefaultRepository();
      // Search is only applicable on default Repository, traverse to default repo node
      Enumeration<DefaultMutableTreeNode> repositoryRoot = node.breadthFirstEnumeration();
      while (repositoryRoot.hasMoreElements())
      {
         DefaultMutableTreeNode nodeTemp = repositoryRoot.nextElement();
         if (nodeTemp.getUserObject() instanceof RepositoryNodeUserObject)
         {
            RepositoryNodeUserObject repositoryNode = (RepositoryNodeUserObject) nodeTemp.getUserObject();
            if (repositoryNode.getRepositoryInstance().getRepositoryId().equalsIgnoreCase(defaultRepository))
            {
               node = repositoryNode.getWrapper();
               break;
            }
         }
      }
      for (int i = 1; i < path.length; i++)
      {
         Enumeration<DefaultMutableTreeNode> en =node.breadthFirstEnumeration();
         while (en.hasMoreElements())
         {
            node = en.nextElement();
            // Parent Node is repository Node, use next Folder Node to findNode
            if (node.getUserObject() instanceof RepositoryFolderUserObject)
            {
               searchNode = findNode(node, path[i], forceReload);
            }
            else
            {
               continue;
            }
            if(null !=  searchNode)
            {
               break;
            }
         }
      }
      return searchNode;
   }

   /**
    * expands the tree bottoms up, from the particular resource to Root
    * 
    * @param rootNode
    * @param path
    * @return
    */
   public static RepositoryResourceUserObject expandTreeNodeToRoot(DefaultMutableTreeNode rootNode, String path)
   {
      DefaultMutableTreeNode node = searchNode(rootNode, path, true);
      RepositoryResourceUserObject selectedUserObject = null;
      if (node == null)
      {
         return null;
      }
      RepositoryResourceUserObject userObject = (RepositoryResourceUserObject) node.getUserObject();
      selectedUserObject = userObject;
      userObject.getResourceFoundEffect().setFired(false);
      userObject.setExpanded(false);

      while (!node.isRoot())
      {
         node = (DefaultMutableTreeNode) node.getParent();
         ((RepositoryResourceUserObject) node.getUserObject()).setExpanded(true);
      }
      return selectedUserObject;
   }

   /**
    * displays popup error message
    * 
    * @param msgKey
    */
   public static void showErrorPopup(String msgKey, String param, Exception exception)
   {
      if (null == exception)
      {
         MessageDialog.addErrorMessage(getMessage(msgKey, param));
      }
      else
      {
         ExceptionHandler.handleException(exception, getMessage(msgKey, param));
      }
   }

   /**
    * Executes the search query
    * 
    * @param query
    * @return query result
    */
   public static List<SelectItem> executeQuery(String query)
   {
      Set<String> matchPossibilitiesSet = new HashSet<String>();
      query = query.toLowerCase();
      String xPathQuery = "//*[jcr:like(fn:lower-case(@vfs:name), '%" + query + "%')]";

      List<Document> documents = DocumentMgmtUtility.getDocumentManagementService().findDocuments(xPathQuery);
      for (Document document : documents)
      {
         matchPossibilitiesSet.add(document.getPath());
      }
      List<Folder> folders = DocumentMgmtUtility.getDocumentManagementService().findFolders(xPathQuery,
            Folder.LOD_NO_MEMBERS);
      for (Folder folder : folders)
      {
         matchPossibilitiesSet.add(folder.getPath());
      }
      return getUniquePossibilitiesList(matchPossibilitiesSet);
   }

   /**
    * if process attachment folder exist, return it or if not, create and return process
    * attachment folder
    * 
    * @param pi
    * @return
    */
   public static Folder getProcessAttachmentsFolder(ProcessInstance pi)
   {
      String path = DocumentMgmtUtility.getProcessAttachmentsFolderPath(pi);
      Folder folder = DocumentMgmtUtility.createFolderIfNotExists(path);
      if (null == folder)
      {
         throw new RuntimeException(getMessage("views.genericRepositoryView.jcrFolderError", path));
      }
      return folder;
   }

   /**
    * Helps creating new file name if file with the given name already exist
    * 
    * @param processAttachmentsFolder
    * @param docName
    * @param count
    */
   public static String createDocumentName(Folder processAttachmentsFolder, String docName, int count)
   {
      String docName1;

      Document document = DocumentMgmtUtility.getDocument(processAttachmentsFolder.getPath(), docName);
      if (null == document)
      {
         return docName;
      }
      else
      {
         count++;
         if (count == 1)
         {
            docName1 = docName + "(" + count + ")";
         }
         else
         {
            docName1 = docName.substring(0, docName.indexOf("(") > 0 ? docName.indexOf("(") : docName.length()) + "("
                  + count + ")";
         }
      }
      return createDocumentName(processAttachmentsFolder, docName1, count);
   }

   /**
    * checks if the file type is permissible in the current folder for file upload or
    * create new file or drap file operations
    * <Not in use so far>
    * @param parentNode
    * @return
    */
   public static boolean isFileTypePermissible(DefaultMutableTreeNode node, String fileType)
   {
      DefaultMutableTreeNode tempNode = node;
      RepositoryResourceUserObject userObject;
      Set<String> mimeTypes;
      while (!tempNode.isRoot())
      {
         userObject = (RepositoryResourceUserObject) tempNode.getUserObject();
         mimeTypes = userObject.getPermissibleMimeTypes();
         if (null != mimeTypes && !mimeTypes.contains(fileType))
         {
            MessageDialog.addErrorMessage(getMessage("views.genericRepositoryView.fileTypeNotAllowed"));
            return false;
         }
         tempNode = (DefaultMutableTreeNode) tempNode.getParent();
      }
      return true;
   }

   /**
    * finds a node under parent node based on the resource name passed in
    * 
    * @param node
    * @param resourceName
    * @param forceReload
    * @return
    */
   public static DefaultMutableTreeNode findNode(DefaultMutableTreeNode node, String resourceName, boolean forceReload)
   {
      if (forceReload)
      {
         RepositoryUtility.expandTree(node);
      }
      DefaultMutableTreeNode subNode;
      DefaultMutableTreeNode tempNode = null;
      RepositoryResourceUserObject userObject;
      for (int i = 0; i < node.getChildCount(); i++)
      {
         subNode = (DefaultMutableTreeNode) node.getChildAt(i);
         userObject = (RepositoryResourceUserObject) subNode.getUserObject();
         if (userObject.getResource().getName().equalsIgnoreCase(resourceName))
         {
            tempNode = subNode;
            break;
         }
      }
      return tempNode;
   }

   /**
    * creates new document node
    * 
    * @param document
    * @return Document Node
    */
   public static DefaultMutableTreeNode createDocumentNode(Document document)
   {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode();
      RepositoryDocumentUserObject repositoryDocumentUserObject = new RepositoryDocumentUserObject(node, document);
      node.setUserObject(repositoryDocumentUserObject);
      return node;
   }
   
   /**
    * replaces the proxy node with respective true node
    * 
    * @param node
    */
   public static DefaultMutableTreeNode replaceProxyNode(DefaultMutableTreeNode node)
   {
      DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
      RepositoryFolderProxyUserObject proxyUserObject = (RepositoryFolderProxyUserObject) node.getUserObject();
      String resourceId = proxyUserObject.getResourceId();
      DefaultMutableTreeNode newNode = null;
      ProcessInstance processInstance = proxyUserObject.getProcessInstance();
      String myDocumentsPath = DocumentMgmtUtility.getMyDocumentsPath();
      String myReportsDesignPath = DocumentMgmtUtility.getMyReportDesignsPath();
      String myArchivedReportsPath = DocumentMgmtUtility.getMyArchivedReportsPath();

      if (myDocumentsPath.equals(resourceId))
      {
         // create my documents folder
         DocumentMgmtUtility.createFolderIfNotExists(myDocumentsPath);
         newNode = getPersonalDocNode();
      }
      else if (null != processInstance
            && DocumentMgmtUtility.getProcessAttachmentsFolderPath(processInstance).equals(resourceId))
      {
         // just to create a process attachments folder if it is not created already
         getProcessAttachmentsFolder(processInstance);
         newNode = createProcessAttachmentsNode(processInstance);
      }
      else if (myReportsDesignPath.equals(resourceId))
      {
         DocumentMgmtUtility.createFolderIfNotExists(myReportsDesignPath);
         newNode = createReportDesignsNode();
      }
      else if (myArchivedReportsPath.equals(resourceId))
      {
         DocumentMgmtUtility.createFolderIfNotExists(myArchivedReportsPath);
         newNode = createArchivedReportsNode();
      }

      if (null != newNode)
      {
         int index = parentNode.getIndex(node);
         parentNode.remove(index);
         parentNode.insert(newNode, index);
      }
      return newNode;
   }

   
   
   /**
    * add reports to favorite list
    * 
    * @param document
    */
   public static void addToFavorite(String name, String id)
   {
      List<String> favoriteReports;
      favoriteReports = getFavoriteReportsStr();
      if (null == favoriteReports)
      {
         favoriteReports = new ArrayList<String>();
      }
      if (!isFavoriteReport(id))
      {
         favoriteReports.add(name + "##" + id);
      }
      setFavoriteList(favoriteReports);
   }

   /**
    * set favorite reports preferences
    * 
    * @param reports
    */
   public static void setFavoriteList(List<String> reports)
   {
      getUserPrefenceHelper().setString(UserPreferencesEntries.V_REPORTS_CONFIG,
            UserPreferencesEntries.F_REPORTS_FAVORITE, reports);
   }

   /**
    * returns favorite reports map HshMap<documentId, Name>
    * 
    * @return HshMap<String, String> reports
    */
   public static HashMap<String, String> getFavoriteReports()
   {
      List<String> favoriteReportsStr = getFavoriteReportsStr();
      HashMap<String, String> favoriteReports = new HashMap<String, String>();
      if (CollectionUtils.isNotEmpty(favoriteReportsStr))
      {
         for (String reportStr : favoriteReportsStr)
         {
            String reportName = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringBeforeLast(reportStr,
                  "##");
            String reportId = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringAfterLast(reportStr, "##");
            favoriteReports.put(reportId, reportName);
         }
      }
      return favoriteReports;
   }

   /**
    * return reports list in List<documentId##Name> format
    * @return
    */
   private static List<String> getFavoriteReportsStr()
   {
      return getUserPrefenceHelper().getString(UserPreferencesEntries.V_REPORTS_CONFIG,
            UserPreferencesEntries.F_REPORTS_FAVORITE);
   }

   /**
    * @return
    */
   public static UserPreferencesHelper getUserPrefenceHelper()
   {
      return UserPreferencesHelper.getInstance(UserPreferencesEntries.M_BCC, PreferenceScope.USER);
   }

   
   /**
    * remove reports from favorite list
    * @param document
    */
   public static void removeFromFavorite(Document document)
   {
      removeFromFavorite(document.getId());
   }

   
   /**
    *  remove reports from favorite list
    * @param documentId
    */
   public static void removeFromFavorite(String documentId)
   {
      List<String> favoriteReportsStr = getFavoriteReportsStr();
      if (CollectionUtils.isNotEmpty(favoriteReportsStr))
      {
         for (Iterator<String> iterator = favoriteReportsStr.iterator(); iterator.hasNext();)
         {
            String reportId = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringAfterLast(iterator.next(),
                  "##");
            if (documentId.equals(reportId))
            {
               iterator.remove();
               break;
            }
         }
         setFavoriteList(favoriteReportsStr);
      }
   }

   /**
    * returns true if the report is in favorite list
    * 
    * @param documentId
    * @return
    */
   public static boolean isFavoriteReport(String documentId)
   {
      HashMap<String, String> favoriteReports = getFavoriteReports();
      if (favoriteReports.containsKey(documentId))
      {
         return true;
      }
      return false;
   }
   
   /**
    * Create Report Designs node
    * 
    * @return
    */
   private static DefaultMutableTreeNode createReportDesignsNode()
   {
      String myReportDesignsPath = DocumentMgmtUtility.getMyReportDesignsPath();
      Folder folder = DocumentMgmtUtility.getFolder(myReportDesignsPath);
      DefaultMutableTreeNode myReportDesignsNode;
      RepositoryResourceUserObject resourceObject = null;
      if (null == folder)
      {
         myReportDesignsNode = createFolderProxyNode(I18nFolderUtils.getLabel(I18nFolderUtils.MY_REPORT_DESIGNS_V),
               ResourcePaths.I_FOLDER, null, myReportDesignsPath);
         resourceObject = (RepositoryResourceUserObject) myReportDesignsNode.getUserObject();
      }
      else
      {
         myReportDesignsNode = createFolderNode(folder);
         RepositoryFolderUserObject folderObject = (RepositoryFolderUserObject) myReportDesignsNode.getUserObject();
         folderObject.setLabel(I18nFolderUtils.getLabel(I18nFolderUtils.MY_REPORT_DESIGNS_V));
         folderObject.setIcon(ResourcePaths.I_FOLDER);
         resourceObject = folderObject;
      }
      resourceObject.setEditable(false);
      resourceObject.setCanCreateFile(false);
      resourceObject.setDeletable(false);
      resourceObject.setCanCreateFolder(true);
      return myReportDesignsNode;
   }

   /**
    * Create Report Designs node
    * 
    * @return
    */
   private static DefaultMutableTreeNode createArchivedReportsNode()
   {
      String myArchivedReportPath = DocumentMgmtUtility.getMyArchivedReportsPath();
      Folder folder = DocumentMgmtUtility.getFolder(myArchivedReportPath);
      DefaultMutableTreeNode archivedReportsNode;
      RepositoryResourceUserObject resourceObject = null;
      if (null == folder)
      {
         archivedReportsNode = createFolderProxyNode(I18nFolderUtils.getLabel(I18nFolderUtils.MY_SAVED_REPORTS_V),
               ResourcePaths.I_FOLDER, null, myArchivedReportPath);
         resourceObject = (RepositoryResourceUserObject) archivedReportsNode.getUserObject();
      }
      else
      {
         archivedReportsNode = createFolderNode(folder);
         RepositoryFolderUserObject folderObject = (RepositoryFolderUserObject) archivedReportsNode.getUserObject();
         folderObject.setLabel(I18nFolderUtils.getLabel(I18nFolderUtils.MY_SAVED_REPORTS_V));
         folderObject.setIcon(ResourcePaths.I_FOLDER);
         resourceObject = folderObject;
      }
      resourceObject.setCanCreateFolder(true);
      resourceObject.setEditable(false);
      resourceObject.setCanCreateFile(false);
      resourceObject.setDeletable(false);
      resourceObject.setCanUploadFile(false);
      return archivedReportsNode;
   }


   /**
    * create static predefined report node
    * 
    * @return
    */
   private static DefaultMutableTreeNode createPredefinedReportNode()
   {
      DefaultMutableTreeNode predefinedReportNode = createVirtualNode(
            I18nFolderUtils.getLabel(I18nFolderUtils.PREDEFINED_REPORTS), ResourcePaths.I_FOLDER, null);
      RepositoryVirtualUserObject virtualUserObject = (RepositoryVirtualUserObject) predefinedReportNode
            .getUserObject();
      virtualUserObject.setExpanded(false);
      return predefinedReportNode;
   }

   /**
    * updates the process document node with "typed" documents from IN and OUT path
    * 
    * @param node
    * @param procesInstance
    * @throws ResourceNotFoundException
    */
   private static void updateProcessDocumentNode(DefaultMutableTreeNode node, List<TypedDocument> typedDocuments)
   {
      node.removeAllChildren();
      DefaultMutableTreeNode docNode;
      for (TypedDocument typedDocument : typedDocuments)
      {
         docNode = createTypedDocumentNode(typedDocument);
         node.add(docNode);
      }
   }
   
   /**
    * 
    * @param possibilitiesSet
    * @return
    */
   private static List<SelectItem> getUniquePossibilitiesList(Set<String> possibilitiesSet)
   {
      List<SelectItem> resourceMatchPossibilities = new ArrayList<SelectItem>();

      HashSet<String> possibHashSet = new HashSet<String>(possibilitiesSet);
      for (String item : possibHashSet)
      {
         resourceMatchPossibilities.add(new SelectItem(item, item));
      }
      return resourceMatchPossibilities;
   }

   /**
    * creates new folder node
    * 
    * @param folder
    * @return Folder Node
    */
   private static DefaultMutableTreeNode createFolderNode(Folder folder)
   {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode();
      RepositoryFolderUserObject repositoryFolderUserObject = new RepositoryFolderUserObject(node, folder);
      node.setUserObject(repositoryFolderUserObject);
      return node;
   }

  
   /**
    * creates new Folder Proxy node
    * 
    * @param folder
    * @return Folder Node
    */
   private static DefaultMutableTreeNode createFolderProxyNode(String label, String iconFile, ProcessInstance pi,
         String resourceId)
   {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode();
      RepositoryFolderProxyUserObject repositoryFolderProxyUserObject = new RepositoryFolderProxyUserObject(node,
            resourceId);
      if (null != iconFile)
      {
         repositoryFolderProxyUserObject.setIcon(iconFile);
      }
      if (null != label)
      {
         repositoryFolderProxyUserObject.setLabel(label);
      }
      if (null != pi)
      {
         repositoryFolderProxyUserObject.setProcessInstance(pi);
      }
      node.setUserObject(repositoryFolderProxyUserObject);
      return node;
   }

   /**
    * creates new virtual node
    * 
    * @param folder
    * @return Folder Node
    */
   private static DefaultMutableTreeNode createVirtualNode(String label, String iconFile, ProcessInstance pi)
   {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode();
      RepositoryVirtualUserObject repositoryVirtualUserObject = new RepositoryVirtualUserObject(node);
      if (null != iconFile)
      {
         repositoryVirtualUserObject.setIcon(iconFile);
      }
      if (null != label)
      {
         repositoryVirtualUserObject.setLabel(label);
      }
      if (null != pi)
      {
         repositoryVirtualUserObject.setProcessInstance(pi);
      }
      repositoryVirtualUserObject.setExpanded(true);
      node.setUserObject(repositoryVirtualUserObject);
      return node;
   }
   
   /**
    * creates new document node
    * 
    * @param document
    * @return Document Node
    */
   private static DefaultMutableTreeNode createTypedDocumentNode(TypedDocument typedDocument)
   {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode();
      TypedDocumentUserObject typedDocumentUserObject = new TypedDocumentUserObject(node, typedDocument);
      node.setUserObject(typedDocumentUserObject);
      return node;
   }

   /**
    * returns latest document
    * 
    * @param node
    * @return latest document
    */

   private static Document getUpdatedDocument(DefaultMutableTreeNode node)
   {
      return DocumentMgmtUtility.getDocumentManagementService().getDocument(
            (((RepositoryResourceUserObject) node.getUserObject()).getResource()).getId());
   }

   /**
    * update node with notes nodes
    * 
    * @param parentNode
    * @param notesList
    */

   public static void refreshNoteNodes(DefaultMutableTreeNode parentNode, List<Note> notesList,
         ProcessInstance processInstance)
   {
      parentNode.removeAllChildren();

      int noteIndex = 0;
      for (Note note : notesList)
      {
         parentNode.add(createNoteNode(note, String.valueOf(++noteIndex), processInstance));
      }
   }

   
   /**
    * This method is temporary and needs to be removed after some time. This is kept here
    * to provided backward compatibility to read version comments from old versions
    * 
    * @param document
    * @return
    */
   public static String getVersionComment(Document document)
   {
      String comment = "";
      if (null != document && null != document.getProperties())
      {
         if (document.getProperties().containsKey(CommonProperties.COMMENTS))
            comment = (String) document.getProperties().get(CommonProperties.COMMENTS);
      }
      if (StringUtils.isEmpty(comment))
      {
         comment = document.getRevisionComment();
      }

      return comment;
   }

   
   
   /**
    * This method is temporary and needs to be removed after some time. This is kept here
    * to provided backward compatibility to read description from old versions
    * 
    * @param document
    * @return
    */
   public static String getDescription(Document document)
   {
      String description = "";
      if (null != document && null != document.getProperties())
      {
         if (document.getProperties().containsKey(CommonProperties.DESCRIPTION))
            description = (String) document.getProperties().get(CommonProperties.DESCRIPTION);
      }
      if (StringUtils.isEmpty(description))
      {
         description = document.getDescription();
      }

      return description;
   }

   
   /**
    * create process attachment node
    * 
    * @param processInstance
    * @return
    */
   private static DefaultMutableTreeNode createProcessAttachmentsNode(ProcessInstance processInstance)
   {
      Folder folder = DocumentMgmtUtility.getFolder(DocumentMgmtUtility.getProcessAttachmentsFolderPath(processInstance));
      DefaultMutableTreeNode processAttachmentsNode;
      
      if (null == folder)
      {
         processAttachmentsNode = createFolderProxyNode(
               I18nFolderUtils.getLabel(I18nFolderUtils.PROCESS_ATTACHMENTS_V),
               ResourcePaths.I_PROCESS_ATTACHMENT, processInstance, DocumentMgmtUtility
                     .getProcessAttachmentsFolderPath(processInstance));
         RepositoryResourceUserObject userObject = (RepositoryResourceUserObject)processAttachmentsNode.getUserObject();
         userObject.setExpanded(true);
      }
      else
      {
         processAttachmentsNode = new DefaultMutableTreeNode();
         ProcessAttachmentUserObject processAttachmentUserObject = new ProcessAttachmentUserObject(
               processAttachmentsNode, folder, processInstance);
         processAttachmentsNode.setUserObject(processAttachmentUserObject);
      }
      // Update process attachments
      updateProcessAttachmentNode(processAttachmentsNode, processInstance);
      return processAttachmentsNode;
   }
   
   /**
    * create and return Note node
    * 
    * @param note
    * @return
    */
   private static DefaultMutableTreeNode createNoteNode(Note note, String noteIndex, ProcessInstance processInstance)
   {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode();
      NoteUserObject noteUserObject = new NoteUserObject(node, note, noteIndex, processInstance);
      noteUserObject.setMenuPopupApplicable(false);
      node.setUserObject(noteUserObject);
      return node;
   }

   /**
    * creates the common document node
    * 
    * @return
    */
   private static DefaultMutableTreeNode getCommonDocNode()
   {
      Folder folder = DocumentMgmtUtility.createFolderIfNotExists(DocumentMgmtUtility.DOCUMENTS);
      DefaultMutableTreeNode node = createFolderNode(folder);
      RepositoryFolderUserObject folderObject = (RepositoryFolderUserObject) node.getUserObject();
      folderObject.setLabel(I18nFolderUtils.getLabel(I18nFolderUtils.COMMON_DOCUMENTS_V));
      folderObject.setEditable(false);
      folderObject.setDeletable(false);
      return node;
   }

   /**
    * create and return personal document node
    * 
    * @return
    */
   private static DefaultMutableTreeNode getPersonalDocNode()
   {
      String myDocumentsFolderPath = DocumentMgmtUtility.getMyDocumentsPath();
      Folder folder = DocumentMgmtUtility.getFolder(myDocumentsFolderPath);
      DefaultMutableTreeNode myDocumentNode;

      if (null == folder)
      {
         myDocumentNode = createFolderProxyNode(I18nFolderUtils.getLabel(I18nFolderUtils.MY_DOCUMENTS_V),
               ResourcePaths.I_FOLDER_PERSONAL, null, myDocumentsFolderPath);
         RepositoryFolderProxyUserObject repositoryUserObject = (RepositoryFolderProxyUserObject) myDocumentNode
               .getUserObject();
         repositoryUserObject.setCanCreateFolder(true);
      }
      else
      {
         myDocumentNode = createFolderNode(folder);
         RepositoryFolderUserObject folderObject = (RepositoryFolderUserObject) myDocumentNode.getUserObject();
         folderObject.setLabel(I18nFolderUtils.getLabel(I18nFolderUtils.MY_DOCUMENTS_V));
         folderObject.setIcon(ResourcePaths.I_FOLDER_PERSONAL);
         folderObject.setEditable(false);
         folderObject.setDeletable(false);
      }
      return myDocumentNode;
   }

   /**
    * create and return common stamps folder node
    */
   private static void createCommonStampsFolder()
   {
      DocumentMgmtUtility.createFolderIfNotExists(CommonProperties.COMMON_STAMPS_FOLDER);
   }

   /**
    * Populates the expandedFolders List
    * 
    * @param node
    * @param expandedFolders
    */
   private static void populateExpandedFolderList(DefaultMutableTreeNode node, List<String> expandedFolders)
   {
      if (node.getUserObject() instanceof RepositoryFolderUserObject)
      {
         RepositoryFolderUserObject folderUserObject = (RepositoryFolderUserObject) node.getUserObject();
         if (folderUserObject.isExpanded())
         {
            expandedFolders.add(folderUserObject.getResource().getId());
            int count = node.getChildCount();
            DefaultMutableTreeNode tempNode;
            for (int i = 0; i < count; i++)
            {
               tempNode = (DefaultMutableTreeNode) node.getChildAt(i);
               populateExpandedFolderList(tempNode, expandedFolders);
            }
         }
      }
   }

   /**
    * recursive function to refresh each expanded node
    * 
    * @param node
    * @param expandedFolders
    */
   private static void refreshExpandedNodes(DefaultMutableTreeNode node, List<String> expandedFolders)
   {
      RepositoryResourceUserObject userObject = (RepositoryResourceUserObject) node.getUserObject();
      if (userObject instanceof RepositoryFolderUserObject)
      {
         Folder refreshedFolder = getUpdatedFolder(node);
         if (null != refreshedFolder)
         {
            node.removeAllChildren();

            // update current node
            if (userObject.isEditable())
            {
               RepositoryFolderUserObject folderUserObject = new RepositoryFolderUserObject(node, refreshedFolder);
               node.setUserObject(folderUserObject);
            }

            // update child nodes
            int folderCount = refreshedFolder.getFolderCount();
            int documentCount = refreshedFolder.getDocumentCount();
            Folder folder;
            DefaultMutableTreeNode childNode;
            if (folderCount > 0 || documentCount > 0)
            {
               node.setAllowsChildren(true);
               // create new folder nodes
               for (int i = 0; i < folderCount; i++)
               {
                  folder = (Folder) refreshedFolder.getFolders().get(i);
                  childNode = createFolderNode(folder);
                  node.add(childNode);
                  // refresh the child nodes if it is expanded
                  if (expandedFolders.contains(folder.getId()))
                  {
                     refreshExpandedNodes(childNode, expandedFolders);
                     expandedFolders.remove(folder.getId());
                  }
               }
               // create new document nodes
               Document doc;
               for (int i = 0; i < documentCount; i++)
               {
                  doc = (Document) refreshedFolder.getDocuments().get(i);
                  node.add(createDocumentNode(doc));
               }
            }
            ((RepositoryFolderUserObject) node.getUserObject()).setExpanded(true);
         }
         else
         {
            removeNode(node);
         }
      }// refreshes single document
      else if (node.getUserObject() instanceof RepositoryDocumentUserObject)
      {
         Document refreshedDocument = getUpdatedDocument(node);
         if (null != refreshedDocument)
         {
            RepositoryDocumentUserObject repositoryDocumentUserObject = new RepositoryDocumentUserObject(node,
                  refreshedDocument);
            node.setUserObject(repositoryDocumentUserObject);
         }
         else
         {
            removeNode(node);
         }
      }
   }

   /**
    * removes the node from tree
    * 
    * @param node
    */
   private static void removeNode(DefaultMutableTreeNode node)
   {
      DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
      parentNode.remove(node);
   }
   
   /**
    * @param key
    * @param params
    * @return
    */
   private static String getMessage(String key, String... params)
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      return propsBean.getParamString(key, params);
   }  
   
   private static String getRepositoryLabel(RepositoryNodeUserObject userObject, String defaultRepository)
   {
      String label = "";
      String repoName = userObject.getRepositoryInstance().getRepositoryId();
      label = repoName;
      if (repoName.equals(defaultRepository) || userObject.isDefaultRepository())
      {
         label = label + POSTFIX_OPEN
               + MessagesViewsCommonBean.getInstance().getString(
                     "views.genericRepositoryView.treeMenuItem.repo.default") + POSTFIX_CLOSE;
         
         userObject.setIcon(ResourcePaths.I_REPOSITORY_DEFAULT);
      }
      return label;
   }
   
   /**
    * Create Private Report Definitions node
    * 
    * @return
    */
   private static DefaultMutableTreeNode createPrivateReportDefinitionsNode()
   {
      String myReportDesignsPath = DocumentMgmtUtility.getMyReportDesignsPath();
      Folder folder = DocumentMgmtUtility.getFolder(myReportDesignsPath);
      DefaultMutableTreeNode myReportDesignsNode;
      RepositoryResourceUserObject resourceObject = null;
      if (null == folder)
      {
         myReportDesignsNode = createFolderProxyNode(I18nFolderUtils.getLabel(I18nFolderUtils.PRIVATE_REPORT_DEFINITIONS),
               ResourcePaths.I_FOLDER, null, myReportDesignsPath);
         resourceObject = (RepositoryResourceUserObject) myReportDesignsNode.getUserObject();
      }
      else
      {
         myReportDesignsNode = createFolderNode(folder);
         RepositoryFolderUserObject folderObject = (RepositoryFolderUserObject) myReportDesignsNode.getUserObject();
         folderObject.setLabel(I18nFolderUtils.getLabel(I18nFolderUtils.PRIVATE_REPORT_DEFINITIONS));
         folderObject.setIcon(ResourcePaths.I_FOLDER);
         resourceObject = folderObject;
      }
      resourceObject.setEditable(false);
      resourceObject.setCanCreateFile(false);
      resourceObject.setDeletable(false);
      resourceObject.setCanCreateFolder(true);
      return myReportDesignsNode;
   }
   
   /**
    * Create Public Report Definitions node
    * 
    * @return
    */
   private static DefaultMutableTreeNode createPublicReportDefinitionsNode()
   {
      String publicReportDefinitionsPath = DocumentMgmtUtility.getPublicReportDefinitionsPath();
      Folder folder = DocumentMgmtUtility.getFolder(publicReportDefinitionsPath);
      DefaultMutableTreeNode myReportDesignsNode;
      RepositoryResourceUserObject resourceObject = null;
      if (null == folder)
      {
         myReportDesignsNode = createFolderProxyNode(I18nFolderUtils.getLabel(I18nFolderUtils.PUBLIC_REPORT_DEFINITIONS),
               ResourcePaths.I_FOLDER, null, publicReportDefinitionsPath);
         resourceObject = (RepositoryResourceUserObject) myReportDesignsNode.getUserObject();
      }
      else
      {
         myReportDesignsNode = createFolderNode(folder);
         RepositoryFolderUserObject folderObject = (RepositoryFolderUserObject) myReportDesignsNode.getUserObject();
         folderObject.setLabel(I18nFolderUtils.getLabel(I18nFolderUtils.PUBLIC_REPORT_DEFINITIONS));
         folderObject.setIcon(ResourcePaths.I_FOLDER);
         resourceObject = folderObject;
      }
      resourceObject.setEditable(false);
      resourceObject.setCanCreateFile(false);
      resourceObject.setDeletable(false);
      resourceObject.setCanCreateFolder(true);
      return myReportDesignsNode;
   }
   
   /**
    * Create Role/ORG Report Definitions node
    * 
    * @return
    */
   private static List<DefaultMutableTreeNode> createRoleOrgReportDefinitionsNode()
   {
      
       List<Grant> roleOrgReportDefinitionsGrants = DocumentMgmtUtility.getRoleOrgReportDefinitionsGrants();
       List<DefaultMutableTreeNode> roleOrgReportDefinitionsNodes = new ArrayList<DefaultMutableTreeNode>();
       
       for (Grant grant : roleOrgReportDefinitionsGrants)
       {
          String roleOrgReportDefinitionsPath = DocumentMgmtUtility.getRoleOrgReportDefinitionsPath(grant.getQualifiedId());
          Folder folder = DocumentMgmtUtility.getFolder(roleOrgReportDefinitionsPath);
          DefaultMutableTreeNode roleOrgReportDefinitionsNode = null;
          RepositoryResourceUserObject resourceObject = null;
          String folderLabel = grant.getQualifiedId() + " " + I18nFolderUtils.getLabel(I18nFolderUtils.MY_REPORT_DESIGNS_V);
          if (null == folder)
          {
             roleOrgReportDefinitionsNode = createFolderProxyNode(folderLabel,
                   ResourcePaths.I_FOLDER, null, roleOrgReportDefinitionsPath);
             resourceObject = (RepositoryResourceUserObject) roleOrgReportDefinitionsNode.getUserObject();
          }
          else
          {
             roleOrgReportDefinitionsNode = createFolderNode(folder);
             RepositoryFolderUserObject folderObject = (RepositoryFolderUserObject) roleOrgReportDefinitionsNode.getUserObject();
             folderObject.setLabel(I18nFolderUtils.getLabel(folderLabel));
             folderObject.setIcon(ResourcePaths.I_FOLDER);
             resourceObject = folderObject;
          }
          resourceObject.setEditable(false);
          resourceObject.setCanCreateFile(false);
          resourceObject.setDeletable(false);
          resourceObject.setCanCreateFolder(true);
          
          roleOrgReportDefinitionsNodes.add(roleOrgReportDefinitionsNode);
       }
      
      return roleOrgReportDefinitionsNodes;
   }
   
   /**
    * Create Saved Reports node
    * 
    * @return
    */
   private static DefaultMutableTreeNode createSavedReportsNode(String savedReportsPath, String reportType)
   {
      Folder folder = DocumentMgmtUtility.getFolder(savedReportsPath);
      DefaultMutableTreeNode savedReportsNode;
      RepositoryResourceUserObject resourceObject = null;
      if (null == folder)
      {
         savedReportsNode = createFolderProxyNode(I18nFolderUtils.getLabel(reportType),
               ResourcePaths.I_FOLDER, null, savedReportsPath);
         resourceObject = (RepositoryResourceUserObject) savedReportsNode.getUserObject();
      }
      else
      {
         savedReportsNode = createFolderNode(folder);
         RepositoryFolderUserObject folderObject = (RepositoryFolderUserObject) savedReportsNode.getUserObject();
         folderObject.setLabel(I18nFolderUtils.getLabel(reportType));
         folderObject.setIcon(ResourcePaths.I_FOLDER);
         resourceObject = folderObject;
      }
      resourceObject.setCanCreateFolder(true);
      resourceObject.setEditable(false);
      resourceObject.setCanCreateFile(false);
      resourceObject.setDeletable(false);
      resourceObject.setCanUploadFile(false);
      return savedReportsNode;
   }
   
   /**
    * Create Role/Org Saved Reports node
    * 
    * @return
    */
   private static List<DefaultMutableTreeNode> createRoleOrgSavedReportsNode(
         boolean isAdHoc)
   {
      List<Grant> roleOrgSavedReportsGrants = DocumentMgmtUtility
            .getRoleOrgReportDefinitionsGrants();
      List<DefaultMutableTreeNode> roleOrgSavedReportsNodes = new ArrayList<DefaultMutableTreeNode>();

      for (Grant grant : roleOrgSavedReportsGrants)
      {
         String roleOrgSavedReportsPath = DocumentMgmtUtility.getRoleOrgSavedReportsPath(
               grant.getQualifiedId(), isAdHoc);

         String folderLabel = (isAdHoc) ? I18nFolderUtils.getLabel(I18nFolderUtils.MY_SAVED_REPORTS_V
                     + I18nFolderUtils.AD_HOC) 
               : grant.getQualifiedId() + " "
                  + I18nFolderUtils.getLabel(I18nFolderUtils.MY_SAVED_REPORTS_V);

         DefaultMutableTreeNode savedReportsNode = createSavedReportsNode(
               roleOrgSavedReportsPath, folderLabel);

         roleOrgSavedReportsNodes.add(savedReportsNode);
      }

      return roleOrgSavedReportsNodes;
   }
   
   /**
    * 
    * 
    * @param node
    */
   public static void populateFolderContents(DefaultMutableTreeNode node)
   {
      if (node.getUserObject() instanceof RepositoryFolderUserObject)
      {
         expandTree(node);
         RepositoryFolderUserObject folderUserObject = (RepositoryFolderUserObject) node.getUserObject();
         folderUserObject.setExpanded(false);
      }
   }
}