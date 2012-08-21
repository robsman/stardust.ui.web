/**
 *
 * @author Francesca.Herpertz
 *
 */
define(
		[ "m_commandsController", "m_urlUtils", "m_user", "jquery.atmosphere" ],
		function(m_commandsController, m_urlUtils, m_user) {

			var socket = jQuery.atmosphere;
			var request = new jQuery.atmosphere.AtmosphereRequest();
			var subsocket = null;

			return {
				init : function(url) {
					request.contentType = "application/json";
					request.transport = "websocket";
					request.fallbackTransport = 'long-polling';
					request.loglevel = "debug";
					request.url = m_urlUtils.getContextName() + '/services/streaming/bpm-modeling/collaboration'
							+ url;

					request.onOpen = function(response) {
						response.responseBody;
						socket.info(response.responseBody);
					};

					request.onMessage = function(response) {
						var currentUser = m_user.getCurrentUser();
						var message = response.responseBody;
						var obj = jQuery.parseJSON(message);

						if ((obj.type != null && obj.type == "SUBMIT_CHAT_MESSAGE_COMMAND")
								|| (obj.commandId != null && currentUser.account != obj.account)) {
							m_commandsController.broadcastCommand(obj);
						}

					};

					request.onReconnect = function(request, response) {
						socket.info(response.responseBody);
					};

					request.onErrer = function(response) {
						socket.info(response.responseBody);
					};

					subsocket = socket.subscribe(request);
				},
				send : function(message) {
					subsocket.push(message);
				}
			};

		});
