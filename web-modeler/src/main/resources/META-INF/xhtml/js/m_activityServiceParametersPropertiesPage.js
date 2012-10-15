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

					this.tableRows = [];
				};

				/**
				 * 
				 */
				ActivityServiceParametersPropertiesPage.prototype.setElement = function() {
					if (this.getModelElement().activityType != m_constants.APPLICATION_ACTIVITY_TYPE
							|| this.getModelElement().applicationFullId == null) {
						m_dialog.makeVisible(this.noParametersPanel);
						m_dialog.makeInvisible(this.parametersPanel);

						return;
					}

					var application = m_model.findApplication(this
							.getModelElement().applicationFullId);

					m_utils.debug("Application for Service Parameters ===>");
					m_utils.debug(application);

					if (application.attributes["carnot:engine:camel::routeEntries"] == null
							|| application.attributes["carnot:engine:camel::routeEntries"]
									.indexOf("isb:") <= 0
							|| application.attributes["carnot:engine:camel:serviceConnectorApplicationIntegrationOverlay::serviceType"] != "query") {
						m_dialog.makeVisible(this.noParametersPanel);
						m_dialog.makeInvisible(this.parametersPanel);

						return;
					}

					m_dialog.makeInvisible(this.noParametersPanel);
					m_dialog.makeVisible(this.parametersPanel);

					this.parametersTableLabel.empty();
					this.parametersTableLabel
							.append("Query parameters for application "
									+ application.name);

					this.tableRows = [];

					for ( var m in application.accessPoints) {
						var accessPoint = application.accessPoints[m];

						if (accessPoint.direction == m_constants.OUT_ACCESS_POINT) {
							// TODO Move to m_accessPoint
							var typeDeclaration = this
									.getTypeDeclaration(accessPoint);

							this.initializeTableRowsRecursively(accessPoint,
									typeDeclaration.getBody(), null,
									typeDeclaration.model);
						}
					}

					this.populateTableRows();
					this.parametersTable.tableScroll({
						height : 200
					});
					this.parametersTable.treeTable();
				};

				/**
				 * TODO: Very ugly conversion, because server stores data
				 * reference in a server-specific string.
				 */
				ActivityServiceParametersPropertiesPage.prototype.getTypeDeclaration = function(
						accessPoint) {
					// TODO Workaround for client site programming, this is not
					// what the server returns
					if (accessPoint.structuredDataTypeFullId != null) {
						return m_model
								.findTypeDeclaration(accessPoint.structuredDataTypeFullId);
					}

					var encodedId = accessPoint.attributes["carnot:engine:dataType"];

					if (encodedId == null) {
						return null;
					}

					if (encodedId.indexOf("typeDeclaration") == 0) {
						var parts = encodedId.split("{")[1].split("}");

						return m_model.findTypeDeclaration(parts[0] + ":"
								+ parts[1]);
					} else {
						return this.getModel().typeDeclarations[encodedId];
					}
				};

				/**
				 * 
				 */
				ActivityServiceParametersPropertiesPage.prototype.initializeTableRowsRecursively = function(
						accessPoint, element, parentPath, scopeModel) {
					var path = parentPath == null ? accessPoint.id
							: (parentPath + "." + element.name);
					var tableRow = {};

					this.tableRows.push(tableRow);

					tableRow.accessPoint = accessPoint;
					tableRow.element = element;
					tableRow.path = path;
					tableRow.parentPath = parentPath;
					tableRow.name = parentPath == null ? accessPoint.name
							: element.name;
					tableRow.typeName = parentPath == null ? this
							.getTypeDeclaration(accessPoint).getSchemaName()
							: element.type;

					// Embedded structure

					var childElements = element.elements;

					// Recursive resolution

					if (childElements == null && element.type != null) {
						var typeDeclaration = scopeModel
								.findTypeDeclarationBySchemaName(element.type);

						if (typeDeclaration != null
								&& typeDeclaration.isSequence()) {
							childElements = typeDeclaration.getBody().elements;
						}
					}

					if (childElements == null) {
						return;
					}

					for ( var childElement in childElements) {
						this.initializeTableRowsRecursively(accessPoint,
								childElements[childElement], path, scopeModel);
					}
				};

				/**
				 * 
				 */
				ActivityServiceParametersPropertiesPage.prototype.populateTableRows = function() {
					this.parametersTableBody.empty();

					for ( var tableRow in this.tableRows) {
						var rowId = this.tableRows[tableRow].path.replace(
								/\./g, "-");

						var content = "<tr id=\""
								+ rowId
								+ "\" "
								+ (this.tableRows[tableRow].parentPath != null ? ("class=\"child-of-"
										+ this.tableRows[tableRow].parentPath
												.replace(/\./g, "-") + "\"")
										: "") + ">";

						content += "<td>";
						content += "<span class=\"data-element\">"
								+ this.tableRows[tableRow].name + "</span>";
						content += "</td>";
						content += "<td>" + this.tableRows[tableRow].typeName;
						+"</td>";

						content += "</tr>";

						var row = jQuery(content);

						this.parametersTableBody.append(row);

						row.mousedown({
							panel : this
						}, function(event) {
							jQuery(this).addClass("selected");

							m_utils.debug(jQuery(this));
							
							// TODO Add all selected to property
						});
					}
				};
			}
		});