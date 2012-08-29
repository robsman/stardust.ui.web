/**
 * @author Marc.Gille
 */

define(
		[ "m_utils", "m_constants", "m_dialog", "m_propertiesPage",
				"m_typeDeclaration", "m_dataTypeSelector" ],
		function(m_utils, m_constants, m_dialog, m_propertiesPage,
				m_typeDeclaration, m_dataTypeSelector) {
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
					this.currentParameterDefinition = null;
					this.processInterfaceTypeSelectInput = this
							.mapInputId("processInterfaceTypeSelectInput");
					this.noInterfacePanel = this.mapInputId("noInterfacePanel");
					this.providesProcessInterfacePanel = this
							.mapInputId("providesProcessInterfacePanel");
					this.implementsProcessInterfacePanel = this
							.mapInputId("implementsProcessInterfacePanel");
					this.webServiceInterfaceCheckboxInput = this
							.mapInputId("webServiceInterfaceCheckboxInput");
					this.restInterfaceCheckboxInput = this
							.mapInputId("restInterfaceCheckboxInput");
					this.parameterDefinitionsTable = this
							.mapInputId("parameterDefinitionsTable");
					this.parameterDefinitionsTableBody = this
							.mapInputId("parameterDefinitionsTable tbody");
					this.processInterfaceFromDataCreationWizardLink = this
							.mapInputId("processInterfaceFromDataCreationWizardLink");
					this.processDataTableBody = jQuery("#processDataTable tbody"); // TODO
					this.parameterDefinitionNameInput = this
							.mapInputId("parameterDefinitionNameInput");
					this.parameterDefinitionDirectionSelect = this
							.mapInputId("parameterDefinitionDirectionSelect");
					this.parameterDefinitionDataSelect = this
							.mapInputId("parameterDefinitionDataSelect");
					this.parameterDefinitionPathInput = this
							.mapInputId("parameterDefinitionPathInput");
					this.addParameterDefinitionButton = this
							.mapInputId("addParameterDefinitionButton");

					this.dataTypeSelector = m_dataTypeSelector
							.create("parameterDefinitionTypeSelector");

					this.dataTypeSelector.setPrimitiveDataType();

					this.addParameterDefinitionButton.click({
						page : this
					}, function(event) {
						event.data.page.addParameterDefinition();

					});

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

					this.webServiceInterfaceCheckboxInput.change({
						page : this
					}, function(event) {
						event.data.page.submitProtocol();
					});
					this.restInterfaceCheckboxInput.change({
						page : this
					}, function(event) {
						event.data.page.submitProtocol();
					});
					this.parameterDefinitionNameInput
							.change(
									{
										page : this
									},
									function(event) {
										if (event.data.page.currentParameterDefinition != null) {
											event.data.page.currentParameterDefinition.name = event.data.page.parameterDefinitionNameInput
													.val();
											event.data.page
													.initializeParameterDefinitionsTable();
										}
									});
					this.parameterDefinitionDirectionSelect
							.change(
									{
										page : this
									},
									function(event) {
										if (event.data.page.currentParameterDefinition != null) {
											event.data.page.currentParameterDefinition.direction = event.data.page.parameterDefinitionDirectionSelect
													.val();
											event.data.page
													.initializeParameterDefinitionsTable();
										}
									});
					this.parameterDefinitionDataSelect
							.change(
									{
										page : this
									},
									function(event) {
										if (event.data.page.currentParameterDefinition != null) {
											event.data.page.currentParameterDefinition.dataFullId = event.data.page.parameterDefinitionDataSelect
													.val();
											event.data.page
													.initializeParameterDefinitionsTable();
										}
									});
					this.parameterDefinitionPathInput
							.change(
									{
										page : this
									},
									function(event) {
										if (event.data.page.currentParameterDefinition != null) {
											event.data.page.currentParameterDefinition.path = event.data.page.parameterDefinitionPathInput
													.val();
											event.data.page
													.initializeParameterDefinitionsTable();
										}
									});

					this.populateDataItemsList();
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
				ProcessProcessInterfacePropertiesPage.prototype.populateDataItemsList = function() {
					this.parameterDefinitionDataSelect.empty();

					for ( var n in this.propertiesPanel.models) {
						for ( var m in this.propertiesPanel.models[n].dataItems) {
							var dataItem = this.propertiesPanel.models[n].dataItems[m];

							this.parameterDefinitionDataSelect
									.append("<option value='"
											+ dataItem.getFullId()
											+ "'>"
											+ this.propertiesPanel.models[n].name
											+ "/" + dataItem.name + "</option>");
						}
					}
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.initializeParameterDefinitionsTable = function() {
					this.parameterDefinitionsTableBody.empty();

					for ( var m in this.getModelElement().formalParameters) {
						var formalParameter = this.getModelElement().formalParameters[m];
						var content = "<tr id=\"" + m + "\"><td class=\"";

						if (formalParameter.direction == "IN") {
							content += "outDataPathListItem";
						} else {
							content += "inDataPathListItem";
						}

						content += "\"><td>" + formalParameter.name;

						content += "</td><td>" + formalParameter.direction
						content += "</td><td>" + formalParameter.path + "</td>";

						this.parameterDefinitionsTableBody.append(content);

						jQuery("table#parameterDefinitionsTable tr")
								.mousedown(
										{
											page : this
										},
										function(event) {
											event.data.page
													.deselectParameterDefinitions();
											jQuery("tr.selected").removeClass(
													"selected");
											jQuery(this).addClass("selected");

											var id = jQuery(this).attr("id");

											event.data.page.currentParameterDefinition = event.data.page
													.getModelElement().formalParameters[id];
											event.data.page
													.populateParameterDefinitionFields();
										});
					}

					// Initialize event handling

					this.parameterDefinitionsTable.tableScroll({
						height : 150
					});
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.deselectParameterDefinitions = function(
						dataPath) {
					jQuery("table#parameterDefinitionsTable tr.selected")
							.removeClass("selected");
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.populateParameterDefinitionFields = function() {
					this.parameterDefinitionNameInput
							.val(this.currentParameterDefinition.name);
					this.parameterDefinitionDirectionSelect
							.val(this.currentParameterDefinition.direction);
					this.parameterDefinitionDataSelect
							.val(this.currentParameterDefinition.dataFullId);
					this.parameterDefinitionPathInput
							.val(this.currentParameterDefinition.path);
					this.dataTypeSelector
							.setDataType(this.currentParameterDefinition);
				};

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.addParameterDefinition = function() {
					var n = 1;

					if (this.getModelElement().formalParameters == null) {
						this.getModelElement().formalParameters = {};
					} else {
						for ( var m in this.getModelElement().formalParameters) {
							++n;
						}
					}

					this.currentParameterDefinition = {
						id : "New" + n,
						name : "New " + n,
						dataFullId : null,
						direction : "IN",
						path : null
					};

					this.dataTypeSelector
							.getDataType(this.currentParameterDefinition);

					this.getModelElement().formalParameters[this.currentParameterDefinition.id] = this.currentParameterDefinition;

					// TODO Replace by submit

					this.initializeParameterDefinitionsTable();

					this.populateParameterDefinitionFields();
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
					this.dataTypeSelector
							.setScopeModel(this.getModelElement().model);
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

				/**
				 * 
				 */
				ProcessProcessInterfacePropertiesPage.prototype.submitProtocol = function() {
					var attributes = {};

					if (this.webServiceInterfaceCheckboxInput.is(":checked")) {
						if (this.restInterfaceCheckboxInput.is(":checked")) {
							attributes["carnot:engine:externalInvocationType"] = "both";
						} else {
							attributes["carnot:engine:externalInvocationType"] = "SOAP";
						}
					} else {
						if (this.restInterfaceCheckboxInput.is(":checked")) {
							attributes["carnot:engine:externalInvocationType"] = "REST";
						} else {
							attributes["carnot:engine:externalInvocationType"] = "none";
						}
					}

					this.propertiesPanel.submitChanges({
						attributes : attributes
					});
				};
			}
		});