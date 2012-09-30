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
		[ "m_utils", "m_constants", "m_model", "m_dialog", "m_propertiesPage" ],
		function(m_utils, m_constants, m_model, m_dialog, m_propertiesPage) {
			return {
				create : function(propertiesPanel) {
					var page = new ActivityServiceParametersPropertiesPage(
							propertiesPanel);

					page.initialize();

					return page;
				}
			};

			/**
			 * 
			 */
			function ActivityServiceParametersPropertiesPage(propertiesPanel) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "serviceParametersPropertiesPage",
						"Service Parameters");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ActivityServiceParametersPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				ActivityServiceParametersPropertiesPage.prototype.initialize = function() {
					this.noParametersPanel = this
							.mapInputId("noParametersPanel");
					this.parametersPanel = this.mapInputId("parametersPanel");
					this.parametersTableLabel = this
							.mapInputId("parametersTableLabel");
					this.parametersTable = this.mapInputId("parametersTable");
					this.parametersTableBody = this
							.mapInputId("parametersTableBody");

					this.parametersTable.tableScroll({
						height : 200
					});
				};

				/**
				 * 
				 */
				ActivityServiceParametersPropertiesPage.prototype.setElement = function() {
					m_utils.debug("Activity for Service Parameters ===>");
					m_utils.debug(this.propertiesPanel.element.modelElement);

					if (this.getModelElement().activityType == m_constants.APPLICATION_ACTIVITY_TYPE
							&& this.getModelElement().applicationFullId != null) {
						m_dialog.makeInvisible(this.noParametersPanel);
						m_dialog.makeVisible(this.parametersPanel);

						var application = m_model.findApplication(this
								.getModelElement().applicationFullId);

						this.parametersTableLabel.empty();
						this.parametersTableLabel
								.append("Query parameters for application "
										+ application.name);

						var routeEntries = application.attributes["carnot:engine:camel::routeEntries"];

						if (routeEntries != null
								&& routeEntries.indexOf("isb:")) // TODO
						// Should be
						// more
						// elaborated
						{
							var additionalSpringBeanDefinitions = application.attributes["carnot:engine:camel::additionalSpringBeanDefinitions"];

							m_utils.debug("Spring Bean Definition: "
									+ additionalSpringBeanDefinitions);

							var xmlDoc = jQuery
									.parseXML(additionalSpringBeanDefinitions);

							var xml = jQuery(xmlDoc);

							var parameters = [];
							
							this.parameters = parameters;

							jQuery(xml)
									.find("bean")
									.each(
											function() {
												m_utils.debug("Bean: "
														+ jQuery(this));
												jQuery(this)
														.find("map")
														.each(
																function() {
																	m_utils
																			.debug("Map: "
																					+ jQuery(this));
																	jQuery(this)
																			.find(
																					"entry")
																			.each(
																					function() {
																						if (jQuery(
																								this)
																								.attr(
																										"key") == "name") {
																							parameters
																									.push(jQuery(
																											this)
																											.attr(
																													"value"));
																						}
																					});
																});
											});
						}

						this.populateParametersTable();
					} else {
						m_dialog.makeVisible(this.noParametersPanel);
						m_dialog.makeInvisible(this.parametersPanel);
					}
				};

				/**
				 * 
				 */
				ActivityServiceParametersPropertiesPage.prototype.populateParametersTable = function() {

					this.parametersTableBody.empty();

					for ( var n = 0; n < this.parameters.length; ++n) {
						var parameter = this.parameters[n];
						var rowContent = "<tr id='parameter-" + n + "'>";

						rowContent += "<td>";
						rowContent += "<input type='checkbox'></input>";
						rowContent += "</td>";
						rowContent += "<td>";
						rowContent += parameter;
						rowContent += "</td>";
						rowContent += "</tr>";

						var row = jQuery(rowContent);

						this.parametersTableBody.append(rowContent);
					}
				};
			}
		});