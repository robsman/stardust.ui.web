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

	angular.module('viewscommon-ui.services').provider(
			'sdFolderService',
			function() {
				this.$get = [
						'$resource',
						'sdLoggerService',
						'sdUtilService',
						function( $resource, sdLoggerService, sdUtilService) {
							var service = new FolderService( $resource, sdLoggerService, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function FolderService($rootScope, $resource, sdLoggerService, $q, $http, sdDataTableHelperService, sdUtilService) {

		/**
		 * 
		 */
		this.getFolderInformationByFolderId = function (folderId){ 
			var url =  sdUtilService.getBaseUrl() +"services/rest/portal/folders/"+folderId;
			return $resource(url).get().$promise;
		}
		
	};
	
})();
