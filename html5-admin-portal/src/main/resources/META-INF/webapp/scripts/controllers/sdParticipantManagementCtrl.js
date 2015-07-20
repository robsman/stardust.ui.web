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
    this.showAllUsersTable = true;
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

  /**
   * 
   */
  /**
   * 
   */
  ParticipantManagementCtrl.prototype.setShowAllUsersTable = function() {
    var self = this;
    self.showAllUsersTable = !self.showAllUsersTable;
  };

  /**
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
    this.treeApi = {};
    this.selectedItem = {};
    this.selectedTreeNodes = [];
    this.resetMessages();
  }

  ParticipantManagementCtrl.prototype.iconCallback = function(item) {

    if (!item.type) { return "sc sc-fw sc-1x sc-spiral"; }

    if (item.type === "USER") { return "js-icon glyphicon glyphicon-user"; }

    if (item.type === "ROLE_UNSCOPED") { return "sc sc-fw sc-1x sc-cog"; }

    if (item.type === "ROLE_SCOPED") { return "sc sc-fw sc-1x sc-cog"; }

    return "sc sc-badge-portrait sc-fw sc-1x sc-users sc-right";
  };

  ParticipantManagementCtrl.prototype.menuCallback = function(menuData) {
    // We only support menu items for removing documents attached to nodes.
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
    } else if (item.type === "USER") {
      menu.push("(removeUser, LABEL)".replace('LABEL',
              adminMessages('views.participantMgmt.participantTree.contextMenu.removeUserGrant')));
    } else {
      menu.push("(createUser, LABEL)".replace('LABEL',
              adminMessages('views.participantMgmt.participantTree.contextMenu.createUser')));
    }

    menuData.deferred.resolve(menu.toString());
  };

  ParticipantManagementCtrl.prototype.onTreeInit = function(api) {
    this.treeApi = api;
  };

  // Handle our tree callbacks inclduing lazy load on node expand
  ParticipantManagementCtrl.prototype.eventCallback = function(data, e) {
    this.resetMessages();
    var promises = [], that = this;

    this.selectedItem = data.valueItem;

    // On expansion of a node we need to build out its children
    if (data.treeEvent === "node-expand") {
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
        _sdParticipantManagementService.getSubParticipants(data.valueItem).then(function(users) {
          // Add a uniqueID to each item that we can leverage as the
          // nodeID
          // needed by our tree directive.
          users.forEach(function(v) {
            v.nodeId = _sdParticipantManagementService.createGuid();
          });
          // always add to children as template recurses on this
          data.valueItem.children = users;
          // add isLoaded = true so we know we can skip this node next
          // expansion
          data.valueItem.isLoaded = true;
          data.deferred.resolve();
        });
      }
    } else if (data.treeEvent === "node-dragend" || data.treeEvent === "node-drop") {
      console.log("Drag-Drop");
      this.handleUsersDrop(data, e);
    } else if (data.treeEvent.indexOf("menu-") == 0 || (data.treeEvent === "node-delete")) {
      console.log("Menu-Option selected");
      this.handleMenuClick(data, e);
      data.deferred.resolve();
    } else if (data.treeEvent === "node-click") {
      console.log("Node selected..");
      this.handleTreeNodeClick(data, e);
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

    switch (option) {
    case 'menu-createDepartment':
    case 'menu-modifyDepartment':
      this.openCreateModifyDepartment();
      break;

    case 'node-delete':
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

      break;

    case 'menu-createUser':

      break;

    default:
      break;
    }

  }

  // return the style for label style - future user - support multi-select
  ParticipantManagementCtrl.prototype.getNodeStyle = function(item) {
    if (this.selectedTreeNodes.indexOf(item)) {
      return "{'selected': ture}";
    } else {
      return "{'selected': false}";
    }
  }

  // handle users drop event
  ParticipantManagementCtrl.prototype.handleUsersDrop = function(data, event) {
    var dropTarget = data.srcScope.nodeItem;
    var participants = this.selectedTreeNodes;

    if (this.selectedTreeNodes.indexOf(dropTarget) == -1) {
      participants = [dropTarget];
    }

    _sdParticipantManagementService.saveParticipants(participants, this.rowSelectionForAllUsersTable).then(
            function(result) {
              // update the tree with server response
              for (var i = 0; i < participants.length; i++) {
                participants[i].children = result[getParticipatUiId(participants[i])];
              }
              data.deferred.resolve();
            }, function(response) {
              if (response.data && response.data.message) {
                self.showParticipantMessage(response.data.message, "error");
              }
            });
  }

  // handle tree node click
  ParticipantManagementCtrl.prototype.handleTreeNodeClick = function(data, event) {
    var selectedNode = data.srcScope.nodeItem;

    // model node is selected
    if (!selectedNode.type) { return; }

    this.resetMessages();

    // if the droptarget was already selected then remove it
    var participantIndex = this.selectedTreeNodes.indexOf(selectedNode);

    if (event.ctrlKey) {
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

    var selectedParticipants = [];
    for (var i = 0; i < this.selectedTreeNodes.length; i++) {
      selectedParticipants.push(getParticipatUiId(this.selectedTreeNodes[i]));
    }

    var participantsMsg = selectedParticipants.join(", ");
    this.showParticipantMessage(participantsMsg, "ok");
  }

  // open create or modify cepartment dialog
  ParticipantManagementCtrl.prototype.openCreateModifyDepartment = function() {
    var self = this;
    var participant = this.contextParticipantNode.valueItem;

    self.departmentTitle = _sdI18nService.getInstance('admin-portal-messages').translate(
            'views.participantMgmt.createDepartment.title');

    self.departmentTitle = _sdI18nService.getInstance('admin-portal-messages').translate(
            'views.participantMgmt.modifyDepartment.title');

    if (participant.type == "DEPARTMENT") {
      self.department = angular.copy(participant);
    } else {
      self.department = {};
      self.department.organization = participant.name;
      self.department.parentDepartmentName = participant.parentDepartmentName;
      self.department.description = null;
      self.department.uiQualifiedId = participant.qualifiedId;
      if (participant.uiQualifiedId) {
        self.department.uiQualifiedId = participant.uiQualifiedId;
      }
    }

    self.showCreateOrModifyDeparatmentDialog = true;

  };

  // persist department
  ParticipantManagementCtrl.prototype.createModifyDepartment = function() {
    var self = this;
    delete self.department.parentDepartmentName;
    _sdParticipantManagementService.createModifyDepartment(this.department).then(function(data) {
      var participants = data.participants;
      var contextParticipant = self.contextParticipantNode.valueItem;
      var department = undefined;
      for (var i = 0; i < participants.length; i++) {
        if (self.department.id === participants[i].id) {
          department = participants[i];
        }
      }
      if (contextParticipant.type == "DEPARTMENT") {
        contextParticipant.name = department.name;
        contextParticipant.description = department.description;
      } else {
        contextParticipant.children.push(department);
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
    _sdParticipantManagementService.getModels().then(function(data) {
      data.forEach(function(v) {
        v.children = v.allTopLevelRoles.concat(v.allTopLevelOrganizations);
        v.children.forEach(function(v) {
          v.nodeId = _sdParticipantManagementService.createGuid();
        });
        v.nodeId = _sdParticipantManagementService.createGuid();
      });
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

  // prepares participantId in a contracted format
  function getParticipatUiId(participant) {
    if (participant.uiQualifiedId) { return participant.uiQualifiedId }
    return participant.qualifiedId;
  }

})();