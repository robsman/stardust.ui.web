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
              'sdLoggedInUserService', 'sdPreferenceService', 'sdI18nService', '$scope', ParticipantManagementCtrl]);

  var _q;
  var _sdParticipantManagementService
  var trace;
  var _sdUtilService;
  var i18n;
  var _sdUserService;
  var _sdLoggedInUserService;
  var _sdPreferenceService;

  /**
   * 
   */
  function ParticipantManagementCtrl($q, sdParticipantManagementService, sdLoggerService, sdUtilService, sdUserService,
          sdLoggedInUserService, sdPreferenceService, sdI18nService, $scope) {
    trace = sdLoggerService.getLogger('admin-ui.sdParticipantManagementCtrl');
    _q = $q;
    _sdParticipantManagementService = sdParticipantManagementService;
    _sdUtilService = sdUtilService;
    _sdUserService = sdUserService;
    _sdLoggedInUserService = sdLoggedInUserService;
    _sdPreferenceService = sdPreferenceService;

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
    this.lastEvent = {};
  }

  ParticipantManagementCtrl.prototype.iconCallback = function(item) {

    if (!item.type) { return "sc sc-fw sc-1x sc-spiral"; }

    if (item.type === "USER") { return "js-icon glyphicon glyphicon-user"; }

    if (item.type === "ROLE_UNSCOPED") { return "sc sc-fw sc-1x sc-cog"; }

    if (item.type === "ROLE_SCOPED") { return "sc sc-fw sc-1x sc-cog"; }

    return "sc sc-badge-portrait sc-fw sc-1x sc-users sc-right";
  };

  ParticipantManagementCtrl.prototype.menuCallback = function(menuData) {
    menuData.deferred.resolve("(test,test)");
  };

  ParticipantManagementCtrl.prototype.onTreeInit = function(api) {
    this.treeApi = api;
  };

  // Handle our tree callbacks inclduing lazy load on node expand
  ParticipantManagementCtrl.prototype.eventCallback = function(data, e) {
    // data.srcScope is not currently in the html5-common sdTree implementation
    // but will be added extremely soon as you are going to go nuts trying to
    // respond to drag-n-drop events on recursive nodes without it.

    var promises = [], that = this;

    this.selectedItem = data.valueItem;
    this.lastEvent = data.treeEvent;

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
      // access srcScope for item in this case as
      // ?? not sure how to use scrScope
      console.log("Drag-Drop");

      var participants = [];
      participants.push(data.valueItem);

      _sdParticipantManagementService.saveParticipants(participants, this.rowSelectionForAllUsersTable).then(
              function(data) {
                // update the tree with server response

              }, function() {

              });
    } else if (data.treeEvent === "menu-test") {
      console.log("Menu Event");
      console.log(this.treeApi.childNodes[data.nodeId].nodeItem);
      console.log("----------------------");
    }
    // Resovle everything else
    else {
      data.deferred.resolve();
    }

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

})();