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
	function CorrespondenceService($resource, $q, sdUtilService) {
		
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
			var url = sdUtilService.getBaseUrl() + "services/rest/portal/activity-instances/"+aiOid;
			 $resource(url).get().$promise.then(function(result){
				deferred.resolve({piOid :result.processInstance.oid })
			})
			return deferred.promise;
		};
		
		
		/**
		 * 
		 */
		this.resolveTemplate = function (documentId){
			var deferred = $q.defer();
			deferred.resolve({result : 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum' })
			return deferred.promise;
		};
		
		
		/**
		 * 
		 */
		this.getFolderInformationByActivityOid = function (aiOid){ 
			var url =  sdUtilService.getBaseUrl() +"services/rest/portal/activity-instances/"+aiOid+"/correspondence-out";
			return $resource(url).get().$promise;
		}
		
		
		/**
		 * 
		 */
		this.copyDocumentToCorrespondenceFolder = function ( documentId, folderPath){ 
			var restUrl = sdUtilService.getBaseUrl() +"services/rest/portal/documents/"+documentId+"/copy";
			var requestObj = {
				"parentFolderPath" : folderPath
			};
			var document = $resource(restUrl, {}, {
				copyToFolder : {
					method : 'POST'
				}
			});
			return document.copyToFolder({}, requestObj).$promise;
		}
		
		
		/**
		 * 
		 */
		this.resolveDocumentTemplate = function ( documentId, folderPath){ 
			//TODO Get response from the templating engine.
			var deferred = $q.defer();
				deferred.resolve({name : documentId});
			return deferred.promise;
		}
		
		

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
					console.log("Failure in uploading files");
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
	
	//Dependency injection array for our controller.
	CorrespondenceService.$inject = ['$resource','$q','sdUtilService'];
	
	//Require capable return object to allow our angular code to be initialized
	//from a require-js injection system.
	return {
		init: function(angular,appName){
			angular.module(appName)
			.service("sdCorrespondenceService", CorrespondenceService);
		}
	};
});