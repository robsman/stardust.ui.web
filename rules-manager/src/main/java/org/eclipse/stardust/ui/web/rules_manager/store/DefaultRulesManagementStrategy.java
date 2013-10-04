package org.eclipse.stardust.ui.web.rules_manager.store;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.rules_manager.common.ServiceFactoryLocator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Shrikant.Gangal
 * 
 */
public class DefaultRulesManagementStrategy implements RulesManagementStrategy
{
   private final ServiceFactoryLocator serviceFactoryLocator;

   private ServiceFactory serviceFactory;

   private DocumentManagementService documentManagementService;

   public static final String RULES_DIR = "/rules/";

   /**
    * 
    */
   @Autowired
   public DefaultRulesManagementStrategy(ServiceFactoryLocator serviceFactoryLocator)
   {
      this.serviceFactoryLocator = serviceFactoryLocator;
   }

   @Override
   public Document getRuleSet(String ruleSetId)
   {
      return getDocumentManagementService().getDocument(RULES_DIR + ruleSetId + ".json");
   }
   
   @Override
   public void saveRuleSet(String ruleSetId, String content)
   {
      Document ruleSet = getDocumentManagementService().getDocument(
            RULES_DIR + ruleSetId + ".json");
      if (null != ruleSet)
      {
         getDocumentManagementService().updateDocument(ruleSet, content.getBytes(), "",
               true, "", null, false);
      } else {
         DocumentInfo docInfo = DmsUtils.createDocumentInfo(ruleSetId + ".json");
         docInfo.setContentType("text/plain");
         Document doc = getDocumentManagementService().createDocument(RULES_DIR, docInfo, content.getBytes(), null);
         getDocumentManagementService().versionDocument(doc.getId(), "", null);
      }
   }
   

   @Override
   public List<Document> getAllRuleSets()
   {
      List<Document> ruleSetDocuments = new LinkedList<Document>();
      @SuppressWarnings("unchecked")
      List<Document> candidateRuleSetDocuments = getDocumentManagementService().getFolder(
            RULES_DIR)
            .getDocuments();

      for (Document ruleSetDocument : candidateRuleSetDocuments)
      {
         ruleSetDocuments.add(ruleSetDocument);
      }

      return ruleSetDocuments;
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
