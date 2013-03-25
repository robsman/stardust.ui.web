/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * View Management
 * 
 * @author Marc.Gille
 */
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_extensionManager",
		"bpm-modeler/js/m_urlUtils",
		"bpm-modeler/js/m_communicationController", "rules-manager/js/RuleSet",
		"stardust-portal-shell/js/OrionFileManager" ], function(Utils,
		m_extensionManager, m_urlUtils, m_communicationController, RuleSet,
		OrionFileManager) {
	return {
		create : function(fileManager) {
			var ruleSetManager = new OrionRuleSetManager();

			ruleSetManager.initialize();

			return ruleSetManager;
		}
	};

	/**
	 * @class
	 * @name OrionRuleSetManager
	 */
	function OrionRuleSetManager() {
		/**
		 * 
		 */
		OrionRuleSetManager.prototype.toString = function() {
			return "Lightdust.OrionRuleSetManager";
		};

		/**
		 * 
		 */
		OrionRuleSetManager.prototype.initialize = function() {
			OrionFileManager.getInstance().addExtensionManager("drl", this);

			this.uuidToUriMap = {};
			this.uriToUuidMap = {};
			
			RuleSet.emptyRuleSets();
		};

		/**
		 * 
		 */
		OrionRuleSetManager.prototype.getViewInfo = function(uri) {
			var ruleSet = this.uriToUuidMap[uri];
			var queryString = "id=" + ruleSet.id + "&name="
			+ ruleSet.name
			+ "&uuid="
			+ ruleSet.uuid;
			
			return {viewId: "ruleSetView",
				queryString: queryString,
				objectId: ruleSet.uuid,
				perspectiveId: "rulesManagement"};
		};

		/**
		 * 
		 */
		OrionRuleSetManager.prototype.loadElements = function(uri, content) {
			return this.convertRuleSet(uri, content);
		};

		/**
		 * Converts DRL content into rule set
		 */
		OrionRuleSetManager.prototype.convertRuleSet = function(uri, content) {
			var deferred = jQuery.Deferred();

			var ruleSetManager = this;

			Utils.debug("Do AJAX call for parsing");

			jQuery.when(jQuery.ajax({
				type : "POST",
				url : m_communicationController.getEndpointUrl() + "/parseDrl",
				contentType : "text/plain",
				data : content
			})).done(function(ruleSet) {
				Utils.debug("Rule File(s) Uploaded");

				RuleSet.typeObject(ruleSet);

				Utils.debug("Typed Rule Set");
				Utils.debug(ruleSet);

				ruleSetManager.uuidToUriMap[ruleSet.uuid] = uri;
				ruleSetManager.uriToUuidMap[uri] = ruleSet;

				var name = OrionFileManager.getFileName(uri);

				ruleSet.id = name;
				ruleSet.name = name;

				RuleSet.typeObject(ruleSet);
				RuleSet.getRuleSets()[ruleSet.uuid] = ruleSet;

				deferred.resolve();
			}).fail(deferred.reject);

			return deferred.promise();
		};

		/**
		 * Saves all loaded rule sets
		 */
		OrionRuleSetManager.prototype.saveRuleSets = function() {
			var deferred = jQuery.Deferred();
			var uris = [];
			var contents = [];

			for ( var uuid in RuleSet.getRuleSets()) {
				var uri = this.uuidToUriMap[uuid];
				
				uris.push(uri);
				contents.push(RuleSet.getRuleSets()[uuid].generateDrl());
			}

			var n = 0;

			this.saveRuleSetsRecursively(n, uris, contents).done(
					deferred.resolve).fail(deferred.reject);

			return deferred.promise();
		};

		/**
		 * 
		 */
		OrionRuleSetManager.prototype.saveRuleSetsRecursively = function(n,
				uris, contents) {
			var deferred = jQuery.Deferred();

			Utils.debug("n = " + n);

			if (n == uris.length) {
				deferred.resolve();
			} else {
				Utils.debug("Up to Save content in Orion");

				OrionFileManager.getInstance()
						.saveFileContent(uris[n], contents[n]).then(
								deferred.resolve, deferred.reject);
			}

			return deferred.promise();
		};
	}
});