/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Shrikant.Gangal
 */

(function() {
	'use strict';

	angular.module('modeler-ui').controller(
			'sdAuthorizationPageController',
			[ '$scope', 'sdUtilService', 'sdI18nService', 'sdModelerConstants',
					AuthorizationPageController ]);

	/**
	 * 
	 */
	function AuthorizationPageController($scope, sdUtilService,
			sdI18nService, sdModelerConstants) {
		var self = this;
		this.i18n = sdI18nService.getInstance('bpm-modeler-messages').translate;
		this.constants = sdModelerConstants;
		this.selectedAuths = [];
		this.element;
		this.model;
		this.scope = $scope;
		this.selectedPermissionsCache = [];

		$scope.$on('REFRESH_PROPERTIES_PANEL',
				function(event, propertiesPanel) {
					self.init();
					self.refresh();
				});
		$scope.$on('VIEW_MODEL_ELEMENT_INITIALIZED',
				function(event, propertiesPanel) {
					self.init();
					self.refresh();
				});
	}
	
	/**
	 * 
	 */
	AuthorizationPageController.prototype.init = function() {
		this.propertiesPanel = this.scope.page.propertiesPanel;
		this.commandsController = this.propertiesPanel
				.getMCommandsController();
		this.commandHelper = this.propertiesPanel.getMCommand();
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.refresh = function() {
		var self = this;
		if (this.propertiesPanel.getModelElement && this.propertiesPanel.getModelElement()) {
			var elem = this.propertiesPanel.getModelElement();
		} else  if (this.propertiesPanel.element.type === 'processDefinition'
				|| this.propertiesPanel.element.type === 'process') {
			var elem = this.propertiesPanel.element;
		} else {
			var elem = this.propertiesPanel.element.modelElement;
		}
		if (!this.element || this.element != elem) {
			this.element = elem;
			this.model = this.propertiesPanel.getModel();
			// Reset selected participants and authorizations
			this.selectedAuths = [];
			this.populateParticipants();
		} else {
			this.applyCachedSelections();
			this.resetParticipantSelection();
		}
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.populateParticipants = function() {
		this.allParticipants = [];
		var self = this;
		jQuery.each(this.model.participants, function(_, participant) {
			self.allParticipants.push({
				modelName : participant.model.name,
				name : participant.name,
				type : participant.type,
				displayName : participant.name,
				fullId : participant.getFullId()
			});
		});

		// TODO check if cross modelling is supported - doens't seem to work
//		var models = this.propertiesPanel.getMModel().getModels();
//		jQuery.each(models, function(id, model) {
//			if (model.id !== self.model.id) {
//				jQuery.each(model.participants, function(_, participant) {
//					if (participant.id !== 'Administrator') {
//						self.allParticipants.push({
//							modelName : model.name,
//							name : participant.name,
//							type : participant.type,
//							displayName : model.name + "/" + participant.name,
//							fullId : participant.getFullId()
//						});
//					}
//				});
//			}
//		});
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.addParticipantsToPermissions = function() {
		var self = this;
		var selectedParticipants = this.getSelectedParticipants();
		var nochanges = true;
		if (selectedParticipants.length > 0) {
			jQuery
					.each(
							this.element.permissions,
							function(_, permission) {
								if (permission.selected) {
									jQuery
											.each(
													permission.participants,
													function(_, participant) {
														if (selectedParticipants
																.indexOf(participant.participantFullId) >= 0) {
															selectedParticipants
																	.splice(
																			selectedParticipants
																					.indexOf(participant.participantFullId),
																			1);
														}
													});

									var shortlist = [];
									jQuery
											.each(
													selectedParticipants,
													function(_,
															participantFullId) {
														shortlist
																.push({
																	participantFullId : participantFullId
																});
													})
									if (shortlist.length > 0) {
										self.commandsController
												.submitCommand(self.commandHelper
														.createAddPermissionParticipantsCommand(
																self.model.id,
																self.element.uuid,
																{
																	permissionID : permission.id,
																	participants : shortlist
																}));
										nochanges = false;
									}
								}
							});
		}

		if (nochanges) {
			// TODO - display message
		}
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.addAllParticipantsToPermissions = function() {
		var self = this;
		jQuery.each(this.element.permissions, function(_, permission) {
			if (permission.selected) {
				self.commandsController.submitCommand(self.commandHelper
						.createPermissionSetAllCommand(self.model.id,
								self.element.uuid, {
									permissionID : permission.id
								}));
			}
		});
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.restorePermissionDefaults = function() {
		var self = this;
		jQuery.each(this.element.permissions, function(_, permission) {
			if (permission.selected) {
				self.commandsController.submitCommand(self.commandHelper
						.createPermissionRestoreDefaultsCommand(
								self.model.id, self.element.uuid, {
									permissionID : permission.id
								}));
			}
		});
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.removeParticipant = function(
			permissionId, participantFullId) {
		var self = this;

		this.addToSelectionsCache(permissionId);
		self.commandsController.submitCommand(self.commandHelper
				.createRemovePermissionParticipantCommand(
						self.model.id, self.element.uuid, {
							permissionID : permissionId,
							participantFullId : participantFullId
						}));
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.getSelectedPermissions = function() {
		var selectedPermissions = [];
		jQuery.each(this.element.permissions, function(_, permission) {
			if (permission.selected) {
				selectedPermissions.push(permission);
			}
		});

		return selectedPermissions;
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.getSelectedParticipants = function() {
		var selectedParticipants = [];
		jQuery.each(this.allParticipants, function(_, participant) {
			if (participant.selected) {
				selectedParticipants.push(participant.fullId);
			}
		});

		return selectedParticipants;
	};

	/**
	 * TODO - move to service
	 */
	AuthorizationPageController.prototype.getParticipantDisplayName = function(
			fullId) {
		if (fullId) {
			var m_model = this.propertiesPanel.getMModel();
			var participant = m_model.findParticipant(fullId);
			return participant.model.name + "/" + participant.name;
		}
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.getParticipantIcon = function(fullId) {
		if (fullId) {
			var m_model = this.propertiesPanel.getMModel();
			var participant = m_model.findParticipant(fullId);
			if (participant.type === 'conditionalPerformerParticipant') {
				return "plugins/bpm-modeler/images/icons/conditional.png";
			} else if (participant.type === 'organizationParticipant') {
				return "plugins/bpm-modeler/images/icons/organization.png";
			} else {
				return "plugins/bpm-modeler/images/icons/role.png";
			}
		}
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.isAddAllEnabled = function() {
		return (this.getSelectedParticipants().length == 0 && this.selectedPermissionsCache.length > 0);
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.isAddSelectedEnabled = function() {
		return (this.getSelectedParticipants().length > 0 && this.selectedPermissionsCache.length > 0);
	};
	
	/**
	 * TODO - for now only single selection is supported
	 */
	AuthorizationPageController.prototype.togglePermissionSelection = function(
			permission) {
		if (permission.selected) {
			permission.selected = false;
			this.removeFromSelectionsCache(permission.id);
		} else {
			jQuery.each(this.element.permissions, function(_, perm) {
				perm.selected = false;
			});
			// Empty selections cache
			this.selectedPermissionsCache.length = 0;

			permission.selected = true;
			this.addToSelectionsCache(permission.id);
		}
	};

	/**
	 * TODO - move to service
	 */
	AuthorizationPageController.prototype.toggleSelection = function(obj) {
		obj.selected = (obj && obj.selected) ? false : true;
	};

	/**
	 * 
	 */
	AuthorizationPageController.prototype.addToSelectionsCache = function(
			permissionId) {
		this.selectedPermissionsCache.push(permissionId);
	}

	/**
	 * 
	 */
	AuthorizationPageController.prototype.removeFromSelectionsCache = function(
			permissionId) {
		if (this.selectedPermissionsCache.indexOf(permissionId) >= 0) {
			this.selectedPermissionsCache.splice(this.selectedPermissionsCache
					.indexOf(permissionId), 1);
		}
	}

	/**
	 * 
	 */
	AuthorizationPageController.prototype.applyCachedSelections = function() {
		var self = this;
		jQuery.each(this.element.permissions, function(_, permission) {
			if (self.selectedPermissionsCache.indexOf(permission.id) >= 0) {
				//permission.selected = true;
				permission.expanded = true;
			}
		});
		this.selectedPermissionsCache.length = 0;
	}

	/**
	 * 
	 */
	AuthorizationPageController.prototype.resetParticipantSelection = function() {
		jQuery.each(this.allParticipants, function(_, participant) {
			participant.selected = false;
		});
	}
})();