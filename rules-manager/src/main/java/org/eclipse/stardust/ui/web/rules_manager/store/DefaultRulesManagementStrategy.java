package org.eclipse.stardust.ui.web.rules_manager.store;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.rules_manager.common.ServiceFactoryLocator;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Shrikant.Gangal
 * @author Yogesh.Manware
 * 
 */
public class DefaultRulesManagementStrategy implements RulesManagementStrategy
{
   private final ServiceFactoryLocator serviceFactoryLocator;

   private ServiceFactory serviceFactory;

   private DocumentManagementService documentManagementService;

   public static final String RULES_DIR = "/rules/";
   
   public static final String RULES_DIR_NAME = "rules";

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.rules_manager.store.RulesManagementStrategy#initialize(java.util.Map)
    */
   public void initialize(Map<String, Object> params)
   {
      // NOP
   }
   
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
   public Document getRuleSetByName(String ruleSetFileName)
   {
      // TODO - assumption is that rules file name will be same as its id.
      return getDocumentManagementService().getDocument(RULES_DIR + ruleSetFileName);
   }
   
   @Override
   public Document createRuleSet(String rulesetName, byte[] content)
   {
      createRulesFolderIfAbsent();
      DocumentInfo docInfo = DmsUtils.createDocumentInfo(rulesetName);
      docInfo.setContentType("text/plain");
      Document doc = getDocumentManagementService().createDocument(RULES_DIR, docInfo, content, null);
      getDocumentManagementService().versionDocument(doc.getId(), "", null);
      
      return doc;
   }

   @Override
   public Document saveRuleSet(String rulesetDocId, byte[] content)
   {
      Document ruleSet = getDocumentManagementService().getDocument(rulesetDocId);
      return getDocumentManagementService().updateDocument(ruleSet, content,
            "", true, "", null, false);
   }

   @Override
   public void deleteRuleSet(String docId)
   {
      getDocumentManagementService().removeDocument(docId);
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