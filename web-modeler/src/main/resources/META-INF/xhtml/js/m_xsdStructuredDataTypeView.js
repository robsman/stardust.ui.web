/**
 * @author Marc.Gille
 */
define(
		[ "jquery", "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_propertiesTree", "bpm-modeler/js/m_structuredTypeBrowser", "bpm-modeler/js/m_i18nUtils" ],
		function(jQuery, m_utils, m_constants, m_communicationController, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_propertiesTree, m_structuredTypeBrowser, m_i18nUtils) {
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

				XsdStructuredDataTypeView.prototype.initializeRow = function(row, element, schemaType) {

					row.data("typeDeclaration", this.typeDeclaration);
					row.data("elementName", element.name);

					var elementName = element.name;
					var propertyName = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.inputText.new");
					elementName = elementName.replace("New", propertyName);
					var nameColumn = jQuery("<td></td>").appendTo(row);
					nameColumn.append("<span class='data-element'><input class='nameInput' type='text' value='" + elementName + "'/></span>");

					var typeColumn = jQuery("<td></td>").appendTo(row);
					if (this.typeDeclaration.isSequence()) {

						if ( !this.typeDeclaration.isReadOnly()) {
							typeColumn.append(this.getTypeSelectList(schemaType));
						} else {
							typeColumn.append(schemaType.name);
						}
					}

					var cardinalityColumn = jQuery("<td></td>").appendTo(row);
					if (this.typeDeclaration.isSequence()) {
						if ( !this.typeDeclaration.isReadOnly()) {
							cardinalityColumn.append("<select size='1' class='cardinalitySelect'>"
									+ "  <option value='required'" + (element.cardinality == "required" ? "selected" : "") + ">" + m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.cardinality.option.required") + "</option>"
									+ "  <option value='many'" + (element.cardinality == "many" ? "selected" : "") + ">" + m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.cardinality.option.many") + "</option>"
									+ "</select>");
						} else {
							cardinalityColumn.append(element.cardinality);
						}
					}
				};

				XsdStructuredDataTypeView.prototype.refreshElementsTable = function() {
					// TODO merge instead of fully rebuild table
					this.tableBody.empty();

					// Find root schema

					var n = 0;

					var view = this;
					var rootSchemaType = this.typeDeclaration.asSchemaType();
					var roots = m_structuredTypeBrowser.generateChildElementRows(null,
							rootSchemaType.isStructure() ? rootSchemaType : rootSchemaType.getElements(),
							jQuery.proxy(this.initializeRow, view));

					jQuery.each(roots, function(i, parentRow) {
						var parentPath = parentRow.data("path");
						var schemaType = parentRow.data("schemaType");
						var childRows = m_structuredTypeBrowser.generateChildElementRows(parentPath, schemaType);

						parentRow.appendTo(view.tableBody);

						jQuery.each(childRows, function(i, childRow) {
							childRow.addClass("child-of-" + parentPath);
							view.tableBody.append(childRow);
						});
					});

					this.tableBody.append("<tr id='newRow'>"
						+ "  <td><span class='data-element'><input id='newLink' type='image' src='../../images/icons/add.png'/></span></td>"
						+ "  </td><td>"
						+ "  </td><td>"
						+ "</tr>");

					this.tree.tableScroll({
						height : 150
					});

					this.tree.treeTable({
						indent: 14,
						onNodeShow: function() {
							m_structuredTypeBrowser.insertChildElementRowsLazily(jQuery(this));
						}
					});

					// bind events after tree got initialized, otherwise renames
					// in parent rows don't get triggered
					this.bindTableEventHandlers();
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

					jQuery(".nameInput", this.tree).on("change", function(event) {
							view.renameElement(jQuery(event.target).closest("tr"), jQuery(event.target).val());
						});
					jQuery(".typeSelect", this.tree).on("change", function(event) {
							view.setElementType(jQuery(event.target).closest("tr"), jQuery(event.target).val());
						});
					jQuery(".cardinalitySelect", this.tree).on("change", function(event) {
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
				XsdStructuredDataTypeView.prototype.getTypeSelectList = function(schemaType) {
					var select = "<select size='1' class='typeSelect'>";

					var xsdTypes = [
		                { id: "xsd:string", label: m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectType.string") },
	                    { id: "xsd:int", label: m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectType.integer") },
		                { id: "xsd:double", label: m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectType.float") },
		                { id: "xsd:decimal", label: m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectType.decimal") },
		                { id: "xsd:date", label: m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectType.date") }
	                ];

					select += "<optgroup label='" + m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveData") + "'>";

					jQuery.each(xsdTypes, function() {
						select += "<option value='" + this.id + "' ";
						if (schemaType.isBuiltinType() && (this.id == schemaType.name)) {
							select += "selected ";
						}
						select += ">" + this.label + "</option>";
					});

					select += "</optgroup>";
					select += "<optgroup label='" + m_i18nUtils.getProperty("modeler.element.properties.commonProperties.thisModel") + "'>";
					jQuery.each(this.typeDeclaration.model.typeDeclarations, function() {
						var typeDeclaration = this;

						var mainElement = typeDeclaration.typeDeclaration.schema.elements[typeDeclaration.id];
						if (mainElement) {
							// consumable type, as there is an equivalent global element
							var elementType = typeDeclaration.resolveSchemaType(mainElement.type);

							select += "<option value='{" + elementType.nsUri +"}" + elementType.name + "' ";
							if ( !schemaType.isBuiltinType() && (null != elementType)) {
								select += ((schemaType.name == elementType.name) && (schemaType.nsUri == elementType.nsUri) ? "selected " : "");
							}
							select += ">" + typeDeclaration.name + "</option>";
						}
					});

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