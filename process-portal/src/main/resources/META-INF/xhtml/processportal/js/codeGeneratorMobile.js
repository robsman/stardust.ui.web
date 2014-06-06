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

define(["processportal/js/codeGenerator", "processportal/js/htmlElement", "processportal/js/m_utils"], function(codeGenerator, htmlElement, m_utils){
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
			// return as is for mobile clients.
			return html;
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
			for (var i = 0; i < children.length; i++) {
				if (children[i].isPrimitive && !children[i].isList) {
					if (children[i].typeName == "document") {
						return;
					}
					var fieldContainerDiv = htmlElement.create("div", {parent: parent, attributes: {class: "ui-field-contain"}});
					var elemPrimitive = this.generatePath(null, children[i], {labelAsBooleanInput: true});

					for (var c in elemPrimitive.children) {
						fieldContainerDiv.children.push(elemPrimitive.children[c]);
					}

					// TODO - add suffix related code 
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
			}
		};
		
		/**
		 * 
		 */
		CodeGeneratorMobile.prototype.handleDateTimeInputs = function(elem) {
			elem.attributes['type'] = "datetime";
		};
				
		/**
		 * 
		 */
		CodeGeneratorMobile.prototype.haveSeparateFieldForTime = function(path) {
			if (path.typeName == "time") {
				return true;
			} else {
				return false;
			}
		};
		
		/**
		 * 
		 */
		CodeGeneratorMobile.prototype.addTimeInputField = function(parent) {
			// Dummy lable is added only to get a decent layout in jQM
			var dummyLabel = htmlElement.create("label", {parent: parent});
			var elem2 = htmlElement.create("input", {parent: parent});
			elem2.attributes["ng-model-onblur"] = null;
			elem2.attributes['type'] = "time";
			elem2.attributes['class'] = "panel-input-dateTime-time panel-input";
			elem2.attributes['maxlength'] = 5; // HH:mm
			
			return elem2;
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