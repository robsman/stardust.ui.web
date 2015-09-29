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
 * @author Abhay.Thappan
 */

(function() {
	'use strict';

	angular.module('bpm-common').directive('sdNameTableFilter', [ 'sdUtilService', '$filter', NameFilterDirective ]);

	/*
	 * 
	 */
	function NameFilterDirective(sdUtilService, $filter) {
		return {
			restrict : 'A',
			template : '<form name="filterForm">'
					+ '<table cellspacing="2" cellpadding="2" style="white-space: nowrap;">'
					+ '<tr>'
					+ '<td><label class="label-form">{{i18n(\'views-common-messages.common-filterPopup-userName-firstName-label\')}}</label></td>'
					+ '<td>'
					+ '<input type="text" ng-model="filterData.firstName" id="firstName" name="firstName" ng-model-onblur> </input>'
					+ '</td>'
					+ '</tr>'
					+ '<tr>'
					+ '<td><label class="label-form">{{i18n(\'views-common-messages.common-filterPopup-userName-lastName-label\')}}</label></td>'
					+ '<td>'
					+ '<input type="text" ng-model="filterData.lastName" id="lastName" name="lastName" ng-model-onblur> </input>'
					+ '</td>' + '</tr>' + '</table>' + '</form>',
			link : function(scope, element, attr, ctrl) {
				/*
				 * 
				 */
				scope.handlers.applyFilter = function() {
					if (sdUtilService.isEmpty(scope.filterData.lastName)
							&& sdUtilService.isEmpty(scope.filterData.firstName)) {
						return false;
					} else if (sdUtilService.isEmpty(scope.filterData.lastName)
							&& !sdUtilService.isEmpty(scope.filterData.firstName)) {
						scope.setFilterTitle(scope.filterData.firstName);
						return true;
					} else if (!sdUtilService.isEmpty(scope.filterData.lastName)
							&& sdUtilService.isEmpty(scope.filterData.firstName)) {
						scope.setFilterTitle(scope.filterData.lastName);
						return true;
					} else {
						scope.setFilterTitle(scope.filterData.lastName + ', ' + scope.filterData.firstName);
						return true;
					}
				};

				/*
				 * 
				 */
				scope.handlers.resetFilter = function() {
					// NOP
				};

			}
		};
	}
})();