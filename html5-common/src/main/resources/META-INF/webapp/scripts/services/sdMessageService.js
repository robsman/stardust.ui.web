/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

(function(){
	'use strict';

	angular.module('bpm-common.services').factory('sdMessageService', ['eventBus', function (eventBus) {
		
		function emitMessage(message, namespace) {
			if (!angular.isDefined(namespace)) {
				namespace = 'global.error';
			}
			eventBus.emitMsg(namespace, message);
		}
		
		return {
			
			/**
			 * Shows the message wherever sd-message is used.
			 *  
			 * message: A string or an object {message: <message>, type: <type>}. 
			 * 			Supported type values : 'error' / 'info' / 'warn' 
			 * namespace: A namespace that eventBus uses to push the message against. Default is 'global.error'.
			 * 
			 */
			showMessage: function(message, namespace) {
				emitMessage(message, namespace);
			}
		};
	}]);
})();
