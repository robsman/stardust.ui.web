/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Abhay.Thappan
 * @author Yogesh.Manware
 */

(function() {
  'use strict';

  angular.module("admin-ui").controller(
          'sdParticipantManagementCtrl',
          ['$q', 'sdParticipantManagementService', 'sdLoggerService', 'sdUtilService', 'sdUserService',
              'sdLoggedInUserService', 'sdPreferenceService', 'sdI18nService', '$scope', 'sdMessageService',
              ParticipantManagementCtrl]);

  var _q;
  var _sdParticipantManagementService
  var trace;
  var _sdUtilService;
  var i18n, _sdI18nService;
  var _sdUserService;
  var _sdLoggedInUserService;
  var _sdPreferenceService, _sdMessageService;
  var  lazyLoad = false;

  /**
   * 
   */
  function ParticipantManagementCtrl($q, sdParticipantManagementService, sdLoggerService, sdUtilService, sdUserService,
          sdLoggedInUserService, sdPreferenceService, sdI18nService, $scope, sdMessageService) {
    trace = sdLoggerService.getLogger('admin-ui.sdParticipantManagementCtrl');
    _q = $q;
    _sdParticipantManagementService = sdParticipantManagementService;
    _sdUtilService = sdUtilService;
    _sdUserService = sdUserService;
    _sdLoggedInUserService = sdLoggedInUserService;
    _sdPreferenceService = sdPreferenceService;
    _sdMessageService = sdMessageService;
    _sdI18nService = sdI18nService;
    i18n = $scope.sdI18nHtml5Admin = sdI18nService.getInstance('html5-admin-portal').translate;

    this.allUsersTable = null;
    this.hideInvalidatedUsers = false;
    this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
    this.rowSelectionForAllUsersTable = null;
    this.exportFileNameForAllUsers = "AllUsers";

    this.getAllCounts();

    this.treeInit();
  }

  /**
   * @returns
   */
  ParticipantManagementCtrl.prototype.getAllUsers = function(options) {
    var deferred = _q.defer();
    var self = this;
    self.allUsers = {};
    var query = {
      'options': options,
      'hideInvalidatedUsers': self.hideInvalidatedUsers
    }
    _sdParticipantManagementService.getAllUsers(query).then(function(data) {
      self.allUsers.list = data.list;
      self.allUsers.totalCount = data.totalCount;
      deferred.resolve(self.allUsers);
    }, function(error) {
      trace.log(error);
      deferred.reject(error);
    });

    return deferred.promise;
  };

  ParticipantManagementCtrl.prototype.getAllCounts = function() {
    var self = this;
    _sdUserService.getAllCounts().then(function(data) {
      self.activeCount = data.activeCount;
      self.allCount = data.totalCount;
    }, function(error) {
      trace.log(error);
    });
  }

  /**
   * 
   */
  ParticipantManagementCtrl.prototype.refresh = function() {
    var self = this;
    self.allUsersTable.refresh();
    self.getAllCounts();
  };

  ParticipantManagementCtrl.prototype.changeHideInvalidatedUsersFlag = function() {
    var self = this;
    self.hideInvalidatedUsers = !self.hideInvalidatedUsers;
    self.allUsersTable.refresh();
  };
  /**
   * @param mode
   * @param oid
   */
  ParticipantManagementCtrl.prototype.openCreateCopyModifyUser = function(mode, oid) {
    var self = this;
    self.mode = mode;
    _sdParticipantManagementService.openCreateCopyModifyUser(mode, oid).then(function(data) {
      self.user = data;
      if (mode == 'CREATE_USER') {
        self.title = 'views-common-messages.views-createUser-title';
        self.titleParams = '';
      } else if (mode == 'COPY_USER') {
        self.title = 'views-common-messages.views-copyUser-title';
        self.titleParams = self.rowSelectionForAllUsersTable[0].displayName;
        self.user.oid = oid;
      } else if (mode == 'MODIFY_USER') {
        self.title = 'views-common-messages.views-modifyUser-title';
        self.titleParams = '';
      }
      self.user.isPasswordEnabled = self.isPasswordEnabled(mode, self.user.isInternalAuthentication);
      self.initDisplayFormats();
      self.showUserProfileDialog = true;
      self.loadUserProfileDialog = true;
    }, function(error) {
      trace.log(error);
    });
  };
  /**
   * @param mode
   * @param isInternalAuthentication
   * @returns {Boolean}
   */
  ParticipantManagementCtrl.prototype.isPasswordEnabled = function(mode, isInternalAuthentication) {
    if ((mode == 'MODIFY_USER' && isInternalAuthentication) || mode == 'MODIFY_PROFILE_CONFIGURATION') {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 
   */
  ParticipantManagementCtrl.prototype.initDisplayFormats = function() {
    var self = this;
    var USER_NAME_DISPLAY_FORMAT_0 = "{1}, {0} ({2})";
    var USER_NAME_DISPLAY_FORMAT_1 = "{0} {1} ({2})";
    var USER_NAME_DISPLAY_FORMAT_2 = "{1} {0} ({2})";
    self.user.displayFormats = [];
    if (!_sdUtilService.isEmpty(self.user.firstName) && !_sdUtilService.isEmpty(self.user.lastName)) {
      self.user.displayFormats.push({
        'value': USER_NAME_DISPLAY_FORMAT_0,
        'label': _sdUtilService.format(USER_NAME_DISPLAY_FORMAT_0, [self.user.firstName, self.user.lastName,
            self.user.account])
      });
      self.user.displayFormats.push({
        'value': USER_NAME_DISPLAY_FORMAT_1,
        'label': _sdUtilService.format(USER_NAME_DISPLAY_FORMAT_1, [self.user.firstName, self.user.lastName,
            self.user.account])
      });
      self.user.displayFormats.push({
        'value': USER_NAME_DISPLAY_FORMAT_2,
        'label': _sdUtilService.format(USER_NAME_DISPLAY_FORMAT_2, [self.user.firstName, self.user.lastName,
            self.user.account])
      });
    }
  };
  /**
   * @param res
   * @returns {Boolean}
   */
  ParticipantManagementCtrl.prototype.onConfirmFromCreateUser = function(res) {
    var self = this;
    if (self.userProfileForm.$valid) {
      var error = this.validateData();
      if (error) {
        return false;
      } else {
        var deferred = _q.defer();
        var user = {};
        angular.extend(user, self.user);
        delete user.allRealms;
        _sdParticipantManagementService.createCopyModifyUser(user, self.mode).then(function(data) {
          if (data.success == true) {
            deferred.resolve();
            self.allUsersTable.refresh();
            self.getAllCounts();
            // add id
            user.id = user.account;
            self.createUserCallback(user);
          } else if (data.success == false) {
            if (data.passwordValidationMsg != undefined) {
              self.passwordValidationMsg = data.passwordValidationMsg;
              self.userProfileForm.$error.passwordValidationMsg = true;
            } else if (data.validationMsg != undefined) {
              self.validationMsg = data.validationMsg;
              self.userProfileForm.$error.validationMsg = true;
            }
            deferred.reject();
          }
        }, function(error) {
          trace.log(error);
          deferred.reject();
        });
        return deferred.promise;

      }
    } else {

      return false;
    }
  };

  /**
   * @returns {Boolean}
   */
  ParticipantManagementCtrl.prototype.validateData = function() {
    var self = this;
    var error = false;

    // Validate Dates
    if (!_sdUtilService.validateDateRange(self.user.validFrom, self.user.validTo)) {
      error = true;
      self.userProfileForm.$error.invalidDateRange = true;
    } else {
      self.userProfileForm.$error.invalidDateRange = false;
    }

    // Validate password
    if (self.user.changePassword) {
      if (_sdUtilService.validatePassword(self.user.password, self.user.confirmPassword)) {
        error = true;
        self.userProfileForm.$error.passwordMismatch = true;

      } else {
        self.userProfileForm.$error.passwordMismatch = false;
      }
    }

    return error;

  };
  /**
   * @param res
   */
  ParticipantManagementCtrl.prototype.onCloseFromCreateUser = function(res) {
    var self = this;
    self.loadUserProfileDialog = false;
    delete self.user;
  };
  /**
   * 
   */
  ParticipantManagementCtrl.prototype.invalidateUsers = function() {
    var self = this;
    var oids = this.getSelectedUserOids(self.rowSelectionForAllUsersTable);
    _sdParticipantManagementService.invalidateUsers(oids).then(function(data) {
      self.activityInstances = data.activityInstances;
      self.notificationMap = data.notificationMap;
      self.allUsersTable.refresh();
      self.getAllCounts();
      if (self.activityInstances != undefined && self.activityInstances.length > 0) {
        self.showDefaultDelegateDialog = true;
      } else {
        self.showNotificationDialog = true;
      }

    }, function(error) {
      trace.log(error);

    });

  };
  /**
   * @param res
   */
  ParticipantManagementCtrl.prototype.onConfrimFromDefaultDelegateDialog = function(res) {
    var self = this;
    var userOids = this.getInvalidatedUserOids(self.notificationMap.success);
    _sdParticipantManagementService.delegateToDefaultPerformer(self.activityInstances, userOids).then(function(data) {

      if (data.success != undefined && data.success) {
        self.showNotificationDialog = true;
      } else {
        self.showStrandedActivitiesAlert = true;
      }

    }, function(error) {
      trace.log(error);

    });
  };
  /**
   * @param res
   */
  ParticipantManagementCtrl.prototype.onCloseFromDefaultDelegateDialog = function(res) {
    var self = this;
    self.showStrandedActivitiesAlert = true;
  };
  /**
   * @param res
   */
  ParticipantManagementCtrl.prototype.onConfirmFromStrandedActivitiesAlert = function(res) {
    var self = this;
    self.showNotificationDialog = true;
  };

  /**
   * 
   */
  ParticipantManagementCtrl.prototype.getSelectedUserOids = function(selectedUsers) {
    var oids = [];
    for ( var user in selectedUsers) {
      oids.push(selectedUsers[user].oid);
    }
    return oids;
  };

  /**
   * 
   */
  ParticipantManagementCtrl.prototype.getInvalidatedUserOids = function(successNotificationList) {
    var oids = [];
    for ( var index in successNotificationList) {
      oids.push(successNotificationList[index].OID);
    }
    return oids;
  };

  /*
   * 
   */
  ParticipantManagementCtrl.prototype.preferenceDelegate = function(prefInfo) {
    var preferenceStore = _sdPreferenceService.getStore(prefInfo.scope, 'ipp-administration-perspective', 'preference'); // Override
    preferenceStore.marshalName = function(scope) {
      return "ipp-administration-perspective.userMgmt.selectedColumns";
    }
    return preferenceStore;
  };

  // ********************************
  // Participant Tree related Code
  // ********************************

  ParticipantManagementCtrl.prototype.treeInit = function() {
    this.models = [];
    this.loadModels();// Loading top level tree structure.
    this.selectedItem = {};
    this.selectedTreeNodes = [];
    this.resetMessages();
  }

  ParticipantManagementCtrl.prototype.iconCallback = function(item) {

    var isSelected = "";

    if (this.selectedTreeNodes.some(function(treeNode) {
      return getParticipatQId(item) === getParticipatQId(treeNode);
    })) {
      isSelected = " selected ";
    }

    if (!item.type) { return "sc sc-fw icon-lg sc-spiral" + isSelected; } //model

    var styleClass = "sc sc-fw icon-lg ";
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
      case "USER": styleClass += "pi-user ";
        break;
      case "DEPARTMENT": styleClass += "pi-department ";
        break;
      case "DEPARTMENT_DEFAULT": styleClass += "pi-department ";
        break;
      default: styleClass += "pi-department ";
        break;
    }
    styleClass += isSelected;
    
    return styleClass;
  };

  ParticipantManagementCtrl.prototype.menuCallback = function(menuData) {
    var item = menuData.item;

    // model node
    if (!item.type) menuData.deferred.reject();
    var menu = [];

    var adminMessages = _sdI18nService.getInstance('admin-portal-messages').translate;

    if (item.type === 'ORGANIZATON_SCOPED_EXPLICIT') {
      menu.push("(createDepartment, LABEL)".replace('LABEL',
              adminMessages('views.participantMgmt.participantTree.contextMenu.createDepartment')));
    } else if (item.type === "DEPARTMENT") {
      menu.push("(delete, LABEL)".replace('LABEL',
              adminMessages('views.participantMgmt.participantTree.contextMenu.deleteDepartment')));
      menu.push("(modifyDepartment, LABEL)".replace('LABEL',
              adminMessages('views.participantMgmt.participantTree.contextMenu.modifyDepartment')));
      menu.push("(createUser, LABEL)".replace('LABEL',
              adminMessages('views.participantMgmt.participantTree.contextMenu.createUser')));
      menu.push("(removeAllUsers, LABEL)".replace('LABEL',
              adminMessages('views.participantMgmt.participantTree.contextMenu.removeAllUsers')));
    } else if (item.type === "USER") {
      menu.push("(delete, LABEL)".replace('LABEL',
              adminMessages('views.participantMgmt.participantTree.contextMenu.removeUserGrant')));
    } else {
      menu.push("(createUser, LABEL)".replace('LABEL',
              adminMessages('views.participantMgmt.participantTree.contextMenu.createUser')));
      menu.push("(removeAllUsers, LABEL)".replace('LABEL',
              adminMessages('views.participantMgmt.participantTree.contextMenu.removeAllUsers')));
    }

    menuData.deferred.resolve(menu.toString());
  };

  ParticipantManagementCtrl.prototype.onTreeInit = function(api) {
    this.treeApi = api;
  };
  
  /**
   * Wrapper for our TreeApi's filter function.
   * @param filter - string to match upon, in the case when no
   * 				 filter is passed then the tree will be 
   * 				 reset to its unfiltered state.
   */
  ParticipantManagementCtrl.prototype.filterTree = function(filter){
	  var comparatorFx; //filterFX for the filterTree invocation.
	  
	  comparatorFx= function(nodeItem){
		  console.log(nodeItem);
		  return nodeItem.name.indexOf(filter) > -1;
	  }
	  
	  //deselect all currently selected nodes
	  this.selectedTreeNodes = [];
	  
	  //If no filter passed then just reset the tree
	  if(filter===undefined){
		  //invocation with no parameters will reset the tree
		  this.treeApi.filterTree();
	  }
	  //Otherwise invoke our treeAPI's filter function
	  else{
		  //filter tree forcing an internal elementMap update,
		  //ideally we should only pass true when we know that
		  //the tree is dirty to avoid needless overhead.
		  //TODO: maintain proper dirty state of tree.
		  this.treeApi.filterTree(comparatorFx,true);
	  }
	  
  };
  
  /**
   * Filter the participant tree based on the users selected in the user table.
   */
  ParticipantManagementCtrl.prototype.filterByTableSelection = function(){
	  
	  var comparatorFx, //filterFX for the filterTree invocation.
	  	  that = this;
	  
	  if(this.rowSelectionForAllUsersTable.length ===0){
		  return;
	  }
	  
	  //deselect all currently selected nodes
	  this.selectedTreeNodes = [];
	  
	  comparatorFx= function(nodeItem){
		  return that.rowSelectionForAllUsersTable.some(function(v){
			  return v.oid === nodeItem.OID && nodeItem.type==="USER";
		  });
	  };
	  
	  this.treeApi.filterTree(comparatorFx,true);
  }
  
  ParticipantManagementCtrl.prototype.filterForEmptyUsers = function(){
	  var comparatorFx; //filterFX for the filterTree invocation.
	  
	  //deselect all currently selected nodes
	  this.selectedTreeNodes = [];
	  
	  comparatorFx= function(nodeItem){
		  return nodeItem.type !== "USER" && nodeItem.children.length===0;
	  }
	  this.treeApi.filterTree(comparatorFx,true);
  };
  
  // Handle our tree callbacks inclduing lazy load on node expand
  ParticipantManagementCtrl.prototype.eventCallback = function(data, e) {
    this.resetMessages();
    this.selectedItem = data.valueItem;

    // On expansion of a node we need to build out its children
    if (data.treeEvent === "node-expand") {
      if (!lazyLoad) {  
        //complete tree is already loaded
        data.valueItem.isLoaded = true;
        data.deferred.resolve();
      } else {
        // No data to retrieve at model level as child items are returned in the
        // loadModels call (see constructor)
        if (data.valueItem.isLoaded === true || data.valueItem.type === "model") {
          data.deferred.resolve();
          return;
        }
  
        if (!data.valueItem.type) {
          // assume it is a model
          data.valueItem.isLoaded = true;
          data.deferred.resolve();
        } else {
          // If expanding a role expect an array of users to be returned
          // If expanding an org expect a mixed array of roles and orgs to be
          // returned
          _sdParticipantManagementService.getSubParticipants(data.valueItem, lazyLoad).then(function(participants) {
            // always add to children as template recurses on this
            data.valueItem.children = participants;
            // add isLoaded = true so we know we can skip this node next
            // expansion
            data.valueItem.isLoaded = true;
            data.deferred.resolve();
          });
        }
      }
    } else if (data.treeEvent === "node-dragend" || data.treeEvent === "node-drop") {
      console.log("Drag-Drop");
      this.handleUserDropAction(data, this.rowSelectionForAllUsersTable);
    } else if (data.treeEvent.indexOf("menu-") == 0 || (data.treeEvent === "node-delete")) {
      console.log("Menu-Option selected");
      this.handleMenuClick(data, e);
      data.deferred.resolve();
    } else if (data.treeEvent === "node-click") {
      console.log("Node selected..");
      this.addToSelectedNodes(data, e.ctrlKey);
      data.deferred.resolve();
    }

    // Resolve everything else
    else {
      data.deferred.resolve();
    }

  };

  // Handle a menu click
  ParticipantManagementCtrl.prototype.handleMenuClick = function(data, event) {
    this.resetMessages();
    var option = data.treeEvent;
    this.contextParticipantNode = data;
    var self = this;

    if ('node-delete' === option) {
      if (this.contextParticipantNode.valueItem.type === 'DEPARTMENT') {
        option = 'menu-deleteDepartment';
      } else if (this.contextParticipantNode.valueItem.type === 'USER') {
        option = 'menu-removeUser';
      }
    }

    switch (option) {
    case 'menu-createDepartment':
    case 'menu-modifyDepartment':
      this.openCreateModifyDepartment();
      break;

    case 'menu-deleteDepartment':
      // delete department
      _sdParticipantManagementService.deleteDepartment(this.contextParticipantNode.valueItem).then(
              function(data) {
                self.showParticipantMessage(_sdI18nService.getInstance('views-common-messages').translate(
                        'views.participantTree.departmentDeleted'), "ok");
                self.contextParticipantNode.deferred.resolve();
              }, function(response) {
                if (response.data && response.data.message) {
                  self.showParticipantMessage(response.data.message, "error");
                }
                self.contextParticipantNode.deferred.reject();
              });
      break;
    case 'menu-removeUser':
      var participant = this.treeApi.getParentItem(data.valueItem.uuid);
      this.saveParticipants(data, [participant], null, [data.valueItem]);
      break;

    case 'menu-createUser':
      this.modifyParticipantPostUsrCr = true;
      this.openCreateCopyModifyUser('CREATE_USER');
      break;
    
    case 'menu-removeAllUsers':
      if (getIndexOfParticipant(this.selectedTreeNodes, data.valueItem) == -1) {
        this.selectedTreeNodes.push(data.valueItem);
      }
      var participants = this.selectedTreeNodes;
      
      var usersToBeRemoved = [];
      for (var i = 0; i < participants.length; i++) {
        for (var j = 0; j < participants[i].children.length; j++) {
          if (participants[i].children[j].type == "USER") {
            usersToBeRemoved.push(participants[i].children[j]);
          }
        }
      }
      
      this.saveParticipants(data, participants, null, usersToBeRemoved);
      break;  
      
    default:
      break;
    }

  }

  // modify participant with just created user
  ParticipantManagementCtrl.prototype.createUserCallback = function(user) {
    if (this.modifyParticipantPostUsrCr) {
      this.modifyParticipantPostUsrCr = false;

      if (getIndexOfParticipant(this.selectedTreeNodes, this.contextParticipantNode.valueItem) == -1) {
        this.selectedTreeNodes.push(this.contextParticipantNode.valueItem);
      }

      this.saveParticipants(this.contextParticipantNode, this.selectedTreeNodes, [user]);
    }
  }

  // handle users drop event
  ParticipantManagementCtrl.prototype.handleUserDropAction = function(data) {
    var dropTarget = data.srcScope.nodeItem;
    if (getIndexOfParticipant(this.selectedTreeNodes, dropTarget) == -1) {
      this.addToSelectedNodes(data, true);
    }
    this.saveParticipants(data, this.selectedTreeNodes, this.rowSelectionForAllUsersTable);
  }

  // save the participant
  ParticipantManagementCtrl.prototype.saveParticipants = function(data, participants, addUsers, removeUsers) {
    var self = this;
    _sdParticipantManagementService.saveParticipants(participants, addUsers, removeUsers).then(function(result) {
      // update the tree with server response
      for (var i = 0; i < participants.length; i++) {
        //remove all users
        var nonUserParticipants = [];
        if (participants[i].children) {
          for (var j = 0; j < participants[i].children.length; j++) {
            if (!(participants[i].children[j].type === "USER")) {
              nonUserParticipants.push(participants[i].children[j]);
            }
          }
        }
        // update users received from server
        participants[i].children = result[getParticipatQId(participants[i])].concat(nonUserParticipants);
      }
      data.deferred.resolve();
    }, function(response) {
      if (response.data && response.data.message) {
        self.showParticipantMessage(response.data.message, "error");
      }
      self.selectedTreeNodes = [];
    });
  }

  // handle tree node click
  ParticipantManagementCtrl.prototype.addToSelectedNodes = function(data, update) {
    var selectedNode = data.srcScope.nodeItem;

    // model node is selected
    if (!selectedNode.type) { return; }

    this.resetMessages();

    if (('ORGANIZATON_SCOPED_EXPLICIT' === selectedNode.type) || 'USER' === selectedNode.type) { 
      this.showParticipantMessage(_sdI18nService.getInstance('views-common-messages').translate(
      'views.participantTree.inValidSelection'), "info");
      return; 
      }

    // if the droptarget was already selected then remove it
    var participantIndex = getIndexOfParticipant(this.selectedTreeNodes, selectedNode);

    if (update) {
      if (participantIndex >= 0) {
        this.selectedTreeNodes.splice(participantIndex, 1);
      } else {
        this.selectedTreeNodes.push(selectedNode);
      }
    } else {
      while (this.selectedTreeNodes.pop()) {
      }
      if (participantIndex === -1) {
        this.selectedTreeNodes.push(selectedNode);
      }
    }
  }
  

  // open create or modify cepartment dialog
  ParticipantManagementCtrl.prototype.openCreateModifyDepartment = function() {
    var self = this;
    var participant = this.contextParticipantNode.valueItem;

    if (participant.type == "DEPARTMENT") { //modify department
      self.department = angular.copy(participant);
      self.departmentTitle = _sdI18nService.getInstance('admin-portal-messages').translate(
      'views.participantMgmt.modifyDepartment.title');
      self.department.mode = 'Modify';
    } else {
      self.departmentTitle = _sdI18nService.getInstance('admin-portal-messages').translate(
      'views.participantMgmt.createDepartment.title');
      self.department = {};
      self.department.organization = participant.name;
      self.department.parentDepartmentName = participant.parentDepartmentName;
      self.department.description = null;
      self.department.uiQualifiedId = participant.qualifiedId;
      self.department.mode = 'Create';
      if (participant.uiQualifiedId) {
        self.department.uiQualifiedId = participant.uiQualifiedId;
      }
    }

    self.showCreateOrModifyDeparatmentDialog = true;

  };

  // persist department
  ParticipantManagementCtrl.prototype.createModifyDepartment = function() {
    var self = this;
    // delete unwanted parameters
    delete self.department.parentDepartmentName;
    delete self.department.organization;
    _sdParticipantManagementService.createModifyDepartment(this.department, lazyLoad).then(function(department) {
      var contextParticipant = self.contextParticipantNode.valueItem;
      if (contextParticipant.type == "DEPARTMENT") {
        // modify department
        contextParticipant.name = department.name;
        contextParticipant.description = department.description;
      } else {
        // add new department
        if (contextParticipant.children) {
          contextParticipant.children.push(department);
        } else {
          contextParticipant.children = [department];
        }
      }
      self.contextParticipantNode.deferred.resolve();
    }, function(response) {
      if (response.data && response.data.message) {
        self.showParticipantMessage(response.data.message, "error");
      }
    });

  };

  // load Models
  ParticipantManagementCtrl.prototype.loadModels = function() {
    var that = this;
    _sdParticipantManagementService.getModelParticipants(lazyLoad).then(function(data) {
      that.models = data;
    });
  };

  // add user to selectedUsers list
  ParticipantManagementCtrl.prototype.userDragStart = function(data) {
    var that = this;
    if (this.rowSelectionForAllUsersTable.indexOf(data) === -1) {
      while (that.rowSelectionForAllUsersTable.pop()) {
      }
      that.rowSelectionForAllUsersTable.push(data);
    }
  }

  // check if it is a leaf node
  ParticipantManagementCtrl.prototype.isLeaf = function(item) {
    if (item.type === "USER") { return true; }
    return false;
  }

  ParticipantManagementCtrl.prototype.resetMessages = function() {
    this.showMessage2 = false;
    _sdMessageService.showMessage({
      type: "error"
    });
  }

  ParticipantManagementCtrl.prototype.showParticipantMessage = function(msg, type) {
    this.showMessage2 = true;
    this.showMessage_(msg, type);
  }

  ParticipantManagementCtrl.prototype.showMessage_ = function(msg, type) {
    if (!type) {
      _sdMessageService.showMessage(msg);
    } else {
      _sdMessageService.showMessage({
        message: msg,
        type: type
      });
    }
  }

  function getIndexOfParticipant(participants, participant) {
    for (var i = 0; i < participants.length; i++) {
      if (getParticipatQId(participants[i]) == getParticipatQId(participant)) { return i; }
    }
    return -1;
  }
  
  // prepares participantId in a contracted format
  function getParticipatQId(participant) {
    if (participant.uiQualifiedId) { return participant.uiQualifiedId }
    return participant.qualifiedId;
  }
})();
