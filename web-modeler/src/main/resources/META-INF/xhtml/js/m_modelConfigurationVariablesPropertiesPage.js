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
				"bpm-modeler/js/m_session",
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_session, m_communicationController,
				m_commandsController, m_command, m_propertiesPage, m_i18nUtils) {
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
					this.refreshConfigurationVariablesButton = jQuery("#refreshConfigurationVariablesButton");
					this.refreshConfigurationVariablesButton.click({
						"view" : this
					}, function(event) {
						event.data.view.refreshConfigurationVariables();
					});
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
												+ this.getModel().id
												+ "/configurationVariables"
									},
									{
										"success" : function(json) {
											for ( var n = 0; n < json.length; ++n) {
												var row = jQuery("<tr></tr>");
												var cell = jQuery("<td></td>");

												row.append(cell);
												cell.append(json[n].name);

												cell = jQuery("<td></td>");

												row.append(cell);

												input = jQuery("<input type='text' class='cellEditor'></input>");

												cell.append(input);
												input.val(json[n].defaultValue);
												input
														.change(
																{
																	page : page,
																	variableName : json[n].name
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

												for ( var m = 0; m < json[n].references.length; ++m) {
													var item = jQuery("<li></li>");

													list.append(item);

													var info = "";

													info += json[n].references[m].elementType; // I18N

													if (json[n].references[m].elementName) {
														info += " <span class='emphasis'>";
														info += json[n].references[m].elementName;
														info += "</span>";
													}

													if (json[n].references[m].scopeType) {
														info += " of "; // I18N
														info += json[n].references[m].scopeType; // I18N

														if (json[n].references[m].scopeType) {
															info += " <span class='emphasis'>";
															info += json[n].references[m].scopeName;
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
								+ "/models/" + this.getModel().id
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
		});