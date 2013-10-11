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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

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
    * 
    * @return
    */
   public void saveRuleSets(String ruleSetsJson)
   {
      if (null == ruleSetsJson)
      {
         return;
      }

      // Save all rule sets.
      JsonArray ruleSets = new JsonParser().parse(ruleSetsJson).getAsJsonArray();
      Set<String> updatedDocList = new HashSet<String>();
      for (JsonElement je : ruleSets)
      {
         String uuid = je.getAsJsonObject().get("uuid").getAsString();
         String documentId = ruleSetUUIDVsDocumentIdMap.get(uuid);

         if (null == documentId)
         {
            documentId = je.getAsJsonObject().get("id").getAsString() + ".json";
            trace.info("creating new ruleset with name:  " + documentId);
         }
         else
         {
            trace.info("updating ruleset with uuid: " + uuid);
         }
         Document doc = getRulesManagementStrategy().saveRuleSet(documentId,
               je.toString());

         updatedDocList.add(uuid);
         ruleSetUUIDVsDocumentIdMap.put(uuid, doc.getId());
      }
      
      // find deleted rules and delete the corresponding files
      for (Entry<String, String> rules : ruleSetUUIDVsDocumentIdMap.entrySet())
      {
         if ( !updatedDocList.contains(rules.getKey()))
         {
            getRulesManagementStrategy().deleteRuleSet(rules.getValue());
            // this will reset when you refresh view
            ruleSetUUIDVsDocumentIdMap.put(rules.getKey(), null);
            trace.info("deleting ruleset with uuid:  " + rules.getKey());
         }
      }
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
}