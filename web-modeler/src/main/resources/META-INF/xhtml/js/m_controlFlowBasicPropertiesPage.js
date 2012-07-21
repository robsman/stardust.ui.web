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
					return new ControlFlowBasicPropertiesPage(propertiesPanel);
				}
			};

			/**
			 * 
			 */
			function ControlFlowBasicPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "basicPropertiesPage", "Basic");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ControlFlowBasicPropertiesPage.prototype,
						propertiesPage);

				// Field initialization
				
				this.otherwiseInput = this.mapInputId("otherwiseInput");
				this.conditionExpressionInput = this.mapInputId("conditionExpressionInput");
				this.descriptionInput = this.mapInputId("descriptionInput");
				this.conditionPanel = this.mapInputId("conditionPanel");

				// Initialize callbacks

				this.otherwiseInput.click({
					"callbackScope" : this
				}, function(event) {
					if (event.data.callbackScope.otherwiseInput.is(":checked")) {
						event.data.callbackScope.conditionExpressionInput.attr(
								"disabled", true);
						event.data.callbackScope.conditionExpressionInput.val(null);
					} else {
						event.data.callbackScope.conditionExpressionInput
						.removeAttr("disabled");
						event.data.callbackScope.conditionExpressionInput.val("true");
					}
				});

				/**
				 * 
				 */
				ControlFlowBasicPropertiesPage.prototype.setElement = function() {
					this.descriptionInput
					.val(this.propertiesPanel.element.modelElement.description);

					if (this.propertiesPanel.element.allowsCondition()) {
						this.conditionPanel.removeAttr("class");
						this.conditionExpressionInput
						.val(this.propertiesPanel.element.modelElement.conditionExpression);						
						this.otherwiseInput
						.attr("checked", this.propertiesPanel.element.modelElement.otherwise);
					} else {
						this.conditionPanel.attr("class", "invisible");
					}
				};

				/**
				 * 
				 */
				ControlFlowBasicPropertiesPage.prototype.apply = function() {
					this.propertiesPanel.element.modelElement.description = this.descriptionInput
							.val();
					this.propertiesPanel.element.modelElement.conditionExpression = this.conditionExpressionInput
					.val();
					this.propertiesPanel.element.modelElement.otherwise = this.otherwiseInput.is(":checked");
				};
			}
		});