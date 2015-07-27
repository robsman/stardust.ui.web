package org.eclipse.stardust.ui.web.rules_manager.store;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifacts;
import org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;

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
   DeployedRuntimeArtifacts getAllRuntimeRuleSets(DeployedRuntimeArtifactQuery query);
   
   /**
    * @param ruleSetFileName
    * @return
    */
   DeployedRuntimeArtifacts getRuntimeRuleSet(String ruleSetId);
  
   /**
	 * @param ruleSetId
	 */
   DeployedRuntimeArtifact publishRuleSet(long runtimeArtifactOid, RuntimeArtifact artifact);

   /**
	 * @param ruleSetId
	 */
	void deleteRuntimeRuleSet(String ruleSetId);
	
	/**
	    * 
	    * @param oid
	    * @return
	    */
	RuntimeArtifact getRuntimeArtifact(long oid);
	   
}
