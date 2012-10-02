/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_communicationController", "m_command",
				"m_commandsController", "m_dialog", "m_modelElementView",
				"m_model", "m_propertiesTree" ],
		function(m_utils, m_constants, m_communicationController, m_command,
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
				XsdStructuredDataTypeView.prototype.initialize = function(
						typeDeclaration) {
					this.initializeModelElementView();
					this.initializeModelElement(typeDeclaration);

					this.typeDeclaration = typeDeclaration;

					m_utils.debug("===> Type Declaration");
					m_utils.debug(this.typeDeclaration);

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
										event.data.view.typeDeclaration.schema.types[this.typeDeclaration.id].body = "sequence";

										event.data.view.enumerationRadioButton
												.attr("checked", false);

										event.data.view
												.resumeTableForSequenceDefinition();
									});
					this.enumerationRadioButton
							.click(
									{
										"view" : this
									},
									function(event) {
										event.data.view.this.typeDeclaration.schema.types[this.typeDeclaration.id].body = "enumeration";
										event.data.view.structureRadioButton
												.attr("checked", false);

										event.data.view
												.resumeTableForSequenceDefinition();
									});

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
					if (this.typeDeclaration.isSequence()) {
						this.structureRadioButton.attr("checked", true);
						this.enumerationRadioButton.attr("checked", false);
						this.resumeTableForSequenceDefinition();
					} else {
						this.structureRadioButton.attr("checked", false);
						this.enumerationRadioButton.attr("checked", true);
						this.resumeTableForEnumerationDefinition();
					}
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.addElement = function() {
					if (this.typeDeclaration.isSequence())
						{
					this.typeDeclaration.getBody().elements["New"
							+ this.getElementCount()] = {
						name : "New" + this.getElementCount(),
						type : "xsd:string",
						cardinality : "required"
					};
					this.submitChanges({
						typeDeclaration : this.typeDeclaration.typeDeclaration
					});

					this.resumeTableForSequenceDefinition();
						}
					else
						{
						this.getFacets()["New"
												+ this.getElementCount()] = {classifier: "enumeration",
							name: "New" + this.getElementCount()};
						this.submitChanges({
							typeDeclaration : this.typeDeclaration.typeDeclaration
						});
										this.resumeTableForEnumerationDefinition();						
						}
				};

				XsdStructuredDataTypeView.prototype.getElementCount = function() {
					var n = 0;

					if (this.typeDeclaration.isSequence())
						{
					for ( var element in this.typeDeclaration.getBody().elements) {
						++n;
					}
						}
					else
						{
						for ( var element in this.getFacets()) {
							++n;
						}						
						}

					return n;
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.resumeTableForSequenceDefinition = function() {
					this.tableBody.empty();

					// Find root schema

					var n = 0;

					for ( var element in this.typeDeclaration.getBody().elements) {
						var path = element.replace(/:/g, "-");

						var content = "<tr id='" + path + "'>";

						content += "<td class='elementCell'>";
						content += "<input type='text' value='"
								+ this.typeDeclaration.getBody().elements[element].name + "'/>";
						content += "</td>";
						content += "<td class='typeCell'>";

						if (this.typeDeclaration.getBody().classifier == "sequence") {
							content += this
									.getTypeSelectList(this.typeDeclaration.getBody().elements[element].type);
						}

						content += "<td class='cardinalityCell'>";

						if (this.typeDeclaration.getBody().classifier == "sequence") {
							content += ("<select size=\"1\" class=\"cardinalitySelect\"><option value=\"1\""
									+ (this.typeDeclaration.getBody().elements[element].cardinality == "required" ? "selected"
											: "")
									+ ">Required</option><option value=\"N\""
									+ (this.typeDeclaration.getBody().elements[element].cardinality == "many" ? "selected"
											: "") + ">Many</option></select>");
						}

						content += "</td>";
						content += "</tr>";

						this.tableBody.append(content);
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
				XsdStructuredDataTypeView.prototype.resumeTableForEnumerationDefinition = function() {
					this.tableBody.empty();

					var n = 0;

					for ( var element in this.typeDeclaration.getFacets()) {
						var content = "<tr id='" + element + "'>";

						content += "<td>";
						content += "<input type='text' value='"
								+ this.typeDeclaration.getFacets()[element].name + "'/>";
						content += "</td>";
						content += "<td>";
						content += "</td>";
						content += "<td>";
						content += "</td>";
						content += "</tr>";

						this.tableBody.append(content);
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
					this.initializeTypeDeclaration();
				};

				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.getTypeSelectList = function(
						type) {
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
							&& object.changes.modified[0].uuid == this.typeDeclaration.uuid) {
						this.initialize(this.typeDeclaration);
					}
				};
			}
		});