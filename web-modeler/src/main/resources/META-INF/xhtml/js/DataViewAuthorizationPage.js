/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController,
				m_command, m_propertiesPage, m_i18nUtils) {
			return {
				create : function(propertiesPanel) {
					return new DataViewAuthorizationPage(
							propertiesPanel);
				}
			};
			
			/**
			 * 
			 */
			function DataViewAuthorizationPage(propertiesPanel) {
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, 'dataViewAuthorizationPage', m_i18nUtils.getProperty("modeler.common.authorization"));

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						DataViewAuthorizationPage.prototype,
						propertiesPage);
				
				/**
				 * 
				 */
				DataViewAuthorizationPage.prototype.setElement = function() {
					if (!this.ctrl) {
						this.ctrl = new AuthorizationPageController(propertiesPage, propertiesPanel);
					}
					this.propertiesPanel = propertiesPanel;
					this.ctrl.setElement();
				};
			}
			
			/**
			 * TODO - this is almost a duplicate of AuthorizationPageController from sdAuthorizationPageController.js
			 * Need to find a way to unify the code - convert to directive 
			 */
			function AuthorizationPageController(propertiesPage, propertiesPanel) {
				this.i18n = m_i18nUtils.getProperty;
				this.constants = m_constants;
				this.selectedAuths = [];
				this.model;
				this.selectedPermissionsCache = [];
				this.propertiesPanel = propertiesPanel;

				/**
				 * 
				 */
				AuthorizationPageController.prototype.setElement = function() {
					this.element = this.propertiesPanel.data;
					this.model = this.element.model;
					
					this.commandsController = propertiesPanel.getMCommandsController();
					this.commandHelper = propertiesPanel.getMCommand();
					
					this.populateParticipants();
					this.applyCachedSelections();
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

					var models = this.propertiesPanel.getMModel().getModels();
					jQuery.each(models, function(id, model) {
						if (model.id !== self.model.id) {
							jQuery.each(model.participants, function(_, participant) {
								if (participant.id !== 'Administrator') {
									self.allParticipants.push({
										modelName : model.name,
										name : participant.name,
										type : participant.type,
										displayName : model.name + "/" + participant.name,
										fullId : participant.getFullId()
									});
								}
							});
						}
					});
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
			}
		});