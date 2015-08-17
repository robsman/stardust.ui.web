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
	
	var interaction = null;
	
	var buttons = {
			confirm : '',
			cancel :''	
	}
	
	/*
	 * 
	 */
	function CorrespondenceCtrl($scope, $q , $filter,sdDialogService, sdCorrespondenceService) {
		
		
		
		this.readOnly = false;
		_q = $q;
		_filter =$filter; 
		_sdDialogService = sdDialogService;
		_sdCorrespondenceService = sdCorrespondenceService;

		this.intialize($scope);
		
		this.exposeApis();
		
		this.getScope= function(){
			return $scope;
		}
	}
	
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.intialize = function($scope){
		
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
				message : 'Enter Text',
				subject : ' Subject',
				templateId : '',
				attachments : [],
				piOid : 1014
		};
		
		
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
		
		this.loadAddressBook();
		this.addressTable = null;
		this.selectedAddresses = [];
		
		this.addressEntries= [];
		
		this.i18n = $scope.$root.i18n; 
		$scope = this.i18n;
		this.interactionProvider = new bpm.portal.Interaction();
		
		this.aOid = this.getActivityOid();
		
		buttons.confirm =  this.i18n('views-common-messages.common-Confirm', 'Confirm');
		buttons.cancel = this.i18n('views-common-messages.common-Cancel', 'Cancel');
		
		//if (!window.btoa) window.btoa = $.base64.btoa
		//if (!window.atob) window.atob = $.base64.atob
		//console.log( window.atob)
		
	};
	
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.exposeApis = function(){ 
		
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
		
	}
	
	
	CorrespondenceCtrl.prototype.getActivityOid = function(){ 
		/*var uri = this.interactionProvider.getInteractionUri();
		var endcoded = uri.split('/').pop();
		var b64 = base64.get();
		var decodedId = b64.decode(endcoded);
		var partsMatcher = new RegExp('^(\\d+)\\|(\\d+)$');
		var decodedParts = partsMatcher.exec(decodedId);
		var activityInstanceOid = decodedParts[1];
		alert(activityInstanceOid)
		return activityInstanceOid*/
		return '1082';
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
		return  _filter('filter')(faxFilter, {name : self.filter.address},false);
	};

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
			self.selectedAddresses.push(info.current)
		} else {
			var index = self.selectedAddresses.indexOf(info.current);
			self.selectedAddresses.splice(index,1)
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
		self.filter.address = "";
		var title = "Select Recipients";
		var html = '<div style="padding-bottom:10px;">'+ 
							'<span ng-repeat = "opt in ctrl.selectedAddresses" class="spacing-right "> '+
							'<span class="selected_address"><i class="glyphicon glyphicon-envelope spacing-right"></i> {{opt.name}} </span> </span >'+
						'</div>'+
						'<i class="glyphicon glyphicon-search"> </i>	<input type="text"  class="spacing-right"  ng-model="ctrl.filter.address" ng-change="ctrl.addressTable.refresh();"/>'+
							'<input class="correspondence_addressBook_fax_control" type="checkbox" ng-model="ctrl.filter.showFax" ng-change="ctrl.addressTable.refresh();"/> <span class="iceOutLbl">Fax </span>'+
					   '<div class="correspondence_addressBook_conatiner">'+
							'<table sd-data-table="ctrl.addressTable" '+
								' sd-data="ctrl.getAvailableAddresses();" sda-mode="local" '+
								' sda-selectable="multiple" sda-no-pagination="true"  sda-page-size="{{ctrl.addressEnteries.length}}"  sda-ready="ctrl.addressReady" sda-on-select="ctrl.onAddressSelection(info);" sda-selection="ctrl.selectedAddresses"> '+
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
						self.addAddress(self.selectedAddresses, self.selected.to)
					}else if(addressType == 'CC'){
						self.addAddress(self.selectedAddresses, self.selected.cc)
					}else if(addressType == 'BCC'){
						self.addAddress(self.selectedAddresses, self.selected.bcc)
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
	 function openMessageTemplateSelector() {
			var self = this;
			var title = "Select Template";
			
			var html = '<i class="glyphicon glyphicon-search"> </i>	<input type="text"  class="spacing-right"  ng-model="ctrl.filter.template" />'+
						'<br><input type="text" ng-model="ctrl.selected.templateId" ng-change="ctrl.addressTable.refresh();"/> ';
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
	
	/**
	 * 
	 */
	 function openAttachmentSelector() {
			var self = this;
			var title = "Select Attachments";
			
			var html = '<i class="glyphicon glyphicon-search"> </i>	<input type="text"  class="spacing-right"  ng-model="ctrl.filter.attachment" />'+
						'<br><input type="text" ng-model="ctrl.selected.attachmentId" ng-change="ctrl.addressTable.refresh();"/> ';
			var options = {
					confirmActionLabel : buttons.confirm,
				    cancelActionLabel :  buttons.cancel,
					title : title,
					type : 'confirm',
					width : '500px',
					onConfirm : function() {
						var attachment = {
								name : self.selected.attachmentId
						};
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
	CorrespondenceCtrl.prototype.loadAddressBook =function() {
		var self  = this;
		_sdCorrespondenceService.getAddressBook(self.selected.piOid).then(function(data){
			self.addressEntries = data
		});
	};
	
	//Dependency injection array for our controller.
	CorrespondenceCtrl.$inject = ['$scope','$q','$filter','sdDialogService','sdCorrespondenceService'];
	
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