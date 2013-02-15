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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_session", "bpm-modeler/js/m_urlUtils",
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_session, m_urlUtils,
				m_communicationController, m_commandsController, m_command,
				m_propertiesPage, m_i18nUtils) {
			return {
				create : function(propertiesPanel, id) {
					var page = new ConfigurationVariablesPropertiesPage(
							propertiesPanel, id);

					page.initialize();

					return page;
				}
			};

			/**
			 *
			 */
			function ConfigurationVariablesPropertiesPage(propertiesPanel, id) {
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, id, "Configuration Variables",
						"../../images/icons/basic-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ConfigurationVariablesPropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				ConfigurationVariablesPropertiesPage.prototype.initialize = function() {
					jQuery("#cofigurationVariableHeading")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.title"));
					jQuery("th#name")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.tableHeading.name"));
					jQuery("th#defaultValue")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.tableHeading.defaultValue"));
					jQuery("th#references")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.tableHeading.references"));

					jQuery("label#deleteVariableDialogMsg")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.message"));
					jQuery("label[for='emptyLiteralRadio']")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.replaceWithEmptyLiteral"));
					jQuery("label[for='defaultValueRadio']")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.replaceWithDefaultValue"));
					jQuery("label[for='withLiteralRadio']")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.replaceWithArbitaryVariable"));
					jQuery("input#applyButton")
							.val(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.applyButtonText"));
					jQuery("input#closeButton")
							.val(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.closeButtonText"));

					this.refreshConfigurationVariablesButton = jQuery("#refreshConfigurationVariablesButton");
					this.refreshConfigurationVariablesButton.click({
						"page" : this
					}, function(event) {
						event.data.page.refreshConfigurationVariables();
					});

					jQuery("#deleteConfigurationVariableDialog").dialog({
						autoOpen : false,
						draggable : true,
						title : m_i18nUtils.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.title"),
						width : "auto",
						height : "auto",
						open : function() {
							jQuery(
									"#deleteConfigurationVariableDialog #emptyLiteralRadio")
									.prop("checked", true);
							jQuery(
									"#deleteConfigurationVariableDialog #literalValueInput")
									.val("");
							jQuery(
									"#deleteConfigurationVariableDialog #literalValueInput")
									.prop('disabled', true);
						}
					});

					jQuery(
							"#deleteConfigurationVariableDialog input[name='deleteVariableOptions']")
							.click(
									function() {
										if (jQuery(
												"#deleteConfigurationVariableDialog #withLiteralRadio")
												.prop("checked")) {
											jQuery(
													"#deleteConfigurationVariableDialog #literalValueInput")
													.prop('disabled', false).focus();

										} else {
											jQuery(
													"#deleteConfigurationVariableDialog #literalValueInput")
													.val("");
											jQuery(
													"#deleteConfigurationVariableDialog #literalValueInput")
													.prop('disabled', true);
										}
									});

					jQuery("#deleteConfigurationVariableDialog #closeButton")
							.click(
									function() {
										jQuery(
												"#deleteConfigurationVariableDialog")
												.dialog("close");
									});
					jQuery("#deleteConfigurationVariableDialog #applyButton")
							.click(
									{
										page : this
									},
									function(event) {
										m_utils.debug("Apply");
										m_utils
												.debug(event.data.page.currentConfigurationVariable.name);

										var deleteOptions = "{}";

										if (jQuery(
												"#deleteConfigurationVariableDialog #emptyLiteralRadio")
												.prop("checked")) {
											deleteOptions = {
												mode : "emptyLiteral"
											};
										} else if (jQuery(
												"#deleteConfigurationVariableDialog #defaultValueRadio")
												.prop("checked")) {
											deleteOptions = {
												mode : "defaultValue"
											};
										} else if (jQuery(
												"#deleteConfigurationVariableDialog #withLiteralRadio")
												.prop("checked")) {
											deleteOptions = {
												mode : "withLiteral",
												literalValue : jQuery(
														"#deleteConfigurationVariableDialog #literalValueInput")
														.val()
											};
										}

										m_utils
												.debug("Configuration Variable:");
										m_utils
												.debug(event.data.page.currentConfigurationVariable);
										m_utils.debug(deleteOptions);

										jQuery
												.ajax(
														{
															type : "DELETE",
															url : m_urlUtils
																	.getModelerEndpointUrl()
																	+ "/models/"
																	+ encodeURIComponent(event.data.page.getModel().id)
																	+ "/configurationVariables/"
																	+ event.data.page.currentConfigurationVariable.name,
															contentType : "application/json",
															data : JSON
																	.stringify(deleteOptions)
														})
												.done(
														function() {
															jQuery(
																	"#deleteConfigurationVariableDialog")
																	.dialog(
																			"close");
														}).fail(function() {
												});
									});
				};

				/**
				 *
				 */
				ConfigurationVariablesPropertiesPage.prototype.setElement = function() {
					this.refreshConfigurationVariables();
				};

				/**
				 *
				 */
				ConfigurationVariablesPropertiesPage.prototype.refreshConfigurationVariables = function() {
					var page = this;

					m_communicationController
							.syncGetData(
									{
										url : m_communicationController
												.getEndpointUrl()
												+ "/models/"
												+ encodeURIComponent(this.getModel().id)
												+ "/configurationVariables"
									},
									{
										"success" : function(json) {
											jQuery(
													"table#configurationVariablesTable tbody")
													.empty();

											var variables = m_utils
													.convertToSortedArray(json,
															"name", false);

											for ( var n = 0; n < variables.length; ++n) {
												var row = jQuery("<tr></tr>");
												var cell = jQuery("<td></td>");

												row.append(cell);

												var button = jQuery("<input type='image' title='Delete' alt='Delete' class='toolbarButton' src='"
														+ m_urlUtils
																.getContextName()
														+ "/plugins/bpm-modeler/images/icons/delete.png'/>");

												button
														.click(
																{
																	page : page,
																	configurationVariable : variables[n]
																},
																function(event) {
																	event.data.page.currentConfigurationVariable = event.data.configurationVariable;

																	jQuery(
																			"#deleteConfigurationVariableDialog #emptyLiteralRadio")
																			.prop(
																					"checked",
																					true);
																	jQuery(
																			"#deleteConfigurationVariableDialog")
																			.dialog(
																					"open");
																});

												cell.append(button);

												cell = jQuery("<td></td>");

												row.append(cell);

												cell
														.append(stripVariableName(variables[n].name));

												cell = jQuery("<td></td>");

												row.append(cell);

												input = jQuery("<input type='text' class='cellEditor'></input>");

												cell.append(input);
												input
														.val(variables[n].defaultValue);
												input
														.change(
																{
																	page : page,
																	variableName : variables[n].name
																},
																function(event) {
																	event.data.page
																			.modifyConfigurationVariable(
																					event.data.variableName,
																					jQuery(
																							this)
																							.val());
																});

												cell = jQuery("<td></td>");

												row.append(cell);

												var list = jQuery("<ul class='referencesList'></ul>");

												cell.append(list);

												for ( var m = 0; m < variables[n].references.length; ++m) {
													var item = jQuery("<li></li>");

													list.append(item);

													var info = "";

													info +=  m_i18nUtils.getProperty("modeler.propertyView.modelView.configurationVariables.elementType." + variables[n].references[m].elementType);

													if (variables[n].references[m].elementName) {
														info += " <span class='emphasis'>";
														info += variables[n].references[m].elementName;
														info += "</span>";
													}

													if (variables[n].references[m].scopeType) {
														info += " " + m_i18nUtils.getProperty("modeler.general.ofLiteral") + " ";
														info += m_i18nUtils.getProperty("modeler.propertyView.modelView.configurationVariables.scopeType." + variables[n].references[m].scopeType);

														if (variables[n].references[m].scopeType) {
															info += " <span class='emphasis'>";
															info += variables[n].references[m].scopeName;
															info += "</span>";
														}
													}

													item.append(info);
												}

												jQuery(
														"table#configurationVariablesTable tbody")
														.append(row);
											}
										},
										"error" : function() {
											m_utils.debug("Error");
										}
									});
				};

				/**
				 *
				 */
				ConfigurationVariablesPropertiesPage.prototype.modifyConfigurationVariable = function(
						variableName, defaultValue) {
					m_communicationController.postData({
						url : m_communicationController.getEndpointUrl()
								+ "/models/" + encodeURIComponent(this.getModel().id)
								+ "/configurationVariables/" + variableName
					}, JSON.stringify({
						variableName : variableName,
						defaultValue : defaultValue
					}), {
						"success" : function() {
						},
						"error" : function() {
							m_utils.debug("Error");
						}
					});
				};
			}

			/**
			 *
			 */
			function stripVariableName(fullName) {
				return fullName.substring(fullName.indexOf("${") + 2, fullName
						.indexOf("}"));
			}
		});