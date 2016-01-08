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
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.repository.jcr.JcrVfsRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.FolderDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.RepositoryInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.RepositoryProviderDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
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
   
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   // *******************************
   // Folder specific
   // *******************************
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
      folderDTO.documents.addAll(DocumentDTOBuilder.build(folder.getDocuments(), getDMS()));

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

   // *******************************
   // Document specific
   // *******************************
   /**
    *
    */
   @Override
   public DocumentDTO getDocument(String documentId)
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      Document document = getDMS().getDocument(documentId);
      if (document == null)
      {
         throw new I18NException(MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.documentNotFound"));
      }

      return DocumentDTOBuilder.build(document, getDMS());
   }

   /**
    *
    */
   public DocumentDTO copyDocument(String documentId, String targetFolderPath, boolean createVersion)
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
    *
    */
   @Override
   public String getDocumentContent(String documentId)
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);

      // check document exist
      getJCRDocument(documentId);

      return new String(getDMS().retrieveDocumentContent(documentId));
   }

   /**
    *
    */
   @Override
   public List<DocumentDTO> getDocumentHistory(String documentId)
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
                  documentInfoDTO.contentBytes, documentInfoDTO.documentType, documentInfoDTO.contentType,
                  documentInfoDTO.description, documentInfoDTO.comment, null, (Map)documentInfoDTO.properties);
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
    *
    */
   @Override
   public void detachProcessAttachments(List<String> documentIds, ProcessInstance processInstance)
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
    *
    */
   @Override
   public DocumentDTO updateDocument(String documentId, DocumentContentRequestDTO documentInfoDTO)
   {
      documentId = DocumentMgmtUtility.checkAndGetCorrectResourceId(documentId);
      Document document = getDMS().getDocument(documentId);

      if (document == null)
      {
         throw new I18NException(MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.documentNotFound"));
      }

      // check if name is changed
      if ((documentInfoDTO.name != null) && !document.getName().equals(documentInfoDTO.name))
      {
         if (!DocumentMgmtUtility.validateFileName(documentInfoDTO.name))
         {
            throw new I18NException(MessagesViewsCommonBean.getInstance().get("views.common.invalidCharater.message"));
         }
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
    */
   private Document getJCRDocument(String documentId)
   {
      Document document = getDMS().getDocument(documentId);
      if (null == document)
      {
         throw new I18NException(MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.documentNotFound"));
      }
      return document;
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
         RepositoryInstanceDTO repositoryInstanceDTO = DTOBuilder.build(repoInstanceInfo,
               RepositoryInstanceDTO.class);
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

   // *******************************
   // General/common methods
   // *******************************
   
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