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
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_propertiesPage, m_i18nUtils) {
			return {
				create : function(propertiesPanel) {
					var page = new ProcessRuntimePropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			/**
			 *
			 */
			function ProcessRuntimePropertiesPage(propertiesPanel) {
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "processRuntimePropertiesPage",
						m_i18nUtils
						.getProperty("modeler.processDefinition.propertyPages.runtimeBehavior.heading"),
						"plugins/bpm-modeler/images/icons/process-runtime-settings.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ProcessRuntimePropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				ProcessRuntimePropertiesPage.prototype.initialize = function() {
					this.persistenceSelect = this
							.mapInputId("persistenceSelect");

					jQuery("label[for='persistenceSelect']")
					.text(
							m_i18nUtils
									.getProperty("modeler.processDefinition.propertyPages.runtimeBehavior.persistenceSelect.label"));

					this.registerInputForModelElementAttributeChangeSubmission(
							this.persistenceSelect,
							"carnot:engine:auditTrailPersistence");
				};

				/**
				 *
				 */
				ProcessRuntimePropertiesPage.prototype.setElement = function() {
					this.persistenceSelect
							.val(this.getModelElement().attributes["carnot:engine:auditTrailPersistence"]);
				};

				/**
				 *
				 */
				ProcessRuntimePropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();

					return true;
				};
			}
		});