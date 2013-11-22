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

		/*
		 * 
		 */
		CodeGenerator.prototype.generate = function(paths, prefix) {
			bindingPrefix = prefix;
			bindingData = {};

			var elemForm = htmlElement.create("form", {attributes: {name: "form"}});
			var elemMain = htmlElement.create("div", {parent: elemForm, attributes: {class: "panel-main"}});

			generateChildren(elemMain, paths);

			var html = elemForm.toHtml();

			bindingPrefix = undefined;

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
			elemAddButton.attributes["ng-click"] = "addToList('" + path.fullXPath + "')";
			htmlElement.create("img", {parent: elemAddButton, 
				attributes: {src: "../../plugins/stardust-ui-form-jsf/public/css/images/add.png"}});
			
			var elemRemoveButton = htmlElement.create("a", {parent: htmlElement.create("td", 
					{parent: elemToolbarTr, attributes: {class: "panel-list-toolbar-tbl-cell"}})});
			elemRemoveButton.attributes["href"] = "";
			elemRemoveButton.attributes["ng-click"] = "removeFromList('" + path.fullXPath + "')";
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
			elemTBodyTr.attributes["ng-click"] = "selectListItem($event, " + loopVar + ")";

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
						generatePriEnum(elemTd, child, {noLabel: true, ngModel: loopVar + "['" + child.id + "']"});
					} else {
						var elemLink = htmlElement.create("a", {parent: elemTd, value: "Edit"});
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
			if (path.readonly) {
				elem = htmlElement.create("label", {parent: elemMain, attributes: {class: "panel-output"}});
				elem.value = "{{" + convertFullIdToBinding(path) + "}}";
			} else {
				if (path.isEnum) {
					elem = htmlElement.create("select", {parent: elemMain, 
						attributes: {'ng-model-onblur': null, 'sd-post-data': null}});
					elem.attributes['class'] = "panel-select";
					for(var i in path.enumValues) {
						var val = path.enumValues[i];
						var elemOpt = htmlElement.create("option", {parent: elem, value: val, attributes: {value: val}});
					}
				} else {
					if (path.typeName == "document") {
						elem = htmlElement.create("label", {parent: elemMain, value: "TODO", attributes: {class: "panel-output"}});
					} else {
						var elemWrapper = htmlElement.create("div", {parent: elemMain});
						elem = htmlElement.create("input", {parent: elemWrapper, 
							attributes: {'ng-model-onblur': null, 'sd-post-data': null}});
						if ("boolean" === path.typeName || "java.lang.Boolean" === path.typeName) {
							elem.attributes['type'] = "checkbox";
							elem.attributes['class'] = "panel-checkbox";
						} else {
							elem.attributes['type'] = "text";
							elem.attributes['class'] = "panel-input";
							var pattern = getValidationPattern(path);
							if (pattern) {
								var id = "id" + Math.floor((Math.random() * 100000) + 1);
								var showExpr = "form." + id + ".$error.pattern";
								elem.attributes['id'] = id;
								elem.attributes['name'] = id;
								elem.attributes['ng-pattern'] = pattern;

								htmlElement.create("div", {parent: elemWrapper, value: "Not Valid", 
									attributes: {style: "color:red", "ng-show": showExpr}});
							}
							elem.attributes['maxlength'] = getMaxLength(path);

							var cDirective = getCustomDirective(path);
							if (cDirective != undefined) {
								elem.attributes[cDirective] = null;
							}
						}
					}
				}

				elem.attributes['ng-model'] = options.ngModel == undefined ? convertFullIdToBinding(path) : options.ngModel;
			}
			
			return elemMain;
		};

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

					// Input
					var elemPrimInputTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-input-column"}});
					elemPrimInputTd.children.push(elemPrimitive.children[1]);

					// Prefix
					var elemPrimSuffixTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-suffix-column"}});
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
			
			var binding = bindingPrefix ? (bindingPrefix + ".") : "";
			var currentBindingData = bindingData;
			for (var i in parts) {
				if (i == 0) {
					binding += parts[i];
				} else {
					binding += "['" + parts[i] + "']";
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
		 * 
		 */
		function getI18NLabel(path) {
			return path.id; // TODO
		};
	};
});