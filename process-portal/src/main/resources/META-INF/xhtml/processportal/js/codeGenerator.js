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
		if (!prefs) {
			prefs = {layoutColumns: 3, tableColumns: 0}; // Defaults
		}
		var preferences = prefs;

		/*
		 * 
		 */
		CodeGenerator.prototype.generate = function(paths) {
			var elemMain = htmlElement.create("div", {attributes: {class: "panel-main"}});

			generateChildren(elemMain, paths);

			var html = elemMain.toHtml();
			console.log("HTML START");
			console.log("HTML:\n" + html);
			console.log("HTML END");
			
			return html;
		};

		/*
		 * 
		 */
		function generatePath(parent, path) {
			console.log("To Generate: " + path.fullXPath);

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
			elemAddButton.attributes["href"] = "javascript: alert('Add');";
			htmlElement.create("img", {parent: elemAddButton, 
				attributes: {src: "../../plugins/stardust-ui-form-jsf/public/css/images/add.png"}});
			
			var elemRemoveButton = htmlElement.create("a", {parent: htmlElement.create("td", 
					{parent: elemToolbarTr, attributes: {class: "panel-list-toolbar-tbl-cell"}})});
			elemRemoveButton.attributes["href"] = "javascript: alert('Remove');";
			htmlElement.create("img", {parent: elemRemoveButton, 
				attributes: {src: "../../plugins/stardust-ui-form-jsf/public/css/images/delete.png"}});

			// Table
			var elemTbl = htmlElement.create("table", {parent: elemMain, 
				attributes: {class: "panel-list-tbl", cellpadding: 0, cellspacing: 0}});
			var elemTHead = htmlElement.create("thead", {parent: elemTbl});
			var elemTBody = htmlElement.create("tbody", {parent: elemTbl});

			var elemTHeadTr = htmlElement.create("tr", {parent: elemTHead});
			var elemTBodyTr = htmlElement.create("tr", {parent: elemTBody, attributes: {class: "panel-list-tbl-row"}});
			
			var loopVar = "Obj";
			elemTBodyTr.attributes["ng-repeat"] = loopVar + " in " + convertFullIdToBinding(path.fullXPath);

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
						var elemLink = htmlElement.create("a", {parent: elemTd, value: getI18NLabel(child)});
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
				var elemLabel = htmlElement.create("label", {parent: elemMain, value: getI18NLabel(path), attributes: {class: "panel-label"}});
			}

			var elem;
			if (path.readonly) {
				elem = htmlElement.create("label", {parent: elemMain, attributes: {class: "panel-output"}});
				elem.value = "{{" + convertFullIdToBinding(path.fullXPath) + "}}";
			} else {
				if (path.isEnum) {
					elem = htmlElement.create("select", {parent: elemMain});
					elem.attributes['class'] = "panel-select";
					for(var i in path.enumValues) {
						var val = path.enumValues[i];
						var elemOpt = htmlElement.create("option", {parent: elem, value: val, attributes: {value: val}});
					}
				} else {
					elem = htmlElement.create("input", {parent: elemMain});
					elem.attributes['class'] = "panel-input";
					if ("boolean" === path.typeName || "java.lang.Boolean" === path.typeName) {
						elem.attributes['type'] = "checkbox";
					}
				}

				elem.attributes['ng-model'] = options.ngModel == undefined ? convertFullIdToBinding(path.fullXPath) : options.ngModel;
			}
			
			return elemMain;
		};

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
			// Primitives Container
			var elemMainPrimTbl = htmlElement.create("table", {parent: parent, attributes: {cellpadding: 0, cellspacing: 0, class: "panel-primitive-container"}});
			var elemMainPrimTr = htmlElement.create("tr", {parent: htmlElement.create("tbody", {parent: elemMainPrimTbl})});

			// Other Elements
			var elemContents = htmlElement.create("div", {parent: parent});

			var elemPrimTBody;
			var renderedCount = 0;
			var primitivesCount = countContiguousPrimitives(children);
			for (var i in children) {
				if (children[i].isPrimitive && !children[i].isList) {
					if (renderedCount >= primitivesCount / preferences.layoutColumns) {
						renderedCount = 0;
					}

					if (renderedCount == 0) {
						var elemMainPrimTd = htmlElement.create("td", {parent: elemMainPrimTr, attributes: {class: "panel-primitive-container-cell"}});
						var elemPrimTbl = htmlElement.create("table", {parent: elemMainPrimTd, attributes: {cellpadding: 0, cellspacing: 0}});
						elemPrimTBody = htmlElement.create("tbody", {parent: elemPrimTbl});
					}
					renderedCount++;

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
					generatePath(elemContents, children[i]);	
				}
			}

		}

		/*
		 * 
		 */
		function countContiguousPrimitives(children) {
			var count = 0;
			for(i in children) {
				if (children[i].isPrimitive && !children[i].isList) {
					count++;
				}
			}
			return count;
		}

		/*
		 * 
		 */
		function convertFullIdToBinding(fullId) {
			var parts = fullId.substring(1).split("/");
			
			var binding = "";
			for (var i in parts) {
				if (i == 0) {
					binding += parts[i];
				} else {
					binding += "['" + parts[i] + "']";					
				}
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