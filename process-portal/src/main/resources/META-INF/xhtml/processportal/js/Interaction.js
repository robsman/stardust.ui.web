/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author subodh.godbole
 */

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.portal) {
	bpm.portal = {};
}

if (!window.bpm.portal.Interaction) {
	bpm.portal.Interaction = function Interaction() {

		var MODE_MODELER = "modeler";

		var params = extractParams(window.location.search.substring(1));
		var interactionUri = params['ippInteractionUri'];
		var mode = params['ippMode'];

		/*
		 * 
		 */
		Interaction.prototype.isModelerMode = function () {
			return mode == MODE_MODELER;
		};
		
		/*
		 * 
		 */
		Interaction.prototype.getInteractionUri = function () {
			return interactionUri;
		};
		
		/*
		 * 
		 */
		Interaction.prototype.fetchData = function(dataMapping, callbacks, controller) {
			var endpoint = "/inData";
			var result;
			getData(interactionUri, endpoint, {
				success : function(data) {
					var json;
					if (mode == MODE_MODELER) {
						json = data;
					} else {
						json = convertXMLToJSON(data);
						if (controller != undefined) {
							processJson(json, controller);
						}
					}

					log(json);
					result = json;

					if (callbacks && callbacks.success) {
						callbacks.success(json);
					}
				}
			});

			return result;
		};

		/*
		 * 
		 */
		Interaction.prototype.saveData = function(dataMapping, data, callbacks) {
			var retData = {};
			var endpoint = "/outData";

			if (mode == MODE_MODELER) {
				var dataToPost = JSON.stringify(data);
				log("PUTTING ->" + dataToPost);
				putData(interactionUri, endpoint, dataToPost, "application/json", {
					failure : function(e) {
						if (retData.errors == undefined) {
							retData.errors = {};
						}
						retData.errors['all'] = e;
					}
				});
			} else {
				var envelope, xml, type;
				var converter = new X2JS();
				converter.escapeMode(true);

				for (var name in data) {
					if (typeof data[name] !== "object") {
						xml = data[name];
						type = "text/plain";
					} else {
						envelope = {};
						envelope[name] = data[name];
						xml = converter.json2xml_str(envelope);
						type = "application/xml";
					}
					
					log("PUTTING -> " + name + " = " + xml);
					putData(interactionUri, endpoint + "/" + name, xml, type, {
						failure: function(e) {
							if (retData.errors == undefined) {
								retData.errors = {};
							}
							retData.errors[name] = e;
						}
					});
				}
			}

			if (retData.errors == undefined) {
				if (callbacks) {
					callbacks.success();
				}
				return true;
			} else {
				if (callbacks) {
					callbacks.failure(retData);
				}
				return retData;
			}
		};

		/*
		 * 
		 */
		Interaction.prototype.closeActivityPanel = function(command) {
			if (mode != MODE_MODELER) {
				try {
					if (parent.InfinityBpm.ProcessPortal) {
						parent.InfinityBpm.ProcessPortal.confirmCloseCommandFromExternalWebApp(command);
						return;
					}
				} catch (e) {
					// Ignore
				}
	
				if (parent.postMessage) {
					parent.postMessage(command, "*");
				}
			}
		};

		/*
		 * 
		 */
		function extractParams(str) {
			var params = {}, regex = /([^&=]+)=([^&]*)/g, match;
			if (str != "" && str.length > 0) {
				while (match = regex.exec(str)) {
					params[decodeURIComponent(match[1])] = decodeURIComponent(match[2]);
				}
			}
			return params;
		}

		/*
		 * 
		 */
		function convertXMLToJSON(xml) {
			var converter = new X2JS();
			var json = converter.xml2json(xml);

			var data = {};
			if (json == undefined) {
				data = xml;
			} else if (json.inDataValues && json.inDataValues.parameter) {
				for ( var n = 0; n < json.inDataValues.parameter_asArray.length; ++n) {
					var inData = json.inDataValues.parameter_asArray[n];
					if (inData.primitive) {
						data[inData.name] = inData.primitive;
					} else {
						// Determine structure name
						for (var structureName in inData.xml) {
							if (structureName.indexOf("_asArray") != -1) {
								structureName = structureName.substring(0, structureName.indexOf("_asArray"));
								data[inData.name] = inData.xml[structureName];
								break;
							}
						}
					}
				}
			}

			return data;
		}

		/*
		 * Copy of getBinding() from processportal/js/GenericController.js
		 */
		function getBinding(path, data, ignoreXPath, upToLastLevel) {
			var ret;

			var xPath = path.fullXPath;
			if(ignoreXPath && xPath.indexOf(ignoreXPath) == 0) {
				xPath = xPath.substring(ignoreXPath.length);
			}

			var parts = xPath.substring(1).split("/");
			var lastPart = parts[parts.length - 1];
			var currentBinding = data;
			for(var i = 0; i < parts.length - 1; i++) {
				currentBinding = currentBinding[parts[i]];
				if (currentBinding == undefined) {
					break;
				}
			}

			if (upToLastLevel) {
				if (currentBinding) {
					ret = currentBinding[lastPart];
				}
			} else {
				ret = {};
				if (currentBinding) {
					ret.binding = currentBinding;
					ret.lastPart = lastPart;
				}
			}

			return ret;
		}

		/*
		 * 
		 */
		function processJson(data, controller) {
			// If controller is available get Data Mappings & bindings from there, for backward compatibility
			// Else assume this variable contains data mappings itself
			var controllerAvailable = controller.dataMappings != undefined;

			var arrPaths = controllerAvailable ? controller.dataMappings : controller;
			for (var key in arrPaths) {
				var bindingInfo = controllerAvailable ? controller.getBinding(
						arrPaths[key], data) : getBinding(arrPaths[key], data);
				var binding = bindingInfo.binding[bindingInfo.lastPart];
				if (binding != undefined) {
					if (!isPrimitiveTransferObject(binding)) {
						if (isValidTransferObject(binding)) {
							processTransferObject(binding, arrPaths[key]);
						} else {
							bindingInfo.binding[bindingInfo.lastPart] = ""; // This is not valid Object, then it must be empty primitive. Make it undefined or null instead?
						}
					} else {
						processPrimitiveTransferObject(bindingInfo.binding, bindingInfo.lastPart, arrPaths[key]);
					}
				}
			}

			// Clean up unwanted values in data at 1st level
			for (var k in data) {
				var found = false;
				for (var i in arrPaths) {
					if (arrPaths[i].id == k) {
						found = true;
						break;
					}
				}

				if (!found) {
					delete data[k];
				}
			}
		}

		/*
		 * Blank out empty primitives, so that on UI it won't show [object object]
		 * Also X2JS returns Object instead of Array if it contains only one item, this also fixs this case 
		 * 
		 */
		function processTransferObject(obj, path) {
			for(var key in obj) {
				var currentPath = getChildPath(path, key);
				if (currentPath) {
					// Adjust Array which was exposed as Object. Convert it to Array
					if (currentPath.isList && Object.prototype.toString.call(obj[key]) !== "[object Array]") {
						var tmpObj = obj[key];
						obj[key] = [];
						obj[key].push(tmpObj);
					}

					if (currentPath.isList) {
						for(var j in obj[key]) {
							if (currentPath.children) { // List of Structures
								processTransferObject(obj[key][j], currentPath);
							} else { // List of Primitives
								processPrimitiveTransferObject(obj[key], j, currentPath);
							}
						}
					} else if (!currentPath.isPrimitive) {
						processTransferObject(obj[key], currentPath);
					} else if (currentPath.isPrimitive) {
						processPrimitiveTransferObject(obj, key, currentPath);
					}
				} else {
					delete obj[key];
				}
			} 
		}

		/*
		 * 
		 */
		function processPrimitiveTransferObject(obj, key, path) {
			if (isPrimitiveTransferObject(obj[key])) {
				obj[key] = convertPrimitiveAsPerDataType(obj[key].toString(), path);
			} else {
				obj[key] = ""; // This is not valid Primitive, then it must be empty primitive
			}
		}

		/*
		 * 
		 */
		function convertPrimitiveAsPerDataType(value, path) {
			var val = value;

			try {
				if (path.typeName == "integer" || path.typeName == "int" || path.typeName == "java.lang.Integer" || 
						path.typeName == "short" || path.typeName == "java.lang.Short" ||
						path.typeName == "long" || path.typeName == "java.lang.Long" ||
						path.typeName == "byte") {
					val = parseInt(value);
				} else if (path.typeName == "float" || path.typeName == "java.lang.Float" ||
						path.typeName == "double" || path.typeName == "decimal" || path.typeName == "java.lang.Double") {
					val = parseFloat(value);
				} else if (path.typeName == "boolean" || path.typeName == "java.lang.Boolean") {
					val = value == "true" ? true : false;
				}
			} catch (e) {
				log(e);
			}

			return val;
		}
		
		/*
		 * 
		 */
		function getChildPath(path, childId) {
			if (path.children != undefined && path.children.length > 0) {
				for(var i in path.children){
					if(path.children[i].id == childId) {
						return path.children[i];
					}
				}
			}
			return null;
		}
		
		/*
		 * 
		 */
		function isValidTransferObjectAttribute(attr) {
			return (attr.indexOf("_") != 0 && !endsWith(attr, "_asArray"));
		}

		/*
		 * 
		 */
		function endsWith(str, subStr) {
			if (str.length >= subStr.length) {
				return str.lastIndexOf(subStr) == str.length - subStr.length;
			}

			return false;
		}

		/*
		 * 
		 */
		function isPrimitiveTransferObject(obj) {
			if (typeof obj != "object" || typeof obj == "string") {
				return true;
			} else {
				for(var key in obj) {
					if (key == "toString" && typeof obj[key] == "function") {
						return true;
					}
				}
			}
			return false;
		}

		/*
		 * 
		 */
		function isValidTransferObject(obj) {
			for(var key in obj) {
				if (isValidTransferObjectAttribute(key)) {
					return true;
				}
			}
			return false;
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
				contentType: 'application/xml',
				success: callbacks.success,
				error: callbacks.failure ? callbacks.failure : function(errObj) {
					log('Failed to get ' + extension + ' - ' + errObj.status + ":" + errObj.statusText);
				}
			});
		};

		/*
		 * 
		 */
		function putData(baseUrl, extension, data, contentType, callbacks) {
			var endpoint = baseUrl + extension;
	        log(endpoint);

			jQuery.ajax({
				type: 'PUT',
				url: endpoint,
				async: false,
				contentType: contentType,
				data: "" + data,
				success: callbacks.success ? callbacks.success : null,
				error: callbacks.failure ? callbacks.failure : function(errObj) {
					log('Failed to post ' + extension + ' - ' + errObj.status + ":" + errObj.statusText);
				}
			});
		};

		/*
		 * 
		 */
		function log(msg) {
			if (console) {
				console.log(msg);
			}
		};
	};
}