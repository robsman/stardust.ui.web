/**
 * @author Marc.Gille
 */
define(
		[ "jquery", "m_utils", "m_constants", "m_communicationController", "m_command",
				"m_commandsController", "m_dialog", "m_modelElementView",
				"m_model", "m_propertiesTree" ],
		function(jQuery, m_utils, m_constants, m_communicationController, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_propertiesTree) {
			return {
				initialize : function(fullId) {
					var view = new XsdStructuredDataTypeView();

					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findTypeDeclaration(fullId));
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
				XsdStructuredDataTypeView.prototype.initialize = function(typeDeclaration) {

					this.id = "xsdStructuredDataTypeView";
					this.tree = jQuery("table#typeDeclarationsTable");
					this.tableBody = jQuery("table#typeDeclarationsTable tbody");
					this.deleteButton = jQuery("#deleteElementButton");
					this.upButton = jQuery("#moveElementUpButton");
					this.downButton = jQuery("#moveElementDownButton");
					this.structureDefinitionHintPanel = jQuery("#structureDefinitionHintPanel");
					this.structureRadioButton = jQuery("#structureRadioButton");
					this.enumerationRadioButton = jQuery("#enumerationRadioButton");

					this.propertiesTree = m_propertiesTree.create("fieldPropertiesTable");

					var view = this;
					this.structureRadioButton.click(
						function(event) {
							view.typeDeclaration.switchToComplexType();

							view.submitChanges({
								typeDeclaration : view.typeDeclaration.typeDeclaration
							});

							view.initializeTypeDeclaration();
						});
					this.enumerationRadioButton.click(
						function(event) {
							view.typeDeclaration.switchToEnumeration();

							view.submitChanges({
								typeDeclaration : view.typeDeclaration.typeDeclaration
							});

							view.initializeTypeDeclaration();
						});
					this.initializeModelElementView(typeDeclaration);
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.setModelElement = function(typeDeclaration) {
					this.initializeModelElement(typeDeclaration);

					this.typeDeclaration = typeDeclaration;

					m_utils.debug("===> Type Declaration");
					m_utils.debug(this.typeDeclaration);

					this.initializeTypeDeclaration();
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
				XsdStructuredDataTypeView.prototype.initializeTypeDeclaration = function() {
					this.structureRadioButton.attr("checked", this.typeDeclaration.isSequence());
					this.enumerationRadioButton.attr("checked", !this.typeDeclaration.isSequence());

					this.refreshElementsTable();
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.addElement = function() {
					var newElement = this.typeDeclaration.addElement(/* generate default name */);

					this.submitChanges({
						typeDeclaration : this.typeDeclaration.typeDeclaration
					});

					this.refreshElementsTable();
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.renameElement = function(tableRows, newName) {
					var view = this;

					jQuery(tableRows).each(function(i, tableRow) {
						var typeDeclaration = jQuery(tableRow).data("typeDeclaration");
						typeDeclaration.renameElement(jQuery(tableRow).data("elementName"), newName);

						view.submitChanges({
							typeDeclaration : typeDeclaration.typeDeclaration
						});

						view.refreshElementsTable();
					});
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.setElementType = function(tableRows, typeName) {
					var view = this;

					jQuery(tableRows).each(function(i, tableRow) {
						var typeDeclaration = jQuery(tableRow).data("typeDeclaration");
						typeDeclaration.setElementType(jQuery(tableRow).data("elementName"), typeName);

						view.submitChanges({
							typeDeclaration : typeDeclaration.typeDeclaration
						});

						view.refreshElementsTable();
					});
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.setElementCardinality = function(tableRows, cardinality) {
					var view = this;

					jQuery(tableRows).each(function(i, tableRow) {
						var typeDeclaration = jQuery(tableRow).data("typeDeclaration");
						typeDeclaration.setElementCardinality(jQuery(tableRow).data("elementName"), cardinality);

						view.submitChanges({
							typeDeclaration : typeDeclaration.typeDeclaration
						});

						view.refreshElementsTable();
					});
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.removeElement = function(tableRows) {
					var view = this;

					jQuery(tableRows).each(function(i, tableRow) {
						var typeDeclaration = jQuery(tableRow).data("typeDeclaration");
						typeDeclaration.removeElement(jQuery(tableRow).data("elementName"));

						view.submitChanges({
							typeDeclaration : typeDeclaration.typeDeclaration
						});

						view.refreshElementsTable();
					});
				};

				XsdStructuredDataTypeView.prototype.refreshElementsTable = function() {
					// TODO merge instead of fully rebuild table
					this.tableBody.empty();

					// Find root schema

					var n = 0;

					var view = this;
					jQuery.each(this.typeDeclaration.getElements(), function(i, element) {
						var path = element.name.replace(/:/g, "-");

						var newRow = jQuery("<tr id='" + path + "'></tr>");

						var nameColumn = jQuery("<td></td>").appendTo(newRow);
						nameColumn.append("<input class='nameInput' type='text' value='" + element.name + "'/>");

						var typeColumn = jQuery("<td></td>").appendTo(newRow);
						if (view.typeDeclaration.isSequence()) {
							typeColumn.append(view.getTypeSelectList(element.type));
						}

						var cardinalityColumn = jQuery("<td></td>").appendTo(newRow);
						if (view.typeDeclaration.isSequence()) {
							cardinalityColumn.append("<select size='1' class='cardinalitySelect'>"
								+ "  <option value='required'" + (element.cardinality == "required" ? "selected" : "") + ">Required</option>"
								+ "  <option value='many'" + (element.cardinality == "many" ? "selected" : "") + ">Many</option>"
								+ "</select>");
						}

						newRow.appendTo(view.tableBody);
						newRow.data("typeDeclaration", view.typeDeclaration);
						newRow.data("elementName", element.name);
					});

					this.tableBody.append("<tr id='newRow'>"
						+ "  <td><input id='newLink' type='image' src='../../images/icons/add.png'/></td>"
						+ "  </td><td>"
						+ "  </td><td>"
						+ "</tr>");

					this.bindTableEventHandlers();

					this.tree.tableScroll({
						height : 150
					});

					this.tree.treeTable();
				};

				/**
				 * Initialize event handling
				 */
				XsdStructuredDataTypeView.prototype.bindTableEventHandlers = function() {
					var view = this;

					jQuery("tr", this.tableBody).mousedown(
						function() {
							jQuery("tr.selected", view.tableBody).removeClass("selected");
							jQuery(this).addClass("selected");
						});

					jQuery("#newRow #newLink", this.tree).click(
						function(event) {
							view.addElement();
						});
					jQuery(this.deleteButton).click(
						function(event) {
							view.removeElement(jQuery("tr.selected", view.tableBody));
						});

					jQuery(".nameInput", this.tree).change(
						function(event) {
							view.renameElement(jQuery(event.target).closest("tr"), jQuery(event.target).val());
						});
					jQuery(".typeSelect", this.tree).change(
						function(event) {
							view.setElementType(jQuery(event.target).closest("tr"), jQuery(event.target).val());
						});
					jQuery(".cardinalitySelect", this.tree).change(
						function(event) {
							view.setElementCardinality(jQuery(event.target).closest("tr"), jQuery(event.target).val());
						});
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.removeSchemaElement = function(schemaElement) {
					m_utils.debug("Removing " + schemaElement.name);
					delete this.structuredDataType.typeDeclaration.children[schemaElement.name];

					// TODO For performance improvements we just may delete the
					// table row
					this.initializeTypeDeclaration();
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.getTypeSelectList = function(type) {
					var select = "<select size=\"1\" class=\"typeSelect\">";

					select += "<optgroup label=\"Primitive Data\">";

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

					select += "</optgroup><optgroup label=\"This Model\">";

					for ( var n in this.typeDeclaration.model.typeDeclarations) {
						var typeDeclaration = this.typeDeclaration.model.typeDeclarations[n];

						if (typeDeclaration.typeDeclaration.schema.elements[typeDeclaration.id] == null) {
							continue;
						}

						select += "<option value=\""
								+ typeDeclaration.typeDeclaration.schema.elements[typeDeclaration.id].type
								+ "\""
								+ (type == typeDeclaration.typeDeclaration.schema.elements[typeDeclaration.id].type ? "selected"
										: "") + ">" + typeDeclaration.name
								+ "</option>";
					}

					// select += "</optgroup><optgroup label=\"Other Models\">";
					//
					// for ( var i in m_model.getModels()) {
					// var model = m_model.getModels()[i];
					//
					// if (model == this.typeDeclaration.model) {
					// continue;
					// }
					//
					// for ( var n in model.typeDeclarations) {
					// var typeDeclaration = model.typeDeclarations[n];
					//
					// if (typeDeclaration.schema.elements[typeDeclaration.id]
					// == null) {
					// continue;
					// }
					//
					// select += "<option value=\""
					// +
					// typeDeclaration.schema.elements[typeDeclaration.id].type
					// + "\""
					// + (type == typeDeclaration.name ? "selected"
					// : "") + ">" + model.name + "/" + typeDeclaration.name
					// + "</option>";
					// }
					// }
					//
					// select += "</optgroup>";
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
			}
		});