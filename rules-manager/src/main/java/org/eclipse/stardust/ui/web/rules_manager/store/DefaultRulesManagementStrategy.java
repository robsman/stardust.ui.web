package org.eclipse.stardust.ui.web.rules_manager.store;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifacts;
import org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.rules_manager.common.ServiceFactoryLocator;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Shrikant.Gangal
 * @author Yogesh.Manware
 * 
 */
public class DefaultRulesManagementStrategy implements RulesManagementStrategy
{
   private final ServiceFactoryLocator serviceFactoryLocator;
   private static final String RULESARTIFACT_TYPE_ID = "drools-ruleset";

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
   
   /* Design-time related methods */

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
   
   /* Utility methods */

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

   /* Run-time related methods */

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.rules_manager.store.RulesManagementStrategy#getAllRuntimeRuleSets()
    */
   @Override
   public DeployedRuntimeArtifacts getAllRuntimeRuleSets(DeployedRuntimeArtifactQuery query)
   {
      DeployedRuntimeArtifacts runtimeArtifacts = getServiceFactory().getQueryService().getRuntimeArtifacts(query);
      return runtimeArtifacts;
   }

   /* (non-Javadoc)
	* @see org.eclipse.stardust.ui.web.rules_manager.store.RulesManagementStrategy#getRuntimeRuleSet(java.lang.String)
	*/
   @Override
   public DeployedRuntimeArtifacts getRuntimeRuleSet(String ruleSetId)
   {
      DeployedRuntimeArtifactQuery query = DeployedRuntimeArtifactQuery.findActive(ruleSetId,
            RULESARTIFACT_TYPE_ID, new Date());
      return getServiceFactory().getQueryService().getRuntimeArtifacts(query);
   }
	
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.rules_manager.store.RulesManagementStrategy#publishRuleSet(java.lang.String)
    */
   @Override
   public DeployedRuntimeArtifact publishRuleSet(long oid, RuntimeArtifact runtimeArtifact)
   {
      DeployedRuntimeArtifact runtimeArtifacts = null;
      if(oid > 0)
      {
         runtimeArtifacts = getServiceFactory().getAdministrationService().overwriteRuntimeArtifact(oid, runtimeArtifact);
      }
      else
      {
         runtimeArtifacts = getServiceFactory().getAdministrationService().deployRuntimeArtifact(runtimeArtifact);   
      }
      return runtimeArtifacts;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.rules_manager.store.RulesManagementStrategy#deleteRuntimeRuleSet(java.lang.String)
    */
   @Override
   public void deleteRuntimeRuleSet(String ruleSetId)
   {
	  DeployedRuntimeArtifacts runtimeArtifacts = getRuntimeRuleSet(ruleSetId);
	  if(null != runtimeArtifacts && runtimeArtifacts.size() > 0)
	  {
	     DeployedRuntimeArtifact runtimeArtifact = runtimeArtifacts.get(0);
	     getServiceFactory().getAdministrationService().deleteRuntimeArtifact(runtimeArtifact.getOid());
	  }
   }
   
   /**
    * 
    * @param oid
    * @return
    */
   public RuntimeArtifact getRuntimeArtifact(long oid)
   {
      return getServiceFactory().getAdministrationService().getRuntimeArtifact(oid);
   }
}