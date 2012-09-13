/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_communicationController", "m_command",
				"m_commandsController", "m_dialog", "m_modelElementView",
				"m_model", "m_typeDeclaration", "m_propertiesTree" ],
		function(m_utils, m_constants, m_communicationController, m_command,
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
					this.upButton = jQuery("#upButton");
					this.downButton = jQuery("#downButton");
					this.structureDefinitionHintPanel = jQuery("#structureDefinitionHintPanel");
					this.structureRadioButton = jQuery("#structureRadioButton");
					this.enumerationRadioButton = jQuery("#enumerationRadioButton");
					this.propertiesTree = m_propertiesTree
							.create("fieldPropertiesTable");

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
				XsdStructuredDataTypeView.prototype.initializeForManualDefinition = function() {
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

						content += "<td class='elementCell'>";
						content += "<input type=\"text\" value=\""
								+ schemaElement.name
								+ "\" class=\"nameInput\"></input>";
						content += "</td>";
						content += "<td class='typeCell'>";

						if (this.structuredDataType.typeDeclaration.type == m_typeDeclaration.STRUCTURE_TYPE) {
							content += this
									.getTypeSelectList(schemaElement.typeName);
						}

						content += "</td>"
								+ "<td align=\"right\" class='cardinalityCell'>";

						if (this.structuredDataType.typeDeclaration.type == m_typeDeclaration.STRUCTURE_TYPE) {
							content += ("<select size=\"1\" class=\"cardinalitySelect\"><option value=\"1\""
									+ (schemaElement.cardinality == "1" ? "selected"
											: "")
									+ ">Required</option><option value=\"N\""
									+ (schemaElement.cardinality == "N" ? "selected"
											: "") + ">Many</option></select>");
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
				XsdStructuredDataTypeView.prototype.initializeFromJson = function(
						json) {
					this.setTypeDeclarations(m_typeDeclaration
							.initializeFromJson(json));
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
					if (command.type == m_constants.CHANGE_USER_PROFILE_COMMAND) {
						this.initialize(this.structuredDataType);
						
						return;
					}

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object
							&& null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.structuredDataType.oid) {
						this.initialize(this.structuredDataType);
					}
				};
			}
		});