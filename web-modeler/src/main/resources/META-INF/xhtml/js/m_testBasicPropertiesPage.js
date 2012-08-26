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
				"m_dialog", "m_basicPropertiesPage", "m_dataTraversal" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_dialog, m_basicPropertiesPage, m_dataTraversal) {
			return {
				create : function(propertiesPanel) {
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
								.push("Test szmbol name must not be empty.");
						this.nameInput.addClass("error");

						this.propertiesPanel.showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});