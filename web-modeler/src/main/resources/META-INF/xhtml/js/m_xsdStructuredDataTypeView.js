/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_communicationController", "m_command",
				"m_commandsController", "m_dialog", "m_modelElementView",
				"m_model", "m_typeDeclaration", "m_propertiesTree" ],
		function(m_utils, m_communicationController, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_typeDeclaration, m_propertiesTree) {
			return {
				initialize : function(fullId) {
					var view = new XsdStructuredDataTypeView();

					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findDataStructure(fullId));
				}
			};

			/**
			 * 
			 */
			function XsdStructuredDataTypeView() {
				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(XsdStructuredDataTypeView.prototype,
						view);

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.initialize = function(
						structuredDataType) {
					this.initializeModelElementView();
					this.initializeModelElement(structuredDataType);

					this.structuredDataType = structuredDataType;

					m_utils.debug("===> Structured Data");
					m_utils.debug(this.structuredDataType);

					this.tree = jQuery("#typeDeclarationsTable");
					this.tableBody = jQuery("table#typeDeclarationsTable tbody");
					this.urlTextInput = jQuery("#urlTextInput");
					this.urlRow = jQuery("#urlRow");
					this.loadFromUrlButton = jQuery("#loadFromUrlButton");
					this.importFromUrlRadioButton = jQuery("#importFromUrlRadioButton");
					this.defineManuallyRadioButton = jQuery("#defineManuallyRadioButton");
					this.upButton = jQuery("#upButton");
					this.downButton = jQuery("#downButton");
					this.structureDefinitionHintPanel = jQuery("#structureDefinitionHintPanel");
					this.manualDefinitionRadioButtonPanel = jQuery("#manualDefinitionRadioButtonPanel");
					this.structureRadioButton = jQuery("#structureRadioButton");
					this.enumerationRadioButton = jQuery("#enumerationRadioButton");
					this.propertiesTree = m_propertiesTree
							.create("fieldPropertiesTable");

					this.loadFromUrlButton.click({
						"view" : this
					}, function(event) {
						event.data.view.loadFromUrl();
					});

					this.importFromUrlRadioButton.click({
						"view" : this
					}, function(event) {
						event.data.view.initializeForLoadFromUrl();
					});

					this.defineManuallyRadioButton.click({
						"view" : this
					}, function(event) {
						event.data.view.initializeForManualDefinition();
					});

					this.structureRadioButton
							.click(
									{
										"view" : this
									},
									function(event) {
										event.data.view.structuredDataType.typeDeclaration.type = m_typeDeclaration.STRUCTURE_TYPE;

										event.data.view.enumerationRadioButton
												.attr("checked", false);

										event.data.view
												.resumeTableFoManualDefinition();
									});

					this.enumerationRadioButton
							.click(
									{
										"view" : this
									},
									function(event) {
										event.data.view.structuredDataType.typeDeclaration.type = m_typeDeclaration.ENUMERATION_TYPE;
										event.data.view.structureRadioButton
												.attr("checked", false);

										event.data.view
												.resumeTableFoManualDefinition();
									});

					this.initializeForManualDefinition();
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.toString = function() {
					return "Lightdust.XsdStructuredDataTypeView";
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.getLoadFromUrlUrl = function() {
					var url = "/typeDeclarations/loadFromUrl";

					return url;
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.initializeForManualDefinition = function() {
					m_dialog.makeInvisible(this.urlRow);
					m_dialog.makeInvisible(this.structureDefinitionHintPanel);
					m_dialog.makeVisible(this.manualDefinitionRadioButtonPanel);
					this.defineManuallyRadioButton.attr("checked", true);
					this.importFromUrlRadioButton.attr("checked", false);
					this.urlTextInput.attr("disabled", true);
					this.loadFromUrlButton.attr("disabled", true);
					this.upButton.attr("disabled", false);
					this.downButton.attr("disabled", false);

					// TODO Workaround. Remove when JSON structure is fully
					// clear.

					if (this.structuredDataType.typeDeclaration.type == null) {
						this.structuredDataType.typeDeclaration.type = m_typeDeclaration.STRUCTURE_TYPE;
					}

					if (this.structuredDataType.typeDeclaration.type == m_typeDeclaration.STRUCTURE_TYPE) {
						this.structureRadioButton.attr("checked", true);
						this.enumerationRadioButton.attr("checked", false);
					} else {
						this.structureRadioButton.attr("checked", false);
						this.enumerationRadioButton.attr("checked", true);
					}

					this.resumeTableFoManualDefinition();
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.addElement = function() {
					this.structuredDataType.typeDeclaration.addSchemaElement(
							"New"
									+ this.structuredDataType.typeDeclaration
											.getSchemaElementCount(), "String",
							"1");
					this.resumeTableFoManualDefinition();
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.resumeTableFoManualDefinition = function() {
					this.tableBody.empty();

					var n = 0;

					for (schemaElementName in this.structuredDataType.typeDeclaration.children) {
						var schemaElement = this.structuredDataType.typeDeclaration.children[schemaElementName];

						var content = "<tr id=\"schemaElementRow-" + n + "\">";

						content += "<td style=\"width: 400px;\">";
						content += "<input type=\"text\" value=\""
								+ schemaElement.name
								+ "\" class=\"nameInput\"></input>";
						content += "</td>";
						content += "<td style=\"width: 100px;\">";

						if (this.structuredDataType.typeDeclaration.type == m_typeDeclaration.STRUCTURE_TYPE) {
							content += this
									.getTypeSelectList(schemaElement.typeName);
						}

						content += "</td>"
								+ "<td align=\"right\"  style=\"width: 100px;\">";

						if (this.structuredDataType.typeDeclaration.type == m_typeDeclaration.STRUCTURE_TYPE) {
							content += ("<select size=\"1\" class=\"cardinalitySelect\"><option value=\"1\""
									+ (schemaElement.cardinality == "1" ? "selected"
											: "")
									+ ">1</option><option value=\"N\""
									+ (schemaElement.cardinality == "N" ? "selected"
											: "") + ">N</option></select>");
						}

						content += "</td></tr>";

						this.tableBody.append(content);

						jQuery(
								"table#typeDeclarationsTable #schemaElementRow-"
										+ n).data({
							"schemaElement" : schemaElement
						});

						jQuery(
								"table#typeDeclarationsTable #schemaElementRow-"
										+ n + " .deleteLink")
								.click(
										{
											"view" : this,
											"schemaElement" : schemaElement
										},
										function(event) {
											event.data.view
													.removeSchemaElement(event.data.schemaElement);
										});

						++n;
					}

					this.tableBody
							.append("<tr id=\"newRow\"><td><input id=\"newLink\" type=\"image\" src=\"../../images/icons/add.png\"/></td><td></td><td></td>");

					// Initialize event handling

					jQuery("table#typeDeclarationsTable tbody tr").mousedown(
							function() {
								jQuery("tr.selected").removeClass("selected");
								jQuery(this).addClass("selected");
							});
					jQuery("table#typeDeclarationsTable #newRow #newLink")
							.click({
								"view" : this
							}, function(event) {
								m_utils.debug("Clicked");
								event.data.view.addElement();
							});
					jQuery("table#typeDeclarationsTable .nameInput")
							.change(
									{
										"view" : this
									},
									function(event) {
										jQuery(this).parent().parent().data().schemaElement.name = jQuery(
												this).val();
									});
					jQuery("table#typeDeclarationsTable .typeSelect")
							.change(
									{
										"view" : this
									},
									function(event) {
										jQuery(this).parent().parent().data().schemaElement.typeName = jQuery(
												this).val();
									});
					jQuery("table#typeDeclarationsTable .cardinalitySelect")
							.change(
									{
										"view" : this
									},
									function(event) {
										jQuery(this).parent().parent().data().schemaElement.cardinality = jQuery(
												this).val();
									});

					this.tree.tableScroll({
						height : 150
					});
					this.tree.treeTable();
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.removeSchemaElement = function(
						schemaElement) {
					m_utils.debug("Removing " + schemaElement.name);
					delete this.structuredDataType.typeDeclaration.children[schemaElement.name];

					// TODO For performance improvements we just may delete the
					// table row
					this.initializeForManualDefinition();
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.initializeForLoadFromUrl = function() {
					m_dialog.makeVisible(this.urlRow);
					m_dialog.makeVisible(this.structureDefinitionHintPanel);
					m_dialog
							.makeInvisible(this.manualDefinitionRadioButtonPanel);
					this.importFromUrlRadioButton.attr("checked", true);
					this.defineManuallyRadioButton.attr("checked", false);
					this.urlTextInput.attr("disabled", false);
					this.loadFromUrlButton.attr("disabled", false);
					this.upButton.attr("disabled", true);
					this.downButton.attr("disabled", true);
				};

				XsdStructuredDataTypeView.prototype.initializeFromJson = function(
						json) {
					this.setTypeDeclarations(m_typeDeclaration
							.initializeFromJson(json));
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.setTypeDeclarations = function(
						typeDeclarations) {
					m_utils.debug("===> Type Declarations");
					m_utils.debug(typeDeclarations);

					this.tableBody.empty();

					for ( var element in typeDeclarations.elements) {
						var path = element.replace(/:/g, "-");

						var content = "<tr id=\"" + path + "\">";

						content += "<td>";
						content += "<span class=\"data-element\">"
								+ typeDeclarations.elements[element].name
								+ "</span>";
						content += "</td>";
						content += "<td>";
						content += typeDeclarations.elements[element].name;
						content += "</td>";
						content += "<td>";
						content += "</td>";
						content += "</tr>";

						this.tableBody.append(content);

						if (typeDeclarations.elements[element].body != null) {
							this
									.populateRecursively(
											typeDeclarations.elements[element].body.elements,
											path, true);
						}
					}

					this.tree.tableScroll({
						height : 150
					});
					this.tree.treeTable();

					jQuery("table#typeDeclarationsTable tbody tr").mousedown(
							function() {
								jQuery("tr.selected").removeClass("selected");
								jQuery(this).addClass("selected");
							});
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.populateRecursively = function(
						elements, parentPath, readonly) {
					if (elements == null) {
						return;
					}

					for ( var childElement in elements) {
						var path = parentPath + "."
								+ elements[childElement].name;

						var content = "<tr id=\"" + path + "\" "
								+ "class=\"child-of-" + parentPath + "\"" + ">";

						content += "<td>";
						content += "<span class=\"data-element\">"
								+ elements[childElement].name + "</span>";
						content += "</td>";
						content += "<td>";

						if (readonly) {
							content += elements[childElement].type;
						} else {
							content += this
									.getTypeSelectList(elements[childElement].type);
						}

						content += "</td>" + "<td align=\"right\">";

						// many, required, optional
						if (readonly) {
							content += elements[childElement].cardinality;
						} else {
							content += ("<select size=\"1\"><option value=\"1\""
									+ (elements[childElement].cardinality == "1" ? "selected"
											: "")
									+ ">1</option><option value=\"N\""
									+ (elements[childElement].cardinality == "N" ? "selected"
											: "") + ">N</option></select>");

						}

						content += "</td></tr>";

						this.tableBody.append(content);

						if (elements[childElement].type != null) {
							this
									.populateRecursively(
											elements[childElement].children,
											path, true);
						}
					}
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.getTypeSelectList = function(
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
				 * <code>structure</code> allows to pass a structure if no
				 * structure cannot be retrieved from the server.
				 */
				XsdStructuredDataTypeView.prototype.loadFromUrl = function(
						structure) {
					var successCallback = {
						callbackScope : this,
						callbackMethod : "setTypeDeclarations"
					};

					jQuery("body").css("cursor", "progress");
					this.clearErrorMessages();
					this.urlTextInput.removeClass("error");

					var view = this;

					m_communicationController
							.syncPostData(
									{
										url : m_communicationController
												.getEndpointUrl()
												+ "/typeDeclarations/loadFromUrl"
									},
									JSON.stringify({
										url : this.urlTextInput.val()
									}),
									{
										"success" : function(serverData) {
											successCallback.callbackScope[successCallback.callbackMethod]
													(serverData);
											jQuery("body")
													.css("cursor", "auto");
										},
										"error" : function() {
											jQuery("body")
													.css("cursor", "auto");
											if (structure == null) {
												view.errorMessages
														.push("Could not load XSD from URL.");
												view.showErrorMessages();
												view.urlTextInput
														.addClass("error");
												view.serviceSelect.empty();
												view.portSelect.empty();
												view.operationSelect.empty();
											} else {
												successCallback.callbackScope[successCallback.callbackMethod]
														(structure);
											}
										}
									});
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Data type name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 * Only react to name changes and validation exceptions.
				 */
				XsdStructuredDataTypeView.prototype.processCommand = function(
						command) {
					m_utils.debug("===> Structured Data Process Command");
					m_utils.debug(command);

					if (command.type == m_constants.CHANGE_USER_PROFILE_COMMAND) {
						this.initialize(this.structuredDataType);

						return;
					}

					var obj = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != obj
							&& null != obj.changes
							&& object.changes[this.structuredDataType.oid] != null) {
						this.nameInput
								.val(object.changes[this.structuredDataType.oid].name);

						// Other attributes
					}
				};
			}
		});