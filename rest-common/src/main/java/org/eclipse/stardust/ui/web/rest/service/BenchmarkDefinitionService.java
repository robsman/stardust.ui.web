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
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifacts;
import org.eclipse.stardust.engine.api.runtime.ArtifactType;
import org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;
import org.eclipse.stardust.engine.core.spi.artifact.impl.BenchmarkDefinitionArtifactType;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkMetadataDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.DocumentUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
@Component
public class BenchmarkDefinitionService
{

   public static final String BENCHMARK_DEFINITION_FOLDER = "/benchmark-definitions";
   public static final String BENCHMARK_DEFINITION = "benchmark-definition";
   private static final String UTF_ENCODING = "utf-8";

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   @Resource
   private DocumentUtils documentUtils;

   /**
    * 
    * @return
    */
   public List<BenchmarkDefinitionDTO> getBenchmarkDefinitions() throws Exception
   {
      List<BenchmarkDefinitionDTO> list = new ArrayList<BenchmarkDefinitionDTO>();
      try
      {
    	 //Leverage this call to create the folder if it doesn't exist but as this
    	 //method does not return folder contents (it returns the top level folder with no contents) 
    	 //we still need to explicitly call getFolder to have access to our benchmark definitions.
         DocumentMgmtUtility.createFolderIfNotExists(BENCHMARK_DEFINITION_FOLDER);
         Folder folder = DocumentMgmtUtility.getFolder(BENCHMARK_DEFINITION_FOLDER);
         
         List<Document> documents = folder.getDocuments();
         for (Document doc : documents)
         {
            byte[] documentContents = documentUtils.getDocumentContents(doc.getId());
            String fileContents = new String(documentContents);
            JsonObject benchmarkJSON = jsonIo.readJsonObject(fileContents);
            BenchmarkDefinitionDTO benchmarkDto = buildBenchmarkDTO(doc, null, benchmarkJSON);
            list.add(benchmarkDto);
         }
      }
      catch (Exception e)
      {
         throw e;
      }
      return list;
   }
   
   /**
    * 
    * @return
    * @throws Exception
    */
   public List<BenchmarkDefinitionDTO> getRuntimeBenchmarkDefinitions() throws Exception
   {
      List<BenchmarkDefinitionDTO> list = new ArrayList<BenchmarkDefinitionDTO>();
      try
      {
         DeployedRuntimeArtifactQuery query = DeployedRuntimeArtifactQuery.findActive(
               BenchmarkDefinitionArtifactType.TYPE_ID, new Date());
         DeployedRuntimeArtifacts artifacts = documentUtils.getDeployedBenchmarkDefinitions(query);
         for (DeployedRuntimeArtifact artifact : artifacts)
         {
            BenchmarkDefinitionDTO dto = new BenchmarkDefinitionDTO();
            BenchmarkMetadataDTO metadata = new BenchmarkMetadataDTO();
            metadata.lastModifiedDate = artifact.getValidFrom().getTime();
            metadata.runtimeOid = artifact.getOid();
            RuntimeArtifact runtimeArtifact = documentUtils.getRuntimeArtifacts(artifact.getOid());
            String contents = new String(runtimeArtifact.getContent());
            dto.metadata = metadata;
            dto.content = jsonIo.readJsonObject(contents);
            list.add(dto);
         }
      }
      catch (Exception e)
      {
         throw e;
      }
      return list;
   }
   
   /**
    * 
    * @param runtimeOid
    * @return
    * @throws Exception
    */
   public BenchmarkDefinitionDTO getRuntimeBenchmarkDefinition(long runtimeOid) throws Exception
   {
      BenchmarkDefinitionDTO benchmarkDto = new BenchmarkDefinitionDTO();
      try
      {
         RuntimeArtifact artifact = documentUtils.getRuntimeArtifacts(runtimeOid);
         BenchmarkMetadataDTO metadata = new BenchmarkMetadataDTO();
         metadata.lastModifiedDate = artifact.getValidFrom().getTime();
         metadata.runtimeOid = runtimeOid;
         String contents = new String(artifact.getContent());
         benchmarkDto.metadata = metadata;
         benchmarkDto.content = jsonIo.readJsonObject(contents);
      }
      catch (Exception e)
      {
         throw e;
      }
      return benchmarkDto;
   }

