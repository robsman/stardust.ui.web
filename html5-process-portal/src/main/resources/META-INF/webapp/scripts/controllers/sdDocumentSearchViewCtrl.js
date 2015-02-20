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
 * @author Subodh.Godbole
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
		$scope.documentSearchResult.tableHandleExpr = null;
		$scope.documentSearchResult.columnSelector = 'admin';

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
			serviceName : "userService",
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

		$scope.advancedTextSearch ={
			displayInd : true,
			finalText:'',
			allWords:'',
			exactPhrase:'',
			oneOrMore1:'',
			oneOrMore2:'',
			oneOrMore3:'',
			unwantedWords:''
			//advancedContainingText:false
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

	this.getAuthors = function(serviceName, searchVal){
		if(searchVal.length > 0) {
			searchVal = searchVal.concat("%");

			clearTimeout($scope.typingTimer);

			$scope.typingTimer = setTimeout(
				function(){
					sdDocumentSearchService.search(serviceName, searchVal).then(function(data) {
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
			$scope.closeThisDialog();
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
			this.showUserDetails = true;
			sdDocumentSearchService.getUserDetails(documentOwner).then(function(data){
				$scope.userDetails = data;
				//$scope.processDialogData.totalCount = data.totalCount;
			});
		}
	/*
	 * 
	 */
	/*DocumentSearchViewCtrl.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			this.refresh();
		} else if (event.type == "DEACTIVATED") {
			// TODO
		}
	};*/
	this.getFileTypes = function(){
		if($scope.query.documentSearchCriteria.showAll){
			$scope.fileTypes = $scope.allRegisteredMimeFileTypes;
		}else{
			$scope.fileTypes = $scope.typicalFileTypes
		}
	};
	this.constructFinalText = function(){
		$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.allWords;
		if(!$scope.advancedTextSearch.exactPhrase.length==0){
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat(" ");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat("\"").concat($scope.advancedTextSearch.exactPhrase).concat("\"");
		}
		if(!$scope.advancedTextSearch.oneOrMore1.length==0){
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat(" ");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat($scope.advancedTextSearch.oneOrMore1);

		}

		if(!$scope.advancedTextSearch.oneOrMore1.length==0
			&& !$scope.advancedTextSearch.oneOrMore2.length==0){
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat(" ");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat("OR");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat(" ");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat($scope.advancedTextSearch.oneOrMore2);
		}else if(!$scope.advancedTextSearch.oneOrMore2.length==0)
		{
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat(" ");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat($scope.advancedTextSearch.oneOrMore2);
		}

		if ((!$scope.advancedTextSearch.oneOrMore1.length==0
			|| !$scope.advancedTextSearch.oneOrMore2.length==0) && !$scope.advancedTextSearch.oneOrMore3.length==0)
		{
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat(" ");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat("OR");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat(" ");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat($scope.advancedTextSearch.oneOrMore3);
		}
		else if (!$scope.advancedTextSearch.oneOrMore3.length==0)
		{
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat(" ");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat($scope.advancedTextSearch.oneOrMore3);
		}

		if (!$scope.advancedTextSearch.unwantedWords.length==0)
		{
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat(" -");
			$scope.advancedTextSearch.finalText = $scope.advancedTextSearch.finalText.concat($scope.advancedTextSearch.unwantedWords);
		}

		if(!$scope.advancedTextSearch.finalText.length==0){
			$scope.advancedTextSearch.displayInd = false;
		}else{
			$scope.advancedTextSearch.displayInd = true;
		}
	};

	this.onConfirmFromAdvanceText = function(res){
		var promise = res.promise;
		promise.then(
			function(data){
				$scope.query.documentSearchCriteria.containingText = $scope.advancedTextSearch.finalText;
				$scope.advancedTextSearch.advancedContainingText = false;
				console.log("dialog state: confirmed");
			}).catch(function(){
				console.log("dialog state: rejected");
			});
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

	this.setAdvancedContainingText = function(){
		$scope.advancedTextSearch.advancedContainingText = true;
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
	}
})();