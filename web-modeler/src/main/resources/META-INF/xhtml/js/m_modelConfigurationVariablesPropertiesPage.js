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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_session", "bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage", "bpm-modeler/js/m_i18nUtils"  ],
		function(m_utils, m_constants, m_session, m_communicationController, m_commandsController,
				m_command, m_propertiesPage, m_i18nUtils) {
			return {
				create : function(propertiesPanel, id) {
					var page = new ConfigurationVariablesPropertiesPage(propertiesPanel,
							id);

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
				m_utils.inheritMethods(ConfigurationVariablesPropertiesPage.prototype,
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
									m_utils.debug("Success");
								},
								"error" : function() {
									m_utils.debug("Error");
								}
							});

				};
			}
		});