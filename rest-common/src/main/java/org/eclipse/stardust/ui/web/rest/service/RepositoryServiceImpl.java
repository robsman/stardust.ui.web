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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.FolderDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentInfoDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
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

   @Override
   public DocumentDTO createDocument(DocumentInfoDTO documentInfoDTO)
   {
      if (documentInfoDTO.parentFolderPath == null)
      {
         throw new I18NException(restCommonClientMessages.getParamString("folder.notFound", "unknown"));
      }

      Folder parentFolder = DocumentMgmtUtility.createFolderIfNotExists(documentInfoDTO.parentFolderPath);

      Document document = DocumentMgmtUtility.getDocument(parentFolder.getPath(), documentInfoDTO.name);
      if (document != null)
      {
         throw new I18NException(restCommonClientMessages.getParamString("document.existError", documentInfoDTO.name));
      }

      // create document
      document = DocumentMgmtUtility.createDocument(parentFolder.getId(), documentInfoDTO.name,
            documentInfoDTO.content, documentInfoDTO.documentType, documentInfoDTO.contentType,
            documentInfoDTO.description, documentInfoDTO.comments, null, null);

      return DocumentDTOBuilder.build(document);
   }

   // To support multiple process attachments upload
   @Override
   public Map<String, Object> createProcessAttachments(ProcessInstance processInstance,
         List<DocumentInfoDTO> documentInfoDTOs)
   {
      Map<String, Object> result = new HashMap<String, Object>();
      Map<String, String> failures = new HashMap<String, String>();
      result.put("failures", failures);
      List<DocumentDTO> documentDTOs = new ArrayList<DocumentDTO>();
      result.put("documents", documentDTOs);

      String parentFolderPath = DocumentMgmtUtility.getProcessAttachmentsFolderPath(processInstance);
      Folder parentFolder = DocumentMgmtUtility.createFolderIfNotExists(parentFolderPath);

      List<Document> documents = new ArrayList<Document>();

      for (DocumentInfoDTO documentInfoDTO : documentInfoDTOs)
      {
         Document document = DocumentMgmtUtility.getDocument(parentFolder.getPath(), documentInfoDTO.name);
         if (document != null)
         {
            failures.put(documentInfoDTO.name,
                  restCommonClientMessages.getParamString("document.existError", documentInfoDTO.name));
            continue;
         }

         // create document
         document = DocumentMgmtUtility.createDocument(parentFolder.getId(), documentInfoDTO.name, documentInfoDTO.content,
               documentInfoDTO.documentType, documentInfoDTO.contentType, documentInfoDTO.description,
               documentInfoDTO.comments, null, null);

         documents.add(document);
         documentDTOs.add(DocumentDTOBuilder.build(document));
      }

      if (processInstance != null && CollectionUtils.isNotEmpty(documents))
      {
         DMSHelper.addAndSaveProcessAttachments(processInstance, documents);
      }
      return result;
   }
}