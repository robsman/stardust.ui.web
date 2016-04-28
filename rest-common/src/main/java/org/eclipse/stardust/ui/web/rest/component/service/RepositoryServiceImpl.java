/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.repository.jcr.JcrVfsRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.component.cachemanager.UserAttributesCacheManager;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.builder.FolderDTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.rest.dto.request.RepositorySearchRequestDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.RepositoryInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.RepositoryProviderDTO;
import org.eclipse.stardust.ui.web.rest.util.DocumentSearchUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DefualtResourceDataProvider;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IResourceDataProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class RepositoryServiceImpl implements RepositoryService
{
   private static final String PROVIDER_ID = "providerId";
   private static final String REPOSITORY_ID = "id";
   private static String DOCUMENTS = "documents";
   private static String FOLDERS = "folders";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private UserService userService;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;
   
   @Resource
   UserAttributesCacheManager userAttributesCacheManager;
   

   // *******************************
   // Folder specific
   // *******************************
   /**
    * @throws ResourceNotFoundException
    *
    */
   public FolderDTO getFolder(String folderId, int levelOfDetail, boolean createIfDoesNotExist)
         throws ResourceNotFoundException
   {
      if (StringUtils.isEmpty(folderId))
      {
         folderId = "/";
      }

      // remove the trailing slash if it exist
      if (folderId.length() != 1 && folderId.charAt(folderId.length() - 1) == '/')
      {
         folderId = folderId.substring(0, folderId.length() - 1);
      }

      // remove unwanted slashes
      folderId = folderId.replaceAll("//", "/");

      // fetching of children information may be time consuming, may need to be
      // parameterized later
      Folder folder = getDMS().getFolder(folderId, levelOfDetail);

      if (folder == null && !createIfDoesNotExist)
      {
         throw new ResourceNotFoundException(restCommonClientMessages.getParamString("folder.notFound", folderId));
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
      folderDTO.documents.addAll(DocumentDTOBuilder.build(folder.getDocuments(), getDMS()));

      return folderDTO;
   }

   /**
    * @throws ResourceNotFoundException
    *
    */
   @Override
   public FolderDTO createFolder(String folderId, Map<String, Object> folderDataMap) throws ResourceNotFoundException
   {
      Folder folder = null;
      String parentFolderPath = null;

      if (folderDataMap != null && folderDataMap.get("parentFolderPath") != null)
      {
         parentFolderPath = (String) folderDataMap.get("parentFolderPath");
      }

      if (!StringUtils.isEmpty(parentFolderPath))
      {
         folder = DocumentMgmtUtility.createFolderIfNotExists(parentFolderPath);
         String folderName = (String) folderDataMap.get("name");
         folder = getDMS().createFolder(folder.getId(), DmsUtils.createFolderInfo(folderName));
      }
      else if (StringUtils.isNotEmpty(folderId))
      {
         folderId = DocumentMgmtUtility.checkAndGetCorrectResourceId(folderId);
         folder = DocumentMgmtUtility.createFolderIfNotExists(folderId);
         if (folderDataMap.get("name") != null)
         {
            String folderName = (String) folderDataMap.get("name");
            folder = getDMS().createFolder(folder.getId(), DmsUtils.createFolderInfo(folderName));
         }
      }
      else
      {
         throw new ResourceNotFoundException(restCommonClientMessages.getParamString("folder.notFound", folderId));
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
      Folder folder = DocumentMgmtUtility.getFolderIfExist(folderId);
      String newName = String.valueOf(documentDataMap.get("name"));
      folder.setName(newName);
      getDMS().updateFolder(folder);
   }

   /**
    *
    */
   public IResourceDataProvider exportFolder(String folderId)
   {
      folderId = DocumentMgmtUtility.checkAndGetCorrectResourceId(folderId);
      Folder rootFolder = getDMS().getFolder(folderId);
      return new DefualtResourceDataProvider(rootFolder.getName(), rootFolder.getId(), "", getDMS(), false);
   };

   /**
    * @throws Exception
    *
    */
   @Override
   public Map<String, Set<String>> importFolder(String folderId, List<DocumentContentRequestDTO> uploadedFolder,
         boolean merge) throws Exception
   {
      folderId = DocumentMgmtUtility.checkAndGetCorrectResourceId(folderId);
      for (DocumentContentRequestDTO documentContentRequestDTO : uploadedFolder)
      {
         return DocumentMgmtUtility.importFolderFromZip(folderId, documentContentRequestDTO.contentBytes, merge);
      }
      return null;
   }

   /**
    * @param searchRequestDTO
    * @return
    */
   public QueryResultDTO SearchFolder(RepositorySearchRequestDTO searchRequestDTO)
   {
      String xPathQuery = "//*[jcr:like(fn:lower-case(@vfs:metaData/vfs:name), '%"
            + searchRequestDTO.name.toLowerCase() + "%')]";

      List<Folder> folders = DocumentMgmtUtility.getDocumentManagementService().findFolders(xPathQuery,
            Folder.LOD_NO_MEMBERS);

      return buildFolderSearchResult(folders, searchRequestDTO);
   }

   /**
    * @param folders
    * @param searchRequestDTO
    * @return
    */
   private QueryResultDTO buildFolderSearchResult(List<Folder> folders, RepositorySearchRequestDTO searchRequestDTO)
   {
      QueryResultDTO resultDTO = new QueryResultDTO();

      if (folders != null)
      {
         if (searchRequestDTO.documentDataTableOption != null)
         {
            int pageSize = searchRequestDTO.documentDataTableOption.pageSize;
            if (folders.size() < pageSize)
            {
               pageSize = folders.size();
            }
            resultDTO.list = FolderDTOBuilder.build(folders.subList(0, pageSize));
         }
         else
         {
            resultDTO.list = FolderDTOBuilder.build(folders);
         }

         resultDTO.totalCount = folders.size();
      }
      return resultDTO;
   }

   // *******************************
   // Document specific
   // *******************************
   /**
    * @throws ResourceNotFoundException
    *
    */
   @Override
   public DocumentDTO getDocument(String documentId) throws ResourceNotFoundException
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      Document document = getDMS().getDocument(documentId);
      if (document == null)
      {
         throw new ResourceNotFoundException(MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.documentNotFound"));
      }

      return DocumentDTOBuilder.build(document, getDMS());
   }

   /**
    * @throws ResourceNotFoundException
    *
    */
   public DocumentDTO copyDocument(String documentId, String targetFolderPath, boolean createVersion)
         throws ResourceNotFoundException
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      return DocumentDTOBuilder.build(
            DocumentMgmtUtility.copyDocumentTo(getJCRDocument(documentId), targetFolderPath, createVersion), getDMS());
   }

   /**
    * 
    */
   public DocumentDTO revertDocument(String documentId, DocumentContentRequestDTO documentContentRequestDTO)
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      Document document = getDMS().getDocument(documentId);
      Document versionToRevertTo = getDMS().getDocument(documentContentRequestDTO.uuid);

      document.setDocumentType(versionToRevertTo.getDocumentType());
      document.setDescription(documentContentRequestDTO.description);

      document = getDMS().updateDocument(document, getDMS().retrieveDocumentContent(versionToRevertTo.getRevisionId()),
            "", documentContentRequestDTO.createNewRevision, documentContentRequestDTO.comment, "", false);

      return DocumentDTOBuilder.build(document, getDMS());
   }

   /**
    * @throws ResourceNotFoundException
    *
    */
   @Override
   public String getDocumentContent(String documentId) throws ResourceNotFoundException
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);

      // check document exist
      getJCRDocument(documentId);

      return new String(getDMS().retrieveDocumentContent(documentId));
   }

   /**
    * @throws ResourceNotFoundException
    *
    */
   @Override
   public List<DocumentDTO> getDocumentHistory(String documentId) throws ResourceNotFoundException
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      Document document = getJCRDocument(documentId);
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

      List<DocumentDTO> previousVersions = DocumentDTOBuilder.build(docVersionList, null, getDMS());

      return previousVersions;
   }

   @Override
   public Map<String, Object> createDocuments(List<DocumentContentRequestDTO> documentInfoDTOs)
   {
      return createDocuments(documentInfoDTOs, null, null, false);
   }

   @Override
   public Map<String, Object> createActivityDocuments(List<DocumentContentRequestDTO> documentInfoDTOs,
         ActivityInstance activityInstance)
   {
      return createDocuments(documentInfoDTOs, activityInstance, activityInstance.getProcessInstance(), true);
   }

   @Override
   public Map<String, Object> createProcessDocuments(List<DocumentContentRequestDTO> documentInfoDTOs,
         ProcessInstance processInstance, boolean processAttachments)
   {
      return createDocuments(documentInfoDTOs, null, processInstance, processAttachments);
   }

   @Override
   public Map<String, Object> createProcessDocument(DocumentContentRequestDTO documentInfoDTO,
         ProcessInstance processInstance, boolean processAttachments)
   {
      List<DocumentContentRequestDTO> documentInfoDTOs = new ArrayList<DocumentContentRequestDTO>();
      documentInfoDTOs.add(documentInfoDTO);
      return createDocuments(documentInfoDTOs, null, processInstance, processAttachments);
   }

   /**
    * @param documentInfoDTOs
    * @param activityInstance
    *           - pass this only if it is activity attachment
    * @param processInstance
    * @param processAttachments
    * @return
    */
   private Map<String, Object> createDocuments(List<DocumentContentRequestDTO> documentInfoDTOs,
         ActivityInstance activityInstance, ProcessInstance processInstance, boolean processAttachments)
   {
      Map<String, Object> result = new HashMap<String, Object>();
      List<NotificationDTO> failures = new ArrayList<NotificationDTO>();
      result.put("failures", failures);
      List<DocumentDTO> documentDTOs = new ArrayList<DocumentDTO>();
      result.put("documents", documentDTOs);

      String attachmentFolderPath = null;
      Folder parentFolder = null;
      if (processAttachments)
      {
         if (activityInstance != null)
         {
            attachmentFolderPath = DocumentMgmtUtility.getActivityAttachmentsFolderPath(activityInstance);
         }
         else
         {
            attachmentFolderPath = DocumentMgmtUtility.getProcessAttachmentsFolderPath(processInstance);
         }

         parentFolder = DocumentMgmtUtility.createFolderIfNotExists(attachmentFolderPath);
      }

      List<Document> documents = new ArrayList<Document>();

      for (DocumentContentRequestDTO documentInfoDTO : documentInfoDTOs)
      {
         evaluateContent(documentInfoDTO);

         if (!DocumentMgmtUtility.validateFileName(documentInfoDTO.name))
         {
            failures.add(new NotificationDTO(null, documentInfoDTO.name, MessagesViewsCommonBean.getInstance().get(
                  "views.common.invalidCharater.message")));
            continue;
         }

         if (!processAttachments)
         {
            if (StringUtils.isEmpty(documentInfoDTO.parentFolderPath))
            {
               failures.add(new NotificationDTO(null, documentInfoDTO.name, MessagesViewsCommonBean.getInstance().get(
                     "views.genericRepositoryView.document.parentFolderError")));
               continue;
            }

            parentFolder = DocumentMgmtUtility.createFolderIfNotExists(documentInfoDTO.parentFolderPath);
         }
         else
         {
            documentInfoDTO.dataPathId = CommonProperties.PROCESS_ATTACHMENTS;
         }

         Document document = DocumentMgmtUtility.getDocument(parentFolder, documentInfoDTO.name);

         if (!documentInfoDTO.isCreateVersion() && !documentInfoDTO.isRename() && document != null)
         {
            failures.add(new NotificationDTO(null, documentInfoDTO.name, restCommonClientMessages.getParamString(
                  "document.existError", documentInfoDTO.name)));
            continue;
         }

         if (document != null && documentInfoDTO.isRename())
         {
            String newFileName = getNewFileName(getDMS().getFolder(parentFolder.getId(), Folder.LOD_LIST_MEMBERS)
                  .getDocuments(), document.getName());
            documentInfoDTO.name = newFileName;
            document = null;
         }
         
         if (document == null)
         {
            // create document
            document = DocumentMgmtUtility.createDocument(parentFolder.getId(), documentInfoDTO.name,
                  documentInfoDTO.contentBytes, documentInfoDTO.documentType, documentInfoDTO.contentType,
                  documentInfoDTO.description, documentInfoDTO.comment, null, (Map) documentInfoDTO.properties);
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
            document = updateDocument(document, documentInfoDTO);
         }

         documents.add(document);
         documentDTOs.add(DocumentDTOBuilder.build(document, getDMS()));
      }

      if (processInstance != null && CollectionUtils.isNotEmpty(documents))
      {
         DMSHelper.addAndSaveProcessAttachments(processInstance, documents);
      }
      return result;
   }

   /**
    * @param documentInfoDTO
    * @return
    */
   private void evaluateContent(DocumentContentRequestDTO documentInfoDTO)
   {
      if (documentInfoDTO.contentBytes == null && documentInfoDTO.contentBase64 != null)
      {
         String decryptedContent = new String(Base64.decode(documentInfoDTO.contentBase64.getBytes()));
         documentInfoDTO.contentBytes = decryptedContent.getBytes();
      }
      else if (documentInfoDTO.contentBytes == null && documentInfoDTO.content != null)
      {
         documentInfoDTO.contentBytes = documentInfoDTO.content.getBytes();
      }
   }

   /**
    * @throws ResourceNotFoundException
    *
    */
   @Override
   public void detachProcessAttachments(List<String> documentIds, ProcessInstance processInstance)
         throws ResourceNotFoundException
   {
      List<Document> documentsToBeDetached = new ArrayList<Document>();

      for (String documentId : documentIds)
      {
         documentsToBeDetached.add(getJCRDocument(documentId));
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
   public void deleteDocument(String documentId)
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      // note that if the deleted document is process attachment or typed document, it is
      // also updated as per CRNT-34022, special treatment is not required.
      DocumentMgmtUtility.deleteDocumentWithVersions(getDMS().getDocument(documentId));
   }

   /**
    * @throws ResourceNotFoundException
    *
    */
   @Override
   public DocumentDTO updateDocument(String documentId, DocumentContentRequestDTO documentInfoDTO)
         throws ResourceNotFoundException
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      Document document = getDMS().getDocument(documentId);

      if (document == null)
      {
         throw new ResourceNotFoundException(MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.documentNotFound"));
      }

      // check if name is changed
      if ((documentInfoDTO.name != null) && !document.getName().equals(documentInfoDTO.name))
      {
         if (!DocumentMgmtUtility.validateFileName(documentInfoDTO.name))
         {
            throw new I18NException(MessagesViewsCommonBean.getInstance().get("views.common.invalidCharater.message"));
         }
         document.setName(documentInfoDTO.name);
      }

      // check if content-type has changed
      if ((documentInfoDTO.contentType != null) && !document.getContentType().equals(documentInfoDTO.contentType))
      {
         document.setContentType(documentInfoDTO.contentType);
      }

      return DocumentDTOBuilder.build(updateDocument(document, documentInfoDTO), getDMS());
   }

   /**
    * @param document
    * @param documentInfoDTO
    * @return
    */
   private Document updateDocument(Document document, DocumentContentRequestDTO documentInfoDTO)
   {
      document.setDescription(documentInfoDTO.description);
      document.setProperties(documentInfoDTO.properties);

      // TODO: updated the current user as owner?
      document.setOwner(SessionContext.findSessionContext().getUser().getAccount());

      // TODO: is versioning really required?
      if (documentInfoDTO.createNewRevision && !DocumentMgmtUtility.isDocumentVersioned(document))
      {
         getDMS().versionDocument(document.getId(), "", null);
      }

      if (documentInfoDTO.contentBytes == null)
      {
         // rename or just change in description and addition of comment
         document = getDMS().updateDocument(document, documentInfoDTO.createNewRevision, documentInfoDTO.comment, "",
               false);
      }
      else
      {
         // content changed
         document = getDMS().updateDocument(document, documentInfoDTO.contentBytes, "",
               documentInfoDTO.createNewRevision, documentInfoDTO.comment, "", false);
      }

      return document;
   }

   /**
    * throws exception in case of absence of document
    * 
    * @param documentId
    * @return
    * @throws ResourceNotFoundException
    */
   private Document getJCRDocument(String documentId) throws ResourceNotFoundException
   {
      Document document = getDMS().getDocument(documentId);
      if (null == document)
      {
         throw new ResourceNotFoundException(MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.documentNotFound"));
      }
      return document;
   }

   /**
    * @param searchRequestDTO
    * @return
    */
   public QueryResultDTO SearchDocuments(RepositorySearchRequestDTO searchRequestDTO)
   {
      QueryResult<Document> docs = DocumentSearchUtils.search(searchRequestDTO, getDMS());
      return buildDocumentSearchResult(docs, searchRequestDTO);
   }

   /**
    * @param docs
    * @param searchRequestDTO
    * @return
    */
   private QueryResultDTO buildDocumentSearchResult(QueryResult<Document> docs,
         RepositorySearchRequestDTO searchRequestDTO)
   {
      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = DocumentDTOBuilder.build(docs, getDMS(), searchRequestDTO.documentDetailLevelDTO,
            userAttributesCacheManager);
      resultDTO.totalCount = docs.getTotalCount();
      return resultDTO;
   }

   /**
    *
    */
   @Override
   public List<DocumentDTO> moveDocument(List<String> documentIds, String targetPath)
   {
      List<DocumentDTO> docDTOs = new ArrayList<DocumentDTO>();
      for (String id : documentIds)
      {
         Document document = getDMS().moveDocument(id, targetPath);
         docDTOs.add(DocumentDTOBuilder.build(document, getDMS()));
      }
      return docDTOs;
   }
   
   // *******************************
   // Repository level operations
   // *******************************

   @Override
   public List<RepositoryProviderDTO> getRepositoryProviders()
   {
      List<RepositoryProviderDTO> providers = new ArrayList<RepositoryProviderDTO>();
      List<IRepositoryProviderInfo> repProviders = getDMS().getRepositoryProviderInfos();

      for (IRepositoryProviderInfo repositoryProviderInfo : repProviders)
      {
         RepositoryProviderDTO providerDTO = DTOBuilder.build(repositoryProviderInfo, RepositoryProviderDTO.class);
         providers.add(providerDTO);
      }

      Collections.sort(providers, new Comparator<RepositoryProviderDTO>()
      {
         @Override
         public int compare(RepositoryProviderDTO o1, RepositoryProviderDTO o2)
         {
            return o1.name.compareTo(o2.name);
         }
      });
      return providers;
   }

   /**
    *
    */
   @Override
   public List<RepositoryInstanceDTO> getRepositories()
   {
      List<IRepositoryInstanceInfo> repositoryInstanceInfos = DocumentMgmtUtility.getDocumentManagementService()
            .getRepositoryInstanceInfos();

      List<RepositoryInstanceDTO> repositoryInstances = new ArrayList<RepositoryInstanceDTO>();

      String defaultRepository = DocumentMgmtUtility.getDocumentManagementService().getDefaultRepository();
      for (IRepositoryInstanceInfo repoInstanceInfo : repositoryInstanceInfos)
      {
         RepositoryInstanceDTO repositoryInstanceDTO = DTOBuilder.build(repoInstanceInfo, RepositoryInstanceDTO.class);
         repositoryInstances.add(repositoryInstanceDTO);
         if (defaultRepository.equals(repoInstanceInfo.getRepositoryId()))
         {
            repositoryInstanceDTO.isDefualt = true;
         }
      }

      Collections.sort(repositoryInstances, new Comparator<RepositoryInstanceDTO>()
      {
         @Override
         public int compare(RepositoryInstanceDTO o1, RepositoryInstanceDTO o2)
         {
            return o1.name.compareTo(o2.name);
         }
      });

      return repositoryInstances;
   }

   /**
    *
    */
   @Override
   public void setDefualtRepository(String repositoryId)
   {
      validateRepositoryId(repositoryId);
      DocumentMgmtUtility.getDocumentManagementService().setDefaultRepository(repositoryId);
   }

   /**
    *
    */
   @Override
   public void bindRepository(Map<String, Object> repositoryAttributes)
   {
      String repositoryId = null;
      String providerId = null;

      if (repositoryAttributes.get(REPOSITORY_ID) != null)
      {
         repositoryId = ((String) repositoryAttributes.get(REPOSITORY_ID)).trim();
      }

      if (repositoryAttributes.get(PROVIDER_ID) != null)
      {
         providerId = ((String) repositoryAttributes.get(PROVIDER_ID)).trim();
      }

      validateRepositoryId(repositoryId);

      if (StringUtils.isEmpty(providerId))
      {
         throw new I18NException(MessagesViewsCommonBean.getInstance().getString(
               "views.bindRepositoryDialog.providerId.invalid"));
      }

      Map<String, Serializable> attributes = CollectionUtils.newMap();
      attributes.put(IRepositoryConfiguration.PROVIDER_ID, providerId);
      attributes.put(IRepositoryConfiguration.REPOSITORY_ID, repositoryId);
      for (Map.Entry<String, Object> entry : repositoryAttributes.entrySet())
      {
         if (!PROVIDER_ID.equals(entry.getKey()) || !REPOSITORY_ID.equals(entry.getKey()))
         {
            attributes.put(entry.getKey(), (Serializable) entry.getValue());
         }
      }
      attributes.put(JcrVfsRepositoryConfiguration.USER_LEVEL_AUTHORIZATION, true);
      DocumentMgmtUtility.getDocumentManagementService().bindRepository(new JcrVfsRepositoryConfiguration(attributes));
   }

   /**
    *
    */
   @Override
   public void unbindRepository(String repositoryId)
   {
      validateRepositoryId(repositoryId);
      DocumentMgmtUtility.getDocumentManagementService().unbindRepository(repositoryId);
   }

   /**
    * search both documents and folders based on input parameters
    * 
    * @param repositorySearchRequestDTO
    */
   @Override
   public Map<String, QueryResultDTO> searchResources(RepositorySearchRequestDTO repositorySearchRequestDTO)
   {
      Map<String, QueryResultDTO> result = new HashMap<String, QueryResultDTO>();

      repositorySearchRequestDTO.searchType = repositorySearchRequestDTO.searchType.toLowerCase();

      if (RepositorySearchRequestDTO.Search_Type.Both.name().toLowerCase()
            .equals(repositorySearchRequestDTO.searchType))
      {
         result.put(DOCUMENTS, SearchDocuments(repositorySearchRequestDTO));
         result.put(FOLDERS, SearchFolder(repositorySearchRequestDTO));
      }
      else if (RepositorySearchRequestDTO.Search_Type.Document.name().toLowerCase()
            .equals(repositorySearchRequestDTO.searchType))
      {
         result.put(DOCUMENTS, SearchDocuments(repositorySearchRequestDTO));
      }
      else if (RepositorySearchRequestDTO.Search_Type.Folder.name().toLowerCase()
            .equals(repositorySearchRequestDTO.searchType))
      {
         result.put(FOLDERS, SearchFolder(repositorySearchRequestDTO));
      }
      return result;
   }

   // *******************************
   // General/common methods
   // *******************************

   /**
    * @param documents
    * @param fileName
    * @return
    */
   private String getNewFileName(List<Document> documents, String fileName)
   {
      String justName = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringBeforeLast(fileName, ".");
      String ext = fileName.substring(justName.length());

      String copy = MessagesViewsCommonBean.getInstance().get("views.genericRepositoryView.copy");
      String newName = justName + " - " + copy + ext;

      int count = 2;

      while (true)
      {
         boolean found = false;

         for (Document document : documents)
         {
            if (newName.equals(document.getName()))
            {
               found = true;
            }
         }

         if (!found)
         {
            break;
         }
         newName = justName + " - " + copy + " (" + count++ + ")" + ext;
      }
      return newName;
   }

   /**
    * @param repositoryId
    */
   private void validateRepositoryId(String repositoryId)
   {
      if (StringUtils.isEmpty(repositoryId))
      {
         throw new I18NException(MessagesViewsCommonBean.getInstance().getString(
               "views.bindRepositoryDialog.repoId.empty"));
      }
      else if (!Pattern.matches("^(?=.*[a-zA-Z\\d]).+$", repositoryId))
      {
         throw new I18NException(MessagesViewsCommonBean.getInstance().getString(
               "views.bindRepositoryDialog.repoId.invalid"));
      }
   }

   private DocumentManagementService getDMS()
   {
      return serviceFactoryUtils.getDocumentManagementService();
   }
}