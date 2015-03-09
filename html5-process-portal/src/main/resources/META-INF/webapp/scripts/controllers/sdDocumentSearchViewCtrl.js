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
					'sdUtilService', 'sdMimeTypeService', DocumentSearchViewCtrl ]);

	/*
	 * 
	 */
	function DocumentSearchViewCtrl($q, $scope, sdDocumentSearchService,
			sdViewUtilService, sdUtilService, sdMimeTypeService) {
		// variable for search result table
		this.documentSearchResult = {};
		this.documentVersions = {};
		this.dataTable = null;
		this.columnSelector = 'admin';
		this.docVersionsdataTable = null;
		
		this.exportFileName = new Date();

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.searchAttributes = function() {
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
		DocumentSearchViewCtrl.prototype.defineData = function() {
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
		DocumentSearchViewCtrl.prototype.initialize = function() {
			this.defineData();
			this.searchAttributes();
		}

		this.initialize();

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.resetSearchCriteria = function() {
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
		DocumentSearchViewCtrl.prototype.refresh = function() {
			this.dataTable.refresh(true);
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.performSearch = function(options) {
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
		DocumentSearchViewCtrl.prototype.getAuthors = function(searchVal) {
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
		DocumentSearchViewCtrl.prototype.validateDateRange = function(fromDate, toDate) {
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
		DocumentSearchViewCtrl.prototype.openAttachToProcessDialog = function(rowData) {
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
		DocumentSearchViewCtrl.prototype.openProcessDialog = function(rowData) {
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
		DocumentSearchViewCtrl.prototype.openProcessHistory = function(oid) {
			this.processDialog.close();
			sdViewUtilService.openView("processInstanceDetailsView",
					"processInstanceOID=" + oid, {
						"processInstanceOID" : "" + oid
					}, true);
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.openDocumentView = function(documentId) {
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
		DocumentSearchViewCtrl.prototype.openUserDetails = function(documentOwner) {
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
		DocumentSearchViewCtrl.prototype.validateSearchCriteria = function() {
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
		DocumentSearchViewCtrl.prototype.getFileTypes = function() {
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
		DocumentSearchViewCtrl.prototype.constructFinalText = function() {
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
		DocumentSearchViewCtrl.prototype.onConfirmFromAdvanceText = function(res) {
			var self = this;
			self.query.documentSearchCriteria.containingText = self.advancedTextSearch.finalText;
			delete self.advancedTextSearch;
			self.showAdvancedTextSearch = false;
			console.log("dialog state: confirmed");
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.onCancelFromAdvanceText = function(res) {
			var self = this;
			delete self.advancedTextSearch;
			console.log("dialog state: cancelled");
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.onConfirmFromAttachToProcess = function(res) {
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
		DocumentSearchViewCtrl.prototype.attachDocumentsToProcess = function(processOID, documentId) {
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

		DocumentSearchViewCtrl.prototype.checkForProcessIdEmpty = function() {
			var self = this;
			if (self.processType == "SPECIFY"
					&& self.specifiedProcess == undefined) {
				self.showRequiredProcessId = false;
			}
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.advancedFileTypes = function() {
			var self = this;
			self.query.documentSearchCriteria.selectFileTypeAdvance = true;
			self.query.documentSearchCriteria.selectedFileTypes = [];
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.setShowAdvancedTextSearch = function() {
			var self = this;
			self.advancedTextSearch = {};
			self.showAdvancedTextSearch = true;
			self.showFinalText = true;
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.pickFromList = function() {
			var self = this;
			self.query.documentSearchCriteria.selectFileTypeAdvance = false;
			self.query.documentSearchCriteria.selectedFileTypes = [ self.fileTypes[0].value ];
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.getGlyphiconClass = function(mimeType) {
			return sdMimeTypeService.getIcon(mimeType);
			 
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.getDocumentVersions = function(options) {
			var self = this;
			return self.documentVersions;
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.setShowDocumentVersions = function(documentId, documentName) {
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
		DocumentSearchViewCtrl.prototype.onCloseDocumentVersions = function(res) {
			var self = this;
			self.documentVersions = {};
		};

		/**
		 * 
		 */
		DocumentSearchViewCtrl.prototype.openUserDetailsFromVersionHistory = function(documentOwner) {
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