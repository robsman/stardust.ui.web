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

import java.util.List;

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
import org.eclipse.stardust.ui.web.rules_manager.store.DefaultRulesManagementStrategy;
import org.eclipse.stardust.ui.web.rules_manager.store.RulesManagementStrategy;

/**
 *
 * @author Marc.Gille
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

   /**
    * 
    * @return
    */
   public JsonObject getAllRuleSets(boolean reload) throws Exception
   {
      List<Document> drls = getRulesManagementStrategy().getAllRuleSets();
      JsonObject ruleSets = new JsonObject();
      for (Document doc : drls) {
         JsonObject ruleSet = new JsonParser().parse(new String(getDocumentManagementService().retrieveDocumentContent(doc.getId()))).getAsJsonObject();         
         ruleSets.add(ruleSet.get("uuid").getAsString(), ruleSet);
      }

      return ruleSets;
   }   

   /**
    * 
    * @return
    */
   public void saveRuleSets(String ruleSetsJson) throws Exception
   {
      if (null == ruleSetsJson)
      {
         return;
      }

      // Empty existing rule sets
      // as they need to be overwritten
      getRulesManagementStrategy().emptyRuleSets();

      // Save all rule sets.
      JsonArray ruleSets = new JsonParser().parse(ruleSetsJson).getAsJsonArray();
      for (JsonElement je : ruleSets)
      {
         String ruleSetId = je.getAsJsonObject().get("id").getAsString();
         getRulesManagementStrategy().saveRuleSet(ruleSetId, je.toString());
      }
   }
   
   /**
   *
   * @return
   */
  public RulesManagementStrategy getRulesManagementStrategy()
  {
     return (DefaultRulesManagementStrategy) context.getBean("rulesManagementStrategy");
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

   private ServiceFactory getServiceFactory()
   {
      // TODO Replace

      if (serviceFactory == null)
      {
         serviceFactory = serviceFactoryLocator.get();
      }

      return serviceFactory;
   }
}