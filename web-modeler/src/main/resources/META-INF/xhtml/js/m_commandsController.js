/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_globalVariables", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_command" ],
		function(m_utils, m_globalVariables, m_constants, m_communicationController, m_command) {
			var executeImmediate;
			var needUndoSupport;
			var readonlyExcludeCommandIds = [ "modelLockStatus.update" ];

			return {
				init : function(immediate, undoSupport) {
					executeImmediate = immediate;
					needUndoSupport = undoSupport;
				},

				handleCommand : function(command) {
					if (executeImmediate) {
						command.execute();
					}
				},

				submitImmediately : function(command, successCallback,
						errorCallback) {
					getInstance().submitImmediately(command, successCallback,
							errorCallback);
				},

				submitCommand : function(command, withBroadcast) {
					if (isValid(command)) {
						return getInstance().submitCommand(command, withBroadcast);
					}
				},
				registerCommandHandler : function(commandHandler, manualUnload) {
					getInstance().registerCommandHandler(commandHandler);

					if (!manualUnload) {
						unregisterCommandhandlerOnWindowUnload(commandHandler);
					}
				},
				unregisterCommandHandler : function(commandHandler) {
					getInstance().unregisterCommandHandler(commandHandler);
				},
				broadcastCommand : function(command) {
					getInstance().broadcastCommand(command);
				},
        broadcastError : function(command, response) {
          getInstance().broadcastError(command, response);
        },
				broadcastCommandUndo : function(command) {
					getInstance().broadcastCommandUndo(command);
				}
			};

			/*
			 *
			 */
			function isValid(command) {
				var checkForReadonly = true;
				for (n in readonlyExcludeCommandIds) {
					if (readonlyExcludeCommandIds[n] == command.commandId){
						checkForReadonly = false;
						break;
					}
				}

				if (checkForReadonly && command.modelId != undefined) {
					var models = m_globalVariables.get("models");
					var model = models[command.modelId]; //m_model.findModel(command.modelId);
					if (model != undefined) {
						if (model.isReadonly() && !model.isSimple()) {
							m_utils.debug("Model '" + model.name + "' is marked as Readonly. Skipping server post.");
							return false;
						} else {
							var chgDesc = [];
							for( var i in command.changeDescriptions) {
								var elem = model.findModelElementByUuid(command.changeDescriptions[i].uuid);
								if (elem && elem.isReadonly() && !model.isSimple()) {
									m_utils.debug("Model Element '" + model.name + "/" + elem.name + "' is marked as Readonly. Skipping server post.");
								} else {
									chgDesc.push(command.changeDescriptions[i]);
								}
							}

							if (chgDesc.length > 0) {
								command.changeDescriptions = chgDesc;
							} else {
								return false;
							}
						}
					}
				}

				return true;
			}

			/**
			 * Singleton on DOM level.
			 */
			function getInstance() {
				if (m_globalVariables.get("commandsController") == null) {
					m_globalVariables.set("commandsController", new CommandsController());
				}

				return m_globalVariables.get("commandsController");
			}

			function unregisterCommandhandlerOnWindowUnload(commandHandler) {
				if (!window.callbackScope) {
					window.callbackScope = {};
				}
				if (!window.callbackScope.objectsToUnregister) {
					window.callbackScope.objectsToUnregister = new Array();
					window.onunload = function() {
						if (this.callbackScope
								&& this.callbackScope.objectsToUnregister) {
							for ( var i = 0; i < this.callbackScope.objectsToUnregister.length; i++) {
								m_utils
										.debug("Unregistering command handler: "
												+ this.callbackScope.objectsToUnregister[i]);
								getInstance()
										.unregisterCommandHandler(
												this.callbackScope.objectsToUnregister[i]);
							}
						}
					};
				}
				window.callbackScope.objectsToUnregister.push(commandHandler);
			}

			/**
			 *
			 */
			function CommandsController(newCommunicationController) {
				// Initialize members

				this.communicationController = newCommunicationController;
				this.commandsQueue = [];
				this.commandHandlers = [];

				/**
				 *
				 */
				CommandsController.prototype.toString = function() {
					return "Lightdust.CommandController";
				};

				CommandsController.prototype.queue = function(command) {
				};

				CommandsController.prototype.submitQueue = function() {
				};

				CommandsController.prototype.submitImmediately = function(
						command, successCallback, errorCallback) {
					var transferObject = null;

					m_utils.debug("*** Submit ****");
					m_utils.debug(successCallback);

					if (command.type == m_command.RETRIEVE
							|| command.type == m_command.CREATE
							|| command.type == m_command.UPDATE) {
						m_communicationController
								.postData(
										{
											"url" : m_communicationController
													.getEndpointUrl()
													+ command.url
										},
										command.data,
										new function() {
											return {
												"success" : function(serverData) {
													if (successCallback != null) {
														m_utils
																.debug("===> Callback Scope");
														m_utils
																.debug(successCallback.callbackScope);
														m_utils
																.debug("===> Callback Method "
																		+ successCallback.method);
														m_utils
																.debug("===> Server Data ");
														m_utils
																.debug(serverData);

														successCallback.callbackScope[successCallback.method]
																(serverData);
													}
												},
												"error" : function(serverData) {
													if (errorCallback != null) {
														errorCallback
																.method(
																		errorCallback.callbackScope,
																		serverData);
													}
												}
											};
										});
					} else {
						m_communicationController.deleteData({
							"url" : m_communicationController.getEndpointUrl()
									+ command.url
						}, command.data, new function() {
							return {
								"success" : function(serverData) {
									if (successCallback != null) {
										successCallback.method(
												successCallback.callbackScope,
												serverData);
									}
								},
								"error" : function(serverData) {
									if (errorCallback != null) {
										errorCallback.method(
												errorCallback.callbackScope,
												serverData);
									}
								}
							};
						});

					}
				};

				/**
				 *
				 */
				CommandsController.prototype.submitCommand = function(command, withBroadcast) {
					var url = m_communicationController.getEndpointUrl()
							+ command.path;
					var obj = [];

					// make parameter default to true
					withBroadcast = (typeof withBroadcast !== 'undefined') ? withBroadcast : true;

					if (command.operation != null) {
						url += "/" + command.operation;
					}

					m_utils.debug("\n===> Post Command:\n");
					m_utils.debug(command);

					var deferred = jQuery.Deferred();

					if (command.type == m_constants.DELETE_COMMAND) {
						m_communicationController
								.deleteData(
										{
											"url" : url
										},
										JSON.stringify(command),
										new function() {
											return {
												"success" : function(command) {
													m_utils
															.debug("\n===> Receive Command Confirmation:\n");
													m_utils.debug(command);

													if (withBroadcast) {
														getInstance().broadcastCommand(command);
													}
													deferred.resolve(command);
												},
												"error" : function(response) {
													if (withBroadcast) {
														getInstance().broadcastError(command, response);
													}
													deferred.reject(response);
												}
											};
										});

					} else {
						m_communicationController
								.postData(
										{
											"url" : url,
											"sync" : command.sync ? true // Optional
											// param
											// for
											// sync
											// submit
											: false
										},
										// TODO Needs to be reviewed: it is a
										// guard but any unwanted reference
										// should have been removed before as we
										// intend to control what is passed to
										// the server
										// Added to remove any cyclic reference
										JSON.stringify(command, function(key,
												val) {
											if ((typeof val == "object") && null != val) {
												if (obj.indexOf(val) >= 0) {
													return undefined;
												}
												obj.push(val);
											}
											return val;
										}),
										new function() {
											return {
												"success" : function(command) {
													m_utils
															.debug("\n===> Receive Command Confirmation\n");
													m_utils.debug(command);

													if (!command.problems) {
														if (withBroadcast) {
															getInstance().broadcastCommand(command);
														}
														deferred.resolve(command);
													} else {
														deferred.reject(command);
													}
												},
												"error" : function(response) {
													if (withBroadcast) {
														getInstance().broadcastError(command, response);
													}
													deferred.reject(response);
												}
											};
										});
					}

					return deferred.promise();
				};

				/**
				 *
				 */
				CommandsController.prototype.registerCommandHandler = function(
						commandHandler) {
					this.commandHandlers.push(commandHandler);
				};

				/**
				 *
				 */
				CommandsController.prototype.unregisterCommandHandler = function(
						commandHandler) {
					m_utils.removeItemFromArray(this.commandHandlers,
							commandHandler);
				};

				/**
				 *
				 */
				CommandsController.prototype.broadcastCommand = function(
						command) {
					m_utils.debug("===> Broadcast Command:");
					m_utils.debug(command);

					for ( var n = 0; n < this.commandHandlers.length; ++n) {
						m_utils.debug("Process command on");
						m_utils.debug(this.commandHandlers[n]);
						try {
							if (this.commandHandlers[n]) {
								this.commandHandlers[n].processCommand(command);
							}
						} catch (e) {
							m_utils
									.debug("Exception while invoking command handler "
											+ e);
						}
					}
				};

        CommandsController.prototype.broadcastError = function(command, response) {
          m_utils.debug("===> Broadcast Error:");
          m_utils.debug(command);
          m_utils.debug(response);

          for (var n = 0; n < this.commandHandlers.length; ++n) {
            try {
              if (this.commandHandlers[n] && this.commandHandlers[n].processCommandError) {
                this.commandHandlers[n].processCommandError(command, response);
              }
            } catch (e) {
              m_utils.debug("Exception while invoking error handler " + e);
            }
          }
        };

				/**
         *
         */
				CommandsController.prototype.broadcastCommandUndo = function(
						command) {
					m_utils.debug("===> Broadcast Command Undo:");

					m_utils.debug(command);

					for ( var n = 0; n < this.commandHandlers.length; ++n) {
						try {
							this.commandHandlers[n].processCommand(command);
							// this.commandHandlers[n].undoCommand(command);
							// TODO do we still need a specific undo?
						} catch (e) {
							m_utils.debug("Failed broadcasting undo: " + e);
						}
					}
				};
			}
		});