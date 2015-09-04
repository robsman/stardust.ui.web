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
          ['$scope', 'sdUtilService', 'sdFolderService', 'sdI18nService', '$parse', Controller]);

  var _sdFolderService = null;
  var _sdI18nService = null;
  var _parse = null;
  /*
   * 
   */
  function Controller($scope, sdUtilService, sdFolderService, sdI18nService, $parse) {
    this.readOnly = true;
    _sdI18nService = sdI18nService;
    _sdFolderService = sdFolderService;
    _parse = $parse;
    this.correspondenceTypes = [{
		label : this.i18n("views-common-messages.views-correspondenceView-details-type-email"),
		id : 'email'
	}, {
		label :  this.i18n("views-common-messages.views-correspondenceView-details-type-print"),
		id : 'print'
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
      console.log(ctrl.folderId);
      ctrl.getExistingFolderInformation(ctrl.folderId);
    } else {
      console.error("Couldnt Retrive Folder ID");
    }
  };

  Controller.prototype.addressIconMapper = function(item, index) {
    var tagClass = "glyphicon glyphicon-envelope"

    if (item.type == 'fax') {
      tagClass = "fa fa-fax";
    }
    return tagClass;
  };

  /**
   * 
   */
  Controller.prototype.getExistingFolderInformation = function(folderId) {
    var ctrl = this;
    _sdFolderService.getFolderInformationByFolderId(folderId).then(function(data) {
      console.log("Return from getExistingFolderInformation using folder id - " + folderId)
      console.log(data);
      ctrl.selected = populateCorrespondenceMetaData(data.correspondenceMetaDataDTO, data.documents)
    });
  }

  /**
   * 
   */
  Controller.prototype.i18n = function(key) {
    return _sdI18nService.translate(key);
  }

  function populateCorrespondenceMetaData(metaData, documents) {
    var type = "print";
    if (isEmail(metaData)) {
      type = "email"
    }

    var uiData = {
      type: type, // print / email
      to: formatInDataAddress(metaData.To),
      BCC: metaData.BCC,
      cc: metaData.CC,
      content: metaData.MessageBody,
      subject: metaData.Subject,
      templateId: '',
      attachments: formatInDataAttachments(metaData.Attachments),
      aiOid: '',
      showBCC: metaData.BCC ? metaData.BCC.length > 0 : false,
      showCc: metaData.CC ? metaData.CC.length > 0 : false
    }
    console.log("Populated object")
    console.log(uiData)
    return uiData;
  }

  function formatInDataAddress(addresses) {
    var outAddresses = [];
    angular.forEach(addresses, function(data) {
      if (data.Address && data.Address.length > 1) {
        var type = "email";
        if (data.IsFax) {
          type = "fax";
        }
        outAddresses.push({
          name: data.DataPath,
          value: data.Address,
          type: type
        });
      }
    });

    return outAddresses;
  }

  function formatInDataAttachments(attachments) {
    var outAttachments = [];
    angular.forEach(attachments, function(data) {
      if (data.DocumentId && data.DocumentId.length > 1) {
        outAttachments.push({
          path: data.DocumentId,
          uuid: data.DocumentId,
          templateDocuemntId: data.TemplateDocumentId,
          name: data.Name ? data.Name : "unknown"
        })
      }
    });
    return outAttachments;
  }

  function isEmail(metaData) {
    if (metaData.To || metaData.BCC || metaData.CC) { return true }
    return false;
  }

})();