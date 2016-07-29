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
	

	angular.module('bpm-common').directive('sdDateTableFilter', ['sdUtilService','$filter', DateFilterDirective]);

	/*
	 * 
	 */
	function DateFilterDirective(sdUtilService, $filter) {
		return {
			restrict: 'A',
			template: 
				'<form name="filterForm">' +
					'<table cellspacing="2" cellpadding="2" style="white-space: nowrap;">' +
						'<tr>' +
							'<td><label class="label-item">{{i18n(\'portal-common-messages.common-filterPopup-betweenFilter-first\')}}</label></td>' +
							'<td>' +
								'<div sd-date-time-condensed sda-date-type="dateType" ng-model="filterData.from" id="from" name="from" ng-model-onblur> </div>' +
								'<div class="msg-error" ng-show="filterForm[\'from\'].$error.validate">{{i18n(\'html5-common.date-time-error\')}}</div>' +
							'</td>' +
						'</tr>' +
						'<tr>' +
							'<td><label class="label-item">{{i18n(\'portal-common-messages.common-filterPopup-betweenFilter-last\')}}</label></td>' +
							'<td>' +
        							'<div sd-date-time-condensed sda-date-type="dateType" ng-model="filterData.to" id="to" name="to" ng-model-onblur> </div>' +
        							'<div class="msg-error" ng-show="filterForm[\'to\'].$error.validate">{{i18n(\'html5-common.date-time-error\')}}</div>' +
							'</td>' +
						'</tr>' +
					'</table>' +
					'<div class="msg-error" ng-show="filterForm.$error.range">{{i18n(\'portal-common-messages.common-filterPopup-betweenFilter-message-rangeNotValid\')}}</div>' +
				'</form>',
			link: function(scope, element, attr, ctrl) {
			  
			  scope.dateType = attr.sdaDateType;
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
									scope.setFilterTitle(formatDate(scope.filterData.from, scope.dateType) + ' - ' + formatDate(scope.filterData.to, scope.dateType));
									return true;
								} else {
									scope.filterForm.$error.range = true;
								}
							} else {
								if (!sdUtilService.isEmpty(scope.filterData.from)) {
									scope.setFilterTitle('> ' + formatDate(scope.filterData.from, scope.dateType));
									delete scope.filterData.to;
								} else {
									scope.setFilterTitle('< ' + formatDate(scope.filterData.to, scope.dateType));
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

				/*
				 * 
				 */
				function formatDate(mills, dateType) {
			    var date = new Date(mills);
          var angularDateFilter = $filter('sdDateTimeFilter');
          if (dateType == 'date') {
            angularDateFilter = $filter('sdDateFilter');
          }
			    return angularDateFilter(date);
				}
			}
		};
	}
})();