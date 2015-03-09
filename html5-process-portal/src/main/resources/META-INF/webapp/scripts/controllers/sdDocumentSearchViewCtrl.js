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

	angular.module("workflow-ui").controller(
			'sdDocumentSearchViewCtrl',
			[ '$q', '$scope', 'sdDocumentSearchService', 'sdViewUtilService',
					'sdUtilService', DocumentSearchViewCtrl ]);

	/*
	 * 
	 */
	function DocumentSearchViewCtrl($q, $scope, sdDocumentSearchService,
			sdViewUtilService, sdUtilService) {
		// variable for search result table
		this.documentSearchResult = {};
		this.documentVersions = {};
		this.dataTable = null;
		this.columnSelector = 'admin';
		this.docVersionsdataTable = null;

		/**
		 * 
		 */
		this.searchAttributes = function() {
			var self = this;
			sdDocumentSearchService
					.searchAttributes()
					.then(
							function(data) {
								self.fileTypes = data.typicalFileTypes;
								self.typicalFileTypes = data.typicalFileTypes;
								self.allRegisteredMimeFileTypes = data.allRegisteredMimeFileTypes;
								self.documentTypes = data.documentTypes;
								self.repositories = data.repositories;
								self.query.documentSearchCriteria.selectedFileTypes = [ self.fileTypes[0].value ];
								self.query.documentSearchCriteria.selectedDocumentTypes = [ self.documentTypes[0].value ];
								self.query.documentSearchCriteria.selectedRepository = [ self.repositories[0].value ];
							});

		};

		/**
		 * 
		 */
		this.defineData = function() {
			var self = this;
			self.query = {
				documentSearchCriteria : {
					selectedFileTypes : [],
					selectedDocumentTypes : [],
					selectedRepository : [],
					showAll : false,
					searchContent : true,
					searchData : true,
					advancedFileType : "",
					selectFileTypeAdvance : false,
					documentName : "",
					documentId : "",
					createDateTo : new Date().getTime(),
					modificationDateTo : new Date().getTime(),
					author : "",
					containingText : ""
				}
			};

			self.partialAuthor = "";
		}

		/**
		 * 
		 */
		this.initialize = function() {
			this.defineData();
			this.searchAttributes();
		}

		this.initialize();

		/**
		 * 
		 */
		this.resetSearchCriteria = function() {
			var self = this;
			self.defineData();
			self.fileTypes = self.typicalFileTypes;
			self.query.documentSearchCriteria.selectedFileTypes = [ self.fileTypes[0].value ];
			self.query.documentSearchCriteria.selectedDocumentTypes = [ self.documentTypes[0].value ];
			self.query.documentSearchCriteria.selectedRepository = [ self.repositories[0].value ];
			if (self.selectedAuthors != undefined
					&& self.selectedAuthors.length == 1) {
				delete self.selectedAuthors;
			}
		}

		/**
		 * 
		 */
		this.refresh = function() {
			this.dataTable.refresh(true);
		};

		/**
		 * 
		 */
		this.performSearch = function(options) {
			var deferred = $q.defer();
			var self = this;
			if (self.selectedAuthors != undefined
					&& self.selectedAuthors.length == 1) {
				self.query.documentSearchCriteria.author = self.selectedAuthors[0].id;
			} else {
				self.query.documentSearchCriteria.author = "";
			}

			self.query.options = options;

			sdDocumentSearchService.performSearch(self.query).then(
					function(data) {
						self.documentSearchResult.list = data.list;
						self.documentSearchResult.totalCount = data.totalCount;
						deferred.resolve(self.documentSearchResult);
					});
			return deferred.promise;
		};

		/**
		 * 
		 */
		this.getAuthors = function(searchVal) {
			var self = this;
			if (searchVal.length > 0) {
				searchVal = searchVal.concat("%");

				clearTimeout(self.typingTimer);

				self.typingTimer = setTimeout(function() {
					sdDocumentSearchService.searchUsers(searchVal).then(
							function(data) {
								self.authors = data.list;
							});
				}, 500);
			}
		};

		/**
		 * 
		 */
		this.validateDateRange = function(fromDate, toDate) {
			if (!sdUtilService.isEmpty(fromDate)
					&& !sdUtilService.isEmpty(toDate)) {
				if (fromDate > toDate) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 
		 */
		this.openAttachToProcessDialog = function(rowData) {
			var self = this;
			self.processDefns = {};
			self.showAttachToProcessDialog = true;
			self.documentId = rowData.documentId;
			sdDocumentSearchService.getAvailableProcessDefns().then(
					function(data) {
						self.processDefns.list = data.list;
						self.processDefns.totalCount = data.totalCount;
						if (self.processDefns.totalCount == 0) {
							self.processDefns.disabledSelectProcess = true;
							self.processType = "SPECIFY";
						} else {
							self.processType = "SELECT";
						}

					});
		};

		/**
		 * 
		 */
		this.openProcessDialog = function(rowData) {
			var self = this;
			self.showProcessDialog = true;
			sdDocumentSearchService.fetchProcessDialogData(rowData.documentId)
					.then(function(data) {
						self.processDialogData = {};
						self.processDialogData.list = data.list;
						self.processDialogData.totalCount = data.totalCount;
					});
		}

		/**
		 * 
		 */
		this.openProcessHistory = function(oid) {
			this.processDialog.close();
			sdViewUtilService.openView("processInstanceDetailsView",
					"processInstanceOID=" + oid, {
						"processInstanceOID" : "" + oid
					}, true);
		};

		/**
		 * 
		 */
		this.openDocumentView = function(documentId) {
			var viewKey = "documentOID=" + documentId + "_instance";
			viewKey = window.btoa(viewKey);
			sdViewUtilService.openView("documentView",
					"documentOID=" + viewKey, {
						"documentId" : documentId
					}, true);
		}

		/**
		 * 
		 */
		this.openUserDetails = function(documentOwner) {
			var self = this;
			sdDocumentSearchService.getUserDetails(documentOwner).then(
					function(data) {
						self.userDetails = data;
						self.userDetails.userImageURI = sdUtilService
								.getRootUrl()
								+ data.userImageURI;
						self.showUserDetails = true;
					});
		}

		/**
		 * 
		 */
		this.validateSearchCriteria = function() {
			var self = this;
			var error = false;

			if (!this.validateDateRange(
					self.query.documentSearchCriteria.createDateFrom,
					self.query.documentSearchCriteria.createDateTo)) {
				error = true;
				self.searchCriteriaForm.$error.createDateRange = true;
			} else {
				self.searchCriteriaForm.$error.createDateRange = false;
			}

			if (!this.validateDateRange(
					self.query.documentSearchCriteria.modificationDateFrom,
					self.query.documentSearchCriteria.modificationDateTo)) {
				error = true;
				self.searchCriteriaForm.$error.modificationDateRange = true;
			} else {
				self.searchCriteriaForm.$error.modificationDateRange = false;
			}

			if (error) {
				self.showDocumentSearchResult = false;
			} else {
				self.showDocumentSearchResult = true;
				self.showTableData = true;
				if (self.dataTable != null) {
					self.refresh();
				}
			}
		}

		/**
		 * 
		 */
		this.getFileTypes = function() {
			var self = this;
			if (self.query.documentSearchCriteria.showAll) {
				self.fileTypes = self.allRegisteredMimeFileTypes;
			} else {
				self.fileTypes = self.typicalFileTypes
			}
		};

		/**
		 * 
		 */
		this.constructFinalText = function() {
			var self = this;
			if (self.advancedTextSearch.allWords != undefined
					&& self.advancedTextSearch.allWords != 0) {
				self.advancedTextSearch.finalText = self.advancedTextSearch.allWords;
			} else {
				self.advancedTextSearch.finalText = "";
			}
			if (self.advancedTextSearch.exactPhrase != undefined
					&& self.advancedTextSearch.exactPhrase.length != 0) {
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(" ");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat("\"").concat(
								self.advancedTextSearch.exactPhrase).concat(
								"\"");
			}
			if (self.advancedTextSearch.oneOrMore1 != undefined
					&& self.advancedTextSearch.oneOrMore1.length != 0) {
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(" ");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(self.advancedTextSearch.oneOrMore1);

			}

			if ((self.advancedTextSearch.oneOrMore1 != undefined && self.advancedTextSearch.oneOrMore1.length != 0)
					&& (self.advancedTextSearch.oneOrMore2 != undefined && self.advancedTextSearch.oneOrMore2.length != 0)) {
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(" ");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat("OR");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(" ");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(self.advancedTextSearch.oneOrMore2);
			} else if (self.advancedTextSearch.oneOrMore2 != undefined
					&& self.advancedTextSearch.oneOrMore2.length != 0) {
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(" ");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(self.advancedTextSearch.oneOrMore2);
			}

			if (((self.advancedTextSearch.oneOrMore1 != undefined && self.advancedTextSearch.oneOrMore1.length != 0) || (self.advancedTextSearch.oneOrMore2 != undefined && self.advancedTextSearch.oneOrMore2.length != 0))
					&& (self.advancedTextSearch.oneOrMore3 != undefined && self.advancedTextSearch.oneOrMore3.length != 0)) {
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(" ");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat("OR");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(" ");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(self.advancedTextSearch.oneOrMore3);
			} else if (self.advancedTextSearch.oneOrMore3 != undefined
					&& self.advancedTextSearch.oneOrMore3.length != 0) {
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(" ");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(self.advancedTextSearch.oneOrMore3);
			}

			if (self.advancedTextSearch.unwantedWords != undefined
					&& self.advancedTextSearch.unwantedWords.length != 0) {
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(" -");
				self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
						.concat(self.advancedTextSearch.unwantedWords);
			}

			if (!self.advancedTextSearch.finalText.length == 0) {
				self.showFinalText = false;
			} else {
				self.showFinalText = true;
			}
		};

		/**
		 * 
		 */
		this.onConfirmFromAdvanceText = function(res) {
			var self = this;
			self.query.documentSearchCriteria.containingText = self.advancedTextSearch.finalText;
			delete self.advancedTextSearch;
			self.showAdvancedTextSearch = false;
			console.log("dialog state: confirmed");
		};

		/**
		 * 
		 */
		this.onCancelFromAdvanceText = function(res) {
			var self = this;
			delete self.advancedTextSearch;
			console.log("dialog state: cancelled");
		};

		/**
		 * 
		 */
		this.onConfirmFromAttachToProcess = function(res) {
			var self = this;
			if (self.processType == "SPECIFY" && self.specifiedProcess == null) {
				self.showRequiredProcessId = true;
				return false;
			}

			if (self.processType == "SPECIFY") {
				self.selectedProcess = self.specifiedProcess;
			}

			this
					.attachDocumentsToProcess(self.selectedProcess,
							self.documentId);
		};

		/**
		 * 
		 */
		this.attachDocumentsToProcess = function(processOID, documentId) {
			var self = this;
			sdDocumentSearchService.attachDocumentsToProcess(processOID,
					documentId).then(function(data) {
				self.infoData = {};
				self.infoData.messageType = data.messageType;
				self.infoData.details = data.details;
				self.showAttachDocumentResult = true;
			});
		};

		/**
		 * 
		 */

		this.checkForProcessIdEmpty = function() {
			var self = this;
			if (self.processType == "SPECIFY"
					&& self.specifiedProcess == undefined) {
				self.showRequiredProcessId = false;
			}
		};

		/**
		 * 
		 */
		this.advancedFileTypes = function() {
			var self = this;
			self.query.documentSearchCriteria.selectFileTypeAdvance = true;
			self.query.documentSearchCriteria.selectedFileTypes = [];
		};

		/**
		 * 
		 */
		this.setShowAdvancedTextSearch = function() {
			var self = this;
			self.advancedTextSearch = {};
			self.showAdvancedTextSearch = true;
			self.showFinalText = true;
		};

		/**
		 * 
		 */
		this.pickFromList = function() {
			var self = this;
			self.query.documentSearchCriteria.selectFileTypeAdvance = false;
			self.query.documentSearchCriteria.selectedFileTypes = [ self.fileTypes[0].value ];
		};

		/**
		 * 
		 */
		this.bytesToSize = function(bytes) {
			var kilobyte = 1024;
			var megabyte = kilobyte * 1024;
			var gigabyte = megabyte * 1024;
			var terabyte = gigabyte * 1024;

			if ((bytes >= 0) && (bytes < kilobyte)) {
				return bytes + ' B';

			} else if ((bytes >= kilobyte) && (bytes < megabyte)) {
				return (bytes / kilobyte).toFixed(2) + ' KB';

			} else if ((bytes >= megabyte) && (bytes < gigabyte)) {
				return (bytes / megabyte).toFixed(2) + ' MB';

			} else if ((bytes >= gigabyte) && (bytes < terabyte)) {
				return (bytes / gigabyte).toFixed(2) + ' GB';

			} else if (bytes >= terabyte) {
				return (bytes / terabyte).toFixed(2) + ' TB';

			} else {
				return bytes + ' B';
			}
		};

		/**
		 * 
		 */
		this.getGlyphiconClass = function(iconPath) {
			if (iconPath == 'document-image.png') {
				return "glyphicon glyphicon-export";
			}
			if (iconPath == 'document-code.png') {
				return "glyphicon glyphicon-music";
			}
			if (iconPath == 'tree_document.gif') {
				return "glyphicon glyphicon-th";
			}
			if (iconPath == 'document-music.png')
				return "glyphicon glyphicon-music";
		};

		/**
		 * 
		 */
		this.getDocumentVersions = function(options) {
			var self = this;
			return self.documentVersions;
		};

		/**
		 * 
		 */
		this.setShowDocumentVersions = function(documentId, documentName) {
			var self = this;
			sdDocumentSearchService.getDocumentVersions(documentId).then(
					function(data) {
						self.documentVersions.list = data.list;
						self.documentVersions.totalCount = data.totalCount;
						self.showDocumentVersions = true;
						self.documentVersions.documentName = documentName;
					});

		};

		/**
		 * 
		 */
		this.onCloseDocumentVersions = function(res) {
			var self = this;
			self.documentVersions = {};
		};

		/**
		 * 
		 */
		this.openUserDetailsFromVersionHistory = function(documentOwner) {
			var self = this;
			sdDocumentSearchService.getUserDetails(documentOwner).then(
					function(data) {
						self.userDetails = data;
						self.userDetails.userImageURI = sdUtilService
								.getRootUrl()
								+ data.userImageURI;
						self.showUserDetailsFromDocHistory = true;
					});

		};
	}
})();