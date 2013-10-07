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
    * @param ruleSetId
    * @param content
    */
   void saveRuleSet(String ruleSetFileName, String content);
   
   /**
    * 
    */
   void emptyRuleSets();
}
