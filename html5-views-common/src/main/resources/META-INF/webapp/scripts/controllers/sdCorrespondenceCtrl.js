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
define(["html5-views-common/scripts/utils/base64" ],function(base64){

	var _q;
	var trace;
	var _filter;
	var _sdDialogService;
	var _sdCorrespondenceService;
	var _timeout ;
	var interaction = null;
	var _http = null;
	
	var filesToUpload = [];
	
	var buttons = {
			confirm : '',
			cancel :''	
	}
	
	/*
	 * 
	 */
	function CorrespondenceCtrl($scope, $q ,$http , $filter,$timeout,sdDialogService, sdCorrespondenceService) {
		
		
		
		this.readOnly = false;
		_q = $q;
		_filter =$filter; 
		_sdDialogService = sdDialogService;
		_sdCorrespondenceService = sdCorrespondenceService;
		_timeout = $timeout;
		_http = $http;
		
		this.intialize($scope);
		this.exposeApis($scope);
	}
	
	
	// initialize
	 CorrespondenceCtrl.prototype.initializeFileUploader = function() {
		 var self = this;
		var fileselect = jQuery("#fileselect")[0],
			filedrag = jQuery("#filedrag")[0];
		// file select
		fileselect.addEventListener("change", FileSelectHandler, false);
		self.dragDropAvailable = false;
		// is XHR2 available?
		var xhr = new XMLHttpRequest();
		if (xhr.upload) {
			filedrag.addEventListener("dragleave", FileDragHover, false);
			filedrag.addEventListener("dragenter", FileDragHover, false);
			filedrag.addEventListener("drop", FileSelectHandler, false);
			filedrag.addEventListener("dragover", FileSelectHandler, false);
			
			filedrag.style.display = "block";
			self.dragDropAvailable = true;
		}
	}
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.intialize = function($scope){
		var self = this;
		this.correspondenceTypes = [{
										label : 'Email',
										id : 'email'
									}, {
										label : 'Print',
										id : 'print'
									}];
		this.selected = {
				type  : 'email', // print / email
				showBcc : false,
				showCc : false,
				to: [],
				bcc:[],
				cc:[],
				message : '',
				subject : ' ',
				templateId : '',
				attachments : [],
				aiOid : '',
		};
		
		
		this.dialog ={
				selectedAddresses : [],
				selectedAttachments : [],
				selectedTemplates : []
		}
		
		this.enteredAdd = {
				to  : '', 
				bcc : '',
				cc : ''
		};

				
		this.data = {
			like : {
				to : [],
				bcc : [],
				cc : []
			},
			matchVal : {
				to : "",
				bcc : "",
				cc : ""
			}
		};
		
		this.filter = {
				address : '',
				showFax : false
		};
		

		this.addressTable = null;
		this.selectedAddresses = [];
		this.addressEntries= [];
		
		this.i18n = $scope.$root.i18n; 
		$scope = this.i18n;
	
		this.interactionProvider = new bpm.portal.Interaction();
		this.selected.aiOid = this.getActivityOid();
		this.loadAddressBook();
		
		
		buttons.confirm =  this.i18n('views-common-messages.common-OK', 'OK');
		buttons.cancel = this.i18n('views-common-messages.common-Cancel', 'Cancel');
		// call initialization file
		if (window.File && window.FileList && window.FileReader) {
			this.initializeFileUploader();
		}
		
	};
	
	
	function  uploadAttachments () {
		var deferred = _q.defer();
		var self = this;
		var formData = new FormData();
		for (var i in filesToUpload) {
			formData.append("file", filesToUpload[i]);
		}	
		
		jQuery.ajax({
			url: '/Ipp2/services/rest/portal/process-instances/1047/documents/PROCESS_ATTACHMENTS',  //Server script to process data
			type: 'POST',
			xhr: function() {  // Custom XMLHttpRequest
				var myXhr = jQuery.ajaxSettings.xhr();
				if(myXhr.upload){ // Check if upload property exists
					myXhr.upload.addEventListener('progress',self.progressHandlingFunction, false); // For handling the progress of the upload
				}
				return myXhr;
			},
			//Ajax events
			success: function(data, textStatus, xhr){
				deferred.resolve(data);
			},
			error: function(xhr, textStatus){
				console.log("Failure in uploading files");
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
	
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.exposeApis = function( $scope ){ 
		
		var self = this;
		
		CorrespondenceCtrl.prototype.showBcc = showBcc;
		CorrespondenceCtrl.prototype.showCc = showCc;
		CorrespondenceCtrl.prototype.complete = complete;
		CorrespondenceCtrl.prototype.cancel = cancel;

		
		CorrespondenceCtrl.prototype.getAddressesAutoCompleteBCC = getAddressesAutoCompleteBCC;
		CorrespondenceCtrl.prototype.getAddressesAutoCompleteCC = getAddressesAutoCompleteCC;
		CorrespondenceCtrl.prototype.getAddressesAutoCompleteTO = getAddressesAutoCompleteTO;
		
		CorrespondenceCtrl.prototype.newItemFactory = newItemFactory;
		CorrespondenceCtrl.prototype.getAvailableAddresses = getFilteredAddresses;
		CorrespondenceCtrl.prototype.addressIconMapper = tagPreMapper;
		CorrespondenceCtrl.prototype.onAddressSelection = onAddressSelection;
		CorrespondenceCtrl.prototype.removeAddress = removeAddress;
		CorrespondenceCtrl.prototype.addAddress = addAddress;
		
		CorrespondenceCtrl.prototype.openAddressSelector = openAddressSelector;
		CorrespondenceCtrl.prototype.openMessageTemplateSelector = openMessageTemplateSelector
		CorrespondenceCtrl.prototype.openAttachmentSelector = openAttachmentSelector;
		
		CorrespondenceCtrl.prototype.loadMessageFromTemplate = loadMessageFromTemplate
		CorrespondenceCtrl.prototype.loadAttachments = loadAttachments
		CorrespondenceCtrl.prototype.removeAttachment= removeAttachment;
		
		
		self.getScope= function(){
			return $scope;
		}
	
		CorrespondenceCtrl.prototype.addToUploadQ = addFilesToUploadQ;

		this.uploadLocalFilesToServer = function(){
			
			uploadAttachments().then(function(data){
				angular.forEach(data,function(item){
					self.loadAttachments({name : item.name});
				});
				clearUploadQ();
			});
		}
	}
	
	CorrespondenceCtrl.prototype.templateFolderInit = function(api){
		this.templateFolderAPI = api;
	}
	
	CorrespondenceCtrl.prototype.onTemplateSelection = function(item){
		this.selected.templateId = item;
	}
	

	
	CorrespondenceCtrl.prototype.getActivityOid = function(){ 
	/*	var uri = this.interactionProvider.getInteractionUri();
		var endcoded = uri.split('/').pop();
		var b64 = base64.get();
		var decodedId = b64.decode(endcoded);
		var partsMatcher = new RegExp('^(\\d+)\\|(\\d+)$');
		var decodedParts = partsMatcher.exec(decodedId);
		var activityInstanceOid = decodedParts[1];
		console.log('Activity Oid : '+activityInstanceOid)*/
		//return activityInstanceOid
		return 1100;
	}
	
	function addFilesToUploadQ(files) {
		var self = this;
		console.log('addFilesToUploadQ');
		for (var i = 0, f; f = files[i]; i++) {
			//Avoiding duplicates
			var found = _filter('filter')(filesToUpload,{name:f.name},true);
			if(found && found.length < 1){
				filesToUpload.push(f);
				showFileToBeUploaded(f);
			}
		}
	}
	
	
	function getAddressesAutoCompleteBCC(filterCriteria, resultHolder){
		var self = this;
		self.filter.address = filterCriteria;
		self.filter.showFax = true;
		self.data.like.bcc = self.getAvailableAddresses();
	};

	function getAddressesAutoCompleteCC (filterCriteria, resultHolder){
		var self = this;
		self.filter.address = filterCriteria;
		self.filter.showFax = true;
		self.data.like.cc = self.getAvailableAddresses();
	};


	function getAddressesAutoCompleteTO(filterCriteria, resultHolder){
		var self = this;
		self.filter.address = filterCriteria;
		self.filter.showFax = true;
		self.data.like.to= self.getAvailableAddresses();
	};

	function newItemFactory(val){
		var newEntry = {
				name : val,
				value : val
		};

		return newEntry;
	}

	function getFilteredAddresses(){
		var self = this;
		var faxFilter = self.addressEntries;
		if(!self.filter.showFax) {
			faxFilter = _filter('filter')(self.addressEntries, {type : 'email'},true);
		}
		_timeout(function() {
			self.loadPreSelections();
		});
		return  _filter('filter')(faxFilter, {name : self.filter.address},false);
	};
	
	

	CorrespondenceCtrl.prototype.loadPreSelections = function(){
		var self =this;
		
		var selections = [];
		angular.forEach(self.dialog.selectedAddresses,function(data){
			selections.push( {name: data.name})
		});
		if(self.addressTable){
			self.addressTable.setSelection(selections)
		}
	} 

	/*
	 * 
	 */
	function tagPreMapper(item, index) {
		var tagClass = "glyphicon glyphicon-envelope"
			return tagClass;
	};

	/**
	 * 
	 */
	function onAddressSelection(info) {
		
		var self = this;
		if (info.action == "select") {
				//self.selectedAddresses.push(info.current)
				var found = _filter('filter')(self.dialog.selectedAddresses,{name :info.current.name },true);
				if(found && found == 0){
					self.dialog.selectedAddresses.push(info.current)	
				}
		} else {
			var index = self.dialog.selectedAddresses.indexOf(info.current);
			//self.selectedAddresses.splice(index,1)
			self.dialog.selectedAddresses.splice(index,1)
		}
	};

	/**
	 * 
	 */
	function addAddress(source,destination) {

		angular.forEach(source,function(data){
			var found = _filter('filter')(destination,{name : data.name},true);
			if(found && found.length < 1){
				destination.push(data);
			}
		});
	};	

	/**
	 * 
	 */
	function removeAddress (addressType, data) {
		var desination = null;
		var self = this;
		if (addressType == 'TO') { 
			desination = self.selected.to
		}else if (addressType == 'CC') { 
			desination = self.selected.cc
		}
		else if (addressType == 'BCC') { 
			desination = self.selected.bcc
		}
		var index = desination.indexOf(data);
		desination.splice(index,1 );
	};	


	/**
	 * 
	 */
	function loadMessageFromTemplate( documentId) {
		//Call rest service and load the message 
		var self = this;
		self.selected.message = "Message from template"+new Date().getTime();

	};	

	/*
	 * 
	 */
	function openAddressSelector (addressType){
			
		var self = this;
		self.addressReady = true;
		self.selectedAddresses = [];
		self.dialog.selectedAddresses = [];
		self.filter.address = "";
		var title = "Select Recipients";
		var html = '<div style="padding-bottom:10px;">'+ 
							'<span ng-repeat = "opt in ctrl.dialog.selectedAddresses" class="spacing-right "> '+
								'<span class="selected_address" ng-click="ctrl.removeDialogAddress(opt)">'+
									'<i class="glyphicon glyphicon-envelope spacing-right"></i> {{opt.name}}'+
									 '<i class="glyphicon glyphicon-remove"></i>'+
								'</span>'+
							'</span >'+
						'</div>'+
						'<i class="glyphicon glyphicon-search"> </i>	<input type="text"  class="spacing-right"  ng-model="ctrl.filter.address" ng-change="ctrl.addressTable.refresh();"/>'+
							'<input class="correspondence_addressBook_fax_control" type="checkbox" ng-model="ctrl.filter.showFax" ng-change="ctrl.addressTable.refresh();"/> <span class="iceOutLbl">Fax </span>'+
					   '<div class="correspondence_addressBook_conatiner">'+
							'<table sd-data-table="ctrl.addressTable" sda-selection="ctrl.selectedAddresses" '+
								' sd-data="ctrl.getAvailableAddresses();" sda-mode="local" '+
								' sda-selectable="multiple" sda-no-pagination="true"  sda-page-size="{{ctrl.addressEnteries.length}}"  sda-ready="ctrl.addressReady" sda-on-select="ctrl.onAddressSelection(info);"> '+
								' <thead ng-show="{{false}}">'+
									'<tr>'+
										'<th sda-label="Name"></th>'+
										'<th sda-label="Data name"></th>'+
									'</tr>'+
								' </thead>'+
								' <tbody>'+
									'<tr>'+
										'<td>{{rowData.value}}</td>'+
										'<td>{{rowData.name}}</td>'+
									'</tr>'+
								' </tbody>'+
							' </table>'+
					  '</div> ';
				
		var options = {
				title : title,
				type : 'confirm',
				confirmActionLabel : buttons.confirm,
			    cancelActionLabel :  buttons.cancel,
				width : '500px',
				onConfirm : function() {
					if(addressType == 'TO'){
						self.addAddress(self.dialog.selectedAddresses, self.selected.to)
					}else if(addressType == 'CC'){
						self.addAddress(self.dialog.selectedAddresses, self.selected.cc)
					}else if(addressType == 'BCC'){
						self.addAddress(self.dialog.selectedAddresses, self.selected.bcc)
					}
				}
		};
		_sdDialogService.dialog(self.getScope(), options, html)
	};
		

	/**
	 * 
	 */
	function cancel() {

	};	
	/**
	 * 
	 */
	function complete() {

	};

	/**
	 * 
	 */
	function showCc () {
		this.selected.showCc = true;
	};

	/**
	 * 
	 */
	function showBcc() {
		this.selected.showBcc = true;
	};
	
	
	/**
	 * 
	 */
	function clearUploadQ() {
		var m = jQuery("#messages")[0];
		m.innerHTML ='';
		jQuery("#uploadAttachmentForm")[0].reset();
		
		if(filesToUpload.length){
			filesToUpload.length = 0;
		}
		
	}

	/**
	 * 
	 */
	function FileDragHover(e) {
		e.stopPropagation();
		e.preventDefault();
		e.target.className = (e.type == "dragover" ? "hover" : "");
	}


	/**
	 * 
	 */
	function FileSelectHandler(e) {
		// cancel event and hover styling
		FileDragHover(e);
		var files = e.target.files || e.dataTransfer.files;
		CorrespondenceCtrl.prototype.addToUploadQ(files);
	}


	 /**
	  * 
	  */
	function showFileToBeUploaded(file) {
		var m = jQuery("#messages");
		var msg = 
			"<p style='width:250px'>File Name: <strong>" + file.name +"</strong></p>";
		m.html( msg + m.html());
	}
	
	
	/**
	 * 
	 */
	 function openMessageTemplateSelector() {
			var self = this;
			var title = "Select Template";
			
			var html = '<i class="glyphicon glyphicon-search"> </i>	<input type="text"  class="spacing-right"  ng-model="ctrl.filter.template" />'+
						'<div sd-folder-tree'+
					        ' sda-on-init="ctrl.templateFolderInit(api)"'+
					         'sda-multiselect="false"'+
					         'sda-event-callback="ctrl.onTemplateSelection(item)"'+
					         'sda-root-path="documents/correspondence-templates"></div>'+
					      '</div>';
			var options = {
					confirmActionLabel : buttons.confirm,
				    cancelActionLabel :  buttons.cancel,
					title : title,
					type : 'confirm',
					onConfirm : function() {
						self.loadMessageFromTemplate(self.selected.templateId);
					},
					width : '500px'
			};
			_sdDialogService.dialog(self.getScope(), options, html)
	};
	
	

	CorrespondenceCtrl.prototype.onAttachmentFolder = function(api) {
		this.attachmentFolderAPI = api;
	} 
	
	CorrespondenceCtrl.prototype.onAttachmentSelection = function(item) {
		var self = this;
		
	} 
	
	/**
	 * 
	 */
	 function openAttachmentSelector() {
			var self = this;
			var title = "Select Attachments";
			
			var html = '<i class="glyphicon glyphicon-search"> </i>	<input type="text"  class="spacing-right"  ng-model="ctrl.filter.attachment" /> '+
						'<div sd-process-document-tree '+
								'sda-process-oid="1047" '+
						         'sda-on-init="ctrl.onAttachmentFolder(api)" '+
						         'sda-multiselect="true" '+
						         'sda-event-callback="ctrl.onAttachmentSelection(item)"> '+
						'</div>';
			var options = {
					confirmActionLabel : buttons.confirm,
				    cancelActionLabel :  buttons.cancel,
					title : title,
					type : 'confirm',
					width : '500px',
					onConfirm : function() {
						self.loadAttachments(attachment);
						
					}
			};
			_sdDialogService.dialog(self.getScope(), options, html)
	};
	
	/**
	 * 
	 */
	function loadAttachments(attachment) {
		var self = this;
		//TODO Run this through the Templating API
		self.selected.attachments.push(attachment);
	}
	
	/**
	 * 
	 */
	function removeAttachment(attachment) {
		var self = this;
		var index = self.selected.attachments.indexOf(attachment);
		self.selected.attachments.splice(index,1);
	}
	
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.removeDialogAddress = function(address) {
		var self = this;
		var info = {
			action : 'deselect',
			current : address
		}
	
		var selections = [];
		
		angular.forEach( self.addressTable.getSelection(),function(data){
			if(data.name != address.name ) {
			 selections.push({name:data.name});
			}
		});
		
		self.addressTable.setSelection(selections);
		self.onAddressSelection(info);
	}
	
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.loadAddressBook =function() {
		var self  = this;
		_sdCorrespondenceService.getAddressBook(self.selected.aiOid).then(function(data){
			self.addressEntries = data
		});
	};
	
	
	//Dependency injection array for our controller.
	CorrespondenceCtrl.$inject = ['$scope','$q', '$http','$filter','$timeout','sdDialogService','sdCorrespondenceService'];
	
	//Require capable return object to allow our angular code to be initialized
	//from a require-js injection system.
	return {
		init: function(angular,appName){
			this.interaction = interaction;
			angular.module(appName)
			.controller("sdCorrespondenceCtrl", CorrespondenceCtrl);
		}
	};
	

});