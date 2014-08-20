/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Yogesh.Manware
 * 
 */
define([ "bpm-reporting/js/report/I18NUtils" ], function(I18NUtils) {
	return {
		create : function() {

			var angularModule = angular.module('angularApp', []);

			var controller = new ReportStorageController();

			angularModule.controller("ReportStorageController", function(
					$compile) {

			});

			angular.bootstrap(document, [ 'angularApp' ]);

			controller = controller.mergeControllerWithScope(controller);

			controller.initialize();

			return controller;
		}
	};

	/**
	 * 
	 */
	function ReportStorageController() {

		/**
		 * 
		 */
		ReportStorageController.prototype.getI18N = function(key) {
			return I18NUtils.getProperty(key);
		};

		/**
		 * 
		 */
		ReportStorageController.prototype.initialize = function() {
			// label
			this.report = payloadObj.report;
			this.modelParticipants = payloadObj.modelParticipants;
			this.reportMetada = {
				location : "personalFolder"
			};
			this.updateView();
			jQuery("#reportStoragePopup").css("visibility", "visible");			
		};

		/**
		 * 
		 */
		ReportStorageController.prototype.closePopup = function() {
			closePopup();
		};

		/**
		 * 
		 */
		ReportStorageController.prototype.saveReportInstance = function() {
			payloadObj.acceptFunction(this.reportMetada);
			closePopup();
		};

		/**
		 * 
		 */
		ReportStorageController.prototype.mergeControllerWithScope = function(
				controller) {
			var scope = angular.element(document.body).scope();

			jQuery.extend(scope, controller);
			this.inheritMethods(scope, controller);

			scope.updateView = function() {
				this.$apply();
			};

			scope.runInAngularContext = function(func) {
				scope.$apply(func);
			};

			return scope;
		};

		/**
		 * 
		 */
		ReportStorageController.prototype.inheritMethods = function(
				childObject, parentObject) {
			for ( var member in parentObject) {
				if (parentObject[member] instanceof Function) {
					childObject[member] = parentObject[member];
				}
			}
		};
	}
});
