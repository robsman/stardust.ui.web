define([],function(){
	
	/***********************************************************************
	 * Old-style require wrapped directive and service designed to encapsulate
	 * the behavior of a business object search panel. Includes the directive
	 * as well as a service to interface with the REST layer. 
	 * 
	 * TODO: HTML5 - Split out the directive and service and place
	 * them in HTML5-COMMON, assuming all are agreed it is a good candidate 
	 * for a common resource.
	 * 
	 * TODO: Directive needs to utilize Subodh's dataTable implementation.
	 * 
	 * TODO: Comments, Comments, Comments, convert to jdoc style...
	 * 
	 * TODO: HTML5 - CSS in the template needs to be updated to use Common CSS.
	 * 
	 ***********************************************************************/
	
	return {
		init: function(angular,lModule){
	
			var $http,
			    $q,	  
			    boService,
			    $timeout,
			    boDirective,
			    app,
			    rootUrl,
			    boUnpackCamels;
			
			/*************************************************************
			 * Filter function to convert camel cased names to space 
			 * delimited strings. For cases where the string contains 
			 * consecutive upper case letters the string will delimit 
			 * between the last two characters.
			 * ---------Examples---------
			 * fx('firstName') => 'First Name'
			 * fx('FirstName') => 'First Name'
			 * fx('userIDType') => 'User ID Type'
			 *************************************************************/
			boUnpackCamels = function(){
				return function(str){
					return str
					// Insert a space between lower & upper
					.replace(/([a-z])([A-Z])/g, '$1 $2')
					// Space before last upper in a sequence followed by lower
					.replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3')
					// Uppercase the first character
					.replace(/^./, function(str) {
						return str.toUpperCase();
					});
				};
			};
			
			/*************************************************************
			 * service for REST-CALLS
			 * TODO: For HTML5 move to common or stand-alone .js file
			 *************************************************************/
			boService = function(p_$http ,p_$q,p_$timeout){
				$http = p_$http;
				$q = p_$q,
				$timeout=p_$timeout,
				rootUrl = location.href.substring(0, location.href.indexOf("/plugins"));
			};
			
			/*************************************************************
			 * Non parameterized call to retrieve all business objects
			 *************************************************************/
			boService.prototype.getBusinessObjects = function(){
				var deferred = $q.defer(),
					url;
				
				url= rootUrl + "/services/rest/business-object-management/businessObject.json";
				
				$http.get(url)
				.success(function(data){
					deferred.resolve(data);
				});
				
				return deferred.promise;
			};
			
			/*************************************************************
			 * Find business object instances based on queryable hashmap
			 *************************************************************/
			boService.prototype.findBusinessObjectsInstances = function(modelOID,boId,filters){
				var deferred = $q.defer(),
					url,
					query="?";
				
				url = rootUrl  + "/services/rest/business-object-management/businessObject/" +
					  modelOID + "/" + boId +"/instance.json";
				
				for(key in filters){
					if(filters[key] != ""){
						query=(query === "?")?query:query + "&";			
						query += key + "=" + filters[key]; 
					}
				}
				
				$http.get(url + query)
				.success(function(data){
					deferred.resolve(data);
				})
				.error(deferred.reject);
				
				return deferred.promise;
			};
			
			/************************************************************
			 * Directive to wrap up a search and result panel for business objects.
			 ************************************************************/
			boDirective = function(sdBOService,$timeout,$rootScope){

				return{
					templateUrl: './templates/searchPanel.html',
					scope: {
						onInstanceSelected : "&sdaOnSelect"
					},
					link: function(scope,elem,attrs){
						
						scope.businessObjects =[];  //Instances of our businessObject Model
						
						scope.selectedModel={};     //Model selected (secondarily) by the user
						
						scope.filter = {fields:{}}; //Filter derived from the models fields
						
						scope.selectedBusinessObj={}; //Sub-Model selected by the user, selectedModel = parent
						
						scope.ui={
									predicate:'',
									sortAsc: true,
									showBO: true
								}; //UI housekeeping data
						
						//TODO: Remove once Subodh's data table is used
						//Helper function to examine our field type with in order to determine if
						//it is supported by our table rendering.
						scope.isTypeSupported = function(stype){
							var supported = ['string','date','number','boolean'];
							return supported.indexOf(stype) > -1;
						};
						
						//Helper function to convert a string to JSON, required as the template could not
						//leverage ng-options based on the data-structure of the fields.
						scope.setBusinessObj=function(v){
							this.filter = {fields:{}};
							scope.selectedBusinessObj=JSON.parse(v);
						};
						
						//Reset our filters and their input fields, as well as our table.
						scope.resetFilters = function(){
							for (key in this.filter.fields){
								this.filter.fields[key]="";
							}
							this.businessObjects={};
						};
						
						//Query REST for business object instances and apply to scope, drives result table
						scope.findBusinessObjects=function(){
							var modelOid = this.selectedBusinessObj.modelOid,
								boId = this.selectedBusinessObj.id,
								that = this;
							
							sdBOService
							.findBusinessObjectsInstances(modelOid,boId,this.filter.fields)
							.then(function(data){
								scope.businessObjects=data.businessObjectInstances;
							})
							.catch(function(){
								//TODO: error handling
							});
						};
						
						//Function to wrap our users callback, a bit redundant but gives us room to
						//do a little work, if needed, before the invocation.
						scope.invokeCallback = function(data){
							scope.onInstanceSelected({instance:data});
						};
						
						//QUery for all base BO Models, drives our select box, 
						//this is an initialization function and is always called on link();
						sdBOService.getBusinessObjects()
						.then(function(data){
							scope.businessObjectModels = data.models;
						});
						
					}
				};
			};
			
			//TODO: add to bpm-common.services once doc-triage is refactored to participate in the HTML5 framework.
			app=angular.module(lModule);
			if(!app){app=angular.module(lModule,[]);}
			
			boService.$inject = ['$http','$q','$timeout'];
			boDirective.$inject = ['sdBusinessObjService','$timeout'];
			
			angular.module(app.name)
			.filter("uncamel",boUnpackCamels)
			.service('sdBusinessObjService',boService)
			.directive('sdBusinessObj',boDirective);
			
		}
	
	};
	
});