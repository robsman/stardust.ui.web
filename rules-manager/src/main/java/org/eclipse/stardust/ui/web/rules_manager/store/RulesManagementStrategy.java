package org.eclipse.stardust.ui.web.rules_manager.store;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;


/**
 * @author Shrikant.Gangal
 *
 */
public interface RulesManagementStrategy
{
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
}
