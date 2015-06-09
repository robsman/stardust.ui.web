/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Abhay.Thappan
 */
(function() {
	'use strict';

	angular.module('workflow-ui.services').provider(
			'sdNotesService',
			function() {
				this.$get = [ '$resource', 'sdLoggerService', 'sdUtilService',
						function($resource, sdLoggerService, sdUtilService) {
							var service = new NotesService($resource, sdLoggerService, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function NotesService($resource, sdLoggerService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/notes";

		var trace = sdLoggerService.getLogger('workflow-ui.services.sdNotesService');

	

		NotesService.prototype.getNotes = function(processInstanceOid) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:processInstanceOid";

			var urlTemplateParams = {};
			urlTemplateParams.processInstanceOid = processInstanceOid;

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};
	}
	;
})();
