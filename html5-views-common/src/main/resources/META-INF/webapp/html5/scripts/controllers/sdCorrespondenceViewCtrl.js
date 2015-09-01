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
			['$scope','$q','$parse','sdUtilService','sdFolderService', Controller]);

	var _parse = null;
	var _sdFolderService = null;

	/*
	 * 
	 */
	function Controller( $scope, $q, $parse, sdUtilService, sdFolderService) {
		this.readOnly = true;
		
		_parse = $parse;
		_sdFolderService = sdFolderService;
		
		this.correspondenceTypes = [{
			label : 'Email',
			id : 'email'
		}, {
			label : 'Print',
			id : 'print'
		}];
		
		
		this.documentParams = {
				
		}
		
		this.selected = {
				
		};
		
		
		this.documentParams  = {
				disableSaveAction : true
		}
		
		this.populateCorrespondenceData($scope);
	}
	
	
	Controller.prototype.populateCorrespondenceData = function($scope){
		var ctrl = this;
		var queryGetter = _parse("panel.params.custom");
		var params = queryGetter($scope);

		if(params && params.folderId) {
			ctrl.folderId = params.folderId;
			console.log(ctrl.folderId);
			ctrl.getExistingFolderInformation(ctrl.folderId);
		} else {
			console.error("Couldnt Retrive Folder ID");
		}
	};
	
	/**
	 * 
	 */
	Controller.prototype.getExistingFolderInformation = function( folderId ){
		var ctrl = this;
		_sdFolderService.getFolderInformationByFolderId(folderId).then(function(data){
			console.log("Return from getExistingFolderInformation using folder id - "+folderId)
			console.log(data);
			ctrl.selected = populateCorrespondenceMetaData(data.correspondenceMetaDataDTO, data.documents )
		});
	}
	
	function populateCorrespondenceMetaData(metaData, documents){
		var type = 'print'
		if(metaData.to || metaData.bcc || metaData.cc) {
			type = 'email'
		}
		
		var uiData =  {
			type  : type, // print / email
			to: metaData.to,
			bcc:metaData.bcc,
			cc:metaData.cc,
			content : metaData.content,
			subject : metaData.subject,
			templateId : '',
			attachments : documents,
			aiOid : '',
			showBcc : metaData.bcc ? metaData.bcc.length > 0 : false,
			showCc :   metaData.cc ? metaData.cc.length > 0 : false
		}
		console.log("Populated object")
		console.log(uiData)
		return uiData;
	}

	

})();