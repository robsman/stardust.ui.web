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
 * TODO: Convert and move most of the things to Angular directive
 * 
 * @author Subodh.Godbole
 * 
 */

define(function(require){
    var isMobileClient =  (window.location.search.indexOf('isMobileClient') >= 0);
    var codeGenSrc = ["processportal/js/codeGenerator"];
    if (isMobileClient) {
    	// Load jQM only for mobile clients
    	require(["jquery-mobile"]);
    	codeGenSrc = ["processportal/js/codeGeneratorMobile"];
    	loadCSSFile("../../plugins/mobile-workflow/public/css/jquery.mobile/jquery.mobile-1.4.0.css");
    	loadCSSFile("./css/manual-activity-mobile.css");
    } else {
    	loadCSSFile("./css/manual-activity.css");
    }
    var codeGenerator;
	
	return {
		initialize : function() {
		    require(codeGenSrc, function(c) {
		    	codeGenerator = c;
				var mAPanel = new ManualActivityPanel();
				mAPanel.initialize();
		    });
		}
	};
	
	/*
	 * 
	 */
	function ManualActivityPanel() {
		var REST_END_POINT = "/services/rest/process-portal/manualActivity/";
		
		var interactionEndpoint;

		var dataMappings;
		var bindings;
		var clientDateFormat = "dd-mm-yy";
		var configuration;

		/*
		 * 
		 */
		ManualActivityPanel.prototype.initialize = function() {
	        var urlPrefix = window.location.href;
	        urlPrefix = urlPrefix.substring(0, urlPrefix.indexOf("/plugins"));

			var interactionId = window.location.search;
	        interactionId = interactionId.substring(interactionId.indexOf('interactionId') + 14);
	        interactionId = interactionId.indexOf('&') >= 0 ? interactionId.substring(0, interactionId.indexOf('&')) : interactionId;
	        
	        interactionEndpoint = urlPrefix + REST_END_POINT + interactionId;
	        log("Interaction Rest End Point: " + interactionEndpoint);
	        
	        InfinityBPMI18N.initPluginProps({
				pluginName : "manualActivity",
				singleEndPoint : interactionEndpoint + "/i18n"
			});
	        
	        getData(interactionEndpoint, "/dateFormats", {success: receiveDateFormats});
	        
	        getData(interactionEndpoint, "/configuration", {success: receiveConfiguration});

	        getData(interactionEndpoint, "/dataMappings", {success: generateMarkup});
		
			var ctrl = new bpm.portal.GenericController();
			ctrl.initialize(dataMappings, bindings, clientDateFormat, i18nLabelProvider(), {
					fetchData : function(dataMapping, callbacks) {
						if (dataMapping == null) {
							getData(interactionEndpoint, "/inData", callbacks);
						} else {
							getData(interactionEndpoint, "/inData/" + dataMapping, callbacks);
						}
					},
					
					saveData : function(dataMapping, data, callbacks) {
						if (dataMapping == null) {
							postData(interactionEndpoint, "/outData", data, callbacks);
						} else {
							postData(interactionEndpoint, "/outData/" + dataMapping, data, callbacks);
						}
					},
					closeActivityPanel : function(commandId) {
						window.parent.postMessage(commandId, "*");
					}
				}, {
					getMarkup : function(path, prefix, i18nProvider, ignoreParentXPath, formName) {
						var paths = [];
						paths.push(path);
						return codeGenerator.create().generate(paths, prefix, i18nProvider, ignoreParentXPath, formName);
					}
				});

			runInAngularContext(function($scope){
				$scope.initState = {};
				$scope.initState.success = true;

				jQuery.extend($scope, ctrl);
				inheritMethods($scope, ctrl);
			});
		};

		/*
		 * 
		 */
		function generateMarkup(json) {
			dataMappings = json;
			var BINDING_PREFIX = "dm";

			var data = codeGenerator.create(configuration).generate(json, BINDING_PREFIX, i18nLabelProvider());
			var htm = data.html;
			if (isMobileClient) {
				htm += "<div class='ui-body ui-body-a ui-corner-all'>";
				htm += "<button ng-click='completeActivity()' class='ui-btn ui-shadow ui-corner-all' style='width: 100%'>Complete</button><br>";
				htm += "<button ng-click='suspendActivity()' class='ui-btn ui-shadow ui-corner-all' style='width: 100%'>Suspend</button><br>";
				htm += "<button ng-click='suspendActivity(true)' class='ui-btn ui-shadow ui-corner-all' style='width: 100%'>Suspend And Save</button>";
				htm += "</div>";
			}
			document.getElementsByTagName("body")[0].innerHTML = htm;
			bindings = data.binding;
		};

		/*
		 * 
		 */
		function receiveDateFormats(json) {
			// TODO - check
			// for input[type="date"] wire date format needs to be "yy-mm-dd" for chrome
			clientDateFormat = isMobileClient ? "yy-mm-dd" : json.dateFormat;
			if (clientDateFormat) {
				clientDateFormat = clientDateFormat.toLowerCase();
			}
		}

		/*
		 * 
		 */
		function receiveConfiguration(json) {
			configuration = json;
		}

		/*
		 *
		 */
		function i18nLabelProvider() {
			return {
				getLabel : getLabel,
				getEnumerationLabels : getEnumerationLabels,
				getDescription : getDescription
			};
			
			/*
			 * val: path object or string
			 */
			function getLabel(val, defaultValue) {
				var key = val;

				if (("string" != typeof (val))) {
					var parts = val.fullXPath.substring(1).split("/");
					if (parts.length == 1) { // First Level means Data/DataMapping
						key = "Data." + val.id + ".Name";
					} else { // More than 1 level means XSD
						var prefix = "";
						for(var i = 0; i < parts.length; i++) {
							prefix += parts[i] + ".";
						}
						key = "StructuredType." + prefix + "Name";
					}
				}

				var value = InfinityBPMI18N.manualActivity.getProperty(key, defaultValue);
				return value;
			}

			/*
			 * 
			 */
			function getEnumerationLabels(path) {
				var labels;
				if (path.typeName == "PROCESS_PRIORITY") {
					labels = {};
					for(var i in path.enumValues) {
						var optKey = "" + path.enumValues[i];
						var i18nKey = "common.process.priority.options." + optKey;
						labels[optKey] = InfinityBPMI18N.manualActivity.getProperty(i18nKey);
					}
				}
				return labels;
			}

			/*
			 * 
			 */
			function getDescription(path) {
				// TODO
			}
		}

		/*
		 * 
		 */
		function getData(baseUrl, extension, callbacks) {
			var endpoint = baseUrl + extension;
	        log(endpoint);

			jQuery.ajax({
				type: 'GET',
				url: endpoint,
				async: false,
				success: callbacks.success,
				error: callbacks.failure ? callbacks.failure : function(errObj) {
					log('Failed to get ' + extension + ' - ' + errObj.status + ":" + errObj.statusText);
				}
			});
		};

		/*
		 * 
		 */
		function postData(baseUrl, extension, data, callbacks) {
			var endpoint = baseUrl + extension;
	        log(endpoint);

			jQuery.ajax({
				type: 'POST',
				url: endpoint,
				async: false,
				contentType: 'application/json',
				data: JSON.stringify(data),
				success: callbacks.success ? callbacks.success : null,
				error: callbacks.failure ? callbacks.failure : function(errObj) {
					log('Failed to post ' + extension + ' - ' + errObj.status + ":" + errObj.statusText);
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
		function inheritMethods (childObject, parentObject) {
			for (var member in parentObject) {
				if (parentObject[member] instanceof Function) {
					childObject[member] = parentObject[member];
				}
			}
		};

		/*
		 * 
		 */
		function log(msg) {
			if (console) {
				console.log(msg);
			}
		}
	};
	
	function loadCSSFile(filename) {
		  var fileref=document.createElement("link")
		  fileref.setAttribute("rel", "stylesheet")
		  fileref.setAttribute("type", "text/css")
		  fileref.setAttribute("href", filename);
		  document.getElementsByTagName("head")[0].appendChild(fileref);
		}
});