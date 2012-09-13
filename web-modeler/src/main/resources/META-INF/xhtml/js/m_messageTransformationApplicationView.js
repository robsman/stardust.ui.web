/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "m_utils", "m_constants", "m_command", "m_commandsController",
				"m_model", "m_typeDeclaration", "m_accessPoint",
				"m_dataTraversal", "m_dialog", "m_modelElementView" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_model, m_typeDeclaration, m_accessPoint, m_dataTraversal,
				m_dialog, m_modelElementView) {
			return {
				initialize : function(fullId) {
					var view = new MessageTransformationApplicationView();

					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
				}
			};

			/**
			 * 
			 */
			function MessageTransformationApplicationView() {
				// Inheritance

				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(
						MessageTransformationApplicationView.prototype, view);

				this.inputData = {};
				this.outputData = {};
				this.mappingExpressions = {};
				this.inputTable = jQuery("#sourceTable");
				this.inputTableBody = jQuery("table#sourceTable tbody");
				this.sourceFilterInput = jQuery("#sourceFilterInput");
				this.targetFilterInput = jQuery("#targetFilterInput");
				this.outputTable = jQuery("#targetTable");
				this.outputTableBody = jQuery("table#targetTable tbody");
				this.expressionTextArea = jQuery("#expressionTextArea");
				this.filterFieldsWithMappingInput = jQuery("#filterFieldsWithMappingInput");
				this.filterFieldsWithNoMappingInput = jQuery("#filterFieldsWithNoMappingInput");
				this.filterHighlightedSourceFieldsInput = jQuery("#filterHighlightedSourceFieldsInput");
				this.filterHighlightedTargetFieldsInput = jQuery("#filterHighlightedTargetFieldsInput");
				this.showAllSourceFieldsInput = jQuery("#showAllSourceFieldsInput");
				this.showAllTargetFieldsInput = jQuery("#showAllTargetFieldsInput");

				this.selectedOutputTableRow = null;

				this.inputTableBody.empty();
				this.outputTableBody.empty();

				this.inputTableRows = [];
				this.outputTableRows = [];

				this.sourceFilterInput.keypress({
					"view" : this
				}, function(event) {
					event.data.view
							.filterSource(event.data.view.sourceFilterInput
									.val());
				});

				this.targetFilterInput.change({
					"view" : this
				}, function(event) {
					event.data.view
							.filterTarget(event.data.view.targetFilterInput
									.val());
				});

				this.filterHighlightedSourceFieldsInput.click({
					"view" : this
				}, function(event) {
					event.data.view.filterHighlightedSourceFields();
				});

				this.filterHighlightedTargetFieldsInput.click({
					"view" : this
				}, function(event) {
					event.data.view.filterHighlightedTargetFields();
				});

				this.showAllSourceFieldsInput.click({
					"view" : this
				}, function(event) {
					event.data.view.showAllSourceFields();
				});

				this.showAllTargetFieldsInput.click({
					"view" : this
				}, function(event) {
					event.data.view.showAllTargetFields();
				});

				m_dialog.makeInvisible(this.showAllSourceFieldsInput);
				m_dialog.makeInvisible(this.showAllTargetFieldsInput);

				this.filterFieldsWithNoMappingInput.click({
					"view" : this
				}, function(event) {
					event.data.view.filterFieldsWithNoMapping();
				});

				this.filterFieldsWithMappingInput.click({
					"view" : this
				}, function(event) {
					event.data.view.filterFieldsWithMapping();
				});

				this.expressionTextArea
						.droppable({
							accept : ".data-element",
							drop : function(e, ui) {
								var view = ui.draggable.data("view");
								var outputTableRow = view.selectedOutputTableRow;

								if (outputTableRow != null) {
									var mappingExpression = outputTableRow.mappingExpression;

									if (mappingExpression != null
											&& mappingExpression != "") {
										mappingExpression += " + ";
									} else {
										mappingExpression = "";
									}

									var inputTableRow = ui.draggable
											.data("tableRow")
									mappingExpression += inputTableRow.path;

									outputTableRow.mappingExpression = mappingExpression;

									jQuery(this).val(
											outputTableRow.mappingExpression);

									var rowId = outputTableRow.path.replace(
											/\./g, "-");

									// Set mapping column content

									var mappingCell = jQuery("#targetTable tr#"
											+ rowId + " .mapping");

									mappingCell.empty();
									mappingCell
											.append(outputTableRow.mappingExpression);

									view.submitChanges(this
											.determineTransformationChanges());
								}

								view.expressionTextArea.css({
									"cursor" : "default"
								});
							},
							hoverClass : "accept",
							over : function(e, ui) {
								var view = ui.draggable.data("view");
								var outputTableRow = view.selectedOutputTableRow;

								if (outputTableRow == null) {
									view.expressionTextArea.css({
										"cursor" : "wait"
									});
								}
							}
						});

				jQuery("#inputDataDialog").dialog({
					autoOpen : false,
					draggable : true
				});

				jQuery("#inputDataDialog #closeButton").click(function() {
					jQuery("#inputDataDialog").dialog("close");
				});

				jQuery("#inputDataDialog #applyButton")
						.click(
								{
									"view" : this
								},
								function(event) {
									event.data.view
											.addInputAccessPoint(
													jQuery(
															"#inputDataDialog #nameTextInput")
															.val(),
													m_model
															.findDataStructure(jQuery(
																	"#inputDataDialog #typeSelectInput")
																	.val()));
									event.data.view.resume();
									jQuery("#inputDataDialog").dialog("close");
								});

				jQuery("#addInputDataButton")
						.click(
								{
									"view" : this
								},
								function(event) {
									var inputDataTypeSelectInput = jQuery("#inputDataDialog #typeSelectInput");
//									var models = m_model.getModels();
//
//									for ( var m in models) {
//										var model = models[m];
									var model = event.data.view.application.model;
										for ( var n in model.structuredDataTypes) {
											var dataStructure = model.structuredDataTypes[n];
											inputDataTypeSelectInput
													.append("<option value='"
															+ dataStructure
																	.getFullId()
															+ "'>"
															+ dataStructure
																	.getFullId()
															+ "</option>");
										}
//									}

									jQuery("#inputDataDialog").dialog("open");
								});

				jQuery("#outputDataDialog").dialog({
					autoOpen : false,
					draggable : true
				});

				jQuery("#outputDataDialog #closeButton").click(function() {
					jQuery("#outputDataDialog").dialog("close");
				});

				jQuery("#outputDataDialog #applyButton")
						.click(
								{
									"view" : this
								},
								function(event) {
									event.data.view
											.addOutputAccessPoint(
													jQuery(
															"#outputDataDialog #nameTextInput")
															.val(),
													m_model
															.findDataStructure(jQuery(
																	"#outputDataDialog #typeSelectInput")
																	.val()));
									event.data.view.resume();
									jQuery("#outputDataDialog").dialog("close");
								});

				jQuery("#addOutputDataButton")
						.click(
								{
									view : this
								},
								function(event) {
									var outputDataTypeSelectInput = jQuery("#outputDataDialog #typeSelectInput");
									var typeDeclarations = m_typeDeclaration
											.getTypeDeclarations();

									// var models = m_model.getModels();
									//
									// for ( var m in models) {
									// var model = models[m];
									var model = event.data.view.application.model;
									for ( var n in model.structuredDataTypes) {
										var dataStructure = model.structuredDataTypes[n];
										outputDataTypeSelectInput
												.append("<option value='"
														+ dataStructure
																.getFullId()
														+ "'>"
														+ dataStructure
																.getFullId()
														+ "</option>");
									}
									// }

									jQuery("#outputDataDialog").dialog("open");
								});

				this.expressionTextArea.autocomplete({
					minLength : 0,
					source : function(request, response) {
						response(m_dataTraversal.getStepOptions(null,
								request.term));
					},
					focus : function() {
						return false;
					},
					select : function(event, ui) {
						var steps = m_dataTraversal.split(this.value);

						steps.pop();
						steps.push(ui.item.value);

						if (steps.length > 1) {
							this.value = steps.join(".");
						} else {
							this.value = steps[0];
						}

						return false;
					}
				});

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.initialize = function(
						application) {
					this.initializeModelElementView();
					this.initializeModelElement(application);

					this.application = application;

					m_utils.debug("===> Application");
					m_utils.debug(application);

					this
							.convertFromMappingsXml(this.application.attributes["messageTransformation:TransformationProperty"]);

					m_utils.debug("===> Mapping Expressions");
					m_utils.debug(this.mappingExpressions);

					for ( var m in this.application.accessPoints) {
						var accessPoint = this.application.accessPoints[m];

						if (accessPoint.direction == "IN") {
							this.addInputData(accessPoint);
						} else {
							this.addOutputData(accessPoint);
						}
					}

					this.populateTableRows(this.inputTableBody,
							this.inputTableRows, true);
					this.populateTableRows(this.outputTableBody,
							this.outputTableRows, false);
					this.resume();
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.convertFromMappingsXml = function(
						xml) {
					// TODO Very rudimentary parsing - not robust against any
					// changes in format

					var fieldMappings = xml
							.substring(
									xml
											.indexOf('<mapping:TransformationProperty') + 31,
									xml
											.indexOf('</mapping:TransformationProperty>'))
							.split("<fieldMappings")

					for ( var n = 1; n < fieldMappings.length; ++n) {
						var fieldPath = fieldMappings[n]
								.substr(
										fieldMappings[n].indexOf('fieldPath="') + 11,
										fieldMappings[n]
												.indexOf('mappingExpression="') - 14);
						var mappingExpression = fieldMappings[n]
								.substr(fieldMappings[n]
										.indexOf('mappingExpression="') + 19,
										fieldMappings[n].indexOf('"/>') - 10);
						mappingExpression = mappingExpression.substr(0,
								mappingExpression.indexOf('"/>'));

						fieldPath = fieldPath.replace(/\//g, ".");

						this.mappingExpressions[fieldPath] = mappingExpression;
					}
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.convertToMappingsXml = function() {
					var xml = "&lt;?xml version=&quot;1.0&quot; encoding=&quot;ASCII&quot;?&gt;&#13;&#10;&lt;mapping:TransformationProperty xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:mapping=&quot;java://com.infinity.bpm.messaging.model&quot; xsi:schemaLocation=&quot;java://com.infinity.bpm.messaging.model java://com.infinity.bpm.messaging.model.mapping.MappingPackage&quot;&gt;&#13;&#10;";

					for ( var n = 0; n < this.outputTableRows.length; ++n) {
						var outputTableRow = this.outputTableRows[n];

						xml += "&lt;fieldMappings";
						xml += " fieldPath=&quot;";
						xml += outputTableRow.path;
						xml += "&quot; mappingExpression=&quot;";
						xml += outputTableRow.mappingExpression;
						xml += "&quot;/&gt;&#13;&#10;";
					}

					return xml;
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.resume = function() {
					this.inputTable.tableScroll({
						height : 200
					});
					this.inputTable.treeTable();
					this.outputTable.tableScroll({
						height : 200
					});
					this.outputTable.treeTable();

					jQuery("table#sourceTable tbody tr").mousedown({
						"view" : this
					}, function() {
						jQuery("tr.selected").removeClass("selected");
						jQuery(this).addClass("selected");
					});

					jQuery("table#sourceTable tbody tr span").mousedown(
							function() {
								jQuery(jQuery(this).parents("tr")[0]).trigger(
										"mousedown");
							});

					jQuery("table#targetTable tbody tr")
							.mousedown(
									function() {
										var view = jQuery(this).data("view");

										jQuery("tr.selected").removeClass(
												"selected");
										jQuery(this).addClass("selected");

										view.selectedOutputTableRow = jQuery(
												this).data("tableRow");

										jQuery("#elementIndicatorText").empty();
										jQuery("#elementIndicatorText")
												.append(
														view.selectedOutputTableRow.path
																+ " = ");

										view.expressionTextArea
												.val(view.selectedOutputTableRow.mappingExpression);
									});

					jQuery("table#targetTable tbody tr span").mousedown(
							function() {
								jQuery(jQuery(this).parents("tr")[0]).trigger(
										"mousedown");
							});
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.addInputAccessPoint = function(
						dataName, dataStructure) {
					this.application.accessPoints[dataName] = m_accessPoint
							.createFromDataStructure(dataStructure, dataName,
									dataName, m_constants.IN_ACCESS_POINT);

					this.addInputData(this.application.accessPoints[dataName]);
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.addInputData = function(
						accessPoint) {
					// TODO Works only for references in the same model

					var typeDeclaration = m_model
							.findDataStructure(this.application.model.id
									+ ":"
									+ accessPoint.attributes["carnot:engine:dataType"]).typeDeclaration;

					typeDeclaration.resolveTypes(m_typeDeclaration
							.getTypeDeclarations());

					this.inputData[accessPoint.id] = typeDeclaration;
					this.initializeInputTableRowsRecursively(accessPoint,
							typeDeclaration, null, true);
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.addOutputAccessPoint = function(
						dataName, dataStructure) {
					this.application.accessPoints[dataName] = m_accessPoint
							.createFromDataStructure(dataStructure, dataName,
									dataName, m_constants.OUT_ACCESS_POINT);

					this.addOutputData(this.application.accessPoints[dataName]);
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.addOutputData = function(
						accessPoint) {
					// TODO Works only for references in the same model

					var typeDeclaration = m_model
							.findDataStructure(this.application.model.id
									+ ":"
									+ accessPoint.attributes["carnot:engine:dataType"]).typeDeclaration;

					typeDeclaration.resolveTypes(m_typeDeclaration
							.getTypeDeclarations());

					this.outputData[accessPoint.id] = typeDeclaration;
					this.initializeOutputTableRowsRecursively(accessPoint,
							typeDeclaration, null);
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.initializeInputTableRowsRecursively = function(
						accessPoint, element, parentPath) {
					var path = parentPath == null ? accessPoint.id
							: (parentPath + "." + element.name);
					var tableRow = {};

					this.inputTableRows.push(tableRow);

					tableRow.accessPoint = accessPoint;
					tableRow.element = element;
					tableRow.path = path;
					tableRow.parentPath = parentPath;
					tableRow.name = parentPath == null ? accessPoint.name
							: element.name;
					tableRow.typeName = parentPath == null ? ""
							: element.typeName;

					if (element.children == null) {
						return;
					}

					for ( var childElement in element.children) {
						this.initializeInputTableRowsRecursively(accessPoint,
								element.children[childElement], path);
					}
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.initializeOutputTableRowsRecursively = function(
						accessPoint, element, parentPath) {
					var path = parentPath == null ? accessPoint.id
							: (parentPath + "." + element.name);
					var tableRow = {};

					this.outputTableRows.push(tableRow);

					tableRow.accessPoint = accessPoint;
					tableRow.element = element;
					tableRow.path = path;
					tableRow.parentPath = parentPath;
					tableRow.name = parentPath == null ? accessPoint.name
							: element.name;
					tableRow.typeName = parentPath == null ? ""
							: element.typeName;
					m_utils.debug("===> Path: " + path);
					m_utils.debug("===> Expression: "
							+ this.mappingExpressions[path]);
					tableRow.mappingExpression = this.mappingExpressions[path] == null ? ""
							: this.mappingExpressions[path];
					tableRow.problems = "";

					if (element.children == null) {
						return;
					}

					for ( var childElement in element.children) {
						this.initializeOutputTableRowsRecursively(accessPoint,
								element.children[childElement], path);
					}
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.populateTableRows = function(
						tableBody, tableRows, source) {
					for ( var tableRow in tableRows) {
						var rowId = tableRows[tableRow].path
								.replace(/\./g, "-");

						var content = "<tr id=\""
								+ rowId
								+ "\" "
								+ (tableRows[tableRow].parentPath != null ? ("class=\"child-of-"
										+ tableRows[tableRow].parentPath
												.replace(/\./g, "-") + "\"")
										: "") + ">";

						content += "<td>";
						content += "<span class=\"data-element\">"
								+ tableRows[tableRow].name + "</span>";
						content += "</td>";
						content += "<td>" + tableRows[tableRow].typeName;
						+"</td>";

						if (source) {
							content += "</tr>";

							tableBody.append(content);

							var dataElement = jQuery("#sourceTable #" + rowId
									+ " .data-element");

							dataElement.data({
								"view" : this,
								"tableRow" : tableRows[tableRow]
							});
							dataElement.draggable({
								helper : "clone",
								opacity : .75,
								refreshPositions : true,
								revert : "invalid",
								revertDuration : 300,
								scroll : true
							});

							var row = jQuery("#sourceTable #" + rowId);

							row.click({
								"view" : this,
								"tableRow" : tableRow
							}, function(event) {
								event.data.view
										.highlightSource(event.data.tableRow);
							});

						} else {
							content += "<td class=\"mapping\">";
							content += tableRows[tableRow].mappingExpression;
							content += "<td/>";
							content += "<td>";
							content += "</td>";
							content += "</tr>";

							tableBody.append(content);

							var row = jQuery("#targetTable #" + rowId);
							row.data({
								"view" : this,
								"tableRow" : tableRows[tableRow]
							});

							row
									.droppable({
										accept : ".data-element",
										drop : function(e, ui) {
											var view = jQuery(this)
													.data("view");
											var outputTableRow = tableRows[tableRow];
											// jQuery(this)
											// .data("tableRow");
											var inputTableRow = ui.draggable
													.data("tableRow");

											outputTableRow.mappingExpression = inputTableRow.path;

											var mappingCell = jQuery(this)
													.children(".mapping");
											mappingCell.empty();
											mappingCell
													.append(outputTableRow.mappingExpression);

											// Update expression text
											// area if needed

											if (view.selectedOutputTableRow == outputTableRow) {
												view.expressionTextArea
														.val(outputTableRow.mappingExpression);
											}

											view
													.submitChanges(this
															.determineTransformationChanges());

										},
										hoverClass : "accept",
										over : function(e, ui) {
										}
									});

							row.click({
								"view" : this,
								"tableRow" : tableRow
							}, function(event) {
								event.data.view
										.highlightTarget(event.data.tableRow);
							});
						}
					}
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.highlightSource = function(
						tableRow) {
					jQuery("#sourceTable .data-element").removeClass(
							"highlighted");
					jQuery("#targetTable .data-element").removeClass(
							"highlighted");

					for ( var n = 0; n < this.outputTableRows.length; ++n) {
						if (this.outputTableRows[n].mappingExpression
								.indexOf(this.inputTableRows[tableRow].path) != -1) {
							jQuery(
									"#targetTable #"
											+ this.outputTableRows[n].path
													.replace(/\./g, "-")
											+ " .data-element").addClass(
									"highlighted");
						}
					}
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.highlightTarget = function(
						tableRow) {
					jQuery("#sourceTable .data-element").removeClass(
							"highlighted");
					jQuery("#targetTable .data-element").removeClass(
							"highlighted");

					for ( var n = 0; n < this.inputTableRows.length; ++n) {
						if (this.outputTableRows[tableRow].mappingExpression
								.indexOf(this.inputTableRows[n].path) != -1) {
							jQuery(
									"#sourceTable #"
											+ this.inputTableRows[n].path
													.replace(/\./g, "-")
											+ " .data-element").addClass(
									"highlighted");
						}
					}
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.filterSource = function(
						filter) {
					if (filter == null || filter == "") {
						jQuery("table#sourceTable tbody tr").removeClass(
								"invisible");
					} else {
						// // NEW selector
						// jQuery.expr[':'].Contains = function(a, i, m) {
						// return jQuery(a).text().toUpperCase()
						// .indexOf(m[3].toUpperCase()) >= 0;
						// };
						//
						// // OVERWRITES old selecor
						// jQuery.expr[':'].contains = function(a, i, m) {
						// return jQuery(a).text().toUpperCase()
						// .indexOf(m[3].toUpperCase()) >= 0;
						// };

						// Low-level filtering for maximum performance

						jQuery("table#sourceTable tbody tr").addClass(
								"invisible");
						jQuery(
								"table#sourceTable tbody tr:contains('"
										+ filter + "')").removeClass(
								"invisible");
					}
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.filterTarget = function(
						filter) {
					if (filter == null || filter == "") {
						jQuery("table#targetTable tbody tr").removeClass(
								"invisible");
					} else {
						jQuery("table#targetTable tbody tr").addClass(
								"invisible");
						jQuery(
								"table#targetTable tbody tr:contains('"
										+ filter + "')").removeClass(
								"invisible");
					}
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.filterFieldsWithNoMapping = function() {
					jQuery("table#targetTable tbody tr").addClass("invisible");
					m_utils
							.debug(jQuery("table#targetTable tbody tr .mapping:empty"));
					jQuery("table#targetTable tbody tr .mapping:empty")
							.parent().removeClass("invisible");
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.filterFieldsWithMapping = function() {
					jQuery("table#targetTable tbody tr").addClass("invisible");
					m_utils
							.debug(jQuery("table#targetTable tbody tr .mapping:not(:empty)"));
					jQuery("table#targetTable tbody tr .mapping:not(:empty)")
							.parent().removeClass("invisible");
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.filterHighlightedSourceFields = function() {
					m_dialog
							.makeInvisible(this.filterHighlightedSourceFieldsInput);
					m_dialog.makeVisible(this.showAllSourceFieldsInput);

					jQuery("table#sourceTable tbody tr").addClass("invisible");
					jQuery("table#sourceTable tbody tr .highlighted").parent()
							.parent().removeClass("invisible");
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.filterHighlightedTargetFields = function() {
					m_dialog
							.makeInvisible(this.filterHighlightedTargetFieldsInput);
					m_dialog.makeVisible(this.showAllTargetFieldsInput);

					jQuery("table#targetTable tbody tr").addClass("invisible");
					jQuery("table#targetTable tbody tr .highlighted").parent()
							.parent().removeClass("invisible");
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.showAllSourceFields = function() {
					m_dialog
							.makeVisible(this.filterHighlightedSourceFieldsInput);
					m_dialog.makeInvisible(this.showAllSourceFieldsInput);
					jQuery("table#sourceTable tbody tr").removeClass(
							"invisible");
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.showAllTargetFields = function() {
					m_dialog
							.makeVisible(this.filterHighlightedTargetFieldsInput);
					m_dialog.makeInvisible(this.showAllTargetFieldsInput);
					jQuery("table#targetTable tbody tr").removeClass(
							"invisible");
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Application name must not be empty.");
						this.nameInput.addClass("error");
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
				MessageTransformationApplicationView.prototype.determineTransformationChanges = function() {
					var transformationProperty = "&lt;?xml version=&quot;1.0&quot; encoding=&quot;ASCII&quot;?&gt;&#13;&#10;&lt;mapping:TransformationProperty xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:mapping=&quot;java://com.infinity.bpm.messaging.model&quot; xsi:schemaLocation=&quot;java://com.infinity.bpm.messaging.model java://com.infinity.bpm.messaging.model.mapping.MappingPackage&quot;&gt;&#13;&#10;";

					for ( var n = 0; n < this.outputTableRows.length; ++n) {
						var outputTableRow = this.outputTableRows[n];

						transformationProperty += "&lt;fieldMappings";
						transformationProperty += " fieldPath=&quot;";
						transformationProperty += outputTableRow.path;
						transformationProperty += "&quot; mappingExpression=&quot;";
						transformationProperty += outputTableRow.mappingExpression;
						transformationProperty += "&quot;/&gt;&#13;&#10;";
					}

					transformationProperty += ";&lt;/mapping:TransformationProperty&gt;&#13;&#10;";

					return {
						attributes : {
							"messageTransformation:TransformationProperty" : transformationProperty
						}
					};
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.processCommand = function(
						command) {
					if (command.type == m_constants.CHANGE_USER_PROFILE_COMMAND) {
						this.initialize(this.application);

						return;
					}

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object
							&& null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.application.oid) {
						this.initialize(this.application);
					}
				};
			}
		});