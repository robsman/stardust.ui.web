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
			var root = htmlElement.create("div");;
			for(var i in paths) {
				generatePath(root, paths[i]);
			}
			
			var html = root.toHtml();
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
				// TODO: Support List at later stage
				//return generateList(parent, path);
			} else if (path.isEnum) {
				return generateEnum(parent, path);
			} else if (path.isPrimitive) {
				return generatePrimitive(parent, path);
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
										{parent: htmlElement.create("table", {parent: elemToolbar})})});
			
			var elemAddButton = htmlElement.create("input", {parent: htmlElement.create("td", {parent: elemToolbarTr}), 
													attributes: {type: "button", value: "Add"}});
			var elemRemoveButton = htmlElement.create("input", {parent: htmlElement.create("td", {parent: elemToolbarTr}), 
													attributes: {type: "button", value: "Remove"}});
			
			// Table
			var elemTbl = htmlElement.create("table", {parent: elemMain});
			var elemTHead = htmlElement.create("thead", {parent: elemTbl});
			var elemTBody = htmlElement.create("tbody", {parent: elemTbl});

			var elemTHeadTr = htmlElement.create("tr", {parent: elemTHead});

			if (path.isPrimitive) { // Primitive List
				var elemTd = htmlElement.create("th", {parent: elemTHeadTr, value: getI18NLabel(path)});
				//generatePrimitive(elemTd, path);
			} else { // Structured List
				for (var i in path.children) {
					var child = path.children[i];
					var elemTd = htmlElement.create("th", {parent: elemTHeadTr, value: getI18NLabel(child)});

					/*
					// In Lists Generate only Primitives
					if (child.isPrimitive) {
						if (child.isEnum) {
							generateEnum(elemTd, child, true);
						} else {
							generatePrimitive(elemTd, child, true);
						}
					} else {
						// TODO: Structures in Lists
						var elemLink = htmlElement.create("a", {parent: elemTd, value: getI18NLabel(child)});
					}
					*/
				}
			}
			return elemMain;
		}

		/*
		 * 
		 */
		function generateEnum(parent, path, noLabel) {
			var elemMain = htmlElement.create("div", {parent: parent});

			if (noLabel == undefined || !noLabel) {
				var elemLabel = htmlElement.create("label", {parent: elemMain, value: getI18NLabel(path)});
			}

			var elem = null;
			if (path.readonly) {
				elem = htmlElement.create("label", {parent: elemMain});
			} else {
				elem = htmlElement.create("select", {parent: elemMain});
				for(var i in path.enumValues) {
					var val = path.enumValues[i];
					var elemOpt = htmlElement.create("option", {parent: elem, value: val, attributes: {value: val}});
				}
			}
			elem.attributes['ng-model'] = convertFullIdToBinding(path.fullXPath);
			
			return elemMain;
		}

		/*
		 * 
		 */
		function generatePrimitive(parent, path, noLabel) {
			var elemMain = htmlElement.create("div", {parent: parent});
			
			if (noLabel == undefined || !noLabel) {
				var elemLabel = htmlElement.create("label", {parent: elemMain, value: getI18NLabel(path)});
			}

			var elem;
			if (path.readonly) {
				elem = htmlElement.create("label", {parent: elemMain});
				elem.value = "{{" + convertFullIdToBinding(path.fullXPath) + "}}";
			} else {
				elem = htmlElement.create("input", {parent: elemMain});
				elem.attributes['ng-model'] = convertFullIdToBinding(path.fullXPath);

				if ("string" === path.typeName) {
					elem.attributes['type'] = "text";
				} else if ("integer" === path.typeName) {
					elem.attributes['type'] = "number";
				} else if ("float" === path.typeName) {
					elem.attributes['type'] = "text";
				} else if ("long" === path.typeName) {
					elem.attributes['type'] = "text";
				} else if ("boolean" === path.typeName) {
					elem.attributes['type'] = "checkbox";
				}
			}
			
			return elemMain;
		}

		/*
		 * 
		 */
		function generateStruct(parent, path) {
			var elemMain = htmlElement.create("div", {parent: parent});

			// Header
			htmlElement.create("div", {parent: elemMain, value: getI18NLabel(path)});
			
			// Contents
			var elemPrimTbl = htmlElement.create("table", {parent: elemMain});
			var elemPrimTBody = htmlElement.create("tbody", {parent: elemPrimTbl});
			
			var elemContents = htmlElement.create("div", {parent: elemMain});
			
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