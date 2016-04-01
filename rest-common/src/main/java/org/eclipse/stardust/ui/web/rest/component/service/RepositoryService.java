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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.rest.dto.request.RepositorySearchRequestDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.RepositoryInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.RepositoryProviderDTO;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IResourceDataProvider;

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
    * @throws ResourceNotFoundException
    */
   FolderDTO getFolder(String folderId, int levelOfDetail, boolean createIfDoesNotExist)
         throws ResourceNotFoundException;

   /**
    * @param folderId
    * @param folderDataMap
    * @throws ResourceNotFoundException
    */
   FolderDTO createFolder(String folderId, Map<String, Object> folderDataMap) throws ResourceNotFoundException;

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
   IResourceDataProvider exportFolder(String resourceId);

   /**
    * @param folderId
    * @param uploadedFolder
    * @param merge
    * @return
    * @throws Exception
    */
   Map<String, Set<String>> importFolder(String folderId, List<DocumentContentRequestDTO> uploadedFolder, boolean merge)
         throws Exception;

   // *******************************
   // Document specific
   // *******************************
   /**
    * @param documentId
    * @return
    * @throws ResourceNotFoundException
    */
   DocumentDTO getDocument(String documentId) throws ResourceNotFoundException;

   /**
    * @param documentId
    * @param targetFolderPath
    * @param createVersion
    * @return
    * @throws ResourceNotFoundException
    */
   DocumentDTO copyDocument(String documentId, String targetFolderPath, boolean createVersion)
         throws ResourceNotFoundException;

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
    * @throws ResourceNotFoundException
    */
   String getDocumentContent(String documentId) throws ResourceNotFoundException;

   /**
    * @return
    * @throws ResourceNotFoundException
    */
   List<DocumentDTO> getDocumentHistory(String DocumentId) throws ResourceNotFoundException;

   /**
    * @param documentInfoDTO
    * @return
    */
   Map<String, Object> createDocuments(List<DocumentContentRequestDTO> documentInfoDTO);

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
   Map<String, Object> createProcessDocuments(List<DocumentContentRequestDTO> documentInfoDTO,
         ProcessInstance processInstance, boolean processAttachments);

   /**
    * This method support multiple documents upload, By default creates new version of the
    * document if one exist with the same name, If you don't want to create new version
    * use 'createVersion=false' to overwrite existing document user flag
    * 'createNewRevision=false', by new revision is created.
    * 
    * @param documentInfoDTO
    * @param activityInstance
    * @param processAttachments
    * @return
    */
   Map<String, Object> createActivityDocuments(List<DocumentContentRequestDTO> documentInfoDTO, ActivityInstance activityInstance);

   /**
    * internally calls createDocuments
    * 
    * @param documentInfoDTO
    * @param processInstance
    * @param processAttachments
    * @return
    */
   Map<String, Object> createProcessDocument(DocumentContentRequestDTO documentInfoDTO, ProcessInstance processInstance,
         boolean processAttachments);

   /**
    * @param documentIds
    * @param processInstance
    * @return
    * @throws ResourceNotFoundException
    */
   void detachProcessAttachments(List<String> documentIds, ProcessInstance processInstance)
         throws ResourceNotFoundException;

   /**
    * @param documentId
    * @throws DocumentManagementServiceException
    */
   void deleteDocument(String documentId) throws DocumentManagementServiceException;

   /**
    * @param documentId
    * @param documentInfoDTO
    * @return
    * @throws ResourceNotFoundException
    */
   DocumentDTO updateDocument(String documentId, DocumentContentRequestDTO documentInfoDTO)
         throws ResourceNotFoundException;

   /**
    * @param documentIds
    * @param targetPath
    * @return
    */
   List<DocumentDTO> moveDocument(List<String> documentIds, String targetPath);
   
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

   /**
    * @param repositorySearchRequestDTO
    * @return
    */
   Map<String, QueryResultDTO> searchResources(RepositorySearchRequestDTO repositorySearchRequestDTO);

}