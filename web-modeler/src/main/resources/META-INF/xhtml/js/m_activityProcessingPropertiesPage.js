/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_propertiesPage", "bpm-modeler/js/m_activity", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_activityProcessingPropertiesCommon", "bpm-modeler/js/m_angularContextUtils"],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_propertiesPage, m_activity, m_i18nUtils, m_activityProcessingPropertiesCommon, m_angularContextUtils) {
			return {
				create: function(propertiesPanel) {
					return new ActivityProcessingPropertiesPage(propertiesPanel);
				}
			};

			function ActivityProcessingPropertiesPage(propertiesPanel) {
				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "processingPropertiesPage",
						"Processing",  "plugins/bpm-modeler/images/icons/arrow-circle.png");


				/*Internationalization*/
				m_utils.jQuerySelect("#processingPropertiesPage > .heading")
					.text(m_i18nUtils.getProperty("modeler.propertiesPage.activity.processing.heading"));

				m_utils.jQuerySelect("label[for='processingTypeSelect']")
					.text(m_i18nUtils.getProperty("modeler.activity.propertyPages.general.processingType.label"));
				m_utils.jQuerySelect("label[for='inputListParam']")
					.text(m_i18nUtils.getProperty("modeler.propertiesPage.activity.processing.inputParam"));
				m_utils.jQuerySelect("label[for='indexListParam']")
					.text(m_i18nUtils.getProperty("modeler.propertiesPage.activity.processing.indexParam"));
				m_utils.jQuerySelect("label[for='outputListParam']")
					.text(m_i18nUtils.getProperty("modeler.propertiesPage.activity.processing.outputParam"));
				m_utils.jQuerySelect("label[for='batchSizeInput']")
					.text(m_i18nUtils.getProperty("modeler.propertiesPage.activity.processing.limitTransactionBatchSizeInput"));
				m_utils.jQuerySelect("label[for='maximumBatchSize']")
					.text(m_i18nUtils.getProperty("modeler.propertiesPage.activity.processing.maximumBatchSize"));
				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ActivityProcessingPropertiesPage.prototype,
						propertiesPage);

				// Field initialization
				this.processingTypeSelect = this.mapInputId("processingTypeSelect");
				this.inputListParam = this.mapInputId("inputListParam");
				this.indexListParam = this.mapInputId("indexListParam");
				this.outputListParam = this.mapInputId("outputListParam");
				this.batchSizeInput = this.mapInputId("batchSizeInput");
				this.maximumBatchInput = this.mapInputId("maximumBatchSize");
				
				// Initialize callbacks
				this.processingTypeSelect.change({
					"callbackScope" : this
				}, function(event) {
					var me = event.data.callbackScope.propertiesPanel.getModelElement();
					var val = event.data.callbackScope.processingTypeSelect.val();
					if (val === m_constants.SINGLE_PROCESSING_TYPE) {
						me.setProcessingTypeSingleInstance();
					} else {
						me.setProcessingTypeMultiInstance(val === m_constants.SEQUENTIAL_MULTI_PROCESSING_TYPE);
						// Show batch size checkbox for sequential multi-instance only
						if(val === m_constants.SEQUENTIAL_MULTI_PROCESSING_TYPE){
							event.data.callbackScope.batchSizeInput.attr("disabled", false);
						}
					}

					event.data.callbackScope.submitChanges({modelElement : {loop : me.loop}});
				});

				this.inputListParam.change({
					"callbackScope" : this
				}, function(event) {
					var me = event.data.callbackScope.propertiesPanel.getModelElement();
					var val = event.data.callbackScope.inputListParam.val();
					if (val == m_constants.TO_BE_DEFINED) {
						me.setMultiInstanceInputListParam(null);
					} else {
						me.setMultiInstanceInputListParam(val);
					}

					event.data.callbackScope.submitChanges({modelElement : {loop : me.loop}});
				});

				this.indexListParam.change({
					"callbackScope" : this
				}, function(event) {
					var me = event.data.callbackScope.propertiesPanel.getModelElement();
					var val = event.data.callbackScope.indexListParam.val();
					if (val == m_constants.TO_BE_DEFINED) {
						me.setMultiInstanceIndexListParam(null);
					} else {
						me.setMultiInstanceIndexListParam(val);
					}

					event.data.callbackScope.submitChanges({modelElement : {loop : me.loop}});
				});

				this.outputListParam.change({
					"callbackScope" : this
				}, function(event) {
					var me = event.data.callbackScope.propertiesPanel.getModelElement();
					var val = event.data.callbackScope.outputListParam.val();
					if (val == m_constants.TO_BE_DEFINED) {
						me.setMultiInstanceOutputListParam(null);
					} else {
						me.setMultiInstanceOutputListParam(val);
					}

					event.data.callbackScope.submitChanges({modelElement : {loop : me.loop}});
				});

				this.batchSizeInput.change({
					"callbackScope" : this
				}, function(event) {
					var me = event.data.callbackScope.propertiesPanel.getModelElement();
					if (event.data.callbackScope.batchSizeInput.prop("checked")) {
						event.data.callbackScope.maximumBatchInput.attr("disabled",false);
						event.data.callbackScope.maximumBatchInput.attr("value", m_constants.MULTI_INTSTANCE_TRANSACTION_BATCH_INPUT);
						me.setMultiInstanceBatchSizeParam(m_constants.MULTI_INTSTANCE_TRANSACTION_BATCH_INPUT);
					} else {
						// remove batch size attribute when batch size support checkbox is unchecked/disabled
						me.setMultiInstanceBatchSizeParam(null);
						event.data.callbackScope.maximumBatchInput.attr("disabled",true);
						event.data.callbackScope.maximumBatchInput.attr("value", "");
						me.setMultiInstanceBatchSizeParam(null);
					}
					event.data.callbackScope.submitChanges({modelElement : {loop : me.loop}});
				});	
				
				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.setElement = function() {
					m_activityProcessingPropertiesCommon.initProcessingType(this);
					if (m_activityProcessingPropertiesCommon.getProcessingType(this) === m_constants.SINGLE_PROCESSING_TYPE) {
						this.disableListParamInputs();
						this.hideBatchInputs();
					} else {
						this.enableListParamInputs();
						this.initInputListParams();
						this.initIndexListParams();
						this.initOutputListParams();
						this.showBatchInputs();
					}
				};

				this.maximumBatchInput.change({
					"callbackScope" : this
				}, function(event) {
					var self = event.data.callbackScope;
				m_angularContextUtils.runInAngularContext(function($scope) {
					$scope.$watch("batchSize", function(newValue, oldValue) {
						if (newValue !== oldValue && $scope.batchForm.maximumBatchSize.$valid) {
								if (newValue == "" || self.maximumBatchInput.val() != newValue) {
									return;
								} else {
									var me = self.propertiesPanel.getModelElement();
									me.setMultiInstanceBatchSizeParam(self.maximumBatchInput.val());
										self.submitChanges({modelElement : {loop : me.loop}});
								}
						}
					});
				}, m_utils.jQuerySelect("#configurationTab").get(0));	
				});	
				
				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.initInputListParams = function() {
					this.inputListParam.empty();
					this.inputListParam.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>" + m_i18nUtils.getProperty("modeler.general.toBeDefined") + "</option>");
					this.inputListParam.val(m_constants.TO_BE_DEFINED);
					var activity = this.getModelElement();
					for ( var i in activity.getContexts()) {
						if (i != "application" && i != "processInterface") {
							continue;
						}
						var context = activity.getContexts()[i];
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

							this.inputListParam.append(option);
						}
					}

					if (this.getModelElement().loop.inputId) {
						this.inputListParam.val(this.getModelElement().loop.inputId);
					}
				};

				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.initIndexListParams = function() {
					this.indexListParam.empty();
					this.indexListParam.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>" + m_i18nUtils.getProperty("modeler.general.toBeDefined") + "</option>");
					this.indexListParam.val(m_constants.TO_BE_DEFINED);

					var activity = this.getModelElement();
					for ( var i in activity.getContexts()) {
						if (i != "application" && i != "processInterface") {
							continue;
						}
						var context = activity.getContexts()[i];
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

							this.indexListParam.append(option);
						}
					}

					if (this.getModelElement().loop.indexId) {
						this.indexListParam.val(this.getModelElement().loop.indexId);
					}
				};

				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.initOutputListParams = function() {
					this.outputListParam.empty();
					this.outputListParam.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>" + m_i18nUtils.getProperty("modeler.general.toBeDefined") + "</option>");
					this.outputListParam.val(m_constants.TO_BE_DEFINED);

					var activity = this.getModelElement();
					for ( var i in activity.getContexts()) {
						if (i != "application" && i != "processInterface") {
							continue;
						}
						var context = activity.getContexts()[i];
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

							this.outputListParam.append(option);
						}
					}

					if (this.getModelElement().loop.outputId) {
						this.outputListParam.val(this.getModelElement().loop.outputId);
					}
				};

				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.enableListParamInputs = function() {
					this.inputListParam.removeAttr("disabled");
					this.indexListParam.removeAttr("disabled");
					this.outputListParam.removeAttr("disabled");
				};

				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.disableListParamInputs = function() {
					this.inputListParam.empty();
					this.indexListParam.empty();
					this.outputListParam.empty();
					this.inputListParam.attr("disabled", true);
					this.indexListParam.attr("disabled", true);
					this.outputListParam.attr("disabled", true);
				};
				
				/**
				 * 
				 */
				ActivityProcessingPropertiesPage.prototype.showBatchInputs = function() {
					// for sequence loop only enable limit batch size checkbox
					if (this.getModelElement().loop.sequential) {
						this.batchSizeInput.attr("disabled", false);
						// loop.batchsize is available only when limit batch checkbox is checked
						if (this.getModelElement().loop.batchSize) {
							this.maximumBatchInput.attr("disabled", false);
							this.batchSizeInput.attr("checked", true);
							if (this.getModelElement().loop.batchSize) {
								this.maximumBatchInput.val(this
										.getModelElement().loop.batchSize);
							}
						}
					} else {
						this.hideBatchInputs();
					}
				};
				
				/**
				 * 
				 */
				ActivityProcessingPropertiesPage.prototype.hideBatchInputs = function() {
					this.batchSizeInput.attr("disabled",true);
					this.batchSizeInput.attr("checked",false);
					this.maximumBatchInput.attr("disabled",true);
					this.maximumBatchInput.val("");
				};

				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.apply = function() {

				};
			}
		});