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
				"bpm-modeler/js/m_angularContextUtils", "bpm-modeler/js/m_modelerUtils" ],
		function(jQuery, m_utils, m_constants, m_communicationController, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_propertiesTree, m_typeDeclaration, m_structuredTypeBrowser, m_i18nUtils, m_elementConfiguration, m_jsfViewManager, m_modelElementUtils,
				m_angularContextUtils, m_modelerUtils) {
			return {
				initialize : function(fullId) {
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();
					
					m_utils.jQuerySelect("#hideGeneralProperties").hide();
					initViewCollapseClickHandlers();
					var view = new XsdStructuredDataTypeView();

					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findTypeDeclaration(fullId));
					m_utils.hideWaitCursor();
				}
			};

			/**
			 * 
			 */
			function initViewCollapseClickHandlers() {
				m_utils.jQuerySelect("#showGeneralProperties").click(function() {
					m_utils.jQuerySelect("#showAllProperties").hide();
					m_utils.jQuerySelect("#hideGeneralProperties").show();
				});
				m_utils.jQuerySelect("#hideGeneralProperties").click(function() {
					m_utils.jQuerySelect("#showAllProperties").show();
					m_utils.jQuerySelect("#hideGeneralProperties").hide();
				});
			}
			
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
					this.view = m_utils.jQuerySelect("#" + this.id);

					this.tree = m_utils.jQuerySelect("table#typeDeclarationsTable");
					this.tableBody = m_utils.jQuerySelect("table#typeDeclarationsTable tbody");
					this.addButton = m_utils.jQuerySelect("#addElementButton");
					this.deleteButton = m_utils.jQuerySelect("#deleteElementButton");
					this.upButton = m_utils.jQuerySelect("#moveElementUpButton");
					this.downButton = m_utils.jQuerySelect("#moveElementDownButton");
					this.structureDefinitionHintPanel = m_utils.jQuerySelect("#structureDefinitionHintPanel");
					this.visibilitySelect = m_utils.jQuerySelect("#publicVisibilityCheckbox");
					this.structureKindSelect = m_utils.jQuerySelect("#structureKind select");
					this.baseTypeSelect = m_utils.jQuerySelect("#baseTypeSelect select");
					this.bindToJavaSelect = m_utils.jQuerySelect("#bindJavaClassCheckbox");
					this.bindJavaClassEdit = m_utils.jQuerySelect("#javaClassInput");
					this.minimumLengthEdit = m_utils.jQuerySelect("#minLenghtInput");
					this.maximumLengthEdit = m_utils.jQuerySelect("#maxLenghtInput");
					this.focusAttr = {};

					var view = this;
					
					this.visibilitySelect.change(function(event) {
						var currentVisibility = view.typeDeclaration.attributes["carnot:engine:visibility"];
						var newVisibility = m_utils.jQuerySelect(event.target).is(":checked") ? "Public" : "Private";
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
							if (("struct" === m_utils.jQuerySelect(event.target).val()) && !view.typeDeclaration.isSequence()) {
								view.typeDeclaration.switchToComplexType();
								doSubmit = true;
							} else if (("enum" === m_utils.jQuerySelect(event.target).val()) && view.typeDeclaration.isSequence()) {
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

					this.baseTypeSelect
							.change(function(event) {
								if (m_utils.jQuerySelect(event.target).val() == "None") {
									view.typeDeclaration.setBaseType();
								} else {
									view.typeDeclaration.setBaseType(m_model
											.findElementByUuid(m_utils.jQuerySelect(
													event.target).val()));
								}

								view
										.submitChanges({
											typeDeclaration : view.typeDeclaration.typeDeclaration
										});
							});

					
					this.bindToJavaSelect.change(function(event) {
						var removeTypeDeclaration = false;
						var bindJavaClass = m_utils.jQuerySelect(event.target).is(":checked");
						if (bindJavaClass) {
							view.typeDeclaration.removeEnumTable(); //clear the min,max lenght and ENUM table
							removeTypeDeclaration =true; //flag to submit the removed table
						}
						m_angularContextUtils.runInAngularContext(function($scope) {
							if (bindJavaClass) {
								$scope.javaClassBinding = true; //Flag to show/hide other facets
								$scope.javaClassRequiredError = true; //Error message to enter java class path
							}else{
								$scope.javaClassRequiredError = false;
								$scope.javaClassBinding = false;
							}
							$scope.noEnumFoundError = false; // Valid on server roundtrip, not on pageLoad
						}, m_utils.jQuerySelect("#configurationTab").get(0));
						
						if(removeTypeDeclaration){
							view.submitChanges({
								typeDeclaration : view.typeDeclaration.typeDeclaration
							});
						}else{
							view.submitChanges({
								attributes : {
									"carnot:engine:className" : null
								}
							});
						}
					});
					
					var self = this;
					
					m_angularContextUtils.runInAngularContext(function($scope) {
						$scope.$watch("javaClassInput", function(newValue, oldValue) {
							if (newValue !== oldValue && $scope.form.javaClassInput.$valid) {
								if (newValue == "" || newValue == undefined) {
									$scope.javaClassInput = undefined;
									$scope.javaClassRequiredError = true; // For empty java class, show error
									$scope.noEnumFoundError = false;
								}else{
									$scope.javaClassRequiredError = false;
									$scope.noEnumFoundError = false;
									self.submitChanges({
										typeDeclaration : self.typeDeclaration.typeDeclaration,
										attributes : {
											"carnot:engine:className" : $scope.javaClassInput
										}
									});
								}
							}
						});
					}, m_utils.jQuerySelect("#configurationTab").get(0));
						
					m_angularContextUtils.runInAngularContext(function($scope) {
						$scope.$watch("minLength", function(newValue, oldValue) {
							if (newValue !== oldValue && $scope.form.minLenghtInput.$valid) {
								if (newValue == "" || validateRange(parseInt(newValue), $scope.maxLength)){
									if (newValue == "") {
										$scope.minLength = undefined;
//										self.typeDeclaration.getTypeDeclaration().minLength = undefined;
										self.typeDeclaration.deleteFacet("minLength");
									} else {
										//self.typeDeclaration.getTypeDeclaration().minLength = parseInt(newValue);
										self.typeDeclaration.deleteFacet("minLength");
										self.typeDeclaration.addFacet({
											classifier : "minLength",
											name : parseInt(newValue)
										});
									}
									if ($scope.maxLength) {
										//self.typeDeclaration.getTypeDeclaration().maxLength = parseInt($scope.maxLength);
										self.typeDeclaration.deleteFacet("maxLength");
										self.typeDeclaration.addFacet({
											classifier : "maxLength",
											name : parseInt($scope.maxLength)
										});
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
										//self.typeDeclaration.getTypeDeclaration().maxLength = undefined;
										self.typeDeclaration.deleteFacet("maxLength");
									} else {
										//self.typeDeclaration.getTypeDeclaration().maxLength = parseInt(newValue);
										self.typeDeclaration.deleteFacet("maxLength");
										self.typeDeclaration.addFacet({
											classifier : "maxLength",
											name : parseInt(newValue)
										});
									}
									if ($scope.minLength) {
										//self.typeDeclaration.getTypeDeclaration().minLength = parseInt($scope.minLength);
										self.typeDeclaration.deleteFacet("minLength");
										self.typeDeclaration.addFacet({
											classifier : "minLength",
											name : parseInt($scope.minLength)
										});
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
					}, m_utils.jQuerySelect("#configurationTab").get(0));

					if (typeDeclaration.getType() === "importedStructuredDataType") {
						m_modelerUtils.disableToolbarControl(this.addButton);
						m_modelerUtils.disableToolbarControl(this.deleteButton);
						m_modelerUtils.disableToolbarControl(this.upButton);
						m_modelerUtils.disableToolbarControl(this.downButton);
						this.baseTypeSelect.attr("disabled", true);
					} else {
						m_utils.jQuerySelect(this.addButton).click(
								function(event) {
									m_utils.jQuerySelect("tr.selected", view.tableBody)
											.removeClass("selected")
									view.addElement();
									rowAdded = true;
								});
						m_utils.jQuerySelect(this.deleteButton).click(
								function(event) {
									view.removeElement(m_utils.jQuerySelect("tr.selected",
											view.tableBody));
								});
						m_utils.jQuerySelect(this.upButton).click(
								function(event) {
									view.moveElementUp(m_utils.jQuerySelect("tr.selected",
											view.tableBody));
									rowMoved = true;
								});
						m_utils.jQuerySelect(this.downButton).click(
								function(event) {
									view.moveElementDown(m_utils.jQuerySelect("tr.selected",
											view.tableBody));
									rowMoved = true;
								});
					}

					this.populateBaseTypeSelectMenu(typeDeclaration);
					this.setBaseType(typeDeclaration);

					this.initializeModelElementView(typeDeclaration);
					this.view.css("visibility", "visible");
				};

				XsdStructuredDataTypeView.prototype.populateBaseTypeSelectMenu = function(typeDeclaration) {
					var optionsString = "<option value='None'>" + m_i18nUtils.getProperty("modeler.element.properties.commonProperties.none") + "</option>";

					optionsString += "<optgroup label='" + m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectTypeSection.thisModel") + "'>";
					if (typeDeclaration) {
						var thisModel = m_model.findModelForElement(typeDeclaration.uuid);
						for (var i in thisModel.typeDeclarations) {
							if (thisModel.typeDeclarations[i].uuid !== typeDeclaration.uuid
									&& thisModel.typeDeclarations[i].isSequence()
									&& (typeDeclaration.getType() === "importedStructuredDataType"
										|| thisModel.typeDeclarations[i].getType() !== "importedStructuredDataType")) {
								optionsString += "<option value='" + thisModel.typeDeclarations[i].uuid + "'>" + thisModel.typeDeclarations[i].name + "</option>";
							}
						}
					}
					optionsString += "</optgroup>";

					optionsString += "<optgroup label='" + m_i18nUtils.getProperty("modeler.element.properties.commonProperties.otherModel") + "'>";
					var allModels = m_model.getModels();
					for ( var i in allModels) {
						var model = allModels[i];

						if (model == typeDeclaration.model) {
							continue;
						}

						for ( var n in model.typeDeclarations) {
							var typeDeclarationN = model.typeDeclarations[n];
							if (m_modelElementUtils
									.hasPublicVisibility(typeDeclarationN)
									&& typeDeclarationN.isSequence()
									&& (typeDeclaration.getType() === "importedStructuredDataType" || typeDeclarationN
											.getType() !== "importedStructuredDataType")) {
								var x = "<option value='" + typeDeclarationN.uuid + "' ";
								x += ">" + model.name + "/"
										+ typeDeclarationN.name + "</option>";
								optionsString += x;
							}
						}
					}
					optionsString += "</optgroup>";

					this.baseTypeSelect.html(optionsString);
				};

				XsdStructuredDataTypeView.prototype.setBaseType = function(
						typeDeclaration) {
					var baseTypeDeclaration = null;

					if (typeDeclaration
							&& "enumStructuredDataType" !== typeDeclaration.getType()
							&& !typeDeclaration.isComplexTypeWithSimpleContent()) {
						if (typeDeclaration.isSequence()
								&& typeDeclaration.getTypeDeclaration()
								&& typeDeclaration.getTypeDeclaration().base) {
							baseTypeDeclaration = this
									.resolveBaseType(typeDeclaration);

								if (baseTypeDeclaration == null) {
									this.errorMessages.push(m_i18nUtils
											.getProperty("modeler.model.propertyView.structuredTypes.parentStrTypeNotResolved")
											+ " " + typeDeclaration.getTypeDeclaration().base);

								if (this.baseTypeSelect.val() != 'pleaseSpecify') {
									var optionsString = "<option value='pleaseSpecify'>"
											+ m_i18nUtils.getProperty("modeler.general.toBeDefined")
											+ "</option>";
									this.baseTypeSelect.append(optionsString);
									this.baseTypeSelect.val('pleaseSpecify');
								}
							} else {
								this.baseTypeSelect.val(baseTypeDeclaration.uuid);
							}
						} else {
							this.baseTypeSelect.val("None");
						}	
					}
				};

				/**
				 * resolve base type from current and other models
				 */
				XsdStructuredDataTypeView.prototype.resolveBaseType = function(
						typeDeclaration) {
					var basetypeDecl = null;
					 {
						var baseTypeId = typeDeclaration.getTypeDeclaration().base
								.substring(typeDeclaration.getTypeDeclaration().base
										.indexOf(":") + 1);
						//get from current model
						basetypeDecl = typeDeclaration.model.typeDeclarations[baseTypeId];

						//search in other models
						if (!basetypeDecl
								&& typeDeclaration.getTypeDeclaration().base
										.indexOf(":") != -1) {
							var nsMappingPrefix = typeDeclaration
									.getTypeDeclaration().base.split(":")[0];
							var nameSpace = typeDeclaration.typeDeclaration.schema.nsMappings[nsMappingPrefix];

							if (!nameSpace
									|| !typeDeclaration.typeDeclaration.schema.locations) {
								return null;
							}
							var location = typeDeclaration.typeDeclaration.schema.locations[nameSpace];

							if (!location) {
								return null;
							}
							var modelIdTypeId = m_typeDeclaration
									.parseQName(location);

							referenceModel = m_model.findModel(modelIdTypeId.namespace);

							if (!referenceModel) {
								return null;
							}

							jQuery.each(referenceModel.typeDeclarations, function(i,
									declaration) {
								if (declaration.id === modelIdTypeId.name) {
									basetypeDecl = declaration;
									return false;
								}
							});
						}
						return basetypeDecl;
					}
				};

				XsdStructuredDataTypeView.prototype.internationalizeStaticData = function() {
					m_utils.jQuerySelect("#hideGeneralProperties label")
						.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));
			
					m_utils.jQuerySelect("#showGeneralProperties label")
						.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));
				
					m_utils.jQuerySelect("#publicVisibility")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.publicVisibility"));
					m_utils.jQuerySelect("tr#structureKind > td > label")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.structuredTypes.dataStructureType")
											+ ":");
					m_utils.jQuerySelect("tr#structureKind select option.label-struct")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.structuredTypes.dataStructureType.composite"));
					m_utils.jQuerySelect("tr#structureKind select option.label-enum")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.structuredTypes.dataStructureType.enumeration"));
					m_utils.jQuerySelect("tr#minimumLength td.label")
					.text(
							m_i18nUtils
									.getProperty("modeler.model.propertyView.structuredTypes.enumeration.minLength") + ":");
					m_utils.jQuerySelect("tr#maximumLength td.label")
					.text(
							m_i18nUtils
									.getProperty("modeler.model.propertyView.structuredTypes.enumeration.maxLength") + ":");
					m_utils.jQuerySelect("#intMinLengthError, #intMaxLengthError")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
					m_utils.jQuerySelect("#minGreaterThanMax")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.structuredTypes.enumeration.minGreaterThanMaxError"));
					m_utils.jQuerySelect("tr#baseTypeSelect > td > label")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.structuredTypes.parentType") + ":");
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.setModelElement = function(typeDeclaration) {
					this.initializeModelElement(typeDeclaration);

					this.typeDeclaration = typeDeclaration;

					var self = this;
					m_angularContextUtils.runInAngularContext(function($scope) {
						if (self.typeDeclaration && self.typeDeclaration.getTypeDeclaration()) {
//							$scope.minLength = self.typeDeclaration.getTypeDeclaration().minLength;
//							$scope.maxLength = self.typeDeclaration.getTypeDeclaration().maxLength;
							var facets = self.typeDeclaration.getFacets();
							// reset the java class binding for ENUM
							$scope.noEnumFoundError = false;
							if (facets && !jQuery.isEmptyObject(facets)) {
								var minVal;
								var maxVal;
								for (var i in facets) {
									if (facets[i].classifier === "minLength") {
										minVal = facets[i].name;
									}
									if (facets[i].classifier === "maxLength") {
										maxVal = facets[i].name;
									}
								}
								$scope.minLength = minVal;
								$scope.maxLength = maxVal;
							} else {
								if($scope.javaClassBinding == true && $scope.javaClassRequiredError == false){
									//On server roundtrip , for valid java class provided , show No-enum msg
									$scope.noEnumFoundError = true;
								}else{
									$scope.noEnumFoundError = false;
								}
							}
						}
					}, m_utils.jQuerySelect("#configurationTab").get(0));

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
					m_utils.jQuerySelect(this.typeDeclaration.isSequence() ? ".show-when-struct" : ".show-when-enum").show();
					m_utils.jQuerySelect(this.typeDeclaration.isSequence() ? ".show-when-enum" : ".show-when-struct").hide();

					this.visibilitySelect.prop("checked", (!this.typeDeclaration.attributes["carnot:engine:visibility"]
																|| "Public" === this.typeDeclaration.attributes["carnot:engine:visibility"]));
					this.structureKindSelect.val(this.typeDeclaration.isSequence() ? "struct" : "enum");
					
					this.bindJavaClassEdit.val(this.typeDeclaration.attributes["carnot:engine:className"]);
					
					if(!this.bindToJavaSelect.prop("checked")){
						this.bindToJavaSelect.prop("checked",this.typeDeclaration.attributes["carnot:engine:className"] != undefined);	
					}
					
					view=this;
					m_angularContextUtils.runInAngularContext(function($scope) {
						if (view.bindToJavaSelect.prop("checked")) {
							$scope.javaClassBinding = true;
						} else {
							$scope.javaClassBinding = false; //reset java bindling flag.
							$scope.javaClassRequiredError = false; //reset javaClassRequiredflag, as javaBinding is false
						}
					}, m_utils.jQuerySelect("#configurationTab").get(0));
					
					this.clearErrorMessages();
					this.setBaseType(this.typeDeclaration);

					this.refreshElementsTable();
					this.showErrorMessages();

					//restore focus if the entry is changed
					this.restoreFocus();
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.addElement = function() {
					var newElement = this.typeDeclaration.addElement(/* generate default name */);

					this.submitChanges({
						typeDeclaration : this.typeDeclaration.typeDeclaration
					});
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.renameElement = function(tableRows, nameInput) {
					var view = this;

					var isValidName = view.validateElementName(nameInput);
					m_utils.jQuerySelect(tableRows).each(function(i, tableRow) {
						var typeDeclaration = m_utils.jQuerySelect(tableRow).data("typeDeclaration");
						var oldName = m_utils.jQuerySelect(tableRow).data("elementName");
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

					return isValidName;
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.setElementType = function(tableRows, dataValue) {
					var view = this;

					m_utils.jQuerySelect(tableRows).each(function(i, tableRow) {
						var typeDeclaration = m_utils.jQuerySelect(tableRow).data("typeDeclaration");
						var typeName = dataValue;
						var location = null;

						if (dataValue.indexOf("#location:") != -1) {
							typeName = dataValue.substr(dataValue.indexOf("#typeName:") + 10);
							location = dataValue.substring(dataValue.indexOf("#location:") + 10, dataValue.indexOf("#typeName:"));
						}

						if (location) {
							if (!typeDeclaration.typeDeclaration.schema.locations) {
								typeDeclaration.typeDeclaration.schema.locations = {};
							}
							typeDeclaration.typeDeclaration.schema.locations[m_typeDeclaration
									.parseQName(typeName).namespace] = location;
						}

						typeDeclaration.setElementType(m_utils.jQuerySelect(tableRow).data("elementName"), typeName);

						view.submitChanges({
							typeDeclaration : typeDeclaration.typeDeclaration
						});
					});
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.setElementCardinality = function(tableRows, cardinality) {
					var view = this;

					m_utils.jQuerySelect(tableRows).each(function(i, tableRow) {
						var typeDeclaration = m_utils.jQuerySelect(tableRow).data("typeDeclaration");
						typeDeclaration.setElementCardinality(m_utils.jQuerySelect(tableRow).data("elementName"), cardinality);

						view.submitChanges({
							typeDeclaration : typeDeclaration.typeDeclaration
						});
					});
				};

				/**
				 *
				 */
				XsdStructuredDataTypeView.prototype.removeElement = function(tableRows) {
					var view = this;

					m_utils.jQuerySelect(tableRows).each(function(i, tableRow) {
						var typeDeclaration = m_utils.jQuerySelect(tableRow).data("typeDeclaration");
						typeDeclaration.removeElement(m_utils.jQuerySelect(tableRow).data("elementName"));

						view.submitChanges({
							typeDeclaration : typeDeclaration.typeDeclaration
						});
					});
				};

				XsdStructuredDataTypeView.prototype.moveElementUp = function(tableRows) {
					var view = this;

					m_utils.jQuerySelect(tableRows).each(function(i, tableRow) {
						var typeDeclaration = m_utils.jQuerySelect(tableRow).data("typeDeclaration");
						var moved = typeDeclaration.moveElement(m_utils.jQuerySelect(tableRow).data("elementName"), -1);

						if (moved) {
							view.submitChanges({
								typeDeclaration : typeDeclaration.typeDeclaration
							});
						}
					});
				};

				XsdStructuredDataTypeView.prototype.moveElementDown = function(tableRows) {
					var view = this;

					m_utils.jQuerySelect(tableRows).each(function(i, tableRow) {
						var typeDeclaration = m_utils.jQuerySelect(tableRow).data("typeDeclaration");
						var moved = typeDeclaration.moveElement(m_utils.jQuerySelect(tableRow).data("elementName"), 1);

						if (moved) {
							view.submitChanges({
								typeDeclaration : typeDeclaration.typeDeclaration
							});
						}
					});
				};

				XsdStructuredDataTypeView.prototype.initializeRow = function(row, element, schemaType) {

					row.data("typeDeclaration", this.typeDeclaration);
					row.data("elementName", element.name);
					row.data("element", element);

					if (element.inherited) {
						row.addClass("locked");
					}

					var elementName = element.name;
					var propertyName = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.inputText.new");
					elementName = elementName.replace("New", propertyName);
					var nameColumn = m_utils.jQuerySelect("<td class='elementCell'></td>").appendTo(row);
					if (this.isRowEditable(element)) {
						nameColumn.append("<span class='data-element'><input class='nameInput' type='text' value='" + elementName + "'/></span>");
					} else {
						nameColumn.append("<span class='data-element'></span>");
						nameColumn.children("td span").text(element.name);
					}

					var typeColumn = m_utils.jQuerySelect("<td class='typeCell'></td>").appendTo(row);
					if (this.typeDeclaration.isSequence()) {

						if (this.isRowEditable(element)) {
							typeColumn.append(this.getTypeSelectList(schemaType, element));
						} else {
							var type = schemaType ? schemaType.name : (element.type ? element.type : "");
							if (!type) {
								if (element.primitiveType && !element.attributes) {
									type = m_structuredTypeBrowser.getSchemaTypeLabel(element.primitiveType);
								} else {
									// Assumes that this is an element of anonymous type
									// and sets the element name as its type
									type = element.name;
								}
							}
							typeColumn.append(m_structuredTypeBrowser.getSchemaTypeLabel(type));
						}
					}

					var cardinalityColumn = m_utils.jQuerySelect("<td class='cardinalityCell'></td>").appendTo(row);
					if (this.typeDeclaration.isSequence()) {
						if (this.isRowEditable(element)) {
							var cardinalityBox = m_utils.jQuerySelect("<select size='1' class='cardinalitySelect'></select>");
							jQuery.each(["required", "optional", "many", "atLeastOne"], function(i, key) {
								cardinalityBox.append("<option value='" + key + "'" + (element.cardinality === key ? "selected" : "") + ">" + m_structuredTypeBrowser.getCardinalityLabel(key) + "</option>");
							});
							cardinalityColumn.append(cardinalityBox);
						} else {
							if (element.classifier === "attribute") {
								// Cardinality not displayed for attributes
								cardinalityColumn.append(element.cardinality);
							} else {
								cardinalityColumn.append(m_structuredTypeBrowser.getCardinalityLabel(element.cardinality));
							}
						}
					}
				};
				
				/**
				 * 
				 */
				XsdStructuredDataTypeView.prototype.isRowEditable = function(element) {
					if ( !this.typeDeclaration.isExternalReference() && !element.inherited && (element.classifier !== "attribute")) {
						return true;
					}
					
					return false;
				}

				XsdStructuredDataTypeView.prototype.refreshElementsTable = function() {
					var selectedRowIndex = m_utils.jQuerySelect("table#typeDeclarationsTable tr.selected").first().index();

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
						
						var attributes = parentRow.data("attributes");
						if (attributes) {
							var attributeRows = m_structuredTypeBrowser.generateChildElementRows(parentPath, attributes);
							m_utils.insertArrayAt(childRows, attributeRows, 0);
						}

						parentRow.appendTo(view.tableBody);
						if(view.bindToJavaSelect.prop("checked")){
							m_utils.jQuerySelect("table#typeDeclarationsTable *").attr("disabled","disabled");
						}
						jQuery.each(childRows, function(i, childRow) {
							childRow.addClass("child-of-" + parentPath);
							if (parentRow.hasClass("locked")) {
								childRow.addClass("locked");
							}
							view.tableBody.append(childRow);
						});
					});

					m_utils.jQuerySelect("table#typeDeclarationsTable tr").eq(selectedRowIndex).addClass("selected");

					//update properties/annotation table
					if(this.propertiesTree){
						m_propertiesTree.refresh(this.propertiesTree, m_utils.jQuerySelect(m_utils.jQuerySelect("tr.selected", this.tableBody)).data("element"), view);
					}

					//this.tree.tableScroll("undo");
					this.tree.tableScroll({
						height : 150
					});

					this.tree.treeTable({
						indent: 14,
						onNodeShow: function() {
							m_structuredTypeBrowser.insertChildElementRowsLazily(m_utils.jQuerySelect(this));
						}
					});

					// bind events after tree got initialized, otherwise renames
					// in parent rows don't get triggered
					this.bindTableEventHandlers();

					// Scrolls down if the a row is added
					if (rowAdded) {
						m_utils.jQuerySelect("div.tablescroll_wrapper").scrollTop(
								m_utils.jQuerySelect("div.tablescroll_wrapper table")
										.height());
						m_utils.jQuerySelect("tr:last", "div.tablescroll_wrapper table")
								.find("input.nameInput").focus();
						rowAdded = false;
					}

					// Keeps the selected row within the wrapper div's view port
					// TODO - check if the logic can be simplified.
					if (rowMoved) {
						var wrapperDiv = m_utils.jQuerySelect("div.tablescroll_wrapper");
						var divTop = wrapperDiv.position().top;
						var divBottom = wrapperDiv.position().top
								+ wrapperDiv.height();
						var selectedRow = m_utils.jQuerySelect("div.tablescroll_wrapper table tr.selected");
						var rowPosition = selectedRow.position();
						if (rowPosition
								&& !((rowPosition.top > (divTop + selectedRow
										.height())) && (rowPosition.top < (divBottom - selectedRow
										.height())))) {
							wrapperDiv.scrollTop(rowPosition.top
									- m_utils.jQuerySelect("div.tablescroll_wrapper table")
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

					m_utils.jQuerySelect("tr", this.tableBody).mousedown(
						function() {
							m_utils.jQuerySelect("tr.selected", view.tableBody).removeClass("selected");
							m_utils.jQuerySelect(this).addClass("selected");
							view.propertiesTree = m_propertiesTree.create(m_utils.jQuerySelect(this).data("element"), view);
						});

					m_utils.jQuerySelect(".nameInput", this.tree).on("change", function(event) {
							return view.renameElement(m_utils.jQuerySelect(event.target).closest("tr"), m_utils.jQuerySelect(event.target));
						});

					m_utils.jQuerySelect(".nameInput", this.tree).on("keydown", function(event) {
						if (event.which == 9) { //tab key pressed
							if(!event.shiftKey){
								view.preserveFocus(m_utils.jQuerySelect(this), true);
							}
						}
					});

					m_utils.jQuerySelect(".typeSelect", this.tree).on("change", function(event) {
							view.preserveFocus(m_utils.jQuerySelect(this), false);
							view.setElementType(m_utils.jQuerySelect(event.target).closest("tr"), m_utils.jQuerySelect(event.target).val());
						});

					m_utils.jQuerySelect(".cardinalitySelect", this.tree).on("keydown", function(event) {
						if (event.which == 9) { //tab key pressed
							if(!event.shiftKey){
								view.preserveFocus(m_utils.jQuerySelect(this), true);
								view.restoreFocus();
								event.preventDefault();
							}
						}
					});

					m_utils.jQuerySelect(".cardinalitySelect", this.tree).on("change", function(event) {
							view.preserveFocus(m_utils.jQuerySelect(this), false);
							view.setElementCardinality(m_utils.jQuerySelect(event.target).closest("tr"), m_utils.jQuerySelect(event.target).val());
						});
				};

				/**
				 *  record the focus attributes (row, column)
				 */
				XsdStructuredDataTypeView.prototype.preserveFocus = function(
						element, focusNext) {
					this.focusAttr.rowIndex = element.closest("tr").index();
					this.focusAttr.colIndex = element.closest("td").index();
					this.focusAttr.focusNext = focusNext;
				};

				/**
				 *	restore the focus attributes (row, column)
				 */
				XsdStructuredDataTypeView.prototype.restoreFocus = function() {
					if (this.focusAttr.rowIndex == 'undefined'
							|| this.focusAttr.colIndex == 'undefined') {
						return;
					}
					var lastRowIndex = m_utils.jQuerySelect(".nameInput:last", this.tree)
							.closest("tr").index();

					if (2 == this.focusAttr.colIndex
							&& lastRowIndex == this.focusAttr.rowIndex
							&& this.focusAttr.focusNext) {

						this.addButton.focus();

					} else {
						var columnId = {
							0 : ".nameInput",
							1 : ".typeSelect",
							2 : ".cardinalitySelect"
						};

						var nextRowIndex = this.focusAttr.rowIndex;
						if (this.focusAttr.colIndex == 2
								&& this.focusAttr.focusNext) {
							nextRowIndex++;
						}

						var nextColIndex = this.focusAttr.colIndex;
						if (this.focusAttr.focusNext) {
							nextColIndex = (this.focusAttr.colIndex + 1) % 3;
						}

						var cell = m_utils.jQuerySelect("table#typeDeclarationsTable tr").eq(
								nextRowIndex).find("td").eq(nextColIndex).find(
								columnId[nextColIndex]);
						cell.focus();
					}
					//reset
					this.focusAttr = {};
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
				XsdStructuredDataTypeView.prototype.getTypeSelectList = function(schemaType, element) {
					var select = "<select size='1' class='typeSelect' style='width: 90%;'>";
					var selected = false;

					select += "<optgroup label='" + m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectTypeSection.primitives") + "'>";

					jQuery.each(m_typeDeclaration.getXsdCoreTypes(), function() {
						var typeQName = "xsd:" + this;
						select += "<option value='" + typeQName + "' ";
						if (schemaType && schemaType.isBuiltinType() && (typeQName === schemaType.name)) {
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

						if (thisTypeDeclaration.uuid != typeDeclaration.uuid
								&& typeDeclaration.getType() !== "importedStructuredDataType") {
							var tdType = typeDeclaration.asSchemaType();
							if (tdType) {
								select += "<option value='{" + tdType.nsUri +"}" + tdType.name + "' ";
								if ( schemaType && !schemaType.isBuiltinType()) {
									select += ((schemaType.name === tdType.name) && (schemaType.nsUri === tdType.nsUri) ? "selected " : "");
									selected = true;
								}
								select += ">" + m_structuredTypeBrowser.getSchemaTypeLabel(typeDeclaration.name) + "</option>";
							}
						}
					});
					select += "</optgroup>";

					select += "<optgroup label='" + m_i18nUtils.getProperty("modeler.element.properties.commonProperties.otherModel") + "'>";
					 for ( var i in m_model.getModels()) {
							var model = m_model.getModels()[i];

							if (model == this.typeDeclaration.model) {
								continue;
							}

							for ( var n in model.typeDeclarations) {
								var typeDeclaration = model.typeDeclarations[n];
								 if (m_modelElementUtils.hasPublicVisibility(typeDeclaration)) {
										var tdType = typeDeclaration.asSchemaType();
										if (tdType) {
											var typeName = "{" + tdType.nsUri + "}" + tdType.name;
											var dataValue = "#location:" + "{" + model.id + "}" + tdType.name + "#typeName:" + typeName;

											var x = "<option value='" + dataValue + "' ";
											if (schemaType && !schemaType.isBuiltinType()) {
												x += ((schemaType.name === tdType.name) && (schemaType.nsUri === tdType.nsUri) ? "selected " : "");
												selected = true;
											}
											x += ">" + model.name + "/" + typeDeclaration.name + "</option>";
											select += x;
										}
								 }
							}
						}

					select += "</optgroup>";


					select += "<optgroup label='" + m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectTypeSection.extraPrimitives") + "'>";

					jQuery.each(m_typeDeclaration.getXsdExtraTypes(), function() {
						var typeQName = "xsd:" + this;
						select += "<option value='" + typeQName + "' ";
						if (schemaType && schemaType.isBuiltinType() && (typeQName === schemaType.name)) {
							select += "selected ";
							selected = true;
						}
						select += ">" + m_structuredTypeBrowser.getSchemaTypeLabel(typeQName) || typeQName + "</option>";
					});

					select += "</optgroup>";

					if (!selected) {
						this.errorMessages.push(element.name  + " : " + m_i18nUtils
								.getProperty("modeler.model.propertyView.structuredTypes.nestedStrTypeNotResolved")
								+ " " + element.type);

						select += "<option value=\"other\" selected>"
						+ m_i18nUtils
								.getProperty("modeler.general.toBeDefined")
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

					if (m_utils.isEmptyString(this.nameInput.val())) {
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

					if (m_utils.isEmptyString(nameInput.val())) {
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
						
						// Check for duplicates.
						for (var i in this.getModelElement().getElements()) {
							if (this.getModelElement().getElements()[i].name === nameInput.val()) {
								this.errorMessages
								.push(m_i18nUtils.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.errorMessage.duplicateName"));
								break;
							}
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
						this.populateBaseTypeSelectMenu(typeDeclaration);
						this.setBaseType(typeDeclaration);
						this.refreshElementsTable();
					}
				};
			}
		});