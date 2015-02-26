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

	angular.module('bpm-common').directive('sdTextSearchFilter', ['sdUtilService', TextSearchFilterDirective]);

	/*
	 * 
	 */
	function TextSearchFilterDirective(sdUtilService) {
		var MAX_TITLE_LENGTH = 35;

		return {
			restrict: 'A',
			template:
				'<form name="filterForm">' +
					'<table cellspacing="2" cellpadding="2" style="white-space: nowrap;">' +
						'<tr>' +
							'<td><label class="label-form">{{i18n(\'portal-common-messages.common-filterPopup-searchFilter-like\')}}</label></td>' +
							'<td>' +
								'<input type="text" id="textSearch" name="textSearch" ng-required="true" ng-model="filterData.textSearch" ng-model-onblur />' +
								'<div class="msg-error" ng-show="filterForm[\'textSearch\'].$error.required">{{i18n(\'portal-common-messages.common-filterPopup-searchFilter-message-required\')}}</div>' +
							'</td>' +
						'</tr>' +
					'</table>' +
				'</form>',
			link: function(scope, element, attr, ctrl) {
				/*
				 * 
				 */
				scope.handlers.applyFilter = function() {
					if (scope.filterForm.$valid) {
						var title = scope.filterData.textSearch;
						scope.setFilterTitle(sdUtilService.truncateTitle(title));
						return true;
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