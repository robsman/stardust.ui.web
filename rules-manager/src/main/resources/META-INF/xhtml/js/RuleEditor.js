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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_model", "rules-manager/js/RuleSet",
				"rules-manager/js/PopupSelector" ],
		function(m_utils, m_i18nUtils, m_model, RuleSet, PopupSelector) {
			return {
				create : function() {
					var ruleEditor = new RuleEditor();

					ruleEditor.initialize();

					return ruleEditor;
				}
			};

			/**
			 * 
			 */
			function RuleEditor() {
				/**
				 * 
				 */
				RuleEditor.prototype.initialize = function() {
					this.conditionsTable = m_utils.jQuerySelect("#ruleView #ruleEditorPanel #conditionsTable");
					this.actionsTable = m_utils.jQuerySelect("#ruleView #ruleEditorPanel #actionsTable");
					this.addConditionButton = m_utils.jQuerySelect("#ruleView #ruleEditorPanel #addConditionButton");
					this.addActionButton = m_utils.jQuerySelect("#ruleView #ruleEditorPanel #addActionButton");

					this.selector = PopupSelector.create({
						anchor : "ruleEditorSelectDialog"
					});

					this.addConditionButton
							.click(
									{
										view : this
									},
									function(event) {
										m_utils.debug("Add condition clicked");
										var view = event.data.view;

										view.selector
												.setEventSource(jQuery(this));
										view.selector.clearItems();

										for ( var i = 0; i < view.ruleSet.parameterDefinitions.length; ++i) {
											m_utils
													.debug("Parameter: "
															+ view.ruleSet.parameterDefinitions[i].name);

											if (view.ruleSet.parameterDefinitions[i].direction == "OUT") {
												continue;
											}

											view.selector
													.addItem(
															"Conditions on data of <span class='factTerm'>"
																	+ view.ruleSet.parameterDefinitions[i].name
																	+ "</span>",
															{
																view : view,
																fact : view.ruleSet.parameterDefinitions[i]
															},
															function(data) {
																data.view
																		.addFactCondition(data.fact);
															});
										}
										view.selector
												.addItem(
														"Conditions of which on one or more is true",
														{
															view : view
														},
														function(data) {
															data.view
																	.addOrCondition();
														});
										view.selector
												.addItem(
														"Free form condition entry",
														{
															view : view
														},
														function(data) {
															data.view
																	.addFreeFormCondition();
														});

										event.data.view.selector.open(event);
									});

					this.addActionButton
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										view.selector
												.setEventSource(jQuery(this));
										view.selector.clearItems();

										for ( var i = 0; i < view.ruleSet.parameterDefinitions.length; ++i) {
											if (view.ruleSet.parameterDefinitions[i].direction == "IN") {
												continue;
											}

											view.selector
													.addItem(
															"Actions on data of <span class='factTerm'>"
																	+ view.ruleSet.parameterDefinitions[i].name
																	+ "</span>",
															{
																view : view,
																fact : view.ruleSet.parameterDefinitions[i]
															},
															function(data) {
																data.view
																		.addFactAction(data.fact);
															});
										}

										view.selector
												.addItem(
														"Free form action entry",
														{
															view : view
														},
														function(data) {
															data.view
																	.addFreeFormAction();
														});

										event.data.view.selector.open(event);
									});
				};

				/**
				 * 
				 */
				RuleEditor.prototype.activate = function(ruleSet, rule) {
					this.rule = rule;
					this.ruleSet = ruleSet;
				};

				/**
				 * 
				 */
				RuleEditor.prototype.toString = function() {
					return "rules-manager.RuleEditor";
				};

				/**
				 * 
				 */
				RuleEditor.prototype.createRuleRow = function() {
					var row = jQuery("<tr valign='center' class='ruleRow'></tr>");

					row.click({
						row : row
					}, function(event) {
						event.data.row.toggleClass("selected");
					});

					return row;
				};

				/**
				 * 
				 */
				RuleEditor.prototype.createButtonCell = function(deleteButton,
						addButton) {
					var topCell = jQuery("<td align='right' class='ruleCell'></td>");
					var buttonTable = jQuery("<table cellpadding='0' cellspacing='0' class='layoutTable'></table>");

					topCell.append(buttonTable);

					var buttonRow = jQuery("<tr></tr>");

					buttonTable.append(buttonRow);

					var buttonCell = null;

					if (deleteButton) {
						buttonCell = jQuery("<td></td>");

						buttonRow.append(buttonCell);
						buttonCell.append(deleteButton);
					}

					if (addButton) {
						buttonCell = jQuery("<td></td>");

						buttonRow.append(buttonCell);
						buttonCell.append(addButton);
					}

					return topCell;
				};

				/**
				 * 
				 */
				RuleEditor.prototype.createAddButton = function(items) {
					var button = jQuery("<input type='image' src='plugins/rules-manager/images/icons/add.png' "
							+ "title='Add' alt='Add' class='toolbarButton' />");

					button.click({
						view : this
					}, function(event) {
						event.data.view.selector.setEventSource(jQuery(this));
						event.data.view.selector.clearItems();
						event.data.view.selector.addItems(items);
						event.data.view.selector.open(event);
					});

					return button;
				};

				/**
				 * 
				 */
				RuleEditor.prototype.createDeleteButton = function() {
					var button = jQuery("<input type='image' src='plugins/rules-manager/images/icons/delete.png' "
							+ "title='Delete' alt='Delete' class='toolbarButton' />");

					return button;
				};

				/**
				 * 
				 */
				RuleEditor.prototype.addFactCondition = function(fact) {
					var row = this.createRuleRow();

					row
							.append(jQuery("<td class='ruleCell'>For <span class='factTerm'>"
									+ fact.name + "</span></td>"));

					var items = [];

					if (fact.dataType == "struct") {
						var typeDeclaration = m_model
								.findTypeDeclaration(fact.structuredDataTypeFullId);

						for ( var m = 0; m < typeDeclaration.getBody().elements.length; ++m) {
							var element = typeDeclaration.getBody().elements[m];
							items
									.push({
										label : "Condition for <span class='factTerm'>"
												+ element.name + "</span>",
										data : {
											view : this,
											element : element
										},
										callback : function(data) {
											data.view
													.addPropertyCondition(data.element.name);
										}
									});
						}
					}

					row.append(this.createButtonCell(this.createDeleteButton(),
							this.createAddButton(items)));

					this.conditionsTable.append(row);
				};

				/**
				 * 
				 */
				RuleEditor.prototype.addOrCondition = function() {
					var row = this.createRuleRow();

					row
							.append(jQuery("<td class='ruleCell'>All of the following conditions</td>"));
					row.append(this.createButtonCell(this.createAddButton(),
							this.createDeleteButton()));

					this.conditionsTable.append(row);
				};

				/**
				 * 
				 */
				RuleEditor.prototype.addPropertyCondition = function(property) {
					var row = this.createRuleRow();

					var propertyTable = jQuery("<table cellspacing='0' cellpadding='0' class='layoutTable'></table>");

					var propertyRow = jQuery("<tr valign='center'></tr>");

					propertyTable.append(propertyRow);

					propertyRow
							.append("<td style='vertical-align: center;'>Property <span class='factTerm'>"
									+ property + "</span></td>");

					var conditionCell = jQuery("<td style='vertical-align: center;'></td>");

					propertyRow.append(conditionCell);

					var conditionSelect = jQuery("<select></select>");

					conditionCell.append(conditionSelect);

					conditionSelect
							.append("<option value='lessThan'>equals</option>");
					conditionSelect
							.append("<option value='lessThan'>less than</option>");
					conditionSelect
							.append("<option value='lessThan'>greater than</option>");

					var operandCell = jQuery("<td style='vertical-align: center;'><input type='text'></input></td>");

					propertyRow.append(operandCell);

					var propertyTableCell = jQuery("<td class='ruleCell' style='padding-left: 20px;'></td>");

					propertyTableCell.append(propertyTable);

					row.append(propertyTableCell);
					row
							.append(this.createButtonCell(this
									.createDeleteButton()));

					this.conditionsTable.append(row);
				};

				/**
				 * 
				 */
				RuleEditor.prototype.addFreeFormCondition = function() {
					var row = this.createRuleRow();
					var cell = jQuery("<td class='ruleCell'></td>");
					row.append(cell);

					var textArea = jQuery("<textarea></textarea>");

					cell.append(textArea);

					row
							.append(this.createButtonCell(this
									.createDeleteButton()));

					this.conditionsTable.append(row);
				};

				/**
				 * 
				 */
				RuleEditor.prototype.addFactAction = function(fact) {
					var row = this.createRuleRow();

					row
							.append(jQuery("<td class='ruleCell'>On <span class='factTerm'>"
									+ fact.name + "</span></td>"));

					var items = [];

					if (fact.dataType == "struct") {
						var typeDeclaration = m_model
								.findTypeDeclaration(fact.structuredDataTypeFullId);

						for ( var m = 0; m < typeDeclaration.getBody().elements.length; ++m) {
							var element = typeDeclaration.getBody().elements[m];
							items
									.push({
										label : "Condition for <span class='factTerm'>"
												+ element.name + "</span>",
										data : {
											view : this,
											element : element
										},
										callback : function(data) {
											data.view
													.addPropertyAction(data.element.name);
										}
									});
						}
					}

					row.append(this.createButtonCell(this.createDeleteButton(),
							this.createAddButton(items)));
					this.actionsTable.append(row);
				};

				/**
				 * 
				 */
				RuleEditor.prototype.addPropertyAction = function(property) {
					var row = this.createRuleRow();

					var propertyTable = jQuery("<table cellspacing='0' cellpadding='0' class='layoutTable'></table>");

					var propertyRow = jQuery("<tr valign='center'></tr>");

					propertyTable.append(propertyRow);

					propertyRow
							.append("<td>Set property <span class='factTerm'>"
									+ property + "</span> to </td>");

					var valueCell = jQuery("<td><input type='text'></input></td>");

					propertyRow.append(valueCell);

					var propertyTableCell = jQuery("<td class='ruleCell' style='padding-left: 20px;'></td>");

					propertyTableCell.append(propertyTable);

					row.append(propertyTableCell);
					row
							.append(this.createButtonCell(this
									.createDeleteButton()));

					this.actionsTable.append(row);
				};

				/**
				 * 
				 */
				RuleEditor.prototype.addFreeFormAction = function() {
					var row = this.createRuleRow();
					var cell = jQuery("<td class='ruleCell'></td>");
					row.append(cell);

					var textArea = jQuery("<textarea></textarea>");

					cell.append(textArea);

					row
							.append(this.createButtonCell(this
									.createDeleteButton()));

					this.actionsTable.append(row);
				};

				/**
				 * 
				 */
				RuleEditor.prototype.validate = function() {
					return true;
				};
			}
		});