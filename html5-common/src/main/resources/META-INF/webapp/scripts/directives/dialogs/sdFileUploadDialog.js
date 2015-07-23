/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

(function(){
	
	'use strict'
	
	/**
	 * Controller function for our directive
	 */
	function fileUploadController($scope,sdUtilService,$q,$timeout){
		var that = this;
		this.files = [];
		this.currentFile = {};
		this.$scope = $scope;
		this.$timeout = $timeout;
		this.state = "initial";
		this.nonFileData = {};//data to send with the file upload, key->values
		this.fileDefer = {};
		this.id = "sdFileUploadDialog_" + $scope.$id;
		
		//Function has to be on scope so we can call it
		//from non angular environment as ng-change does not
		//work for file input types (see fileUpload.Html template script).
		$scope.fileNameChanged = function(elem){
			$timeout(function(){
				that.state = "fileSelected";
				that.files = elem.files;
			},0);
		}
		
		//Execute in a timeout so we allow the sdDialog in our template to be compiled
		//and have time to initialize the api object tagged to its attribute.
		$timeout(function(){
			var customApi = {
					"close" : that.uploadDialog.close,
					"open" : function(nonFileData){
						that.resetState();
						that.fileDefer = $q.defer();
						that.nonFileData = nonFileData;
						that.uploadDialog.open();
						return that.fileDefer.promise;
					}
			}
			$scope.dialogApi = that.uploadDialog;
			$scope.initCallback({api : customApi});
		},0);
	}
	
	/**
	 * Reset controller state otherwise we will carry over
	 * state for each open call.
	 */
	fileUploadController.prototype.resetState = function(){
		this.files = [];
		this.currentFile = {};
		this.state = "initial";
		this.nonFileData = {};
		this.fileDefer = {};
	};
	
	/**
	 * Closes a dialog and resolves promise;
	 */
	fileUploadController.prototype.closeDialog = function(){
		this.uploadDialog.close();
		if(this.fileDefer && this.fileDefer.resolve){
			this.fileDefer.resolve();
		}
	};
	
	//handles file upload, as suspected
	fileUploadController.prototype.uploadFile = function(files,nonFileData){
		
		var xhr = new XMLHttpRequest(),
		    fd = new FormData(),
		    that = this,
		    fileKey;
		
		//form data key we will upload files against
		fileKey = this.$scope.fileKey || "file";
		//add files
		for (var i in files) {
			fd.append(fileKey, files[i]);
		}
		
		//add other data
		for(var key in nonFileData){
			fd.append(key,nonFileData[key]);
		}
		
		xhr.upload.addEventListener("progress", function(e){
			console.log("Progress...");
			console.log(e);
			that.$timeout(function(){that.state="progress"},0);
		}, false);
		xhr.addEventListener("load", function(e){
			var status = (e.srcElement || e.target).status;
			
			//integer division -> any 2XX status = success
			if((status/100 | 0)===2){ 
				that.$timeout(function(res){that.state="success"},0);
				that.fileDefer.resolve(e);
			}
			//reject everything else
			else{
				that.$timeout(function(res){that.state="error"},0);
				that.fileDefer.reject(e);
			}
			
		}, false);
		xhr.addEventListener("error", function(e){
			var status = (e.srcElement || e.target)
			that.$timeout(function(){that.state="error"},0);
			that.fileDefer.reject(e);
		}, false);
        xhr.open("POST", that.$scope.url, true);
		xhr.send(fd);

	};
	
	fileUploadController.$inject = ["$scope","sdUtilService","$q","$timeout"];
	
	angular.module("bpm-common.directives").controller("sdFileUploadController",fileUploadController);
	
	/**
	 * Directive function
	 */
	function fileUploadDialog(sdUtilService,$q){
		
		var templateUrl = sdUtilService.getBaseUrl() + 'plugins/html5-common/scripts/directives/dialogs/templates/fileUpload.html';
		
		return {
			"scope": {
				"subTitle" : "@sdaSubTitle",
				"initCallback" : "&sdaOnInit",
				"fileKey" : "@sdaFileKey",
				"url" : "@sdaUrl"
			},
			"transclude" : true,
			"templateUrl" : templateUrl
		}
		
	};
	
	
	fileUploadDialog.$inject = ["sdUtilService","$q"];
	
	angular.module("bpm-common.directives")
	.directive("sdFileUploadDialog",fileUploadDialog);
	
})();