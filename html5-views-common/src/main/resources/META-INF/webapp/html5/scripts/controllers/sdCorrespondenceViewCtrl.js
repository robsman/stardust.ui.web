/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Johnson.Quadras
 * @author Yogesh.Manware
 */

(function() {
  'use strict';

  angular.module("viewscommon-ui").controller('sdCorrespondenceViewCtrl',
          ['$scope', 'sdUtilService', 'sdFolderService', 'sdI18nService', '$parse','sdLoggerService', 'sdPreferenceService', Controller]);

  var _sdFolderService = null;
  var _sdI18nService = null;
  var _parse = null;
  var trace = null;
  var config = null;
  var _sdPreferenceService = null;
  var _sdUtilService = null;
  var FAX_NUMBER_FORMAT = null;
  
  /*
   * 
   */
  function Controller($scope, sdUtilService, sdFolderService, sdI18nService, $parse, sdLoggerService, sdPreferenceService) {
    this.readOnly = true;
    _sdI18nService = sdI18nService;
    _sdFolderService = sdFolderService;
    _sdPreferenceService = sdPreferenceService;
    _sdUtilService = sdUtilService;
    _parse = $parse;
    trace = sdLoggerService.getLogger('bpm-common.sdCorrespondenceViewCtrl');
    
    this.correspondenceTypes = [{
      label: this.i18n("views-common-messages.views.correspondenceView.details.type.email"),
      id: 'email'
    }, {
      label: this.i18n("views-common-messages.views.correspondenceView.details.type.print"),
      id: 'print'
    }];

    this.selected = {};
    
    this.fetchPreference();
    FAX_NUMBER_FORMAT =  this.geFaxNumberFormat();

    this.documentParams = {
      disableSaveAction: true
    }

    this.populateCorrespondenceData($scope);
  }

  /**
   * 
   */
  Controller.prototype.populateCorrespondenceData = function($scope) {
    var ctrl = this;
    var queryGetter = _parse("panel.params.custom");
    var params = queryGetter($scope);

    if (params && params.folderId) {
      ctrl.folderId = params.folderId;
      ctrl.getExistingFolderInformation(ctrl.folderId);
    } else {
      trace.error("Couldnt Retrive Folder ID");
    }
  };
  
  /**
   * 
   */
  Controller.prototype.fetchPreference = function (){
	  var moduleId = 'ipp-views-common';
	  var preferenceId = 'preference';
	  var scope = 'PARTITION';
	  config =  _sdPreferenceService.getStore(scope, moduleId, preferenceId);
	  config.fetch();
  }


  /**
   * 
   */
  Controller.prototype.geFaxNumberFormat = function (){
	  var fromParent = false;
	  var format = config.getValue('ipp-views-common.correspondencePanel.prefs.correspondence.numberFormat', fromParent);
	  if(format){
		  FAX_NUMBER_FORMAT =  new RegExp(format);
	  }
	  return FAX_NUMBER_FORMAT;
  }

  /**
   * 
   */
  Controller.prototype.addressIconMapper = function(item, index) {
    var tagClass = "pi pi-email"

    if (item.type == 'fax') {
      tagClass = "pi pi-fax";
    }
    return tagClass;
  };

  /**
   * 
   */
  Controller.prototype.getExistingFolderInformation = function(folderId) {
    var ctrl = this;
    _sdFolderService.getFolderInformationByFolderId(folderId).then(function(data) {
      trace.log("Return from getExistingFolderInformation using folder id - " , folderId, "Data : ",data)
      ctrl.selected = populateCorrespondenceMetaData( data.correspondenceMetaDataDTO );
    });
  }

  /**
   * 
   */
  Controller.prototype.i18n = function(key) {
    return _sdI18nService.translate(key);
  }

  /**
   * 
   */
  function populateCorrespondenceMetaData(metaData) {
    if (!metaData) {
      metaData = {};
    }
    
    var uiData = {
    		content: metaData.MessageBody,
    		subject: metaData.Subject ? metaData.Subject : "",
    		templateId: '',
    		attachments: formatInDataAttachments(metaData.Documents),
    		aiOid: ""
    }

    uiData = formatInDataAddresses(uiData,metaData.Recipients);
    uiData.showBcc = uiData.bcc.length > 0 ;
    uiData.showCc = uiData.cc.length > 0 ; 
    
    var type = "print";
    if (isEmail(uiData)) {
      type = "email"
    }
    uiData.type = type;
    return uiData;
  }
  
  /**
   * 
   */
  function formatInDataAddresses ( uiData, addresses){
		uiData.to = []
		uiData.bcc = [];
		uiData.cc = [];
		angular.forEach(addresses,function( data ){
			
			var add = {
				name : data.DataPath,
				value : data.Address,
				type  : _sdUtilService.isFaxNumber(data.Address,FAX_NUMBER_FORMAT) ? "fax" :"email" 
			}
			
			if(data.Channel == "EMAIL_TO") {
				uiData.to.push(add);
			}
			else if(data.Channel == "EMAIL_CC") {
				uiData.cc.push(add);
			}
			else if(data.Channel == "EMAIL_BCC") {
				uiData.bcc.push(add);
			}
		});
		return uiData;
	}

	/**
	 * 
	 */
	function formatInDataAttachments(attachments){
		var outAttachments = []; 
		angular.forEach(attachments,function(data){
			if(data.Name && data.Name.length > 1) {
				outAttachments.push({
					documentId :  data.OutgoingDocumentID,
					name : data.Name,
					path: data.OutgoingDocumentID,
					uuid: data.OutgoingDocumentID,
					convertToPdf:data.ConvertToPDF
				});
			}
		});
		return outAttachments;
	}
	

	/**
	 * 
	 */
  function isEmail(uiData) {
    if (uiData && (uiData.to.length > 0  || uiData.bcc.length > 0  || uiData.cc.length > 0 || uiData.subject.trim().length > 0 )) { return true }
    return false;
  }

})();