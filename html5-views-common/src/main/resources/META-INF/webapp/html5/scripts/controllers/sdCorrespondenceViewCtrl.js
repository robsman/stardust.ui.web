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

	angular.module("viewscommon-ui").controller(
			'sdCorrespondenceViewCtrl',
			['$scope','$q','sdUtilService', Controller]);
	var _q;
	var _sdDocumentSearchService;
	var _sdViewUtilService;
	var _sdUtilService;
	var _sdMimeTypeService;
	var trace;
	var _sdCommonViewUtilService;
	var _sdLoggedInUserService;
	var _sdPreferenceService;

	/*
	 * 
	 */
	function Controller( $scope, $q, sdUtilService) {
		this.readOnly = true;
		
		this.correspondenceTypes = [{
			label : 'Email',
			id : 'email'
		}, {
			label : 'Print',
			id : 'print'
		}];
		
		this.selected = {
				type  : 'email', // print / email
				showBcc : false,
				showCc : false,
				to: [	{name : "Test",
						value : "Test@gmail.com",
						type : 'email'
						}],
				bcc:[],
				cc:[],
				message : 'This is a sample message',
				subject : 'This is sample text',
				templateId : 'testId',
				attachments : [],
				aiOid : '',
		};
	}

	

})();