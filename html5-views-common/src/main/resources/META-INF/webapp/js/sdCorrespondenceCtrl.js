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
define(["html5-views-common/js/lib/base64" ],function(base64){

	var _q;
	var trace;
	var _filter;
	var _sdDialogService;
	var _sdCorrespondenceService;
	var _timeout ;
	var interaction = null;
	var _http = null;

	var filesToUpload = [];
	var VALID_TEMPLATE_FORMATS = ['text/plain' , 'text/html']
	var buttons = {
			confirm : '',
			cancel :''	
	}

	var piOid = null; 

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
		var ctrl = this;
		var fileselect = jQuery("#fileselect")[0],
		filedrag = jQuery("#filedrag")[0];
		// file select
		fileselect.addEventListener("change", FileSelectHandler, false);
		ctrl.dragDropAvailable = false;
		// is XHR2 available?
		var xhr = new XMLHttpRequest();
		if (xhr.upload) {
			filedrag.addEventListener("dragleave", FileDragHover, false);
			filedrag.addEventListener("dragenter", FileDragHover, false);
			filedrag.addEventListener("drop", FileDropHandler, false);
			filedrag.addEventListener("dragover", FileDragHover, false);
			filedrag.style.display = "block";
			ctrl.dragDropAvailable = true;
		}
	}
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.intialize = function($scope){
		var ctrl = this;
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
				selectedTemplates : [],
				templateId :'',
				filter : {
					address : {
						value : '',
						showFax : false
					}
				},
				templateSelector : {
					api : null,
					showErorMessage : false
				},
				attachmentSelector : {
					api : null
				}
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


		this.addressTable = null;
		this.selectedAddresses = [];
		this.addressEntries= [];

		this.i18n = $scope.$root.i18n; 
		$scope = this.i18n;


		this.interactionProvider = new bpm.portal.Interaction();
		this.selected.aiOid = this.getActivityOid();
		_sdCorrespondenceService.getProcessOidForActivity(ctrl.selected.aiOid).then(function(result){
			ctrl.selected.piOid = result.piOid;
			console.log(ctrl.selected.piOid);
			piOid = ctrl.selected.piOid;
			ctrl.loadAddressBook();

		});


		this.buttons = {
				confirm : this
				.i18n('views-common-messages.common-OK', 'OK'),
				cancel : this.i18n('views-common-messages.common-Cancel',
				'Cancel')
		}
		// call initialization file
		if (window.File && window.FileList && window.FileReader) {
			this.initializeFileUploader();
		}

		CorrespondenceCtrl.prototype.addAttachment = function(file){
			ctrl.selected.attachments.push(file)
			console.log(ctrl.selected.attachments)
		}; 
		CorrespondenceCtrl.prototype.showTest = function(value){
			ctrl.test =value
		}; 


	};



	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.exposeApis = function( $scope ){ 

		var ctrl = this;

		CorrespondenceCtrl.prototype.loadAttachments = loadAttachments
		CorrespondenceCtrl.prototype.removeAttachment= removeAttachment;


		ctrl.getScope= function(){
			return $scope;
		}

		CorrespondenceCtrl.prototype.addToUploadQ = addFilesToUploadQ;

	}


	function uploadLocalFilesToServer(files) {
		_sdCorrespondenceService.uploadAttachments(files, piOid).then(function(data){
			console.log("File upload response");
			console.log(data);
			
			if(data.documents.length > 0) {
				angular.forEach(data.documents,function(item){
					CorrespondenceCtrl.prototype.addAttachment({name : item.name});
				});
			}

			if(data.failure) {
				console.log("Failed in uploadig documents")
				console.log(data.failure)
			}

			clearUploadQ();
		});
	}



	CorrespondenceCtrl.prototype.getActivityOid = function(){ 
		/*	var uri = this.interactionProvider.getInteractionUri();
		var endcoded = uri.split('/').pop();
		var b64 = base64.get();
		var decodedId = b64.decode(endcoded);
		var partsMatcher = new RegExp('^(\\d+)\\|(\\d+)$');
		var decodedParts = partsMatcher.exec(decodedId);
		var activityInstanceOid = decodedParts[1];
		console.log('Activity Oid  *****: '+activityInstanceOid)
		return activityInstanceOid;*/
		return 1208;
	}

	function addFilesToUploadQ(files) {
		var ctrl = this;

		console.log('addFilesToUploadQ');
		var filesToUpload =[];
		for (var i = 0, f; f = files[i]; i++) {
			//Avoiding duplicates
			var found = _filter('filter')(filesToUpload,{name:f.name},true);
			if(found && found.length < 1){
				filesToUpload.push(f);
			}
		}
		if(filesToUpload.length > 0){
			uploadLocalFilesToServer(filesToUpload);
		}
		filesToUpload = [];
	}


	/**
	 * 
	 */
	function clearUploadQ() {
		jQuery("#uploadAttachmentForm")[0].reset();
		jQuery(".correspondence_file_uploading").hide()
		/*if(filesToUpload.length){
			filesToUpload.length = 0;
		}*/
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
		console.log(files)
	}


	/**
	 * 
	 */
	function FileDropHandler(e) {
		// cancel event and hover styling
		FileDragHover(e);
		var files = e.target.files || e.dataTransfer.files;
		console.log(files);
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



	/***
	 * Address Selection
	 */

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.getAddressesAutoCompleteBCC = function (filterCriteria, resultHolder){
		var ctrl = this;
		ctrl.dialog.filter.address.value = filterCriteria;
		ctrl.data.like.bcc = ctrl.getAvailableAddresses();
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.getAddressesAutoCompleteCC = function(filterCriteria, resultHolder){
		var ctrl = this;
		ctrl.dialog.filter.address.value = filterCriteria;
		ctrl.data.like.cc = ctrl.getAvailableAddresses();
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.getAddressesAutoCompleteTO = function (filterCriteria, resultHolder){
		var ctrl = this;
		ctrl.dialog.filter.address.value = filterCriteria;
		ctrl.data.like.to= ctrl.getAvailableAddresses();
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.newItemFactory = function (val){
		var newEntry = {
				name : val,
				value : val
		};

		return newEntry;
	}
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.getAvailableAddresses  = function(){
		var ctrl = this;
		var faxFilter = ctrl.addressEntries;
		if(! ctrl.dialog.filter.address.showFax) {
			faxFilter = _filter('filter')(ctrl.addressEntries, {type : 'email'},true);
		}
		_timeout(function() {
			ctrl.loadPreSelections();
		});
		return  _filter('filter')(faxFilter, {name : ctrl.dialog.filter.address.value},false);
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.loadPreSelections = function(){
		var ctrl =this;

		var selections = [];
		angular.forEach(ctrl.dialog.selectedAddresses,function(data){
			selections.push( {name: data.name})
		});
		if(ctrl.addressTable){
			ctrl.addressTable.setSelection(selections)
		}
	} 

	/*
	 * 
	 */
	CorrespondenceCtrl.prototype.addressIconMapper = function(item, index) {
		var tagClass = "glyphicon glyphicon-envelope"
			return tagClass;
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.onAddressSelection = function(info) {

		var ctrl = this;
		if (info.action == "select") {
			//ctrl.selectedAddresses.push(info.current)
			var found = _filter('filter')(ctrl.dialog.selectedAddresses,{name :info.current.name },true);
			if(found && found == 0){
				ctrl.dialog.selectedAddresses.push(info.current)	
			}
		} else {
			var index = ctrl.dialog.selectedAddresses.indexOf(info.current);
			//ctrl.selectedAddresses.splice(index,1)
			ctrl.dialog.selectedAddresses.splice(index,1)
		}
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.addAddress = function(source,destination) {

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
	CorrespondenceCtrl.prototype.removeAddress = function(addressType, data) {
		var desination = null;
		var ctrl = this;
		if (addressType == 'TO') { 
			desination = ctrl.selected.to
		}else if (addressType == 'CC') { 
			desination = ctrl.selected.cc
		}
		else if (addressType == 'BCC') { 
			desination = ctrl.selected.bcc
		}
		var index = desination.indexOf(data);
		desination.splice(index,1 );
	};	


	/*
	 * 
	 */
	CorrespondenceCtrl.prototype.openAddressSelector = function(addressType){

		var ctrl = this;
		ctrl.addressReady = true;
		ctrl.selectedAddresses = [];
		ctrl.dialog.selectedAddresses = [];
		ctrl.dialog.filter.address.value = "";
		var title = "Select Recipients";
		var html = '<div style="padding-bottom:10px;">'+ 
		'<span ng-repeat = "opt in ctrl.dialog.selectedAddresses" class="spacing-right "> '+
		'<span class="selected_address" ng-click="ctrl.removeDialogAddress(opt)">'+
		'<i class="glyphicon glyphicon-envelope spacing-right"></i> {{opt.name}}'+
		'<i class="glyphicon glyphicon-remove"></i>'+
		'</span>'+
		'</span >'+
		'</div>'+
		'<i class="glyphicon glyphicon-search"> </i>	<input type="text"  class="spacing-right"  ng-model="ctrl.dialog.filter.address.value" ng-change="ctrl.addressTable.refresh();"/>'+
		'<input class="correspondence_addressBook_fax_control" type="checkbox" ng-model="ctrl.dialog.filter.address.showFax" ng-change="ctrl.addressTable.refresh();"/>'+
		'<span class="iceOutLbl">Fax </span>'+
		'<div class="correspondence_addressBook_conatiner">'+
		'<table sd-data-table="ctrl.addressTable" sda-selection="ctrl.selectedAddresses" '+
		' sd-data="ctrl.getAvailableAddresses();" sda-mode="local" '+
		' sda-selectable="multiple" sda-no-pagination="true"  sda-page-size="{{ctrl.addressEnteries.length}}"  sda-ready="ctrl.addressReady" sda-on-select="ctrl.onAddressSelection(info);"> '+
		' <thead>'+
		'<tr>'+
		'<th sda-label="Value"></th>'+
		'<th sda-label="Name"></th>'+
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
				confirmActionLabel : this.buttons.confirm,
				cancelActionLabel :  this.buttons.cancel,
				width : '500px',
				onConfirm : function() {
					if(addressType == 'TO'){
						ctrl.addAddress(ctrl.dialog.selectedAddresses, ctrl.selected.to)
					}else if(addressType == 'CC'){
						ctrl.addAddress(ctrl.dialog.selectedAddresses, ctrl.selected.cc)
					}else if(addressType == 'BCC'){
						ctrl.addAddress(ctrl.dialog.selectedAddresses, ctrl.selected.bcc)
					}
				}
		};
		_sdDialogService.dialog(ctrl.getScope(), options, html)
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.showCc = function() {
		this.selected.showCc = true;
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.showBcc = function () {
		this.selected.showBcc = true;
	};


	/**
	 * Message Template Selector
	 */

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.openMessageTemplateSelector = function() {
		var ctrl = this;
		ctrl.templateSelectorDialog.open();
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.isTemplateSelectionInvalid = function(){
		var ctrl = this;
		var api = ctrl.dialog.templateSelector.api;
		if(!api){
			return true;
		}

		if (api.getSelectedNodes().length < 1) {
			return true;
		} else {
			var format = api.getSelectedNodes()[0].valueItem.contentType;
			if(VALID_TEMPLATE_FORMATS.indexOf(format) == -1){
				ctrl.dialog.templateSelector.showErorMessage = true;
				return true;
			}
		}
		ctrl.dialog.templateSelector.showErorMessage = false;
		return false;
	}

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.templateFolderInit = function(api){
		this.dialog.templateSelector.api = api;
	}

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.onTemplateSelection = function(){
		var ctrl = this;
		var api = ctrl.dialog.templateSelector.api;
		console.log("Selected document")
		console.log(api.getSelectedNodes()[0].valueItem);
		ctrl.templateSelectorDialog.close();
		_sdCorrespondenceService.resolveTemplate().then(function(data) {
			ctrl.oldMessage = data.result;
			ctrl.selected.message = data.result;
		});
	}

	/**
	 * Attachment Selector
	 */

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.openAttachmentSelector = function () {
		var ctrl = this;
		ctrl.attachmentSelectorDialog.open();

	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.onAttachmentFolder = function(api) {
		this.dialog.attachmentSelector.api = api;
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.onAttachmentSelection = function() {
		var ctrl = this;
		var processApi = ctrl.dialog.attachmentSelector.api;
		var messageTemplateApi = ctrl.dialog.templateSelector.api;
		console.log("Selected attachments")
		angular.forEach(processApi.getSelectedNodes(),function(node){
			console.log( node.valueItem );
		});
		angular.forEach(messageTemplateApi.getSelectedNodes(),function(node){
			console.log( node.valueItem );
		});

		ctrl.attachmentSelectorDialog.close();
	}; 

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.isAttachmentSelectionInValid = function(){
		var ctrl = this;
		var processApi = ctrl.dialog.attachmentSelector.api;
		var messageTemplateApi = ctrl.dialog.templateSelector.api;
		if(!processApi && !messageTemplateApi ){
			return true;
		}

		if (processApi.getSelectedNodes().length < 1  && messageTemplateApi.getSelectedNodes().length < 1) {
			return true;
		}
		return false;
	};



	/**
	 * 
	 */
	function loadAttachments(attachment) {
		var ctrl = this;
		//TODO Run this through the Templating API
		ctrl.selected.attachments.push(attachment);
	}

	/**
	 * 
	 */
	function removeAttachment(attachment) {
		var ctrl = this;
		var index = ctrl.selected.attachments.indexOf(attachment);
		ctrl.selected.attachments.splice(index,1);
	}

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.removeDialogAddress = function(address) {
		var ctrl = this;
		var info = {
				action : 'deselect',
				current : address
		}

		var selections = [];

		angular.forEach( ctrl.addressTable.getSelection(),function(data){
			if(data.name != address.name ) {
				selections.push({name:data.name});
			}
		});

		ctrl.addressTable.setSelection(selections);
		ctrl.onAddressSelection(info);
	}

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.loadAddressBook =function() {
		var ctrl  = this;
		_sdCorrespondenceService.getAddressBook(ctrl.selected.piOid).then(function(data){
			ctrl.addressEntries = data
		});
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.onMessageChange = function(){
		var ctrl = this;
		if( ctrl.oldMessage != ctrl.selected.message) {
			//TODO change this to use the identifier
			if(ctrl.selected.message.indexOf('$$') > -1) {
				console.log('Run through the templating engine');
			}
		}
		this.oldMessage = this.selected.message;
	}

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