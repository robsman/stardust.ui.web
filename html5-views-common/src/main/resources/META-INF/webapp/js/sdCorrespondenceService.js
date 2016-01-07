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
 * @author Johnson.Quadras
 */
define([],function(){

	
	/*
	 * 
	 */
	function CorrespondenceService($resource, $q, $http, sdUtilService,sdLoggerService) {
		var trace = sdLoggerService.getLogger('bpm-common.sdCorrespondenceService');
		/**
		 * 
		 */
		this.getAddressBook = function (piOid){
			var url = sdUtilService.getBaseUrl() + "services/rest/portal/process-instances/"+piOid+"/address-book";
			return $resource(url).query().$promise;
		};

		/**
		 * 
		 */
		this.getProcessOidForActivity = function (aiOid){
			var deferred = $q.defer();
			var url = sdUtilService.getBaseUrl() + "services/rest/portal/activity-instances/"+aiOid+"/correspondence-process-instance";
			$resource(url).get().$promise.then(function(result){
				deferred.resolve({piOid :result.oid })
			})
			return deferred.promise;
		};
		/**
		 * 
		 */
		this.resolveMessageContent = function ( piOid, content){
			var deferred = $q.defer();

			var url = sdUtilService.getBaseUrl() + "services/rest/templating";

			var templateConfig = {
					format : "text",
					processOid : piOid,
					template :   content
			};

			var postPromise = $http.post(url, templateConfig, {
				headers : {
					'Content-type' : 'application/json'
				}
			});
			postPromise.success(function(data) {
				trace.log("Success in Resolving Message Content.")
				deferred.resolve(data);
			});
			postPromise.error(function(response, status) {
				trace.error("The request failed with response :-",response," and status code:-", status);
			});
			return deferred.promise;
		};

		/**
		 * 
		 */
		this.resolveMessageTemplate = function ( piOid, path){
			var deferred = $q.defer();

			var url = sdUtilService.getBaseUrl() + "services/rest/templating";

			var templateConfig = {
					format : "text",
					processOid : piOid,
					templateUri : 'repository://'+ path
			};

			var postPromise = $http.post(url, templateConfig, {
				headers : {
					'Content-type' : 'application/json'
				}
			});
			postPromise.success(function(data) {
				trace.log("Success in Resolving Message template.");
				deferred.resolve(data);
			});
			postPromise.error(function(response, status) {
				trace.error("The request failed with response ", response
						, " and status code ", status);
			});
			return deferred.promise;
		};
		/**
		 * 
		 */
		this.resolveAttachmentTemplate = function (item, correspondenceData, outputFolder) {
			var deferred = $q.defer();
			
			var piOid = correspondenceData.piOid;
			
			var url = sdUtilService.getBaseUrl() + "services/rest/templating";
			var extension = item.path.split('.').pop();
			var format = 	getFormat(extension)
			var fileName = item.name;
			if(item.convertToPdf) {
				fileName = fileName.split(".")[0]+".pdf"
			}

			var templateConfig = {
					templateUri:  'repository://'+item.path,   
					format : format, 
					processOid : piOid,
					parameters : {fieldsMetaData: correspondenceData.fieldMetaData},
					output:{name: fileName, path: outputFolder},
					pdf :item.convertToPdf
			};

			var postPromise = $http.post(url, templateConfig, {
				headers : {
					'Content-type' : 'application/json'
				}
			});
			postPromise.success(function(result) {
				trace.log("Success in resolving attachment Template.");
				deferred.resolve({
					documentId : outputFolder + "/"+fileName,
					templateDocumentId : item.path,
					convertToPdf : item.convertToPdf,
					name : fileName
				});
			});

			postPromise.error(function(response, status) {
				trace.error("The request failed with response :- ",response," and status code :- ",status);
			});
			return deferred.promise;
		};

		/**
		 * 
		 */
		this.getFolderInformationByActivityOid = function (aiOid){ 
			var url =  sdUtilService.getBaseUrl() +"services/rest/portal/activity-instances/"+aiOid+"/correspondence-out";
			return $resource(url).get().$promise;
		};

		/**
		 * 
		 */
		this.copyDocumentToCorrespondenceFolder = function ( documentId, folderPath){ 
			var restUrl = sdUtilService.getBaseUrl() +"services/rest/portal/documents/"+documentId+"/copy";
			var requestObj = {
					"targetFolderPath" : folderPath
			};
			var document = $resource(restUrl, {}, {
				copyToFolder : {
					method : 'PUT'
				}
			});
			return document.copyToFolder({}, requestObj).$promise;
		};

		/**
		 * 
		 */
		this.removeAttachment = function ( documentId){ 
			var restUrl = sdUtilService.getBaseUrl() +"services/rest/portal/documents/"+documentId;
			var attachment = $resource(restUrl, {},
					{
				remove : {
					method : 'DELETE'
				}
					});
			return attachment.remove({}, {}).$promise;
		}
		/**
		 * 
		 */
		this.uploadAttachments = function (files, piOid, progressHandlingFunction){
			var deferred = $q.defer();
			var self = this;
			var formData = new FormData();
			for (var i in files) {
				formData.append("file", files[i]);
			}	

			jQuery.ajax({
				url:   sdUtilService.getBaseUrl() +'services/rest/portal/process-instances/'+piOid+'/documents/PROCESS_ATTACHMENTS',  
				type: 'POST',
				xhr: function() {  // Custom XMLHttpRequest
					var myXhr = jQuery.ajaxSettings.xhr();
					if(myXhr.upload){ // Check if upload property exists
						myXhr.upload.addEventListener('progress',progressHandlingFunction, false); // For handling the progress of the upload
					}
					return myXhr;
				},
				//Ajax events
				success: function(data, textStatus, xhr){
					deferred.resolve(data);
				},
				error: function(xhr, textStatus){
					trace.log("Failure in uploading files");
					deferred.reject(xhr);
				},
				// Form data
				data: formData,
				//Options to tell jQuery not to process data or worry about content-type.
				cache: false,
				contentType: false,
				processData: false
			});
			return deferred.promise;
		}
	}

	/**
	 * 
	 */
	function getFormat(extension) {
		var format = extension;
		if(extension =='txt'){
			format = "text";
		}else if( ['html','htm','HTM'].indexOf(extension) > -1 ){
			format = "html";
		}else if(['docx','DOCX'].indexOf(extension) > -1){
			format = "docx";
		}
		else if(['xml','XML'].indexOf(extension) > -1){
			format = "xml";
		}
		return format;
	}

	//Dependency injection array for our controller.
	CorrespondenceService.$inject = ['$resource','$q','$http','sdUtilService','sdLoggerService'];

	//Require capable return object to allow our angular code to be initialized
	//from a require-js injection system.
	return {
		init: function(angular,appName){
			angular.module(appName)
			.service("sdCorrespondenceService", CorrespondenceService);
		}
	};
});