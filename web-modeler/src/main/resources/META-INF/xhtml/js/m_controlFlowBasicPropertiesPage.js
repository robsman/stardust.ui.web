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
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "basicPropertiesPage", "Basic");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ControlFlowBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				ControlFlowBasicPropertiesPage.prototype.initialize = function() {
					this.otherwiseInput = this.mapInputId("otherwiseInput");
					this.conditionExpressionInput = this
							.mapInputId("conditionExpressionInput");
					this.descriptionInput = this.mapInputId("descriptionInput");
					this.conditionPanel = this.mapInputId("conditionPanel");

					this.otherwiseInput.click({
						"page" : this
					}, function(event) {
						if (event.data.page.otherwiseInput.is(":checked")) {
							event.data.page.conditionExpressionInput.attr(
									"disabled", true);
							event.data.page.conditionExpressionInput.val(null);
						} else {
							event.data.page.conditionExpressionInput
									.removeAttr("disabled");
							event.data.page.conditionExpressionInput
									.val("true");
						}
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
						this.otherwiseInput
								.attr(
										"checked",
										this.propertiesPanel.element.modelElement.otherwise);
						this.conditionExpressionInput
								.val(this.propertiesPanel.element.modelElement.conditionExpression);

						if (this.propertiesPanel.element.modelElement.otherwise) {
							this.conditionExpressionInput
									.attr("disabled", true);
						} else {
							this.conditionExpressionInput.removeAttr("disabled");
						}

						this.conditionPanel.removeAttr("class");
					} else {
						this.conditionPanel.attr("class", "invisible");
					}
				};
			}
		});