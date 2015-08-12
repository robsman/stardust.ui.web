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

	var _q;
	var trace;
	var _filter;
	var _sdDialogService;
	var _sdCorrespondenceService;
	
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
		console.log(_sdCorrespondenceService.get())
		
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
				attachments : []
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
		
		this.addressEnteries = getAddressBook();
		this.addressTable = null;
		this.selectedAddresses = [];
		
		this.i18n = $scope.$root.i18n; 
		
		buttons.confirm =  this.i18n('views-common-messages.common-Confirm', 'Confirm');
		buttons.cancel = this.i18n('views-common-messages.common-Cancel', 'Cancel');
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
				path : val,
				value : val
		};

		return newEntry;
	}

	function getFilteredAddresses(){
		var self = this;
		var faxFilter = self.addressEnteries;
		if(!self.filter.showFax) {
			faxFilter = _filter('filter')(faxFilter, {fax : false},true);
		}

		console.log("Filter:")
		console.log( _filter('filter')(faxFilter, {path : self.filter.address},false))
		return  _filter('filter')(faxFilter, {path : self.filter.address},false);
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
			var found = _filter('filter')(destination,{path : data.path},true);
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
							'<span class="selected_address"><i class="glyphicon glyphicon-envelope spacing-right"></i> {{opt.path}} </span> </span >'+
						'</div>'+
						'<i class="glyphicon glyphicon-search"> </i>	<input type="text"  class="spacing-right"  ng-model="ctrl.filter.address" ng-change="ctrl.addressTable.refresh();"/>'+
							'<input class="correspondence_addressBook_fax_control" type="checkbox" ng-model="ctrl.filter.showFax" ng-change="ctrl.addressTable.refresh();"/> <span class="iceOutLbl">Fax </span>'+
					   '<div class="correspondence_addressBook_conatiner">'+
							'<table sd-data-table="ctrl.addressTable" '+
								' sd-data="ctrl.getAvailableAddresses();" sda-mode="local" '+
								' sda-selectable="multiple" sda-no-pagination="true"  sda-page-size="{{ctrl.addressEnteries.length}}"  sda-ready="ctrl.addressReady" sda-on-select="ctrl.onAddressSelection(info);" sda-selection="ctrl.selectedAddresses"> '+
								' <thead >'+
									'<tr>'+
										'<th sda-label="Name"></th>'+
										'<th sda-label="Data Path"></th>'+
									'</tr>'+
								' </thead>'+
								' <tbody>'+
									'<tr>'+
										'<td>{{rowData.value}}</td>'+
										'<td>{{rowData.path}}</td>'+
									'</tr>'+
								' </tbody>'+
							' </table>'+
					  '</div> ';
				
		var options = {
				title : title,
				type : 'confirm',
				confirmActionLabel : buttons.confirm,
			    cancelActionLabel :  buttons.cancel,
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
					dialogActionType : 'OK_CLOSE'
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
					onConfirm : function() {
						var attachment = {
								name : self.selected.attachmentId
						};
						self.loadAttachments(attachment);
						
					},
					dialogActionType : 'OK_CLOSE'
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
	function getAddressBook() {
		 return  [
				         {
				        	 path : ' path1',
				        	 value : 'email1@email.com',
				        	 fax :  false
				         },
				         {
				        	 path : ' path2',
				        	 value : '122331@email',
				        	 fax :  true
				         },
				         {
				        	 path : ' path3',
				        	 value : '122331@email',
				        	 fax :  false
				         },
				         {
				        	 path : ' path1',
				        	 value : 'email1@email.com',
				        	 fax :  false
				         },
				         {
				        	 path : ' path2',
				        	 value : '122331@email',
				        	 fax :  true
				         }
				         ]
	};
	
	//Dependency injection array for our controller.
	CorrespondenceCtrl.$inject = ['$scope','$q','$filter','sdDialogService','sdCorrespondenceService'];
	
	//Require capable return object to allow our angular code to be initialized
	//from a require-js injection system.
	return {
		init: function(angular,appName){
			angular.module(appName)
			.controller("sdCorrespondenceCtrl", CorrespondenceCtrl);
		}
	};
	
});