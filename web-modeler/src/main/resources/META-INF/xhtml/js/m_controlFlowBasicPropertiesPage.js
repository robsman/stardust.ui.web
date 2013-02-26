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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_basicPropertiesPage", "bpm-modeler/js/m_dataTraversal", "bpm-modeler/js/m_codeEditorAce" ],
		function(m_utils, m_constants, m_basicPropertiesPage, m_dataTraversal, m_codeEditorAce) {
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

					// TODO - ace code editor doesn't have refresh at present
					//this.conditionExpressionInputEditor.refresh();

					// TODO - ace code editor doesn't have code complete at present
					// Global variables for Code Editor auto-complete / validation
					//var globalVariables = m_dataTraversal.getAllDataAsJavaScriptObjects(this.propertiesPanel.diagram.model);
					//this.conditionExpressionInputEditor.setGlobalVariables(globalVariables);
				};

				/**
				 *
				 */
				ControlFlowBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();
					this.otherwiseInput = this.mapInputId("otherwiseInput");
					this.conditionExpressionDiv = jQuery("#"
							+ this.propertiesPanel.id
							+ " #conditionExpressionDiv");

					this.descriptionInput = this.mapInputId("descriptionInput");
					this.conditionPanel = this.mapInputId("conditionPanel");

					var page = this;
					this.conditionExpressionInputEditor = m_codeEditorAce.getJSCodeEditor("conditionExpressionDiv");
					this.conditionExpressionInputEditor.getEditor().on('blur', function(e){
						var property = "conditionExpression";
						if (!page.validate()) {
							return;
						}

						if (page.getModelElement()[property] != page.conditionExpressionInputEditor.getValue()) {
							page.submitChanges(page
									.assembleChangedObjectFromProperty(
											property, page.conditionExpressionInputEditor.getValue()));
						}
					});

					this.registerInputForModelElementChangeSubmission(
							this.descriptionInput, "description");
					this.registerCheckboxInputForModelElementChangeSubmission(
							this.otherwiseInput, "otherwise");
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
						this.conditionExpressionInputEditor
								.setValue(this.propertiesPanel.element.modelElement.conditionExpression);

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