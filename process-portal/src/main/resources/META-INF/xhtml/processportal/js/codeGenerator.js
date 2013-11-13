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
			prefs = {};
		}
		var preferences = prefs;

		/*
		 * 
		 */
		CodeGenerator.prototype.generate = function(paths) {
			var elemMain = htmlElement.create("div");

			// Contents
			var elemPrimTbl = htmlElement.create("table", {parent: elemMain, attributes: {cellpadding: 0, cellspacing: 0}});
			var elemPrimTBody = htmlElement.create("tbody", {parent: elemPrimTbl});
			
			var elemContents = htmlElement.create("div", {parent: elemMain});
			
			for(var i in paths) {
				if (paths[i].isPrimitive && !paths[i].isList) {
					var elemPrimTr = htmlElement.create("tr", {parent: elemPrimTBody});
					
					var elemPrimitive = generatePath(null, paths[i]);
					for (var j in elemPrimitive.children) {
						var elemPrimTd = htmlElement.create("td", {parent: elemPrimTr});
						elemPrimTd.children.push(elemPrimitive.children[j]);
					}
				} else {
					generatePath(elemContents, paths[i]);	
				}
			}
			
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
			var elemMain = htmlElement.create("div", {parent: parent});

			// Header
			htmlElement.create("div", {parent: elemMain, value: getI18NLabel(path)});

			// Toolbar
			var elemToolbar = htmlElement.create("div", {parent: elemMain});
			var elemToolbarTr = htmlElement.create("tr", {parent: htmlElement.create("tbody", 
					{parent: htmlElement.create("table", {parent: elemToolbar, attributes: {cellpadding: 0, cellspacing: 0}})})});
			
			var elemAddButton = htmlElement.create("a", {parent: htmlElement.create("td", {parent: elemToolbarTr})});
			elemAddButton.attributes["href"] = "javascript: alert('Add');";
			htmlElement.create("img", {parent: elemAddButton, 
				attributes: {src: "../../plugins/stardust-ui-form-jsf/public/css/images/add.png"}});
			
			var elemRemoveButton = htmlElement.create("a", {parent: htmlElement.create("td", {parent: elemToolbarTr})});
			elemRemoveButton.attributes["href"] = "javascript: alert('Remove');";
			htmlElement.create("img", {parent: elemRemoveButton, 
				attributes: {src: "../../plugins/stardust-ui-form-jsf/public/css/images/delete.png"}});
			
			// Table
			var elemTbl = htmlElement.create("table", {parent: elemMain, attributes: {cellpadding: 0, cellspacing: 0}});
			var elemTHead = htmlElement.create("thead", {parent: elemTbl});
			var elemTBody = htmlElement.create("tbody", {parent: elemTbl});

			var elemTHeadTr = htmlElement.create("tr", {parent: elemTHead});
			var elemTBodyTr = htmlElement.create("tr", {parent: elemTBody});
			
			var loopVar = "Obj";
			elemTBodyTr.attributes["ng-repeat"] = loopVar + " in " + convertFullIdToBinding(path.fullXPath);

			if (path.isPrimitive) { // Primitive List
				htmlElement.create("th", {parent: elemTHeadTr, value: getI18NLabel(path)});
				var elemTd = htmlElement.create("td", {parent: elemTBodyTr});
				generatePriEnum(elemTd, path, {noLabel: true, ngModel: loopVar});
			} else { // Structured List
				for (var i in path.children) {
					var child = path.children[i];
					htmlElement.create("th", {parent: elemTHeadTr, value: getI18NLabel(child)});
					var elemTd = htmlElement.create("td", {parent: elemTBodyTr});

					if (child.isPrimitive) {
						generatePriEnum(elemTd, child, {noLabel: true, ngModel: loopVar + "['" + child.id + "']"});
					} else {
						var elemLink = htmlElement.create("a", {parent: elemTd, value: getI18NLabel(child)});
						elemLink.attributes["disabled"] = "true"; // TODO: Structures in Lists
					}
				}
			}
			return elemMain;
		}

		/*
		 * options: noLabel, ngModel
		 */
		function generatePriEnum(parent, path, options) {
			if (options == undefined) {
				options = {};
			}

			var elemMain = htmlElement.create("div", {parent: parent});
			
			if (options.noLabel == undefined || !options.noLabel) {
				var elemLabel = htmlElement.create("label", {parent: elemMain, value: getI18NLabel(path)});
			}

			var elem;
			if (path.readonly) {
				elem = htmlElement.create("label", {parent: elemMain});
				elem.value = "{{" + convertFullIdToBinding(path.fullXPath) + "}}";
			} else {
				if (path.isEnum) {
					elem = htmlElement.create("select", {parent: elemMain});
					for(var i in path.enumValues) {
						var val = path.enumValues[i];
						var elemOpt = htmlElement.create("option", {parent: elem, value: val, attributes: {value: val}});
					}
				} else {
					elem = htmlElement.create("input", {parent: elemMain});
					if ("boolean" === path.typeName || "java.lang.Boolean" === path.typeName) {
						elem.attributes['type'] = "checkbox";
					}
				}

				elem.attributes['ng-model'] = options.ngModel == undefined ? convertFullIdToBinding(path.fullXPath) : options.ngModel;
			}
			
			return elemMain;
		}

		/*
		 * 
		 */
		function generateStruct(parent, path) {
			var elemMain = htmlElement.create("div", {parent: parent, attributes: {class: 'panel-struct'}});

			// Header
			htmlElement.create("div", {parent: elemMain, value: getI18NLabel(path), attributes: {class: 'panel-struct-header'}});
			
			var elemContainer = htmlElement.create("div", {parent: elemMain, attributes: {class: 'panel-struct-container'}});
			
			// Contents
			var elemPrimTbl = htmlElement.create("table", {parent: elemContainer, attributes: {cellpadding: 0, cellspacing: 0}});
			var elemPrimTBody = htmlElement.create("tbody", {parent: elemPrimTbl});
			
			var elemContents = htmlElement.create("div", {parent: elemContainer});
			
			for (var i in path.children) {
				if (path.children[i].isPrimitive && !path.children[i].isList) {
					var elemPrimTr = htmlElement.create("tr", {parent: elemPrimTBody});
					
					var elemPrimitive = generatePath(null, path.children[i]);
					for (var j in elemPrimitive.children) {
						var elemPrimTd = htmlElement.create("td", {parent: elemPrimTr});
						elemPrimTd.children.push(elemPrimitive.children[j]);
					}
				} else {
					generatePath(elemContents, path.children[i]);	
				}
			}

			return elemMain;
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
		}
		
		/*
		 * 
		 */
		function getI18NLabel(path) {
			return path.id; // TODO
		}
	};
});