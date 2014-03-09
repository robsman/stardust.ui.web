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

					writeTag("function initialize() {");
					writeTag("require.config({baseUrl : baseUrl + '/plugins/',");
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
					writeTag(" 'bpm.portal.Interaction' : [ 'processportal/Interaction' ],");
					writeTag(" 'bpm.portal.GenericController' : [ 'processportal/GenericController' ]");
					// writeTag(" 'xml2json' : [
					// 'bpm-modeler/public/mashup/xml2js' ],");
					// writeTag(" 'bpm.portal.Interaction' : [
					// 'bpm-modeler/public/mashup/Interaction' ],");
					// writeTag(" 'bpm.portal.GenericController' : [
					// 'bpm-modeler/public/mashup/GenericController' ]");
					writeTag("   },");
					writeTag("   shim : {");
					writeTag("      'jquery-ui' : [ 'jquery' ],");
					writeTag("      'jquery.url' : [ 'jquery' ],");
					writeTag("      'angularjs' : {");
					writeTag("         require : 'jquery',");
					writeTag("         exports : 'angular'");
					writeTag("      },");
					writeTag("      'bpm.portal.Interaction' : [ 'jquery' ],");
					writeTag("      'bpm.portal.GenericController' : [ 'jquery' ]");
					writeTag("   }");
					writeTag("});");
					writeTag("require([ 'require', 'jquery', 'jquery-ui', 'json', 'jquery.url', 'angularjs',");
					writeTag("   'xml2json', 'bpm.portal.Interaction', 'bpm.portal.GenericController' ], function(require, jquery, jqueryUi,");
					writeTag("   json, jqueryUrl, angularjs, xml2json, stardustPortalInteraction, stardustGenericController) {");
					writeTag("      jQuery(document).ready(");
					writeTag("      function() {");
					writeTag("         var interaction = new bpm.portal.Interaction();");
					writeTag("         var controller = new bpm.portal.GenericController();");
					writeTag("         jQuery('.structureTabs').tabs();");
					writeTag("      interaction.bind().done(function(){");
					writeTag("         controller.bind(angularjs, interaction);");
					writeTag("         });");
					writeTag("   });");
					writeTag("});");
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
					writeTag("injectCSS(head, baseUrl + '/plugins/views-common/css/thirdparty/jquery/jquery-ui-1.10.2.custom.css');");
					writeTag("injectCSS(head, baseUrl + '/plugins/processportal/css/bpm-form.css');");
					if (THE_NEW_WAY) {
						writeTag("injectCSS(head, baseUrl + '/plugins/processportal/css/manual-activity.css');");
					}

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

					if (THE_NEW_WAY) {
						this.generateNewWay(parameterDefinitions);
					} else {
						this.generateTheOldWay(parameterDefinitions);
					}

					indentDown();
					writeTag("</body>");
				}

				/**
				 * 
				 */
				MarkupGenerator.prototype.generateNewWay = function(parameterDefinitions) {
					var jsonDMs = [];
					for ( var n = 0; n < parameterDefinitions.length; ++n) {
						var parameterDefinition = parameterDefinitions[n];

						var readonly = (parameterDefinition.direction == m_constants.IN_ACCESS_POINT);
						var jsonDM;
						if (parameterDefinition.dataType == "primitive") {
							jsonDM =  {};
							jsonDM.id = parameterDefinition.id;
							jsonDM.fullXPath = "/" + parameterDefinition.id;
							jsonDM.readonly = readonly;
							jsonDM.typeName = parameterDefinition.primitiveDataType;
							jsonDM.isPrimitive = true;
							jsonDM.isList = false;
							jsonDM.isEnum = false;
							jsonDM.properties = {};
						} else if (parameterDefinition.dataType == "struct" || parameterDefinition.dataType == "dmsDocument") {
							var typeDeclaration = m_model.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);
							jsonDM = buildDataMappings(typeDeclaration.model, typeDeclaration, parameterDefinition.id, "", readonly);
						}
						
						jsonDMs.push(jsonDM);
					}
					console.log("UI Mashup Json Tree ->");
					console.log(jsonDMs);
					
					var prefs = {
						layoutColumns: this.options.numberOfPrimitivesPerColumns,
						ngModelSepAsDot: true,
						pluginsUrl: "../../../../../plugins", // This is relative to /services/rest/engine/interactions/<ID>/embeddedMarkup
						skipMultiCardinalityNested: true,
						splitDateTimeFields: false
					};
					var data = codeGenerator.create(prefs).generate(jsonDMs);
					writeTag(data.html);
					indentUp();
					this.generateButtons();
					indentDown();
				}

				/**
				 * 
				 */
				function buildDataMappings (model, typeDeclaration, id, parentXPath, readonly) {
					var jsonRet = {};
					jsonRet.id = id;
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
								jsonChild.fullXPath = jsonRet.fullXPath + "/" + element.name;
								jsonChild.readonly = readonly;
								jsonChild.typeName = type;
								jsonChild.isPrimitive = true;
								jsonChild.isList = element.cardinality === "many" ? true : false;
								jsonChild.isEnum = false;
								jsonChild.properties = {};
								
								if (childTypeDeclaration != null && childTypeDeclaration.isEnumeration()) {
									jsonChild.isEnum = true;
									jsonChild.enumValues = [];
									jQuery.each(childTypeDeclaration.getElements(), function(i, enumElem) {
										jsonChild.enumValues.push(enumElem.name);
									});
								}
							} else { // XSD
								if (element.cardinality === "required") {
									jsonChild = buildDataMappings(model, childTypeDeclaration, element.name, jsonRet.fullXPath, readonly);
								} else { // element.cardinality === "many"
									jsonChild = buildDataMappings(model, childTypeDeclaration, element.name, jsonRet.fullXPath, readonly);
									jsonChild.isList = true;
								}
							}
							jsonRet.children.push(jsonChild);
						});
					}
					
					return jsonRet;
				}

				/**
				 * 
				 */
				MarkupGenerator.prototype.generateTheOldWay = function(parameterDefinitions) {
					writeTag("<table>");
					indentUp();
					writeTag("<tr>");
					writeTag("<td><ul class='errorMessagesPanel'><li ng-repeat='error in errors'>{{error.message}}</li></ul></td>");
					writeTag("</tr>");

					for ( var n = 0; n < parameterDefinitions.length; ++n) {
						var parameterDefinition = parameterDefinitions[n];

						m_utils.debug("Parameter Definition");
						m_utils.debug(parameterDefinition);

						var readonly = (parameterDefinition.direction == m_constants.IN_ACCESS_POINT);

						m_utils.debug("readonly = " + readonly);

						if (parameterDefinition.dataType == "primitive") {
							this.generateRowForPrimitive(
									parameterDefinition.primitiveDataType,
									parameterDefinition.id,
									parameterDefinition.name, readonly);
						}
					}

					for ( var n = 0; n < parameterDefinitions.length; ++n) {
						var parameterDefinition = parameterDefinitions[n];

						m_utils.debug("Parameter Definition");
						m_utils.debug(parameterDefinition);

						var readonly = (parameterDefinition.direction == m_constants.IN_ACCESS_POINT);

						m_utils.debug("readonly = " + readonly);

						if (parameterDefinition.dataType == "struct"
								|| parameterDefinition.dataType == "dmsDocument") {
							writeTag("<tr>");
							indentUp();
							writeTag("<td>");
							this.generateStructurePanel(parameterDefinition,
									readonly);
							writeTag("</td>");
							indentDown();
							writeTag("</tr>");
						}
					}

					writeTag("<tr>");
					indentUp();
					writeTag("<td>");
					indentUp();
					
					this.generateButtons();
					
					indentDown();
					writeTag("</td>");
					indentDown();
					writeTag("</tr>");
					indentDown();
					writeTag("</table>");
					writeTag("<div id='validationErrorMessageDialog' title=''>");
					indentUp();
					writeTag("<p>Correct your validation errors first.</p>");
					indentDown();
					writeTag("</div>");
				};

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

				/**
				 *
				 */
				MarkupGenerator.prototype.generateRowForPrimitive = function(
						type, path, name, readonly, annotations) {
					writeTag("<tr>");
					indentUp();
					this.generateCellForPrimitive(type, path, name, readonly);
					indentDown();
					writeTag("</tr>");
				};

				/**
				 *
				 */
				MarkupGenerator.prototype.generateCellForPrimitive = function(
						type, path, name, readonly, annotations) {
					// <carnot:InputPreferences_prefixKey>bla</carnot:InputPreferences_prefixKey>
					// <carnot:InputPreferences_prefix>bla</carnot:InputPreferences_prefix>
					// <carnot:InputPreferences_suffixKey>blub</carnot:InputPreferences_suffixKey>
					// <carnot:InputPreferences_suffix>blub</carnot:InputPreferences_suffix>

					writeTag("<td>");
					indentUp();
					writeTag("<label>" + this.generateLabel(name) + "</label>");
					indentDown();
					writeTag("</td>");
					writeTag("<td>");
					indentUp();
					this.generateInputForPrimitive(type, path, readonly);
					indentDown();
					writeTag("</td>");
				};

				/**
				 *
				 */
				MarkupGenerator.prototype.generateInputForPrimitive = function(
						type, path, readonly, annotations) {
					if (!annotations) {
						annotations = {};
					}

					readonly = readonly
							|| annotations.InputPreferences_readonly;

					var mandatory = annotations.InputPreferences_mandatory;
					var disabled = readonly ? "disabled" : "";
					var required = mandatory ? "required" : "";
					var ngModel = path == null ? "" : " ng-model='" + path
							+ "'";

					if (type === "xsd:boolean") {
						if (readonly
								&& annotations.BooleanInputPreferences_readonlyOutputType !== "CHECKBOX") {
							writeTag("<label>" + path == null ? "" : "{{"
									+ path + "}}" + "</label>");
						} else {
							writeTag("<input type='checkbox' class='input' "
									+ disabled + ngModel + "/>");
						}
					} else if (type === "xsd:int" || type === "xsd:integer") {
						writeTag("<input type='number' class='integerInputField' "
								+ disabled
								+ ngModel
								+ " sd-integer "
								+ required + "/>");
					} else if (type === "xsd:decimal") {
						writeTag("<input type='text' class='decimalInputField' "
								+ disabled
								+ ngModel
								+ " sd-decimal "
								+ required + "/>");
					} else if (type === "xsd:date") {
						writeTag("<input type='text' " + disabled + ngModel
								+ " sd-date " + required + "/>");
					} else {
						if (annotations.StringInputPreferences_stringInputType === "TEXTAREA") {
							var textAreaRows = annotations.StringInputPreferences_textAreaRows;

							if (!textAreaRows) {
								textAreaRows = 10;
							}

							var textAreaColumns = annotations.StringInputPreferences_textAreaColumns;

							if (!textAreaColumns) {
								textAreaColumns = 50;
							}

							writeTag("<textarea " + disabled + " rows="
									+ textAreaRows + " cols=" + textAreaColumns
									+ ngModel + " " + required + "/>");
						} else {
							writeTag("<input type='text' " + disabled + ngModel
									+ " " + required + "/>");
						}
					}
				};

				/**
				 *
				 */
				MarkupGenerator.prototype.generateStructurePanel = function(
						parameterDefinition, readonly) {
					var typeDeclaration = m_model
							.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

					m_utils.debug("Type Declaration");
					m_utils.debug(typeDeclaration);

					this.generateStructurePanelRecursively(
							typeDeclaration.model, typeDeclaration,
							parameterDefinition.id, readonly, 1);
				};

				/**
				 *
				 */
				MarkupGenerator.prototype.generateStructurePanelRecursively = function(
						model, typeDeclaration, path, readonly, depth) {
					// Count primitives

					var primitiveCount = 0;
					var self = this;

					jQuery.each(typeDeclaration.getElements(), function(i,
							element) {
						if (element.cardinality === "required") {
							var type = element.type;

							// Strip prefix

							if (element.type.indexOf(':') !== -1) {
								type = element.type.split(":")[1];
							}

							var childTypeDeclaration = model
									.findTypeDeclarationBySchemaName(type);

							if (childTypeDeclaration == null
									|| !childTypeDeclaration.isSequence()) {
								primitiveCount++;
							}
						}
					});

					var columnCount = Math.ceil(primitiveCount
							/ this.options.numberOfPrimitivesPerColumns);
					var inputCount = 0;

					if (primitiveCount > 0) {
						writeTag("<table cellpadding='0' cellspacing='0' class='formTable'>");
						indentUp();
						writeTag("<tr>");
						indentUp();

						// All primitives

						jQuery.each(typeDeclaration.getElements(), function(i,
								element) {
							if (element.cardinality === "required") {
								var type = element.type;

								// Strip prefix

								if (element.type.indexOf(':') !== -1) {
									type = element.type.split(":")[1];
								}

								var childTypeDeclaration = model
										.findTypeDeclarationBySchemaName(type);

								if (childTypeDeclaration == null) {
									self.generateCellForPrimitive(element.type,
											path + "." + element.name,
											element.name, readonly,
											element.annotations);
								} else {
									if (!childTypeDeclaration.isSequence()) {
										self.generateCellForEnumeration(
												childTypeDeclaration, path
														+ "." + element.name,
												element.name, readonly);
									}
								}

								inputCount++;

								if (inputCount == columnCount) {
									inputCount = 0;

									indentDown();
									writeTag("</tr>");
									writeTag("<tr>");
									indentUp();
								}
							}
						});

						indentDown();
						writeTag("</tr>");
						indentDown();
						writeTag("</table>");
					}

					// Generate structures and arrays

					++depth;

					m_utils.debug("Arrays: "
							+ this.options.tabsForFirstLevelTables);

					if (this.options.tabsForFirstLevel && depth == 2) {
						writeTag("<div class='structureTabs'>");
						indentUp();
						writeTag("<ul>");
						indentUp();

						jQuery
								.each(
										typeDeclaration.getElements(),
										function(i, element) {
											if (element.cardinality === "required") {
												var type = element.type;

												// Strip prefix

												if (element.type.indexOf(':') !== -1) {
													type = element.type
															.split(":")[1];
												}

												var childTypeDeclaration = model
														.findTypeDeclarationBySchemaName(type);

												if (childTypeDeclaration != null) {
													if (childTypeDeclaration
															.isSequence()) {

														writeTag("<li><a href='#"
																+ element.name
																+ "Tab'><span id='"
																+ element.name
																+ "'>"
																+ self
																		.generateLabel(element.name)
																+ "</span></a></li>");
													}
												}
											} else if (self.options.tabsForFirstLevelTables) {
												m_utils.debug("Generate LIs");

												writeTag("<li><a href='#"
														+ element.name
														+ "Tab'><span id='"
														+ element.name
														+ "'>"
														+ self
																.generateLabel(element.name)
														+ "</span></a></li>");

											}
										});

						indentDown();
						writeTag("</ul>");
					} else {
						writeTag("<table cellpadding='0' cellspacing='0' class='formTable'>");
						indentUp();
					}

					jQuery
							.each(
									typeDeclaration.getElements(),
									function(i, element) {
										if (element.cardinality === "required") {
											var type = element.type;

											// Strip prefix

											if (element.type.indexOf(':') !== -1) {
												type = element.type.split(":")[1];
											}

											var childTypeDeclaration = model
													.findTypeDeclarationBySchemaName(type);

											if (childTypeDeclaration != null) {
												if (childTypeDeclaration
														.isSequence()) {
													if (self.options.tabsForFirstLevel
															&& depth == 2) {
														writeTag("<div id='"
																+ element.name
																+ "Tab'>");
														indentUp();
													} else {
														writeTag("<tr>");
														indentUp();
														writeTag("<td>");
														writeTag("<h"
																+ Math.min(
																		depth,
																		5)
																+ ">"
																+ self
																		.generateLabel(element.name)
																+ "</h"
																+ Math.min(
																		depth,
																		5)
																+ ">");
													}

													self
															.generateStructurePanelRecursively(
																	model,
																	childTypeDeclaration,
																	path
																			+ "."
																			+ element.name,
																	readonly,
																	depth);

													if (self.options.tabsForFirstLevel
															&& depth == 2) {
														indentDown();
														writeTag("</div>");
													} else {
														writeTag("</td>");
														indentDown();
														writeTag("</tr>");
													}
												}
											}
										} else {
											self.generateTableForToMany(model,
													element, path, depth);
										}

									});

					if (this.options.tabsForFirstLevel && depth == 2) {
						indentDown();
						writeTag("</div>");
					} else {
						indentDown();
						writeTag("</table>");
					}
				};

				/**
				 *
				 */
				MarkupGenerator.prototype.generateTableForToMany = function(
						model, element, path, depth) {
					if (this.options.tabsForFirstLevelTables) {
						writeTag("<div id='" + element.name + "Tab'>");
						indentUp();
					} else {
						writeTag("<tr>");
						indentUp();
						writeTag("<td>");
						indentUp();
						writeTag("<h" + Math.min(depth, 5) + ">"
								+ this.generateLabel(element.name) + "</h"
								+ Math.min(depth, 5) + ">");
						indentDown();
						writeTag("</td>");
						indentDown();
						writeTag("</tr>");
						writeTag("<tr>");
						indentUp();
						writeTag("<td>");
						indentUp();
					}

					writeTag("<table cellpadding='0' cellspacing='0' class='dataTable'>");
					indentUp();

					var type = element.type;

					// Strip prefix

					if (element.type.indexOf(':') !== -1) {
						type = element.type.split(":")[1];
					}

					var childTypeDeclaration = model
							.findTypeDeclarationBySchemaName(type);

					if (childTypeDeclaration != null) {
						if (childTypeDeclaration.isSequence()) {
							writeTag("<thead>");
							indentUp();
							writeTag("<tr>");
							indentUp();
							writeTag("<th></th>");

							var self = this;

							jQuery
									.each(
											childTypeDeclaration.getElements(),
											function(i, element) {
												writeTag("<th>"
														+ self
																.generateLabel(element.name)
														+ "</th>");
											});

							indentDown();
							writeTag("</tr>");
							indentDown();
							writeTag("</thead>");
							writeTag("<tbody>");
							indentUp();
							writeTag("<tr class='referenceRow'>");
							indentUp();

							writeTag("<td><img src='"
									+ m_urlUtils.getContextName()
									+ "/plugins/views-common/images/icons/delete.png' alt='Delete'/></td>");

							jQuery
									.each(
											childTypeDeclaration.getElements(),
											function(i, element) {
												writeTag("<td>");
												indentUp();
												var embeddedType = element.type;

												// Strip prefix

												if (embeddedType.indexOf(':') !== -1) {
													embeddedType = embeddedType
															.split(":")[1];
												}

												var embeddedTypeDeclaration = model
														.findTypeDeclarationBySchemaName(embeddedType);

												if (embeddedTypeDeclaration != null
														&& !embeddedTypeDeclaration
																.isSequence()) {
													// Add placeholder for
													// select
													// generateSelectForEnumeration(embeddedTypeDeclaration,
													// null,
													// false);
												} else {
													self
															.generateInputForPrimitive(
																	element.type,
																	null,
																	false,
																	false);
												}

												indentDown();
												writeTag("</td>");
											});

							indentDown();
							writeTag("</tr>");
							writeTag("<tr ng-repeat='$tableIterator in " + path
									+ "." + element.name + "'>");
							indentUp();

							writeTag("<td><href ng-click='deleteRow($event, $index)' path='"
									+ path
									+ "."
									+ element.name
									+ "'><img src='"
									+ m_urlUtils.getContextName()
									+ "/plugins/views-common/images/icons/delete.png' alt='Delete'/></href></td>");

							jQuery
									.each(
											childTypeDeclaration.getElements(),
											function(i, element) {
												writeTag("<td>");
												indentUp();
												var embeddedType = element.type;

												// Strip prefix

												if (embeddedType.indexOf(':') !== -1) {
													embeddedType = embeddedType
															.split(":")[1];
												}

												var embeddedTypeDeclaration = model
														.findTypeDeclarationBySchemaName(embeddedType);

												if (embeddedTypeDeclaration != null
														&& !embeddedTypeDeclaration
																.isSequence()) {
													self
															.generateSelectForEnumeration(
																	embeddedTypeDeclaration,
																	"$tableIterator."
																			+ element.name,
																	false);
												} else {
													self
															.generateInputForPrimitive(
																	element.type,
																	"$tableIterator."
																			+ element.name,
																	false,
																	element.annotations);
												}

												indentDown();
												writeTag("</td>");
											});

							indentDown();
							writeTag("</tr>");
							writeTag("<tr>");
							indentUp();
							writeTag("<td><href ng-click='addRow($event)' path='"
									+ path
									+ "."
									+ element.name
									+ "'><img src='"
									+ m_urlUtils.getContextName()
									+ "/plugins/views-common/images/icons/add.png' alt='Add'/></href></td>");
							writeTag("<td></td>");
							writeTag("<td></td>");
							writeTag("<td></td>");
							writeTag("<td></td>");
							indentDown();
							writeTag("</tr>");
							indentDown();
							writeTag("</tbody>");
						} else {
						}
					} else {
					}

					indentDown();
					writeTag("</table>");

					if (this.options.tabsForFirstLevelTables) {
						indentDown();
						writeTag("</div>");
					} else {
						indentDown();
						writeTag("</td>");
						indentDown();
						writeTag("</tr>");
					}
				};

				/**
				 *
				 */
				MarkupGenerator.prototype.generateRowForEnumeration = function(
						type, path, name, readonly) {
					writeTag("<tr>");
					indentUp();
					this.generateCellForEnumeration(type, path, name, readonly);
					indentDown();
					writeTag("</tr>");
				};

				/**
				 *
				 */
				MarkupGenerator.prototype.generateCellForEnumeration = function(
						type, path, name, readonly) {
					writeTag("<td>");
					indentUp();
					writeTag("<label>" + this.generateLabel(name) + "</label>");
					indentDown();
					writeTag("</td>");
					writeTag("<td>");
					indentUp();
					this.generateSelectForEnumeration(type, path, readonly);
					indentDown();
					writeTag("</select>");
					indentDown();
					writeTag("</td>");
				};

				/**
				 *
				 */
				MarkupGenerator.prototype.generateSelectForEnumeration = function(
						type, path, readonly) {
					var disabled = readonly ? "disabled" : "";

					writeTag("<select ng-model='" + path + "' " + disabled
							+ ">");
					indentUp();

					for ( var enumerator in type.getFacets()) {
						writeTag("<option value='"
								+ type.getFacets()[enumerator].name + "'>"
								+ type.getFacets()[enumerator].name
								+ "</option>");
					}
				};

				/**
				 *
				 */
				MarkupGenerator.prototype.generateLabel = function(identifier) {
					var previousCharacter = identifier.charAt(0);
					var label = previousCharacter.toUpperCase();

					identifier = identifier.slice(1);

					for ( var n = 0; n < identifier.length; ++n) {
						if (isLowerCase(previousCharacter)
								&& isUpperCase(identifier.charAt(n))) {
							label += " ";
						}

						label += identifier.charAt(n);
						previousCharacter = identifier.charAt(n);
					}

					return label;
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

			/**
			 *
			 */
			function isLowerCase(c) {
				if (c >= 'a' && c <= 'z') {
					return true;
				}

				return false;
			}

			/**
			 *
			 */
			function isUpperCase(c) {
				if (c >= 'A' && c <= 'Z') {
					return true;
				}

				return false;
			}
		});