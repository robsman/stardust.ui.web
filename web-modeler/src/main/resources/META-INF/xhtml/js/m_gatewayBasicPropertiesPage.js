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
		[ "m_utils", "m_constants", "m_command", "m_commandsController",
				"m_basicPropertiesPage" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_basicPropertiesPage) {
			return {
				create : function(propertiesPanel) {
					var page = new GatewayBasicPropertiesPage(propertiesPanel);
					
					page.initialize();
					
					return page;
				}
			};

			function GatewayBasicPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var propertiesPage = m_basicPropertiesPage.create(
						newPropertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(GatewayBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				GatewayBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();										

					this.gatewayTypeInput = this.mapInputId("gatewayTypeInput");

					this.gatewayTypeInput
							.change(
									{
										"page" : this
									},
									function(event) {
										var page = event.data.page;

										if (!page.validate()) {
											return;
										}

										if (page.propertiesPanel.element.modelElement.gatewayType != page.gatewayTypeInput
												.val()) {
											page
													.submitChanges({
														modelElement : {
															gatewayType : page.gatewayTypeInput
																	.val()
														}
													});
										}
									});
				};
				
				/**
				 * 
				 */
				GatewayBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();
					this.gatewayTypeInput
							.val(this.propertiesPanel.element.modelElement.attributes.gatewayType);
				};

				/**
				 * 
				 */
				GatewayBasicPropertiesPage.prototype.validate = function() {
					return true;
				};

				/**
				 * 
				 */
				GatewayBasicPropertiesPage.prototype.submitChanges = function(
						changes) {
					m_commandsController
							.submitCommand(m_command
									.createUpdateModelElementCommand(
											this.propertiesPanel.element.diagram.modelId,
											this.propertiesPanel.element.oid,
											changes));
				};
			}
		});