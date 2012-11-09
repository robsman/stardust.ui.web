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
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_dialog", "m_basicPropertiesPage", "m_dataTraversal", "m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_dialog, m_basicPropertiesPage, m_dataTraversal, m_i18nUtils) {
			return {
				create : function(propertiesPanel) {
					// I18N static labels on the page
					i18nStaticLabels();
					var page = new TestBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			/**
			 *
			 */
			function TestBasicPropertiesPage(propertiesPanel) {
				var propertiesPage = m_basicPropertiesPage
						.create(propertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(TestBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				TestBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();
				};

				/**
				 *
				 */
				TestBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();
				};

				/**
				 *
				 */
				TestBasicPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();
					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.propertiesPanel.errorMessages
								.push("Test symbol name must not be empty.");
						this.nameInput.addClass("error");

						this.propertiesPanel.showErrorMessages();

						return false;
					}

					return true;
				};
			}
			/**
			 * 
			 */
			function i18nStaticLabels() {
				jQuery("#inputData").html(m_i18nUtils.getProperty("modeller.element.properties.testProperties.inputData"));
				jQuery("#outputData").html(m_i18nUtils.getProperty("modeller.element.properties.testProperties.outputData"));
			}
			;
		});