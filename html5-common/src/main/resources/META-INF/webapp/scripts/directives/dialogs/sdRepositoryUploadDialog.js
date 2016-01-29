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
	
	var FileState = {
		"BASE": 1,
		"UPLOADING": 2,
		"ERROR": 3,
		"COMPLETE": 4
	};

	/**
	 * Controller function for our directive
	 */
	function fileRepoUploadController($scope,sdUtilService,$q,$timeout,sdLoggerService){

		var that = this;
		this.FileState = FileState;
		this.name="RepoUpload"
		this.files = [];
		this.responseFiles=[]; //files obj returned from server on success.
		this.selectedFile;
		this.totalSize = 0;
		this.totalUploadedSize = 0;
		this.removedFiles = [];
		this.curatedFiles = [];
		this.parentPath="/";
		this.$scope = $scope;
		this.$timeout = $timeout;
		this.$q = $q;
		this.state = "initial";
		this.nonFileData = {};//data to send with the file upload, key->values
		this.fileDefer = {};
		this.id = "sdFileUploadDialog_" + $scope.$id;
		this.trace = sdLoggerService.getLogger('bpm-common.directives.sdFileUploadController');

		//Function has to be on scope so we can call it
		//from non angular environment as ng-change does not
		//work for file input types (see fileUpload.Html template script).
		$scope.fileNameChanged = function(elem){
			$timeout(function(){

				var file;
				that.state = "fileSelected";
				that.files = elem.files;

				for (var i = 0;i<that.files.length;i++) {
					file=that.files[i];
					that.totalSize += file.size;
					that.curatedFiles.push({
						"versionComments" : "",
						"description" : "",
						"documentType" : {},
						"fileState" : FileState.BASE,
						"send" : true,
						"name" : file.name,
						"type" : file.type,
						"size" : file.size,
						"percentUploaded" : 0, 
						"blob" : file
					});
				};//for end

			},0);//timeout invoke end
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
	
	//handle file drops and shunt them through the same folder
	fileRepoUploadController.prototype.onFileDrop = function(data,e){
		console.log(data);
		console.log(e);
		this.$scope.fileNameChanged({"files" : e.dataTransfer.files});
	};

	fileRepoUploadController.prototype.selectedFileNames = function(files){

		var fileString = "";

		for (var i in files) {
			fileString += files[i].name = ", ";
		};
		fileString.substring(0,fileString.length -2);
	};

	/**
	 * Reset controller state otherwise we will carry over
	 * state for each open call.
	 */
	fileRepoUploadController.prototype.resetState = function(){
		this.files = [];
		this.curatedFiles=[];
		this.responseFiles = [];
		this.currentFile = {};
		this.state = "initial";
		this.nonFileData = {};
		this.fileDefer = {};
	};
	
	/**
	 * Closes a dialog and resolves promise;
	 */
	fileRepoUploadController.prototype.closeDialog = function(){
		this.uploadDialog.close();
		if(this.fileDefer && this.fileDefer.resolve){
			this.fileDefer.resolve(this.responseFiles);
		}
		this.resetState();
	};

	fileRepoUploadController.prototype.uploadFile = function(){

		var fileKey = this.$scope.fileKey,
			that = this,
			promises=[],
			invocations=[];
			

		this.curatedFiles.forEach(function(file){
			var filePromise,
				deferred,
				nonFileData;

			deferred = that.$q.defer();
			filePromise = deferred.promise;
			nonFileData = angular.extend({}, file);
			nonFileData.parentFolderPath = that.parentPath;

			promises.push(filePromise);

			delete nonFileData.blob;
			invocations.push([file,nonFileData,fileKey,deferred]);

			filePromise.then(function(file){
				file.fileState = FileState.COMPLETE;
			})
			["catch"](function(file){
				file.fileState = FileState.ERROR;
			});

		});

		this.state = "progress";

		this.$q.all(promises).then(function(data){
			var result,
				failed = false;

			data.forEach(function(result){

				if(result.failures.length>0){
					failed = true;
				}
				else{
					that.responseFiles.push(result.documents[0]);
				}
			});

			that.state = (failed)? "error" : "success";

		})
		["catch"](function(err){
			that.state = "error"
		});

		invocations.forEach(function(params){
			that.__uploadFile.apply(that,params);
		});
	};

	fileRepoUploadController.prototype.uploadNewVersion = function(dataPathId,content){
		//stubbed
	}

	//handles file upload, as suspected
	fileRepoUploadController.prototype.__uploadFile = function(file,nonFileData,fileKey,deferred){
		
		var xhr = new XMLHttpRequest(),
		    fd = new FormData(),
		    that = this;
		
		//add file
		fd.append(fileKey, file.blob);

		//add other data
		for(var key in nonFileData){
			fd.append(key,nonFileData[key]);
		}
		
		xhr.upload.addEventListener("progress", function(e){
			that.$timeout(function(){
				file.fileState = FileState.UPLOADING;
				file.percentUploaded = (e.loaded / e.total);
			},0);
		}, false);
		xhr.addEventListener("load", function(e){
			//We are going top resolve everything as what we want is Q.allSettled functionality.
			//We will let our main ALL handler determine state for the upload.
			var src = (e.srcElement || e.target),
			    status = src.status,
			    result = JSON.parse(src.response || src.responseText);

			//integer division -> any 2XX status = success
			if((status/100 | 0)===2){ 
				if(result.failures.length > 0){
					that.$timeout(function(res){file.fileState = FileState.ERROR;},0);
					deferred.resolve(result);
				}
				else{
					that.$timeout(function(res){file.fileState = FileState.COMPLETE;},0);
					deferred.resolve(result);
				}
			}
			//reject everything else
			else{
				that.$timeout(function(res){file.fileState = FileState.ERROR;},0);
				deferred.resolve(result);
			}
			
		}, false);
		xhr.addEventListener("error", function(e){
			var status = (e.srcElement || e.target)
			that.$timeout(function(){file.fileState = FileState.ERROR;},0);
			deferred.reject(file);
		}, false);
        xhr.open("POST", that.$scope.url, true);
		xhr.send(fd);

	};
	
	fileRepoUploadController.$inject = ["$scope","sdUtilService","$q","$timeout","sdLoggerService"];
	
	//angular.module("bpm-common.directives").controller("sdFileUploadController",fileUploadController);
	
	/**
	 * Directive function
	 */
	function fileUploadDialog(sdUtilService,$q){
		
		var templateUrl = sdUtilService.getBaseUrl() + 'plugins/html5-common/scripts/directives/dialogs/templates/fileRepoUpload.html',
			linkfx,
			that = this;

		linkfx = function(scope, element, attrs){

			scope.$watch('parentPath', function(newValue, oldValue, scope) {
				scope.repoUploadCtrl.parentPath = newValue;
			});

			scope.$watch('targetDocument', function(newValue, oldValue, scope) {
				scope.repoUploadCtrl.targetDocument = newValue;

				if(scope.repoUploadCtrl.targetDocument){
					scope.repoUploadCtrl.mode="CREATE";
				}
				else{
					scope.repoUploadCtrl.mode="UPDATE";
				}

			});

		};
		
		return {
			"controller" : fileRepoUploadController,
			"controllerAs" : "repoUploadCtrl",
			"link" : linkfx,
			"scope": {
				"subTitle" : "@sdaSubTitle",
				"initCallback" : "&sdaOnInit",
				"targetDocument" : "=sdaTargetDocument",
				"fileKey" : "@sdaFileKey",
				"parentPath" : "=sdaParentPath",
				"url" : "@sdaUrl"
			},
			"transclude" : true,
			"templateUrl" : templateUrl
		}
		
	};
	
	
	fileUploadDialog.$inject = ["sdUtilService","$q"];
	
	angular.module("bpm-common.directives")
	.directive("sdRepositoryUploadDialog",fileUploadDialog);
	
})();