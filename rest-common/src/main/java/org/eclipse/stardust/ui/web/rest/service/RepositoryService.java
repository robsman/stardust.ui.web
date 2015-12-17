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
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentInfoDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
public interface RepositoryService
{

   // Folder specific
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

   // Document specific
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
   Map<String, Object> createDocuments(List<DocumentInfoDTO> documentInfoDTO, ProcessInstance processInstance,
         boolean processAttachments);

   /**
    * internally calls createDocuments
    * 
    * @param documentInfoDTO
    * @param processInstance
    * @param processAttachments
    * @return
    */
   Map<String, Object> createDocument(DocumentInfoDTO documentInfoDTO, ProcessInstance processInstance,
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
    * @throws ResourceNotFoundException
    * @throws DocumentManagementServiceException
    */
   void deleteDocument(String documentId) throws DocumentManagementServiceException, ResourceNotFoundException;

   /**
    * @param documentId
    * @param documentInfoDTO
    * @throws DocumentManagementServiceException
    * @throws UnsupportedEncodingException
    */
   void updateDocument(String documentId, DocumentInfoDTO documentInfoDTO) throws DocumentManagementServiceException,
         UnsupportedEncodingException;
}