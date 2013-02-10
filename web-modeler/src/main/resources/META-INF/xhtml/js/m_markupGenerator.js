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
				generateMarkup : generateMarkup
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
								parameterDefinition.name,
								readonly);
					} else if (parameterDefinition.dataType == "struct") {
						writeTag("<tr>");
						indentUp();
						writeTag("<td>");
						generateStructurePanel(parameterDefinition, readonly);
						writeTag("</td>");
						indentDown();
						writeTag("</tr>");
					} else {
						// Deal
						// other types
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
				writeTag("<label for='" + path + "Input'>" + name + "</label>");
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
			function generateInputForPrimitive(type, path, readonly) {
				m_utils.debug("Element " + path);
				m_utils.debug(type);

				var disabled = readonly ? "disabled" : "";
				
				if (type === "xsd:boolean") {
					writeTag("<input id='" + path
							+ "Input' type='checkbox' class='input' " + disabled + ">");
				} else if (type === "xsd:int" || type === "xsd:integer") {
					writeTag("<input id='"
							+ path
							+ "Input' type='text' class='input integerInputField' " + disabled + ">");
				} else {
					writeTag("<input id='"
							+ path
							+ "Input' type='text' class='input stringInputField' " + disabled + ">");
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

				writeTag("<h1>" + parameterDefinition.name + "</h1>");
				writeTag("<table>");
				generateStructurePanelRecursively(typeDeclaration.model,
						typeDeclaration, parameterDefinition.id, readonly);
				writeTag("</table>");
			}

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
							writeTag("<h1>" + element.name + "</h1>");
							writeTag("<table>");
							generateStructurePanelRecursively(model,
									childTypeDeclaration, path + "-"
											+ element.name, readonly);
							writeTag("</table>");
							writeTag("</td>");
							writeTag("</tr>");
						} else {
							generatorRowForEnumeration(childTypeDeclaration,
									path + "-" + element.name, element.name, readonly);
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
			function generatorRowForEnumeration(type, path, name, readonly) {
				writeTag("<tr>");
				indentUp();
				writeTag("<td>");
				indentUp();
				writeTag("<label for='" + path + "Input'>" + name + "</label>");
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
		});