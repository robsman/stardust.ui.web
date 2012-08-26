/**
 * @author Marc.Gille
 */

define(
		[ "m_utils", "m_constants", "m_dialog", "m_propertiesPage",
				"m_typeDeclaration" ],
		function(m_utils, m_constants, m_dialog, m_propertiesPage,
				m_typeDeclaration) {
			return {
				create : function(propertiesPanel) {
					var page = new ProcessProcessInterfacePropertiesPage(
							propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function ProcessProcessInterfacePropertiesPage(newPropertiesPanel,
					newId, newTitle) {
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "processInterfacePropertiesPage",
						"Process Interface");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ProcessProcessInterfacePropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.initialize = function() {
					this.processInterfaceTypeSelectInput = this
							.mapInputId("processInterfaceTypeSelectInput");
					this.noInterfacePanel = this.mapInputId("noInterfacePanel");
					this.providesProcessInterfacePanel = this
							.mapInputId("providesProcessInterfacePanel");
					this.implementsProcessInterfacePanel = this
							.mapInputId("implementsProcessInterfacePanel");
					this.parameterDefinitionsTable = this
							.mapInputId("parameterDefinitionsTable");
					this.parameterDefinitionsTableBody = this
							.mapInputId("parameterDefinitionsTable tbody");
					this.processInterfaceFromDataCreationWizardLink = this
							.mapInputId("processInterfaceFromDataCreationWizardLink");
					this.processDataTableBody = jQuery("#processDataTable tbody"); // TODO
					// embed
					// dialog
					// into
					// DIV

					this.processInterfaceTypeSelectInput
							.change(
									{
										"callbackScope" : this
									},
									function(event) {
										if (event.data.callbackScope.processInterfaceTypeSelectInput
												.val() == "noInterface") {
											event.data.callbackScope
													.setNoInterface();
										} else if (event.data.callbackScope.processInterfaceTypeSelectInput
												.val() == "providesProcessInterface") {
											event.data.callbackScope
													.setProvidesProcessInterface();
										} else if (event.data.callbackScope.processInterfaceTypeSelectInput
												.val() == "implementsProcessInterface") {
											event.data.callbackScope
													.setImplementsProcessInterface();
										}
									});

					this.processInterfaceFromDataCreationWizardLink.click({
						"callbackScope" : this
					}, function(event) {
						jQuery("#processInterfaceFromDataCreationWizard")
								.dialog("open");
					});

					jQuery("#processInterfaceFromDataCreationWizard").dialog({
						autoOpen : false,
						draggable : true
					});

					jQuery(
							"#processInterfaceFromDataCreationWizard #cancelButton")
							.click(
									function() {
										jQuery(
												"#processInterfaceFromDataCreationWizard")
												.dialog("close");
									});

					jQuery(
							"#processInterfaceFromDataCreationWizard #generateButton")
							.click(
									{
										"page" : this
									},
									function(event) {
										event.data.page.parameterDefinitionsTableBody
												.empty();

										var rows = jQuery("#processDataTable tbody tr");

										for ( var n = 0; n < rows.length; ++n) {
											var row = rows[n];
											var dataSymbol = jQuery.data(row,
													"dataSymbol");

											if (jQuery(
													"#processDataTable tbody tr input:eq("
															+ n + ")").is(
													":checked")) {
												var content = "<tr id=\"parameterRow-"
														+ n + "\">";

												content += "<td>";
												content += "<input type=\"text\" value=\""
														+ dataSymbol.dataName
														+ "\" class=\"nameInput\"></input>";
												content += "</td>";

												content += "<td>";
												content += event.data.page
														.getTypeSelectList("");
												content += "</td>";

												content += "<td align=\"right\">";
												content += ("<select size=\"1\" class=\"directionSelect\"><option value=\"IN\">IN</option>"
														+ "<option value=\"OUT\">OUT</option>"
														+ "<option value=\"INOUT\">INOUT</option>"
														+ "</select>");
												content += "</td>";

												content += "<td>";
												content += "<input type=\"text\" value=\""
														+ dataSymbol.dataName
														+ "\" class=\"nameInput\"></input>";
												content += "</td>";
												content += "</tr>";

												event.data.page.parameterDefinitionsTableBody
														.append(content);

												jQuery(
														"#parameterDefinitionsTable tbody tr #parameterRow-"
																+ n + " select")
														.val(
																jQuery(
																		"#processDataTable tbody tr select:eq("
																				+ n
																				+ ")")
																		.val());
											}
										}

										event.data.page.parameterDefinitionsTableBody
												.append("<tr id=\"newRow\"><td><a id=\"newLink\"><img src=\"../../images/icons/add.png\"/></a></td><td></td><td></td><td></td>");

										jQuery(
												"#processInterfaceFromDataCreationWizard")
												.dialog("close");
									});
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.populateProcessDataTable = function() {
					this.processDataTableBody.empty();

					var dataSymbols = this.propertiesPanel.diagram.dataSymbols;

					for ( var n in dataSymbols) {
						var dataSymbol = dataSymbols[n];
						var row = "<tr id=\"data-" + dataSymbol.oid + "\">";

						row += "<td>";
						row += "<input type=\"checkbox\">";
						row += "</td>";
						row += "<td>";
						row += dataSymbol.dataName;
						row += "</td>";
						row += "<td>";
						row += "<select>";
						row += "<option value=\"IN\">IN</option>";
						row += "<option value=\"OUT\">OUT</option>";
						row += "<option value=\"INOUT\">INOUT</option>";
						row += "</select>";
						row += "</td>";
						row += "</tr>";

						this.processDataTableBody.append(row);

						jQuery("#processDataTable #data-" + dataSymbol.oid)
								.data("dataSymbol", dataSymbol);
					}
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.setNoInterface = function() {
					this.processInterfaceTypeSelectInput.val("noInterface");
					m_dialog.makeVisible(this.noInterfacePanel);
					m_dialog.makeInvisible(this.providesProcessInterfacePanel);
					m_dialog
							.makeInvisible(this.implementsProcessInterfacePanel);
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.setProvidesProcessInterface = function() {
					this.processInterfaceTypeSelectInput
							.val("providesProcessInterface");
					m_dialog.makeInvisible(this.noInterfacePanel);
					m_dialog.makeVisible(this.providesProcessInterfacePanel);
					m_dialog
							.makeInvisible(this.implementsProcessInterfacePanel);
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.setImplementsProcessInterface = function() {
					this.processInterfaceTypeSelectInput
							.val("implementsProcessInterface");
					m_dialog.makeInvisible(this.noInterfacePanel);
					m_dialog.makeInvisible(this.providesProcessInterfacePanel);
					m_dialog.makeVisible(this.implementsProcessInterfacePanel);
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.initializeParameterDefinitionsTable = function() {
					this.parameterDefinitionsTableBody.empty();

					for ( var m in this.propertiesPanel.element.formalParameters) {
						var formalParameter = this.propertiesPanel.element.formalParameters[m];

						this.parameterDefinitionsTableBody.append("<tr><td>"
								+ formalParameter.name
								+ "</td><td></td><td></td><td></td>");
					}

					this.parameterDefinitionsTableBody
							.append("<tr id=\"newRow\"><td><a id=\"newLink\"><img src=\"../../images/icons/add.png\"/></a></td><td></td><td></td><td></td>");

					// Initialize event handling

					// jQuery("table#typeDeclarationsTable #newRow #newLink")
					// .click({
					// "view" : this
					// }, function(event) {
					// m_utils.debug("Clicked");
					// event.data.view.addElement();
					// });
					// jQuery("table#typeDeclarationsTable .nameInput")
					// .change(
					// {
					// "view" : this
					// },
					// function(event) {
					// jQuery(this).parent().parent().data().schemaElement.name
					// = jQuery(
					// this).val();
					// });
					// jQuery("table#typeDeclarationsTable .typeSelect")
					// .change(
					// {
					// "view" : this
					// },
					// function(event) {
					// jQuery(this).parent().parent().data().schemaElement.typeName
					// = jQuery(
					// this).val();
					// });
					// jQuery("table#typeDeclarationsTable .cardinalitySelect")
					// .change(
					// {
					// "view" : this
					// },
					// function(event) {
					// jQuery(this).parent().parent().data().schemaElement.cardinality
					// = jQuery(
					// this).val();
					// });

					this.parameterDefinitionsTable.tableScroll({
						height : 150
					});
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element;
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.setElement = function() {
					if (this.getModelElement().processInterfaceType == m_constants.NO_PROCESS_INTERFACE_KEY) {
						this.setNoInterface();
					} else if (this.getModelElement().processInterfaceType == m_constants.PROVIDES_PROCESS_INTERFACE_KEY) {
						this.setProvidesProcessInterface();
					} else if (this.getModelElement().processInterfaceType == m_constants.IMPLEMENTS_PROCESS_INTERFACE_KEY) {
						this.setImplementsProcessInterface();
					}

					this.initializeParameterDefinitionsTable();
					this.populateProcessDataTable();

					m_utils.debug("Process");
					m_utils.debug(this.getModelElement());
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.validate = function() {
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.apply = function() {
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.getTypeSelectList = function(
						type) {
					var select = "<select size=\"1\" class=\"typeSelect\">";

					select += "<option value=\"xsd:string\""
							+ (type == "xsd:string" ? "selected" : "")
							+ ">String</option>";
					select += "<option value=\"xsd:int\""
							+ (type == "xsd:int" ? "selected" : "")
							+ ">Integer</option>";
					select += "<option value=\"xsd:double\""
							+ (type == "xsd:double" ? "selected" : "")
							+ ">Float</option>";
					select += "<option value=\"xsd:decimal\""
							+ (type == "xsd:decimal" ? "selected" : "")
							+ ">Decimal</option>";
					select += "<option value=\"xsd:date\""
							+ (type == "xsd:date" ? "selected" : "")
							+ ">Date</option>";

					var typeDeclarations = m_typeDeclaration
							.getTypeDeclarations();

					for ( var n in typeDeclarations) {
						select += "<option value=\""
								+ typeDeclarations[n].name
								+ "\""
								+ (type == typeDeclarations[n].name ? "selected"
										: "") + ">" + typeDeclarations[n].name
								+ "</option>";
					}

					select += "</select>";

					return select;
				};
			}
		});