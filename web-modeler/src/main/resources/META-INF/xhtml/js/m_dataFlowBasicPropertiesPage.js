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
				"bpm-modeler/js/m_user", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_basicPropertiesPage", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_ruleSetsHelper" ],
		function(m_utils, m_constants, m_user, m_dialog, m_basicPropertiesPage, m_i18nUtils, m_model, m_ruleSetsHelper) {
			return {
				create : function(propertiesPanel) {
					var page = new DataFlowBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function DataFlowBasicPropertiesPage(propertiesPanel) {

				// Inheritance

				var propertiesPage = m_basicPropertiesPage
						.create(propertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(DataFlowBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				DataFlowBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();

					this.inputInput = this.mapInputId("inputInput");
					this.outputInput = this.mapInputId("outputInput");
					this.descriptionInput = this.mapInputId("descriptionInput");
					this.inputDataPathInput = this
							.mapInputId("inputDataPathInput");
					this.outputDataPathInput = this
							.mapInputId("outputDataPathInput");
					this.inputAccessPointPanel = this
							.mapInputId("inputAccessPointPanel");
					this.outputAccessPointPanel = this
							.mapInputId("outputAccessPointPanel");
					this.inputAccessPointSelectInput = this
							.mapInputId("inputAccessPointSelectInput");
					this.outputAccessPointSelectInput = this
							.mapInputId("outputAccessPointSelectInput");
					this.inputAccessPointSelectInputPanel = this
							.mapInputId("inputAccessPointSelectInputPanel");
					this.outputAccessPointSelectInputPanel = this
							.mapInputId("outputAccessPointSelectInputPanel");

					this.inputInput
							.click(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;

										if (page.inputInput.is(":checked")
												&& page.propertiesPanel.element.modelElement.inputDataMapping == null) {
											page.propertiesPanel.element.modelElement.inputDataMapping = {};
										} else if (!page.outputInput
												.is(":checked")) {
											// At least one checkbox has to be
											// checked
											page.inputInput.attr("checked",
													true);

											return;
										} else {
											page.propertiesPanel.element.modelElement.inputDataMapping = undefined;
										}

										page
												.setDirection(page.inputInput
														.is(":checked"),
														page.outputInput
																.is(":checked"));

										if (page.inputInput.is(":checked") && page.outputInput.is(":checked")
												&& page.propertiesPanel.element.fromAnchorPoint.symbol.type !== m_constants.DATA_SYMBOL) {
											var tempFromAnchorPoint = page.propertiesPanel.element.fromAnchorPoint;
											page.propertiesPanel.element.fromAnchorPoint = page.propertiesPanel.element.toAnchorPoint;
											page.propertiesPanel.element.toAnchorPoint = tempFromAnchorPoint;
											page.propertiesPanel.element.fromModelElementOid = page.propertiesPanel.element.fromAnchorPoint.symbol.oid;
											page.propertiesPanel.element.toModelElementOid = page.propertiesPanel.element.toAnchorPoint.symbol.oid;
											var tempFromOrientation = page.propertiesPanel.element.fromAnchorPointOrientation;
											page.propertiesPanel.element.fromAnchorPointOrientation = page.propertiesPanel.element.toAnchorPointOrientation;
											page.propertiesPanel.element.toAnchorPointOrientation = tempFromOrientation;
										} else if (!page.inputInput.is(":checked") && page.outputInput.is(":checked")
												&& page.propertiesPanel.element.fromAnchorPoint.symbol.type === m_constants.DATA_SYMBOL) {
											var tempFromAnchorPoint = page.propertiesPanel.element.fromAnchorPoint;
											page.propertiesPanel.element.fromAnchorPoint = page.propertiesPanel.element.toAnchorPoint;
											page.propertiesPanel.element.toAnchorPoint = tempFromAnchorPoint;
											page.propertiesPanel.element.fromModelElementOid = page.propertiesPanel.element.fromAnchorPoint.symbol.oid;
											page.propertiesPanel.element.toModelElementOid = page.propertiesPanel.element.toAnchorPoint.symbol.oid;
											var tempFromOrientation = page.propertiesPanel.element.fromAnchorPointOrientation;
											page.propertiesPanel.element.fromAnchorPointOrientation = page.propertiesPanel.element.toAnchorPointOrientation;
											page.propertiesPanel.element.toAnchorPointOrientation = tempFromOrientation;
										}
										
										page
												.submitChanges({
													// TODO Usually, we are not
													// submitting the object
													// itself
													// modelElement :
													// page.propertiesPanel.element.modelElement
													modelElement : {
														id : page.propertiesPanel.element.modelElement.id,
														name : page.propertiesPanel.element.modelElement.name,
														fromAnchorPointOrientation : page.propertiesPanel.element.fromAnchorPointOrientation,
														toAnchorPointOrientation : page.propertiesPanel.element.toAnchorPointOrientation,
														toModelElementOid : page.propertiesPanel.element.toModelElementOid,
														fromModelElementOid : page.propertiesPanel.element.fromModelElementOid,
														updateDataMapping : true,
														inputDataMapping : page.propertiesPanel.element.modelElement.inputDataMapping,
														outputDataMapping : page.propertiesPanel.element.modelElement.outputDataMapping
													}
												});
									});

					this.outputInput
							.click(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;

										if (page.inputInput.is(":checked")
												&& page.propertiesPanel.element.modelElement.outputDataMapping == null) {
											page.propertiesPanel.element.modelElement.outputDataMapping = {};
										} else if (!page.inputInput
												.is(":checked")) {
											// At least one checkbox has to be
											// checked
											page.outputInput.attr("checked",
													true);

											return;
										} else {
											page.propertiesPanel.element.modelElement.outputDataMapping = undefined;
										}

										page
												.setDirection(page.inputInput
														.is(":checked"),
														page.outputInput
																.is(":checked"));
										page
												.submitChanges({
													// TODO Usually, we are not
													// submitting the object
													// itself
													// modelElement :
													// page.propertiesPanel.element.modelElement
													modelElement : {
														id : page.propertiesPanel.element.modelElement.id,
														name : page.propertiesPanel.element.modelElement.name,
														updateDataMapping : true,
														inputDataMapping : page.propertiesPanel.element.modelElement.inputDataMapping,
														outputDataMapping : page.propertiesPanel.element.modelElement.outputDataMapping
													}
												});
									});

					this.registerInputForModelElementChangeSubmission(
							this.descriptionInput, "description");

					this.inputDataPathInput
							.change(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;

										m_utils.debug("Submitting data flow changes");

										// TODO Usually we push less
										// information, but current server code
										// requires this
										page
												.submitChanges({
													modelElement : {
														inputDataMapping : {
															id : page
																	.getModelElement().inputDataMapping.id,
															name : page
																	.getModelElement().inputDataMapping.name,
															accessPointId : page
																	.getModelElement().inputDataMapping.accessPointId,
															dataPath : page.inputDataPathInput
																	.val()
														},
														outputDataMapping : page
																.getModelElement().outputDataMapping
													}
												});
									});
					this.outputDataPathInput
							.change(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;

										m_utils.debug("Submitting data flow changes");

										// TODO Usually we push less
										// information, but current server code
										// requires this
										page
												.submitChanges({
													modelElement : {
														inputDataMapping : page
																.getModelElement().inputDataMapping,
														outputDataMapping : {
															id : page
																	.getModelElement().outputDataMapping.id,
															name : page
																	.getModelElement().outputDataMapping.name,
															accessPointId : page
																	.getModelElement().outputDataMapping.accessPointId,
															dataPath : page.outputDataPathInput
																	.val()
														}
													}
												});
									});

					this.inputAccessPointSelectInput
							.change(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;
										var value = page.inputAccessPointSelectInput
												.val();

										if (value == "DEFAULT") {
											page.propertiesPanel.element.modelElement.inputDataMapping.accessPointContext = null;
											page.propertiesPanel.element.modelElement.inputDataMapping.accessPointId = null;
										} else {
											var colIndex = value.indexOf(":");
											var context = value.substring(0, colIndex);
											var accessPointId = value.substring(colIndex + 1);

											page.propertiesPanel.element.modelElement.inputDataMapping.accessPointContext = context;
											page.propertiesPanel.element.modelElement.inputDataMapping.accessPointId = accessPointId;
										}

										page
												.submitChanges({
													modelElement : {
														inputDataMapping : page
																.getModelElement().inputDataMapping,
														outputDataMapping : page
																.getModelElement().outputDataMapping
													}
												});
									});
					this.outputAccessPointSelectInput
							.change(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;
										var value = page.outputAccessPointSelectInput
												.val();

										if (value == "DEFAULT") {
											page.propertiesPanel.element.modelElement.outputDataMapping.accessPointContext = null;
											page.propertiesPanel.element.modelElement.outputDataMapping.accessPointId = null;
										} else {
											var data = value.split(":");

											page.propertiesPanel.element.modelElement.outputDataMapping.accessPointContext = data[0];
											page.propertiesPanel.element.modelElement.outputDataMapping.accessPointId = data[1];
										}

										page
												.submitChanges({
													modelElement : {
														inputDataMapping : page
																.getModelElement().inputDataMapping,
														outputDataMapping : page
																.getModelElement().outputDataMapping
													}
												});
									});
				};

				/**
				 *
				 */
				DataFlowBasicPropertiesPage.prototype.setDirection = function(
						hasInputMapping, hasOutputMapping) {
					if (hasInputMapping) {
						m_dialog.makeVisible(this.inputAccessPointPanel);
						this.disableDataPath(this.inputDataPathInput);
					} else {
						m_dialog.makeInvisible(this.inputAccessPointPanel);
					}

					if (hasOutputMapping) {
						m_dialog.makeVisible(this.outputAccessPointPanel);
						this.disableDataPath(this.outputDataPathInput);
					} else {
						m_dialog.makeInvisible(this.outputAccessPointPanel);
					}

					this.inputInput.attr("checked", hasInputMapping);
					this.outputInput.attr("checked", hasOutputMapping);
				};

				/**
				 * Input / output dataPath text-boxes are disabled for
				 * java-like application activities (plainJava, springBean, sessionBean)
				 * and data (entity, hibernate).
				 */
				DataFlowBasicPropertiesPage.prototype.disableDataPath = function(dataPath) {
					dataPath.removeAttr("disabled");
					if (this.propertiesPanel.element
							&& this.propertiesPanel.element.modelElement) {
						if (this.propertiesPanel.element.modelElement.activity
								&& this.propertiesPanel.element.modelElement.activity.activityType === "Task"
								&& this.propertiesPanel.element.modelElement.activity.applicationFullId) {
							var app = m_model
									.findApplication(this.propertiesPanel.element.modelElement.activity.applicationFullId);
							if (app
									&& (app.applicationType === m_constants.JAVA_APPLICATION_TYPE
											|| app.applicationType === m_constants.SPRING_BEAN_APPLICATION_TYPE
											|| app.applicationType === m_constants.SESSION_BEAN_APPLICATION_TYPE)) {
								dataPath.attr("disabled", "disabled");
							}
						}

						if (this.propertiesPanel.element.modelElement.data
								&& (this.propertiesPanel.element.modelElement.data.dataType === m_constants.ENTITY_DATA_TYPE
									|| this.propertiesPanel.element.modelElement.data.dataType === m_constants.HIBERNATE_DATA_TYPE)) {
							dataPath.attr("disabled", "disabled");
						}
					}
				};

				/**
				 *
				 */
				DataFlowBasicPropertiesPage.prototype.populateInputAccessPointSelectInput = function(
						dataFlow) {
					this.inputAccessPointSelectInput.empty();

					if (dataFlow.activity.hasInputAccessPoints()) {
						m_dialog
								.makeVisible(this.inputAccessPointSelectInputPanel);
					} else {
						m_dialog
								.makeInvisible(this.inputAccessPointSelectInputPanel);
					}

					// TODO Use method of m_activity; proper type binding
					// required
					if (dataFlow.activity.taskType != m_constants.TASK_ACTIVITY_TYPE) {
						this.inputAccessPointSelectInput
								.append("<option value='DEFAULT'>Default</option>"); // I18N
					} else {
						this.inputAccessPointSelectInput
								.append("<option value='DEFAULT'>" + m_i18nUtils.getProperty("modeler.general.toBeDefined") + "</option>");
					}

					m_utils.debug("Contexts");
					m_utils.debug(dataFlow.activity.getContexts());

					for ( var i in dataFlow.activity.getContexts()) {
						var context = dataFlow.activity.getContexts()[i];
						var count = 0;

						m_utils.debug("i = " + i);
						m_utils.debug(context);

						for ( var m = 0; m < context.accessPoints.length; ++m) {
							var accessPoint = context.accessPoints[m];

							m_utils.debug("m = " + m);
							m_utils.debug(accessPoint);

							if (accessPoint.direction == m_constants.IN_ACCESS_POINT
									|| accessPoint.direction == m_constants.IN_OUT_ACCESS_POINT) {
								count++;
							}
						}

						if (count == 0) {
							continue;
						}

						var group = m_utils.jQuerySelect("<optgroup label='" + i + "'/>"); // I18N

						this.inputAccessPointSelectInput.append(group);

						for ( var m = 0; m < context.accessPoints.length; ++m) {
							var accessPoint = context.accessPoints[m];

							if (accessPoint.direction == m_constants.OUT_ACCESS_POINT) {
								continue;
							}

							var option = "<option value='";

							option += i;
							option += ":";
							option += accessPoint.id;
							option += "'>";
							option += accessPoint.name;
							option += "</option>";

							group.append(option);
						}
					}
					this.populateEngineAccessPoints(this.inputAccessPointSelectInput);
					this.populateRulesInAccesspoints(this.inputAccessPointSelectInput);
					
				};

				/**
				 *
				 */
				DataFlowBasicPropertiesPage.prototype.populateOutputAccessPointSelectInput = function(
						dataFlow) {
					this.outputAccessPointSelectInput.empty();

					if (dataFlow.activity.hasOutputAccessPoints()) {
						m_dialog
								.makeVisible(this.outputAccessPointSelectInputPanel);
					} else {
						m_dialog
								.makeInvisible(this.outputAccessPointSelectInputPanel);
					}

					m_utils.debug("Before default");

					// TODO Use method of m_activity; proper type binding
					// required
					if (dataFlow.activity.taskType != m_constants.TASK_ACTIVITY_TYPE) {
						this.outputAccessPointSelectInput
								.append("<option value='DEFAULT'>Default</option>");
					} else {
						this.outputAccessPointSelectInput
								.append("<option value='DEFAULT'>" + m_i18nUtils.getProperty("modeler.general.toBeDefined") + "</option>");
					}

					for ( var i in dataFlow.activity.getContexts()) {
						var context = dataFlow.activity.getContexts()[i];
						var count = 0;

						for ( var m = 0; m < context.accessPoints.length; ++m) {
							var accessPoint = context.accessPoints[m];

							if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
									|| accessPoint.direction == m_constants.IN_OUT_ACCESS_POINT) {
								count++;
							}
						}

						if (count == 0) {
							continue;
						}

						var group = m_utils.jQuerySelect("<optgroup label='" + i + "'/>"); // I18N

						this.outputAccessPointSelectInput.append(group);

						for ( var m = 0; m < context.accessPoints.length; ++m) {
							var accessPoint = context.accessPoints[m];

							if (accessPoint.direction == m_constants.IN_ACCESS_POINT) {
								continue;
							}

							var option = "<option value='";

							option += i;
							option += ":";
							option += accessPoint.id;
							option += "'>";
							option += accessPoint.name;
							option += "</option>";

							group.append(option);
						}
					}
					this.populateEngineAccessPoints(this.outputAccessPointSelectInput);
					this.populateRulesOutAccesspoints(this.outputAccessPointSelectInput);
				};

				/**
				 *
				 */
				DataFlowBasicPropertiesPage.prototype.populateEngineAccessPoints = function(inputElement) {
					// Generate engine context access points for all data in the model,
					// for sub-process activities with where copyAllData is disabled.
					if (this.getModelElement()
							&& this.getModelElement().activity
							&& this.getModelElement().activity.activityType === m_constants.SUBPROCESS_ACTIVITY_TYPE
							&& this.getModelElement().activity.subprocessMode !== "synchShared"
							&& (this.getModelElement().activity.attributes 
									&& !this.getModelElement().activity.attributes["carnot:engine:subprocess:copyAllData"])) {
						
						var group = m_utils.jQuerySelect("<optgroup label='engine'/>"); // I18N
						inputElement.append(group);
						for (var i in this.getModel().dataItems) {
							var d = this.getModel().dataItems[i];
							var option = "<option value='engine:";
							option += d.id;
							option += "'>";
							option += d.name;
							option += "</option>";

							group.append(option);
						}	
					}
				};
				
				/**
				 *
				 */
				DataFlowBasicPropertiesPage.prototype.populateRulesInAccesspoints = function(inputElement) {
					// Generate engine context access points for all data in the model,
					// for sub-process activities with where copyAllData is disabled.
					if (this.getModelElement()
							&& this.getModelElement().activity
							&& this.getModelElement().activity.activityType === m_constants.TASK_ACTIVITY_TYPE
							&& this.getModelElement().activity.attributes["ruleSetId"]) {						
						var ruleOptGroupName = m_i18nUtils.getProperty("modeler.dataFlow.propertiesPage.accessPoints.rules.optGroup.name");
						var group = m_utils.jQuerySelect("<optgroup label='" + ruleOptGroupName + "'/>");
						inputElement.append(group);
						var ruleSets = m_ruleSetsHelper.getRuleSets();
						if (ruleSets) {
							var rule = ruleSets[this.getModelElement().activity.attributes["ruleSetId"]];
							if (rule) {
								for (var i in rule.parameterDefinitions) {
									var param = rule.parameterDefinitions[i];
									if (param.direction === "IN" || param.direction === "INOUT") {
										var option = "<option value='engine:";
										option += param.id;
										option += "'>";
										option += param.name;
										option += "</option>";

										group.append(option);
									}
								}
							}
						}	
					}
				};
				
				
				/**
				 *
				 */
				DataFlowBasicPropertiesPage.prototype.populateRulesOutAccesspoints = function(inputElement) {
					// Generate engine context access points for all data in the model,
					// for sub-process activities with where copyAllData is disabled.
					if (this.getModelElement()
							&& this.getModelElement().activity
							&& this.getModelElement().activity.activityType === m_constants.TASK_ACTIVITY_TYPE
							&& this.getModelElement().activity.attributes["ruleSetId"]) {						
						var ruleOptGroupName = m_i18nUtils.getProperty("modeler.dataFlow.propertiesPage.accessPoints.rules.optGroup.name");
						var group = m_utils.jQuerySelect("<optgroup label='" + ruleOptGroupName + "'/>");
						inputElement.append(group);
						if (ruleSets) {
							var rule = ruleSets[this.getModelElement().activity.attributes["ruleSetId"]];
							if (rule) {
								for (var i in rule.parameterDefinitions) {
									var param = rule.parameterDefinitions[i];
									if (param.direction === "OUT" || param.direction === "INOUT") {
										var option = "<option value='engine:";
										option += param.id;
										option += "'>";
										option += param.name;
										option += "</option>";

										group.append(option);
									}
								}
							}
						}	
					}
				};
				
				/**
				 *
				 */
				DataFlowBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					//disable description section
					this.descriptionInput.hide();
					m_utils.jQuerySelect("label[for='descriptionInput']").hide();
					
					m_utils.debug("===> Data Flow");
					m_utils.debug(this.propertiesPanel.element.modelElement);

					this
							.populateInputAccessPointSelectInput(this.propertiesPanel.element.modelElement);
					this
							.populateOutputAccessPointSelectInput(this.propertiesPanel.element.modelElement);
					this.descriptionInput
							.val(this.propertiesPanel.element.modelElement.description);
					this
							.setDirection(
									this.propertiesPanel.element.modelElement.inputDataMapping != null,
									this.propertiesPanel.element.modelElement.outputDataMapping != null);

					if (this.propertiesPanel.element.modelElement.inputDataMapping) {
						this.inputDataPathInput
								.val(this.propertiesPanel.element.modelElement.inputDataMapping.dataPath);
						if (this.propertiesPanel.element.modelElement.inputDataMapping.accessPointId == null) {
							this.inputAccessPointSelectInput.val("DEFAULT");
						} else {
							this.inputAccessPointSelectInput
									.val(this.propertiesPanel.element.modelElement.inputDataMapping.accessPointContext
											+ ":"
											+ this.propertiesPanel.element.modelElement.inputDataMapping.accessPointId);
						}
					}

					if (this.propertiesPanel.element.modelElement.outputDataMapping) {
						this.outputDataPathInput
								.val(this.propertiesPanel.element.modelElement.outputDataMapping.dataPath);
						if (this.propertiesPanel.element.modelElement.outputDataMapping.accessPointId == null) {
							this.outputAccessPointSelectInput.val("DEFAULT");
						} else {
							this.outputAccessPointSelectInput
									.val(this.propertiesPanel.element.modelElement.outputDataMapping.accessPointContext
											+ ":"
											+ this.propertiesPanel.element.modelElement.outputDataMapping.accessPointId);
						}
					}
				};
			}
		});