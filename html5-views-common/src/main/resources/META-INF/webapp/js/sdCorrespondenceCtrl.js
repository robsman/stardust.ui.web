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
	var _parse = null;
	var _sdViewUtilService = null;
	var trace = null;

	var filesToUpload = [];
	var VALID_TEMPLATE_FORMATS = ['text/plain' , 'text/html'];
	var TEPMPLATING_SUPPORTED_FILE_FORMATS = ["doc","docx","html","htm","txt"]
	var buttons = {
			confirm : '',
			cancel :''	
	}

	var piOid = null; 
	var _scope = null;

	/*
	 * 
	 */
	function CorrespondenceCtrl($scope, $q ,$http , $filter,$timeout, $parse, sdDialogService, sdCorrespondenceService, sdViewUtilService, sdLoggerService, sdPreferenceService) {

		this.readOnly = false;
		_q = $q;
		_filter =$filter; 
		_sdDialogService = sdDialogService;
		_sdCorrespondenceService = sdCorrespondenceService;
		_timeout = $timeout;
		_http = $http;
		_parse = $parse;
		_sdViewUtilService = sdViewUtilService;
		_scope = $scope;
		_sdPreferenceService = sdPreferenceService;
		trace = sdLoggerService.getLogger('html5-views-common-ui.sdCorrespondenceCtrl');
		
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
			label : ctrl.i18n("views-common-messages.views-correspondenceView-details-type-email"),
			id : 'email'
		}, {
			label :  ctrl.i18n("views-common-messages.views-correspondenceView-details-type-print"),
			id : 'print'
		}];
		this.selected = {
				type  : 'print', // print / email
				showBcc : false,
				showCc : false,
				to: [],
				bcc:[],
				cc:[],
				content : '',
				subject : ' ',
				templateId : '',
				attachments : [],
				aiOid : '',
				convertToPdf : false
		};
		
		var preferedCorrespondenceType = this.getDefaultCorrespondenceType();
		

		if (preferedCorrespondenceType) {
			if (preferedCorrespondenceType == 'Print') {
				this.selected.type = 'print'
			} else {
				this.selected.type = 'email';
			}
		}

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

		this.convertToPdf = false;
		this.addressTable = null;
		this.selectedAddresses = [];
		this.addressEntries= [];

		this.i18n = $scope.$root.i18n; 
		$scope = this.i18n;


		this.interactionProvider = new bpm.portal.Interaction();
		this.selected.aiOid = this.getActivityOid();

		this.selected = this.loadExistingState(this.selected);
		this.getIntialFolderInformation(this.selected.aiOid);

		_sdCorrespondenceService.getProcessOidForActivity(ctrl.selected.aiOid).then(function(result){
			ctrl.selected.piOid = result.piOid;
			console.log(ctrl.selected.piOid);
			piOid = ctrl.selected.piOid;
			ctrl.loadAddressBook();

		});

		this.buttons = {
				confirm : ctrl.i18n('views-common-messages.common-OK', 'OK'),
				cancel : ctrl.i18n('views-common-messages.common-Cancel','Cancel')
		}

		this.installPanelCloseHandlers($scope);
	};


	/*
	 * 
	 */
	CorrespondenceCtrl.prototype.installPanelCloseHandlers = function($scope) {
		var ctrl = this;
		// Setup panel close handler
		window.performIppAiClosePanelCommand = function(commandId) {
			// Call function with appropriate 'this' context
			ctrl.performIppAiClosePanelCommand(commandId);
		}
	};
	
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.getDefaultCorrespondenceType = function (){
		    var moduleId = 'ipp-views-common';
		    var preferenceId = 'preference';
		    var scope = 'PARTITION';
		    var config =  _sdPreferenceService.getStore(scope, moduleId, preferenceId);
		    config.fetch();
		    var fromParent = false;
		    var type = config.getValue('ipp-views-common.correspondencePanel.prefs.correspondence.defaultType', fromParent);
		    return type;
	}
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.getIntialFolderInformation = function( aiOid){
		var ctrl = this;
		_sdCorrespondenceService.getFolderInformationByActivityOid(aiOid).then(function(data){
			ctrl.parentFolderPath = data.path;
			ctrl.folderId= data.uuid;
			trace.log("Parent folder Id ",	ctrl.folderId);
		});
	}



	/*
	 * 
	 */
	CorrespondenceCtrl.prototype.performIppAiClosePanelCommand = function(commandId) {
		try{
			var postData = preparePostData(this.selected);
			trace.log("Data to be saved to structured data",postData);
			this.interactionProvider.saveData(null, postData);
			parent.InfinityBpm.ProcessPortal.confirmCloseCommandFromExternalWebApp(commandId);
		}catch(e){
			trace.error("Exception when performing Close panel command.",e);
		}
	}

	/**
	 * 
	 */
	function preparePostData(uiData) {


		var postData = {
				"CORRESPONDENCE_REQUEST" : {
					"Type" : _filter('uppercase')(uiData.type),
					"ProcessInstanceOID" : uiData.piOid,
					"Subject" :uiData.subject,
					"MessageBody" : uiData.content
				}
		};

		if(uiData.type == 'email') {
			var to = formatOutDataAddress(uiData.to);
			if(to && to.length > 0) {
				postData.CORRESPONDENCE_REQUEST.To = to
			}

			var bcc = formatOutDataAddress(uiData.bcc);
			if(bcc && bcc.length > 0) {
				postData.CORRESPONDENCE_REQUEST.BCC = bcc
			}

			var cc = formatOutDataAddress(uiData.cc);
			if(cc && cc.length > 0) {
				postData.CORRESPONDENCE_REQUEST.CC = cc
			}
		}
		
		if(uiData.attachments && uiData.attachments.length > 0){
			var formated_attachments = formatOutDataAttachments(uiData.attachments);
			postData.CORRESPONDENCE_REQUEST.Attachments = formated_attachments
		}
		return postData;
	}

	/**
	 * 
	 */
	function prepareUiData(data, uiData) {
		
		trace.log("Data from structured data",data);
		
		uiData.type = data.Type ? angular.lowercase(data.Type.__text) : uiData.type ;
		if(uiData.type == 'email'){
			uiData.to = data.To ? formatInDataAddress(data.To_asArray) : uiData.to ;
			uiData.bcc = data.BCC_asArray ? formatInDataAddress(data.BCC_asArray) :uiData.bcc ;
			uiData.cc =  data.CC_asArray ?formatInDataAddress(data.CC_asArray) : uiData.cc;
			uiData.subject = data.Subject ? data.Subject.__text :	uiData.subject ;
		}

		uiData.content =  data.MessageBody?  data.MessageBody.__text :	uiData.content;
		uiData.attachments =data.Attachments_asArray ? formatInDataAttachments(data.Attachments_asArray): uiData.attachments;

		if(uiData.bcc ){
			uiData.showBcc = uiData.bcc.length > 0
		}

		if(uiData.cc ){
			uiData.showCc =uiData.cc.length > 0
		}

   	uiData.fieldMetaData = {
      "fields": data.FieldMetaData_asArray ? formatInDataFieldsMetaData(data.FieldMetaData_asArray) : []
    };
		
		trace.log("Data after conversion to ui format",uiData);
		return uiData;
	}
	
	/**
	 * 
	 */
	function formatOutDataAttachments( attachments ){
		var outAttachments = []; 
		angular.forEach(attachments,function(data){
			var templateDocumentId = data.templateDocumentId ?  data.templateDocumentId : data.documentId;
			outAttachments.push({
				DocumentId : data.documentId,
				TemplateDocumentId :templateDocumentId,
				Name : data.name,
				ConvertToPdf :data.convertToPdf ? true : false 
			});
		});
		return outAttachments;
	}

	/**
	 * 
	 */
	function formatInDataAddress(addresses){
		var outAddresses = []; 
		angular.forEach(addresses,function(data){
			if(data.Address && data.Address.length > 1) {
				var type = "email";
				if(data.IsFax == "true") {
					type = "fax";
				}
				outAddresses.push( {
					name : data.DataPath,
					value : data.Address,
					type  : type
				});
			}
		});
		return outAddresses;
	}

	/**
	 * 
	 */
	function formatOutDataAddress(addresses){
		var outAddresses = []; 
		angular.forEach(addresses,function(data){
			outAddresses.push( {
				DataPath : data.name,
				Address : data.value,
				IsFax  : data.type != 'email'
			})
		});

		return outAddresses;
	}

	/**
	 * 
	 */
	function formatInDataAttachments(attachments){
		var outAttachments = []; 
		angular.forEach(attachments,function(data){
			if(data.Name && data.Name.length > 1) {
				outAttachments.push({
					documentId : data.DocumentId,
					documentId :  data.DocumentId,
					templateDocumentId : data.TemplateDocumentId,
					name : data.Name ? data.Name : "sample",
							convertToPdf:data.ConvertToPdf
				})
			}
		});
		return outAttachments;
	}
	
	 /**
   * 
   */
  function formatOutDataAddress(addresses){
    var outAddresses = []; 
    angular.forEach(addresses,function(data){
      outAddresses.push( {
        DataPath : data.name,
        Address : data.value,
        IsFax  : data.type != 'email'
      })
    });

    return outAddresses;
  }

	/**
	 * 
	 */
	function formatInDataFieldsMetaData(fieldsMD){
		var fields = []; 
		angular.forEach(fieldsMD,function(field){
		  fields.push({
				type : field.Type,
				name : field.Name,
				location : field.Location,
				useImageSize : (field.UseImageSize == "true")? true : false  
			});
		});
		return fields;
	}

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.copyDocumentToCorrespondenceFolder = function( item ){ 
		var ctrl = this;
		_sdCorrespondenceService.copyDocumentToCorrespondenceFolder(item.path,ctrl.parentFolderPath).then(function(result){
			//Converting data to standard format
			ctrl.addAttachment({
				documentId : result.path,
				templateDocumentId : result.path,
				name : result.name,
				convertToPdf : false
			});
		});
	};


	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.resolveTemplateAndAddDocument = function( item ){ 
		var ctrl = this;
		item.convertToPdf = ctrl.convertToPdf;
		_sdCorrespondenceService.resolveAttachmentTemplate( item, ctrl.selected, ctrl.parentFolderPath).then(function(result) {
			trace.log("Template Resolved successfully attachments is ", result);
			ctrl.addAttachment(result);
		});
	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.exposeApis = function( $scope ){ 

		var ctrl = this;


		ctrl.getScope= function(){
			return $scope;
		}

		CorrespondenceCtrl.prototype.addAttachment = function(file){
			var found = _filter('filter')(ctrl.selected.attachments,{name : file.name},true);
			if(found && found.length > 0) {
				var index = ctrl.selected.attachments.indexOf(found[0]);
				ctrl.selected.attachments.splice(index,1);
				trace.log("Document already exists deleting the previous one.");
			}
			ctrl.selected.attachments.push(file);
		}; 
		CorrespondenceCtrl.prototype.showTest = function(value){
			ctrl.test =value
		}; 

		CorrespondenceCtrl.prototype.handleDocumentUpload = function(item){ 
		
			var fileFormat = item.name.split('.').pop();
			trace.log("Document being uploaded ",  item.name);
			if(TEPMPLATING_SUPPORTED_FILE_FORMATS.indexOf(fileFormat) > -1){
				//Run it through templating engine
				trace.log("Format Supported sending to resolving template");
				ctrl.resolveTemplateAndAddDocument(item);
			}else{
				trace.log("File format not supported for templating copying directly to correspondence folder.");
				ctrl.copyDocumentToCorrespondenceFolder(item);
			}
		};

	};

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.loadExistingState = function( uiData ){
		trace.log("Loading existing structured data.");
		var inData =   this.interactionProvider.fetchData( "CORRESPONDENCE_REQUEST");
		if(inData && inData.CORRESPONDENCE_REQUEST) {
			uiData = prepareUiData(inData.CORRESPONDENCE_REQUEST, uiData);
		}
		return uiData;
	};

	function populateCorrespondenceMetaData(metaData, documents){

		if(metaData.to || metaData.bcc || metaData.cc) {
			type = 'email'
		}else{
			type = "print";
		}

		var uiData =  {
				type  : type, // print / email
				to: metaData.to,
				bcc:metaData.bcc,
				cc:metaData.cc,
				content : metaData.content,
				subject : metaData.subject,
				templateId : '',
				attachments : documents,
				aiOid : '',
				showBcc : metaData.bcc ? metaData.bcc.length > 0 : false,
						showCc :   metaData.cc ? metaData.cc.length > 0 : false
		}
		console.log(uiData)
		return uiData;
	}

	/**
	 * 
	 */
	function uploadLocalFilesToServer(files) {
		_sdCorrespondenceService.uploadAttachments(files, piOid).then(function(data){
			trace.log("File uploaded succssfully response.",data);

			if(data.documents.length > 0) {
				angular.forEach(data.documents,function(item){
					CorrespondenceCtrl.prototype.handleDocumentUpload(item);
				});
			}else {
				var html = "";
				angular.forEach(data.failures,function(response){
					html = html+ "<div>"+response.message +"</div>";
				});
				_sdDialogService.error(_scope, html,{} );
			}
			clearUploadQ();
		});
	}

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.getActivityOid = function(){ 
		var uri = this.interactionProvider.getInteractionUri();
		var endcoded = uri.split('/').pop();
		var b64 = base64.get();
		var decodedId = b64.decode(endcoded);
		var partsMatcher = new RegExp('^(\\d+)\\|(\\d+)$');
		var decodedParts = partsMatcher.exec(decodedId);
		var activityInstanceOid = decodedParts[1];
		trace.log("Activity Oid is ",activityInstanceOid);
		return activityInstanceOid;

	}

	CorrespondenceCtrl.prototype.addFilesToUploadQ = function (files) {
		var ctrl = this;

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
		CorrespondenceCtrl.prototype.addFilesToUploadQ(files);
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
		CorrespondenceCtrl.prototype.addFilesToUploadQ(files);
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
				value : val,
				type : "email"
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
		var tagClass = "pi pi-email"
			if(item.type == 'fax'){
				tagClass ="pi pi-fax";
			}
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
								'<i class="pi pi-close pi-lg"></i>'+
							'</span>'+
						'</span >'+
						'</div>'+
						'<i class="pi pi-search pi-lg"> </i>	<input type="text"  class="spacing-right"  ng-model="ctrl.dialog.filter.address.value" ng-change="ctrl.addressTable.refresh();"/>'+
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
		var selectedItem = 	api.getSelectedNodes()[0].valueItem;
		trace.log("Template selected ",selectedItem);
		ctrl.templateSelectorDialog.close();
		_sdCorrespondenceService.resolveMessageTemplate( ctrl.selected.piOid, selectedItem.path).then(function(result) {
			ctrl.oldMessage = result;
			ctrl.selected.content = result;
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

		var attachments = [];

		angular.forEach(processApi.getSelectedNodes(),function(node){
			attachments.push(node.valueItem );
		});
		angular.forEach(messageTemplateApi.getSelectedNodes(),function(node){
			attachments.push(node.valueItem );
		});
		ctrl.attachmentSelectorDialog.close();

		angular.forEach(attachments,function(item){
			CorrespondenceCtrl.prototype.handleDocumentUpload(item);
		})
		
		trace.log("Attachment selected ",attachments);
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
	CorrespondenceCtrl.prototype.loadAttachments = function(attachment) {
		var ctrl = this;
		ctrl.selected.attachments.push(attachment);
	}

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.removeAttachment = function(attachment) {
		var ctrl = this;
		var index = ctrl.selected.attachments.indexOf(attachment);
		_sdCorrespondenceService.removeAttachment(attachment.documentId).then(function() {
        trace.log("Attachment remoced Successfully");
        ctrl.selected.attachments.splice(index, 1);
      }, function(e) {
        trace.log("Exception Occurred, seems that document is already deleted" );
        ctrl.selected.attachments.splice(index, 1)
      });
	}

	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.openAttachment = function(attachment) {
		var documentId = attachment.documentId;
		if(!documentId) {
			documentId =  attachment.templateDocumentId;
		}
		var viewKey = 'documentOID=' + encodeURIComponent(documentId);
		var  parameters = {
				"documentId" :  documentId
		}
		var message = {
				"type" : "OpenView",
				"data" : {
					"viewId" : 'documentView',
					"viewKey" : window.btoa(viewKey),
					"params" : parameters,
					"nested" : true
				}
		};

		parent.window.postMessage(JSON.stringify(message), "*");
	};

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
		if( ctrl.oldMessage != ctrl.selected.content) {
			//TODO change this to use the identifier
			if(ctrl.selected.content.indexOf('$') > -1) {
				_sdCorrespondenceService.resolveMessageContent( ctrl.selected.piOid, ctrl.selected.content).then(function(result) {
					ctrl.selected.content = result;
				});
			}
		}
		this.oldMessage = this.selected.content;
	}
	
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.i18n = function(key){
		var ctrl = this;
		return parent.i18n.translate(key)
	}

	//Dependency injection array for our controller.
	CorrespondenceCtrl.$inject = ['$scope','$q', '$http','$filter','$timeout','$parse','sdDialogService','sdCorrespondenceService', 'sdViewUtilService','sdLoggerService', 'sdPreferenceService'];

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