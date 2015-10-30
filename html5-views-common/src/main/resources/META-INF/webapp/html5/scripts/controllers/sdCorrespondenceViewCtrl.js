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
          ['$scope', 'sdUtilService', 'sdFolderService', 'sdI18nService', '$parse','sdLoggerService', Controller]);

  var _sdFolderService = null;
  var _sdI18nService = null;
  var _parse = null;
  var trace = null;
  /*
   * 
   */
  function Controller($scope, sdUtilService, sdFolderService, sdI18nService, $parse, sdLoggerService) {
    this.readOnly = true;
    _sdI18nService = sdI18nService;
    _sdFolderService = sdFolderService;
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

    this.documentParams = {
      disableSaveAction: true
    }

    this.populateCorrespondenceData($scope);
  }

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
    		subject: metaData.Subject,
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
				type  : "email"  //Method required to Determine type
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
					templateDocumentId : data.TemplateID,
					name : data.Name,
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
    if (uiData && (uiData.to.length > 0  || uiData.bcc.length > 0  || uiData.cc.length > 0 )) { return true }
    return false;
  }

})();