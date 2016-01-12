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

	angular.module('viewscommon-ui').directive('sdUserDetails', [ 'sdUtilService' ,'sdUserService', UserDetails ]);
	/*
	 * 
	 */
	function UserDetails(sdUtilService,sdUserService) {

		return {
			restrict : 'EA',
			scope : { // Creates a new sub scope
				userOID : '=sdaUserOid',
				linkDisabled : '=?sdaLinkDisabled',
				account: '=sdaAccount',
				autoIdPrefix: '@sdaAidPrefix'
			},
			transclude : true,
			replace : true,
			templateUrl : sdUtilService.getBaseUrl() + 'plugins/html5-views-common/html5/scripts/directives/partials/userDetailsDialog.html',
			link : function(scope, element, attrs, ctrl) {
				new UserDetailsLink(scope, element, attrs, ctrl);
			}
		};

		/**
		 * 
		 */
		function UserDetailsLink(scope, element, attrs, ctrl) {

			var self = this;

			scope.userDetailsCtrl = self;

			initialize();

			/*
			 * 
			 */
			UserDetailsLink.prototype.safeApply = function() {
				sdUtilService.safeApply(scope);
			};

			function initialize() {
				// Make sure i18n is available in the current scope
				if (!angular.isDefined(scope.i18n)) {
					scope.i18n = scope.$parent.i18n;
				}
				
				if(!angular.isDefined(scope.linkDisabled)){
					scope.linkDisabled = false;
				}

				self.openUserDetails = openUserDetails;
			}
			
			/**
			 * 
			 * @param documentOwner
			 */
			function openUserDetails() {
				var self = this;
				if (scope.userOID != undefined){
					sdUserService.getUserDetails(scope.userOID).then(function(data) {
						self.userDetails = data;
						self.userDetails.userImageURI = (data.userImageURI.indexOf("/") > -1) ? sdUtilService.getRootUrl() + data.userImageURI : data.userImageURI; 
						self.showUserDetails = true;
					}, function(error) {
						trace.log(error);
					});
				}
				
			}
		}
	}
})();
