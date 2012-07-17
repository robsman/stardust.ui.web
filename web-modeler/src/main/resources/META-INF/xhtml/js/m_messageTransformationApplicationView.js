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
				"m_dataTraversal", "m_dialog" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_model, m_typeDeclaration, m_accessPoint, m_dataTraversal,
				m_dialog) {
			var view;
			var typeDeclarations = m_typeDeclaration.getTestTypeDeclarations();

			return {
				initialize : function() {
					var modelId = jQuery.url.setUrl(window.location.search)
							.param("modelId");
					var applicationId = jQuery.url.setUrl(
							window.location.search).param("applicationId");
					var model = m_model.findModel(modelId);
					var application = model.applications[applicationId];

					view = new MessageTransformationApplicationView();

					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(application);
				}
			};

			/**
			 * 
			 */
			function MessageTransformationApplicationView(application) {
				this.application = application;
				this.inputData = {};
				this.outputData = {};
				this.nameInput = jQuery("#nameInput");
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

				this.nameInput.change({
					"view" : this
				}, function(event) {
					var view = event.data.view;

					if (view.application.name != view.nameInput.val()) {
						view.submitChanges({
							name : view.nameInput.val()
						});
					}
				});

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

									view
											.submitChanges({
												attributes : {
													fieldMappings : [ {
														"fieldPath" : outputTableRow.path,
														"mappingExpression" : outputTableRow.mappingExpression
													} ]
												}
											});
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
											.addInputData(
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
									var models = m_model.getModels();

									for ( var m in models) {
										var model = models[m];
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
									}

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
											.addOutputData(
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
								function() {
									var outputDataTypeSelectInput = jQuery("#outputDataDialog #typeSelectInput");
									var typeDeclarations = m_typeDeclaration
											.getTypeDeclarations();

									var models = m_model.getModels();

									for ( var m in models) {
										var model = models[m];
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
									}

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
					this.application = application;

					m_utils.debug("Initializing MTA");
					m_utils.debug(this.application);
					
					this.nameInput.val(application.name);
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
				MessageTransformationApplicationView.prototype.addInputData = function(
						dataName, dataStructure) {
					var typeDeclaration = dataStructure.typeDeclaration;

					typeDeclaration.resolveTypes(m_typeDeclaration
							.getTypeDeclarations());

					this.application.accessPoints[dataName] = m_accessPoint
							.createFromDataStructure(dataStructure, dataName,
									dataName, m_constants.IN_ACCESS_POINT);

					this.inputData[dataName] = typeDeclaration;
					this.populateInputTableRowsRecursively(dataName,
							typeDeclaration, null, true);
					this.populateTableRows(this.inputTableBody,
							this.inputTableRows, true);
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.addOutputData = function(
						dataName, dataStructure) {
					var typeDeclaration = dataStructure.typeDeclaration;

					typeDeclaration.resolveTypes(m_typeDeclaration
							.getTypeDeclarations());

					this.application.accessPoints[dataName] = m_accessPoint
							.createFromDataStructure(dataStructure, dataName,
									dataName, m_constants.OUT_ACCESS_POINT);

					this.outputData[dataName] = typeDeclaration;
					this.populateOutputTableRowsRecursively(dataName,
							typeDeclaration, null, false);
					this.populateTableRows(this.outputTableBody,
							this.outputTableRows, false);
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.populateInputTableRowsRecursively = function(
						dataName, element, parentPath, source) {
					var path = parentPath == null ? dataName : (parentPath
							+ "." + element.name);
					var tableRow = {};

					this.inputTableRows.push(tableRow);

					tableRow.dataName = dataName;
					tableRow.element = element;
					tableRow.path = path;
					tableRow.parentPath = parentPath;
					tableRow.name = parentPath == null ? dataName
							: element.name;
					tableRow.typeName = parentPath == null ? ""
							: element.typeName;

					if (element.children == null) {
						return;
					}

					for ( var childElement in element.children) {
						this.populateInputTableRowsRecursively(dataName,
								element.children[childElement], path, source);
					}
				};

				/**
				 * 
				 */
				MessageTransformationApplicationView.prototype.populateOutputTableRowsRecursively = function(
						dataName, element, parentPath, source) {
					var path = parentPath == null ? dataName : (parentPath
							+ "." + element.name);
					var tableRow = {};

					this.outputTableRows.push(tableRow);

					tableRow.dataName = dataName;
					tableRow.element = element;
					tableRow.path = path;
					tableRow.parentPath = parentPath;
					tableRow.name = parentPath == null ? dataName
							: element.name;
					tableRow.typeName = parentPath == null ? ""
							: element.typeName;
					tableRow.mappingExpression = "";
					tableRow.problems = "";

					if (element.children == null) {
						return;
					}

					for ( var childElement in element.children) {
						this.populateOutputTableRowsRecursively(dataName,
								element.children[childElement], path, source);
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
							content += "<td class=\"mapping\"/>";
							content += "<td/>";
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
													.submitChanges({
														attributes : {
															fieldMappings : [ {
																"fieldPath" : outputTableRow.path,
																"mappingExpression" : outputTableRow.mappingExpression
															} ]
														}
													});

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
				MessageTransformationApplicationView.prototype.submitChanges = function(
						changes) {
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(this.application.model.id,
									this.application.oid, changes));
				};

				/**
				 * Only react to name changes and validation exceptions.
				 */
				MessageTransformationApplicationView.prototype.processCommand = function(
						command) {
					m_utils.debug("===> MTA Process Command");
					m_utils.debug(command);

					// Parse the response JSON from command pattern

					var obj = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != obj && null != obj.changes
							&& object.changes[this.application.oid] != null) {
						this.nameInput
								.val(object.changes[this.application.oid].name);

						// Validation Exceptions!
					}
				};
			};
		});