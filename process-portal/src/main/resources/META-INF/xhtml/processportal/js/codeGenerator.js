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
 * @author Subodh.Godbole
 */

define(["processportal/js/htmlElement"], function(htmlElement){
	return {
		create: function(prefs) {
			return new CodeGenerator(prefs);
		}
	};

	/*
	 * 
	 */
	function CodeGenerator(prefs) {

		var UPPER_CASE_CHAR_REG_EX = /^[A-Z]$/;

		// Set Defaults
		if (prefs == undefined) {
			prefs = {};
		}
		
		if (prefs.layoutColumns == undefined) {
			prefs.layoutColumns = 3;
		} 
		if (prefs.tableColumns == undefined) {
			prefs.tableColumns = 0;
		}
		if (prefs.ngModelSepAsDot == undefined) {
			prefs.ngModelSepAsDot = false;
		}
		if (prefs.pluginsUrl == undefined) {
			prefs.pluginsUrl = "../../plugins";
		}
		if (prefs.skipMultiCardinalityNested == undefined) {
			prefs.skipMultiCardinalityNested = false;
		}
		if (prefs.splitDateTimeFields == undefined) {
			prefs.splitDateTimeFields = true;
		}

		var preferences = prefs;
		this.preferences = preferences;

		this.bindingPrefix;
		this.bindingData;
		this.i18nLabelProvider;
		this.ignoreXPath;
		this.formElemName;
		this.validationRulesAndMessages = {rules: {}, messages: {}};

		var nestedBindings;
	
		/*
		 * 
		 */
		CodeGenerator.prototype.generate = function(paths, prefix, i18nProvider, ignoreParentXPath, formName) {
			this.bindingPrefix = prefix;
			this.i18nLabelProvider = i18nProvider;
			this.ignoreXPath = ignoreParentXPath;
			this.formElemName = formName;
			if (!this.formElemName) {
				this.formElemName = "form";
			}
	
			this.bindingData = {};
			nestedBindings = [];

			var elemForm = htmlElement.create("form", {attributes: {name: this.formElemName}});
			var elemMain = htmlElement.create("div", {parent: elemForm, attributes: {class: "panel-main"}});

			this.generateValidationBar(elemMain);
			
			// Generate
			this.generateChildren(elemMain, paths);

			var html = elemForm.toHtml();
			html = this.generateNestedTypeDialogIFRAME(html);

			this.bindingPrefix = undefined;
			this.i18nLabelProvider = undefined;
			this.ignoreXPath = undefined;
			this.formElemName = undefined;

			return {html: html, binding: this.bindingData, nestedBindings: nestedBindings};
		};
		
		/**
		 * 
		 */
		CodeGenerator.prototype.generateValidationBar = function(elemMain) {
			// Validation
			var showExpr = this.formElemName + ".$invalid";
			var elemValidationBar = htmlElement.create("div", {parent: elemMain, 
				attributes: {class: "panel-validation-summary-bar", "ng-show": showExpr}});
			htmlElement.create("span", {parent: elemValidationBar, attributes: {class: "panel-validation-summary-bar-img"}});
			htmlElement.create("span", {parent: elemValidationBar, value: this.getI18NLabel("validation.err", "Form contains error(s)."), 
				attributes: {class: "panel-validation-summary-bar-text"}});
		}
		
		/**
		 * 
		 */
		CodeGenerator.prototype.generateNestedTypeDialogIFRAME = function(html) {
			if (this.ignoreXPath == undefined) {
				var nestedListsDiv =
					"<iframe ng-show=\"showNestedDM\" class=\"panel-list-dialog-modal-iframe\"" +
						"style=\"width: {{sizes.frameWidth}}; height: {{sizes.frameHeight}};\" src=\"about:blank\"></iframe>\n" +
					"<div ng-show=\"showNestedDM\" class=\"panel-list-dialog\" style=\"left: {{sizes.dialogLeft}}; top: {{sizes.dialogTop}}\">" + 
						"<div class=\"panel-list-dialog-title\">" +
							"<span class=\"panel-list-dialog-title-text\">{{nestedDMTitle}}</span>" + 
							"<input type=\"button\" class=\"panel-list-dialog-close\" ng-click=\"closeNestedList()\"></input></div>" + 
						"<div class=\"panel-list-dialog-breadcrumb\">" + 
							"<span class=\"panel-list-dialog-breadcrumb-item\" ng-repeat=\"bCrumb in nestedDMs\">" + 
								"<a href=\"\" ng-click=\"showNestedList($index)\" ng-show=\"!$last\">{{bCrumb.label}}</a>" + 
								"<span ng-show=\"$last\">{{bCrumb.label}}</span>" +
								"<span ng-show=\"!$last\"> &raquo; </span></span></div>" + 
						"<div class=\"panel-list-dialog-content\"></div>" + 
						"<div class=\"panel-list-dialog-footer\">" +
							"<input type=\"button\" value=\"Close\" class=\"panel-list-dialog-footer-control\" ng-click=\"closeNestedList()\" /></div>" +
					"</div>";
				html += "\n" + nestedListsDiv;
			}
			return html;
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.generatePath = function(parent, path, options) {
			if (path.isList) {
				return this.generateList(parent, path, options);
			} else if (path.isPrimitive) {
				return this.generatePriEnum(parent, path, options);
			} else {
				return this.generateStruct(parent, path, options);
			}
		};

		/*
		 * 
		 */
		CodeGenerator.prototype.generateList = function(parent, path) {
			var elemMain = htmlElement.create("div", {parent: parent, attributes: {class: "panel-list"}});

			var listBinding = this.convertFullIdToBinding(path);

			// Header
			htmlElement.create("div", {parent: elemMain, value: this.getI18NLabel(path), attributes: {class: "panel-header"}});

			// Toolbar
			if (!this.isReadonly(path)) {
				var elemToolbar = htmlElement.create("div", {parent: elemMain, attributes: {class: "panel-list-toolbar"}});
				var elemToolbarTrTbl = htmlElement.create("table", 
						{parent: elemToolbar, attributes: {class: "panel-list-toolbar-tbl", cellpadding: 0, cellspacing: 0}});
				var elemToolbarTr = htmlElement.create("tr", {parent: htmlElement.create("tbody", 
						{parent: elemToolbarTrTbl}), attributes: {class: "panel-list-toolbar-tbl-row"}});
				
				var elemAddButton = htmlElement.create("a", {parent: htmlElement.create("td", 
						{parent: elemToolbarTr, attributes: {class: "panel-list-toolbar-tbl-cell"}})});
				elemAddButton.attributes["href"] = "";
				elemAddButton.attributes["ng-click"] = "addToList(" + listBinding + ", '" + path.fullXPath + "')";
				htmlElement.create("img", {parent: elemAddButton, 
					attributes: {src: preferences.pluginsUrl + "/stardust-ui-form-jsf/public/css/images/add.png", class: "panel-image"}});
				
				var elemRemoveButton = htmlElement.create("a", {parent: htmlElement.create("td", 
						{parent: elemToolbarTr, attributes: {class: "panel-list-toolbar-tbl-cell"}})});
				elemRemoveButton.attributes["href"] = "";
				elemRemoveButton.attributes["ng-click"] = "removeFromList(" + listBinding + ", '" + path.fullXPath + "')";
				htmlElement.create("img", {parent: elemRemoveButton, 
					attributes: {src: preferences.pluginsUrl + "/stardust-ui-form-jsf/public/css/images/delete.png", class: "panel-image"}});
			}

			// Table
			var elemTbl = htmlElement.create("table", {parent: elemMain, 
				attributes: {class: "panel-list-tbl", cellpadding: 0, cellspacing: 0}});
			var elemTHead = htmlElement.create("thead", {parent: elemTbl});
			var elemTBody = htmlElement.create("tbody", {parent: elemTbl});

			var loopVar = "$listIterator";

			var elemTHeadTr = htmlElement.create("tr", {parent: elemTHead});
			var elemTBodyTr = htmlElement.create("tr", {parent: elemTBody});
			
			elemTBodyTr.attributes["ng-class"] = "{'panel-list-tbl-row-sel': " 
				+ loopVar + ".$$selected, 'panel-list-tbl-row1': ($index % 2 == 0), 'panel-list-tbl-row2': ($index % 2 != 0) }";
			elemTBodyTr.attributes["ng-repeat"] = loopVar + " in " + listBinding;
			var innerForm = "innerForm"
			elemTBodyTr.attributes["ng-form"] = innerForm;
			if (!this.isReadonly(path)) {
				elemTBodyTr.attributes["ng-click"] = "selectListItem($event, " + loopVar + ")";
			}

			if (path.isPrimitive) { // List of Primitives
				htmlElement.create("th", {parent: elemTHeadTr, value: this.getLabel(path), attributes: {class: "panel-list-tbl-header"}});
				var elemTd = htmlElement.create("td", {parent: elemTBodyTr, attributes: {class: "panel-list-tbl-cell"}});
				
				var loopNgModel = preferences.ngModelSepAsDot ? (loopVar + ".$value") : (loopVar + "['$value']");
				this.generatePriEnum(elemTd, path, {noLabel: true, ngModel: loopNgModel, ngFormName: innerForm});
			} else { // List of Structures
				for (var i in path.children) {
					if (preferences.tableColumns > 0 && i >= preferences.tableColumns) {
						break;
					}

					var child = path.children[i];
					
					// Table Head
					if (child.isPrimitive || !preferences.skipMultiCardinalityNested) {
						htmlElement.create("th", {parent: elemTHeadTr, value: this.getLabel(child), attributes: {class: "panel-list-tbl-header"}});
						var elemTd = htmlElement.create("td", {parent: elemTBodyTr, attributes: {class: "panel-list-tbl-cell"}});
					}

					// Table Body
					if (child.isPrimitive && !child.isList) {
						var loopNgModel = preferences.ngModelSepAsDot ? (loopVar + "." + child.id) : (loopVar + "['" + child.id + "']");
						var elemPrimitive = this.generatePriEnum(null, child, 
								{noLabel: true, ngModel: loopNgModel, ngFormName: innerForm});
						if (elemPrimitive.children.length > 1) { // Control with Validation
							var elemWrapperTr = htmlElement.create("tr", {parent: 
								htmlElement.create("table", {parent: elemTd, attributes: {cellpadding: 0, cellspacing: 0}})});
							
							var elemWrapperTd1 = htmlElement.create("td", {parent: elemWrapperTr, 
								attributes: {class: "panel-list-tbl-input-column"}});
							elemWrapperTd1.children.push(elemPrimitive.children[0]);
							
							var elemWrapperTd2 = htmlElement.create("td", {parent: elemWrapperTr, 
								attributes: {class: "panel-list-tbl-validation-image-column"}});
							elemWrapperTd2.children.push(elemPrimitive.children[1]);
						} else { // Only Control no Validation
							elemTd.children.push(elemPrimitive.children[0]);
						}
					} else if (!preferences.skipMultiCardinalityNested) {
						var linkValue = this.isReadonly(path) ? this.getI18NLabel("panel.list.view", "View") : this.getI18NLabel("panel.list.edit", "Edit");
						if (child.isList) {
							var loopChild = loopVar + "['" + child.id + "']";
							linkValue += " ({{ {'true': " + loopChild + ".length, false: '0'}[" + loopChild + " != undefined] }})";
						}
						var elemLink = htmlElement.create("a", {parent: elemTd, value: linkValue});

						elemLink.attributes["ng-click"] = "openNestedList(" + loopVar + ", '" + child.fullXPath + 
							"', $index, '" + listBinding.replace(/'/g, '\\\'') + "', '" + this.getI18NLabel(path) + "', '" + 
							this.getI18NLabel(child) + "', " + this.isReadonly(path) + ")";
						
						nestedBindings.push(child);
					}
				}
			}
			return elemMain;
		};

		/*
		 * options: noLabel, ngModel, idExpr
		 */
		CodeGenerator.prototype.generatePriEnum = function(parent, path, options) {
			if (options == undefined) {
				options = {};
			}

			var elemMain = htmlElement.create("div", {parent: parent});
			
			if (options.noLabel == undefined || !options.noLabel) {
				var elemLabel = htmlElement.create("label", 
						{parent: elemMain, value: this.getLabel(path), attributes: {class: "panel-label"}});
			}

			var elem;
			if (path.typeName == "document") {
				elem = this.generateDocument(elemMain, path);
			} else if (this.isReadonly(path)) {
				var binding = (options.ngModel == undefined ? this.convertFullIdToBinding(path) : options.ngModel);

				if (path.properties["BooleanInputPreferences_readonlyOutputType"] != undefined &&
						path.properties["BooleanInputPreferences_readonlyOutputType"] == "CHECKBOX") {
					elem = htmlElement.create("input", {parent: elemMain, 
								attributes: {type: "checkbox", class: "panel-checkbox", disabled: true}});
					elem.attributes['ng-model'] = binding;
				} else {
					elem = htmlElement.create("label", {parent: elemMain, attributes: {class: "panel-output"}});

					var customFilter = this.getCustomFilter(path);
					if (customFilter) {
						binding = binding + " | " + customFilter;
					}

					elem.value = "{{" + binding + "}}";
				}
			} else {
				if (path.isEnum) {
					elem = htmlElement.create("select", {parent: elemMain, attributes: {'ng-model-onblur': null}});
					elem.attributes['class'] = "panel-select";
					var enumValues = this.getI18NEnumerationLabels(path);
					for(var key in enumValues) {
						var elemOpt = htmlElement.create("option", 
								{parent: elem, value: enumValues[key], attributes: {value: key}});
					}
				} else {
					
					var elemWrapper = this.generatePanelWrapper(elemMain);
					
					var validations = [];

					if (path.properties["StringInputPreferences_stringInputType"] == "TEXTAREA") {
						elem = htmlElement.create("textarea", {parent: elemWrapper});
						elem.attributes["style"] = "width: auto;";
						if (path.properties["StringInputPreferences_textAreaRows"] != undefined) {
							elem.attributes["rows"] = path.properties["StringInputPreferences_textAreaRows"];
						}
						if (path.properties["StringInputPreferences_textAreaColumns"] != undefined) {
							elem.attributes["cols"] = path.properties["StringInputPreferences_textAreaColumns"];
						}
					} else {
						if (options.labelAsBooleanInput && ("boolean" === path.typeName || "java.lang.Boolean" === path.typeName)) {
							elem = htmlElement.create("input", {parent: elemLabel});
						} else {
							elem = htmlElement.create("input", {parent: elemWrapper});
						}
					}

					elem.attributes["ng-model-onblur"] = null;

					if (path.properties["InputPreferences_mandatory"] != undefined && 
							path.properties["InputPreferences_mandatory"] == "true") {
						validations.push({type: "ng-required", value: true, msg: this.getI18NLabel("validation.err.Required", "Required")});
					}

					if (path.typeName == "date" || path.typeName == "java.util.Date" || path.typeName == "dateTime"
						|| path.typeName == "java.util.Calendar" || path.typeName == "time") {
						this.setInputElementTypeForDateInput(elem);
						this.addCustomDirective(path, elem);
					} else if (this.isNumber(path)) {
						this.setInputElementTypeForNumericalInput(elem);
					} else if ("boolean" === path.typeName || "java.lang.Boolean" === path.typeName) {
						elem.attributes['type'] = "checkbox";
						elem.attributes['class'] = "panel-checkbox";
					} else {
						elem.attributes['type'] = "text";
					}

					if (!elem.attributes['class']) { 
						elem.attributes['class'] = "panel-input";
					}

					var valInfo = this.getValidationInfo(path);
					if (valInfo.type) {
						validations.push({type: valInfo.type, value: valInfo.value, 
							msg: this.getI18NLabel("validation.err." + valInfo.key, "Invalid " + valInfo.key)});
					}

					var maxLength = this.getMaxLength(path);
					if (maxLength) {
						elem.attributes['maxlength'] = maxLength;
					}

					var valInfo = this.addCustomDirective(path, elem);
					if (valInfo.type) {
						validations.push({type: valInfo.type, value: valInfo.value, 
							msg: this.getI18NLabel("validation.err." + valInfo.key, "Invalid " + valInfo.key)});
					}

					if(this.isNumber(path)) {
						elem.attributes['class'] += " panel-input-number";
					}

					// Handle Date and Time
					if (preferences.splitDateTimeFields) {
						if (path.typeName == "dateTime" || path.typeName == "java.util.Date" || 
								path.typeName == "java.util.Calendar" || path.typeName == "time") {
							if (path.typeName == "time") {
								elem.attributes['ng-show'] = "false"; // Permonently Hide Date Part
							} else if (path.typeName == "dateTime") {
								this.handleDateTimeInputs(elem);
							} else {
								elem.attributes['class'] = "panel-input-dateTime-date " + elem.attributes['class'];
							}
	
							if (this.haveSeparateFieldForTime(path)) {
								// Input field for Time Part
								var elem2 = this.addTimeInputField(elemWrapper);
								
								var validations2 = [];
								validations2.push({type: "ng-pattern", value: /^(0?[0-9]|1[0-9]|2[0123]):[0-5][0-9]$/, 
									msg: this.getI18NLabel("validation.err.time", "Invalid Time")});
		
								this.processValidations(elem2, validations2, 
										options.ngFormName ? options.ngFormName : this.formElemName, elemWrapper, elemMain);
		
								// (loopVar + "." + child.id) : (loopVar + "['" + child.id + "']");
								var ngModel2 = options.ngModel == undefined ? this.convertFullIdToBinding(path) : options.ngModel;
								if (ngModel2.lastIndexOf("']") > -1) {
									var index = ngModel2.lastIndexOf("']");
									var part1 = ngModel2.substring(0, index);
									var part2 = ngModel2.substring(index);
									ngModel2 = part1 + "_timePart" + part2;
								} else {
									ngModel2 += "_timePart";
								}
		
								elem2.attributes['ng-model'] = ngModel2;
							}
						}
					}

					this.processValidations(elem, validations, 
							options.ngFormName ? options.ngFormName : this.formElemName, elemWrapper, elemMain);
				}

				if(path.properties["InputPreferences_styleClass"] != undefined) {
					var clazz = path.properties["InputPreferences_styleClass"];
					if (elem.attributes['class']) {
						clazz = clazz + " " + elem.attributes['class'];	
					}
					elem.attributes['class'] = clazz;
				}

				if(path.properties["InputPreferences_style"] != undefined) {
					var style = path.properties["InputPreferences_style"] + ";";
					if (elem.attributes['style']) {
						style = style + " " + elem.attributes['style'];	
					}
					elem.attributes['style'] = style;
				}

				elem.attributes['ng-model'] = options.ngModel == undefined ? this.convertFullIdToBinding(path) : options.ngModel;
			}
			
			return elemMain;
		};
		
		/**
		 * 
		 */
		CodeGenerator.prototype.handleDateTimeInputs = function(elem) {
			// DO nothing. Subclasses may handle things differently
		};
		
		/**
		 * 
		 */
		CodeGenerator.prototype.haveSeparateFieldForTime = function(path) {
			return true;
		};
		
		/**
		 * 
		 */
		CodeGenerator.prototype.addTimeInputField = function(parent) {
			var elem2 = htmlElement.create("input", {parent: parent});
			elem2.attributes["ng-model-onblur"] = null;
			elem2.attributes['type'] = "text";
			elem2.attributes['class'] = "panel-input-dateTime-time panel-input";
			elem2.attributes['maxlength'] = 5; // HH:mm
			
			return elem2;
		};
		
		/**
		 * 
		 */
		CodeGenerator.prototype.setInputElementTypeForDateInput = function(elem) {
			elem.attributes['type'] = "text";
		};
		
		/**
		 * 
		 */
		CodeGenerator.prototype.setInputElementTypeForNumericalInput = function(elem) {
			elem.attributes['type'] = "text";
		};
		
		/**
		 * 
		 */
		CodeGenerator.prototype.generatePanelWrapper = function(parentElem) {
			return htmlElement.create("div", {parent: parentElem, attributes: {class: "panel-wrapper"}});
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.processValidations = function(elem, validations, ngFormName, elemWrapper, elemMain) {
			if (validations.length > 0) {
				var id = "id" + Math.floor((Math.random() * 100000) + 1);
				var formId = "'" + id + "'";

				elem.attributes['id'] = id;
				elem.attributes['name'] = id;

				//var ngFormName = options.ngFormName ? options.ngFormName : this.formElemName;
				for (var i = 0; i < validations.length; i++) {
					elem.attributes[validations[i].type] = validations[i].value;
					var showExpr = ngFormName + "[" + formId + "].$error." + validations[i].type.split("-")[1];
					htmlElement.create("div", {parent: elemWrapper, value: validations[i].msg, 
						attributes: {class: "panel-invalid-msg", "ng-show": showExpr}});
				}

				var showExpr = ngFormName + "[" + formId + "].$invalid";
				htmlElement.create("span", {parent: elemMain, 
					attributes: {class: "panel-invalid-icon", "ng-show": showExpr}});
			}
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.isReadonly = function(path) {
			return path.properties["InputPreferences_readonly"] == "true" || path.readonly;
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.getValidationInfo = function(path) {
			var ret = {};
			if (path.typeName == "integer" || path.typeName == "int" ||path.typeName == "java.lang.Integer") {
				ret.type = "sd-validate";
				ret.value = "integer";
				ret.key = "integer";
			} else if (path.typeName == "short" || path.typeName == "java.lang.Short") {
				ret.type = "sd-validate";
				ret.value = "short";
				ret.key = "short";
			} else if (path.typeName == "long" || path.typeName == "java.lang.Long") {
				ret.type = "sd-validate";
				ret.value = "long";
				ret.key = "long";
			} else if (path.typeName == "float" || path.typeName == "java.lang.Float") {
				ret.type = "ng-pattern";
				ret.value = /^[-+]?\d{0,308}(\.\d{1,309})?%?$/;
				ret.key = "float";
			} else if (path.typeName == "double" || path.typeName == "decimal" || path.typeName == "java.lang.Double") {
				ret.type = "ng-pattern";
				ret.value = /^[-+]?\d{0,308}(\.\d{1,309})?%?$/;
				ret.key = "double";
			} else if (path.typeName == "byte" || path.typeName == "java.lang.Byte") {
				ret.type = "sd-validate";
				ret.value = "byte";
				ret.key = "byte";
			} else if (path.typeName == "character" || path.typeName == "java.lang.Character") {
				ret.key = "char";
			} else if (path.typeName == "date") {
				ret.key = "date";
			} else if (path.typeName == "dateTime" || path.typeName == "java.util.Date" || path.typeName == "java.util.Calendar") {
				ret.key = "dateTime";
			} else if (path.typeName == "time") {
				ret.key = "time";
			} else if (path.typeName == "duration") {
				ret.type = "sd-validate";
				ret.value = "duration";
				ret.key = "duration";
			}

			return ret;
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.isNumber = function(path) {
			if (path.typeName == "integer" || path.typeName == "int" || path.typeName == "java.lang.Integer" || 
					path.typeName == "short" || path.typeName == "java.lang.Short" ||
					path.typeName == "long" || path.typeName == "java.lang.Long" ||
					path.typeName == "float" || path.typeName == "java.lang.Float" ||
					path.typeName == "double" || path.typeName == "decimal" || path.typeName == "java.lang.Double") {
				return true;
			} else {
				return false;
			}
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.getMaxLength = function(path) {
			if (path.typeName == "integer" || path.typeName == "int" ||path.typeName == "java.lang.Integer") {
				return 11;
			} else if (path.typeName == "short" || path.typeName == "java.lang.Short") {
				return 6;
			} else if (path.typeName == "long" || path.typeName == "java.lang.Long") {
				return 20;
			} else if (path.typeName == "float" || path.typeName == "java.lang.Float") {
				return 620;
			} else if (path.typeName == "double" || path.typeName == "decimal" || path.typeName == "java.lang.Double") {
				return 620;
			} else if (path.typeName == "byte" || path.typeName == "java.lang.Byte") {
				return 4;
			} else if (path.typeName == "character" || path.typeName == "java.lang.Character") {
				return 1;
			} else if (path.typeName == "date" || path.typeName == "java.util.Date" || path.typeName == "dateTime" || path.typeName == "java.util.Calendar") {
				return 10;
			} else if (path.typeName == "time") {
				return 8;
			} else if (path.typeName == "duration") {
				return 41;
			}
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.addCustomDirective = function(path, elem) {
			var valInfo = {};
			if (path.typeName == "date" || path.typeName == "java.util.Date" || path.typeName == "dateTime"
					|| path.typeName == "java.util.Calendar" || path.typeName == "time") {
				elem.attributes["sd-date"] = null;

				valInfo.type = "sd-date";
				valInfo.value = "date";
				valInfo.key = "date";
			}
			
			return valInfo;
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.getCustomFilter = function(path) {
			if (path.typeName == "date") {
				return "sdFilterDate";
			} else if (path.typeName == "dateTime" || path.typeName == "java.util.Date" || path.typeName == "java.util.Calendar") {
				return "sdFilterDateTime";
			} else if (path.typeName == "time") {
				return "sdFilterTime";
			}
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.generateStruct = function(parent, path) {
			var elemMain = htmlElement.create("div", {parent: parent, attributes: {class: 'panel-struct'}});

			// Header
			htmlElement.create("div", {parent: elemMain, value: this.getI18NLabel(path), attributes: {class: 'panel-header'}});

			// Contents
			var elemContainer = htmlElement.create("div", {parent: elemMain, attributes: {class: 'panel-nested-container'}});

			this.generateChildren(elemContainer, path.children);

			return elemMain;
		};

		/*
		 * 
		 */
		CodeGenerator.prototype.generateDocument = function(parent, path) {
			var binding = this.convertFullIdToBinding(path);
			
			var elemWrapper = htmlElement.create("div", {parent: parent});
			
			var docLink = htmlElement.create("a", {parent: elemWrapper, attributes: {href: "", style: "text-decoration:none;"}});
			docLink.attributes["ng-click"] = "openDocument('" + path.fullXPath + "','" + this.getI18NLabel(path) + "', " + this.isReadonly(path) + ")";
			docLink.attributes["title"] = "{{" + binding + ".docName}}";
			docLink.attributes["ng-right-click"] = "showDocumentMenu('" + path.fullXPath+ "')";
			
			if(!this.isReadonly(path)){
				var docMenu = htmlElement.create("div", {parent: elemWrapper});
				docMenu.attributes["class"] = "document-menu-popup";
				docMenu.attributes["ng-show"] = binding + ".showDocMenu";
				docMenu.attributes["ng-click"] = "deleteDocument('" + path.fullXPath+ "')";	
				var menuTable = htmlElement.create("table", {parent: docMenu, attributes: {cellpadding: 2, cellspacing: 0}});
				var menuTableTr = htmlElement.create("tr", {parent: htmlElement.create("tbody", {parent: menuTable})});
				var menuTableTd1 = htmlElement.create("td", {parent: menuTableTr, attributes: {class: "panel-primitive-container-cell"}});
				htmlElement.create("img", {parent: menuTableTd1, attributes: {
					"src": preferences.pluginsUrl + "/views-common/images/icons/page_white_delete.png", class: "panel-image"}});
				var menuTableTd2 = htmlElement.create("td", {parent: menuTableTr, attributes: {class: "panel-primitive-container-cell"}});
				htmlElement.create("span", {parent: menuTableTd2, value:"Delete", attributes: {class: "panel-label"}});
			}
			
			elemWrapper.attributes["ng-mouseleave"] = "hideAllDocumentMenus()";
			
			htmlElement.create("img", {parent: docLink, 
				attributes: {"ng-src": "{{" + binding + ".docIcon}}", class: "panel-image"}});
			
			docLink.attributes["ng-disabled"] = "isDocumentLinkDisabled('" + path.fullXPath + "', " + this.isReadonly(path) + ")";
			docLink.attributes["ng-class"] = "getDocumentLinkClass('" + path.fullXPath + "', " + this.isReadonly(path) + ")";

			return elemWrapper;
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.generateChildren = function(parent, children) {
			var elemMainPrimTr;
			var elemPrimTBody;
			var renderedPrimitivesCount = 0;
			var createPrimitiveContainer = true;
			for (var i = 0; i < children.length; i++) {
				if (children[i].isPrimitive && !children[i].isList) {
					var primitivesCount = this.countContiguousPrimitives(children, i);

					if (createPrimitiveContainer) {
						// Primitives Container
						var elemMainPrimTbl = htmlElement.create("table", {parent: parent, attributes: {cellpadding: 0, cellspacing: 0, class: "panel-primitive-container"}});
						elemMainPrimTr = htmlElement.create("tr", {parent: htmlElement.create("tbody", {parent: elemMainPrimTbl})});

						createPrimitiveContainer = false;
					}

					if (renderedPrimitivesCount >= primitivesCount / preferences.layoutColumns) {
						renderedPrimitivesCount = 0;
					}

					if (renderedPrimitivesCount == 0) {
						var elemMainPrimTd = htmlElement.create("td", {parent: elemMainPrimTr, attributes: {class: "panel-primitive-container-cell"}});
						var elemPrimTbl = htmlElement.create("table", {parent: elemMainPrimTd, attributes: {cellpadding: 0, cellspacing: 0}});
						elemPrimTBody = htmlElement.create("tbody", {parent: elemPrimTbl});
					}
					renderedPrimitivesCount++;

					var elemPrimTr = htmlElement.create("tr", {parent: elemPrimTBody, attributes: {class: "panel-primitive-row"}});
					
					var elemPrimitive = this.generatePath(null, children[i]);

					// Label
					var elemPrimLabelTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-label-column"}});
					elemPrimLabelTd.children.push(elemPrimitive.children[0]);

					// Prefix
					var elemPrimPrefixTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-prefix-column"}});
					this.addPrefix(elemPrimPrefixTd, children[i]);

					// Input
					var elemPrimInputTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-input-column"}});
					elemPrimInputTd.children.push(elemPrimitive.children[1]);

					// Suffix
					var elemPrimSuffixTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-suffix-column"}});
					this.addSuffix(elemPrimSuffixTd, children[i]);
					
					var elemValidationImgTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-validation-image-column"}});
					if (elemPrimitive.children[2] != undefined) {
						elemValidationImgTd.children.push(elemPrimitive.children[2]);
					}
				} else {
					renderedPrimitivesCount = 0;
					createPrimitiveContainer = true;
					this.generatePath(parent, children[i]);
				}
			}
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.addPrefix = function(parent, path) {
			var prefix = path.properties["InputPreferences_prefix"];
			var prefixKey = path.properties["InputPreferences_prefixKey"];

			var value;
			if (prefixKey != null && prefixKey != "") {
				value = this.getI18NLabel(prefixKey);
			} else {
				value = prefix;
			}
			
			if (value) {
				htmlElement.create("label", {parent: parent, value: value, attributes: {class: "panel-output"}});
			}
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.addSuffix = function(parent, path) {
			var suffix = path.properties["InputPreferences_suffix"];
			var suffixKey = path.properties["InputPreferences_suffixKey"];

			var value;
			if (suffixKey != null && suffixKey != "") {
				value = this.getI18NLabel(suffixKey);
			} else {
				value = suffix;
			}
			
			if (value) {
				htmlElement.create("label", {parent: parent, value: value, attributes: {class: "panel-output"}});
			}
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.getLabel = function(path) {
			var label = path.properties["InputPreferences_label"];
			var labelKey = path.properties["InputPreferences_labelKey"];

			var value;
			if (labelKey != null && labelKey != "") {
				value = this.getI18NLabel(labelKey);
			} else {
				value = label;
			}

			if(value == undefined || value == null || value == "") {
				value = this.getI18NLabel(path);
			}
			return value;
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.countContiguousPrimitives = function(children, refIndex) {
			var count = 0;

			for(var i = refIndex; i >= 0; i--) {
				if (children[i].isPrimitive && !children[i].isList) {
					count++;
				} else {
					break;
				}
			}

			for(var i = refIndex + 1; i < children.length; i++) {
				if (children[i].isPrimitive && !children[i].isList) {
					count++;
				} else {
					break;
				}
			}
			return count;
		}

		/*
		 * 
		 */
		CodeGenerator.prototype.convertFullIdToBinding = function(path) {
			var xPath = path.fullXPath;
			if(this.ignoreXPath && xPath.indexOf(this.ignoreXPath) == 0) {
				xPath = xPath.substring(this.ignoreXPath.length);
			}

			var parts = xPath.substring(1).split("/");
			
			var binding = this.bindingPrefix ? this.bindingPrefix : "";
			var currentBindingData = this.bindingData;
			for (var i in parts) {
				if (binding == "") {
					binding = parts[i];
				} else {
					if (preferences.ngModelSepAsDot) {
						binding += "." + parts[i];
					} else {
						binding += "['" + parts[i] + "']";
					}
				}

				if (currentBindingData[parts[i]] == undefined) {
					if (i < parts.length - 1) {
						currentBindingData[parts[i]] = {};
					} else {
						if (path.isList) {
							currentBindingData[parts[i]] = [];
						} else if (path.isPrimitive) {
							currentBindingData[parts[i]] = "";
						}
					}
				}
				currentBindingData = currentBindingData[parts[i]];
			}
			
			return binding;
		};

		/*
		 * val : path object or string
		 */
		CodeGenerator.prototype.getI18NLabel = function(val, defaultValue) {
			var label;

			if (this.i18nLabelProvider && this.i18nLabelProvider.getLabel) {
				label = this.i18nLabelProvider.getLabel(val, defaultValue);
			}

			if ((label == undefined || label == null || label == "") && ("string" != typeof (val))) {
				if (val.name != undefined && val.name != null && val.name != "" && val.name != val.id) {
					label = val.name;
				} else {
					label = this.convertToLabel(val.id);
				}
			}

			if ((label == null || label == "") && defaultValue != undefined) {
				label = defaultValue;
			}
			return label;
		};

		/*
		 * 
		 */
		CodeGenerator.prototype.getI18NEnumerationLabels = function(path) {
			var labels;

			if (this.i18nLabelProvider && this.i18nLabelProvider.getEnumerationLabels) {
				labels = this.i18nLabelProvider.getEnumerationLabels(path);
			}
			
			if (labels == undefined) {
				labels = {};
				for(var i in path.enumValues) {
					labels["" + path.enumValues[i]] = path.enumValues[i];	
				}
			}

			return labels;
		};

		/*
		 * 
		 */
		CodeGenerator.prototype.convertToLabel = function(key) {
			var label = "";
			for (var i = 0; i < key.length; i++)
		      {
		         if (i == 0)
		         {
		            label += key.charAt(i).toUpperCase();
		         }
		         else if (key.charAt(i) != '.') // Ignore dots in the key 
		         {
		        	label += this.isUpperCase(key.charAt(i)) ? " " : "";
		        	label += key.charAt(i);
		         }
		      }
			
			return label;
		};

		/*
		 * 
		 */
		CodeGenerator.prototype.isUpperCase = function(char) {
			return UPPER_CASE_CHAR_REG_EX.test(char);
		}
	};
});