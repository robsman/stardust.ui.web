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



	angular.module("workflow-ui").controller('sdDocumentSearchViewCtrl', ['$q','$scope','sdDocumentSearchService','sdViewUtilService', DocumentSearchViewCtrl]);

	/*
	 * 
	 */
	function DocumentSearchViewCtrl($q,$scope,sdDocumentSearchService,sdViewUtilService) {
	    // variable for search result table
		$scope.documentSearchResult={};
		this.documentVersions={};
		this.dataTable = null;
		this.docSearchRsltTblHndExp = 'docSearchViewCtrl.dataTable';
		this.columnSelector = 'admin';
		this.docVersionsdataTable = null;
		this.docVersionsTblHndExp = 'docSearchViewCtrl.docVersionsdataTable'

		$scope.documentTypes = [];

		$scope.fileTypes = [];

		$scope.repositories=[];



	    // $scope.query.documentSearchCriteria = this;
         $scope.query ={
			 options:{}
		 };
		 
         $scope.query.documentSearchCriteria ={
			selectedFileTypes :[],
			selectedDocumentTypes : [],
			selectedRepository :[],
			showAll : false,
			searchContent:true,
			searchData:true,
			advancedFileType : "",
			selectFileTypeAdvance : false,
			documentName : "",
			documentId : "",
			// createDateFrom : new Date(),
			// createDateTo : new Date(),
			// modificationDateFrom : new Date(),
			// modificationDateTo : new Date(),
			author:"",
			containingText : ""
		};

		this.advancedTextSearch ={
			finalText:'',
			allWords:'',
			exactPhrase:'',
			oneOrMore1:'',
			oneOrMore2:'',
			oneOrMore3:'',
			unwantedWords:''
		};


		$scope.authors=[];
		$scope.selectedAuthors=[];
	    $scope.partialAuthor="";

		$scope.processDialogData={};




	 this.searchAttributes = function(){
		sdDocumentSearchService.searchAttributes().then(function(data){
			$scope.fileTypes = data.typicalFileTypes;
			$scope.typicalFileTypes = data.typicalFileTypes;
			$scope.allRegisteredMimeFileTypes = data.allRegisteredMimeFileTypes;
			$scope.documentTypes = data.documentTypes;
			$scope.repositories = data.repositories;
			$scope.query.documentSearchCriteria.selectedFileTypes =[$scope.fileTypes[0].value];
			$scope.query.documentSearchCriteria.selectedDocumentTypes = [$scope.documentTypes[0].value];
			$scope.query.documentSearchCriteria.selectedRepository = [$scope.repositories[0].value];
		});

	    };

	    this.searchAttributes();

		DocumentSearchViewCtrl.prototype.refresh = function() {
			this.dataTable.refresh(true);
		};

	    this.performSearch = function(options){
			var deferred = $q.defer();
            if($scope.selectedAuthors.length == 1) {
				$scope.query.documentSearchCriteria.author = $scope.selectedAuthors[0].id;
			}
            
            $scope.query.options = options;
            
	    	sdDocumentSearchService.performSearch($scope.query).then(function(data){
	    		$scope.documentSearchResult.list = data.list;
	    		$scope.documentSearchResult.totalCount = data.totalCount;
				deferred.resolve($scope.documentSearchResult);
	    	});
			return deferred.promise;
	    };



	this.getAuthors = function(searchVal){
		if(searchVal.length > 0) {
			searchVal = searchVal.concat("%");

			clearTimeout($scope.typingTimer);

			$scope.typingTimer = setTimeout(
				function(){
					sdDocumentSearchService.searchUsers(searchVal).then(function(data) {
						$scope.authors = data.list;
					});
				},
				500
			);
		}
	    };

		this.openAttachToProcessDialog = function(rowData){
			this.showAttachToProcessDialog = true;
		}

		this.openProcessDialog = function(rowData){
			this.showProcessDialog = true;
			sdDocumentSearchService.fetchProcessDialogData(rowData.documentId).then(function(data){
				$scope.processDialogData.list = data.list;
				$scope.processDialogData.totalCount = data.totalCount;
			});
		}

		this.openProcessHistory = function(oid) {
			this.processDialog.close();
			sdViewUtilService.openView("processInstanceDetailsView",
				"processInstanceOID=" + oid,
				{
					"processInstanceOID": "" + oid
				}, true
			);
		};

		this.openDocumentView = function(documentId){
			var viewKey = "documentOID=" + documentId
			+ "_instance";
			viewKey = window.btoa(viewKey);
			sdViewUtilService.openView("documentView",
				"documentOID=" + viewKey,
				{
					"documentId":documentId
				}, true
			);
		}

		this.openUserDetails = function(documentOwner){
			var self = this;
			sdDocumentSearchService.getUserDetails(documentOwner).then(function(data){
				self.userDetails = data;
				self.userDetails.userImageURI = sdDocumentSearchService.getRootUrl() + data.userImageURI;
				self.showUserDetails = true;
			});
		}

		this.setShowTableData=function(){
			this.showTableData = true;
			if(this.dataTable != null){
				this.refresh();
			}
		}

	/*
	 * DocumentSearchViewCtrl.prototype.handleViewEvents = function(event) { if
	 * (event.type == "ACTIVATED") { this.refresh(); } else if (event.type ==
	 * "DEACTIVATED") { // TODO } };
	 */
		
	this.getFileTypes = function(){
		if($scope.query.documentSearchCriteria.showAll){
			$scope.fileTypes = $scope.allRegisteredMimeFileTypes;
		}else{
			$scope.fileTypes = $scope.typicalFileTypes
		}
	};
	
	this.constructFinalText = function(){
		var self = this;
		if(self.advancedTextSearch.allWords != undefined && self.advancedTextSearch.allWords != 0){
			self.advancedTextSearch.finalText = self.advancedTextSearch.allWords;
		}else{
			self.advancedTextSearch.finalText = "";
		}
		if(self.advancedTextSearch.exactPhrase != undefined && self.advancedTextSearch.exactPhrase.length != 0){
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat("\"").concat(self.advancedTextSearch.exactPhrase).concat("\"");
		}
		if(self.advancedTextSearch.oneOrMore1 != undefined && self.advancedTextSearch.oneOrMore1.length != 0){
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(self.advancedTextSearch.oneOrMore1);

		}

		if((self.advancedTextSearch.oneOrMore1 != undefined && self.advancedTextSearch.oneOrMore1.length != 0) && (self.advancedTextSearch.oneOrMore2 !=undefined
			&& self.advancedTextSearch.oneOrMore2.length!=0)){
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat("OR");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(self.advancedTextSearch.oneOrMore2);
		}else if(self.advancedTextSearch.oneOrMore2 != undefined && self.advancedTextSearch.oneOrMore2.length!=0)
		{
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(self.advancedTextSearch.oneOrMore2);
		}

		if (((self.advancedTextSearch.oneOrMore1 != undefined && self.advancedTextSearch.oneOrMore1.length!=0)
			|| (self.advancedTextSearch.oneOrMore2 != undefined && self.advancedTextSearch.oneOrMore2.length!=0)) 
			&& (self.advancedTextSearch.oneOrMore3 != undefined && self.advancedTextSearch.oneOrMore3.length!=0))
		{
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat("OR");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(self.advancedTextSearch.oneOrMore3);
		}
		else if (self.advancedTextSearch.oneOrMore3 != undefined && self.advancedTextSearch.oneOrMore3.length!=0)
		{
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" ");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(self.advancedTextSearch.oneOrMore3);
		}

		if (self.advancedTextSearch.unwantedWords != undefined && self.advancedTextSearch.unwantedWords.length!=0)
		{
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(" -");
			self.advancedTextSearch.finalText = self.advancedTextSearch.finalText.concat(self.advancedTextSearch.unwantedWords);
		}

		if(!self.advancedTextSearch.finalText.length==0){
			self.showFinalText = false;
		}else{
			self.showFinalText = true;
		}
	};

	this.onConfirmFromAdvanceText = function(res){
		var self = this;
		$scope.query.documentSearchCriteria.containingText = self.advancedTextSearch.finalText;
		self.advancedTextSearch ={};
		self.showAdvancedTextSearch = false;
		console.log("dialog state: confirmed");		
	};

	this.onCancelFromAdvanceText = function(res){
		var self = this;
		self.advancedTextSearch ={};
		console.log("dialog state: cancelled");		
	};
	
	this.onConfirmFromAttachToProcess = function(res){
			var promise = res.promise;
			promise.then(
				function(data){
					/*
					 * $scope.query.documentSearchCriteria.containingText =
					 * $scope.advancedTextSearch.finalText;
					 * $scope.advancedTextSearch.advancedContainingText = false;
					 */
					console.log("dialog state: confirmed");
				}).catch(function(){
					console.log("dialog state: rejected");
				});
	};

	this.advancedFileTypes = function(){
		$scope.query.documentSearchCriteria.selectFileTypeAdvance = true;
		$scope.query.documentSearchCriteria.selectedFileTypes = [];
	};

	this.setShowAdvancedTextSearch = function(){
		var self = this;
		self.showAdvancedTextSearch = true;
		self.showFinalText = true;
	};

	this.pickFromList = function(){
		$scope.query.documentSearchCriteria.selectFileTypeAdvance = false;
		$scope.query.documentSearchCriteria.selectedFileTypes =[$scope.fileTypes[0].value];
	};

	this.bytesToSize = function(bytes)
		{
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

		this.getGlyphiconClass = function(iconPath){
			if(iconPath == 'document-image.png'){
				return "glyphicon glyphicon-export";
			}
			if(iconPath =='document-code.png'){
				return "glyphicon glyphicon-music";
			}
			if(iconPath == 'tree_document.gif'){
				return "glyphicon glyphicon-th";
			}
			if(iconPath == 'document-music.png')
			return "glyphicon glyphicon-music";
		};
		
	  this.getDocumentVersions = function(options){
		  var deferred = $q.defer();
		  var self = this;
		  deferred.resolve(self.documentVersions);
          return deferred.promise;
	  };
	  
	  this.setShowDocumentVersions = function(documentId,documentName){
		    var self = this;
		    // this.documentId = documentId;
	    	sdDocumentSearchService.getDocumentVersions(documentId).then(function(data){
	    		self.documentVersions.list = data.list;
	    		self.documentVersions.totalCount = data.totalCount;
	    		self.showDocumentVersions = true;
	    		self.documentVersions.documentName = documentName;
	    	});	
		    
	    	
	  };
	  
	  this.onCloseDocumentVersions= function(res){	  
	  var self = this;
		  self.documentVersions = {};
	};
	
	this.openUserDetailsFromVersionHistory=function(documentOwner){
		var self = this;
		sdDocumentSearchService.getUserDetails(documentOwner).then(function(data){
			self.userDetails = data;
			self.userDetails.userImageURI = sdDocumentSearchService.getRootUrl() + data.userImageURI;
			self.showUserDetailsFromDocHistory = true;
		});
		
	};
	
	this.downloadDocument = function(res){
		var self = this;
		sdDocumentSearchService.downloadDocument(self.documentDownload.documentId,self.documentDownload.documentName);
		delete self.documentDownload;
		
	};
	
	this.setShowDocumentDownload = function(documentId,documentName){
		var self = this;
		self.showDoumentDownload = true;
		var documentDownload = {
			documentId : documentId,
			documentName : documentName
		};
		self.documentDownload = documentDownload;
		
	};
	
	this.downloadDocumentClose = function(){
		var self = this;
		delete self.documentDownload;
	};
	
	}
})();