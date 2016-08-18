/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/
(function() {
	'use strict';
	
	angular.module('bcc-ui').directive('sdProcessDescriptor', [ 'sdUtilService','sdProcessDescriptorService','$q','sdI18nService', processDescriptor ]);
	function processDescriptor(sdUtilService,sdProcessDescriptorService,$q,sdI18nService){
		return {
			restrict : 'A', 
			scope : {
				poid : "="
			},
			
			templateUrl : sdUtilService.getBaseUrl() + 'plugins/html5-business-control-center/scripts/directives/partials/processDescriptor.html', 
			controller : [ '$scope',processDescriptorCtrl ],
			compile : function(elem, attr, transclude) {
				return {
					post : function(scope, element, attr, ctrl) {
						new DescriptorTableCompiler(scope, element, attr, ctrl);
					}
				};
			}
		};
		function processDescriptorCtrl($scope,sdProcessDescriptorService){
			this.poid = $scope.poid;
		};
				
		function DescriptorTableCompiler(scope, element, attr, ctrl){
			var self = this;
			this.poid = scope.poid;
			scope.i18n = scope.$parent.i18n;
			
			this.dataTable = null;
			this.tableHandleExpr = "processDescriptorCtrl.dataTable";
			this.updateMsg = "";
			this.descriptorName = "";
			DescriptorTableCompiler.prototype.initialize = function(){
				var self = this;
				var scopeToUse = scope.$parent;
				
			};		
			DescriptorTableCompiler.prototype.fetchData = function(){
				
				var deferred = $q.defer();
				var self = this;
				
				sdProcessDescriptorService.getProcessDescriptors(this.poid).then(function(descriptorList){
					for(var i in descriptorList){
						if(descriptorList[i].type=="TimeStamp"){
							descriptorList[i].value = new Date(descriptorList[i].value).getTime();
						}
					}
					var dataObject = {};
					dataObject.totalCount = descriptorList.length;
					dataObject.list = descriptorList;  
					deferred.resolve(dataObject);
				},function(responseObj){
					
				});
				return deferred.promise;
			};
			DescriptorTableCompiler.prototype.updateDescriptor = function(descriptorObj){
				self.descriptorName = descriptorObj.name; 
				sdProcessDescriptorService.updateProcessDescriptors(this.poid,descriptorObj.id,descriptorObj.value,descriptorObj.type).then(function(response){
					if(response == "true"){
						self.dataTable.refresh(true);
						self.updateMsg = self.descriptorName+" Updated Successfully"; 
					}else{
						self.dataTable.refresh(true);
						self.updateMsg = "Last Update did not get saved, please try again, Reason - "+response;
					}
					
				},function(response){
					self.dataTable.refresh(true);
					self.updateMsg = "Last Update did not get saved, please try again"; 
				});
			};
			scope.processDescriptorCtrl = self;
		};
	};
	 
	
	
})();