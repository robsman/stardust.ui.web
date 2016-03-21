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
package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.dto.DocumentVersionDTO;
import org.eclipse.stardust.ui.web.rest.dto.InfoDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.rest.util.DocumentSearchUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DocumentSearchService
{

   private static final Logger trace = LogManager.getLogger(DocumentSearchService.class);

   /**
    * @return
    */
   public String createDocumentSearchFilterAttributes()
   {
      return DocumentSearchUtils.getFilterAttributes();
   }

   /**
    * 
    * @param documentId
    * @return
    */
   public QueryResultDTO getProcessInstancesFromDocument(String documentId)
   {
      List<ProcessInstanceDTO> processList = DocumentSearchUtils.getProcessInstancesFromDocument(documentId);
      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = processList;
      resultDTO.totalCount = processList.size();
      return resultDTO;

   }

   /**
    * 
    * @param documentId
    * @return
    * @throws ResourceNotFoundException
    */
   public QueryResultDTO getDocumentVersions(String documentId) throws ResourceNotFoundException
   {
      List<DocumentVersionDTO> docVersions = DocumentSearchUtils.getDocumentVersions(documentId);
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
      List<SelectItemDTO> processDefns = DocumentSearchUtils.loadAvailableProcessDefinitions();
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
      return DocumentSearchUtils.attachDocuments(processOid, documentIds);
   }
}
