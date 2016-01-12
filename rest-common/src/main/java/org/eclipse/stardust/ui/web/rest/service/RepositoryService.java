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

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
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

   /**
    * @param folderId
    * @param levelOfDetail
    * @param createIfDoesNotExist
    * @return
    */
   FolderDTO getFolder(String folderId, int levelOfDetail, boolean createIfDoesNotExist);

   /**
    * @param documentInfoDTO
    * @param processInstance
    * @return
    */
   DocumentDTO createDocument(DocumentInfoDTO documentInfoDTO, ProcessInstance processInstance);

   /**
    * @param documentInfoDTO
    * @param processInstance
    * @return
    */
   Map<String, Object> createProcessAttachments(List<DocumentInfoDTO> documentInfoDTO, ProcessInstance processInstance);

   /**
    * @param documentIds
    * @param processInstance
    * @return
    * @throws ResourceNotFoundException
    */
   void detachProcessAttachments(List<String> documentIds, ProcessInstance processInstance)
         throws ResourceNotFoundException;

   /*
    * public DocumentDTO renameFolder(String participantQidIn);
    *//**
    * return parent folder
    * 
    * @param participantQidIn
    * @return
    */
   /*
    * public Map<String, List<DocumentDTO>> deleteFolder(String participantQidIn);
    * 
    * public DocumentDTO getDocument(String documentId);
    * 
    * 
    * 
    * public DocumentDTO deleteDocument(String participantQidIn);
    * 
    * public DocumentDTO updateDocument(String participantQidIn);
    * 
    * public DocumentDTO renameDocument(String participantQidIn);
    */

}