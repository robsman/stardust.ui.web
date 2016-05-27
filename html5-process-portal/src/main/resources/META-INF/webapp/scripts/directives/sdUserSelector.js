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
 * @author Nikhil.Gahlot
 */

(function(){
	'use strict';

	angular.module('workflow-ui').directive('sdUserSelector', ['$q', 'sdUtilService', 'sdUserService',
	                                                                 UserSelectorDirective]);

	/*
	 * Directive class
	 * 
	 * Attributes supported:
	 * 	sda-selected-data (=) REQUIRED
	 * 	sda-active-only (true or false)
	 * 	sda-multiple (true or false)
	 * 	sda-max (Number)
	 */
	function UserSelectorDirective($q, sdUtilService, sdUserService) {
		
		var directiveDefObject = {
				restrict : 'AE',
				scope: {  // Creates a new sub scope
					selectedData: '=sdaSelectedData',
					autoIdPrefix : '@sdaAidPrefix'
				},
				transclude: true,
				template: '<div sd-participant-selector'
			      		+ ' sda-selected-data="selectedData"'
			      		+ ' sda-multiple="userSelectorController.allowMultiple"'
			      		+ ' sd-data="userSelectorController.fetchUserData(params)"'
			      		+ ' sda-aid-prefix="{{autoIdPrefix}}">'
			      		+ '</div>',
				link: function(scope, element, attrs, ctrl) {
					new UserSelectorLink(scope, element, attrs, ctrl);
				}
			};
		
		/*
		 * Link class
		 */
		function UserSelectorLink(scope, element, attrs, ctrl) {
			
			var self = this;
			
			scope.userSelectorController = self;
			
			initialize();
			
			/*
			 * 
			 */
			UserSelectorLink.prototype.safeApply = function() {
				sdUtilService.safeApply(scope);
			};
			
			/*
			 * Initialize the component
			 */
			function initialize() {
				// Make sure i18n is available in the current scope
				if (!angular.isDefined(scope.i18n)) {
					scope.i18n = scope.$parent.i18n;
				}

				self.fetchUserData = fetchUserData;

				reset();
			}
			
			/*
			 * 
			 */
			function reset() {
				self.allowMultiple = (attrs.sdaMultiple == 'true');
				self.active = (attrs.sdaActiveOnly == 'true');
				
				if (angular.isDefined(attrs.sdaMax) && angular.isNumber(parseInt(attrs.sdaMax))) {
					self.max = parseInt(attrs.sdaMax);
				} else {
					self.max = 20; // Default max is 20
				}
			}
			
			/*
			 * 
			 */
			function fetchUserData(searchParam) {
				var deferred = $q.defer();

				if (angular.isDefined(searchParam) && searchParam.length > 0) {
					searchParam = searchParam.concat('%');

					clearTimeout(self.typingTimer);

					self.typingTimer = setTimeout(function() {
						sdUserService.searchUsers(searchParam, self.active, self.max).then(function(data) {
							deferred.resolve(data.list);
						}, function(result) {
							deferred.reject(result);
						});
					}, 500);
				} else {
					deferred.resolve({});
				}
				
				return deferred.promise;
			};
		}
		
		return directiveDefObject;
	}
})();