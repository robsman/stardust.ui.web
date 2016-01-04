/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Subodh.Godbole
 */

'use strict';

angular.module('bpm-ui.services').provider('sdSidebarService', function () {
	var self = this;
	var REST_BASE_URL = 'services/rest/portal/';

	self.$get = ['sdUtilService', function (sdUtilService) {

		var service = {};

		/*
		 * 
		 */
		service.getPerspectives = function() {
			return sdUtilService.ajax(REST_BASE_URL, '', 'perspectives');
		}

		return service;
	}];
});