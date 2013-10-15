package org.eclipse.stardust.ui.web.rules_manager.store;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;

/**
 * @author Shrikant.Gangal
 * @author Yogesh.Manware
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
    * @param ruleSetFileName
    * @return
    */
   Document getRuleSetByName(String ruleSetFileName);
   
  
   /**
    * @param rulesetdocId
    * @param content
    * @return
    */
   Document saveRuleSet(String rulesetdocId, String content);
   
   /**
    * @param rulesetName
    * @param content
    * @return
    */
   Document createRuleSet(String rulesetName, String content);
   
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
