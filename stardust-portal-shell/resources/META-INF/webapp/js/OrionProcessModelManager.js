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
		"bpm-modeler/js/m_communicationController", 
		"stardust-portal-shell/js/OrionFileManager" ], function(Utils,
		m_extensionManager, m_urlUtils, m_communicationController, 
		OrionFileManager) {
	return {
		create : function() {
			var processModelManager = new OrionProcessModelManager();

			processModelManager.initialize();

			return processModelManager;
		}
	};

	/**
	 * @class
	 * @name OrionProcessModelManager
	 */
	function OrionProcessModelManager() {
		/**
		 * 
		 */
		OrionProcessModelManager.prototype.toString = function() {
			return "Lightdust.OrionProcessModelManager";
		};

		/**
		 * 
		 */
		OrionProcessModelManager.prototype.initialize = function() {
			OrionFileManager.getInstance().addExtensionManager("xpdl", this);
			OrionFileManager.getInstance().addExtensionManager("bpmn", this);
			
			this.uriToModelDescriptorMap = {};
		};

		/**
		 * 
		 */
		OrionProcessModelManager.prototype.getViewInfo = function(uri) {
			var model = this.uriToModelDescriptorMap[uri];
			var queryString = "modelId=" + model.id + "&modelName="
			+ model.name
			+ "&uuid="
			+ model.uuid;
			
			return {viewId: "modelView",
				queryString: queryString,
				objectId: model.uuid,
				perspectiveId: "processModeling"};
		};

		/**
		 * 
		 */
		OrionProcessModelManager.prototype.loadElements = function(uri, content) {
			return this.uploadModelFileContent(uri, content);
		};

		/**
		 * Converts DRL content into rule set
		 */
		OrionProcessModelManager.prototype.uploadModelFileContent = function(uri, content) {
			var deferred = jQuery.Deferred();

			Utils.debug("Upload XPDL or BPMN Model File " + uri);
			
			var name = OrionFileManager.getFileName(uri);
			var extension = OrionFileManager.getFileExtension(uri);
			
			// TODO Receive back info about the loaded model
			
			var processModelManager = this;
				
			jQuery.ajax({
				type : "POST",
				url : m_communicationController
						.getEndpointUrl()
						+ "/models/upload/" + name + "." + extension,
				contentType : "text/plain",
				data : content,
				success : function(modelDescriptor) {
					Utils.debug("Model File(s) Uploaded");
					Utils.debug(modelDescriptor);
					
					processModelManager.uriToModelDescriptorMap[uri] = modelDescriptor;
					
					deferred.resolve();
				},
				error : function() {
					deferred.reject();
				}
			});

			return deferred.promise();
		};

		/**
		 * Saves all loaded models
		 */
		OrionProcessModelManager.prototype.saveProcessModels = function() {
			var deferred = jQuery.Deferred();
			var uris = [];
			var contents = [];

//			for ( var uuid in RuleSet.getRuleSets()) {
//				var uri = this.uuidToUriMap[uuid];
//				
//				// TODO Retrieve model content from server
//				
//				uris.push(uri);
//				contents.push(RuleSet.getRuleSets()[uuid].generateDrl());
//			}

			var n = 0;

			this.saveProcessModelsRecursively(n, uris, contents).done(
					deferred.resolve).fail(deferred.reject);

			return deferred.promise();
		};

		/**
		 * TODO Make generic function of FileManager
		 */
		OrionProcessModelManager.prototype.saveProcessModelsRecursively = function(n,
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