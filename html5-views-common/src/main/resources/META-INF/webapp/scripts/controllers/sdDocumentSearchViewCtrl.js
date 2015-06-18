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

	angular.module("viewscommon-ui").controller(
			'sdDocumentSearchViewCtrl',
			[ '$q', 'sdDocumentSearchService', 'sdViewUtilService', 'sdUtilService', 'sdMimeTypeService',
					'sdLoggerService','sdCommonViewUtilService','sdLoggedInUserService', DocumentSearchViewCtrl ]);
	var _q;
	var _sdDocumentSearchService;
	var _sdViewUtilService;
	var _sdUtilService;
	var _sdMimeTypeService;
	var trace;
	var _sdCommonViewUtilService;
	var _sdLoggedInUserService;
	
	/*
	 * 
	 */
	function DocumentSearchViewCtrl($q, sdDocumentSearchService, sdViewUtilService, sdUtilService, sdMimeTypeService,
			sdLoggerService, sdCommonViewUtilService, sdLoggedInUserService) {
		// variable initialization for various services
		trace = sdLoggerService.getLogger('viewscommon-ui.sdDocumentSearchViewCtrl');
		_q = $q;
		_sdDocumentSearchService = sdDocumentSearchService;
		_sdViewUtilService = sdViewUtilService;
		_sdUtilService = sdUtilService;
		_sdMimeTypeService = sdMimeTypeService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_sdLoggedInUserService = sdLoggedInUserService;
		
		// variable for search result table
		this.documentSearchResult = {};
		this.documentVersions = {};
		this.docSrchRsltTable = null;
		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ?  'admin' : true;
		this.docVersionsdataTable = null;
		this.exportFileNameForDocumentSearchResult = "DocumentSearchResult";
		this.rowSelection = null;
		this.showDocumentSearchCriteria = true;

		this.initialize();
	}

	/**
	 * 
	 */
	DocumentSearchViewCtrl.prototype.searchAttributes = function() {
		var self = this;
		_sdDocumentSearchService.searchAttributes().then(function(data) {
			self.fileTypes = data.typicalFileTypes;
			self.typicalFileTypes = data.typicalFileTypes;
			self.allRegisteredMimeFileTypes = data.allRegisteredMimeFileTypes;
			self.documentTypes = data.documentTypes;
			self.repositories = data.repositories;
			self.query.documentSearchCriteria.selectedFileTypes = [ self.fileTypes[0].value ];
			self.query.documentSearchCriteria.selectedDocumentTypes = [ self.documentTypes[0].value ];
			self.query.documentSearchCriteria.selectedRepository = [ self.repositories[0].value ];
		}, function(error) {
			trace.log(error);
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
	}

	/**
	 * 
	 */
	DocumentSearchViewCtrl.prototype.initialize = function() {
		this.defineData();
		this.searchAttributes();
	}

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
		if (self.selectedAuthors != undefined && self.selectedAuthors.length == 1) {
			delete self.selectedAuthors;
		}
	}

	/**
	 * To refresh the document search table
	 */
	DocumentSearchViewCtrl.prototype.refresh = function() {
		this.docSrchRsltTable.refresh(true);
	};

	/**
	 * This method is for getting the document search result by using search criteria
	 * @param options
	 * @returns
	 */
	DocumentSearchViewCtrl.prototype.performSearch = function(options) {
		var deferred = _q.defer();
		var self = this;
		if (self.selectedAuthors != undefined && self.selectedAuthors.length == 1) {
			self.query.documentSearchCriteria.author = self.selectedAuthors[0].id;
		} else {
			self.query.documentSearchCriteria.author = "";
		}

		self.query.options = options;

		_sdDocumentSearchService.performSearch(self.query).then(function(data) {
			self.documentSearchResult.list = data.list;
			self.documentSearchResult.totalCount = data.totalCount;
			deferred.resolve(self.documentSearchResult);
		}, function(error) {
			trace.log(error);
			deferred.reject(error);
		});
		return deferred.promise;
	};

	/**
	 * 
	 * @param rowData
	 */
	DocumentSearchViewCtrl.prototype.openProcessDialog = function(rowData) {
		var self = this;
		_sdDocumentSearchService.fetchProcessDialogData(rowData.documentId).then(function(data) {
			self.processDialogData = {};
			self.processDialogData.list = data.list;
			self.processDialogData.totalCount = data.totalCount;
			self.showProcessDialog = true;
		}, function(error) {
			trace.log(error);
		});
	}

	/**
	 * 
	 * @param oid
	 */
	DocumentSearchViewCtrl.prototype.openProcessHistory = function(oid) {
		this.processDialog.close();
		_sdCommonViewUtilService.openProcessInstanceDetailsView(oid,true);
	};

	/**
	 * 
	 * @param documentId
	 */
	DocumentSearchViewCtrl.prototype.openDocumentView = function(documentId) {
		_sdCommonViewUtilService.openDocumentView(documentId,true);
	}

	/**
	 * 
	 */
	DocumentSearchViewCtrl.prototype.validateSearchCriteria = function() {
		var self = this;
		var error = false;
		// validating the createDateTo and createDateFrom
		if (!_sdUtilService.validateDateRange(self.query.documentSearchCriteria.createDateFrom,
				self.query.documentSearchCriteria.createDateTo)) {
			error = true;
			self.searchCriteriaForm.$error.createDateRange = true;
		} else {
			self.searchCriteriaForm.$error.createDateRange = false;
		}
		// validating the modificationDateTo and modificationDateFrom
		if (!_sdUtilService.validateDateRange(self.query.documentSearchCriteria.modificationDateFrom,
				self.query.documentSearchCriteria.modificationDateTo)) {
			error = true;
			self.searchCriteriaForm.$error.modificationDateRange = true;
		} else {
			self.searchCriteriaForm.$error.modificationDateRange = false;
		}

		if (error) {
			// validation error in search criteria, then don't perform search
			self.showDocumentSearchResult = false;
		} else {
			// search criteria is valid then perform search 
			self.showDocumentSearchResult = true;
			self.showDocumentSearchCriteria = false;
			self.showTableData = true;
			if (self.docSrchRsltTable != null) {
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
		if (self.advancedTextSearch.allWords != undefined && self.advancedTextSearch.allWords != 0) {
			self.advancedTextSearch.finalText = self.advancedTextSearch.allWords;
		} else {
			self.advancedTextSearch.finalText = "";
		}
		if (self.advancedTextSearch.exactPhrase != undefined && self.advancedTextSearch.exactPhrase.length != 0) {
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat("\"").concat(
					self.advancedTextSearch.exactPhrase).concat("\"");
		}
		if (self.advancedTextSearch.oneOrMore1 != undefined && self.advancedTextSearch.oneOrMore1.length != 0) {
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
					.concat(self.advancedTextSearch.oneOrMore1);

		}

		if ((self.advancedTextSearch.oneOrMore1 != undefined && self.advancedTextSearch.oneOrMore1.length != 0)
				&& (self.advancedTextSearch.oneOrMore2 != undefined && self.advancedTextSearch.oneOrMore2.length != 0)) {
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat("OR");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
					.concat(self.advancedTextSearch.oneOrMore2);
		} else if (self.advancedTextSearch.oneOrMore2 != undefined && self.advancedTextSearch.oneOrMore2.length != 0) {
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
					.concat(self.advancedTextSearch.oneOrMore2);
		}

		if (((self.advancedTextSearch.oneOrMore1 != undefined && self.advancedTextSearch.oneOrMore1.length != 0) || (self.advancedTextSearch.oneOrMore2 != undefined && self.advancedTextSearch.oneOrMore2.length != 0))
				&& (self.advancedTextSearch.oneOrMore3 != undefined && self.advancedTextSearch.oneOrMore3.length != 0)) {
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat("OR");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
					.concat(self.advancedTextSearch.oneOrMore3);
		} else if (self.advancedTextSearch.oneOrMore3 != undefined && self.advancedTextSearch.oneOrMore3.length != 0) {
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText
					.concat(self.advancedTextSearch.oneOrMore3);
		}

		if (self.advancedTextSearch.unwantedWords != undefined && self.advancedTextSearch.unwantedWords.length != 0) {
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" -");
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
	 * @param res
	 */
	DocumentSearchViewCtrl.prototype.onConfirmFromAdvanceText = function(res) {
		var self = this;
		self.query.documentSearchCriteria.containingText = self.advancedTextSearch.finalText;
		delete self.advancedTextSearch;
		self.showAdvancedTextSearch = false;
	};

	/**
	 * 
	 * @param res
	 */
	DocumentSearchViewCtrl.prototype.onCancelFromAdvanceText = function(res) {
		var self = this;
		delete self.advancedTextSearch;
	};

	/**
	 * 
	 * @param rowSelection
	 */
	DocumentSearchViewCtrl.prototype.openAttachToProcessDialog = function(rowSelection) {
		var self = this;
		self.processDefns = {};
		self.showAttachToProcessDialog = true;
		if (angular.isArray(rowSelection)) {
			self.documentIds = self.getSelectedRoleIds(rowSelection);
		} else {
			self.documentIds = [ rowSelection.documentId ];
		}
		_sdDocumentSearchService.getAvailableProcessDefns().then(function(data) {
			self.processDefns.list = data.list;
			self.processDefns.totalCount = data.totalCount;
			if (self.processDefns.totalCount == 0) {
				self.processDefns.disabledSelectProcess = true;
				self.processType = "SPECIFY";
			} else {
				self.processType = "SELECT";
				self.selectedProcess = self.processDefns.list[0].value;
			}

		}, function(error) {
			trace.log(error);
		});
	};

	/**
	 * 
	 * @param rowSelection
	 * @returns
	 */
	DocumentSearchViewCtrl.prototype.getSelectedRoleIds = function(rowSelection) {
		var documentIds = [];
		for ( var index in rowSelection) {
			documentIds.push(rowSelection[index].documentId);
		}
		return documentIds;
	};

	/**
	 * 
	 * @param res
	 * @returns {Boolean}
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

		this.attachDocumentsToProcess(self.selectedProcess, self.documentIds);
		delete self.documentIds;
		delete self.selectedProcess;
		delete self.specifiedProcess;
	};

	/**
	 * 
	 * @param res
	 */
	DocumentSearchViewCtrl.prototype.onCloseFromAttachToProcess = function(res) {
		var self = this;
		delete self.documentIds;
		delete self.selectedProcess;
		delete self.specifiedProcess;
	};

	/**
	 * 
	 * @param processOID
	 * @param documentIds
	 */
	DocumentSearchViewCtrl.prototype.attachDocumentsToProcess = function(processOID, documentIds) {
		var self = this;
		_sdDocumentSearchService.attachDocumentsToProcess(processOID, documentIds).then(function(data) {
			self.infoData = {};
			self.infoData.messageType = data.messageType;
			self.infoData.details = data.details;
			self.showAttachDocumentResult = true;
		}, function(error) {
			trace.log(error);
		});
	};

	/**
	 * 
	 */
	DocumentSearchViewCtrl.prototype.checkForProcessIdEmpty = function() {
		var self = this;
		if (self.processType == "SPECIFY" && self.specifiedProcess == undefined) {
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
	 * @param mimeType
	 * @returns
	 */
	DocumentSearchViewCtrl.prototype.getGlyphiconClass = function(mimeType) {
		return _sdMimeTypeService.getIcon(mimeType);

	};

	/**
	 * 
	 */
	DocumentSearchViewCtrl.prototype.getDocumentVersions = function() {
		var self = this;
		return self.documentVersions;
	};

	/**
	 * 
	 * @param documentId
	 * @param documentName
	 */
	DocumentSearchViewCtrl.prototype.setShowDocumentVersions = function(documentId, documentName) {
		var self = this;
		_sdDocumentSearchService.getDocumentVersions(documentId).then(function(data) {
			self.documentVersions.list = data.list;
			self.documentVersions.totalCount = data.totalCount;
			self.showDocumentVersions = true;
			self.documentVersions.documentName = documentName;
			self.showDocumentVersion = true;
		}, function(error) {
			trace.log(error);
		});

	};

	/**
	 * 
	 * @param res
	 */
	DocumentSearchViewCtrl.prototype.onCloseDocumentVersions = function(res) {
		var self = this;
		self.documentVersions = {};
	};

	/**
	 * 
	 */
	DocumentSearchViewCtrl.prototype.setShowDocumentSearchCriteria = function() {
		var self = this;
		self.showDocumentSearchCriteria = !self.showDocumentSearchCriteria;
	};


	/**
	 * 
	 * @param metadata
	 * @returns
	 */
	DocumentSearchViewCtrl.prototype.getMetaData = function(metadata) {
		var metadataToExport = [];

		angular.forEach(metadata, function(metadataItem) {
			metadataToExport.push(metadataItem.first + " : " + metadataItem.second);
		});
		return metadataToExport.join(',');
	};
})();