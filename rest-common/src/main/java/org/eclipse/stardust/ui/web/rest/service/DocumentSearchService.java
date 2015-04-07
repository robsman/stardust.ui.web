/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 *
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchCriteriaDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentVersionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InfoDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.DocumentSearchUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DocumentSearchService
{

   private static final Logger trace = LogManager.getLogger(DocumentSearchService.class);

   @Resource
   private DocumentSearchUtils documentSearchUtils;

   /**
    * @return
    */
   public String createDocumentSearchFilterAttributes()
   {
      return documentSearchUtils.getFilterAttributes();
   }

   public QueryResultDTO performSearch(Options options, DocumentSearchCriteriaDTO documentSearchAttributes)
   {
      QueryResult<Document> docs = documentSearchUtils.performSearch(options, documentSearchAttributes);
      return buildDocumentSearchResult(docs);

   }

   /**
    * 
    * @param documentId
    * @return
    */
   public QueryResultDTO getProcessInstancesFromDocument(String documentId)
   {
      List<ProcessInstanceDTO> processList = documentSearchUtils.getProcessInstancesFromDocument(documentId);
      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = processList;
      resultDTO.totalCount = processList.size();
      return resultDTO;

   }

   /**
    * 
    * @param docs
    * @return
    */
   private QueryResultDTO buildDocumentSearchResult(QueryResult<Document> docs)
   {
      List<DocumentSearchResultDTO> list = new ArrayList<DocumentSearchResultDTO>();

      for (Document doc : docs)
      {
         DocumentSearchResultDTO docSearchResultDTO = new DocumentSearchResultDTO(doc);
         list.add(docSearchResultDTO);
      }

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = list;
      resultDTO.totalCount = docs.getTotalCount();

      return resultDTO;

   }

   /**
    * 
    * @param documentOwner
    * @return
    */
   public UserDTO getUserDetails(String documentOwner)
   {
      User user = UserUtils.getUser(documentOwner);
      UserDTO userDTO = DTOBuilder.build(user, UserDTO.class);
      userDTO.name = UserUtils.getUserDisplayLabel(user);
      userDTO.userImageURI = MyPicturePreferenceUtils.getUsersImageURI(user);
      return userDTO;
   }

   /**
    * 
    * @param documentId
    * @return
    * @throws ResourceNotFoundException
    */
   public QueryResultDTO getDocumentVersions(String documentId) throws ResourceNotFoundException
   {
      List<DocumentVersionDTO> docVersions = documentSearchUtils.getDocumentVersions(documentId);
      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = docVersions;
      resultDTO.totalCount = docVersions.size();
      return resultDTO;
   }

   /**
    * 
    * @return
    */
   public QueryResultDTO loadAvailableProcessDefinitions()
   {
      List<SelectItemDTO> processDefns = documentSearchUtils.loadAvailableProcessDefinitions();
      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = processDefns;
      resultDTO.totalCount = processDefns.size();
      return resultDTO;
   }

   /**
    * 
    * @param processOid
    * @param documentIds
    * @return
    * @throws ResourceNotFoundException
    */
   public InfoDTO attachDocumentsToProcess(long processOid, List<String> documentIds) throws ResourceNotFoundException
   {
      return documentSearchUtils.attachDocuments(processOid, documentIds);
   }
}
