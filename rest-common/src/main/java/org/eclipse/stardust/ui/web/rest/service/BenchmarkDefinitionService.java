/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkMetadataDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.DocumentUtils;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
@Component
public class BenchmarkDefinitionService
{

   public static final String BENCHMARK_DEFINITION_FOLDER = "/benchmark-definitions";

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   @Resource
   private DocumentUtils documentUtils;

   /**
    * 
    * @return
    */
   public List<BenchmarkDefinitionDTO> getBenchmarkDefinitions()
   {
      List<BenchmarkDefinitionDTO> list = new ArrayList<BenchmarkDefinitionDTO>();
      Folder folder = documentUtils.getFolder(BENCHMARK_DEFINITION_FOLDER);
      List<Document> documents = folder.getDocuments();
      for (Document doc : documents)
      {
         byte[] documentContents = documentUtils.getDocumentContents(doc.getId());
         String fileContents = new String(documentContents);
         BenchmarkDefinitionDTO benchmarkDto = new BenchmarkDefinitionDTO();
         BenchmarkMetadataDTO metadata = DTOBuilder.build(doc, BenchmarkMetadataDTO.class);
         benchmarkDto.metadata = metadata;
         if (null != documentContents)
         {
            benchmarkDto.contents = jsonIo.readJsonObject(fileContents);
         }
         list.add(benchmarkDto);
      }
      return list;
   }
}
