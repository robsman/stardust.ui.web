package org.eclipse.stardust.ui.web.rules_manager.store;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
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
   
   public static final String RULES_DIR_NAME = "rules";

   /**
    * 
    */
   @Autowired
   public DefaultRulesManagementStrategy(ServiceFactoryLocator serviceFactoryLocator)
   {
      this.serviceFactoryLocator = serviceFactoryLocator;
   }

   @Override
   public List<Document> getAllRuleSets()
   {
      createRulesFolderIfAbsent();

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

   @Override
   public Document getRuleSet(String ruleSetId)
   {
      createRulesFolderIfAbsent();
      
      // TODO - assumption is that rules file name will be same as its id.
      return getDocumentManagementService().getDocument(RULES_DIR + ruleSetId + ".json");
   }

   @Override
   public void saveRuleSet(String ruleSetId, String content)
   {
      createRulesFolderIfAbsent();

      // TODO - check!
      // Here the assumption is that the rules file name is same as the ruleset id
      // and will never change
      // still need additional support for uploaded files that may have file name that
      // is different from id.
      DocumentInfo docInfo = DmsUtils.createDocumentInfo(ruleSetId + ".json");
      docInfo.setContentType("text/plain");
      Document doc = getDocumentManagementService().createDocument(RULES_DIR, docInfo,
            content.getBytes(), null);
      getDocumentManagementService().versionDocument(doc.getId(), "", null);
   }

   @Override
   public void emptyRuleSets()
   {
      // Deletes the rules folder with all its contents and recreates an empty one.
      Folder folder = getDocumentManagementService().getFolder(RULES_DIR);
      if (null != folder)
      {
         getDocumentManagementService().removeFolder(folder.getId(), true);
      }

      getDocumentManagementService().createFolder("/",
            DmsUtils.createFolderInfo(RULES_DIR_NAME));
   }
   
   /**
    * 
    */
   private void createRulesFolderIfAbsent()
   {
      Folder folder = getDocumentManagementService().getFolder(RULES_DIR);
      if (null == folder)
      {
         getDocumentManagementService().createFolder("/",
               DmsUtils.createFolderInfo(RULES_DIR_NAME));
      }
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
