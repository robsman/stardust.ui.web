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
								"type" : "boolean"
							},
							{
								"name" : "persistent",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.storage.persistent"),
								"type" : "boolean"
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
							{
								"name" : "InputPreferences_uiformat",
								"label" : m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.fieldProperties.ui.format"),
								"type" : "string"
							},
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

							if (this.element.annotations) {
								this.element.annotations[name] = newValue;
								this.view
										.submitChanges({
											typeDeclaration : this.view.typeDeclaration.typeDeclaration
										});
							}
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
										rowExpandedStatus[this.id] = $(this)
												.hasClass("expanded");
									});
							var selectedRowId = jQuery(
									"table#fieldPropertiesTable tr.selected")
									.first().attr('id');

							this.initialize(categories, element, view);

							// Restore expanded status
							this.tableBody
									.find("tr")
									.each(
											function(index) {
												if (rowExpandedStatus[this.id]) {
													jQuery(this).addClass(
															"expanded");
												}
												for (id in rowExpandedStatus) {
													var parentClassId = "child-of-"
															+ id;
													if (rowExpandedStatus[id]
															&& this.classList
																	.contains(parentClassId)) {
														jQuery(this)
																.removeClass(
																		"ui-helper-hidden");
													}
												}
											});

							// Restore the selected tree nodes
							jQuery("#fieldPropertiesTable #" + selectedRowId)
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

							this.table = jQuery("#fieldPropertiesTable");
							this.tableBody = jQuery("table#fieldPropertiesTable tbody");

							this.tableBody.empty();

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

								var keyPrefix = "appinfo.stardust."
										+ categoryName + ".";
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

									var propertyPrefV;

									if (element.annotations) {
										propertyPrefV = element.annotations[keyPrefix
												+ property.name];
									}

									propertyPrefV = propertyPrefV ? propertyPrefV
											: "";

									if (property.type == "string") {
										if (property.enumeration == null) {
											content += "<input type=\"text\" name="
													+ keyPrefix
													+ property.name
													+ " value="
													+ propertyPrefV
													+ "\>";
										} else {
											content += "<select name="
													+ keyPrefix + property.name
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
										content += "<input type=\"text\"\ style=\"text-align: right;\">";
									} else if (property.type == "boolean") {
										content += "<input type=\"checkbox\" name="
												+ keyPrefix + property.name;
										if (propertyPrefV) {
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

							jQuery("table#fieldPropertiesTable tbody tr")
									.mousedown(
											function() {
												jQuery(
														"table#fieldPropertiesTable tbody tr.selected")
														.removeClass("selected");
												jQuery(this).addClass(
														"selected");
											});

							var self = this;
							jQuery(
									"table#fieldPropertiesTable tbody tr input:text")
									.on(
											"change",
											function(event) {
												self.updateAnnotation(
														this.name, jQuery(
																event.target)
																.val());
											});

							jQuery("table#fieldPropertiesTable tbody tr select")
									.on(
											"change",
											function(event) {
												self.updateAnnotation(
														this.name, jQuery(
																event.target)
																.val());
											});

							jQuery(
									"table#fieldPropertiesTable tbody tr input:checkbox")
									.change(
											function(event) {
												self
														.updateAnnotation(
																this.name,
																jQuery(
																		event.target)
																		.is(
																				":checked") ? true
																		: false);
											});
						};
			}
			;
		});