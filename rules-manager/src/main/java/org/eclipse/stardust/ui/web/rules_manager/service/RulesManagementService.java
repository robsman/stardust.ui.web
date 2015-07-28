/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rules_manager.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifacts;
import org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rules_manager.common.ServiceFactoryLocator;
import org.eclipse.stardust.ui.web.rules_manager.service.RulesManagementService.Response.OPERATION;
import org.eclipse.stardust.ui.web.rules_manager.store.RulesManagementStrategy;

/**
 * 
 * @author Marc.Gille
 * @author Yogesh.Manware
 * 
 */
public class RulesManagementService
{
   private static final Logger trace = LogManager.getLogger(RulesManagementService.class);
   private static final String RULESARTIFACT_TYPE_ID = "drools-ruleset";
   
   @Resource
   private ApplicationContext context;

   @Resource
   @Qualifier("default")
   private ServiceFactoryLocator serviceFactoryLocator;

   private ServiceFactory serviceFactory;

   private DocumentManagementService documentManagementService;

   private Map<String, String> ruleSetUUIDVsDocumentIdMap = new HashMap<String, String>();

   /**
    * 
    * @return
    */
   public JsonObject getAllRuleSets()
   {
      ruleSetUUIDVsDocumentIdMap.clear();

      List<Document> drls = getRulesManagementStrategy().getAllRuleSets();
      JsonObject ruleSets = new JsonObject();

      for (Document doc : drls)
      {
         JsonObject ruleSet = new JsonParser().parse(
               new String(getDocumentManagementService().retrieveDocumentContent(
                     doc.getId()))).getAsJsonObject();
         String uuid = ruleSet.get("uuid").getAsString();
         String id = ruleSet.get("id").getAsString();
         ruleSets.add(uuid, ruleSet);
         ruleSetUUIDVsDocumentIdMap.put(id, doc.getId());
      }

      return ruleSets;
   }

   /**
    * @param ruleSetsJson
    */
   public String saveRuleSets(String ruleSetsJson)
   {
      JsonArray ruleSets = new JsonParser().parse(ruleSetsJson).getAsJsonArray();

      // track errors
      List<RulesManagementService.Response> consolidatedResponse = new ArrayList<RulesManagementService.Response>();

      for (JsonElement je : ruleSets)
      {
         String id = je.getAsJsonObject().get("id").getAsString();
         String uuid = je.getAsJsonObject().get("uuid").getAsString();
         String documentId = ruleSetUUIDVsDocumentIdMap.get(id);
         OPERATION op = OPERATION.SAVE;
         try
         {
            if (je.getAsJsonObject().get("deleted") != null)
            {
               op = OPERATION.DELETE;
               deleteRules(id, documentId);
               consolidatedResponse.add(new Response(uuid, op, true, "deleted"));
            }
            else
            {
               op = OPERATION.SAVE;
               persistRules(je, id, documentId);
               consolidatedResponse.add(new Response(uuid, op, true, "saved"));
            }
           
         }
         catch (Exception e)
         {
            consolidatedResponse.add(new Response(uuid, op, false, e.getMessage()));
         }
      }

      // Error case
      if ( !consolidatedResponse.isEmpty())
      {
         return new Gson().toJson(consolidatedResponse);
      }

      return new Gson().toJson("saved");
   }

   /**
    * @param rulesetUUID
    * @return
    */
   public byte[] getRuleSet(String rulesetId)
   {
      return getDocumentManagementService().retrieveDocumentContent(
            ruleSetUUIDVsDocumentIdMap.get(rulesetId));
   }

   /**
    * @param je
    * @param id
    * @param documentId
    */
   private void persistRules(JsonElement je, String id, String documentId)
   {
      Document doc;
      if (null == documentId)
      {
         String rulesetFileName =  id + ".json";
         trace.info("creating new ruleset with id, name:  " + id + ", "
               + rulesetFileName);
         doc = getRulesManagementStrategy().createRuleSet(rulesetFileName,
               je.toString().getBytes());
      }
      else
      {
         trace.info("updating ruleset with uuid: " + id);
         doc = getRulesManagementStrategy().saveRuleSet(documentId,
               je.toString().getBytes());
      }

      ruleSetUUIDVsDocumentIdMap.put(id, doc.getId());
   }

   /**
    * @param id
    * @param documentId
    */
   private void deleteRules(String id, String documentId)
   {
      trace.info("deleting ruleset with id:  " + id);

      getRulesManagementStrategy().deleteRuleSet(documentId);

      ruleSetUUIDVsDocumentIdMap.remove(id);
   }

   /**
    * @return
    */
   public JsonArray getAllRuntimeRuleSets()
   {
      JsonArray ruleSets = new JsonArray();
      DeployedRuntimeArtifactQuery query = DeployedRuntimeArtifactQuery.findActive(
            RULESARTIFACT_TYPE_ID, new Date());
      
      DeployedRuntimeArtifacts artifacts = getRulesManagementStrategy().getAllRuntimeRuleSets(query);
      for(DeployedRuntimeArtifact artifact : artifacts)
      {
         JsonObject ruleSet = createRuleSetJson(artifact);
         ruleSets.add(ruleSet);
      }
      return ruleSets;
   }
   
