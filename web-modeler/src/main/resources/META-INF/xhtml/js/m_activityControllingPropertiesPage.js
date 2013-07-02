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
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_dialog, m_propertiesPage, m_i18nUtils) {

			return {
				create : function(propertiesPanel) {
					return new ActivityControllingPropertiesPage(
							propertiesPanel);
				}
			};

			function i18nactivity() {
				$("label[for='measureInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.measure"));
				$("label[for='targetMeasureQuantityInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.targetMeasureQuantity"));
				$("label[for='difficultyInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.difficulty"));
				$("label[for='targetProcessingTimeInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.targetProcessingTime"));
				$("label[for='targetExecutionTimeInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.targetExecutionTimeInput")); // Execution
				$("label[for='targetIdleTimeInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.targetIdleTimeInput"));
				$("label[for='targetWaitingTimeInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.targetWaitingTimeInput"));
				$("label[for='targetQueueDepthInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.targetQueueDepthInput"));
				$("label[for='targetCostPerExecutionInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.targetCostPerExecutionInput"));
				$("label[for='targetCostPerSecondInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.targetCostPerSecondInput"));
				$("label[for='resourcePerformanceCalculationSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.resourcePerformanceCalculationSelect"));
				jQuery("#activityhours")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.hours"));
				jQuery("#activityhours2")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.hours"));
				jQuery("#activityhours3")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.hours"));
				jQuery("#activityhours4")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.hours"));
				jQuery("#activitydollar")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.dollar"));
				jQuery("#activityControling")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.controlling.header"));

				this.resourcePerformanceCalculationSelect = jQuery("#resourcePerformanceCalculationSelect");

				var selectdata = m_i18nUtils
						.getProperty("modeler.activity.propertyPages.controlling.resourcePerformanceCalculationSelect.included");
				resourcePerformanceCalculationSelect
						.append("<option value=\"included\">" + selectdata
								+ "</option>");
				selectdata = m_i18nUtils
						.getProperty("modeler.activity.propertyPages.controlling.resourcePerformanceCalculationSelect.notIncluded");
				resourcePerformanceCalculationSelect
						.append("<option value=\"notIncluded\">" + selectdata
								+ "</option>");
			}

			/**
			 * 
			 */
			function ActivityControllingPropertiesPage(propertiesPanel) {
				var controlling = m_i18nUtils
						.getProperty("modeler.activity.propertyPages.controlling.heading");
				i18nactivity();
				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "controllingPropertiesPage",
						controlling,
						"plugins/bpm-modeler/images/icons/chart-up.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ActivityControllingPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				this.measureInput = this.mapInputId("measureInput");
				this.targetMeasureQuantityInput = this
						.mapInputId("targetMeasureQuantityInput");
				this.difficultyInput = this.mapInputId("difficultyInput");
				this.targetCostPerExecutionInput = this
						.mapInputId("targetCostPerExecutionInput");
				this.targetProcessingTimeInput = this
						.mapInputId("targetProcessingTimeInput");
				this.targetExecutionTimeInput = this
						.mapInputId("targetExecutionTimeInput");
				this.targetIdleTimeInput = this
						.mapInputId("targetIdleTimeInput");
				this.targetWaitingTimeInput = this
						.mapInputId("targetWaitingTimeInput");
				this.targetQueueDepthInput = this
						.mapInputId("targetQueueDepthInput");
				this.targetCostPerExecutionInput = this
						.mapInputId("targetCostPerExecutionInput");
				this.targetCostPerSecondInput = this
						.mapInputId("targetCostPerSecondInput");
				this.resourcePerformanceCalculationSelect = this
						.mapInputId("resourcePerformanceCalculationSelect");

				m_dialog
						.registerForNumericFormatValidation(targetCostPerExecutionInput);
				m_dialog
						.registerForNumericFormatValidation(targetQueueDepthInput);

				// Change handling

				this.registerInputForModelElementAttributeChangeSubmission(
						this.measureInput, "carnot:pwh:measure");
				this.registerInputForModelElementAttributeChangeSubmission(
						this.targetMeasureQuantityInput,
						"carnot:pwh:targetMeasureQuantity");
				this.registerInputForModelElementAttributeChangeSubmission(
						this.difficultyInput, "carnot:pwh:difficulty");
				this.registerInputForModelElementAttributeChangeSubmission(
						this.targetProcessingTimeInput,
						"carnot:pwh:targetProcessingTime");
				this.registerInputForModelElementAttributeChangeSubmission(
						this.targetExecutionTimeInput,
						"carnot:pwh:targetExecutionTime");
				this.registerInputForModelElementAttributeChangeSubmission(
						this.targetIdleTimeInput, "carnot:pwh:targetIdleTime");
				this.registerInputForModelElementAttributeChangeSubmission(
						this.targetWaitingTimeInput,
						"carnot:pwh:targetWaitingTime");
				this.registerInputForModelElementAttributeChangeSubmission(
						this.targetQueueDepthInput,
						"carnot:pwh:targetQueueDepth");
				this.registerInputForModelElementAttributeChangeSubmission(
						this.targetCostPerExecutionInput,
						"carnot:pwh:targetCostPerExecution");
				this.registerInputForModelElementAttributeChangeSubmission(
						this.targetCostPerSecondInput,
						"carnot:pwh:targetCostPerSecond");

				/**
				 * 
				 */
				ActivityControllingPropertiesPage.prototype.setElement = function() {
					m_utils.debug("activity ===>");
					m_utils.debug(this.propertiesPanel.element.modelElement);

					this.measureInput
							.val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:measure"]);
					this.targetMeasureQuantityInput
							.val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:targetMeasureQuantity"]);
					this.difficultyInput
							.val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:difficulty"]);
					this.targetProcessingTimeInput
							.val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:targetProcessingTime"]);
					this.targetExecutionTimeInput
							.val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:targetExecutionTime"]);
					this.targetIdleTimeInput
							.val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:targetIdleTime"]);
					this.targetWaitingTimeInput
							.val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:targetWaitingTime"]);
					this.targetQueueDepthInput
							.val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:targetQueueDepth"]);
					this.targetCostPerExecutionInput
							.val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:targetCostPerExecution"]);
					this.targetCostPerSecondInput
							.val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:targetCostPerSecond"]);
				};
			}
		});