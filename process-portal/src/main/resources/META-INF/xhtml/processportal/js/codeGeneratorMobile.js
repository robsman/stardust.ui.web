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
 * @author Shrikant.Gangal
 */

define(["processportal/js/codeGenerator", "processportal/js/htmlElement", "bpm-modeler/js/m_utils"], function(codeGenerator, htmlElement, m_utils){
	return {
		create: function(prefs) {
			return new CodeGeneratorMobile(prefs);
		}
	};

	/*
	 * 
	 */
	function CodeGeneratorMobile(prefs) {
		var baseCodeGenerator = codeGenerator.create(prefs);
		m_utils.inheritFields(this, baseCodeGenerator);
		m_utils.inheritMethods(CodeGeneratorMobile.prototype, baseCodeGenerator);
		
		/**
		 * 
		 */
		CodeGeneratorMobile.prototype.generateValidationBar = function(elemMain) {
			// Do nothing for mobile clients
		};
		
		/**
		 * 
		 */
		CodeGeneratorMobile.prototype.generateNestedTypeDialogIFRAME = function(html) {
			// Do nothing for mobile clients
		};
		
		/*
		 * 
		 */
		CodeGeneratorMobile.prototype.generateStruct = function(parent, path) {
			var elemMain = htmlElement.create("div", {parent: parent, attributes: {class: 'ui-body ui-body-a ui-corner-all'}});

			// Header
			htmlElement.create("h3", {parent: elemMain, value: this.getI18NLabel(path)});

			// Contents
			//var elemContainer = htmlElement.create("div", {parent: elemMain, attributes: {class: 'panel-nested-container'}});

			this.generateChildren(elemMain, path.children);

			return elemMain;
		};		

		/*
		 * 
		 */
		CodeGeneratorMobile.prototype.addCustomDirective = function(path, elem) {
			return {};
		};
		
		/*
		 * 
		 */
		CodeGeneratorMobile.prototype.generateChildren = function(parent, children) {
			var elemMainPrimTr;
			var elemPrimTBody;
			var renderedPrimitivesCount = 0;
			var createPrimitiveContainer = true;
			for (var i = 0; i < children.length; i++) {
				if (children[i].isPrimitive && !children[i].isList) {
					var primitivesCount = this.countContiguousPrimitives(children, i);

					if (createPrimitiveContainer) {
						// Primitives Container
//						var elemMainPrimTbl = htmlElement.create("table", {parent: parent, attributes: {cellpadding: 0, cellspacing: 0, class: "panel-primitive-container"}});
//						elemMainPrimTr = htmlElement.create("tr", {parent: htmlElement.create("tbody", {parent: elemMainPrimTbl})});

						createPrimitiveContainer = false;
					}

					if (renderedPrimitivesCount >= primitivesCount / this.preferences.layoutColumns) {
						renderedPrimitivesCount = 0;
					}

//					if (renderedPrimitivesCount == 0) {
//						var elemMainPrimTd = htmlElement.create("td", {parent: elemMainPrimTr, attributes: {class: "panel-primitive-container-cell"}});
//						var elemPrimTbl = htmlElement.create("table", {parent: elemMainPrimTd, attributes: {cellpadding: 0, cellspacing: 0}});
//						elemPrimTBody = htmlElement.create("tbody", {parent: elemPrimTbl});
//					}
//					renderedPrimitivesCount++;
	//
//					var elemPrimTr = htmlElement.create("tr", {parent: elemPrimTBody, attributes: {class: "panel-primitive-row"}});

					if (children[i].typeName == "document") {
						return;
					}
					var fieldContainerDiv = htmlElement.create("div", {parent: parent, attributes: {class: "ui-field-contain"}});
					var elemPrimitive = this.generatePath(null, children[i], {labelAsBooleanInput: true});

					// Label
//					var elemPrimLabelTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-label-column"}});
					fieldContainerDiv.children.push(elemPrimitive.children[0]);

					// Prefix
//					var elemPrimPrefixTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-prefix-column"}});
//					this.addPrefix(elemPrimPrefixTd, children[i]);

					// Input
//					var elemPrimInputTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-input-column"}});
					fieldContainerDiv.children.push(elemPrimitive.children[1]);
					
					// Has a validation error message field
					if (elemPrimitive.children.length > 2) {
						fieldContainerDiv.children.push(elemPrimitive.children[2]);	
					}

					// Suffix
//					var elemPrimSuffixTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-suffix-column"}});
//					this.addSuffix(elemPrimSuffixTd, children[i]);
					
//					var elemValidationImgTd = htmlElement.create("td", {parent: elemPrimTr, attributes: {class: "panel-validation-image-column"}});
//					if (elemPrimitive.children[2] != undefined) {
//						elemValidationImgTd.children.push(elemPrimitive.children[2]);
//					}
				} else {
					renderedPrimitivesCount = 0;
					createPrimitiveContainer = true;
					this.generatePath(parent, children[i]);
				}
			}
		};
		
		/**
		 * 
		 */
		CodeGeneratorMobile.prototype.generateList = function(parent, path) {
			
		};
		
		/**
		 * 
		 */
		CodeGeneratorMobile.prototype.generateDocument = function(parent, path) {
			
		};

		/*
		 * 
		 */
		CodeGeneratorMobile.prototype.processValidations = function(elem, validations, ngFormName, elemWrapper, elemMain) {
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
		};
		
		/**
		 * 
		 */
		CodeGeneratorMobile.prototype.setInputElementTypeForDateInput = function(elem) {
			elem.attributes['type'] = "date";
		};
		
		/**
		 * 
		 */
		CodeGeneratorMobile.prototype.setInputElementTypeForNumericalInput = function(elem) {
			elem.attributes['type'] = "number";
		};
		
		/**
		 * 
		 */
		CodeGeneratorMobile.prototype.generatePanelWrapper = function(parentElem) {
			return parentElem;
		};
	};
});