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

(function() {
	'use strict';

	angular.module("viewscommon-ui").controller('sdCorrespondenceCtrl',
			['$scope','$q','$filter','sgI18nService','sdDialogService','sdProcessInstanceService',
					CorrespondenceCtrl]);
	var _q;
	var trace;
	var _filter;
	var _sgI18nService;
	var _sdDialogService;
	var _sdProcessInstanceService;
	/*
	 * 
	 */
	function CorrespondenceCtrl($scope, $q , $filter, sgI18nService,sdDialogService, sdProcessInstanceService) {

		_q = $q;
		_filter =$filter; 
		_sgI18nService = sgI18nService;
		_sdDialogService = sdDialogService;
		_sdProcessInstanceService = sdProcessInstanceService;

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
				type  : 'print', // print / email
				showBcc : false,
				showCc : false,
				to: [],
				bcc:[],
				cc:[]
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
		}
		
		this.filter = {
				address : '',
				showFax : false
		}
		
		this.addressEnteries = getAddressBook();
		this.addressTable = null;
		this.selectedAddresses = [];
		
		
	};
	
	/**
	 * 
	 */
	CorrespondenceCtrl.prototype.exposeApis = function(){ 
		
		CorrespondenceCtrl.prototype.showBcc = showBcc;
		CorrespondenceCtrl.prototype.showCc = showCc;
		
	}
	
	
	CorrespondenceCtrl.prototype.getAddressesAutoCompleteBCC = function(filterCriteria, resultHolder){
		var self = this;
		self.filter.address = filterCriteria;
		self.filter.showFax = true;
		self.data.like.bcc = self.getAvailableAddresses();
	};
	
	CorrespondenceCtrl.prototype.getAddressesAutoCompleteCC = function(filterCriteria, resultHolder){
		var self = this;
		self.filter.address = filterCriteria;
		self.filter.showFax = true;
		self.data.like.cc = self.getAvailableAddresses();
	};

	
	CorrespondenceCtrl.prototype.getAddressesAutoCompleteTO = function(filterCriteria, resultHolder){
		var self = this;
		self.filter.address = filterCriteria;
		self.filter.showFax = true;
		self.data.like.to= self.getAvailableAddresses();
	};


	
	
	CorrespondenceCtrl.prototype.newItemFactory = function(val){
		 var newEntry = {
				path : val,
				value : val
		};
		 
		 return newEntry;
	}
	
	CorrespondenceCtrl.prototype.getAvailableAddresses = function(){
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
	CorrespondenceCtrl.prototype.tagPreMapper = function(item, index) {
		   var tagClass = "fa fa-envelope-o"
		   return tagClass;
	   };
	  
	   
	   CorrespondenceCtrl.prototype.onSelect = function(info) {
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
		 CorrespondenceCtrl.prototype.addAddress = function(source,destination) {
				
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
		CorrespondenceCtrl.prototype.addInlineAddress = function(addressType) {
			var self = this;

			var data = null;
			var destination = null;

			if (addressType == 'TO') {
				data = self.enteredAdd.to;
				destination = self.selected.to;
				self.enteredAdd.to = '';
			} else if (addressType == 'CC') {
				data = self.enteredAdd.cc;
				destination = self.selected.cc;
				self.enteredAdd.cc = '';
			} else if (addressType == 'BCC') {
				data = self.enteredAdd.bcc;
				destination = self.selected.bcc;
				self.enteredAdd.bcc = '';
			}
			console.log("Add Single Address");
			console.log(data);
			if(!data || data == ''){
					return;
				}
			self.addAddress([{
				path : data,
				value : data
			}], destination);
			
			
		};	
		
		
		/**
		 * 
		 */
		CorrespondenceCtrl.prototype.removeAddress = function(addressType, data) {
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

		/*
		 * 
		 */
		CorrespondenceCtrl.prototype.openAddressSelector = function(addressType) {
			
			var self = this;
			self.addressReady = true;
			self.selectedAddresses = [];
			var title = "Select Recipients";
			var html = '<div style="padding-bottom:10px;">'+ 
							'<span ng-repeat = "opt in ctrl.selectedAddresses" class="spacing-right "> '+
							'<span class="correspondence_mail_id"><i class="fa fa-envelope-o spacing-right"></i> {{opt.path}} </span> </span >'+
						'</div>'+
						'<i class="glyphicon glyphicon-search"> </i>	<input type="text"  class="spacing-right"  ng-model = "ctrl.filter.address" ng-change="ctrl.addressTable.refresh();"/>'+
							'<input class="correspondence_addressBook_fax_control" type="checkbox" ng-model="ctrl.filter.showFax" ng-change="ctrl.addressTable.refresh();"/> <span class="iceOutLbl">Fax </span>'+
					   '<div class="correspondence_addressBook_conatiner">'+
							'<table sd-data-table="ctrl.addressTable" '+
								' sd-data="ctrl.getAvailableAddresses();" sda-mode="local" '+
								' sda-selectable="multiple" sda-no-pagination="true"  sda-page-size="{{ctrl.addressEnteries.length}}"  sda-ready="ctrl.addressReady" sda-on-select="ctrl.onSelect(info);" sda-selection="ctrl.selectedAddresses"> '+
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
					onConfirm : function() {
						if(addressType == 'TO'){
							self.addAddress(self.selectedAddresses, self.selected.to)
						}else if(addressType == 'CC'){
							self.addAddress(self.selectedAddresses, self.selected.cc)
						}else if(addressType == 'BCC'){
							self.addAddress(self.selectedAddresses, self.selected.bcc)
						}
					},
					dialogActionType : 'OK_CLOSE'
			};
			_sdDialogService.dialog(self.getScope(), options, html)
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
	 function getAddressBook() {
		 
		 return _sdProcessInstanceService.getCorrespondenceAddressBook();
	 };
	
	
})();