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
		[ "m_utils", "m_constants", "m_basicPropertiesPage", "m_dataTraversal" ],
		function(m_utils, m_constants, m_basicPropertiesPage, m_dataTraversal) {
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

					// Bind the Model Data as top "window" level objects to be used for Code Editor auto-complete
					var globalVariables = m_dataTraversal.getAllDataAsJavaScriptObjects(this.propertiesPanel.diagram.model);
					for (var key in globalVariables) {
						window[key] = globalVariables[key];
					}
				};

				/**
				 *
				 */
				ControlFlowBasicPropertiesPage.prototype.initialize = function() {
					this.otherwiseInput = this.mapInputId("otherwiseInput");
					this.conditionExpressionInput = this
							.mapInputId("conditionExpressionInput");
					this.descriptionInput = this.mapInputId("descriptionInput");
					this.conditionPanel = this.mapInputId("conditionPanel");

					// Set up code editor for JS code expression
					CodeMirror.commands.autocomplete = function(cm) {
						CodeMirror.simpleHint(cm, CodeMirror.javascriptHint);
					}

					var editor = CodeMirror.fromTextArea(this.conditionExpressionInput[0], {
						mode: "javascript",
						theme: "eclipse",
						lineNumbers: true,
						lineWrapping: true,
						indentUnit: 3,
						matchBrackets: true,
						extraKeys: {"Ctrl-Space": "autocomplete"},
						onCursorActivity: function() {
							// Highlight selected text
							editor.matchHighlight("CodeMirror-matchhighlight");
							// Set active line
							editor.setLineClass(hlLine, null, null);
							hlLine = editor.setLineClass(editor.getCursor().line, null, "activeline");
						},
						onBlur: function() {
							editor.save();
							// Programmatically invoke the change handler on the hidden text area
							// as it will not be invoked automatically
							jQuery(editor.getTextArea()).change();
						}
					});
					var hlLine = editor.setLineClass(0, "activeline");
					this.conditionExpressionInputEditor = editor;

					this.otherwiseInput.click({
						"page" : this
					}, function(event) {
						if (event.data.page.otherwiseInput.is(":checked")) {
							event.data.page.conditionExpressionInput.val("");
						} else {
							event.data.page.conditionExpressionInput.val("true");
						}
						// Programmatically invoke the change handler on the hidden text area
						// as it will not be invoked automatically
						event.data.page.conditionExpressionInput.change();

						// TODO: Review - below statements are probably not necessary
						// as the code editor will be refreshed in setElement()
						// event.data.page.conditionExpressionInputEditor.setValue(event.data.page.conditionExpressionInput.val());
						// event.data.page.conditionExpressionInputEditor.refresh();
					});

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
					this.descriptionInput
							.val(this.propertiesPanel.element.modelElement.description);

					if (this.propertiesPanel.element.allowsCondition()) {
						var editor = this.conditionExpressionInputEditor;

						this.otherwiseInput
								.attr(
										"checked",
										this.propertiesPanel.element.modelElement.otherwise);
						this.conditionExpressionInput
								.val(this.propertiesPanel.element.modelElement.conditionExpression);
						editor.setValue(this.conditionExpressionInput.val());

						var editorWrapperNode = editor.getWrapperElement();
						if (this.propertiesPanel.element.modelElement.otherwise) {
							editor.setOption("readOnly", "nocursor");
							jQuery(editorWrapperNode).addClass("CodeMirror-disabled");
							this.setTitle("Default Sequence Flow");
						} else {
							editor.setOption("readOnly", false);
							jQuery(editorWrapperNode).removeClass("CodeMirror-disabled");
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