/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_basicPropertiesPage", "m_dataTraversal", "m_codeEditor" ],
		function(m_utils, m_constants, m_basicPropertiesPage, m_dataTraversal, m_codeEditor) {
			return {
				create : function(propertiesPanel) {
					var page = new ControlFlowBasicPropertiesPage(
							propertiesPanel);

					page.initialize();

					return page;
				}
			};

			/**
			 *
			 */
			function ControlFlowBasicPropertiesPage(propertiesPanel) {
				var propertiesPage = m_basicPropertiesPage.create(propertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ControlFlowBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * Override base class PropertiesPage#show() method so that codeEditor.refresh() can be called
				 */
				ControlFlowBasicPropertiesPage.prototype.show = function() {
					propertiesPage.show();
					this.conditionExpressionInputEditor.refresh();

					// Global variables for Code Editor auto-complete / validation
					var globalVariables = m_dataTraversal.getAllDataAsJavaScriptObjects(this.propertiesPanel.diagram.model);
					this.conditionExpressionInputEditor.setGlobalVariables(globalVariables);
				};

				/**
				 *
				 */
				ControlFlowBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();
					this.otherwiseInput = this.mapInputId("otherwiseInput");
					this.conditionExpressionInput = this
							.mapInputId("conditionExpressionInput");
					this.descriptionInput = this.mapInputId("descriptionInput");
					this.conditionPanel = this.mapInputId("conditionPanel");

					this.conditionExpressionInputEditor = m_codeEditor.getCodeEditor(this.conditionExpressionInput[0]);

					this.registerInputForModelElementChangeSubmission(
							this.descriptionInput, "description");
					this.registerCheckboxInputForModelElementChangeSubmission(
							this.otherwiseInput, "otherwise");
					this.registerInputForModelElementChangeSubmission(
							this.conditionExpressionInput,
							"conditionExpression");
				};

				/**
				 *
				 */
				ControlFlowBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();
					this.descriptionInput
							.val(this.propertiesPanel.element.modelElement.description);

					if (this.propertiesPanel.element.allowsCondition()) {
						this.otherwiseInput
								.attr(
										"checked",
										this.propertiesPanel.element.modelElement.otherwise);
						this.conditionExpressionInput
								.val(this.propertiesPanel.element.modelElement.conditionExpression);
						this.conditionExpressionInputEditor.setValue(this.conditionExpressionInput.val());

						if (this.propertiesPanel.element.modelElement.otherwise) {
							this.conditionExpressionInputEditor.disable();
							this.setTitle("Default Sequence Flow");
						} else {
							this.conditionExpressionInputEditor.enable();
							this.setTitle("Conditional Sequence Flow");
						}

						this.conditionPanel.removeAttr("class");
					} else {
						this.conditionPanel.attr("class", "invisible");
						this.setTitle("Sequence Flow");
					}
				};

				/**
				 *
				 */
				ControlFlowBasicPropertiesPage.prototype.setTitle = function(title) {
					if (title) {
						jQuery("#controlFlowPropertiesPanel div.propertiesPanelTitle").text(title);
					}
				};
			}
		});