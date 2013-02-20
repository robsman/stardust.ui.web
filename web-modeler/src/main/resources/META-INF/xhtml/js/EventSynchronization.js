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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController" ],
		function(m_utils, m_constants, m_commandsController) {
			return {
				create : function(event, filter, body, windowStub) {
					var synchronization = new EventSynchronization();

					synchronization.initialize(event, filter, body, windowStub);

					return synchronization;
				}
			};

			/**
			 * Executes the body where the event event/filter is expected to be published and executes callbacks.
			 */
			function EventSynchronization() {
				/**
				 * 
				 */
				EventSynchronization.prototype.initialize = function(
						event, filter, body, windowStub) {
					m_utils
							.debug("===> Initializing EventSynchronization for Event " + event + " and filter " + filter);
					m_utils.debug(this.changeDescriptor);

					var self = this;
					
					windowStub.parent.EventHub.events.subscribe(event, function(){
						self.doneCallback();
					});
					
					// Execute the body
					
					body();
				};

				/**
				 * 
				 */
				EventSynchronization.prototype.done = function(doneCallback) {
					this.doneCallback = doneCallback;

					return this;
				};

				/**
				 * 
				 */
				EventSynchronization.prototype.fail = function(failCallback) {
					this.failCallback = failCallback;

					return this;
				};
			}
		});