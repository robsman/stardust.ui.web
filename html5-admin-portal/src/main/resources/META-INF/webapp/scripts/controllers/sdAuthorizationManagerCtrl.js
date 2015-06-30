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
          ['$interval', '$timeout', '$scope', '$q', 'sdLoggerService',
              'sdUtilService', 'sdI18nService', 'sdMessageService',
              'sdAuthorizationManagerService', AMCtrl]);

  var trace;
  var _sdAuthorizationManagerService;
  var _$q;
  var _sdUtilService;
  var _sdMessageService;
  var i18n;
  var ParticipantType = {
    ORGANIZATION: "ORGANIZATION",
    ROLE: "ROLE"
  }
  var All = null;

  /**
   * 
   */
  function AMCtrl($interval, $timeout, $scope, $q, sdLoggerService,
          sdUtilService, sdI18nService, sdMessageService,
          sdAuthorizationManagerService) {

    var self = this;

    trace = sdLoggerService.getLogger('admin-ui.sdAuthorizationManagerCtrl');

    _$q = $q;
    _sdUtilService = sdUtilService;
    _sdMessageService = sdMessageService;

    i18n = $scope.sdI18nHtml5Admin = sdI18nService
            .getInstance('html5-admin-portal').translate;

    All = {
      name: sdI18nService.getInstance('views-common-messages').translate(
              "views.common.all"),
      id: 'all',
      participantQualifiedId: 'all',
      qualifiedId: 'all'
    };

    _sdAuthorizationManagerService = sdAuthorizationManagerService;

    this.participants = {
      list: [],
      totalCount: 0
    };

    // Track our multi selectable roles
    this.selectedParticipants = [];

    this.refreshParticipants();

    // expose our injected dependencies for use on our prototype chain.
    this.$interval = $interval;
    this.$timeout = $timeout;
    this.refreshPermissions();

    // initialize heavily used labels to reduce the size of html
    this.labels = {};
    this.labels.removeParticipant = i18n("views.authorizationManagerViewHtml5.permissionTree.removeParticipant");
    this.labels.removeAllParticipants = i18n("views.authorizationManagerViewHtml5.permissionTree.removeAll");
    this.labels.restore = i18n("views.authorizationManagerViewHtml5.permissionTree.restore");

    AMCtrl.prototype.safeApply = function() {
      sdUtilService.safeApply($scope);
    }
  }

  /**
   * 
   */
  AMCtrl.prototype.onParticipantSelect = function(selectInfo) {
    this.showMessage = true;
    // count organizations and roles
    var orgs = 0, roles = 0;
    this.selectedParticipants = [];
    for (var i = 0; i < selectInfo.all.length; i++) {
      if (selectInfo.all[i].type == ParticipantType.ORGANIZATION) {
        orgs++;
      } else if (selectInfo.all[i].type == ParticipantType.ROLE) {
        roles++;
      }
      this.selectedParticipants.push(selectInfo.all[i]);
    }

    this.showMessage = true;
    var participantsMsg = {};
    participantsMsg.message = i18n(
            "views.authorizationManagerViewHtml5.selectedParticipantInfo",
            "Selected Participants", [orgs, roles]);
    participantsMsg.type = "ok";
    _sdMessageService.showMessage(participantsMsg);
  }

  /**
   * 
   */
  AMCtrl.prototype.refreshParticipants = function() {
    this.showMessage = false;
    var self = this;
    _sdAuthorizationManagerService.searchParticipants({
      type: 3
    }).then(function(result) {
      self.participants.list = result;
      self.participants.totalCount = result.length;
      if (angular.isDefined(self.dataTable)) {
        self.dataTable.refresh(true);
      }
    }, function(error) {
      trace.error("Error occured while fetching Participants!")
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
        if (self.participants.list[i].name.search(new RegExp(matchStr + '+',
                "gi")) > -1
                || self.participants.list[i].id.search(new RegExp(matchStr
                        + '+', "gi")) > -1) {
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
    if (!this.selection || this.selection.length < 1) {
      _sdMessageService
              .showMessage({
                message: i18n("views.authorizationManagerViewHtml5.selectedParticipant.clone.warning"),
                type: "error"
              });
      this.showMessage = true;
    } else {
      this.showMessage = false;
      this.showCloneParticipantDialog = true;
    }
  }

  /**
   * 
   */
  AMCtrl.prototype.cloneParticipantConfirmed = function() {
    if (!this.selectedTargetParticipants
            || this.selectedTargetParticipants.length == 0) {
      _sdMessageService
              .showMessage({
                message: i18n("views.authorizationManagerViewHtml5.cloneParticipant.targetNotSelected"),
                type: "error"
              });
      this.showMessage = true;
      return;
    }

    var sourceParticipantsIds = [];
    for (var i = 0; i < this.selection.length; i++) {
      sourceParticipantsIds.push(this.selection[i].qualifiedId);
    }

    var targetParticipantsIds = [];
    for (var j = 0; j < this.selectedTargetParticipants.length; j++) {
      targetParticipantsIds
              .push(this.selectedTargetParticipants[j].qualifiedId);
    }
    this.selectedTargetParticipants = [];

    _sdAuthorizationManagerService.cloneParticipant(sourceParticipantsIds,
            targetParticipantsIds);
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
        name: 'Allow',
        id: 'LABEL_ALLOW',
        nodeType: "label"
      }],
      deny: [{
        name: 'Deny',
        id: 'LABEL_DENY',
        nodeType: "label"
      }],
      models: [{
        name: 'Models',
        id: 'ROOT_MD',
        nodeType: "label"
      }],
      gp: [{
        name: 'General Permissions',
        id: 'ROOT_GP',
        nodeType: "label"
      }],
      pd: [{
        name: 'Process Definitions',
        id: 'ROOT_PD',
        nodeType: "label"
      }],
      ac: [{
        name: 'Activities',
        id: 'ROOT_ACT',
        nodeType: "label"
      }],
      data: [{
        name: 'Data',
        id: 'ROOT_DATA',
        nodeType: "label"
      }],
      persp: [{
        name: 'Perspectives',
        id: 'ROOT_PERSPECTIVES',
        nodeType: "label"
      }],
      lp: [{
        name: 'Launch Panels',
        id: 'ROOT_LAUNCHPANELS',
        nodeType: "label"
      }],
      vw: [{
        name: 'Views',
        id: 'ROOT_VIEWS',
        nodeType: "label"
      }],
      gx: [{
        name: 'Global Extensions',
        id: 'GLOBAL_EXTENSIONS',
        nodeType: "label"
      }]
    };

    // Acquire our classic DOM references after angular has finished compiling
    // our page. This block is entirely concerned with building a structure we
    // can leverage for our filtering operations.
    this
            .$timeout(
                    function() {

                      var scope, rootNode, ulNodeList, nonFilterableNodes, // array
                      // which
                      // will
                      // hold
                      // all
                      // nodes
                      // in
                      // the
                      // tree
                      filterableNodes, // only those nodes with a class of
                      // js-filterable,
                      temp;

                      rootNode = document.querySelectorAll("ul.sd-tree")[0];
                      nonFilterableNodes = rootNode
                              .querySelectorAll("ul.sd-tree .tree-node span:not(.js-filterable)");
                      filterableNodes = rootNode
                              .querySelectorAll("ul.sd-tree .tree-node .js-filterable");
                      ulNodeList = rootNode.querySelectorAll("ul");

                      // Loop over
                      for (var i = 0; i < nonFilterableNodes.length; i++) {
                        temp = angular.element(nonFilterableNodes.item(i));
                        that.allNodes.push(that.buildNodeEntry(temp, false));
                      }

                      for (i = 0; i < filterableNodes.length; i++) {
                        temp = angular.element(filterableNodes.item(i));
                        that.allNodes.push(that.buildNodeEntry(temp, true));
                      }

                      for (i = 0; i < ulNodeList.length; i++) {
                        temp = angular.element(ulNodeList.item(i));
                        that.ulNodes.push(that.buildNodeEntry(temp, true));
                      }

                    }, 0);

  }

  // Handle select and multi selects of our permissions.
  AMCtrl.prototype.addSelectedPermission = function(permission, e, target) {

    var srcArray = this.selectedAllow;

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
    var that = this;
    if (this.selectedParticipants.indexOf(data) === -1) {
      that.$timeout(function() {
        while (that.selectedParticipants.pop()) {
        }
        that.selectedParticipants.push(data);

      }, 0);
    }
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

  // Filter function which works against our allNodes array
  AMCtrl.prototype.applyFilter = function(filter) {
    var matches = [];

    // step 1: collapse entire tree, but look for matches along the way
    this.allNodes.forEach(function(v) {
      v.scope.isVisible = false;
      v.elem.removeClass("match");
      if (v.filterable && v.text && v.text.indexOf(filter) > -1) {
        matches.push(v);
      }
    });

    if (filter) {
      // step 2 ,iterate over matches expanding to the root
      matches.forEach(function(v) {
        v.scope.isVisible = true;
        v.elem.addClass("match");
        v = v.scope.$parent;
        while (v) {
          if (v.hasOwnProperty("isVisible")) {
            v.isVisible = true;
          }
          v = v.$parent;
        }
      });
    }
  };

  // Add a role to a permission ALLOW,
  // this is a callback wired to the sd-data-drop directive
  // and as such we need to invoke $timeout to initiate a digest.
  AMCtrl.prototype.allowRole = function(data, e) {
    var scope = angular.element(e.srcElement).scope();
    var permission = scope.$parent.$parent.genItem;
    var allow = null;
    var deny = null;
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
    var scope = angular.element(e.srcElement).scope();
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
    var self = this;
    this.showMessage2 = false;

    this
            .$timeout(
                    function() {
                      // TODO: need to add to our allNodes array or it isnt
                      // filterable
                      self.selectedParticipants
                              .forEach(function(participant) {
                                // adding to permissiosn in our multiselect
                                // collection

                                // Allow
                                allow
                                        .forEach(function(p, i, arr) {
                                          var exist = false;
                                          var allindex = null;

                                          p.allow
                                                  .forEach(function(
                                                          participant2, j, arr) {
                                                    if (participant2.participantQualifiedId == participant.qualifiedId) {
                                                      exist = true;
                                                    } else if (participant2.participantQualifiedId == 'all') {
                                                      allindex = j;
                                                    }
                                                  });

                                          if (!exist) {
                                            // check there exist "All", if yes,
                                            // remove it.
                                            if (allindex != null) {
                                              p.allow.splice(allindex, 1);
                                            }

                                            p.allow
                                                    .push({
                                                      name: participant.name,
                                                      participantQualifiedId: participant.qualifiedId
                                                    });
                                          }
                                        });

                                // Deny
                                deny
                                        .forEach(function(p) {
                                          var exist = false;
                                          var allindex = null;

                                          p.deny
                                                  .forEach(function(
                                                          participant2, j, arr) {
                                                    if (participant2.participantQualifiedId == participant.qualifiedId) {
                                                      exist = true;
                                                    } else if (participant2.participantQualifiedId == 'all') {
                                                      allindex = j;
                                                    }
                                                  });

                                          if (!exist) {
                                            // check there exist "All", if yes,
                                            // remove it.
                                            if (allindex) {
                                              p.deny.splice(allindex, 1);
                                            }
                                            p.deny
                                                    .push({
                                                      name: participant.name,
                                                      participantQualifiedId: participant.qualifiedId
                                                    });
                                          }
                                        });
                              });
                      scope.$parent.isVisible = true;
                      _sdAuthorizationManagerService.savePermissions(
                              self.selectedParticipants, self.selectedAllow,
                              self.selectedDeny);

                    }, 0);
  }

  // handles removing items from our allow or deny arrays
  AMCtrl.prototype.removeParticipant = function(v, e) {
    var self = this;
    this.showMessage2 = false;
    var scope = angular.element(e.srcElement).scope();
    var permission = scope.$parent.$parent.genItem;

    if (v.menuEvent === "menuItem.clicked") {
      v.item.ref[v.item.target]
              .forEach(function(w, i, arr) {
                if (w.participantQualifiedId === v.item.role.participantQualifiedId) {
                  if (w.participantQualifiedId == All.id) {
                    self.showMessage2 = true;
                    var warning = {};
                    warning.message = i18n("views.authorizationManagerViewHtml5.permissionTree.warning.removeAll");
                    warning.type = "warn";
                    _sdMessageService.showMessage(warning);
                  }

                  arr.splice(i, 1);
                }
              });

      var participants = angular.copy(v.item.ref[v.item.target]);

      var allow = null;
      var deny = null;
      if (v.item.target === "allow") {
        // TODO: set the defailt participants
        allow = [permission];

        if (v.item.ref[v.item.target].length == 0) {
          v.item.ref[v.item.target].push(All);
        }
      }
      if (v.item.target === "deny") {
        deny = [permission];
      }

      _sdAuthorizationManagerService.savePermissions(participants, allow, deny,
              true);
    }
    v.deferred.resolve();
  };

  // Remove All Participants
  AMCtrl.prototype.removeAllParticipants = function(v, e) {
    var scope = angular.element(e.srcElement).scope();
    var permission = scope.$parent.$parent.genItem;

    v.item.ref[v.item.target] = [];

    if (v.menuEvent === "menuItem.clicked") {
      var allow = null;
      var deny = null;
      if (v.item.target === "allow") {
        // if (selectedAll) { v.deferred.resolve(); return; }
        allow = [permission];

        // TODO: set actual default participant for this node
        v.item.ref[v.item.target].push(All);
      }
      if (v.item.target === "deny") {
        deny = [permission];
      }

      _sdAuthorizationManagerService.savePermissions([], allow, deny, true);
    }
    v.deferred.resolve();
  }

  // Restore All permissions
  AMCtrl.prototype.restorePermission = function(v, e) {
    var scope = angular.element(e.srcElement).scope();
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

      _sdAuthorizationManagerService.savePermissions([], allow, deny, true);
    }
    v.deferred.resolve();
  }

  AMCtrl.prototype.filterTree = function(val) {
    matches = this.rootElement.querySelectorAll(val)
  };

  /**
   * 
   */
  AMCtrl.prototype.refreshPermissions = function() {
    var self = this;
    _sdAuthorizationManagerService.getPermissions().then(function(permissions) {
      self.initializePermissionTree(permissions);
    }, function(err) {
      // handle error
    });
  };

  /**
   * 
   */
  AMCtrl.prototype.highlightParticipants = function() {
    alert('highlight participant');
  }

})();