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
 * @author Subodh.Godbole
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdNumberTableFilter', ['sdUtilService', NumberFilterDirective]);

	/*
	 * 
	 */
	function NumberFilterDirective(sdUtilService) {
		return {
			restrict: 'A',
			template:
				'<form name="filterForm">' +
					'<table cellspacing="2" cellpadding="2" style="white-space: nowrap;">' +
						'<tr>' +
							'<td><label class="label-item">{{i18n(\'portal-common-messages.common-filterPopup-betweenFilter-first\')}}</label></td>' +
							'<td>' +
								'<input type="number" id="from" name="from" ng-model="filterData.from" style="text-align: left;" ng-model-onblur sd-validate="integer" />' +
								'<div class="msg-error" ng-show="filterForm.from.$error.number || filterForm.from.$error.validate">' +
									'{{i18n(\'html5-common.converter-number-error\')}}' +
								'</div>' +
							'</td>' +
						'</tr>' +
						'<tr>' +
							'<td><label class="label-item">{{i18n(\'portal-common-messages.common-filterPopup-betweenFilter-last\')}}</label></td>' +
							'<td>' +
								'<input type="number" id="to" name="to" ng-model="filterData.to" style="text-align: left;" ng-model-onblur sd-validate="integer" />' +
								'<div class="msg-error" ng-show="filterForm.to.$error.number || filterForm.to.$error.validate">' +
									'{{i18n(\'html5-common.converter-number-error\')}}' +
								'</div>' +
							'</td>' +
						'</tr>' +
					'</table>' +
					'<div class="msg-error" ng-show="filterForm.$error.range">{{i18n(\'portal-common-messages.common-filterPopup-betweenFilter-message-rangeNotValid\')}}</div>' +
				'</form>',
			link: function(scope, element, attr, ctrl) {
				/*
				 * 
				 */

				
				scope.handlers.applyFilter = function() {
					
					sdUtilService.removeFormErrors(scope.filterForm,['range']);
					
					if (scope.filterForm.$valid) {
						if (sdUtilService.isEmpty(scope.filterData.from) && sdUtilService.isEmpty(scope.filterData.to)) {
							scope.filterForm.$error.range = true;
						} else {
							if (!sdUtilService.isEmpty(scope.filterData.from) && !sdUtilService.isEmpty(scope.filterData.to)) {
								if (scope.filterData.from <= scope.filterData.to) {
									scope.setFilterTitle(scope.filterData.from + ' - ' + scope.filterData.to);
									return true;
								} else {
									scope.filterForm.$error.range = true;
								}
							} else {
								if (!sdUtilService.isEmpty(scope.filterData.from)) {
									scope.setFilterTitle('> ' + scope.filterData.from);
									delete scope.filterData.to;
								} else {
									scope.setFilterTitle('< ' + scope.filterData.to);
									delete scope.filterData.from;
								}
								return true;
							}
						}
					}
					return false;
				};

				/*
				 * 
				 */
				scope.handlers.resetFilter = function() {
					// NOP
				};

				/*
				 * Return true to show data
				 */
				scope.handlers.filterCheck = function(rowData, value) {
					var filterData = scope.filterData;

					if (value) {
						if (filterData.from != undefined && filterData.to != undefined) {
							return value >= filterData.from && value <= filterData.to;
						} else if (filterData.from != undefined) {
							return value >= filterData.from;
						} else if (filterData.from != undefined) {
							return value <= filterData.to;
						}
					}

					return false;
				};
			}
		};
	}
})();