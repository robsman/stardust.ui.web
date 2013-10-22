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
						"plugins/bpm-modeler/images/icons/table.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ConfigurationVariablesPropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				ConfigurationVariablesPropertiesPage.prototype.initialize = function() {
					m_utils.jQuerySelect("#cofigurationVariableHeading")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.title"));
					m_utils.jQuerySelect("th#name")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.tableHeading.name"));
					m_utils.jQuerySelect("th#defaultValue")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.tableHeading.defaultValue"));
					m_utils.jQuerySelect("th#description")
					.text(
							m_i18nUtils
									.getProperty("modeler.propertyView.modelView.configurationVariables.tableHeading.description"));
					m_utils.jQuerySelect("th#references")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.tableHeading.references"));

					m_utils.jQuerySelect("label#deleteVariableDialogMsg")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.message"));
					m_utils.jQuerySelect("label[for='emptyLiteralRadio']")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.replaceWithEmptyLiteral"));
					m_utils.jQuerySelect("label[for='defaultValueRadio']")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.replaceWithDefaultValue"));
					m_utils.jQuerySelect("label[for='withLiteralRadio']")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.replaceWithArbitaryVariable"));
					m_utils.jQuerySelect("input#applyButton")
							.val(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.applyButtonText"));
					m_utils.jQuerySelect("input#closeButton")
							.val(
									m_i18nUtils
											.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.closeButtonText"));

					this.refreshConfigurationVariablesButton = m_utils.jQuerySelect("#refreshConfigurationVariablesButton");
					this.refreshConfigurationVariablesButton.click({
						"page" : this
					}, function(event) {
						event.data.page.refreshConfigurationVariables();
					});

					m_utils.jQuerySelect("#deleteConfigurationVariableDialog").dialog({
						autoOpen : false,
						draggable : true,
						title : m_i18nUtils.getProperty("modeler.propertyView.modelView.configurationVariables.deleteDialog.title"),
						width : "auto",
						height : "auto",
						open : function() {
							m_utils.jQuerySelect(
									"#deleteConfigurationVariableDialog #emptyLiteralRadio")
									.prop("checked", true);
							m_utils.jQuerySelect(
									"#deleteConfigurationVariableDialog #literalValueInput")
									.val("");
							m_utils.jQuerySelect(
									"#deleteConfigurationVariableDialog #literalValueInput")
									.prop('disabled', true);
						}
					});

					m_utils.jQuerySelect(
							"#deleteConfigurationVariableDialog input[name='deleteVariableOptions']")
							.click(
									function() {
										if (m_utils.jQuerySelect(
												"#deleteConfigurationVariableDialog #withLiteralRadio")
												.prop("checked")) {
											m_utils.jQuerySelect(
													"#deleteConfigurationVariableDialog #literalValueInput")
													.prop('disabled', false).focus();

										} else {
											m_utils.jQuerySelect(
													"#deleteConfigurationVariableDialog #literalValueInput")
													.val("");
											m_utils.jQuerySelect(
													"#deleteConfigurationVariableDialog #literalValueInput")
													.prop('disabled', true);
										}
									});

					m_utils.jQuerySelect("#deleteConfigurationVariableDialog #closeButton")
							.click(
									function() {
										m_utils.jQuerySelect(
												"#deleteConfigurationVariableDialog")
												.dialog("close");
									});
					m_utils.jQuerySelect("#deleteConfigurationVariableDialog #applyButton")
							.click(
									{
										page : this
									},
									function(event) {
										m_utils.debug("Apply");
										m_utils
												.debug(event.data.page.currentConfigurationVariable.name);

										var deleteOptions = "{}";

										if (m_utils.jQuerySelect(
												"#deleteConfigurationVariableDialog #emptyLiteralRadio")
												.prop("checked")) {
											deleteOptions = {
												mode : "emptyLiteral"
											};
										} else if (m_utils.jQuerySelect(
												"#deleteConfigurationVariableDialog #defaultValueRadio")
												.prop("checked")) {
											deleteOptions = {
												mode : "defaultValue"
											};
										} else if (m_utils.jQuerySelect(
												"#deleteConfigurationVariableDialog #withLiteralRadio")
												.prop("checked")) {
											deleteOptions = {
												mode : "withLiteral",
												literalValue : m_utils.jQuerySelect(
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
															m_utils.jQuerySelect(
																	"#deleteConfigurationVariableDialog")
																	.dialog(
																			"close");
															event.data.page.refreshConfigurationVariables();
														}).fail(function(e) {
															alert("Delete failed: " + e);
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
											page.refreshConfigurationVariablesTable(json);
										},
										"error" : function() {
											m_utils.debug("Error");
										}
									});
				};
				
				/**
				 * 
				 */
				ConfigurationVariablesPropertiesPage.prototype.refreshConfigurationVariablesTable = function(json) {
					m_utils.jQuerySelect(
							"table#configurationVariablesTable tbody")
							.empty();

					var variables = json;

					for ( var n = 0; n < variables.length; ++n) {
						var row = m_utils.jQuerySelect("<tr></tr>");
						var cell = m_utils.jQuerySelect("<td></td>");

						row.append(cell);

						var button = m_utils.jQuerySelect("<input type='image' title='Delete' alt='Delete' class='toolbarButton' src='"
								+ m_urlUtils
										.getContextName()
								+ "/plugins/bpm-modeler/images/icons/delete.png'/>");

						button
								.click(
										{
											page : this,
											configurationVariable : variables[n]
										},
										function(event) {
											event.data.page.currentConfigurationVariable = event.data.configurationVariable;

											m_utils.jQuerySelect(
													"#deleteConfigurationVariableDialog #emptyLiteralRadio")
													.prop(
															"checked",
															true);
											m_utils.jQuerySelect(
													"#deleteConfigurationVariableDialog")
													.dialog(
															"open");
										});

						cell.append(button);

						cell = m_utils.jQuerySelect("<td></td>");

						row.append(cell);

						cell
								.append(stripVariableName(variables[n].name));

						cell = m_utils.jQuerySelect("<td></td>");

						row.append(cell);

						input = m_utils.jQuerySelect("<input type='text' class='cellEditor'></input>");

						cell.append(input);
						input
								.val(variables[n].defaultValue);
						input
								.change(
										{
											page : this,
											variableName : variables[n].name
										},
										function(event) {
											event.data.page
													.modifyConfigurationVariable(
															event.data.variableName,
															m_utils.jQuerySelect(
																	this)
																	.val());
										});

						cell = m_utils.jQuerySelect("<td></td>");

						row.append(cell);

						input = m_utils.jQuerySelect("<input type='text' class='cellEditor'></input>");

						cell.append(input);
						input
								.val(variables[n].description);
						input.prop('title', variables[n].description);
						input
								.change(
										{
											page : this,
											variableName : variables[n].name
										},
										function(event) {
											event.data.page
													.modifyConfigurationVarDescription(
															event.data.variableName,
															m_utils.jQuerySelect(
																	this)
																	.val());
										});
						
						cell = m_utils.jQuerySelect("<td></td>");
						
						row.append(cell);

						var list = m_utils.jQuerySelect("<ul class='referencesList'></ul>");

						cell.append(list);

						for ( var m = 0; m < variables[n].references.length; ++m) {
							var item = m_utils.jQuerySelect("<li></li>");

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
						
						var showMoreLink = m_utils
								.jQuerySelect('<li class="show_more"><a class="configLink" style="text-decoration:none">'
										+ m_i18nUtils
												.getProperty("modeler.propertyView.modelView.configurationVariables.showAllReferences")
										+ '</a></li>');
						var showLessLink = m_utils
								.jQuerySelect('<li class="show_less"><a class="configLink" style="text-decoration:none">'
										+ m_i18nUtils
												.getProperty("modeler.propertyView.modelView.configurationVariables.collapseReferences")
										+ '</a></li>');
						showMoreLink.click({
							currList : list
						}, function(event) {
							event.data.currList.children().show();
							event.data.currList.find('li.show_more').hide();
						});
						

						showLessLink.click({
							currList : list
						}, function(event) {
							event.data.currList.find('li:gt('+ (m_constants.CONFIG_VAR_REF_LIMIT - 1)+')').hide();
							event.data.currList.find('li.show_more').show();
							
						});
						
						if (list.children().size() > m_constants.CONFIG_VAR_REF_LIMIT) {
							var lastElement=list.find('li:gt('+ (m_constants.CONFIG_VAR_REF_LIMIT - 1)+')').hide().end();
							lastElement.append(showMoreLink);
							list.append(showLessLink);
							list.find('li.show_less').hide();
						}
						 
						m_utils.jQuerySelect(
								"table#configurationVariablesTable tbody")
								.append(row);
					}
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
				
				/**
				 *
				 */
				ConfigurationVariablesPropertiesPage.prototype.modifyConfigurationVarDescription = function(
						variableName, description) {
					m_communicationController.postData({
						url : m_communicationController.getEndpointUrl()
								+ "/models/" + encodeURIComponent(this.getModel().id)
								+ "/configurationVariables/" + variableName
					}, JSON.stringify({
						variableName : variableName,
						description : description
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