/**
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils"],
		function(m_utils) {

			return {
				submitCommand : function(command) {
					getInstance().broadcastCommand(command);
				},
				registerCommandHandler : function(commandHandler) {
					getInstance().registerCommandHandler(commandHandler);
					//unregisterCommandhandlerOnWindowUnload(commandHandler);
				},
				unregisterCommandHandler : function(commandHandler) {
					getInstance().unregisterCommandHandler(commandHandler);
				},
				broadcastCommand : function(command) {
					getInstance().broadcastCommand(command);
				}
			};

			/**
			 * Singleton on DOM level.
			 */
			function getInstance() {
				// Must not interfere with Browser Modeler

				if (window.top.commandsDispatcher == null) {
					window.top.commandsDispatcher = new CommandsDispatcher();
				}

				return window.top.commandsDispatcher;
			}

			/**
			 * 
			 */
			function CommandsDispatcher(newCommunicationController) {
				this.commandHandlers = [];
				
				/**
				 *
				 */
				CommandsDispatcher.prototype.registerCommandHandler = function(
						commandHandler) {
					this.commandHandlers.push(commandHandler);
				};

				/**
				 *
				 */
				CommandsDispatcher.prototype.unregisterCommandHandler = function(
						commandHandler) {
					m_utils.removeItemFromArray(this.commandHandlers, commandHandler);
				};

				/**
				 *
				 */
				CommandsDispatcher.prototype.broadcastCommand = function(
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

			}
		});