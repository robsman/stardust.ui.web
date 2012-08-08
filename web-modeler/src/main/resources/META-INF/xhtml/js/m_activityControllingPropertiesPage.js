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
		[ "m_utils", "m_constants", "m_propertiesPage" ],
		function(m_utils, m_constants, m_propertiesPage) {
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

				this.targetCostPerExecutionInput = jQuery("#"
						+ this.propertiesPanel.id + " #" + this.id
						+ " #targetCostPerExecutionInput");
				this.targetProcessingTimeInput = jQuery("#"
						+ this.propertiesPanel.id + " #" + this.id
						+ " #targetProcessingTimeInput");
				this.targetExecutionTimeInput = jQuery("#"
						+ this.propertiesPanel.id + " #" + this.id
						+ " #targetExecutionTimeInput");
				this.targetIdleTimeInput = jQuery("#" + this.propertiesPanel.id
						+ " #" + this.id + " #targetIdleTimeInput");
				this.targetWaitingTimeInput = jQuery("#"
						+ this.propertiesPanel.id + " #" + this.id
						+ " #targetWaitingTimeInput");
				this.targetQueueDepthInput = jQuery("#"
						+ this.propertiesPanel.id + " #" + this.id
						+ " #targetQueueDepthInput");
				this.targetCostPerExecutionInput = jQuery("#"
						+ this.propertiesPanel.id + " #" + this.id
						+ " #targetCostPerExecutionInput");
				this.resourcePerformanceCalculationSelect = jQuery("#"
						+ this.propertiesPanel.id + " #" + this.id
						+ " #resourcePerformanceCalculationSelect");

				// Change handling

				this.registerTextInputForModelElementAttributeChangeSubmission(
						this.targetProcessingTimeInput,
						"carnot:pwh:targetProcessingTime");
				this.registerTextInputForModelElementAttributeChangeSubmission(
						this.targetExecutionTimeInput,
						"carnot:pwh:targetExecutionTime");
				this.registerTextInputForModelElementAttributeChangeSubmission(
						this.targetIdleTimeInput, "carnot:pwh:targetIdleTime");
				this.registerTextInputForModelElementAttributeChangeSubmission(
						this.targetWaitingTimeInput,
						"carnot:pwh:targetWaitingTime");
				this.registerTextInputForModelElementAttributeChangeSubmission(
						this.targetQueueDepthInput,
						"carnot:pwh:targetQueueDepth");
				this.registerTextInputForModelElementAttributeChangeSubmission(
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