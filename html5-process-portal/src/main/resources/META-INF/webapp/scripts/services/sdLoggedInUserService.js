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
 * @author Johnson.Quadras
 */
(function() {
	'use strict';

	/**
	 * 
	 */
	angular.module('workflow-ui.services').provider( 'sdLoggedInUserService', function() {
		this.$get = [ '$q', '$resource', function ( $q, $resource) {
			var service = new LoggedInUserService($q, $resource);
			return service;
		}];
	});
	/**
	 *
	 */
	function LoggedInUserService( $q, $resource) {
		
		var user = {
			userId : 'motu'	
		};
		
		/**
		 * 
		 */
		LoggedInUserService.prototype.getUserId = function() {
			return  user.userId;
		};
		
	};
})();

