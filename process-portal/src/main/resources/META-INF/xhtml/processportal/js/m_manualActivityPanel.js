/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Subodh.Godbole
 */

define(["processportal/js/codeGenerator"], function(codeGenerator){
	return {
		initialize : function() {
			var mAPanel = new ManualActivityPanel();
			mAPanel.initialize();
		}
	};
	
	/*
	 * 
	 */
	function ManualActivityPanel() {
		var restEndPoint = "http://localhost:7200/Demo720M4/services/rest/process-portal/manualActivity/";

		/*
		 * 
		 */
		ManualActivityPanel.prototype.initialize = function() {
			/* 
				1. Fetch Data Mappings
				2. Then Use m_markupGenerator.js to generate markup
				3. Insert Markup into current DOM
				4. Bootstrap Angular
			*/
	        var interactionId = window.location.search;
	        interactionId = interactionId.substring(interactionId.indexOf('interactionId') + 14);
	        interactionId = interactionId.indexOf('&') >= 0 ? interactionId.substring(0, interactionId.indexOf('&')) : interactionId;
	        restEndPoint = restEndPoint + interactionId;
	        console.log("Interaction Rest End Point: " + restEndPoint);
	        
	        fetchData(restEndPoint, "/dataMappings", {success: generateCode});

			bootstrapAngular();
			
			fetchData(restEndPoint, "/inData", {success: bindInData});

			runInAngularContext(function($scope){
				$scope.test1 = "Subodh";
				$scope.test2 = "Godbole";
			});
		};

		/*
		 * 
		 */
		function bootstrapAngular() {
			var moduleName = 'ManualActivityModule';
			var angularModule = angular.module(moduleName, []);
			angular.bootstrap(document, [moduleName]);
		};

		/*
		 * 
		 */
		function generateCode(json) {
			var html = codeGenerator.create().generate(json);
			document.getElementsByTagName("body")[0].innerHTML = html;
		};

		/*
		 * 
		 */
		function bindInData(data) {
			runInAngularContext(function($scope){
				for(var key in data) {
					$scope[key] = data[key];
				}
			});
		};

		/*
		 * 
		 */
		function fetchData(baseUrl, extension, callbacks) {
			var endpoint = baseUrl + extension;
	        console.log(endpoint);

			jQuery.ajax({
				type: 'GET',
				url: endpoint,
				async: false,
				success: callbacks.success,
				error: callbacks.failure ? callbacks.failure : function(errObj) {
					alert('Failed to load ' + extension + ' - ' + errObj.status + ":" + errObj.statusText);
				}
			});
		};

		/*
		 * 
		 */
		function runInAngularContext(func) {
			var scope = angular.element(document).scope();
			scope.$apply(func);
		};

		/*
		 * 
		 */
		function testDom() {
			var dom = ""; 
			dom += "<h3> Welcome to Manual Activity Rewrite</h3><br/>";
			dom += "First Name: <input ng-model=\"test1\"/><br/>";
			dom += "Last Name: <input ng-model=\"test2\"/><br/><br/>";
			dom += "First Name: {{test1}}<br/>";
			dom += "Last Name: {{test2}}<br/><br/><br/>";
			
			document.write(dom);			
		};
	};
});