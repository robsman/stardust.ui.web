package org.eclipse.stardust.ui.web.rules_manager.store;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;

/**
 * @author Shrikant.Gangal
 *
 */
public interface RulesManagementStrategy
{

   enum RulesUploadStatus
   {
      NEW_RULESET_CREATED, RULESET_ALREADY_EXISTS, NEW_RULESET_VERSION_CREATED
   };
   
   /**
    * @return
    */
   List<Document> getAllRuleSets();
   
   /**
    * @param ruleSetId
    * @return
    */
   Document getRuleSet(String ruleSetFileName);
   
   /**
    * @param rulesetNameOrdocId
    * @param content
    * @return document id
    */
   Document saveRuleSet(String rulesetNameOrdocId, String content);
   
   /**
    * 
    */
   void deleteRuleSet(String documentId);

   /**
    * @param fileName
    * @param fileContent
    * @param createNewVersion
    * @return
    */
   RulesUploadStatus uploadRulesFile(String fileName, byte[] fileContent,
         boolean createNewVersion);
}
