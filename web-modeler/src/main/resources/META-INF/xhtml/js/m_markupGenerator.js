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
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_urlUtils, m_constants, m_model, m_modelElement,
				m_command, m_commandsController, m_elementConfiguration,
				m_i18nUtils) {
			var markup = "";
			var indentLevel = 0;

			return {
				generateMarkup : generateMarkup,
				generateMarkupForAngular : generateMarkupForAngular
			};

			/**
			 * 
			 */
			function generateMarkup(parameterDefinitions) {
				markup = "";

				writeTag("<html>");
				indentUp();
				generateHeader(parameterDefinitions);
				generateBody(parameterDefinitions);
				indentDown();
				writeTag("</html>");

				return markup;
			}

			/**
			 * 
			 */
			function generateMarkupForAngular(parameterDefinitions) {
				markup = "";

				writeTag("<html>");
				indentUp();
				generateHeaderForAngular(parameterDefinitions);
				generateBodyForAngular(parameterDefinitions);
				indentDown();
				writeTag("</html>");

				return markup;
			}

			/**
			 * 
			 */
			function generateHeader(parameterDefinitions) {
				writeTag("<head>");
				indentUp();
				generateCss();
				generateJavaScript(parameterDefinitions);
				indentDown();
				writeTag("</head>");
			}

			/**
			 * 
			 */
			function generateHeaderForAngular(parameterDefinitions) {
				writeTag("<head>");
				indentUp();
				generateCss();
				generateJavaScriptForAngular(parameterDefinitions);
				indentDown();
				writeTag("</head>");
			}

			/**
			 * 
			 */
			function generateCss() {
				writeTag("<link rel='stylesheet' type='text/css' href='"
						+ m_urlUtils.getPlugsInRoot()
						+ "/bpm-modeler/css/lightdust.css'></link>");
			}

			/**
			 * 
			 */
			function generateJavaScript(parameterDefinitions) {
				writeTag("<script src='http://localhost:8080/pepper-test/plugins/bpm-modeler/js/libs/require/2.0.5/require.js'></script>");
				writeTag("<script>");
				indentUp();
				writeTag("require.config({");
				indentUp();
				writeTag("baseUrl : 'http://localhost:8080/pepper-test/plugins/',");
				writeTag("paths : {");
				writeTag("'jquery' : [ 'bpm-modeler/js/libs/jquery/jquery-1.7.2', '//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],");
				writeTag("'jquery.url' : ['bpm-modeler/js/libs/jquery/plugins/jquery.url', 'https://raw.github.com/allmarkedup/jQuery-URL-Parser/4f5254f2519111ad7037d398b2efa61d3cda58d4/jquery.url']");
				writeTag("}");
				indentDown();
				writeTag("});");
				emptyLine();
				writeTag("require([ 'require', 'jquery', 'jquery.url', 'ui-mashup/public/GenericController' ],");
				writeTag("function(require, jquery, jqueryUrl, GenericController) {");
				indentUp();
				writeTag("jQuery(document).ready(function() {");
				indentUp();
				writeTag("var controller = GenericController.create();");
				emptyLine();
				writeTag("// Add options to Controller");
				writeTag("//");
				writeTag("// * validationCallback");
				emptyLine();
				writeTag("controller.initialize({});");
				indentDown();
				writeTag("});");
				indentDown();
				writeTag("});");
				indentDown();
				writeTag("</script>");
			}

			/**
			 * 
			 */
			function generateJavaScriptForAngular(parameterDefinitions) {
				writeTag("<script src='http://localhost:8080/pepper-test/plugins/bpm-modeler/js/libs/require/2.0.5/require.js'></script>");
				writeTag("<script>");
				indentUp();
				writeTag("require.config({");
				writeTag("baseUrl: '../../',");
				writeTag("paths : {");
				writeTag("'jquery' : ['bpm-modeler/js/libs/jquery/jquery-1.7.2', '//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min'],");
				writeTag("'jquery.url': ['bpm-modeler/js/libs/jquery/plugins/jquery.url', 'https://raw.github.com/allmarkedup/jQuery-URL-Parser/4f5254f2519111ad7037d398b2efa61d3cda58d4/jquery.url'],");
				writeTag("'angularjs' : ['bpm-modeler/js/libs/angular/angular-1.0.2', '//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min'],");
				writeTag("},");
				writeTag("shim: {");
				writeTag("'jquery.url': ['jquery'],");
				writeTag("'angularjs': {");
				writeTag("require: 'jquery',");
				writeTag("exports: 'angular'");
				writeTag("}");
				writeTag("}");
				writeTag("});");
				writeTag("require(['require',");
				writeTag("'jquery',");
				writeTag("'jquery.url',");
				writeTag("'angularjs',");
				writeTag("'bpm-modeler/angular/app',");
				writeTag("'ui-mashup/public/UiMashupInteraction''], function(require, jquery, jqueryUrl, angularjs, angularApp, UiMashupInteraction) {");
				writeTag("jQuery(document).ready(function() {");
				writeTag("angularApp.init();");
				writeTag("var interaction = UiMashupInteraction.create();");
				writeTag("interaction.bind().done(function() {");
				writeTag("var controller = LoanRequestControllerAngular.create(angular.element(document.body).scope());");
				writeTag("controller.initializeFromCustomerId(interaction.input.New_0.customerId).done(function(){");
				writeTag("controller.$apply();");
				writeTag("controller.submitListeners.push(function(){");
				writeTag("interaction.output.loanPlan = controller.loanPlan;");
				writeTag("interaction.complete();");
				writeTag("});");
				writeTag("}).fail();");
				writeTag("}).fail();");
				writeTag("});");
				writeTag("});");
				indentDown();
				writeTag("</script>");
			}

			/**
			 * 
			 */
			function generateBody(parameterDefinitions) {
				writeTag("<body>");
				indentUp();
				writeTag("<table>");
				indentUp();

				for ( var n = 0; n < parameterDefinitions.length; ++n) {
					var parameterDefinition = parameterDefinitions[n];

					m_utils.debug("Parameter Definition");
					m_utils.debug(parameterDefinition);

					var readonly = (parameterDefinition.direction == m_constants.IN_ACCESS_POINT);

					m_utils.debug("readonly = " + readonly);

					if (parameterDefinition.dataType == "primitive") {
						generateRowForPrimitive(
								parameterDefinition.primitiveDataType,
								parameterDefinition.id,
								parameterDefinition.name, readonly);
					} else if (parameterDefinition.dataType == "struct"
							|| parameterDefinition.dataType == "dmsDocument") {
						writeTag("<tr>");
						indentUp();
						writeTag("<td>");
						generateStructurePanel(parameterDefinition, readonly);
						writeTag("</td>");
						indentDown();
						writeTag("</tr>");
					} else {
						// Deal with primitives
					}
				}

				indentDown();
				writeTag("</table>");
				indentDown();
				writeTag("</body>");

				return markup;
			}

			/**
			 * 
			 */
			function generateBodyForAngular(parameterDefinitions) {
				writeTag("<body>");
				indentUp();
				writeTag("<table>");
				indentUp();

				for ( var n = 0; n < parameterDefinitions.length; ++n) {
					var parameterDefinition = parameterDefinitions[n];

					m_utils.debug("Parameter Definition");
					m_utils.debug(parameterDefinition);

					var readonly = (parameterDefinition.direction == m_constants.IN_ACCESS_POINT);

					m_utils.debug("readonly = " + readonly);

					if (parameterDefinition.dataType == "primitive") {
						generateRowForPrimitiveForAngular(
								parameterDefinition.primitiveDataType,
								parameterDefinition.id,
								parameterDefinition.name, readonly);
					} else if (parameterDefinition.dataType == "struct"
							|| parameterDefinition.dataType == "dmsDocument") {
						writeTag("<tr>");
						indentUp();
						writeTag("<td>");
						generateStructurePanelForAngular(parameterDefinition,
								readonly);
						writeTag("</td>");
						indentDown();
						writeTag("</tr>");
					} else {
						// Deal with primitives
					}
				}

				indentDown();
				writeTag("</table>");
				indentDown();
				writeTag("</body>");

				return markup;
			}

			/**
			 * 
			 */
			function generateRowForPrimitive(type, path, name, readonly) {
				writeTag("<tr>");
				indentUp();
				writeTag("<td>");
				indentUp();
				writeTag("<label for='" + path + "Input'>" + generateLabel(name) + "</label>");
				indentDown();
				writeTag("</td>");
				writeTag("<td>");
				indentUp();
				generateInputForPrimitive(type, path, readonly);
				indentDown();
				writeTag("</td>");
				indentDown();
				writeTag("</tr>");
			}

			/**
			 * 
			 */
			function generateRowForPrimitiveForAngular(type, path, name,
					readonly) {
				writeTag("<tr>");
				indentUp();
				writeTag("<td>");
				indentUp();
				writeTag("<label for='" + path + "Input'>" + generateLabel(name) + "</label>");
				indentDown();
				writeTag("</td>");
				writeTag("<td>");
				indentUp();
				generateInputForPrimitiveForAngular(type, path, readonly);
				indentDown();
				writeTag("</td>");
				indentDown();
				writeTag("</tr>");
			}

			/**
			 * 
			 */
			function generateInputForPrimitive(type, path, readonly) {
				m_utils.debug("Element " + path);
				m_utils.debug(type);

				var disabled = readonly ? "disabled" : "";

				if (type === "xsd:boolean") {
					writeTag("<input id='" + path
							+ "Input' type='checkbox' class='input' "
							+ disabled + ">");
				} else if (type === "xsd:int" || type === "xsd:integer") {
					writeTag("<input id='"
							+ path
							+ "Input' type='text' class='input integerInputField' "
							+ disabled + ">");
				} else {
					writeTag("<input id='"
							+ path
							+ "Input' type='text' class='input stringInputField' "
							+ disabled + ">");
				}
			}

			/**
			 * 
			 */
			function generateInputForPrimitiveForAngular(type, path, readonly) {
				m_utils.debug("Element " + path);
				m_utils.debug(type);

				var disabled = readonly ? "disabled" : "";

				if (type === "xsd:boolean") {
					writeTag("<input type='checkbox' class='input' " + disabled
							+ " ng-model='" + path + "'>");
				} else if (type === "xsd:int" || type === "xsd:integer") {
					writeTag("<input type='text' class='input integerInputField' "
							+ disabled + " ng-model='" + path + "'>");
				} else {
					writeTag("<input type='text' class='input stringInputField'"
							+ disabled + " ng-model='" + path + "'>");
				}
			}

			/**
			 * 
			 */
			function generateStructurePanel(parameterDefinition, readonly) {
				var typeDeclaration = m_model
						.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

				m_utils.debug("Type Declaration");
				m_utils.debug(typeDeclaration);

				writeTag("<h1>" + generateLabel(parameterDefinition.name) + "</h1>");
				writeTag("<table>");
				generateStructurePanelRecursively(typeDeclaration.model,
						typeDeclaration, parameterDefinition.id, readonly);
				writeTag("</table>");
			}

			/**
			 * 
			 */
			function generateStructurePanelRecursively(model, typeDeclaration,
					path, readonly) {
				jQuery.each(typeDeclaration.getBody().elements, function(i,
						element) {
					var type = element.type;

					// Strip prefix
					if (element.type.indexOf(':') !== -1) {
						type = element.type.split(":")[1];
					}

					var childTypeDeclaration = model
							.findTypeDeclarationBySchemaName(type);

					if (childTypeDeclaration != null) {
						if (childTypeDeclaration.isSequence()) {
							writeTag("<tr>");
							writeTag("<td colspan='2'>");
							writeTag("<h1>" + generateLabel(element.name) + "</h1>");
							writeTag("<table>");
							generateStructurePanelRecursively(model,
									childTypeDeclaration, path + "-"
											+ element.name, readonly);
							writeTag("</table>");
							writeTag("</td>");
							writeTag("</tr>");
						} else {
							generatorRowForEnumeration(childTypeDeclaration,
									path + "-" + element.name, element.name,
									readonly);
						}
					} else {
						generateRowForPrimitive(element.type, path + "-"
								+ element.name, element.name, readonly);
					}
				});
			}

			/**
			 * 
			 */
			function generateStructurePanelForAngular(parameterDefinition,
					readonly) {
				var typeDeclaration = m_model
						.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

				m_utils.debug("Type Declaration");
				m_utils.debug(typeDeclaration);

				writeTag("<h1>" + generateLabel(parameterDefinition.name) + "</h1>");
				writeTag("<table>");
				generateStructurePanelRecursivelyForAngular(
						typeDeclaration.model, typeDeclaration,
						parameterDefinition.id, readonly);
				writeTag("</table>");
			}

			function generateStructurePanelRecursivelyForAngular(model,
					typeDeclaration, path, readonly) {
				jQuery.each(typeDeclaration.getBody().elements, function(i,
						element) {
					var type = element.type;

					// Strip prefix
					if (element.type.indexOf(':') !== -1) {
						type = element.type.split(":")[1];
					}

					var childTypeDeclaration = model
							.findTypeDeclarationBySchemaName(type);

					if (childTypeDeclaration != null) {
						if (childTypeDeclaration.isSequence()) {
							writeTag("<tr>");
							writeTag("<td colspan='2'>");
							writeTag("<h1>" + generateLabel(element.name) + "</h1>");
							writeTag("<table>");
							generateStructurePanelRecursivelyForAngular(model,
									childTypeDeclaration, path + "."
											+ element.name, readonly);
							writeTag("</table>");
							writeTag("</td>");
							writeTag("</tr>");
						} else {
							generatorRowForEnumeration(childTypeDeclaration,
									path + "-" + element.name, element.name,
									readonly);
						}
					} else {
						generateRowForPrimitiveForAngular(element.type, path
								+ "." + element.name, element.name, readonly);
					}
				});
			}

			/**
			 * 
			 */
			function generatorRowForEnumeration(type, path, name, readonly) {
				writeTag("<tr>");
				indentUp();
				writeTag("<td>");
				indentUp();
				writeTag("<label for='" + path + "Input'>" + generateLabel(name) + "</label>");
				indentDown();
				writeTag("</td>");
				writeTag("<td>");
				indentUp();

				var disabled = readonly ? "disabled" : "";

				writeTag("<select id='" + path + "Input' " + disabled + ">");
				indentUp();

				for ( var enumerator in type.getFacets()) {
					writeTag("<option value='"
							+ type.getFacets()[enumerator].name + "'>"
							+ type.getFacets()[enumerator].name + "</option>");
				}

				indentDown();
				writeTag("</select>");
				indentDown();
				writeTag("</td>");
				indentDown();
				writeTag("</tr>");
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
			function generateLabel(identifier) {
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