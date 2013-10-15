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
   Document saveRuleSet(String rulesetdocId, byte[] content);
   
   /**
    * @param rulesetName
    * @param content
    * @return
    */
   Document createRuleSet(String rulesetName, byte[] content);
   
   /**
    * 
    */
   void deleteRuleSet(String documentId);

}
