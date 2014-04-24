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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", 
		  "bpm-modeler/js/m_basicPropertiesPage", "bpm-modeler/js/m_dataTraversal", 
		  "bpm-modeler/js/m_codeEditorAce","bpm-modeler/js/m_parsingUtils",
		  "bpm-modeler/js/m_autoCompleters","bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_basicPropertiesPage, m_dataTraversal, 
				 m_codeEditorAce,m_parsingUtils,m_autoCompleters,m_i18nUtils) {
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
				this.propertiesPage = m_basicPropertiesPage.create(propertiesPanel);

				m_utils.inheritFields(this, this.propertiesPage);
				m_utils.inheritMethods(
						ControlFlowBasicPropertiesPage.prototype,
						this.propertiesPage);

				/**
				 * Override base class PropertiesPage#show() method so that codeEditor.refresh() can be called
				 */
				ControlFlowBasicPropertiesPage.prototype.show = function() {
					var key,
						temp,
						completerStrings=[];
					
					this.propertiesPage.show();

					// TODO - ace code editor doesn't have refresh at present
					//this.conditionExpressionInputEditor.refresh();
					
					/*Extract our data as JS objects and parse them into unique dot-delimited string paths which
					 *we will supply to our editors session for use by autoCompleters.*/
					var globalVariables = m_dataTraversal.getAllDataAsJavaScriptObjects(this.propertiesPanel.diagram.model);
					for(key in globalVariables){
						if(globalVariables.hasOwnProperty(key)){
							temp=globalVariables[key];
							if(typeof(temp)==="object"){
								completerStrings=completerStrings.concat(m_parsingUtils.parseJSObjToStringFrags(temp,key));
							}else{
								completerStrings.push(key);
							}
						}
					}
					this.conditionExpressionInputEditor.setSessionData("$keywordList",completerStrings);
				};

				/**
				 *
				 */
				ControlFlowBasicPropertiesPage.prototype.initialize = function() {
					var typeKey;
					var typeDecl;
					var completerStrings;
					var that=this;
					this.initializeBasicPropertiesPage();
					this.otherwiseInput = this.mapInputId("otherwiseInput");
					
					this.conditionExpressionDiv = m_utils.jQuerySelect("#" + this.propertiesPanel.id + " [id^='conditionExpressionDiv']").get(0);
					this.conditionExpressionDiv.id = "conditionExpressionDiv" + Math.floor((Math.random()*100000) + 1);

					this.descriptionInput = this.mapInputId("descriptionInput");
					this.conditionPanel = this.mapInputId("conditionPanel");

					var page = this;
					
					/*Internationalization work for elements unique to  panel contents  (the panel title has to be set conditionally
					 * based on the element chosen (see setElement))*/
					m_utils.jQuerySelect("label[for='nameInput']")
						.text(m_i18nUtils.getProperty('modeler.element.properties.commonProperties.name'));
					
					m_utils.jQuerySelect("label[for='descriptionInput']")
						.text(m_i18nUtils.getProperty('modeler.element.properties.commonProperties.description'));
					
					m_utils.jQuerySelect("label[for='conditionExpressionInput']")
						.text(m_i18nUtils.getProperty('modeler.propertyPanel.data.conditionalsequenceFlow.conditonexpression.input.label'));
					
					m_utils.jQuerySelect("label[for='otherwiseInput']")
						.text(m_i18nUtils.getProperty('modeler.propertyPanel.data.conditionalsequenceFlow.otherwise.input.label'));
					
					
					/*Retrieve a javascript code editor for our condition panel*/
					this.conditionExpressionInputEditor = m_codeEditorAce.getJSCodeEditor(this.conditionExpressionDiv.id);
					
					/*listen for a module loaded event indicating we are ready to add completers to our editor.
					 *We will load a session based completer which will provide us with autocomplete for the data
					 *defined in our model*/
					$(this.conditionExpressionInputEditor).on("moduleLoaded",function(event,module){
						var sessionCompleter;
						if(module.name==="ace/ext/language_tools"){
							sessionCompleter=m_autoCompleters.getSessionCompleter();
							that.conditionExpressionInputEditor.addCompleter(sessionCompleter);
						}
					});
					/*Load languageTools,'ace/ext/language_tools', this will trigger a moduleLoaded event.*/
					this.conditionExpressionInputEditor.loadLanguageTools();
					
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
						this.conditionExpressionInputEditor.gotoLine(1);

						if (this.propertiesPanel.element.modelElement.otherwise) {
							this.conditionExpressionInputEditor.disable();
							this.setTitle("Default Sequence Flow");
						} else {
							this.conditionExpressionInputEditor.enable();
							/*Conditional  Flow*/
							this.setTitle(m_i18nUtils.getProperty("modeler.propertyPanel.data.conditionalsequenceFlow.title"));
						}

						this.conditionPanel.removeAttr("class");
					} else {
						this.conditionPanel.attr("class", "invisible");
						/*Sequence Flow*/
						this.setTitle(m_i18nUtils.getProperty("modeler.propertyPanel.data.sequenceFlow.title"));
					}
				};

				/**
				 *
				 */
				ControlFlowBasicPropertiesPage.prototype.setTitle = function(title) {
					if (title) {
						m_utils.jQuerySelect("#controlFlowPropertiesPanel div.propertiesPanelTitle").text(title);
					}
				};
			}
		});