   /**
    * 
    * @param benchmarkId
    * @throws Exception
    */
   public void publishBenchmarkDefinition(String benchmarkId) throws Exception
   {
      try
      {
         RuntimeArtifact artifact = null;
         DeployedRuntimeArtifact deployedArtifacts = null;
         Document document = getBenchmarkDefinitionContent(benchmarkId);
         String benchmarkName = document.getName();
         byte[] content = documentUtils.getDocumentContents(document.getId());
         // Check if runtime benchmark Def exist for current Benchmark Definition
         ArtifactType artifactType = new BenchmarkDefinitionArtifactType();
         DeployedRuntimeArtifactQuery query = DeployedRuntimeArtifactQuery.findActive(benchmarkId,
               BenchmarkDefinitionArtifactType.TYPE_ID, new Date());
         DeployedRuntimeArtifacts runtimeArtifacts = documentUtils.getDeployedBenchmarkDefinitions(query);
         // overwrite runtime artifact
         if (null != runtimeArtifacts && runtimeArtifacts.getTotalCount() > 0)
         {
            DeployedRuntimeArtifact runtimeArtifact = runtimeArtifacts.get(0);
            artifact = documentUtils.getRuntimeArtifacts(runtimeArtifacts.get(0).getOid());
            artifact.setContent(content);
            deployedArtifacts = documentUtils.deployBenchmarkDocument(runtimeArtifact.getOid(), artifact);
         }
         else
         {
            artifact = new RuntimeArtifact(artifactType.getId(), benchmarkId, benchmarkName, content,
                  new java.util.Date());
            deployedArtifacts = documentUtils.deployBenchmarkDocument(0, artifact);
         }
      }
      catch (Exception e)
      {
         throw e;
      }
   }

   /**
    * 
    * @return
    */
   public BenchmarkDefinitionDTO createBenchmarkDefinition(JsonObject benchmarkJSON) throws Exception
   {
      if (null != benchmarkJSON)
      {
         try
         {
            String benchmarkId = GsonUtils.extractString(benchmarkJSON, "id");
            Folder benchmarkFolder = documentUtils.getFolder(BENCHMARK_DEFINITION_FOLDER);
            String fileName = BENCHMARK_DEFINITION + "-" + benchmarkId + ".json";
            byte[] byteContents;
            byteContents = benchmarkJSON.toString().getBytes(UTF_ENCODING);

            Document doc = documentUtils.createDocument(benchmarkFolder.getId(), fileName, null, byteContents);
            BenchmarkDefinitionDTO benchmarkDto = buildBenchmarkDTO(doc, null, benchmarkJSON);
            return benchmarkDto;
         }
         catch (Exception e)
         {
            throw e;
         }
      }
      return null;
   }

   /**
    * 
    * @param benchmarkId
    * @param benchmarkJSON
    * @return
    */
   public BenchmarkDefinitionDTO updateBenchmarkDefinition(String benchmarkId, JsonObject benchmarkJSON) throws Exception
   {
      if (null != benchmarkJSON)
      {
         try
         {
            Document doc = getBenchmarkDefinitionContent(benchmarkId);
            if (null != doc)
            {
               byte[] contents = benchmarkJSON.toString().getBytes(UTF_ENCODING);
               Document updatedDocument = documentUtils.updateDocument(doc, contents, "", false);
               BenchmarkDefinitionDTO benchmarkDto = buildBenchmarkDTO(updatedDocument, null, benchmarkJSON);
               return benchmarkDto;
            }
         }
         catch (Exception e)
         {
           throw e;
         }
      }
      return null;
   }
   
   /**
    * 
    * @param benchmarkId
    * @throws Exception
    */
   public void deleteBenchmarkDefinition(String benchmarkId) throws Exception
   {
      try
      {
         Document doc = getBenchmarkDefinitionContent(benchmarkId);
         if (null != doc)
         {
            DocumentMgmtUtility.deleteDocumentWithVersions(doc);
         }
      }
      catch (Exception e)
      {
         throw e;
      }
   }
   
   private Document getBenchmarkDefinitionContent(String benchmarkId) throws Exception
   {
      Folder folder = documentUtils.getFolder(BENCHMARK_DEFINITION_FOLDER);
      Document doc = null;
      if (null != folder)
      {
         List<Document> documents = folder.getDocuments();
         String fileName = BENCHMARK_DEFINITION + "-" + benchmarkId + ".json";
         for (Document document : documents)
         {
            if (document.getName().equalsIgnoreCase(fileName))
            {
               doc =  document;
               break;
            }
         }
      }
      if(null == doc)
      {
         throw new ResourceNotFoundException("Benchmark definition not found");
      }
      return doc;

   }
   /**
    * 
    * @param doc
    * @param benchmarkDto
    * @param benchmarkJSON
    * @return
    */
   private BenchmarkDefinitionDTO buildBenchmarkDTO(Document doc, BenchmarkDefinitionDTO benchmarkDto,
         JsonObject benchmarkJSON)
   {
      if (null == benchmarkDto)
      {
         benchmarkDto = new BenchmarkDefinitionDTO();
      }
      benchmarkDto.metadata = DTOBuilder.build(doc, BenchmarkMetadataDTO.class);
      benchmarkDto.content = benchmarkJSON;
      return benchmarkDto;
   }
}
