/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_communicationController", "m_command" ],
		function(m_utils, m_constants, m_communicationController, m_command) {
			var executeImmediate;
			var needUndoSupport;
			var commandsController = new CommandsController(null);

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
					commandsController.submitImmediately(command,
							successCallback, errorCallback);
				},

				submitCommand : function(command) {
					commandsController.submitCommand(command);
				},
				registerCommandHandler : function(commandHandler) {
					getInstance().registerCommandHandler(commandHandler);
				},
				broadcastCommand : function(command) {
					getInstance().broadcastCommand(command);
				},
				broadcastCommandUndo : function(command) {
					getInstance().broadcastCommandUndo(command);
				}
			};

			/**
			 * Singleton on DOM level.
			 */
			function getInstance() {
				if (window.top.commandsController == null) {
					window.top.commandsController = new CommandsController();
				}

				return window.top.commandsController;
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
				CommandsController.prototype.submitCommand = function(command) {
					var url = m_communicationController.getEndpointUrl()
							+ command.path;

					if (command.operation != null) {
						url += "/" + command.operation;
					}

					m_utils.debug("URL: " + url);
					m_utils.debug("Type:" + command.type);
					m_utils.debug("Old Object:");
					m_utils.debug(command.oldObject);
					m_utils.debug("New Object:");
					m_utils.debug(command.newObject);

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
															.debug("===> Receive Command Confirmation");
													m_utils.debug(command);

													getInstance()
															.broadcastCommand(
																	command);
												},
												"error" : function(command) {
												}
											};
										});

					} else {
						m_communicationController
								.postData(
										{
											"url" : url
										},
										JSON.stringify(command),
										new function() {
											return {
												"success" : function(command) {
													m_utils
															.debug("===> Receive Command Confirmation");
													m_utils.debug(command);

													getInstance()
															.broadcastCommand(
																	command);
												},
												"error" : function(command) {
												}
											};
										});
					}
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
				CommandsController.prototype.broadcastCommand = function(
						command) {
					m_utils.debug("===> Broadcast Command:");
					m_utils.debug(command);

					for ( var n = 0; n < this.commandHandlers.length; ++n) {
						m_utils.debug("Process command on "
								+ this.commandHandlers[n]);
						try {
							if (this.commandHandlers[n]) {
								this.commandHandlers[n].processCommand(command);
							}
						} catch (e) {
							m_utils.debug("Exception while invoking command handler " +  e);
						}
					}
				};

				/**
				 *
				 */
				CommandsController.prototype.broadcastCommandUndo = function(
						command) {
					m_utils.debug("===> Broadcast Command Undo:");

					// TODO Review

					if (command.type == m_constants.RENAME_COMMAND) {
						m_command.patchRenamePath(command);
					}

					m_utils.debug(command);

					for ( var n = 0; n < this.commandHandlers.length; ++n) {
						try {
							this.commandHandlers[n].undoCommand(command);
						} catch (e) {
							m_utils.debug("Failed broadcasting undo: " + e);
						}
					}
				};
			}
		});