/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Yogesh.Manware
 * @author Zach.McCain
 */

(function() {
  'use strict';

  angular.module("admin-ui").controller(
          'sdAuthorizationManagerCtrl',
          ['$interval', '$timeout', '$scope', '$q', 'sdLoggerService', 'sdUtilService', 'sdI18nService',
              'sdMessageService', 'sdAuthorizationManagerService','sdPortalConfigurationService', AMCtrl]);

  var trace;
  var _sdAuthorizationManagerService;
  var _$q;
  var _sdUtilService;
  var _sdMessageService;
  var i18n;
  var ParticipantType = {
    ORGANIZATION: "ORGANIZATION",
    ROLE: "ROLE",
    SCOPED_ROLE : "SCOPED_ROLE",
    SCOPED_ORGANIZATION : "SCOPED_ORGANIZATION"
  }
  var All = null;
  
  
  var DEFAULT_PAGE_SIZE = 30;

  /**
   * 
   */
  function AMCtrl($interval, $timeout, $scope, $q, sdLoggerService, sdUtilService, sdI18nService, sdMessageService,
          sdAuthorizationManagerService, sdPortalConfigurationService) {

    var self = this;

    trace = sdLoggerService.getLogger('admin-ui.sdAuthorizationManagerCtrl');

    _$q = $q;
    _sdUtilService = sdUtilService;
    _sdMessageService = sdMessageService;
    
    var x=sdI18nService.getInstance('html5-admin-portal');
    i18n = $scope.sdI18nHtml5Admin = sdI18nService.getInstance('html5-admin-portal').translate;
    
    All = {
      name: sdI18nService.getInstance('views-common-messages').translate("views.common.all"),
      id: '__carnot_internal_all_permissions__',
      participantQualifiedId: '__carnot_internal_all_permissions__',
      qualifiedId: '__carnot_internal_all_permissions__'
    };

    _sdAuthorizationManagerService = sdAuthorizationManagerService;

    this.participants = {
      list: [],
      totalCount: 0
    };
    
    //Expose scope ID for use as salt on DOM element IDs
    this.scopeId = $scope.$id;
    
    // Track our multi selectable roles
    this.selectedParticipants = [];

    this.refreshParticipants(true);

    // expose our injected dependencies for use on our prototype chain.
    this.$interval = $interval;
    this.$timeout = $timeout;
    this.refreshPermissions(true);

    // initialize heavily used labels to reduce the size of html
    this.labels = {};
    this.labels.removeParticipant = i18n("views.authorizationManagerViewHtml5.permissionTree.removeParticipant");
    this.labels.removeAllParticipants = i18n("views.authorizationManagerViewHtml5.permissionTree.removeAll");
    this.labels.restore = i18n("views.authorizationManagerViewHtml5.permissionTree.restore");

    var pageSizePreference = sdPortalConfigurationService.getPageSize();
    this.pageSize = pageSizePreference > DEFAULT_PAGE_SIZE ? pageSizePreference: DEFAULT_PAGE_SIZE;
    
    AMCtrl.prototype.safeApply = function() {
      sdUtilService.safeApply($scope);
    }
  }

  
  AMCtrl.prototype.getPerspectiveIcon = function(item){
	var iconClass = "pi-permission"; //default
	
	switch(item.id){
	case "portal.ui.ippBpmModeler":
		iconClass = "pi-model";
		break;
	case "portal.ui.stardustRulesManager":
		iconClass = "pi-perspective-rules-manager";
		break;
	case "portal.ui.businessCalendar":
		iconClass = "pi-calendar";
		break;
	case "portal.ui.checklistManagement": /*Model & Go*/
		iconClass = "pi-model";
		break;
	case "portal.ui.bpmReporting":
		iconClass = "pi-perspective-reporting";
		break;
	case "portal.ui.WorkflowExecution":
		iconClass = "pi-perspective-workflow";
		break;
	case "portal.ui.ippHtml5PortalTestPerspective":
		iconClass = "pi-html";
		break;
	case "portal.ui.ippBccPerspective":
		iconClass = "pi-perspective-control-center";
		break;
	}
	
	return iconClass;
	  
  };
  
  
  
  AMCtrl.prototype.getDragElement = function(item){
	  var dragString ="<ol>";
	  dragString += "<li>" + item.name + "</li>";
	  this.selectedParticipants.forEach(function(participant){
		  if(participant.qualifiedId != item.qualifiedId){
			  dragString += "<li>" + participant.name + "</li>";
		  }
	  });
	  dragString += "</ol>";
	  
	  return dragString;
  }
  
  /**
   * 
   */
  AMCtrl.prototype.onParticipantSelect = function(selectInfo) {
    this.resetMessages();
    // count organizations and roles
    var orgs = 0, roles = 0;
    this.selectedParticipants = [];
    for (var i = 0; i < selectInfo.all.length; i++) {
      if (selectInfo.all[i].type == ParticipantType.ORGANIZATION || 
    	  selectInfo.all[i].type == ParticipantType.SCOPED_ORGANIZATION) {
        orgs++;
      } 
      else if (selectInfo.all[i].type == ParticipantType.ROLE ||  
    		   selectInfo.all[i].type == ParticipantType.SCOPED_ROLE) {
        roles++;
      }
      this.selectedParticipants.push(selectInfo.all[i]);
    }

    var participantsMsg = i18n("views.authorizationManagerViewHtml5.selectedParticipantInfo", "Selected Participants",
            [orgs, roles]);
    this.showParticipantMessage(participantsMsg, "ok");
  }

  /**
   * 
   */
  AMCtrl.prototype.refreshParticipants = function(init) {
    this.resetMessages();

    var self = this;
    this.selectedParticipants = [];
    _sdAuthorizationManagerService.searchParticipants().then(function(result) {
      result = removeCasePerformer(result);
      self.participants.list = result;
      self.participants.totalCount = result.length;

      if (angular.isDefined(self.dataTable)) {
        self.dataTable.refresh(true);
        if (!init) {
          self.showParticipantMessage(i18n("views.authorizationManagerViewHtml5.success"), "info");
        }
      }
    }, function(error) {
      trace.error(error);
      self.showParticipantMessage(i18n("views.authorizationManagerViewHtml5.participants.fetch.error"));
    })
  };

  // Reset Participants
  AMCtrl.prototype.resetParticipants = function() {
    this.resetMessages();

    var self = this;
    _sdAuthorizationManagerService.resetParticipants(this.selectedParticipants).then(function(permissions) {
      self.initializePermissionTree(permissions);
      self.showParticipantMessage(i18n("views.authorizationManagerViewHtml5.success"), "ok");
    }, function(error) {
      trace.error(error);
      self.showParticipantMessage(i18n("views.authorizationManagerViewHtml5.participants.fetch.error"));
    })
  };

  /**
   * @param matchStr
   * @returns
   */
  AMCtrl.prototype.fetchParticipants = function(matchStr) {
    var self = this;
    var deferred = _$q.defer();

    setTimeout(function() {
      var filteredList = [];
      if (!matchStr || matchStr == "") {
        filteredList = self.participants.list;
      }
      for (var i = 0; i < self.participants.list.length; i++) {
        if (self.participants.list[i].name.search(new RegExp(matchStr + '+', "gi")) > -1
                || self.participants.list[i].id.search(new RegExp(matchStr + '+', "gi")) > -1) {
          filteredList.push(self.participants.list[i]);
        }
      }
      deferred.resolve(filteredList);
    }, 0);

    return deferred.promise;
  };

  /**
   * 
   */
  AMCtrl.prototype.cloneParticipant = function() {
    this.resetMessages();
    if (!this.dataTable.getSelection() || this.dataTable.getSelection().length < 1) {
      this.showParticipantMessage(i18n("views.authorizationManagerViewHtml5.selectedParticipant.clone.warning"));
    } else {
      this.showCloneParticipantDialog = true;
    }
  }

  /**
   * 
   */
  AMCtrl.prototype.cloneParticipantConfirmed = function() {
    var self = this;
    this.resetMessages();
    if (!this.selectedTargetParticipants || this.selectedTargetParticipants.length == 0) {
      this.showParticipantMessage(i18n("views.authorizationManagerViewHtml5.cloneParticipant.targetNotSelected"));
      return;
    }

    var sourceParticipantsIds = [];
    for (var i = 0; i < this.dataTable.getSelection().length; i++) {
      sourceParticipantsIds.push(this.dataTable.getSelection()[i].qualifiedId);
    }

    var targetParticipantsIds = [];
    for (var j = 0; j < this.selectedTargetParticipants.length; j++) {
      targetParticipantsIds.push(this.selectedTargetParticipants[j].qualifiedId);
    }
    this.selectedTargetParticipants = [];

    _sdAuthorizationManagerService.cloneParticipant(sourceParticipantsIds, targetParticipantsIds).then(
            function(permissions) {
              self.showParticipantMessage(i18n("views.authorizationManagerViewHtml5.success"), "ok");
              self.initializePermissionTree(permissions);
            }, function(error) {
              trace.error(error);
              self.showParticipantMessage(i18n("views.authorizationManagerViewHtml5.permissionTree.save.error"));
            });
  }

  // Permission Tree related code
  AMCtrl.prototype.initializePermissionTree = function(permissions) {

    var that = this; // self reference
    this.data = permissions;

    // Classic DOM references to our tree which we wil leverage for filtering.
    this.nodeScopes = [];
    this.allNodes = [];
    this.ulNodes = [];

    // Track our multi selected permissions
    this.selectedAllow = [];
    this.selectedDeny = [];

    // labels for our tree node. We put them in an array so we
    // can ng-repeat over a single node and generate its own scope.
    // TODO: this was an approach required by the sd-tree directive, can
    // we just leverage normal nodes here?
    this.treeLabels = {
      allow: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.allow", "xAllow"),
        id: 'LABEL_ALLOW',
        nodeType: "label"
      }],
      deny: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.deny", "xDeny"),
        id: 'LABEL_DENY',
        nodeType: "label"
      }],
      models: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.models", "xModels"),
        id: 'ROOT_MD',
        nodeType: "label"
      }],
      gp: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.generalPermissions", "xGeneral Permissions"),
        id: 'ROOT_GP',
        nodeType: "label"
      }],
      pd: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.processDefinitions", "xProcess Definitions"),
        id: 'ROOT_PD',
        nodeType: "label"
      }],
      ac: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.activities", "xActivities"),
        id: 'ROOT_ACT',
        nodeType: "label"
      }],
      data: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.data", "xData"),
        id: 'ROOT_DATA',
        nodeType: "label"
      }],
      persp: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.perspectives", "xPerspectives"),
        id: 'ROOT_PERSPECTIVES',
        nodeType: "label"
      }],
      lp: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.launchPanels", "xLaunch Panels"),
        id: 'ROOT_LAUNCHPANELS',
        nodeType: "label"
      }],
      vw: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.views", "xViews"),
        id: 'ROOT_VIEWS',
        nodeType: "label"
      }],
      gx: [{
        name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.globalExtensions", "xGlobal Extensions"),
        id: 'GLOBAL_EXTENSIONS',
        nodeType: "label"
      }],
      perm: [{
          name: i18n("views.authorizationManagerViewHtml5.permissionTree.label.uiPermissions", "xUI Permissions"),
          id: 'UI_PERMISSIONS',
          nodeType: "label"
        }]
    };

  }
  
  /**
   * Resets the data structures which keep track of the DENY and ALLOW nodes
   * selected from our authorization tree.
   */
  AMCtrl.prototype.resetSelectedAllowDenyItems = function(){
	  this.selectedAllow=[];
	  this.selectedDeny=[];
  };
  
  // Handle select and multi selects of our permissions.
  AMCtrl.prototype.addSelectedPermission = function(permission, e, target) {

    var srcArray = this.selectedAllow,
    	scope,
    	self;
    
    self = this;
    
    scope = angular.element(e.srcElement || e.target).scope();
    
    permission.setExpansionState = function(val){
    	scope.$parent.$parent.isVisible = true;
    }
    
    permission.setIconClass = function(val){
    	self.$timeout(function(){
    		scope.$parent.$parent.iconClass = val;
    	},0);
    }
    
    if (target === "deny") {
      srcArray = this.selectedDeny;
    }

    var permissionIndex = srcArray.indexOf(permission);

    if (e.ctrlKey) {
      if (permissionIndex >= 0) {
        srcArray.splice(permissionIndex, 1);
      } else {
        srcArray.push(permission);
      }
    } else {
      while (srcArray.pop()) {
      }
      if (permissionIndex === -1) {
        srcArray.push(permission);
      }
    }
  };

  // Handle the drag start event from our role objects
  AMCtrl.prototype.roleDragStart = function(data, e) {
    var that = this,
    	selectedTblRows;
    
    selectedTblRows = this.dataTable.getSelection();
    
    if (this.selectedParticipants.indexOf(data) === -1){
    	while(this.selectedParticipants.pop()){/*clear all*/}
    	this.selectedParticipants.push(data);
    	selectedTblRows = [];
    	selectedTblRows.push({"qualifiedId" : data.qualifiedId});
    }
    
    that.dataTable.setSelection(selectedTblRows);

    
  };

  // Helper function for constructing nodes we register on our allNodes array.
  AMCtrl.prototype.buildNodeEntry = function(elem, filterable) {
    var obj = {
      'text': elem[0].textContent,
      'filterable': filterable,
      'scope': elem.scope(),
      'elem': elem
    };

    return obj;
  };

  /**
   * Takes an array of participants and tests each participant to see
   * if its qualifiedId matches that of the nodeItems participantQualifiedId.
   * If the nodeItem does not have a participantQualifiedId property or indexOf
   * returns -1 then false is returned.
   */
  AMCtrl.prototype.applyFlashlightFilter = function(selectedParticipants){
	var filterFx;
	
	//Filter only applies if user has selected one or more participants
	//from the participant table.
	if(!selectedParticipants.length || selectedParticipants.length ==0){
		return;
	}
	
	//reset our allow/deny nodes as we don't want the user performing drop operations
	//on nodes hidden by the filter.
	this.resetSelectedAllowDenyItems();
	
	filterFx = function(nodeItem){
		
		if(nodeItem.participantQualifiedId){
			return selectedParticipants.some(function(p){
				return p.qualifiedId === nodeItem.participantQualifiedId;
			});
		}
		else{
			return false;
		}
	}
	this.applyFilter(filterFx);
  } 
  
  /**
   * Takes a string value and passes a simple indexOf test
   * to the base ApplyFilter function.
   * @param val
   */
  AMCtrl.prototype.applyTextFilter = function(val){
	  var filterFx;
	  
	  if(val === undefined || !val){
		  this.resetFilter();
		  return;
	  }
	  
	  //reset our allow/deny nodes as we don't want the user performing drop operations
	  //on nodes hidden by the filter.
	  this.resetSelectedAllowDenyItems();
	  
	  filterFx = function(nodeItem){
		if(!nodeItem.name){
			return false;
		}
		return nodeItem.name.indexOf(val) > -1;
	  }
	  
	  this.applyFilter(filterFx);
  }
  
  /**
   * Reset the Dom structure of our tree so that hidden nodes are visible
   * and matched nodes are unmatched. It does not modify node expansion state.
   */
  AMCtrl.prototype.resetFilter = function(){
	  var treeRoot;
	  
	  treeRoot =$("#authTree" + this.scopeId);
	  $(".match-custom",treeRoot).removeClass("match-custom");
	  $(".hide",treeRoot).removeClass("hide");
  }
  
  /**
   * Builds a hashmap of our permission tree giving us access to scope,nodeItems,
   * raw DOM elements etc. We can leverage this to do those things we normally would
   * do through the sdTree directives published API.
   * @returns
   */
  AMCtrl.prototype.getPermissionTreeElements = function(){
	  
	var treeRoot,
		elements,
		elementalMap = {},
		objScope,
		tempElem;

	 //We need the root of our tree...
    treeRoot =$("#authTree" + this.scopeId);
    //...so that we can extract individual nodes from its context
    elements = $('li',treeRoot);
    
    //Loop over every tree node and build a collection
    for(var i = 0 ; i < elements.length; i++){
    	
      tempElem = elements[i];
      objScope = angular.element(tempElem).scope();
 	   
      //Scope can have one of three named items on it so test appropriately.
      //Very fine point in this implementation is that the item types appear
      //hierarchially so we will need to test the lowest type in the hierarchy first
      //to avoid erroneously placing an object as the nodeItem which is being inherited from
      //the prototype chain. 
      //TODO:This bit of nastiness could be removed by naming all the targets
      //of our ng-repeat with the same name so that prototypically inherited objects are overwritten.
      //I will leave that for a time when we aren't 1 week from the RC.
	  if(objScope.item){
		  if(!elementalMap.hasOwnProperty(objScope.item.$$hashKey)){
	 		  elementalMap[objScope.item.$$hashKey] = {
		 			   "scope" : objScope, 
		 			   "elem" : $(tempElem),
		 			   "nodeItem" : objScope.item,
		 			   "nodeType" : "participant"
		 	   };
		  }
	  }
 	  else if(objScope.genItem){
 		 if(!elementalMap.hasOwnProperty(objScope.genItem.$$hashKey)){
		  elementalMap[objScope.genItem.$$hashKey] = {
	 			   "scope" : objScope, 
	 			   "elem" : $(tempElem),
	 			   "nodeItem" : objScope.genItem,
	 			   "nodeType" : "genItem"
	 	   };
 		 }
 	  }
 	  else if(objScope.labelItem){
 		 if(!elementalMap.hasOwnProperty(objScope.labelItem.$$hashKey)){
 		  elementalMap[objScope.labelItem.$$hashKey] = {
 	 			   "scope" : objScope, 
 	 			   "elem" : $(tempElem),
 	 			   "nodeItem" : objScope.labelItem,
 	 			   "nodeType" : "label"
 	 	   };
 		 }
 	   }	   
    }//for loop ends
	return elementalMap;
  }
  
  /**
   * Filter the DOM structure representing our authorization tree.
   * @param comparatorFx - function which will test the provided
   * nodeItem object for a match.
   */
  AMCtrl.prototype.applyFilter = function(comparatorFx) {
    var matches = [],
    	treeRoot,
    	elements,
    	elementalMap = {},
    	objScope,
    	tempElem,
    	tempObj,
    	parentScope,
    	childRecurse;
    
    //Get our elements and their scopes etc...
    elementalMap = this.getPermissionTreeElements();
    
    //Recursive funtion to show or hide all scopes beneath current scope
    childRecurse = function(cs,isVisible){
       for(; cs; cs= cs.$$nextSibling) {
              cs.isVisible=isVisible;
              childRecurse(cs.$$childHead);
        }
    };
    
    //step 1: reset entire tree, but look for matches along the way
    $(".match-custom",treeRoot).removeClass("match-custom"); //remove all match classes
    for(var key in elementalMap){
    	
        tempObj = elementalMap[key];
        tempObj.scope.isVisible=false;
        tempObj.elem.removeClass("hide");
        
        //test our angular object from our scope by injecting it into the compartorFx.
        if(comparatorFx(tempObj.nodeItem)===true){
          matches.push(tempObj);
        }
    }
    
    //If no matches bail out.
    if(matches.length===0){
    	this.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.permissionTree.filter.noMatches"));
    	return;
    }else
    {
    	this.resetMessages();
    }
    
    //step 2 ,iterate over matches expanding to the root
    matches.forEach(function(v){
      v.scope.isVisible = true;
      v.elem.addClass("match-custom");
      childRecurse(v.scope.$$childHead,true);
      v = v.scope.$parent;
      while(v){
        if( v.hasOwnProperty("isVisible")){v.isVisible=true;}
         v = v.$parent;
      }
    });
    
    //now hide all non visible nodes
    for(key in elementalMap){
      tempObj = elementalMap[key];
      if(tempObj.scope.isVisible===false){
        tempObj.elem.addClass("hide");
      }
    }

    //collapse children
    for(var i = matches.length-1;i>=0;i--){
        childRecurse(matches[i].scope.$$childHead,false);
    }
    
    //One final cleanup pass to take care of any children
    //of matches that were matches themselves which were
    //closed in the collapse children step
    for(i = matches.length-1;i>=0;i--){
        matches[i].scope.isVisible=true;
        parentScope = matches[i].scope.$parent;
        while(parentScope){
            if( parentScope.hasOwnProperty("isVisible")){parentScope.isVisible=true;}
            parentScope= parentScope.$parent;
        }
    }

  };

  // Add a role to a permission ALLOW,
  // this is a callback wired to the sd-data-drop directive
  // and as such we need to invoke $timeout to initiate a digest.
  AMCtrl.prototype.allowRole = function(data, e) {
    var scope = angular.element(e.target || e.srcElement).scope();
    var permission = scope.$parent.$parent.genItem;
    var allow = null;
    var deny = null;
    
    //examine permissions, if scoped
    if (this.selectedAllow.indexOf(permission) == -1) {
      allow = [permission];
      deny = [];
    } else {
      allow = this.selectedAllow;
      deny = this.selectedDeny;
    }
    this.updatePermissions(scope, allow, deny);
  };

  // Deny a role to a permission DENY
  // this is a callback wired to the sd-data-drop directive
  // and as such we need to invoke $timeout to initiate a digest.
  AMCtrl.prototype.denyRole = function(data, e) {
    var scope = angular.element(e.target || e.srcElement).scope();
    var permission = scope.$parent.$parent.genItem;
    var deny = null;
    var allow = null;
    if (this.selectedDeny.indexOf(permission) == -1) {
      deny = [permission];
      allow = [];
    } else {
      deny = this.selectedDeny;
      allow = this.selectedAllow;
    }
    this.updatePermissions(scope, allow, deny);
  };

  // update permissions pertaining to all selected nodes
  AMCtrl.prototype.updatePermissions = function(scope, allow, deny) {
    var self = this,
    	isScopedPresent = false,
    	isAllowModelOnly= true,
    	isDenyModelOnly = true,
    	affectedDenyNodes=[],
    	affectedAllowNodes=[],
    	elementalMap;
    
    this.resetMessages();


    //Iterate over selectedParticipants to check if at least one particpant is scoped then allow and deny
    //must only be within the model root node of our tree, test by permission prefix in the set 
    //['activity.','processDefinition.','data.']
    //if so dialog and abort.
    
    //First test, do we have at least one scoped participant type?
    isScopedPresent = this.selectedParticipants.some(function(participant){
    	return participant.type === "SCOPED_ROLE" || participant.type == "SCOPED_ORGANIZATION";
    });
    
    //Second test, are all allow nodes only in the model root or UI permissions?
    allow.forEach(function(item){
    	isAllowModelOnly = isAllowModelOnly && (
    		item.id.indexOf("portal.ui.") === 0 ||
			item.id.indexOf("processDefinition.") === 0 ||
	       	item.id.indexOf("activity.") === 0 ||
	       	item.id.indexOf("data.") === 0)
    });
    

    //Third test, are all deny nodes only in the model root?
    deny.forEach(function(item){
    	isDenyModelOnly = isDenyModelOnly && (
			item.id.indexOf("processDefinition.") === 0 ||
	       	item.id.indexOf("activity.") === 0 ||
	       	item.id.indexOf("data.") === 0)
    });
    
    //Final test, 
    if(isScopedPresent && !(isAllowModelOnly && isDenyModelOnly)){
    	return;
    }
    
    //set icon for the node to the spinner
    scope.iconClass= "isDeferred";
    scope.$parent.$parent.isVisible = true;
    
    allow.forEach(function(item){
    	if(item.setExpansionState){
	    	item.setExpansionState(true);
	    	item.setIconClass("isDeferred");
    	}
    });
    deny.forEach(function(item){
    	if(item.setExpansionState){
	    	item.setExpansionState(true);
	    	item.setIconClass("isDeferred");
    	}
    });
    

    
    
    _sdAuthorizationManagerService.savePermissions(self.selectedParticipants, allow, deny).then(function(result) {
      var permissions = result.permissions;

      // update all selected nodes
      for (var i = 0; i < permissions.length; i++) {
        for (var j = 0; j < allow.length; j++) {
          if (permissions[i].id == allow[j].id) {
            allow[j].allow = permissions[i].allow;
            break;
          }
        }
        for (var j = 0; j < deny.length; j++) {
          if (permissions[i].id == deny[j].id) {
            deny[j].deny = permissions[i].deny;
            break;
          }
        }
      }
      self.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.success"), "ok");
    }, function(error) {
      trace.error(error);
      self.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.permissionTree.save.error"));
    })
    ['finally'](function(){
    	scope.iconClass= "";
    	allow.forEach(function(item){
    		if(item.setIconClass){
    			item.setIconClass("");
    		}
	    });
	    deny.forEach(function(item){
	    	if(item.setIconClass){
	    		item.setIconClass("");
	    	}
	    });
    });
  }

  // handles removing items from our allow or deny arrays
  AMCtrl.prototype.removeParticipant = function(v, e) {
    var self = this;
    this.resetMessages();
    var scope = angular.element(e.target || e.srcElement).scope();
    var permission = scope.$parent.$parent.genItem;

    var allExist = false;

    if (v.menuEvent === "menuItem.clicked") {
      v.item.ref[v.item.target].forEach(function(w, i, arr) {
        if (w.participantQualifiedId === v.item.role.participantQualifiedId) {
          if (w.participantQualifiedId == All.id) {
            var warning = {};
            warning.message = i18n("views.authorizationManagerViewHtml5.permissionTree.warning.removeAll");
            warning.type = "warn";
            self.showPermissionMessage(warning);
            allExist = true;
          } else {
            arr.splice(i, 1);
          }
        }
      });

      if (allExist) {
        v.deferred.resolve();
        return;
      }

      var participants = angular.copy(v.item.ref[v.item.target]);

      var allow = null;
      var deny = null;
      if (v.item.target === "allow") {
        allow = [permission];
      }
      if (v.item.target === "deny") {
        deny = [permission];
      }

      _sdAuthorizationManagerService.savePermissions(participants, allow, deny, true).then(function(result) {
        var permissions = result.permissions;
        v.item.ref['allow'] = permissions[0].allow;
        v.item.ref['deny'] = permissions[0].deny;
        self.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.success"), "ok");
      }, function(error) {
        trace.error(error);
        self.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.permissionTree.save.error"));
      });
    }
    v.deferred.resolve();
  };

  // Remove All Participants
  AMCtrl.prototype.removeAllParticipants = function(v, e) {
    this.resetMessages();
    var self = this;
    var scope = angular.element(e.target || e.srcElement).scope();
    var permission = scope.$parent.$parent.genItem;

    v.item.ref[v.item.target] = [];

    if (v.menuEvent === "menuItem.clicked") {
      var allow = null;
      var deny = null;
      if (v.item.target === "allow") {
        allow = [permission];
      }
      if (v.item.target === "deny") {
        deny = [permission];
      }

      _sdAuthorizationManagerService.savePermissions([], allow, deny, true).then(function(result) {
        var permissions = result.permissions;
        v.item.ref['allow'] = permissions[0].allow;
        v.item.ref['deny'] = permissions[0].deny;
        self.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.success"), "ok");
        v.deferred.resolve();
      }, function(error) {
        trace.error(error);
        self.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.permissionTree.save.error"));
        v.deferred.resolve();
      });

    }
    v.deferred.resolve();
  }

  // Restore All permissions
  AMCtrl.prototype.restorePermission = function(v, e) {
    var self = this;
    this.resetMessages();
    var scope = angular.element(e.target || e.srcElement).scope();
    var permission = scope.$parent.$parent.genItem;

    if (v.menuEvent === "menuItem.clicked") {
      var allow = null;
      var deny = null;
      allow = [permission];
      deny = [permission];

      v.item.ref['allow'] = [];
      v.item.ref['deny'] = [];

      // TODO: set actual default participant for this node
      // Same applies to Deny
      v.item.ref['allow'].push(All);

      _sdAuthorizationManagerService.savePermissions([], allow, deny, true).then(function(result) {
        var permissions = result.permissions;
        v.item.ref['allow'] = permissions[0].allow;
        v.item.ref['deny'] = permissions[0].deny;
        self.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.success"), "ok");
        v.deferred.resolve();
      }, function(error) {
        trace.error(error);
        self.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.permissionTree.save.error"));
        v.deferred.resolve();
      });
    }
  }

  /**
   * 
   */
  AMCtrl.prototype.refreshPermissions = function(init) {
    var self = this;
    this.resetMessages();
    
    //reset our allow/deny nodes
	this.resetSelectedAllowDenyItems();
	
    _sdAuthorizationManagerService.getPermissions().then(function(permissions) {
      self.initializePermissionTree(permissions);
      if (!init) {
        self.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.success"), "ok");
      }
    }, function(err) {
      trace.error(err);
      self.showPermissionMessage(i18n("views.authorizationManagerViewHtml5.permissionTree.fetch.error"));
    });
  };

  /**
   * 
   */
  AMCtrl.prototype.highlightParticipants = function() {
    var participants = [];
    for (var i = 0; i < this.selectedParticipants.length; i++) {
      participants.push(this.selectedParticipants[i].name);
    }
    this.applyFilter(participants.join('|'));
  }

  AMCtrl.prototype.resetMessages = function() {
    this.showMessage = false;
    this.showMessage2 = false;
    _sdMessageService.showMessage({
      type: "error"
    });
  }

  AMCtrl.prototype.showParticipantMessage = function(msg, type) {
    this.showMessage = true;
    this.showMessage2 = false;
    this.showMessage_(msg, type);
  }

  AMCtrl.prototype.showPermissionMessage = function(msg, type) {
    this.showMessage = false;
    this.showMessage2 = true;
    this.showMessage_(msg, type);
  }

  AMCtrl.prototype.showMessage_ = function(msg, type) {
    if (!type) {
      _sdMessageService.showMessage(msg);
    } else {
      _sdMessageService.showMessage({
        message: msg,
        type: type
      });
    }
  }

  function removeCasePerformer(result) {
    for (var i = 0; i < result.length; i++) {
      if ("{PredefinedModel}CasePerformer" === result[i].qualifiedId) {
        result.splice(i, 1);
      }
    }
    return result;
  }
}

)();
