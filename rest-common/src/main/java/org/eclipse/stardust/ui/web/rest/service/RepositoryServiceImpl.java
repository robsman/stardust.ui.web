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

import java.util.ArrayList;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.FolderDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentInfoDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
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
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   /**
    *
    */
   public FolderDTO getFolder(String folderId)
   {
      return getFolder(folderId, 1);
   }

   /**
    *
    */
   public FolderDTO getFolder(String folderId, int levelOfDetail)
   {
      // fetching of children information may be time consuming, may need to be
      // parameterized later
      Folder folder = getDMS().getFolder(folderId, levelOfDetail);

      if (folder == null)
      {
         throw new I18NException(restCommonClientMessages.getParamString("folder.notFound", folderId));
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

   private DocumentManagementService getDMS()
   {
      return serviceFactoryUtils.getDocumentManagementService();
   }

   /**
    *
    */
   @Override
   public DocumentDTO createDocument(DocumentInfoDTO documentInfoDTO)
   {
      if (documentInfoDTO.parentFolderPath == null)
      {
         throw new I18NException(restCommonClientMessages.getParamString("folder.notFound",
               "unknown"));
      }
      
      Folder parentFolder = DocumentMgmtUtility.createFolderIfNotExists(documentInfoDTO.parentFolderPath);
      
      String docName = RepositoryUtility.createDocumentName(parentFolder, documentInfoDTO.name, 0);

      // create document
      Document document = DocumentMgmtUtility.createDocument(parentFolder.getId(), docName, documentInfoDTO.content,
            documentInfoDTO.documentType, documentInfoDTO.contentType, documentInfoDTO.description,
            documentInfoDTO.comments, null, null);

      if (documentInfoDTO.processInstance != null)
      {
         DMSHelper.addAndSaveProcessAttachment(documentInfoDTO.processInstance, document);

      }

      return DocumentDTOBuilder.build(document);
   }
}