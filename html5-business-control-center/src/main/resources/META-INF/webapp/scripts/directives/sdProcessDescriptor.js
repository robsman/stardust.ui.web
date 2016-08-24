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
						if(descriptorList[i].type=="TimeStamp" || (descriptorList[i].type== "Calender" && descriptorList[i].hideTime!=true && descriptorList[i].editable)){
							descriptorList[i].value = new Date(descriptorList[i].value).getTime();
						}else if(descriptorList[i].type== "Calender" && descriptorList[i].hideTime==true){
							var day = new Date(descriptorList[i].value).getDate();
							var month = new Date(descriptorList[i].value).getMonth() + 1;
							var year = new Date(descriptorList[i].value).getFullYear();
							descriptorList[i].value = month+"/"+day+"/"+year;
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
				var restParam = {};
				restParam.id = descriptorObj.id;
				restParam.type = descriptorObj.type;
				restParam.changedValue = descriptorObj.value;
				restParam.hideTime = descriptorObj.hideTime;
				restParam.useServerTimeZone = descriptorObj.useServerTimeZone;
				if(restParam.hideTime == true){
					restParam.changedValue = new Date(descriptorObj.value).getTime();
				}
				sdProcessDescriptorService.updateProcessDescriptors(this.poid,restParam).then(function(response){
					
						self.dataTable.refresh(true);
						self.updateMsg = self.descriptorName+" Updated Successfully"; 
					
				},function(response){
					self.dataTable.refresh(true);
					self.updateMsg = "Last Update did not get saved, please try again"; 
				});
				
			};
			scope.processDescriptorCtrl = self;
		};
	};
	 
	
	
})();