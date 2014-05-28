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
				"bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_session",
				"bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_dataTypeSelector",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_codeEditorAce", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_markupGenerator" ],
		function(m_utils, m_constants, m_urlUtils, m_session, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_dataTypeSelector, m_parameterDefinitionsPanel,
				m_codeEditorAce, m_i18nUtils, m_markupGenerator) {
			return {
				initialize : function(fullId) {
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();

					m_utils.jQuerySelect("#hideGeneralProperties").hide();
					initViewCollapseClickHandlers();

					var view = new UiMashupApplicationView();
					i18uimashupproperties();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
					m_utils.hideWaitCursor();
				}
			};

			/**
			 *
			 */
			function initViewCollapseClickHandlers() {
				m_utils.jQuerySelect("#showGeneralProperties").click(function() {
					m_utils.jQuerySelect("#showAllProperties").hide();
					m_utils.jQuerySelect("#hideGeneralProperties").show();
				});
				m_utils.jQuerySelect("#hideGeneralProperties").click(function() {
					m_utils.jQuerySelect("#showAllProperties").show();
					m_utils.jQuerySelect("#hideGeneralProperties").hide();
				});
			}

			var editorToolbarGroups = [ {
				name : 'clipboard',
				groups : [ 'clipboard', 'undo' ]
			}, {
				name : 'editing',
				groups : [ 'find', 'selection', 'spellchecker' ]
			}, {
				name : 'links'
			}, {
				name : 'insert'
			}, {
				name : 'forms'
			}, {
				name : 'tools'
			}, {
				name : 'document',
				groups : [ 'mode', 'document', 'doctools' ]
			}, {
				name : 'others'
			}, '/', {
				name : 'basicstyles',
				groups : [ 'basicstyles', 'cleanup' ]
			}, {
				name : 'paragraph',
				groups : [ 'list', 'indent', 'blocks', 'align', 'bidi' ]
			}, {
				name : 'styles'
			}, {
				name : 'colors'
			} ];

			function i18uimashupproperties() {

				m_utils.jQuerySelect("#hideGeneralProperties label")
					.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));

				m_utils.jQuerySelect("#showGeneralProperties label")
					.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));

				m_utils.jQuerySelect("label[for='guidOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.uuid"));
				m_utils.jQuerySelect("label[for='idOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.id"));

				m_utils.jQuerySelect("#applicationName")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.applicationName"));
				m_utils.jQuerySelect("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));

				m_utils.jQuerySelect("#configuration")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.configuration"));
				m_utils.jQuerySelect("label[for='viaUriInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.viaUri"));
				m_utils.jQuerySelect("label[for='embeddedInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded"));
				m_utils.jQuerySelect("label[for='markupTexarea']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.markup"));
				m_utils.jQuerySelect("label[for='urlInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.url"));
				m_utils.jQuerySelect("#paramDef")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.parameterDefinitions"));
				m_utils.jQuerySelect("#name")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				m_utils.jQuerySelect("#direction")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.direction"));
				m_utils.jQuerySelect("#dataType")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.datatType"));
				m_utils.jQuerySelect("#primitiveType")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));
				m_utils.jQuerySelect("#deleteParameterDefinitionButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.delete"));
				m_utils.jQuerySelect("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));

				var primitiveDataTypeSelect = m_utils.jQuerySelect("#primitiveDataTypeSelect");
				var selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.string");
				primitiveDataTypeSelect.append("<option value=\"String\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.boolean");
				primitiveDataTypeSelect.append("<option value=\"boolean\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.int");
				primitiveDataTypeSelect.append("<option value=\"int\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.long");
				primitiveDataTypeSelect.append("<option value=\"long\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.double");
				primitiveDataTypeSelect.append("<option value=\"double\">"
						+ selectdata + "</option>");

				// Commented as we don't support Money values yet.
				// selectdata = m_i18nUtils
				// .getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.decimal");
				// primitiveDataTypeSelect.append("<option value=\"Decimal\">"
				// + selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.calender");
				primitiveDataTypeSelect.append("<option value=\"Calendar\">"
						+ selectdata + "</option>");

				m_utils.jQuerySelect("label[for='primitiveDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));

				m_utils.jQuerySelect("#addParameterDefinitionButton")
					.attr("title",m_i18nUtils.getProperty("modeler.element.properties.commonProperties.add"));

				m_utils.jQuerySelect("label#formButtonsSelectionTitleLabel")
					.text(m_i18nUtils.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded.formButtonsSelectionTitle"));
				
				m_utils.jQuerySelect("label[for='generateMobileCodeInput']")
					.text(m_i18nUtils.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded.generateMobileCodeInputLabel"));
				
				m_utils.jQuerySelect("label[for='generateCompleteButtonInput']")
					.text(m_i18nUtils.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded.generateCompleteButton"));

				m_utils.jQuerySelect("label[for='generateSuspendButtonInput']")
					.text(m_i18nUtils.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded.generateSuspendButton"));

				m_utils.jQuerySelect("label[for='generateSaveButtonInput']")
				.text(m_i18nUtils.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded.generateSaveButton"));

				m_utils.jQuerySelect("label[for='generateAbortButtonInput']")
					.text(m_i18nUtils.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded.generateAbortButton"));

				m_utils.jQuerySelect("label[for='generateQaPassButtonInput']")
					.text(m_i18nUtils.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded.generateQASuccessButton"));

				m_utils.jQuerySelect("label[for='generateQaFailButtonInput']")
					.text(m_i18nUtils.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded.generateQAFailButoon"));

				m_utils.jQuerySelect("label[for='numberOfLabelInputPairsInput']")
					.text(m_i18nUtils.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded.numberOfColumnsUsed"));

				m_utils.jQuerySelect("a#generateMarkupForAngularLink")
					.text(m_i18nUtils.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.embedded.generateMarkup"));
			}

			/**
			 *
			 */
			function UiMashupApplicationView() {
				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(UiMashupApplicationView.prototype, view);

				/**
				 *
				 */
				UiMashupApplicationView.prototype.initialize = function(
						application) {
					this.id = "uiMashupApplicationView";
					this.currentAccessPoint = null;

					this.view = m_utils.jQuerySelect("#" + this.id);
					this.viaUriInput = m_utils.jQuerySelect("#viaUriInput");
					this.embeddedInput = m_utils.jQuerySelect("#embeddedInput");
					this.viaUriRow = m_utils.jQuerySelect("#viaUriRow");
					this.embeddedRow = m_utils.jQuerySelect("#embeddedRow");
					this.generateMarkupForAngularLink = m_utils.jQuerySelect("#generateMarkupForAngularLink");
					this.urlInput = m_utils.jQuerySelect("#urlInput");
					this.publicVisibilityCheckbox = m_utils.jQuerySelect("#publicVisibilityCheckbox");
					this.numberOfLabelInputPairsInput = m_utils.jQuerySelect("#numberOfLabelInputPairsInput");
					this.generateCompleteButtonInput = m_utils.jQuerySelect("#generateCompleteButtonInput");
					this.generateSuspendButtonInput = m_utils.jQuerySelect("#generateSuspendButtonInput");
					this.generateSaveButtonInput = m_utils.jQuerySelect("#generateSaveButtonInput");
					this.generateAbortButtonInput = m_utils.jQuerySelect("#generateAbortButtonInput");
					this.generateQaPassButtonInput = m_utils.jQuerySelect("#generateQaPassButtonInput");
					this.generateQaFailButtonInput = m_utils.jQuerySelect("#generateQaFailButtonInput");
					this.generateTabsForFirstLevelInput = m_utils.jQuerySelect("#generateTabsForFirstLevelInput");
					this.generateTabsForFirstLevelTablesInput = m_utils.jQuerySelect("#generateTabsForFirstLevelTablesInput");
					this.generateMobileCodeInput = m_utils.jQuerySelect("#generateMobileCodeInput");

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
							.create({
								scope : "uiMashupApplicationView",
								submitHandler : this,
								supportsOrdering : false,
								supportsDataMappings : false,
								supportsDescriptors : false,
								supportsDataTypeSelection : true,
								supportsInOutDirection : true,
								supportsDocumentTypes : false,
								tableWidth : "500px",
								directionColumnWidth : "50px",
								nameColumnWidth : "250px",
								typeColumnWidth : "200px"
							});

					var rdmNo = Math.floor((Math.random()*100000) + 1);
					this.editorAnchor = m_utils.jQuerySelect("#markupTextareaDiv").get(0);
					this.editorAnchor.id = "markupTextarea" + rdmNo + "Div";
					this.editorTextArea = m_utils.jQuerySelect("#markupTextarea").get(0);
					this.editorTextArea.id = "markupTextarea" + rdmNo;
					var self = this;

					var self = this;
					CKEDITOR.replace(self.editorTextArea.id, {
						toolbarGroups : editorToolbarGroups,
						allowedContent : true
					});

					CKEDITOR.instances[self.editorTextArea.id].on('blur',
							function(e) {
								if (!self.validate()) {
									return;
								}
								self.submitEmbeddedModeChanges();
							});

					this.urlInput
							.change(
									{
										view : this
									},
									function(event) {
										if (!event.data.view.validate()) {
											return;
										}
										event.data.view
												.submitExternalWebAppContextAttributesChange({
													"carnot:engine:ui:externalWebApp:embedded" : false,
													"carnot:engine:ui:externalWebApp:uri" : event.data.view.urlInput
															.val(),
													"ui:externalWebApp:embedded:generateMobileMarkup" : null
												});
									});

					this.generateTable = m_utils.jQuerySelect("#generateTable");

					this.generateMobileCodeInput
							.change(
									{
										view : this
									},
									function(event) {
										event.data.view.submitEmbeddedModeChanges();
									});
					
					this.generateMarkupForAngularLink.click({
						view : this
					}, function(event) {
						CKEDITOR.instances[event.data.view.editorTextArea.id]
								.setData(event.data.view.generateMarkup());
						event.data.view.submitEmbeddedModeChanges();
					});
					this.viaUriInput
							.click(
									{
										view : this
									},
									function(event) {
										event.data.view.setViaUri();
										event.data.view
												.submitExternalWebAppContextAttributesChange({
													"carnot:engine:ui:externalWebApp:embedded" : false,
													"carnot:engine:ui:externalWebApp:uri" : event.data.view.urlInput
															.val(),
													"carnot:engine:ui:externalWebApp:markup" : null
												});
									});
					this.embeddedInput.click({
						view : this
					}, function(event) {
						event.data.view.setEmbedded();
						event.data.view.generateMobileCodeInput.prop("checked",true);						
						event.data.view.submitEmbeddedModeChanges();
					});

					this.publicVisibilityCheckbox
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes["carnot:engine:visibility"]
												&& view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
														}
													});
										} else {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Private"
														}
													});
										}
									});

					/**
					 * This is a workaround for the Chrome browser.
					 * In Chrome, at times, the contents of the CKEditor are not loaded correctly. This on-click handler
					 * ensures that data is set when one switches to the configuration tab.
					 */
					m_utils.jQuerySelect("#configuration").click(function() {
						CKEDITOR.instances[self.editorTextArea.id]
							.setData(self.getContext().attributes["carnot:engine:ui:externalWebApp:markup"]);
					});

					this.initializeModelElementView(application);

					this.view.css("visibility", "visible");
				};

				UiMashupApplicationView.prototype.submitEmbeddedModeChanges = function() {
					this
							.submitExternalWebAppContextAttributesChange({
								"carnot:engine:ui:externalWebApp:embedded" : true,
								"carnot:engine:ui:externalWebApp:uri" : null,
								"ui:externalWebApp:embedded:generateMobileMarkup" : this.generateMobileCodeInput.prop("checked"),
								"carnot:engine:ui:externalWebApp:markup" : CKEDITOR.instances[this.editorTextArea.id].getData()

							});
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.isEmbeddedConfiguration = function() {
					m_utils
							.debug("==> embedded: "
									+ this.getContext().attributes["carnot:engine:ui:externalWebApp:embedded"] == true);
					m_utils
							.debug("==> embedded: "
									+ this.getContext().attributes["carnot:engine:ui:externalWebApp:embedded"] == "true");

					return this.getContext().attributes["carnot:engine:ui:externalWebApp:embedded"];
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.submitExternalWebAppContextAttributesChange = function(
						attributes) {
					this.submitChanges({
						contexts : {
							externalWebApp : {
								attributes : attributes,
								accessPoints : this.getContext().accessPoints
							}
						}
					});
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.setViaUri = function(uri) {
					this.viaUriInput.prop("checked", true);
					this.embeddedInput.prop("checked", false);

					m_dialog.makeVisible(this.viaUriRow);
					m_dialog.makeInvisible(this.embeddedRow);
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.setEmbedded = function() {
					this.viaUriInput.prop("checked", false);
					this.embeddedInput.prop("checked", true);
					this.generateMobileCodeInput
							.prop(
									"checked",
									this.getContext().attributes["ui:externalWebApp:embedded:generateMobileMarkup"]);

					m_dialog.makeInvisible(this.viaUriRow);
					m_dialog.makeVisible(this.embeddedRow);
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.getApplication = function() {
					return this.application;
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.getContext = function() {
					return this.application.contexts["externalWebApp"];
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.setModelElement = function(
						application) {
					this.application = application;

					m_utils.debug("===> Application");
					m_utils.debug(this.application);
					m_utils.debug("===> Context");
					m_utils.debug(this.getContext());

					if (!this.application.attributes["carnot:engine:visibility"]
							|| "Public" == this.application.attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}

					// TODO Guard needed?

					if (this.getContext() == null) {
						this.application.contexts = {
							externalWebApp : {
								accessPoints : [],
								attributes : {}
							}
						};
					}

					if (this.isEmbeddedConfiguration()) {
						this.setEmbedded();
						var self = this;
						CKEDITOR.instances[self.editorTextArea.id].setData(self.getContext().attributes["carnot:engine:ui:externalWebApp:markup"]);
					} else {
						this.setViaUri();
						this.urlInput
								.val(this.getContext().attributes["carnot:engine:ui:externalWebApp:uri"]);
					}

					this.initializeModelElement(application);

					this.parameterDefinitionsPanel
							.setScopeModel(this.application.model);
					this.parameterDefinitionsPanel.setParameterDefinitions(this.mergeINOUTParams(this
							.getContext().accessPoints));

					// UI Only Defaults, do it only once on View load
					if (this.numberOfLabelInputPairsInput.val() == "") {
						this.numberOfLabelInputPairsInput.val(4);
						this.generateCompleteButtonInput.prop("checked", true);
						this.generateSuspendButtonInput.prop("checked", true);
						this.generateSaveButtonInput.prop("checked", true);
						this.generateAbortButtonInput.prop("checked", true);
						this.generateQaPassButtonInput.prop("checked", false);
						this.generateQaFailButtonInput.prop("checked", false);
					}
				};
				
				/**
				 *
				 */
				UiMashupApplicationView.prototype.toString = function() {
					return "Lightdust.UiMashupApplicationView";
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (m_utils.isEmptyString(this.nameInput.val())) {
						this.errorMessages.push("Data name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {
					// Context is regenerated on the server - hence, all data
					// need to be provided
					parameterDefinitionsChanges = this.splitINOUTParameters(parameterDefinitionsChanges);
					this.submitChanges({
						contexts : {
							"externalWebApp" : {
								accessPoints : parameterDefinitionsChanges,
								attributes : this.getContext().attributes
							}
						}
					});
				};

				/**
				 * Splits INOUT params into separate IN and OUT params with same name
				 */
				UiMashupApplicationView.prototype.splitINOUTParameters = function(parameterDefinitionsChanges) {
					var newParameterDefs = [];
					jQuery.each(parameterDefinitionsChanges, function(index, param) {
						if (param.direction === m_constants.IN_OUT_ACCESS_POINT) {
							var inParam = jQuery.extend(true, {}, param);
							inParam.direction = m_constants.IN_ACCESS_POINT;
							inParam.attributes = inParam.attributes ? inParam.attributes : {};
							inParam.attributes[m_constants.IS_INOUT_PARAM] = true
							newParameterDefs.push(inParam);
							var outParam = jQuery.extend(true, {}, param);
							outParam.direction = m_constants.OUT_ACCESS_POINT;
							outParam.attributes = outParam.attributes ? outParam.attributes : {};
							outParam.attributes[m_constants.IS_INOUT_PARAM] = true;
							newParameterDefs.push(outParam);
						} else {
							if (param.attributes) {
								param.attributes[m_constants.IS_INOUT_PARAM] = null;	
							}
							newParameterDefs.push(param);
						}
					});
					
					return newParameterDefs;
				};
				

				/**
				 * Merger two parameter with same names and opposite directions into
				 * a single parameter with same name and direction as INOUT
				 * if the attribute "IS_INOUT_PARAM" is set for it.
				 *  
				 */
				UiMashupApplicationView.prototype.mergeINOUTParams = function(accessPoints) {
					//var paramMap = {};
					var addedINOUTParams = [];
					var newParamArray = [];
					jQuery.each(accessPoints, function(index, accessPoint) {
						if (accessPoint.attributes && accessPoint.attributes[m_constants.IS_INOUT_PARAM]) {
							if (jQuery.inArray(accessPoint.name, addedINOUTParams) < 0) {
								var param = jQuery.extend(true, {}, accessPoint);
								param.direction = m_constants.IN_OUT_ACCESS_POINT;
								newParamArray.push(param);
								addedINOUTParams.push(accessPoint.name);
							}
						} else {
							newParamArray.push(accessPoint);
						}
					});
					
					return newParamArray;
				};
				
				/**
				 * 
				 */
				UiMashupApplicationView.prototype.generateMarkup = function() {
					var generator = m_markupGenerator
							.create({
								numberOfPrimitivesPerColumns : this.numberOfLabelInputPairsInput
										.val(),
								generateCompleteButton : this.generateCompleteButtonInput
										.prop("checked"),
								generateSuspendButton : this.generateSuspendButtonInput
										.prop("checked"),
								generateSaveButton : this.generateSaveButtonInput
										.prop("checked"),
								generateAbortButton : this.generateAbortButtonInput
										.prop("checked"),
								generateQaPassButton : this.generateQaPassButtonInput
										.prop("checked"),
								generateQaFailButton : this.generateQaFailButtonInput
										.prop("checked"),
								tabsForFirstLevel : this.generateTabsForFirstLevelInput
										.prop("checked"),
								tabsForFirstLevelTables : this.generateTabsForFirstLevelTablesInput
										.prop("checked"),
								generateMobileMarkup : this.generateMobileCodeInput.prop("checked")
							});

					return generator
							.generateMarkup(this.getContext().accessPoints);
				};
			}
		});