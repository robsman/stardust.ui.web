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
		[ "m_utils", "m_constants", "m_dialog", "m_propertiesPage" ],
		function(m_utils, m_constants, m_dialog, m_propertiesPage) {
			return {
				create : function(propertiesPanel) {
					return new ActivityControllingPropertiesPage(
							propertiesPanel);
				}
			};

			/**
			 * 
			 */
			function ActivityControllingPropertiesPage(propertiesPanel) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "controllingPropertiesPage",
						"Controlling",
						"../../images/icons/controlling-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ActivityControllingPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				this.targetCostPerExecutionInput = this.mapInputId("targetCostPerExecutionInput");
				this.targetProcessingTimeInput = this.mapInputId("targetProcessingTimeInput");
				this.targetExecutionTimeInput = this.mapInputId("targetExecutionTimeInput");
				this.targetIdleTimeInput = this.mapInputId("targetIdleTimeInput");
				this.targetWaitingTimeInput = this.mapInputId("targetWaitingTimeInput");
				this.targetQueueDepthInput = this.mapInputId("targetQueueDepthInput");
				this.targetCostPerExecutionInput = this.mapInputId("targetCostPerExecutionInput");
				this.resourcePerformanceCalculationSelect = this.mapInputId("resourcePerformanceCalculationSelect");

				m_dialog.registerForNumericFormatValidation(targetCostPerExecutionInput);
				m_dialog.registerForNumericFormatValidation(targetQueueDepthInput);
				
				// Change handling

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

				/**
				 * 
				 */
				ActivityControllingPropertiesPage.prototype.setElement = function() {
					m_utils.debug("activity ===>");
					m_utils.debug(this.propertiesPanel.element.modelElement);

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
					// this.resourcePerformanceCalculationSelect
					// .val(this.propertiesPanel.element.modelElement.attributes["carnot:pwh:targetCostPerSecond"]);
				};
			}
		});