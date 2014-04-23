/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_urlUtils",
				"bpm-modeler/js/m_constants", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_modelElement", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_elementConfiguration",
				"bpm-modeler/js/m_i18nUtils",
				"processportal/js/codeGenerator" ],
		function(m_utils, m_urlUtils, m_constants, m_model, m_modelElement,
				m_command, m_commandsController, m_elementConfiguration,
				m_i18nUtils, codeGenerator) {
			var markup = "";
			var indentLevel = 0;

			var THE_NEW_WAY = true;

			return {
				create : function(options) {
					var generator = new MarkupGenerator();

					generator.initialize(options);

					return generator;
				}
			};

			/**
			 * Generates JS and HTML for a UI to modify data of structures
			 * defined as In and Out Access Points.
			 */
			function MarkupGenerator() {
				/**
				 *
				 */
				MarkupGenerator.prototype.initialize = function(options) {
					this.options = options;

					if (!this.options) {
						this.options = {};
					}

					// Set defaults

					if (this.options.numberOfPrimitivesPerColumns === undefined) {
						this.options.numberOfPrimitivesPerColumns = 4;
					}

					if (!this.options.generateCompleteButton === undefined) {
						this.options.generateCompleteButton = true;
					}

					if (!this.options.generateSuspendButton === undefined) {
						this.options.generateSuspendButton = true;
					}

					if (!this.options.generateSaveButton === undefined) {
						this.options.generateSaveButton = true;
					}

					if (!this.options.generateAbortButton === undefined) {
						this.options.generateAbortButton = true;
					}

					if (!this.options.generateQaPassButton === undefined) {
						this.options.generateQaPassButton = false;
					}

					if (!this.options.generateQaFailButton === undefined) {
						this.options.generateQaFailButton = false;
					}

					if (!this.options.tabsForFirstLevel === undefined) {
						this.options.tabsForFirstLevel = false;
					}

					if (!this.options.tabsForFirstLevelTables === undefined) {
						this.options.tabsForFirstLevelTables = false;
					}
				}

				/**
				 *
				 */
				MarkupGenerator.prototype.generateMarkup = function(
						parameterDefinitions) {
					markup = "";

					writeTag("<html>");
					indentUp();
					this.generateHeader(parameterDefinitions);
					this.generateBody(parameterDefinitions);
					indentDown();
					writeTag("</html>");

					return markup;
				}

				/**
				 *
				 */
				MarkupGenerator.prototype.generateHeader = function(
						parameterDefinitions) {
					writeTag("<head>");
					indentUp();
					this.generateJavaScript(parameterDefinitions);
					indentDown();
					writeTag("</head>");
				};

				/**
				 *
				 */
				MarkupGenerator.prototype.generateJavaScript = function(
						parameterDefinitions) {
					writeTag("<script>");
					indentUp();

					writeTag("function injectJS(head, src) {");
					writeTag("    var script = document.createElement('script');");
					writeTag("    script.src = src;");
					writeTag("    head.appendChild(script);");
					writeTag("}");

					writeTag("function injectCSS(head, src) {");
					writeTag("    var link = document.createElement('link');");
					writeTag("    link.href = src;");
					writeTag("    link.rel = 'stylesheet';");
					writeTag("    link.type = 'text/css';");
					writeTag("    head.appendChild(link);");
					writeTag("}");

					writeTag("function addStyle(head, contents) {");
					writeTag("    var style = document.createElement('style');");
					writeTag("    style.type = 'text/css';");
					writeTag("    if (style.styleSheet) {");
					writeTag("        style.styleSheet.cssText = contents;");
					writeTag("    } else {");
					writeTag("        style.appendChild(document.createTextNode(contents));");
					writeTag("    }");
					writeTag("    head.appendChild(style);");
					writeTag("}");

					writeTag("function initialize() {");
					writeTag("   require.config({waitSeconds: 0, baseUrl : baseUrl + '/plugins/',");
					writeTag("   paths : {");
					writeTag("         'jquery' : [ 'views-common/js/libs/jquery/jquery-1.7.2.min',");
					writeTag("         '//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],");
					writeTag("         'jquery-ui' : ['views-common/js/libs/jquery/plugins/jquery-ui-1.10.2.min',");
					writeTag("      '//ajax.googleapis.com/ajax/libs/jqueryui/1.8.19/jquery-ui.min' ],");
					writeTag("         'json' : [ 'views-common/js/libs/json/json2',");
					writeTag("         '//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],");
					writeTag("      'jquery.url' : [");
					writeTag("            'views-common/js/libs/jquery/plugins/jquery.url',");
					writeTag("         'https://raw.github.com/allmarkedup/jQuery-URL-Parser/4f5254f2519111ad7037d398b2efa61d3cda58d4/jquery.url' ],");
					writeTag("      'angularjs' : [");
					writeTag("            'views-common/js/libs/angular/angular-1.0.2',");
					writeTag("         '//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min' ],");
					writeTag(" 'xml2json' : [ 'processportal/xml2js' ],");
					writeTag("    'bpm.portal.Interaction' : [ 'processportal/js/Interaction' ],");
					writeTag("    'bpm.portal.GenericAngularApp' : [ 'processportal/js/GenericAngularApp' ],");
					writeTag("    'bpm.portal.GenericController' : [ 'processportal/js/GenericController' ],");
					writeTag("    'bpm.portal.UIMashupController' : [ 'processportal/js/UIMashupController' ]");
					writeTag("   },");
					writeTag("   shim : {");
					writeTag("      'jquery-ui' : [ 'jquery' ],");
					writeTag("      'jquery.url' : [ 'jquery' ],");
					writeTag("      'angularjs' : {");
					writeTag("         require : 'jquery',");
					writeTag("         exports : 'angular'");
					writeTag("      },");
					writeTag("      'bpm.portal.Interaction' : [ 'jquery' ],");
					writeTag("         'bpm.portal.GenericAngular' : [ 'jquery' ],");
					writeTag("         'bpm.portal.GenericController' : [ 'jquery' ],");
					writeTag("         'bpm.portal.UIMashupController' : [ 'jquery' ]");
					writeTag("   }");
					writeTag("});");
					writeTag("require([ 'require', 'jquery', 'jquery-ui', 'json', 'jquery.url', 'angularjs',");
					writeTag("      'xml2json', 'bpm.portal.Interaction', 'bpm.portal.GenericAngularApp', 'bpm.portal.GenericController', 'bpm.portal.UIMashupController' ], function(require, jquery, jqueryUi,");
					writeTag("   json, jqueryUrl, angularjs, xml2json, stardustPortalInteraction, stardustGenericController) {");
					writeTag("      jQuery(document).ready(");
					writeTag("      function() {");
					writeTag("            // Move the controller from Div to HTML");
					writeTag("            var ctrlDiv = jQuery(\"div[ng-controller]='ManualActivityCtrl'\");");
					writeTag("            ctrlDiv.removeAttr('ng-controller');");
					writeTag("            jQuery('html').attr('ng-controller', 'ManualActivityCtrl');");
					emptyLine();
					writeTag("            uiMashupController = new bpm.portal.UIMashupController();");
					writeTag("            uiMashupController.init();");
					writeTag("      });");
					writeTag("   });");
					writeTag("}");
					
					writeTag("function waitToLoad() {");
					writeTag("    if (!window['require']) {");
					writeTag("        window.setTimeout(function() {");
					writeTag("            waitToLoad();");
					writeTag("        }, 300);");
					writeTag("    } else {");
					writeTag("        initialize();");
					writeTag("    }");
					writeTag("}");
					
					writeTag("var baseUrl = window.location.search;");
					writeTag("baseUrl = baseUrl.substring(baseUrl.indexOf('ippInteractionUri') + 18);");
					writeTag("baseUrl = baseUrl.indexOf('&') >= 0 ? baseUrl.substring(0, baseUrl.indexOf('&')) : baseUrl;");
					writeTag("baseUrl = baseUrl.substring(0, baseUrl.indexOf('/services'));");

					writeTag("var head = document.getElementsByTagName('head')[0];");
					writeTag("injectJS(head, baseUrl + '/plugins/views-common/js/libs/require/2.0.5/require.js');");
					writeTag("injectJS(head, baseUrl + '/plugins/processportal/js/manualActivityServerSupport.js');");
					writeTag("injectCSS(head, baseUrl + '/plugins/views-common/css/thirdparty/jquery/jquery-ui-1.10.2.custom.css');");
					writeTag("injectCSS(head, baseUrl + '/plugins/processportal/css/bpm-form.css');");

						writeTag("injectCSS(head, baseUrl + '/plugins/processportal/css/manual-activity.css');");
					writeTag("addStyle(head, '[ng\\\\:cloak], [ng-cloak], .ng-cloak {display: none;}');");

					writeTag("waitToLoad();");
					
					indentDown();
					writeTag("</script>");
				}

				/**
				 *
				 */
				MarkupGenerator.prototype.generateBody = function(
						parameterDefinitions) {
					writeTag("<body>");
					indentUp();

					this.generateCode(parameterDefinitions);

					indentDown();
					writeTag("</body>");
				}

				/**
				 * 
				 */
				MarkupGenerator.prototype.generateCode = function(parameterDefinitions) {
					var parameters = {};
					for ( var n = 0; n < parameterDefinitions.length; ++n) {
						var def = parameterDefinitions[n];
						if (parameters[def.id] == undefined || 
								parameters[def.id].direction == m_constants.IN_ACCESS_POINT) {
							parameters[def.id] = def;
						} 
					}

					var jsonDMs = [];
					for (var n in parameters) {
						var parameterDefinition = parameters[n];

						var readonly = (parameterDefinition.direction == m_constants.IN_ACCESS_POINT);
						var jsonDM;
						if (parameterDefinition.dataType == "primitive") {
							jsonDM =  {};
							jsonDM.id = parameterDefinition.id;
							jsonDM.name = parameterDefinition.name;
							jsonDM.fullXPath = "/" + parameterDefinition.id;
							jsonDM.readonly = readonly;
							jsonDM.typeName = parameterDefinition.primitiveDataType;
							if (jsonDM.typeName == "Timestamp") {
								jsonDM.typeName = "dateTime"; // Not sure why non standard data type is used. Change it.
							}
							jsonDM.isPrimitive = true;
							jsonDM.isList = false;
							jsonDM.isEnum = false;
							jsonDM.properties = {};
						} else if (parameterDefinition.dataType == "struct") {
							var typeDeclaration = m_model.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);
							jsonDM = buildDataMappings(typeDeclaration.model, typeDeclaration, parameterDefinition.id, parameterDefinition.name, "", readonly);
						} else  {
							// Not Supported - parameterDefinition.dataType == "dmsDocument"
							continue;
						}
						
						jsonDMs.push(jsonDM);
					}
					console.log("UI Mashup Json Tree ->");
					console.log(jsonDMs);
					
					var prefs = {
						layoutColumns: this.options.numberOfPrimitivesPerColumns,
						pluginsUrl: "../../../../../plugins" // This is relative to /services/rest/engine/interactions/<ID>/embeddedMarkup
					};
					var data = codeGenerator.create(prefs).generate(jsonDMs, "dm");

					// This controller is actually required at <html> element level, 
					// but due to HTML editor's limitations, need to add this at div level
					// at run time this will be moved to <html> in the initialization block
					writeTag("<div ng-controller='ManualActivityCtrl' class='ng-cloak'>");
					indentUp();
					writeTag("<div class='metaData' style='display: none' data-dataMappings='" + 
								JSON.stringify(jsonDMs) + "' data-binding='" + JSON.stringify(data.binding) + "'></div>\n");
					writeTag(data.html);

					// TODO: Generate Nested Structures
					var nestedHTML = generateNested(prefs, data.nestedBindings);
					writeTag("\n<!-- START nestedMarkups -->");
					writeTag("<div class='nestedMarkups' style='display: none'>\n" + nestedHTML + "\n</div>");
					writeTag("<!-- END nestedMarkups -->");

					this.generateButtons();
					indentDown();
					writeTag("</div>");
				}

				/*
				 * 
				 */
				function generateNested(prefs, nestedStructs) {
					var nestedHTML = "";
					if (nestedStructs) {
						for(var i in nestedStructs) {
							var xPath = nestedStructs[i].fullXPath;
							var parentXPath = xPath.substring(0, xPath.lastIndexOf("/"));
							
							var json = [];
							json.push(nestedStructs[i]);
							var data = codeGenerator.create(prefs).generate(json, "BINDING['REPLACEME']", null, parentXPath, "FORM_REPLACEME");
							
							nestedHTML += "\n<!-- START " + xPath + " -->\n" + 
										  "<div class='nestedMarkup' data-xpath='" + xPath + 
										  		"' data-binding='" + JSON.stringify(data.binding) + "'>" + data.html + 
										  "</div>\n<!-- END " + xPath + " -->\n";
							nestedHTML += generateNested(prefs, data.nestedBindings);
						}
					}
					return nestedHTML;
				}

				/**
				 * 
				 */
				function buildDataMappings (model, typeDeclaration, id, name, parentXPath, readonly) {
					var jsonRet = {};
					jsonRet.id = id;
					jsonRet.name = name;
					jsonRet.fullXPath = parentXPath + "/" + id;
					jsonRet.readonly = readonly;
					jsonRet.typeName = typeDeclaration.id;
					jsonRet.isPrimitive = false;
					jsonRet.isList = false;
					jsonRet.isEnum = false;
					jsonRet.children = [];
					jsonRet.properties = {};

					if (typeDeclaration.isEnumeration()) {
						jsonRet.isPrimitive = true;
						jsonRet.isEnum = true;
						jsonRet.enumValues = [];
						jQuery.each(typeDeclaration.getElements(), function(i, enumElem) {
							jsonRet.enumValues.push(enumElem.name);
						});
					} else {
						jQuery.each(typeDeclaration.getElements(), function(i, element) {
							var jsonChild = {};
							var type = element.type;
							// Strip prefix
							if (element.type.indexOf(':') !== -1) {
								type = element.type.split(":")[1];
							}
							var childTypeDeclaration = model.findTypeDeclarationBySchemaName(type);
	
							if (childTypeDeclaration == null || childTypeDeclaration.isEnumeration()) { // Primitive
								jsonChild.id = element.name;
								jsonChild.name = element.name;
								jsonChild.fullXPath = jsonRet.fullXPath + "/" + element.name;
								jsonChild.readonly = readonly;
								jsonChild.typeName = type;
								jsonChild.isPrimitive = true;
								jsonChild.isList = (element.cardinality === "many" || element.cardinality === "atLeastOne") ? true : false;
								jsonChild.isEnum = false;
								if (element.appinfo && element.appinfo.ui) {
									jsonChild.properties = element.appinfo.ui;
								} else {
								jsonChild.properties = {};
								}
								
								if (childTypeDeclaration != null && childTypeDeclaration.isEnumeration()) {
									jsonChild.isEnum = true;
									jsonChild.enumValues = [];
									jQuery.each(childTypeDeclaration.getElements(), function(i, enumElem) {
										jsonChild.enumValues.push(enumElem.name);
									});
								}
							} else { // XSD
								jsonChild = buildDataMappings(model, childTypeDeclaration, element.name, element.name, jsonRet.fullXPath, readonly);
								jsonChild.isList = (element.cardinality === "many" || element.cardinality === "atLeastOne") ? true : false;
								}
							jsonRet.children.push(jsonChild);
						});
					}
					
					return jsonRet;
				}

				/**
				 * 
				 */
				MarkupGenerator.prototype.generateButtons = function() {
					writeTag("<table id='buttonTable'>");
					indentUp();
					writeTag("<tr>");
					indentUp();

					if (this.options.generateCompleteButton) {
						writeTag("<td>");
						indentUp();
						writeTag("<button ng-click='completeActivity()'>Complete</button>");
						indentDown();
						writeTag("</td>");
					}

					if (this.options.generateSuspendButton) {
						writeTag("<td>");
						indentUp();
						writeTag("<button ng-click='suspendActivity()'>Suspend</button>");
						indentDown();
						writeTag("</td>");
					}

					if (this.options.generateSaveButton) {
						writeTag("<td>");
						indentUp();
						writeTag("<button ng-click='suspendActivity(true)'>Suspend And Save</button>");
						indentDown();
						writeTag("</td>");
					}

					if (this.options.generateAbortButton) {
						writeTag("<td>");
						indentUp();
						writeTag("<button ng-click='abortActivity()'>Abort</button>");
						indentDown();
						writeTag("</td>");
					}

					if (this.options.generateQaPassButton) {
						writeTag("<td>");
						indentUp();
						writeTag("<button ng-click='qaPassActivity()'>Quality Assurance Success</button>");
						indentDown();
						writeTag("</td>");
					}

					if (this.options.generateQaFailButton) {
						writeTag("<td>");
						indentUp();
						writeTag("<button ng-click='qaFailActivity()'>Quality Assurance Fail</button>");
						indentDown();
						writeTag("</td>");
					}

					indentDown();
					writeTag("</tr>");
					indentDown();
					writeTag("</table>");
				};
					}

				/**
			 * 
				 */
			function writeTag(tag) {
				markup += indent() + tag + "\n";
			}

			/**
			 *
			 */
			function emptyLine() {
				markup += "\n";
			}

			/**
			 *
			 */
			function indent() {
				var indentSpace = "";

				for ( var n = 0; n < indentLevel; ++n) {
					indentSpace += "   ";
				}

				return indentSpace;
			}

			/**
			 *
			 */
			function indentUp() {
				indentLevel++;
			}

			/**
			 *
			 */
			function indentDown() {
				indentLevel--;
			}
		});