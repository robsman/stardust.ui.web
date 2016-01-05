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

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.RepositoryInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.RepositoryProviderDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
public interface RepositoryService
{
   // *******************************
   // Folder specific
   // *******************************
   
   /**
    * @param folderId
    * @param levelOfDetail
    * @param createIfDoesNotExist
    * @return
    */
   FolderDTO getFolder(String folderId, int levelOfDetail, boolean createIfDoesNotExist);

   /**
    * @param folderId
    * @param folderDataMap
    */
   FolderDTO createFolder(String folderId, Map<String, Object> folderDataMap);

   /**
    * @param folderId
    * @param folderDataMap
    */
   void updateFolder(String folderId, Map<String, Object> folderDataMap);

   /**
    * @param folderId
    */
   void deleteFolder(String folderId);

   /**
    * @param resourceId
    * @return
    */
   byte[] exportFolder(String resourceId);

   /**
    * @param folderId
    * @param uploadedFolder
    * @param merge
    * @throws Exception
    */
   void importFolder(String folderId, List<DocumentContentRequestDTO> uploadedFolder, boolean merge) throws Exception;

   // *******************************
   // Document specific
   // *******************************
   /**
    * @param documentId
    * @return
    */
   DocumentDTO getDocument(String documentId);

   /**
    * @param documentId
    * @param targetFolderPath
    * @param createVersion
    * @return
    */
   DocumentDTO copyDocument(String documentId, String targetFolderPath, boolean createVersion);

   /**
    * reverts to provided previous revision - in a process, copies attributes - content
    * and document type whereas comments and descriptions are provided by client
    * 
    * @param documentId
    * @param documentInfoDTO
    * @return
    */
   DocumentDTO revertDocument(String documentId, DocumentContentRequestDTO documentInfoDTO);

   /**
    * @param documentId
    * @return
    */
   String getDocumentContent(String documentId);

   /**
    * @return
    */
   List<DocumentDTO> getDocumentHistory(String DocumentId);

   /**
    * This method support multiple documents upload By default creates new version of the
    * document if one exist with the same name If you don't want to create new version use
    * 'createVersion=false' to overwrite existing document user flag
    * 'createNewRevision=false', by new revision is created.
    * 
    * @param documentInfoDTO
    * @param processInstance
    * @return
    */
   Map<String, Object> createDocuments(List<DocumentContentRequestDTO> documentInfoDTO,
         ProcessInstance processInstance, boolean processAttachments);

   /**
    * internally calls createDocuments
    * 
    * @param documentInfoDTO
    * @param processInstance
    * @param processAttachments
    * @return
    */
   Map<String, Object> createDocument(DocumentContentRequestDTO documentInfoDTO, ProcessInstance processInstance,
         boolean processAttachments);

   /**
    * @param documentIds
    * @param processInstance
    * @return
    */
   void detachProcessAttachments(List<String> documentIds, ProcessInstance processInstance);

   /**
    * @param documentId
    * @throws DocumentManagementServiceException
    */
   void deleteDocument(String documentId) throws DocumentManagementServiceException;

   /**
    * @param documentId
    * @param documentInfoDTO
    */
   void updateDocument(String documentId, DocumentContentRequestDTO documentInfoDTO);


   // *******************************
   // Repository level operations
   // *******************************

   /**
    * @return list of RepositoryProviderDTOs
    */
   List<RepositoryProviderDTO> getRepositoryProviders();
   
   /**
    * @return list of RepositoryInstanceDTOs
    */
   List<RepositoryInstanceDTO> getRepositories();
   
   /**
    * @param repositoryId
    */
   void setDefualtRepository(String repositoryId);
   
   /**
    * @param repositoryAttributes
    */
   void bindRepository(Map<String, Object> repositoryAttributes);

   /**
    * @param repositoryId
    */
   void unbindRepository(String repositoryId);
}