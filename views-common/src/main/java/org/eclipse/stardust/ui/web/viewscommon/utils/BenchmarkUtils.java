/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifacts;
import org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;
import org.eclipse.stardust.engine.core.spi.artifact.impl.BenchmarkDefinitionArtifactType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */
public class BenchmarkUtils
{
   private static final Logger trace = LogManager.getLogger(BenchmarkUtils.class);

   public static final String BENCHMARK_DEFINITION_FOLDER = "/benchmark-definitions";
   public static final String BENCHMARK_DEFINITION_ID = "id";
   public static final String BENCHMARK_DEFINITION_NAME = "name";

   private static final JsonParser jsonParser = new JsonParser();

   public static Map<String, String> getBenchmarkDefinitionsInfo() throws Exception
   {
      Map<String, String> benchmarkInfo = new TreeMap<String, String>();
      try
      {
         Folder folder = ServiceFactoryUtils.getDocumentManagementService().getFolder(BENCHMARK_DEFINITION_FOLDER);
         List<Document> documents = folder.getDocuments();
         for (Document doc : documents)
         {
            byte[] documentContents = ServiceFactoryUtils.getDocumentManagementService().retrieveDocumentContent(
                  doc.getId());
            String fileContents = new String(documentContents);
            JsonObject benchmarkJSON = readJsonObject(fileContents);
            String benchmarkId = benchmarkJSON.get(BENCHMARK_DEFINITION_ID).getAsString();
            String benchmarkName = benchmarkJSON.get(BENCHMARK_DEFINITION_NAME).getAsString();
            benchmarkInfo.put(benchmarkId, benchmarkName);
         }
      }
      catch (Exception e)
      {
         throw e;
      }
      return benchmarkInfo;
   }

   public static Map<String, String> getRuntimeBenchmarkDefinitionsInfo() throws Exception
   {
      Map<String, String> benchmarkInfo = new TreeMap<String, String>();
      try
      {
         DeployedRuntimeArtifactQuery query = DeployedRuntimeArtifactQuery.findActive(
               BenchmarkDefinitionArtifactType.TYPE_ID, new Date());

         DeployedRuntimeArtifacts artifacts = ServiceFactoryUtils.getQueryService().getRuntimeArtifacts(query);

         for (DeployedRuntimeArtifact artifact : artifacts)
         {
            RuntimeArtifact runtimeArtifact = ServiceFactoryUtils.getAdministrationService().getRuntimeArtifact(
                  artifact.getOid());
            String contents = new String(runtimeArtifact.getContent());
            JsonObject benchmarkJSON = readJsonObject(contents);
            String benchmarkId = benchmarkJSON.get(BENCHMARK_DEFINITION_ID).getAsString();
            String benchmarkName = benchmarkJSON.get(BENCHMARK_DEFINITION_NAME).getAsString();
            benchmarkInfo.put(benchmarkId, benchmarkName);
         }
      }
      catch (Exception e)
      {
         throw e;
      }
      return benchmarkInfo;
   }

   public static JsonObject readJsonObject(String jsonText) throws Exception
   {
      try
      {
         JsonElement parsedJson = jsonParser.parse(jsonText);
         if ((null != parsedJson) && parsedJson.isJsonObject())
         {
            return parsedJson.getAsJsonObject();
         }
         else
         {
            trace.warn("Expected a JSON object, but received something else.");
            throw new Exception("Expected a JSON object, but received something else.");
         }
      }
      catch (JsonParseException jpe)
      {
         trace.warn("Expected a JSON object, but received no valid JSON at all.", jpe);
         throw jpe;
      }
   }
}