   /**
    * 
    * @param artifact
    * @return
    */
   private JsonObject createRuleSetJson(DeployedRuntimeArtifact artifact)
   {
      RuntimeArtifact runtimeArtifact = getRulesManagementStrategy().getRuntimeArtifact(artifact.getOid());
      String contents = new String(runtimeArtifact.getContent());
      JsonObject ruleSet = new JsonParser().parse(contents).getAsJsonObject();
      ruleSet.addProperty("oid", artifact.getOid());
      ruleSet.addProperty("validFrom", artifact.getValidFrom().getTime());
      ruleSet.addProperty("artifactTypeId", artifact.getArtifactTypeId());
      return ruleSet;
   }
   
   /**
    * 
    * @param postedData
    * @return
    */
   public JsonObject publishRuleSet(String postedData)
   {
      JsonObject result = new JsonObject();
      JsonObject ruleSetJson = new JsonParser().parse(postedData).getAsJsonObject();
      String ruleSetId = GsonUtils.extractString(ruleSetJson, "ruleSetId");
      RuntimeArtifact artifact = null;
      DeployedRuntimeArtifact deployedRuntimeArtifact = null;
      Document document;

      String documentId = ruleSetUUIDVsDocumentIdMap.get(ruleSetId);
      if(StringUtils.isEmpty(documentId))
      {
         String ruleSetFileName = ruleSetId +  ".json";
         document = getRulesManagementStrategy().getRuleSetByName(ruleSetFileName);
         if(null == document)
         {
            return null;
         }
         ruleSetUUIDVsDocumentIdMap.put(ruleSetId, document.getId());
      }
      else
      {
         document = getDocumentManagementService().getDocument(documentId);
      }
      // retrieve contents
      byte[] contents = getDocumentManagementService().retrieveDocumentContent(document.getId());
      DeployedRuntimeArtifacts runtimeArtifact = getRulesManagementStrategy().getRuntimeRuleSet(ruleSetId);
      if(null != runtimeArtifact && runtimeArtifact.getSize() > 0)
      {
         DeployedRuntimeArtifact deployedArtifact = runtimeArtifact.get(0);
         artifact = getRulesManagementStrategy().getRuntimeArtifact(deployedArtifact.getOid());
         artifact.setContent(contents);
         deployedRuntimeArtifact = getRulesManagementStrategy().publishRuleSet(deployedArtifact.getOid(), artifact);
      }
      else
      {
         artifact = new RuntimeArtifact(RULESARTIFACT_TYPE_ID, ruleSetId + ".json", document.getName(), contents,
               new Date());   
         deployedRuntimeArtifact = getRulesManagementStrategy().publishRuleSet(0, artifact);
      }
      
      if(null != deployedRuntimeArtifact && deployedRuntimeArtifact.getOid() > 0)
      {
         result = createRuleSetJson(deployedRuntimeArtifact);
      }
      return result;
   }

  /**
   * 
   * @param oid
   * @return
   */
   public JsonObject getRuntimeRuleSet(String ruleSetId)
   {
      JsonObject ruleSetJson = new JsonObject();
      
      DeployedRuntimeArtifacts deployedArtifacts = getRulesManagementStrategy().getRuntimeRuleSet(ruleSetId);
      for(DeployedRuntimeArtifact artifact : deployedArtifacts)
      {
         ruleSetJson = createRuleSetJson(artifact);
         break;
      }
      
      return ruleSetJson;
   }

   /**
    * @param ruleSetId
    * @return
    */
   public JsonObject deleteRuntimeRuleSet(String ruleSetId)
   {
      JsonObject result = new JsonObject();
      
      getRulesManagementStrategy().deleteRuntimeRuleSet(ruleSetId);

      return result;
   }

   /**
    * 
    * @return
    */
   public RulesManagementStrategy getRulesManagementStrategy()
   {
      return (RulesManagementStrategy) context.getBean("rulesManagementStrategy");
   }

   /**
    * 
    * @return
    */
   private DocumentManagementService getDocumentManagementService()
   {
      if (documentManagementService == null)
      {
         documentManagementService = getServiceFactory().getDocumentManagementService();
      }

      return documentManagementService;
   }

   /**
    * @return
    */
   private ServiceFactory getServiceFactory()
   {
      if (serviceFactory == null)
      {
         serviceFactory = serviceFactoryLocator.get();
      }

      return serviceFactory;
   }

   /**
    * 
    * @author Yogesh.Manware
    * 
    */
   public static class Response
   {
      public static enum OPERATION
      {
         SAVE, DELETE
      }

      String uuid;

      OPERATION operation;

      boolean success;

      String message;

      public Response(String uuid, OPERATION Operation, boolean success, String message)
      {
         super();
         this.uuid = uuid;
         this.operation = Operation;
         this.message = message;
         this.success = success; 
      }

      public String getUuid()
      {
         return uuid;
      }

      public OPERATION getOperation()
      {
         return operation;
      }

      public boolean isSuccess()
      {
         return success;
      }

      public String getMessage()
      {
         return message;
      }
   }
}