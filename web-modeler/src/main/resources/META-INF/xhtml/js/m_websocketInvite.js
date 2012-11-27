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
 *
 * @author Francesca.Herpertz
 *
 */
define([ "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_communicationController", "jquery.atmosphere" ],
		function(m_commandsController, m_urlUtils, m_utils, m_constants, m_communicationController) {

	var socket = jQuery.atmosphere;
	var request = new jQuery.atmosphere.AtmosphereRequest();
	var subsocket = null;
	var isInitial = true;

	return {
		init : function(url) {
			request.contentType = "application/json";
			request.transport = "websocket";
			request.fallbackTransport = 'long-polling';
			request.loglevel = "debug";
			request.url = m_urlUtils.getContextName() + '/services/streaming/bpm-modeling/collaboration' + url;

			request.onOpen = function(response) {
				console.log(response);
				if(isInitial == true){
					update();
				}
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

	function update(){
		console.log("fiep");
		var url = m_urlUtils.getContextName()+ "/services/rest/bpm-modeler/modeler/" + new Date().getTime()+"/users/getOfflineInvites";
		isInitial = false;
		m_communicationController.getHead({
			"url" : url
			}, new function() {
				return {
					"success" : function(command) {
						m_utils.debug("recived command ok");
					},
					"error" : function(command) {
						m_utils.debug("recived command error"
									+ command);
					}
				};
			});


	};

});
