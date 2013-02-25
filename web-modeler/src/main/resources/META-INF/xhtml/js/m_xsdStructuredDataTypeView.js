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
		[ "jquery", "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_propertiesTree", "bpm-modeler/js/m_typeDeclaration", "bpm-modeler/js/m_structuredTypeBrowser",
				"bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_elementConfiguration", "bpm-modeler/js/m_jsfViewManager", "bpm-modeler/js/m_modelElementUtils",
				"bpm-modeler/js/m_angularContextUtils" ],
		function(jQuery, m_utils, m_constants, m_communicationController, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_propertiesTree, m_typeDeclaration, m_structuredTypeBrowser, m_i18nUtils, m_elementConfiguration, m_jsfViewManager, m_modelElementUtils,
				m_angularContextUtils) {
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
				var viewManager = m_jsfViewManager.create();
				var rowAdded = false;
				var rowMoved = false;

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(XsdStructuredDataTypeView.prototype,
						view);

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.initialize = function(typeDeclaration) {

					this.internationalizeStaticData();

					this.id = "xsdStructuredDataTypeView";
					this.tree = jQuery("table#typeDeclarationsTable");
					this.tableBody = jQuery("table#typeDeclarationsTable tbody");
					this.addButton = jQuery("#addElementButton");
					this.deleteButton = jQuery("#deleteElementButton");
					this.upButton = jQuery("#moveElementUpButton");
					this.downButton = jQuery("#moveElementDownButton");
					this.structureDefinitionHintPanel = jQuery("#structureDefinitionHintPanel");
					this.visibilitySelect = jQuery("#publicVisibilityCheckbox");
					this.structureKindSelect = jQuery("#structureKind select");
					this.minimumLengthEdit = jQuery("#minLenghtInput");
					this.maximumLengthEdit = jQuery("#maxLenghtInput");

					this.propertiesTree = m_propertiesTree.create("fieldPropertiesTable");

					var view = this;

					this.visibilitySelect.change(function(event) {
						var currentVisibility = view.typeDeclaration.attributes["carnot:engine:visibility"];
						var newVisibility = jQuery(event.target).is(":checked") ? "Public" : "Private";
						if (currentVisibility !== newVisibility) {
							view.submitChanges({
										attributes : {
											"carnot:engine:visibility" : newVisibility
										}
									});
						}
					});
					this.structureKindSelect.change(
						function(event) {
							var doSubmit = false;
							if (("struct" === jQuery(event.target).val()) && !view.typeDeclaration.isSequence()) {
								view.typeDeclaration.switchToComplexType();
								doSubmit = true;
							} else if (("enum" === jQuery(event.target).val()) && view.typeDeclaration.isSequence()) {
								view.typeDeclaration.switchToEnumeration();
								doSubmit = true;
							}

							if (doSubmit) {
								view.submitChanges({
									typeDeclaration : view.typeDeclaration.typeDeclaration
								});

								view.initializeTypeDeclaration();
							}
						});

					var self = this;
					m_angularContextUtils.runInAngularContext(function($scope) {
						$scope.$watch("minLength", function(newValue, oldValue) {
							if (newValue !== oldValue && $scope.form.minLenghtInput.$valid) {
								if (newValue == "" || validateRange(parseInt(newValue), $scope.maxLength)){
									if (newValue == "") {
										$scope.minLength = undefined;
										self.typeDeclaration.getTypeDeclaration().minLength = undefined;
									} else {
										self.typeDeclaration.getTypeDeclaration().minLength = parseInt(newValue);
									}
									if ($scope.maxLength) {
										self.typeDeclaration.getTypeDeclaration().maxLength = parseInt($scope.maxLength);
									}
									$scope.minMaxError = false;
									self.submitChanges({
										typeDeclaration : self.typeDeclaration.typeDeclaration
									});
								} else {
									$scope.minMaxError = true;
								}
							}
						});
						$scope.$watch("maxLength", function(newValue, oldValue) {
							if (newValue !== oldValue && $scope.form.maxLenghtInput.$valid) {
								if (newValue == "" || validateRange($scope.minLength, parseInt(newValue))){
									if (newValue == "") {
										$scope.maxLength = undefined;
										self.typeDeclaration.getTypeDeclaration().maxLength = undefined;
									} else {
										self.typeDeclaration.getTypeDeclaration().maxLength = parseInt(newValue);
									}
									if ($scope.minLength) {
										self.typeDeclaration.getTypeDeclaration().minLength = parseInt($scope.minLength);
									}
									$scope.minMaxError = false;
									self.submitChanges({
										typeDeclaration : self.typeDeclaration.typeDeclaration
									});
								} else {
									$scope.minMaxError = true;
								}
							}
						});

						function validateRange(min, max) {
							if (min && max) {
								return (min <= max);
							}

							return true;
						}
					});

					jQuery(this.addButton).click(
						function(event) {
							jQuery("tr.selected", view.tableBody).removeClass("selected")
							view.addElement();
							rowAdded = true;
						});
					jQuery(this.deleteButton).click(
						function(event) {
							view.removeElement(jQuery("tr.selected", view.tableBody));
						});
					jQuery(this.upButton).click(
							function(event) {
								view.moveElementUp(jQuery("tr.selected", view.tableBody));
								rowMoved = true;
							});
					jQuery(this.downButton).click(
							function(event) {
								view.moveElementDown(jQuery("tr.selected", view.tableBody));
								rowMoved = true;
							});

					this.initializeModelElementView(typeDeclaration);
				};

				XsdStructuredDataTypeView.prototype.internationalizeStaticData = function() {
					jQuery("#publicVisibility")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.publicVisibility"));
					jQuery("tr#structureKind td.label")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.structuredTypes.dataStructureType")
											+ ":");
					jQuery("tr#structureKind select option.label-struct")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.structuredTypes.dataStructureType.composite"));
					jQuery("tr#structureKind select option.label-enum")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.structuredTypes.dataStructureType.enumeration"));
					jQuery("tr#minimumLength td.label")
					.text(
							m_i18nUtils
									.getProperty("modeler.model.propertyView.structuredTypes.enumeration.minLength") + ":");
					jQuery("tr#maximumLength td.label")
					.text(
							m_i18nUtils
									.getProperty("modeler.model.propertyView.structuredTypes.enumeration.maxLength") + ":");
					jQuery("#intMinLengthError, #intMaxLengthError")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
					jQuery("#minGreaterThanMax")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.structuredTypes.enumeration.minGreaterThanMaxError"));
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.setModelElement = function(typeDeclaration) {
					this.initializeModelElement(typeDeclaration);

					this.typeDeclaration = typeDeclaration;

					var self = this;
					m_angularContextUtils.runInAngularContext(function($scope) {
						$scope.minLength = self.typeDeclaration.getTypeDeclaration().minLength;
						$scope.maxLength = self.typeDeclaration.getTypeDeclaration().maxLength;
					});

					this.updateViewIcon();

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
					// In case of external schema reference, check if it's
					// resolved correctly
					// else display an error message.
					if (m_constants.EXTERNAL_SCHEMA_CLASSIFIER_TOKEN === this.typeDeclaration.typeDeclaration.type.classifier
							&& !this.typeDeclaration.typeDeclaration.schema) {
						this.clearErrorMessages();
						this.errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.externalSchemaNotResolved")
										+ " " + this.typeDeclaration.typeDeclaration.type.location);
						this.showErrorMessages();
						return;
					}
					jQuery(this.typeDeclaration.isSequence() ? ".show-when-struct" : ".show-when-enum").show();
					jQuery(this.typeDeclaration.isSequence() ? ".show-when-enum" : ".show-when-struct").hide();

					this.visibilitySelect.prop("checked", (!this.typeDeclaration.attributes["carnot:engine:visibility"]
																|| "Public" === this.typeDeclaration.attributes["carnot:engine:visibility"]));
					this.structureKindSelect.val(this.typeDeclaration.isSequence() ? "struct" : "enum");

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
				XsdStructuredDataTypeView.prototype.renameElement = function(tableRows, nameInput) {
					var view = this;

					var isValidName = view.validateElementName(nameInput);
					jQuery(tableRows).each(function(i, tableRow) {
						var typeDeclaration = jQuery(tableRow).data("typeDeclaration");
						var oldName = jQuery(tableRow).data("elementName");
						var newName = nameInput.val();
						if (isValidName) {
							typeDeclaration.renameElement(oldName, newName);

							view.submitChanges({
								typeDeclaration : typeDeclaration.typeDeclaration
							});
						} else {
							nameInput.val(oldName);
						}
					});

					if (isValidName) {
						view.refreshElementsTable();
					}

					return isValidName;
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

				XsdStructuredDataTypeView.prototype.moveElementUp = function(tableRows) {
					var view = this;

					jQuery(tableRows).each(function(i, tableRow) {
						var typeDeclaration = jQuery(tableRow).data("typeDeclaration");
						typeDeclaration.moveElement(jQuery(tableRow).data("elementName"), -1);

						view.submitChanges({
							typeDeclaration : typeDeclaration.typeDeclaration
						});

					});
				};

				XsdStructuredDataTypeView.prototype.moveElementDown = function(tableRows) {
					var view = this;

					jQuery(tableRows).each(function(i, tableRow) {
						var typeDeclaration = jQuery(tableRow).data("typeDeclaration");
						typeDeclaration.moveElement(jQuery(tableRow).data("elementName"), 1);

						view.submitChanges({
							typeDeclaration : typeDeclaration.typeDeclaration
						});

					});
				};

				XsdStructuredDataTypeView.prototype.initializeRow = function(row, element, schemaType) {

					row.data("typeDeclaration", this.typeDeclaration);
					row.data("elementName", element.name);

					var elementName = element.name;
					var propertyName = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.inputText.new");
					elementName = elementName.replace("New", propertyName);
					var nameColumn = jQuery("<td class='elementCell'></td>").appendTo(row);
					if ( !this.typeDeclaration.isReadOnly()) {
						nameColumn.append("<span class='data-element'><input class='nameInput' type='text' value='" + elementName + "'/></span>");
					} else {
						nameColumn.append("<span class='data-element'>" + element.name + "</span>");
					}

					var typeColumn = jQuery("<td class='typeCell'></td>").appendTo(row);
					if (this.typeDeclaration.isSequence()) {

						if ( !this.typeDeclaration.isReadOnly()) {
							typeColumn.append(this.getTypeSelectList(schemaType));
						} else {
							typeColumn.append(m_structuredTypeBrowser.getSchemaTypeLabel(schemaType.name));
						}
					}

					var cardinalityColumn = jQuery("<td class='cardinalityCell'></td>").appendTo(row);
					if (this.typeDeclaration.isSequence()) {
						if ( !this.typeDeclaration.isReadOnly()) {
							var cardinalityBox = jQuery("<select size='1' class='cardinalitySelect'></select>");
							jQuery.each(["required", "optional", "many", "atLeastOne"], function(i, key) {
								cardinalityBox.append("<option value='" + key + "'" + (element.cardinality === key ? "selected" : "") + ">" + m_structuredTypeBrowser.getCardinalityLabel(key) + "</option>");
							});
							cardinalityColumn.append(cardinalityBox);
						} else {
							cardinalityColumn.append(m_structuredTypeBrowser.getCardinalityLabel(element.cardinality));
						}
					}
				};

				XsdStructuredDataTypeView.prototype.refreshElementsTable = function() {
					var selectedRowId = jQuery("table#typeDeclarationsTable tr.selected").first().attr('id');

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

					jQuery("table#typeDeclarationsTable #" + selectedRowId).addClass("selected");

					//this.tree.tableScroll("undo");
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

					// Scrolls down if the a row is added
					if (rowAdded) {
						jQuery("div.tablescroll_wrapper").scrollTop(
								jQuery("div.tablescroll_wrapper table")
										.height());
						jQuery("tr:last", "div.tablescroll_wrapper table")
								.find("input.nameInput").focus();
						rowAdded = false;
					}

					// Keeps the selected row within the wrapper div's view port
					// TODO - check if the logic can be simplified.
					if (rowMoved) {
						var wrapperDiv = jQuery("div.tablescroll_wrapper");
						var divTop = wrapperDiv.position().top;
						var divBottom = wrapperDiv.position().top
								+ wrapperDiv.height();
						var selectedRow = jQuery("div.tablescroll_wrapper table tr.selected");
						var rowPosition = selectedRow.position();
						if (rowPosition
								&& !((rowPosition.top > (divTop + selectedRow
										.height())) && (rowPosition.top < (divBottom - selectedRow
										.height())))) {
							wrapperDiv.scrollTop(rowPosition.top
									- jQuery("div.tablescroll_wrapper table")
											.position().top
									- selectedRow.height());
							rowMoved = false;
						}
					}
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

					jQuery(".nameInput", this.tree).on("change", function(event) {
							return view.renameElement(jQuery(event.target).closest("tr"), jQuery(event.target));
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
					var selected = false;

					select += "<optgroup label='" + m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectTypeSection.primitives") + "'>";

					jQuery.each(m_typeDeclaration.getXsdCoreTypes(), function() {
						var typeQName = "xsd:" + this;
						select += "<option value='" + typeQName + "' ";
						if (schemaType.isBuiltinType() && (typeQName === schemaType.name)) {
							select += "selected ";
							selected = true;
						}
						var label = m_structuredTypeBrowser.getSchemaTypeLabel(typeQName);

						select += "title='xsd:" + this + "'>" + label || typeQName + "</option>";
					});

					select += "</optgroup>";
					select += "<optgroup label='" + m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectTypeSection.thisModel") + "'>";
					var thisTypeDeclaration = this.typeDeclaration;
					jQuery.each(this.typeDeclaration.model.typeDeclarations, function() {
						var typeDeclaration = this;

						if (thisTypeDeclaration.uuid != typeDeclaration.uuid) {
							var tdType = typeDeclaration.asSchemaType();
							if (tdType) {
								select += "<option value='{" + tdType.nsUri +"}" + tdType.name + "' ";
								if ( !schemaType.isBuiltinType()) {
									select += ((schemaType.name === tdType.name) && (schemaType.nsUri === tdType.nsUri) ? "selected " : "");
									selected = true;
								}
								select += ">" + m_structuredTypeBrowser.getSchemaTypeLabel(typeDeclaration.name) + "</option>";
							}
						}
					});
					select += "</optgroup>";

//					Disabling selection of external structured types as there isn't kernel support for it yet
//					select += "<optgroup label='" + m_i18nUtils.getProperty("modeler.element.properties.commonProperties.otherModel") + "'>";
//					 for ( var i in m_model.getModels()) {
//							var model = m_model.getModels()[i];
//
//							if (model == this.typeDeclaration.model) {
//								continue;
//							}
//
//							for ( var n in model.typeDeclarations) {
//								var typeDeclaration = model.typeDeclarations[n];
//								 if (m_modelElementUtils.hasPublicVisibility(typeDeclaration)) {
//										var tdType = typeDeclaration.asSchemaType();
//										if (tdType) {
//											var x = "<option value='{" + tdType.nsUri +"}" + tdType.name + "' ";
//											if ( !schemaType.isBuiltinType()) {
//												x += ((schemaType.name === tdType.name) && (schemaType.nsUri === tdType.nsUri) ? "selected " : "");
//												selected = true;
//											}
//											x += ">" + model.name + "/" + typeDeclaration.name + "</option>";
//											select += x;
//										}
//								 }
//							}
//						}
//
//					select += "</optgroup>";

					select += "<optgroup label='" + m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectTypeSection.extraPrimitives") + "'>";

					jQuery.each(m_typeDeclaration.getXsdExtraTypes(), function() {
						var typeQName = "xsd:" + this;
						select += "<option value='" + typeQName + "' ";
						if (schemaType.isBuiltinType() && (typeQName === schemaType.name)) {
							select += "selected ";
							selected = true;
						}
						select += ">" + m_structuredTypeBrowser.getSchemaTypeLabel(typeQName) || typeQName + "</option>";
					});

					select += "</optgroup>";

					if (!selected) {
						select += "<option value=\"other\" selected>"
						+ m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.other")
						+ "</option>"
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
								.push(m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.errorMessage.emptyDataType"));
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				XsdStructuredDataTypeView.prototype.validateElementName = function(nameInput) {
					this.clearErrorMessages();

					nameInput.removeClass("error");

					if ((nameInput.val() == null) || (this.nameInput.val() === "")) {
						this.errorMessages
								.push(m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.errorMessage.emptyName"));
						nameInput.addClass("error");
					} else {
						var name = nameInput.val();
						if (this.typeDeclaration.isSequence()) {
							// name must be valid name according to XML rules
							try {
								jQuery.parseXML('<' + name + '/>');
							} catch (e) {
								this.errorMessages
									.push(m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.errorMessage.invalidName"));
								nameInput.addClass("error");
							}
						} else {
							// TODO validate against enum pattern, if any
						}
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.updateViewIcon = function() {
					var dataTypeViewIcon = m_elementConfiguration
							.getIconForElementType(this.typeDeclaration.getType());
					if (dataTypeViewIcon) {
						viewManager.updateView("xsdStructuredDataTypeView",
								m_constants.VIEW_ICON_PARAM_KEY + "="
										+ dataTypeViewIcon, this.typeDeclaration.uuid);
					}
				};

				/**
				 * Overrides the postProcessCommand to update the structured
				 * type list, in case it's changed.
				 */
				XsdStructuredDataTypeView.prototype.postProcessCommand = function(
						command) {
					var refresh = false;

					var obj = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;
					for ( var i = 0; i < obj.changes.added.length; i++) {
						if (m_constants.TYPE_DECLARATION_PROPERTY == obj.changes.added[i].type) {
							refresh = true;
						}
					}
					for ( var i = 0; i < obj.changes.modified.length; i++) {
						if (m_constants.TYPE_DECLARATION_PROPERTY == obj.changes.modified[i].type
								&& obj.changes.modified[i].uuid != this.getModelElement().uuid) {
							refresh = true;
						}
					}
					for ( var i = 0; i < obj.changes.removed.length; i++) {
						if (m_constants.TYPE_DECLARATION_PROPERTY == obj.changes.removed[i].type) {
							refresh = true;
						}
					}
					if (refresh) {
						this.refreshElementsTable();
					}
				};
			}
		});