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
 * @author Yogesh.Manware
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_i18nUtils) {

			var categories = {
				"storage" : {
					"label" : m_i18nUtils
							.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.storage"),
					"categories" : [],
					"properties" : [
							{
								"name" : "indexed",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.storage.indexed"),
								"type" : "boolean",
								"defaultValue" : "true"
							},
							{
								"name" : "persistent",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.storage.persistent"),
								"type" : "boolean",
								"defaultValue" : "true"
							}, ]
				},
				"ui" : {
					"label" : m_i18nUtils
							.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui"),
					"properties" : [
							{
								"name" : "InputPreferences_label",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.label"),
								"type" : "string"
							},
							{
								"name" : "InputPreferences_labelKey",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.labelKey"),
								"type" : "string"
							},
							{
								"name" : "InputPreferences_showDescription",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.showDescription"),
								"type" : "boolean"
							},
							{
								"name" : "InputPreferences_readonly",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.readOnly"),
								"type" : "boolean"
							},
							{
								"name" : "InputPreferences_mandatory",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.mandatory"),
								"type" : "boolean"
							},
//							{
//								"name" : "InputPreferences_uiformat",
//								"label" : m_i18nUtils
//										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.format"),
//								"type" : "string"
//							},
							{
								"name" : "InputPreferences_style",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.cssStyle"),
								"type" : "string"
							},
							{
								"name" : "InputPreferences_styleClass",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.cssStyleClass"),
								"type" : "string"
							},
							{
								"name" : "InputPreferences_prefixKey",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.prefixI18NKey"),
								"type" : "string"
							},
							{
								"name" : "InputPreferences_prefix",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.prefix"),
								"type" : "string"
							},
							{
								"name" : "InputPreferences_suffixKey",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.suffixI18NKey"),
								"type" : "string"
							},
							{
								"name" : "InputPreferences_suffix",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.suffix"),
								"type" : "string"
							},
							{
								"name" : "StringInputPreferences_stringInputType",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.stringInputType"),
								"type" : "string",
								"enumeration" : [
										{
											"label" : m_i18nUtils
													.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.textInput"),
											"value" : "TEXTINPUT"
										},
										{
											"label" : m_i18nUtils
													.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.textArea"),
											"value" : "TEXTAREA"
										} ]
							},
							{
								"name" : "StringInputPreferences_textAreaRows",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.textAreaRows"),
								"type" : "long"
							},
							{
								"name" : "StringInputPreferences_textAreaColumns",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.textAreaColumns"),
								"type" : "long"
							},
							{
								"name" : "BooleanInputPreferences_readonlyOutputType",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.booleanReadonlyInputType"),
								"type" : "string",
								"enumeration" : [
										{
											"label" : m_i18nUtils
													.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.checkBox"),
											"value" : "CHECKBOX"
										},
										{
											"label" : m_i18nUtils
													.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.textOutput"),
											"value" : "TEXTOUTPUT"
										} ]
							} ]
				}
			};

			return {
				create : function(element, view) {
					var propertiesTree = new PropertiesTree();

					propertiesTree.initialize(categories, element, view);

					return propertiesTree;
				},

				refresh : function(propertiesTree, element, view) {
					propertiesTree.refresh(categories, element, view);
				}
			};

			/**
			 *
			 */
			function PropertiesTree() {
				var normalOptionTag = "<option value=\"&value\"\>&label</option\>";
				var selectedOptionTag = "<option value=\"&value\" selected\>&label</option\>";

				this.categories = null;
				this.element = null;
				this.view = null;

				/**
				 *
				 */
				PropertiesTree.prototype.toString = function() {
					return "Lightdust.PropertiesTree";
				};

						/**
						 * update annotations
						 */
						PropertiesTree.prototype.updateAnnotation = function(
								name, newValue) {

							if (!name || name.indexOf(".") == -1) {
								return;
							}

							if(!this.element.appinfo){
								this.element.appinfo = {};
							}

							var nameArr = name.split(".");
							var category = nameArr[0];
							var attr = nameArr[1];

							if(!category || !attr){
								return;
							}

							if (!this.element.appinfo[category]) {
								this.element.appinfo[category] = {};
							}
							this.element.appinfo[category][attr] = newValue;

							this.view
									.submitChanges({
										typeDeclaration : this.view.typeDeclaration.typeDeclaration
									});
						},

						/**
						 * refresh
						 */
						PropertiesTree.prototype.refresh = function(categories,
								element, view) {

							if (!element) {
								return;
							}

							var rowExpandedStatus = [];

							this.tableBody.find("tr").each(
									function(index) {
										rowExpandedStatus[this.id] = m_utils.jQuerySelect(this)
												.hasClass("expanded");
									});
							var selectedRowId = m_utils.jQuerySelect(
									"table#fieldPropertiesTable tr.selected")
									.first().attr('id');

							this.initialize(categories, element, view);

							// Restore expanded status
							this.tableBody
									.find("tr")
									.each(
											function(index) {
												if (rowExpandedStatus[this.id]) {
													m_utils.jQuerySelect(this).addClass(
															"expanded");
												}
												for (id in rowExpandedStatus) {
													var parentClassId = "child-of-"
															+ id;
													if (rowExpandedStatus[id]
															&& this.classList
																	.contains(parentClassId)) {
														m_utils.jQuerySelect(this)
																.removeClass(
																		"ui-helper-hidden");
													}
												}
											});

							// Restore the selected tree nodes
							m_utils.jQuerySelect("#fieldPropertiesTable #" + selectedRowId)
									.addClass("selected");
						},

						/**
						 * Initialize
						 */
						PropertiesTree.prototype.initialize = function(
								categories, element, view) {

							this.categories = categories;
							this.element = element;
							this.view = view;

							this.table = m_utils.jQuerySelect("#fieldPropertiesTable");
							this.tableBody = m_utils.jQuerySelect("table#fieldPropertiesTable tbody");

							this.tableBody.empty();

							if (!element) {
								return;
							}

							var n = 0;

							for (categoryName in this.categories) {
								var category = this.categories[categoryName];
								var content = "<tr id=\"categoryRow-" + n
										+ "\">";

								content += "<td>";
								content += category.label;
								content += "</td>";
								content += "<td>";
								content += "</td>";
								content += "</tr>";

								this.tableBody.append(content);

								var m = 0;

								for (propertyName in category.properties) {
									var property = category.properties[propertyName];

									content = "<tr id=\"propertyRow-"
											+ m
											+ "\" class=\"child-of-categoryRow-"
											+ n + "\">";
									content += "<td><span>";
									content += property.label;
									content += "</span></td>";
									content += "<td class=\"editable\">";

									var propertyPrefV = null;

									if (element.appinfo) {
										if (element.appinfo[categoryName]) {
											propertyPrefV = element.appinfo[categoryName][property.name];
										}
									}

									if (!propertyPrefV) {
										if (property.defaultValue) {
											propertyPrefV = property.defaultValue;
										} else {
											propertyPrefV = "";
										}
									}

									var keyName = categoryName + "." + property.name;

									if (property.type == "string") {
										if (property.enumeration == null) {
											content += "<input type=\"text\" name="
													+ keyName
													+ " value="
													+ "\""
													+ propertyPrefV
													+ "\"" + "\>";
										} else {
											content += "<select name="
													+ keyName
													+ ">";

											for (enumeratorName in property.enumeration) {
												var enumerator = property.enumeration[enumeratorName];
												var optionTag = null;
												if (propertyPrefV == enumerator.value) {
													optionTag = selectedOptionTag;
												} else {
													optionTag = normalOptionTag;
												}
												optionTag = optionTag.replace(
														"&value",
														enumerator.value);
												optionTag = optionTag.replace(
														"&label",
														enumerator.label);
												content += optionTag;
											}

											content += "</select>";
										}
									} else if (property.type == "long") {
										content += "<input type=\"text\" style=\"text-align: right;\" name="
												+ keyName
												+ " value="
												+ propertyPrefV
												+ "\>";
									} else if (property.type == "boolean") {
										content += "<input type=\"checkbox\" name="
												+ keyName;
										if (propertyPrefV == "true"
												|| propertyPrefV == true) {
											content += " checked";
										}
										content += "\>";
									}

									content += "</td>";
									content += "</tr>";

									this.tableBody.append(content);

									++m;
								}

								++n;
							}

							this.table.tableScroll({
								height : 150
							});
							this.table.treeTable();

							m_utils.jQuerySelect("table#fieldPropertiesTable tbody tr")
									.mousedown(
											function() {
												m_utils.jQuerySelect(
														"table#fieldPropertiesTable tbody tr.selected")
														.removeClass("selected");
												m_utils.jQuerySelect(this).addClass(
														"selected");
											});

							var self = this;
							m_utils.jQuerySelect(
									"table#fieldPropertiesTable tbody tr input:text")
									.on(
											"change",
											function(event) {
												self.updateAnnotation(
														this.name, m_utils.jQuerySelect(
																event.target)
																.val());
											});

							m_utils.jQuerySelect("table#fieldPropertiesTable tbody tr select")
									.on(
											"change",
											function(event) {
												self.updateAnnotation(
														this.name, m_utils.jQuerySelect(
																event.target)
																.val());
											});

							m_utils.jQuerySelect(
									"table#fieldPropertiesTable tbody tr input:checkbox")
									.change(
											function(event) {
												self
														.updateAnnotation(
																this.name,
																m_utils.jQuerySelect(
																		event.target)
																		.is(
																				":checked") ? true
																		: false);
											});

							if (this.view.getModelElement().isReadonly()) {
								m_utils
										.markControlsReadonly(
												"fieldPropertiesTableDiv",
												this.view.getModelElement()
														.isReadonly());
							}

							var width = m_utils.jQuerySelect("#fieldPropertiesTableDiv").find("#property").width();
							m_utils.jQuerySelect("table#fieldPropertiesTable").find("tr > td:first").width(width + "px");
						};
			};
		});