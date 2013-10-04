package org.eclipse.stardust.ui.web.rules_manager.store;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;


/**
 * @author Shrikant.Gangal
 *
 */
public interface RulesManagementStrategy
{
   List<Document> getAllRuleSets();
   
   Document getRuleSet(String ruleSetId);
   
   void saveRuleSet(String ruleSetId, String content);
}
