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
 * This services makes available the requireJS modules in Angular context
 * 
 * @author Subodh.Godbole
 */
'use strict';

angular.module('bpm-ui.services').provider('sgViewParamService', function () {
	var self = this;
	
	self.$get = ['$rootScope', function ($rootScope) {

		var service = {};

		/*
		 * 
		 */
		service.getParams = function(scope) {
			return scope.panel.params.custom;
		};

		/*
		 * 
		 */
		service.getParam = function(scope, param) {
			return scope.panel.params.custom[param];
		};

		return service;
	}];
});