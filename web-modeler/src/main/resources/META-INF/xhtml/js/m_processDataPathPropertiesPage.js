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
				"m_propertiesPage",
				"m_parameterDefinitionsPanel" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_propertiesPage, m_parameterDefinitionsPanel) {
			return {
				create : function(propertiesPanel) {
					return new ProcessDataPathPropertiesPage(propertiesPanel);
				}
			};

			function ProcessDataPathPropertiesPage(newPropertiesPanel, newId,
					newTitle) {
				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "dataPathPropertiesPage",
						"DataPath",
						"../../images/icons/data-path-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ProcessDataPathPropertiesPage.prototype,
						propertiesPage);

				this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
				.create({
					scope : "dataPathPropertiesPage",
					submitHandler : this,
					supportsOrdering : true,
					supportsDataMappings : true,
					supportsDataPathes : true,
					supportsDescriptors : true,
					supportsDataTypeSelection : false
				});
				
				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.setElement = function() {
					this.parameterDefinitionsPanel.setScopeModel(this
							.getModelElement().model);
					this.parameterDefinitionsPanel.setParameterDefinitions(this
							.getModelElement().dataPathes);
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element;
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();
//					this.dataPathNameInput.removeClass("error");
//
//					if (this.dataPathNameInput.val() == null
//							|| this.dataPathNameInput.val() == "") {
//						this.propertiesPanel.errorMessages
//								.push("Data Path name must not be empty.");
//						this.dataPathNameInput.addClass("error");
//						this.dataPathNameInput.focus();
//						this.propertiesPanel.showErrorMessages();
//
//						return false;
//					} else {
//						for ( var n = 0; n < this.getModelElement().dataPathes.length; ++n) {
//							if (this.getModelElement().dataPathes[n].name == this.dataPathNameInput
//									.val()) {
//								this.propertiesPanel.errorMessages
//										.push("Duplicate Data Path name \""
//												+ this.dataPathNameInput.val()
//												+ "\"");
//								this.dataPathNameInput.addClass("error");
//								this.dataPathNameInput.focus();
//								this.propertiesPanel.showErrorMessages();
//
//								return false;
//							}
//						}
//					}

					return true;
				};
				
				/**
				 * Callback for parameterDefinitionsPanel. 
				 */
				ProcessDataPathPropertiesPage.prototype.submitParameterDefinitionsChanges = function(dataPathes)
				{
					this.propertiesPanel.submitChanges({"dataPathes": dataPathes});
				};
			}
		});