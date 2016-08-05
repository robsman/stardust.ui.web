/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

(function(){

	var mod = angular.module("viewscommon-ui");

	/*Service Implementation*/
	sdPartTreeSerivce.$inject = ["$q", "$http", "sdUtilService"];

	function sdPartTreeSerivce($q, $http, sdUtilService){
		this.$q = $q;
		this.$http = $http;
		this.baseUrl = sdUtilService.getBaseUrl();
		this.participantsUrl = this.baseUrl + "services/rest/portal/participants"
	};

	sdPartTreeSerivce.prototype.getParticipantTree = function(){

		var deferred = this.$q.defer(),
			url = this.participantsUrl + "/tree?lazyload=false";

		this.$http({
			"method" : "GET",
			"url" : url
		})
		.then(function(res){
			deferred.resolve(res.data);
		})["catch"](function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	};

	mod.service('sdParticipantTreeService', sdPartTreeSerivce)

	/*Controller Implementation*/
	sdPartTreeCntrl.$inject = ["sdParticipantTreeService","sgI18nService", "$scope"];


	function sdPartTreeCntrl(sdParticipantTreeService, sgI18nService, $scope){
		var that = this;

		this.state = "Initializing";
		this.sdParticipantTreeService = sdParticipantTreeService;
		this.selectedParticipants = [];
		this.allowMultiselect = $scope.allowMultiselect === "true";
		this.showUserNodes = $scope.showUserNodes === "true";
		this.$scope = $scope;

		if(this.$scope.apiReference){
			$scope.apiReference = {
				"getSelectedParticipants" : function(){
					return that.selectedParticipants;
				}
			};
		};

		//load tree data
		sdParticipantTreeService.getParticipantTree()
		.then(function(participants){

			if(that.showUserNodes===false){

				participants.forEach(function(model){
					that.removeAllUserNodes(model);
				});

			}	

			that.participants = participants;
			that.state = "Ready"
		})
		["catch"](function(err){
			this.state = "Error";
		});
	};

	sdPartTreeCntrl.prototype.removeAllUserNodes = function(treeData){
		var that = this,
			i = treeData.children.length;

		while(i--){
			child = treeData.children[i];
			if(child.type==="USER"){
				treeData.children.splice(i,1);
			};
			if(child.children && child.children.length > 0){
				that.removeAllUserNodes(child);
			};
		};

	};

	sdPartTreeCntrl.prototype.isParticipantSelected = function(participant){
		return this.selectedParticipants.some(function(p){
			return p === participant;
		})
	};

	sdPartTreeCntrl.prototype.onTreeInit = function(api){
		this.treeApi = api;
	};

	sdPartTreeCntrl.prototype.menuCallback = function(item){
		//stubbed
	};

	sdPartTreeCntrl.prototype.iconCallback = function(item){
		var styleClass = "pi pi-fw pi-lg ";
		switch (item.type) {
		  case "ORGANIZATON_SCOPED_EXPLICIT": styleClass += "pi-org ";
		    break;
		  case "ORGANIZATON_SCOPED_IMPLICIT": styleClass += "pi-org ";
		    break;
		  case "ORGANIZATION_UNSCOPED": styleClass += "pi-org ";
		    break;
		  case "ROLE_SCOPED": styleClass += "pi-scope-role ";
		    break;
		  case "ROLE_UNSCOPED": styleClass += "pi-role ";
		    break;
		  case "USERGROUP": styleClass += "pi-user-group ";
		    break;
		  case "USERGROUPS": styleClass += "pi-user-group ";
		    break;
		  case "USER": styleClass += "pi-user ";
		    break;
		  case "DEPARTMENT": styleClass += "pi-department ";
		    break;
		  case "DEPARTMENT_DEFAULT": styleClass += "pi-department ";
		    break;
		  default: styleClass += "pi-model ";
		}
		if(this.isParticipantSelected(item)){
			styleClass += "selected ";
		}
		return styleClass;
	};

	sdPartTreeCntrl.prototype.recursiveTreeNodeFactory = function(nodeScope){
	  	var template;
	  	
		template ='<li sd-tree-node ng-repeat="item in item.children" \
		             sda-menu-items="(,)" \
					 sda-node-id="item.uuid" \
		   			 sda-lazy-compile="true" \
		             sda-is-leaf="!item.children || item.children.length == 0" \
					 sda-label="item.name"> \
					<ul> \
						<li sd-tree-curse-fx></li> \
					</ul> \
				</li>';

		return template;
  	};

	sdPartTreeCntrl.prototype.eventCallback = function(data,e){
		var partIndex;

		if(data.treeEvent === "node-click" && data.valueItem.type){

			if(this.isParticipantSelected(data.valueItem)){
				partIndex=this.selectedParticipants.indexOf(data.valueItem);
				this.selectedParticipants.splice(partIndex,1);
			}
			else{
				if(!this.allowMultiselect || e.ctrlKey===false){
					while(this.selectedParticipants.pop());
				}
				this.selectedParticipants.push(data.valueItem);
				this.$scope.onParticipantSelected({"item" : angular.extend(data.valueItem)});
			}
		}
		data.deferred.resolve();
	};

	/*Directive implementation*/
	mod.controller("sdParticipantTreeController",sdPartTreeCntrl);
  
    mod.directive("sdParticipantTree",["sdUtilService","$parse",function(sdUtilService,$parse){
    	var templateUrl = sdUtilService.getBaseUrl() +"plugins/html5-views-common/html5/scripts/directives/sdParticipantTree/sdParticipantTree.html";

    	function linkFx(scope,elem,attrs){
    		//scope.onParticipantSelect = $parse(attrs.sdaOnSelect);
    	};

		return {
	    	scope: {
	    		apiReference : "=sdParticipantTree",
		        allowMultiselect : "@sdaMultiSelect",
		        showUserNodes : "@sdaShowUsers",
		        onParticipantSelected : "&sdaOnSelect"
		    },
		    link: linkFx,
	     	controller: "sdParticipantTreeController",
	        controllerAs: "partTreeCtrl",
	        templateUrl : templateUrl
		};
	}]);

})();