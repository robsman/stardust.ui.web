package org.eclipse.stardust.ui.web.rules_manager.store;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.Document;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Shrikant.Gangal
 * @author Yogesh.Manware
 *
 */
public interface RulesManagementStrategy
{
   /**
    * @param params
    */
   void initialize(Map<String, Object> params);

   /*
    * Design-time related methods
    */
   
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
   
   /*
    * Run-time related methods
    */
   
   /**
    * @return
    */
   JsonArray getAllRuntimeRuleSets();
   
   /**
    * @param ruleSetFileName
    * @return
    */
   JsonObject getRuntimeRuleSet(String ruleSetId);
  
   /**
	 * @param ruleSetId
	 */
   void publishRuleSet(String ruleSetId);

   /**
	 * @param ruleSetId
	 */
	void deleteRuntimeRuleSet(String ruleSetId);
}
