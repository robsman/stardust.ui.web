/**
 *
 * @author Francesca.Herpertz
 *
 */
define([ "m_commandsController", "m_urlUtils", "m_user", "m_constants",
		"jquery.atmosphere" ], function(m_commandsController, m_urlUtils,
		m_user, m_constants) {

	var socket = jQuery.atmosphere;
	var request = new jQuery.atmosphere.AtmosphereRequest();
	var subsocket = null;

	return {
		init : function(url) {
			request.contentType = "application/json";
			request.transport = "websocket";
			request.fallbackTransport = 'long-polling';
			request.loglevel = "debug";
			request.url = m_urlUtils.getContextName() + '/services/streaming/bpm-modeling/collaboration' + url;

			request.onOpen = function(response) {
				console.log(response);
			};

			request.onMessage = function(response) {
				var message = response.responseBody;
				var obj = jQuery.parseJSON(message);
				m_commandsController.broadcastCommand(obj);

			};

			request.onReconnect = function(request, response) {
				socket.info("Reconnecting");
			};

			request.onErrer = function(response) {
				alert(response.responseBody);
			};

			subsocket = socket.subscribe(request);

			return true;

		},
		send : function(message) {
			subsocket.push(message);
		}

	};

});
