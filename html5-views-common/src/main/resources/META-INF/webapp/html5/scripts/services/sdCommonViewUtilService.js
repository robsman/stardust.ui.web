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
 * @author Abhay.Thappan
 */
(function() {
	'use strict';

	angular.module('viewscommon-ui.services').provider(
			'sdCommonViewUtilService',
			function() {
				this.$get = ['sdViewUtilService', 'sdLoggerService',
						function(sdViewUtilService, sdLoggerService) {
							var service = new CommonViewUtilService(sdViewUtilService, sdLoggerService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function CommonViewUtilService(sdViewUtilService, sdLoggerService) {
		var trace = sdLoggerService.getLogger('viewscommon-ui.services.sdCommonViewUtilService');

		/**
		 * 
		 */
		CommonViewUtilService.prototype.openDocumentView = function(documentId, nested, params) {
			var viewKey = 'documentOID=' + encodeURIComponent(documentId);
			viewKey = window.btoa(viewKey);

			var parameters = params;

			if(!parameters) {
				parameters = {
						"documentId" :  documentId
				}
			} else {
				parameters["documentId"] = documentId;
			}
			console.log("Document View Params");
			console.log(parameters);
			sdViewUtilService.openView('documentView', viewKey,parameters, nested);

		};

		/**
		 * 
		 */
		CommonViewUtilService.prototype.openCaseDetailsView = function(processInstanceOID, nested) {
			sdViewUtilService.openView('caseDetailsView', "processInstanceOID=" + processInstanceOID, {
				"processInstanceOID" : "" + processInstanceOID
			}, nested);
		};

		/**
		 * 
		 */
		CommonViewUtilService.prototype.openProcessInstanceDetailsView = function(processInstanceOID, nested) {
			sdViewUtilService.openView('processInstanceDetailsView', "processInstanceOID=" + processInstanceOID, {
				"processInstanceOID" : "" + processInstanceOID
			}, nested);
		};

		/**
		 * 
		 */
		CommonViewUtilService.prototype.openUserWorklistView = function(userId, nested) {
			sdViewUtilService.openView('worklistView', "id=" + userId, {
				"userId" : "" + userId
			}, nested);
		};

		/**
		 * 
		 */
		CommonViewUtilService.prototype.openParticipantWorklistView = function(participantQId, nested) {
			sdViewUtilService.openView('worklistView', "id=" + participantQId, {
				"participantQId" : "" + participantQId
			}, nested);
		};

		/**
		 * 
		 */
		CommonViewUtilService.prototype.openProcessWorklistView = function(processQId, nested) {
			sdViewUtilService.openView('worklistView', "id=" + processQId, {
				"processQId" : "" + processQId
			}, nested);
		};

		/**
		 * 
		 */
		CommonViewUtilService.prototype.openNotesView = function(processInstanceOid, nested) {
			sdViewUtilService.openView("notesPanel", "oid=" + processInstanceOid, {
				"oid" : "" + processInstanceOid
			}, nested);
		};

		CommonViewUtilService.prototype.openNotesViewHTML5 = function(processInstanceOid, processName, nested) {
			sdViewUtilService.openView("notesPanelHtml5", "oid=" + processInstanceOid, {
				"oid" : "" + processInstanceOid,
				"processName" : processName
			}, nested);
		};
		/**
		 * 
		 */
		CommonViewUtilService.prototype.openActivityView = function(activityOID, nested) {
			sdViewUtilService.openView("activityPanel", "OID=" + activityOID, {
				"oid" : "" + activityOID
			}, nested);
		};
		
		/**
		 * 
		 */
		CommonViewUtilService.prototype.openUserManagerDetailView = function(userOid, userId, nested) {
			sdViewUtilService.openView("userManagerDetailView", "userOid=" + userOid, {
				"userOid" : "" + userOid,
				"userId" : "" + userId
			}, nested);
		};
		
		/**
		 * 
		 */
		CommonViewUtilService.prototype.openGanttChartView = function(processInstanceOid,nested) {
			sdViewUtilService.openView("ganttChartView", "oid=" + processInstanceOid, {
				"processInstanceOId" : "" + processInstanceOid
			}, nested);
		};

	}
	;
})();
