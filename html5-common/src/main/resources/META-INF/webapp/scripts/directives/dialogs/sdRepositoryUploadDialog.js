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

	//dependency array
	fileRepoUploadController.$inject = ["$scope","sdUtilService","$q","$timeout","sdLoggerService","sdViewUtilService","sdI18nService"];

	/**
	 * Controller function for our directive
	 */
	function fileRepoUploadController($scope,sdUtilService, $q, $timeout, sdLoggerService, sdViewUtilService, sdI18nService){

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
		this.sdUtilService = sdUtilService;
		this.sdViewUtilService = sdViewUtilService;
		this.parentPath="/";
		this.$scope = $scope;
		this.$timeout = $timeout;
		this.$q = $q;
		this.state = "initial";
		this.nonFileData = {};//data to send with the file upload, key->values
		this.fileDefer = {};
		this.id = "sdFileUploadDialog_" + $scope.$id;
		this.openOnComplete = false;
		this.trace = sdLoggerService.getLogger('bpm-common.directives.sdFileUploadController');
		this.versCollapse = true;
		this.descrCollapse = true;
		this.i18n = sdI18nService.getInstance('views-common-messages');

		this.textMap = this.getTextMap(this.i18n);

		if(this.mode === "UPDATE"){
			this.title = this.textMap.uploadNewVersion;
		}
		else if(this.mode==="EXPLODE"){
			this.title = this.textMap.uploadZipArchive;
		}
		else{
			this.title = this.textMap.fileUpload;
		}

		//Function has to be on scope so we can call it
		//from non angular environment as ng-change does not
		//work for file input types (see fileUpload.Html template script).
		$scope.fileNameChanged = function(elem){
			$timeout(function(){

				var file,
					hasDuplicate;

				that.state = "fileSelected";
				that.files = elem.files;

				//In update or explode mode we only allow one curated file
				if(that.$scope.repoUploadCtrl.mode==="UPDATE" || 
				   that.$scope.repoUploadCtrl.mode==="EXPLODE"){
					while(that.curatedFiles.pop());
				};

				for (var i = 0;i<that.files.length;i++) {
					file=that.files[i];
					that.totalSize += file.size;

					hasDuplicate = that.curatedFiles.some(function(f){
						return f.name === file.name;
					});

					if(!hasDuplicate && that.isFile(file)){
						
						that.curatedFiles.push({
							"comments" : "",
							"description" : "",
							"schemaLocation" : "",
							"documentTypeId" : "",
							"fileState" : FileState.BASE,
							"send" : true,
							"name" : file.name,
							"type" : file.type,
							"size" : file.size,
							"percentUploaded" : 0, 
							"blob" : file
						});

					}//duplicate check end

				};//for end

				//if we filtered out all of our files then reset to initial state
				if(that.curatedFiles.length===0){
					that.state = "initial";
				}

			},0);//timeout invoke end
		}
		
		//Execute in a timeout so we allow the sdDialog in our template to be compiled
		//and have time to initialize the api object tagged to its attribute.
		$timeout(function(){
			var customApi = {
					"close" : that.uploadDialog.close,
					"open" : function(stagedFiles){
						that.resetState();
						if(stagedFiles && stagedFiles.length > 0){
							$scope.fileNameChanged({"files" : stagedFiles});
						};
						that.fileDefer = $q.defer();
						that.uploadDialog.open();
						return that.fileDefer.promise;
					}
			}
			$scope.dialogApi = that.uploadDialog;
			$scope.initCallback({api : customApi});
		},0);
	}
	
	fileRepoUploadController.prototype.getTextMap = function(i18n){

		var textMap = {};

		textMap.multiFileError = i18n.translate("views.genericRepositoryView.uploadDialog.multipleFileError ");
		textMap.zipOnlyError = i18n.translate("views.genericRepositoryView.uploadDialog.zipOnlyError");
		textMap.folderMessageUpload = i18n.translate("views.genericRepositoryView.uploadDialog.folderMessage.upload");
		textMap.folderMessageVersion = i18n.translate("views.genericRepositoryView.uploadDialog.folderMessage.version");
		textMap.folderMessageZip = i18n.translate("views.genericRepositoryView.uploadDialog.folderMessage.zip");
		textMap.versionComments = i18n.translate("fileUpload.comment.label");
		textMap.documentTypes = i18n.translate("fileUpload.documentTypes.label");
		textMap.description = i18n.translate("fileUpload.description.label");
		textMap.openDocument = i18n.translate("fileUpload.openDocument.label");
		textMap.close = i18n.translate("common.close");
		textMap.uploadNewVersion = i18n.translate("views.myDocumentsTreeView.documentTree.uploadNewVersion");
		textMap.uploadFile = i18n.translate("views.genericRepositoryView.treeMenuItem.uploadFile");
		textMap.fileUpload = i18n.translate("views.myDocumentsTreeView.fileUploadDialog.fileUpload");
		textMap.upload = i18n.translate("views.myDocumentsTreeView.fileUploadDialog.upload");
		textMap.uploadZipArchive = i18n.translate("views.myDocumentsTreeView.fileUploadDialog.uploadZipArchive");
		textMap.overwrite = i18n.translate("fileUpload.zip.overwrite");
		textMap.documentType = i18n.translate("fileUpload.documentTypes.label");

		return textMap;

	};

	/**
	 * Make a best guess as to whether a file is a actually a folder.
	 * We expect folders will have no file type and be a multiple of 
	 * 4096 bytes. This is definitely not foolproof as a folder named
	 * test.jpg will report a type. Also, files that have no file type and
	 * are a multiple of 4096 bytes in size will look just like a folder to
	 * this algorithm.
	 * @param  {[type]}  file [description]
	 * @return {Boolean}      [description]
	 */
	fileRepoUploadController.prototype.isFile = function(file){
		var result =(file.type || file.size%4096 != 0);
		return result;
	};

	fileRepoUploadController.prototype.isZip = function(file){
		return file.type == "application/x-compressed" ||
			   file.type == "application/x-compress" ||
			   file.type == "application/x-zip-compressed" ||
			   file.type == "application/zip" ||
			   file.type == "multipart/x-zip";
	};

	fileRepoUploadController.prototype.openDocumentView = function(docId){

	    var params = {"documentId" : docId};
	    var viewKey = 'documentOID=' + encodeURIComponent(docId);

	    viewKey = window.btoa(viewKey);
	    this.sdViewUtilService.openView("documentView",viewKey,params,false);
  	};

	//handle file drops and shunt them through the same folder
	fileRepoUploadController.prototype.onFileDrop = function(data,e){

		var hasError = false,
			that = this;

		this.$scope.errorMessage = "";

		//Version update and zip import only allows a single file
		if(this.$scope.repoUploadCtrl.mode==="UPDATE" || this.$scope.repoUploadCtrl.mode==="EXPLODE"){
			while(this.curatedFiles.pop());

			if(e.dataTransfer.files.length > 1){
				this.$scope.errorMessage = this.textMap.multiFileError;
				hasError = true;
			}

			if(!this.isZip(e.dataTransfer.files.item(0)) && this.$scope.repoUploadCtrl.mode==="EXPLODE"){
				this.$scope.errorMessage = this.textMap.zipOnlyError;
				hasError = true;
			}

			if(hasError){
				//force a digest cycle so our error message displays
				this.$timeout(function(){},0);
				return;
			}


		};

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
		this.$scope.errorMessage = "";
		this.nonFileData = {};
		this.fileDefer = {};
		this.comments = "";
		this.documentType = null;
		this.description = "";
	};
	
	/**
	 * Closes a dialog and resolves promise;
	 */
	fileRepoUploadController.prototype.closeDialog = function(){
		var that = this;
		this.uploadDialog.close();
		if(this.fileDefer && this.fileDefer.resolve){
			this.fileDefer.resolve(this.responseFiles);
			if(this.openOnComplete===true){
				this.responseFiles.forEach(function(rFile){
					that.openDocumentView(rFile.uuid);
				});
			};
		}
		this.resetState();
	};

	fileRepoUploadController.prototype.uploadFile = function(){

		var fileKey = this.$scope.fileKey,
			that = this,
			promises=[],
			modelId,
			invocations=[];
			
		
		this.curatedFiles.forEach(function(file){
			if(file.send===true){
				var filePromise,
					deferred,
					nonFileData;

				deferred = that.$q.defer();
				filePromise = deferred.promise;
				nonFileData = angular.extend({}, file);

				//clean up our nonFileData to remove props inherited from the file object
				//which we don't need. These are just extra form data vars we will send,
				//the file obj will keep all the local important data itself.
				delete nonFileData.blob;
				delete nonFileData.fileState;
				delete nonFileData.send;
				delete nonFileData.name;
				delete nonFileData.type;
				delete nonFileData.size;
				delete nonFileData.percentUploaded;

				//add parent path from our inherited scoped context, be sure to
				//remove double slashes as engine does not like that CRNT-39783
				if(that.parentPath){
				  nonFileData.parentFolderPath = that.parentPath.replace("//","/");  
				}

				//add data from our dialog UI, these are global for all files
				if(that.documentType){
					nonFileData.schemaLocation = that.documentType.schemaLocation;
					nonFileData.documentTypeId = that.documentType.documentTypeId;
				}
				else{
					delete nonFileData.schemaLocation;
					delete nonFileData.documentTypeId;
				}

				if(that.description){
					nonFileData.description = that.description;
				}
				else{
					delete nonFileData.description;
				}

				if(that.comments){
					nonFileData.comments = that.comments;
				}
				else{
					delete nonFileData.comments;
				}

				promises.push(filePromise);


				invocations.push([file,nonFileData,fileKey,deferred]);

				filePromise.then(function(file){
					file.fileState = FileState.COMPLETE;
				})
				["catch"](function(file){
					file.fileState = FileState.ERROR;
				});
			}

		});

		this.state = "progress";

		this.$q.all(promises).then(function(data){
			var result,
				failed = false;

			data.forEach(function(result){

				//New version uploads dont return a full result object
				if(result.failures && result.documents){
					if(result.failures.length>0){
						failed = true;
					}
					else{
						that.responseFiles.push(result.documents[0]);
					}
				}
				else if(that.mode==="UPDATE"){
					that.responseFiles.push(result);
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
			    result = JSON.parse(src.response || src.responseText),
			    expUrl;

			//integer division -> any 2XX status = success
			if((status/100 | 0)===2){ 
				if(result.failures && result.failures.length > 0){
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

		//Upload new file(s)
		if(that.$scope.repoUploadCtrl.mode==="CREATE"){
        	xhr.open("POST", that.$scope.url, true);
    	}
    	//upload a new version of an existing file.
    	else if(that.$scope.repoUploadCtrl.mode==="UPDATE"){
    		xhr.open("PUT", that.$scope.url + "/" + that.$scope.repoUploadCtrl.targetDocument, true);
    	}
    	//Upload zip file
    	else if(that.$scope.repoUploadCtrl.mode==="EXPLODE"){
    		
    		var expUrl = that.sdUtilService.getRootUrl() + 
    					 "/services/rest/portal/folders/import/" +
    					 that.$scope.repoUploadCtrl.targetDocument;
    		
    		var verb = (that.$scope.repoUploadCtrl.overwriteFiles===true)?"POST":"PUT";

    		xhr.open(verb, expUrl, true);
    	}

		xhr.send(fd);

	};
	
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

			scope.$watchCollection('documentTypes', function(newValue, oldValue, scope) {
				scope.repoUploadCtrl.documentTypes = newValue;
			});

			//Watch doExplode and examine in combination with targetDocument
			//to determine if we are in explode of update mode.
			scope.$watch('doExplode',function(newValue, oldValue, scope){
				scope.repoUploadCtrl.doExplode=newValue;

				if(scope.repoUploadCtrl.targetDocument){
					
					if(scope.repoUploadCtrl.doExplode===true){
						scope.repoUploadCtrl.mode = "EXPLODE";
						scope.repoUploadCtrl.title = "Upload Zip File"
					}
					else{
						scope.repoUploadCtrl.mode="UPDATE";
						scope.repoUploadCtrl.title = "Upload New File Version";
					}
				}
				

			});

			//Watch taget document in combination with doExplode to determine
			//what mode our dialog is in.
			scope.$watch('targetDocument', function(newValue, oldValue, scope) {
				scope.repoUploadCtrl.targetDocument = newValue;

				if(scope.repoUploadCtrl.targetDocument){
					
					if(scope.repoUploadCtrl.doExplode===true){
						scope.repoUploadCtrl.mode = "EXPLODE";
						scope.repoUploadCtrl.title = "Upload Zip File"
					}
					else{
						scope.repoUploadCtrl.mode="UPDATE";
						scope.repoUploadCtrl.title = "Upload New File Version";
					}

				}
				else{
					scope.repoUploadCtrl.mode="CREATE";
					scope.repoUploadCtrl.title = "Upload New File"
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
				"doExplode" : "=sdaExplodeMode",
				"fileKey" : "@sdaFileKey",
				"parentPath" : "=sdaParentPath",
				"documentTypes" : "=sdaDocumentTypes",
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