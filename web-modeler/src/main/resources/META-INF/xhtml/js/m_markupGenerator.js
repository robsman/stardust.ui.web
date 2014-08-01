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
				"processportal/js/codeGenerator", "processportal/js/codeGeneratorMobile" ],
		function(m_utils, m_urlUtils, m_constants, m_model, m_modelElement,
				m_command, m_commandsController, m_elementConfiguration,
				m_i18nUtils, codeGenerator, codeGeneratorMobile) {
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
					
					if (!this.options.generateMobileMarkup === undefined) {
						this.options.generateMobileMarkup = false;
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

					writeTag("function mobilecheck() {");
					writeTag(" 	var check = false;");
			     	writeTag(" 	(function(a){if(/(android|bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\\-|your|zeto|zte\\-/i.test(a.substr(0,4)))check = true})(navigator.userAgent||navigator.vendor||window.opera);");
			     	writeTag("	return check;");
			     	writeTag("}");
			     	writeTag("var isMobile = " + this.options.generateMobileMarkup + " && mobilecheck();");
					writeTag("function initialize() {");
					writeTag("            var requireConfigPaths = {");
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
					writeTag("               };");
					writeTag("            if (isMobile) {");
					writeTag("            	requireConfigPaths['jquery-mobile'] = ['mobile-workflow/public/js/libs/jquery/plugins/jquery.mobile-1.4.0', '//code.jquery.com/mobile/1.4.0/jquery.mobile-1.4.0'];");
					writeTag("            }");
					writeTag("            var requireConfigShim = {");
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
					writeTag("               };");
					writeTag("            if (isMobile) {");
					writeTag("            	requireConfigShim['jquery-mobile'] = ['jquery'];");
					writeTag("              requireConfigShim['bpm.portal.Interaction'] = [ 'jquery', 'jquery-mobile' ];");
					writeTag("              requireConfigShim['bpm.portal.GenericAngular'] = [ 'jquery', 'jquery-mobile' ];");
					writeTag("              requireConfigShim['bpm.portal.GenericController'] = [ 'jquery', 'jquery-mobile' ];");
					writeTag("              requireConfigShim['bpm.portal.UIMashupController'] = [ 'jquery', 'jquery-mobile' ];");
					writeTag("   }");
					writeTag("            require.config({waitSeconds: 0, baseUrl : baseUrl + '/plugins/',");
					writeTag("               paths : requireConfigPaths,");
					writeTag("               shim : requireConfigShim");
					writeTag("});");
					writeTag("            var mashupDeps = [ 'require', 'jquery', 'jquery-ui', 'json', 'jquery.url', 'angularjs',");
					writeTag("               'xml2json', 'bpm.portal.Interaction', 'bpm.portal.GenericAngularApp', 'bpm.portal.GenericController', 'bpm.portal.UIMashupController' ];");
					writeTag("            if (isMobile) {");
					writeTag("            	mashupDeps.push('jquery-mobile');");
					writeTag("            }");
					writeTag("            require(mashupDeps, function(require, jquery, jqueryUi,");
					writeTag("   json, jqueryUrl, angularjs, xml2json, stardustPortalInteraction, stardustGenericController) {");
					writeTag("            uiMashupController = new bpm.portal.UIMashupController();");
					writeTag("            uiMashupController.init();");
					writeTag("      });");
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

					writeTag("         if (isMobile) {");
					writeTag("         	injectCSS(head, baseUrl + '/plugins/mobile-workflow/public/css/jquery.mobile/jquery.mobile-1.4.0.css');");
					writeTag("         	injectCSS(head, baseUrl + '/plugins/processportal/css/manual-activity-mobile.css');");
					writeTag("         } else {");
					writeTag("         	injectCSS(head, baseUrl + '/plugins/mobile-workflow/public/css/jquery.mobile/jquery.mobile-1.4.0.css');");
						writeTag("injectCSS(head, baseUrl + '/plugins/processportal/css/manual-activity.css');");
					writeTag("         }");
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
					writeTag("<div ng-controller='ManualActivityCtrl' class='ng-cloak'>");
					indentUp();

					this.generateCode(parameterDefinitions);

					indentDown();
					writeTag("</div>");
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
					writeTag("<div class='hideIfMobile'>");
					indentUp();
					writeTag("<div class='metaData' style='display: none' data-dataMappings='" + 
								JSON.stringify(jsonDMs) + "' data-binding='" + JSON.stringify(data.binding) + "'></div>\n");
					writeTag(data.html);

					// TODO: Generate Nested Structures
					var nestedHTML = generateNested(prefs, data.nestedBindings, codeGenerator);
					writeTag("\n<!-- START nestedMarkups -->");
					writeTag("<div class='nestedMarkups' style='display: none'>\n" + nestedHTML + "\n</div>");
					writeTag("<!-- END nestedMarkups -->");
					writeTag("</div>");
					
					if (this.options.generateMobileMarkup) {
						// Get code generator mobile
						data = codeGeneratorMobile.create(prefs).generate(jsonDMs, "dm");
	
						// This controller is actually required at <html> element level, 
						// but due to HTML editor's limitations, need to add this at div level
						// at run time this will be moved to <html> in the initialization block
						writeTag("<div class='hideIfDesktop'>");
						indentUp();
						writeTag("<div class='metaData' style='display: none' data-dataMappings='" + 
									JSON.stringify(jsonDMs) + "' data-binding='" + JSON.stringify(data.binding) + "'></div>\n");
						writeTag(data.html);
	
						// TODO: Generate Nested Structures
						var nestedHTML = generateNested(prefs, data.nestedBindings, codeGeneratorMobile);
						writeTag("\n<!-- START nestedMarkups -->");
						writeTag("<div class='nestedMarkups' style='display: none'>\n" + nestedHTML + "\n</div>");
						writeTag("<!-- END nestedMarkups -->");
						writeTag("</div>");
					}

					this.generateButtons();
					indentDown();
				}

				/*
				 * 
				 */
				function generateNested(prefs, nestedStructs, codeGenerator) {
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
							nestedHTML += generateNested(prefs, data.nestedBindings, codeGenerator);
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
							if (childTypeDeclaration == null || childTypeDeclaration.isEnumeration() || childTypeDeclaration.getElements().length == 0) { // Primitive
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
					writeTag("<div class='hideIfMobile'>");
					indentUp();
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
					indentDown();
					writeTag("</div>");
					
					if (this.options.generateMobileMarkup) {
						this.generateButtonsForMobile();
					}
				};
				

				/**
				 * 
				 */
				MarkupGenerator.prototype.generateButtonsForMobile = function() {
					writeTag("<div class='hideIfDesktop'>");
					indentUp();
					writeTag("<div class='ui-body ui-body-a ui-corner-all'>");
					indentUp();

					if (this.options.generateCompleteButton) {
						writeTag("<button ng-click='completeActivity()' class='ui-btn ui-shadow ui-corner-all' style='width: 100%'>Complete</button><br>");
					}

					if (this.options.generateSuspendButton) {
						writeTag("<button ng-click='suspendActivity()' class='ui-btn ui-shadow ui-corner-all' style='width: 100%'>Suspend</button><br>");
					}

					if (this.options.generateSaveButton) {
						writeTag("<button ng-click='suspendActivity(true)' class='ui-btn ui-shadow ui-corner-all' style='width: 100%'>Suspend And Save</button><br>");
					}

					if (this.options.generateAbortButton) {
						writeTag("<button ng-click='abortActivity()' class='ui-btn ui-shadow ui-corner-all' style='width: 100%'>Abort</button><br>");
					}

					if (this.options.generateQaPassButton) {
						writeTag("<button ng-click='qaPassActivity()' class='ui-btn ui-shadow ui-corner-all' style='width: 100%'>Quality Assurance Success</button><br>");
					}

					if (this.options.generateQaFailButton) {
						writeTag("<button ng-click='qaFailActivity()' class='ui-btn ui-shadow ui-corner-all' style='width: 100%'>Quality Assurance Fail</button><br>");
					}
					indentDown();
					writeTag("</div>");
					indentDown();
					writeTag("</div>");
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