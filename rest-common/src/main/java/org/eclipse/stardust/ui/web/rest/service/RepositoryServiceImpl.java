/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.FolderDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class RepositoryServiceImpl implements RepositoryService
{
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   // Folder Specific
   /**
    *
    */
   public FolderDTO getFolder(String folderId, int levelOfDetail, boolean createIfDoesNotExist)
   {
      // remove the trailing slash if it exist
      if (folderId.length() != 1 && folderId.charAt(folderId.length() - 1) == '/')
      {
         folderId = folderId.substring(0, folderId.length() - 1);
      }

      // fetching of children information may be time consuming, may need to be
      // parameterized later
      Folder folder = getDMS().getFolder(folderId, levelOfDetail);

      if (folder == null && !createIfDoesNotExist)
      {
         throw new I18NException(restCommonClientMessages.getParamString("folder.notFound", folderId));
      }

      if (folder == null)
      {
         folder = DocumentMgmtUtility.createFolderIfNotExists(folderId);
      }

      FolderDTO folderDTO = FolderDTOBuilder.build(folder);
      folderDTO.folders = new ArrayList<FolderDTO>();
      folderDTO.documents = new ArrayList<DocumentDTO>();

      // add sub-folders
      folderDTO.folders.addAll(FolderDTOBuilder.build(folder.getFolders()));

      // add documents
      folderDTO.documents.addAll(DocumentDTOBuilder.build(folder.getDocuments()));

      return folderDTO;
   }

   /**
    *
    */
   @Override
   public FolderDTO createFolder(String folderId, Map<String, Object> folderDataMap)
   {
      Folder folder = null;

      if (!StringUtils.isEmpty(folderId))
      {
         folderId = DocumentMgmtUtility.checkAndGetCorrectResourceId(folderId);
         folder = DocumentMgmtUtility.createFolderIfNotExists(folderId);
      }
      else
      {
         String parentFolderPath = (String) folderDataMap.get("parentFolderId");
         folder = DocumentMgmtUtility.createFolderIfNotExists(parentFolderPath);
         String folderName = (String) folderDataMap.get("folderName");
         folder = DocumentMgmtUtility.createFolderIfNotExists(folder.getPath() + "/" + folderName);
      }
      return FolderDTOBuilder.build(folder);
   }

   /**
    *
    */
   @Override
   public void updateFolder(String folderId, Map<String, Object> documentDataMap)
   {
      folderId = DocumentMgmtUtility.checkAndGetCorrectResourceId(folderId);

      Folder folder = getDMS().getFolder(folderId);

      String newName = String.valueOf(documentDataMap.get("name"));

      String parentPath = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringBeforeLast(
            folder.getPath(), "/");

      if (DocumentMgmtUtility.isFolderPresent(parentPath, newName))
      {
         throw new I18NException(MessagesViewsCommonBean.getInstance().get(
               "views.genericRepositoryView.folderAlreadyPresent"));
      }

      folder.setName(newName);
      getDMS().updateFolder(folder);
   }

   // Document specific

   /**
    *
    */
   @Override
   public DocumentDTO getDocument(String documentId)
   {
      Document document = getDMS().getDocument(documentId);
      if (document == null)
      {
         throw new I18NException(MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.documentNotFound"));
      }

      return DocumentDTOBuilder.build(document);
   }

   /**
    *
    */
   @Override
   public byte[] getDocumentContent(String documentId)
   {
      return getDMS().retrieveDocumentContent(documentId);
   }

   /**
    *
    */
   @Override
   public List<DocumentDTO> getDocumentHistory(String documentId)
   {
      Document document = getDMS().getDocument(documentId);
      List<Document> docVersionList = null;
      
      if (DocumentMgmtUtility.isDocumentVersioned(document))
      {
         docVersionList = getDMS().getDocumentVersions(document.getId());
      }
      else
      {
         return null;
      }
      
      Collections.sort(docVersionList, new Comparator<Document>()
      {
         @Override
         public int compare(Document doc1, Document doc2)
         {
            return doc2.getDateLastModified().compareTo(doc1.getDateLastModified());
         }
      });
     
      List<DocumentDTO> previousVersions = DocumentDTOBuilder.build(docVersionList, null);
      
      return previousVersions;
   }

   /**
    *
    */
   @Override
   public Map<String, Object> createDocument(DocumentContentRequestDTO documentInfoDTO,
         ProcessInstance processInstance, boolean processAttachments)
   {
      List<DocumentContentRequestDTO> documentInfoDTOs = new ArrayList<DocumentContentRequestDTO>();
      documentInfoDTOs.add(documentInfoDTO);
      return createDocuments(documentInfoDTOs, processInstance, processAttachments);
   }

   /**
    *
    */
   @Override
   public Map<String, Object> createDocuments(List<DocumentContentRequestDTO> documentInfoDTOs,
         ProcessInstance processInstance, boolean processAttachments)
   {
      Map<String, Object> result = new HashMap<String, Object>();
      List<NotificationDTO> failures = new ArrayList<NotificationDTO>();
      result.put("failures", failures);
      List<DocumentDTO> documentDTOs = new ArrayList<DocumentDTO>();
      result.put("documents", documentDTOs);

      String processAttachmentFolderPath = null;
      Folder parentFolder = null;
      if (processAttachments)
      {
         processAttachmentFolderPath = DocumentMgmtUtility.getProcessAttachmentsFolderPath(processInstance);
         parentFolder = DocumentMgmtUtility.createFolderIfNotExists(processAttachmentFolderPath);
      }

      List<Document> documents = new ArrayList<Document>();

      for (DocumentContentRequestDTO documentInfoDTO : documentInfoDTOs)
      {
         if (!DocumentMgmtUtility.validateFileName(documentInfoDTO.name))
         {
            failures.add(new NotificationDTO(null, documentInfoDTO.name, MessagesViewsCommonBean.getInstance().get(
                  "views.common.invalidCharater.message")));
            continue;
         }

         if (!processAttachments)
         {
            parentFolder = DocumentMgmtUtility.createFolderIfNotExists(documentInfoDTO.parentFolderPath);
         }

         Document document = DocumentMgmtUtility.getDocument(parentFolder.getPath(), documentInfoDTO.name);

         if (!documentInfoDTO.createVersion && document != null)
         {
            failures.add(new NotificationDTO(null, documentInfoDTO.name, restCommonClientMessages.getParamString(
                  "document.existError", documentInfoDTO.name)));
            continue;
         }

         if (document == null)
         {
            // create document
            document = DocumentMgmtUtility.createDocument(parentFolder.getId(), documentInfoDTO.name,
                  documentInfoDTO.content, documentInfoDTO.documentType, documentInfoDTO.contentType,
                  documentInfoDTO.description, documentInfoDTO.comments, null, null);
            if (processInstance != null)
            {
               if (!CommonProperties.PROCESS_ATTACHMENTS.equals(documentInfoDTO.dataPathId))
               {
                  serviceFactoryUtils.getWorkflowService().setOutDataPath(processInstance.getOID(),
                        documentInfoDTO.dataPathId, document);
               }
            }
         }
         else
         {
            updateDocument(document, documentInfoDTO);
         }

         documents.add(document);
         documentDTOs.add(DocumentDTOBuilder.build(document));
      }

      if (processInstance != null && CollectionUtils.isNotEmpty(documents))
      {
         DMSHelper.addAndSaveProcessAttachments(processInstance, documents);
      }
      return result;
   }

   /**
    *
    */
   @Override
   public void detachProcessAttachments(List<String> documentIds, ProcessInstance processInstance)
         throws ResourceNotFoundException
   {
      List<Document> documentsToBeDetached = new ArrayList<Document>();

      for (String documentId : documentIds)
      {
         documentsToBeDetached.add(DocumentMgmtUtility.getDocument(documentId));
      }
      DMSHelper.detachProcessAttachments(processInstance, documentsToBeDetached);
   }

   /**
    *
    */
   @Override
   public void deleteFolder(String folderId)
   {
      folderId = DocumentMgmtUtility.checkAndGetCorrectResourceId(folderId);
      getDMS().removeFolder(folderId, true);
   }

   /**
    *
    */
   @Override
   public void deleteDocument(String documentId) throws DocumentManagementServiceException, ResourceNotFoundException
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      // note that if the deleted document is process attachment or typed document, it is
      // also updated as per CRNT-34022, special treatment is not required.
      DocumentMgmtUtility.deleteDocumentWithVersions(getDMS().getDocument(documentId));
   }

   /**
    *
    */
   @Override
   public void updateDocument(String documentId, DocumentContentRequestDTO documentInfoDTO)
         throws DocumentManagementServiceException, UnsupportedEncodingException
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      Document document = getDMS().getDocument(documentId);

      // check if name is changed
      if (!document.getName().equals(documentInfoDTO.name))
      {
         if (!DocumentMgmtUtility.validateFileName(documentInfoDTO.name))
         {
            throw new I18NException(MessagesViewsCommonBean.getInstance().get("views.common.invalidCharater.message"));
         }
      }

      updateDocument(document, documentInfoDTO);
   }

   /**
    * @param document
    * @param documentInfoDTO
    */
   private void updateDocument(Document document, DocumentContentRequestDTO documentInfoDTO)
   {
      document.setName(documentInfoDTO.name);
      document.setDescription(documentInfoDTO.description);

      // TODO: updated the current user as owner??
      document.setOwner(SessionContext.findSessionContext().getUser().getAccount());

      // TODO: is versioning really required?
      if (documentInfoDTO.createNewRevision && DocumentMgmtUtility.isDocumentVersioned(document))
      {
         getDMS().versionDocument(document.getId(), "", null);
      }

      if (documentInfoDTO.content == null)
      {
         // rename or just change in description and addition of comment
         document = getDMS().updateDocument(document, documentInfoDTO.createNewRevision, documentInfoDTO.comments, "",
               false);
      }
      else
      {
         // content changed
         document = getDMS().updateDocument(document, documentInfoDTO.content, "", documentInfoDTO.createNewRevision,
               documentInfoDTO.comments, "", false);
      }
   }

   /**
    * @return
    */
   private DocumentManagementService getDMS()
   {
      return serviceFactoryUtils.getDocumentManagementService();
   }
}