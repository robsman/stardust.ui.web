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
		if (prefs == undefined) {
			prefs = {layoutColumns: 3, tableColumns: 0}; // Defaults
		}
		var preferences = prefs;

		var bindingPrefix;
		var bindingData;
		var i18nCallbackFunc;

		/*
		 * 
		 */
		CodeGenerator.prototype.generate = function(paths, prefix, i18nCallback) {
			bindingPrefix = prefix;
			i18nCallbackFunc = i18nCallback;
			bindingData = {};

			var elemForm = htmlElement.create("form", {attributes: {name: "form"}});
			var elemMain = htmlElement.create("div", {parent: elemForm, attributes: {class: "panel-main"}});

			// Validation
			var showExpr = "form.$invalid";
			var elemValidationBar = htmlElement.create("div", {parent: elemMain, 
				attributes: {class: "panel-validation-summary-bar", "ng-show": showExpr}});
			htmlElement.create("span", {parent: elemValidationBar, attributes: {class: "panel-validation-summary-bar-img"}});
			htmlElement.create("span", {parent: elemValidationBar, value: "Form contains error(s).", 
				attributes: {class: "panel-validation-summary-bar-text"}});

			// Generate
			generateChildren(elemMain, paths);

			var html = elemForm.toHtml();

			bindingPrefix = undefined;
			i18nCallbackFunc = undefined;

			return {html: html, binding: bindingData};
		};

		/*
		 * 
		 */
		function generatePath(parent, path) {
			if (path.isList) {
				return generateList(parent, path);
			} else if (path.isPrimitive) {
				return generatePriEnum(parent, path);
			} else {
				return generateStruct(parent, path);
			}
		};

		/*
		 * 
		 */
		function generateList(parent, path) {
			var elemMain = htmlElement.create("div", {parent: parent, attributes: {class: "panel-list"}});

			// Header
			htmlElement.create("div", {parent: elemMain, value: getI18NLabel(path), attributes: {class: "panel-header"}});

			// Toolbar
			var elemToolbar = htmlElement.create("div", {parent: elemMain, attributes: {class: "panel-list-toolbar"}});
			var elemToolbarTrTbl = htmlElement.create("table", 
					{parent: elemToolbar, attributes: {class: "panel-list-toolbar-tbl", cellpadding: 0, cellspacing: 0}});
			var elemToolbarTr = htmlElement.create("tr", {parent: htmlElement.create("tbody", 
					{parent: elemToolbarTrTbl}), attributes: {class: "panel-list-toolbar-tbl-row"}});
			
			var elemAddButton = htmlElement.create("a", {parent: htmlElement.create("td", 
					{parent: elemToolbarTr, attributes: {class: "panel-list-toolbar-tbl-cell"}})});
			elemAddButton.attributes["href"] = "";
			if (isReadonly(path)) {
				elemAddButton.attributes["disabled"] = true;
			} else {
				elemAddButton.attributes["ng-click"] = "addToList('" + path.fullXPath + "')";
			}
			htmlElement.create("img", {parent: elemAddButton, 
				attributes: {src: "../../plugins/stardust-ui-form-jsf/public/css/images/add.png"}});
			
			var elemRemoveButton = htmlElement.create("a", {parent: htmlElement.create("td", 
					{parent: elemToolbarTr, attributes: {class: "panel-list-toolbar-tbl-cell"}})});
			elemRemoveButton.attributes["href"] = "";
			if (isReadonly(path)) {
				elemRemoveButton.attributes["disabled"] = true;
			} else {
				elemRemoveButton.attributes["ng-click"] = "removeFromList('" + path.fullXPath + "')";
			}
			htmlElement.create("img", {parent: elemRemoveButton, 
				attributes: {src: "../../plugins/stardust-ui-form-jsf/public/css/images/delete.png"}});

			// Table
			var elemTbl = htmlElement.create("table", {parent: elemMain, 
				attributes: {class: "panel-list-tbl", cellpadding: 0, cellspacing: 0}});
			var elemTHead = htmlElement.create("thead", {parent: elemTbl});
			var elemTBody = htmlElement.create("tbody", {parent: elemTbl});

			var loopVar = "Obj";

			var elemTHeadTr = htmlElement.create("tr", {parent: elemTHead});
			var elemTBodyTr = htmlElement.create("tr", {parent: elemTBody});
			
			elemTBodyTr.attributes["ng-class"] = "{'panel-list-tbl-row-sel': " 
				+ loopVar + ".$$selected, 'panel-list-tbl-row': !" + loopVar + ".$$selected}";
			elemTBodyTr.attributes["ng-repeat"] = loopVar + " in " + convertFullIdToBinding(path);
			if (!isReadonly(path)) {
				elemTBodyTr.attributes["ng-click"] = "selectListItem($event, " + loopVar + ")";
			}

			if (path.isPrimitive) { // List of Primitives
				htmlElement.create("th", {parent: elemTHeadTr, value: getI18NLabel(path), attributes: {class: "panel-list-tbl-header"}});
				var elemTd = htmlElement.create("td", {parent: elemTBodyTr, attributes: {class: "panel-list-tbl-cell"}});
				generatePriEnum(elemTd, path, {noLabel: true, ngModel: loopVar});
			} else { // List of Structures
				for (var i in path.children) {
					var child = path.children[i];
					htmlElement.create("th", {parent: elemTHeadTr, value: getI18NLabel(child), attributes: {class: "panel-list-tbl-header"}});
					var elemTd = htmlElement.create("td", {parent: elemTBodyTr, attributes: {class: "panel-list-tbl-cell"}});

					if (child.isPrimitive) {
						var elemPrimitive = generatePriEnum(null, child, {noLabel: true, ngModel: loopVar + "['" + child.id + "']"});
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
					} else {
						var elemLink = htmlElement.create("a", {parent: elemTd, value: isReadonly(path) ? "View" : "Edit"});
						elemLink.attributes["disabled"] = "true"; // TODO: Structures in Lists
					}
				}
			}
			return elemMain;
		};

		/*
		 * options: noLabel, ngModel
		 */
		function generatePriEnum(parent, path, options) {
			if (options == undefined) {
				options = {};
			}

			var elemMain = htmlElement.create("div", {parent: parent});
			
			if (options.noLabel == undefined || !options.noLabel) {
				var elemLabel = htmlElement.create("label", 
						{parent: elemMain, value: getI18NLabel(path) + ":", attributes: {class: "panel-label"}});
			}

			var elem;
			if (path.typeName == "document") {
				elem = generateDocument(elemMain, path);
			} else if (isReadonly(path)) {
				if (path.properties["BooleanInputPreferences_readonlyOutputType"] != undefined &&
						path.properties["BooleanInputPreferences_readonlyOutputType"] == "CHECKBOX") {
					elem = htmlElement.create("input", {parent: elemMain, 
								attributes: {type: "checkbox", class: "panel-checkbox", disabled: true}});
				} else {
					elem = htmlElement.create("label", {parent: elemMain, attributes: {class: "panel-output"}});
				}

				elem.value = "{{" + (options.ngModel == undefined ? convertFullIdToBinding(path) : options.ngModel) + "}}";
			} else {
				if (path.isEnum) {
					elem = htmlElement.create("select", {parent: elemMain, attributes: {'ng-model-onblur': null}});
					//elem.attributes["sd-post-data"] = null;
					elem.attributes['class'] = "panel-select";
					for(var i in path.enumValues) {
						var val = path.enumValues[i];
						var elemOpt = htmlElement.create("option", {parent: elem, value: val, attributes: {value: val}});
					}
				} else {
					var elemWrapper = htmlElement.create("div", {parent: elemMain});
					var validations = [];

					if (path.properties["StringInputPreferences_stringInputType"] == "TEXTAREA") {
						elem = htmlElement.create("textarea", {parent: elemWrapper});
						if (path.properties["StringInputPreferences_textAreaRows"] != undefined) {
							elem.attributes["rows"] = path.properties["StringInputPreferences_textAreaRows"];
						}
						if (path.properties["StringInputPreferences_textAreaColumns"] != undefined) {
							elem.attributes["cols"] = path.properties["StringInputPreferences_textAreaColumns"];
						}
					} else {
						elem = htmlElement.create("input", {parent: elemWrapper});
					}

					elem.attributes["ng-model-onblur"] = null;
					//elem.attributes["sd-post-data"] = null;

					if (path.properties["InputPreferences_mandatory"] != undefined && 
							path.properties["InputPreferences_mandatory"] == "true") {
						validations.push({type: "ng-required", value: true, msg: "Required"});
					}

					if ("boolean" === path.typeName || "java.lang.Boolean" === path.typeName) {
						elem.attributes['type'] = "checkbox";
						elem.attributes['class'] = "panel-checkbox";
					} else {
						elem.attributes['type'] = "text";
						elem.attributes['class'] = "panel-input";
						var pattern = getValidationPattern(path);
						if (pattern) {
							validations.push({type: "ng-pattern", value: pattern, msg: "Not Valid"});
						}
						elem.attributes['maxlength'] = getMaxLength(path);

						var cDirective = getCustomDirective(path);
						if (cDirective != undefined) {
							elem.attributes[cDirective] = null;
						}
					}

					if (validations.length > 0) {
						var id = "id" + Math.floor((Math.random() * 100000) + 1);
						elem.attributes['id'] = id;
						elem.attributes['name'] = id;

						for (var i = 0; i < validations.length; i++) {
							elem.attributes[validations[i].type] = validations[i].value;
							var showExpr = "form." + id + ".$error." + validations[i].type.split("-")[1];
							htmlElement.create("div", {parent: elemWrapper, value: validations[i].msg, 
								attributes: {class: "panel-invalid-msg", "ng-show": showExpr}});
						}

						var showExpr = "form." + id + ".$invalid";
						htmlElement.create("span", {parent: elemMain, 
							attributes: {class: "panel-invalid-icon", "ng-show": showExpr}});
					}
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

				elem.attributes['ng-model'] = options.ngModel == undefined ? convertFullIdToBinding(path) : options.ngModel;
			}
			
			return elemMain;
		};

		/*
		 * 
		 */
		function isReadonly(path) {
			return path.properties["InputPreferences_readonly"] == "true" || path.readonly;
		}

		/*
		 * 
		 */
		function getValidationPattern(path) {
			if (path.typeName == "integer" || path.typeName == "int" ||path.typeName == "java.lang.Integer") {
				return /^(\+|-)?([\d]{0,9})$/;
			} else if (path.typeName == "short" || path.typeName == "java.lang.Short") {
				return /^(\+|-)?([\d]{0,9})$/;
			} else if (path.typeName == "long" || path.typeName == "java.lang.Long") {
				return /^(\+|-)?([\d]{0,18})$/;
			} else if (path.typeName == "float" || path.typeName == "java.lang.Float") {
				return /^[-+]?\d{0,308}(\.\d{1,309})?%?$/;
			} else if (path.typeName == "double" || path.typeName == "decimal" || path.typeName == "java.lang.Double") {
				return /^[-+]?\d{0,308}(\.\d{1,309})?%?$/;
			} else if (path.typeName == "byte" || path.typeName == "java.lang.Byte") {
				
			} else if (path.typeName == "character" || path.typeName == "java.lang.Character") {
				
			} else if (path.typeName == "date" || path.typeName == "java.util.Date") {
				
			} else if (path.typeName == "dateTime" || path.typeName == "java.util.Calendar") {
				
			} else if (path.typeName == "time") {
				
			} else if (path.typeName == "duration") {
				
			}
		}

		/*
		 * 
		 */
		function getMaxLength(path) {
			if (path.typeName == "integer" || path.typeName == "int" ||path.typeName == "java.lang.Integer") {
				return 10;
			} else if (path.typeName == "short" || path.typeName == "java.lang.Short") {
				return 5;
			} else if (path.typeName == "long" || path.typeName == "java.lang.Long") {
				return 19;
			} else if (path.typeName == "float" || path.typeName == "java.lang.Float") {
				return 620;
			} else if (path.typeName == "double" || path.typeName == "decimal" || path.typeName == "java.lang.Double") {
				return 620;
			} else if (path.typeName == "byte" || path.typeName == "java.lang.Byte") {
				return 2;
			} else if (path.typeName == "character" || path.typeName == "java.lang.Character") {
				return 1;
			} else if (path.typeName == "date" || path.typeName == "java.util.Date") {
				return 10;
			} else if (path.typeName == "dateTime" || path.typeName == "java.util.Calendar") {
				return 19;
			} else if (path.typeName == "time") {
				return 8;
			} else if (path.typeName == "duration") {
				return 41;
			}
		}

		/*
		 * 
		 */
		function getCustomDirective(path) {
			if (path.typeName == "date" || path.typeName == "java.util.Date" || path.typeName == "dateTime"
					|| path.typeName == "java.util.Calendar" || path.typeName == "time") {
				return "sd-date";
			}
		}

		/*
		 * 
		 */
		function generateStruct(parent, path) {
			var elemMain = htmlElement.create("div", {parent: parent, attributes: {class: 'panel-struct'}});

			// Header
			htmlElement.create("div", {parent: elemMain, value: getI18NLabel(path), attributes: {class: 'panel-header'}});

			// Contents
			var elemContainer = htmlElement.create("div", {parent: elemMain, attributes: {class: 'panel-nested-container'}});

			generateChildren(elemContainer, path.children);

			return elemMain;
		};

		/*
		 * 
		 */
		function generateDocument(parent, path) {
			var docLink = htmlElement.create("a", {parent: parent, attributes: {href: ""}});
			docLink.attributes["ng-click"] = "openDocument('" + path.fullXPath + "')";
			htmlElement.create("img", {parent: docLink, 
				attributes: {src: "../../plugins/views-common/images/icons/mime-types/document-text.png"}});
			return docLink;
		}

		/*
		 * 
		 */
		function generateChildren(parent, children) {
			var elemMainPrimTr;
			var elemPrimTBody;
			var renderedPrimitivesCount = 0;
			var createPrimitiveContainer = true;
			for (var i = 0; i < children.length; i++) {
				if (children[i].isPrimitive && !children[i].isList) {
					var primitivesCount = countContiguousPrimitives(children, i);

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
					
					var elemPrimitive = generatePath(null, children[i]);

					// Label
					var elemPrimLabelTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-label-column"}});
					elemPrimLabelTd.children.push(elemPrimitive.children[0]);

					// Prefix
					var elemPrimPrefixTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-prefix-column"}});
					addPrefix(elemPrimPrefixTd, children[i]);

					// Input
					var elemPrimInputTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-input-column"}});
					elemPrimInputTd.children.push(elemPrimitive.children[1]);

					// Suffix
					var elemPrimSuffixTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-suffix-column"}});
					addSuffix(elemPrimSuffixTd, children[i]);
					
					var elemValidationImgTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-validation-image-column"}});
					if (elemPrimitive.children[2] != undefined) {
						elemValidationImgTd.children.push(elemPrimitive.children[2]);
					}
				} else {
					renderedPrimitivesCount = 0;
					createPrimitiveContainer = true;
					generatePath(parent, children[i]);
				}
			}
		}

		/*
		 * 
		 */
		function addPrefix(parent, path) {
			var prefix = path.properties["InputPreferences_prefix"];
			var prefixKey = path.properties["InputPreferences_prefixKey"];

			var value;
			if (prefixKey != null && prefixKey != "") {
				value = getI18NLabel(prefixKey);
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
		function addSuffix(parent, path) {
			var suffix = path.properties["InputPreferences_suffix"];
			var suffixKey = path.properties["InputPreferences_suffixKey"];

			var value;
			if (suffixKey != null && suffixKey != "") {
				value = getI18NLabel(suffixKey);
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
		function countContiguousPrimitives(children, refIndex) {
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
		function convertFullIdToBinding(path) {
			var parts = path.fullXPath.substring(1).split("/");
			
			var binding = bindingPrefix ? bindingPrefix : "";
			var currentBindingData = bindingData;
			for (var i in parts) {
				binding += "['" + parts[i] + "']";

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
		function getI18NLabel(val) {
			var label;

			if (i18nCallbackFunc) {
				label = i18nCallbackFunc(val);
			}

			if (label == undefined || label == null || label == "") {
				label = convertToLabel(("string" == typeof (val))? val : val.id);
			}

			return label;
		};

		/*
		 * 
		 */
		function convertToLabel(key) {
			var label = "";
			for (var i = 0; i < key.length; i++)
		      {
		         if (i == 0)
		         {
		            label += key.charAt(i).toUpperCase();
		         }
		         else if (key.charAt(i) != '.') // Ignore dots in the key 
		         {
		        	label += isUpperCase(key.charAt(i)) ? " " : "";
		        	label += key.charAt(i);
		         }
		      }
			
			return label;
		};

		/*
		 * 
		 */
		function isUpperCase(char) {
			return char == char.toUpperCase();
		}
	};
});