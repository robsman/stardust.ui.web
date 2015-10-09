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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
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
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkCategoryDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkMetadataDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.DocumentUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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
   public static final String BENCHMARK_CATEGORIES = "categories";

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   @Resource
   private DocumentUtils documentUtils;
   
   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   /**
    * 
    * @return
    */
   public List<BenchmarkDefinitionDTO> getBenchmarkDefinitions()
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
         String errorMsg = StringUtils.isNotEmpty(e.getLocalizedMessage())
               ? e.getLocalizedMessage()
               : restCommonClientMessages.getString("benchmark.design.error");
         throw new I18NException(errorMsg);
      }
      return list;
   }
   
   /**
    * 
    * @return
    * @throws Exception
    */
   public List<BenchmarkDefinitionDTO> getRuntimeBenchmarkDefinitions()
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
         String errorMsg = StringUtils.isNotEmpty(e.getLocalizedMessage())
               ? e.getLocalizedMessage()
               : restCommonClientMessages.getString("benchmark.runtime.error");
         throw new I18NException(errorMsg);
      }
      return list;
   }
   
   /**
    * 
    * @param runtimeOid
    * @return
    * @throws Exception
    */
   public BenchmarkDefinitionDTO getRuntimeBenchmarkDefinition(long runtimeOid)
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
         String errorMsg = StringUtils.isNotEmpty(e.getLocalizedMessage())
               ? e.getLocalizedMessage()
               : restCommonClientMessages.getString("benchmark.fetch.error");
         throw new I18NException(errorMsg);
      }
      return benchmarkDto;
   }
   
   public Set<BenchmarkCategoryDTO> getRuntimeBenchmarkCategories(String benchmarkOids)
   {
      try
      {
         Set<BenchmarkCategoryDTO> categoriesSet = CollectionUtils.newTreeSet();
         boolean noMatchingCategory = false;
         if (StringUtils.isNotEmpty(benchmarkOids))
         {
            String[] oids = benchmarkOids.split(",");
            for (int i = 0; i < oids.length; i++)
            {
               BenchmarkDefinitionDTO benchmarkDef = getRuntimeBenchmarkDefinition(Long.valueOf(oids[i]));

               if (benchmarkDef != null && benchmarkDef.content != null)
               {
                  JsonObject contents = benchmarkDef.content.getAsJsonObject();
                  JsonArray benchmarkCategories = GsonUtils.extractJsonArray(contents, BENCHMARK_CATEGORIES);
                  Type typeBenchmarkCategories = new TypeToken<Set<BenchmarkCategoryDTO>>()
                  {
                  }.getType();
                  if (null != benchmarkCategories)
                  {
                     Set<BenchmarkCategoryDTO> categories = new Gson().fromJson(benchmarkCategories.toString(),
                           typeBenchmarkCategories);
                     if (!CollectionUtils.isEmpty(categories))
                     {
                        if(i == 0)
                        {
                           // Store the categories of first benchmark in SET for comparision
                           categoriesSet.addAll(categories);
                        }
                        else
                        {
                           if(CollectionUtils.isEmpty(categoriesSet))
                           {
                              // First benchmark did not have any category
                              noMatchingCategory = true;
                              break;
                           }
                           if (categories.containsAll(categoriesSet))
                           {
                              if (categories.size() == categoriesSet.size())
                              {
                                 continue;
                              }
                           }
                           else
                           {
                              noMatchingCategory = true;
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
         if (noMatchingCategory || CollectionUtils.isEmpty(categoriesSet))
         {
            throw new I18NException(restCommonClientMessages.getParamString("benchmark.category.notFound",
                  benchmarkOids));
         }
         return categoriesSet;
      }
      catch (I18NException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new I18NException(restCommonClientMessages.getString("benchmark.category.error"));
      }
   }
   
   /**
    * 
    * @param runtimeOid
    * @throws Exception
    */
   public void deletePublishedBenchmarkDefinition(long runtimeOid)
   {
      try
      {
        documentUtils.deleteRuntimeArtifacts(runtimeOid);
      }
      catch (Exception e)
      {
         String errorMsg = StringUtils.isNotEmpty(e.getLocalizedMessage())
               ? e.getLocalizedMessage()
               : restCommonClientMessages.getParamString("benchmark.delete.error", String.valueOf(runtimeOid));
         throw new I18NException(errorMsg);
      }
   }
   
   /**
    * 
    * @param benchmarkId
    * @throws Exception
    */
   public void publishBenchmarkDefinition(String benchmarkId)
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
      catch (I18NException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         String errorMsg = StringUtils.isNotEmpty(e.getLocalizedMessage())
               ? e.getLocalizedMessage()
               : restCommonClientMessages.getParamString("benchmark.publish.error", benchmarkId);
         throw new I18NException(errorMsg);
      }
   }
   

   /**
    * 
    * @return
    */
   public BenchmarkDefinitionDTO createBenchmarkDefinition(JsonObject benchmarkJSON)
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
            String errorMsg = StringUtils.isNotEmpty(e.getLocalizedMessage())
                  ? e.getLocalizedMessage()
                  : restCommonClientMessages.getString("benchmark.save.error");
            throw new I18NException(errorMsg);
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
   public BenchmarkDefinitionDTO updateBenchmarkDefinition(String benchmarkId, JsonObject benchmarkJSON)
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
            String errorMsg = StringUtils.isNotEmpty(e.getLocalizedMessage())
                  ? e.getLocalizedMessage()
                  : restCommonClientMessages.getString("benchmark.save.error");
            throw new I18NException(errorMsg);
         }
      }
      return null;
   }
   
   /**
    * 
    * @param benchmarkId
    * @throws Exception
    */
   public void deleteBenchmarkDefinition(String benchmarkId)
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
         String errorMsg = StringUtils.isNotEmpty(e.getLocalizedMessage())
               ? e.getLocalizedMessage()
               : restCommonClientMessages.getParamString("benchmark.delete.error", benchmarkId);
         throw new I18NException(errorMsg);
      }
   }
   
   private Document getBenchmarkDefinitionContent(String benchmarkId)
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
         throw new I18NException(restCommonClientMessages.getString("benchmark.fetch.error"));
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
