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
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
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
   public Document getRuleSet(String ruleSetFileName)
   {
      createRulesFolderIfAbsent();
      
      // TODO - assumption is that rules file name will be same as its id.
      return getDocumentManagementService().getDocument(RULES_DIR + ruleSetFileName);
   }

   @Override
   public Document saveRuleSet(String rulesetNameOrdocId, String content)
   {
      createRulesFolderIfAbsent();
      Document ruleSet = getDocumentManagementService().getDocument(rulesetNameOrdocId);

      Document doc;
      if (null != ruleSet)
      {
         doc = getDocumentManagementService().updateDocument(ruleSet, content.getBytes(),
               "", true, "", null, false);
      }
      else
      {
         DocumentInfo docInfo = DmsUtils.createDocumentInfo(rulesetNameOrdocId);
         docInfo.setContentType("text/plain");
         doc = getDocumentManagementService().createDocument(RULES_DIR, docInfo,
               content.getBytes(), null);
         getDocumentManagementService().versionDocument(doc.getId(), "", null);
      }

      return doc;
   }

   @Override
   public void deleteRuleSet(String docId)
   {
      getDocumentManagementService().removeDocument(docId);
   }   

   @Override
   public RulesUploadStatus uploadRulesFile(String fileName, byte[] fileContent,
         boolean createNewVersion)
   {
      if (DocumentMgmtUtility.isExistingResource("/" + RULES_DIR_NAME, fileName))
      {
         if (createNewVersion)
         {
            Document modelDocument = getDocumentManagementService().getDocument(
                  RULES_DIR + fileName);
            DocumentMgmtUtility.updateDocument(modelDocument, fileContent,
                  modelDocument.getDescription(), "");

            return RulesUploadStatus.NEW_RULESET_VERSION_CREATED;
         }

         return RulesUploadStatus.RULESET_ALREADY_EXISTS;
      }
      else
      {
         DocumentInfo docInfo = DmsUtils.createDocumentInfo(fileName);

         docInfo.setOwner(getServiceFactory().getUserService().getUser().getAccount());
         docInfo.setContentType(MimeTypesHelper.XML.getType());

         getDocumentManagementService().createDocument(RULES_DIR, docInfo, fileContent,
               null);

         return RulesUploadStatus.NEW_RULESET_CREATED;
      }
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
