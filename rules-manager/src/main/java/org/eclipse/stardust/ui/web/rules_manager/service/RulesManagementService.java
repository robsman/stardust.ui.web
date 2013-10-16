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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.rules_manager.common.ServiceFactoryLocator;
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
         ruleSets.add(uuid, ruleSet);
         ruleSetUUIDVsDocumentIdMap.put(uuid, doc.getId());
      }

      return ruleSets;
   }

   /**
    * @param ruleSetsJson
    */
   public String saveRuleSets(String ruleSetsJson)
   {
      JsonArray ruleSets = new JsonParser().parse(ruleSetsJson).getAsJsonArray();
      
      //track errors
      List<RulesManagementService.RulesError> ruleErrorList = new ArrayList<RulesManagementService.RulesError>();
      
      for (JsonElement je : ruleSets)
      {
         String uuid = je.getAsJsonObject().get("uuid").getAsString();
         String documentId = ruleSetUUIDVsDocumentIdMap.get(uuid);

         try
         {
            if (je.getAsJsonObject().get("deleted") != null)
            {
               deleteRules(uuid, documentId);
            }
            else
            {
               persistRules(je, uuid, documentId);
            }
         }
         catch (Exception e)
         {
            ruleErrorList.add(new RulesError(uuid, e.getMessage()));
         }
      }
      
      //Error case
      if ( !ruleErrorList.isEmpty())
      {
         return new Gson().toJson(ruleErrorList);
      }
      
      return new Gson().toJson("saved");
   }

   /**
    * @param rulesetUUID
    * @return
    */
   public Map<String, String> getRuleSet(String rulesetUUID)
   {
      // TODO - not good
      // map used to avoid 2 different calls to get file content and name
      Map<String, String> ruleSetNameAndContent = new HashMap<String, String>();
      Document ruleSetFile = getDocumentManagementService().getDocument(
            ruleSetUUIDVsDocumentIdMap.get(rulesetUUID));
      ruleSetNameAndContent.put("fileName", ruleSetFile.getName());
      ruleSetNameAndContent.put("content", new String(
            getDocumentManagementService().retrieveDocumentContent(ruleSetFile.getId())));

      return ruleSetNameAndContent;
   }
   
   /**
    * @param je
    * @param uuid
    * @param documentId
    */
   private void persistRules(JsonElement je, String uuid, String documentId)
   {
      Document doc;
      if (null == documentId)
      {
         String rulesetFileName = je.getAsJsonObject().get("name").getAsString() + uuid + ".json";
         trace.info("creating new ruleset with uuid, name:  " + uuid + ", " + rulesetFileName);
         doc = getRulesManagementStrategy().createRuleSet(rulesetFileName, je.toString().getBytes());
      }
      else
      {
         trace.info("updating ruleset with uuid: " + uuid);
         doc = getRulesManagementStrategy().saveRuleSet(documentId, je.toString().getBytes());
      }

      ruleSetUUIDVsDocumentIdMap.put(uuid, doc.getId());
   }

   /**
    * @param uuid
    * @param documentId
    */
   private void deleteRules(String uuid, String documentId)
   {
      trace.info("deleting ruleset with uuid:  " + uuid);

      getRulesManagementStrategy().deleteRuleSet(documentId);

      ruleSetUUIDVsDocumentIdMap.remove(uuid);
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
   
   public static class RulesError
   {
      String uuid;
      String error;

      public RulesError(String uuid, String error)
      {
         super();
         this.uuid = uuid;
         this.error = error;
      }

      public String getUuid()
      {
         return uuid;
      }

      public String getError()
      {
         return error;
      }
   }
}