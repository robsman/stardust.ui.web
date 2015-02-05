/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Subodh.Godbole
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdNumberFilter', ['sdUtilService', NumberFilterDirective]);

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
							'<td><label class="label-form">{{i18n(\'portal-common-messages.common-filterPopup-betweenFilter-first\')}}</label></td>' +
							'<td>' +
								'<input type="number" id="from" name="from" ng-model="filterData.from" ng-model-onblur sd-validate="integer" />' +
								'<div class="msg-error" ng-show="filterForm[\'from\'].$error.number || filterForm[\'from\'].$error.validate">' +
									'{{i18n(\'html5-common.converter-number-error\')}}' +
								'</div>' +
							'</td>' +
						'</tr>' +
						'<tr>' +
							'<td><label class="label-form">{{i18n(\'portal-common-messages.common-filterPopup-betweenFilter-last\')}}</label></td>' +
							'<td>' +
								'<input type="number" id="to" name="to" ng-model="filterData.to" ng-model-onblur sd-validate="integer" />' +
								'<div class="msg-error" ng-show="filterForm[\'to\'].$error.number || filterForm[\'to\'].$error.validate">' +
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
					scope.filterForm.$error.range = false;

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
								} else {
									scope.setFilterTitle('< ' + scope.filterData.to);
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
			}
		};
	}
})();