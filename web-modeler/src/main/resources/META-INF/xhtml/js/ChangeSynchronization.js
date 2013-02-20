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
				create : function(changeDescriptor) {
					var synchronization = new ChangeSynchronization();

					synchronization.initialize(changeDescriptor);

					return synchronization;
				}
			};

			/**
			 * 
			 */
			function ChangeSynchronization() {
				/**
				 * 
				 */
				ChangeSynchronization.prototype.initialize = function(
						changeDescriptor) {
					this.changeDescriptor = changeDescriptor;

					m_utils
							.debug("===> Initializing ChangeSynchronization with Change Descriptor");
					m_utils.debug(this.changeDescriptor);

					m_commandsController.registerCommandHandler(this, true);
				};

				/**
				 * 
				 */
				ChangeSynchronization.prototype.processCommand = function(
						changeDescriptor) {
					// TODO Find ways to identify matching change
					if (true) {
						// TODO Heuristics to find the primary object
						// TODO Object seems to be bound its type as this is done by command processing in Outline (?); guarantee order?

						if (changeDescriptor.changes.added) {
							this.doneCallback(changeDescriptor.changes.added[0]);
						} else if (changeDescriptor.changes.modified) {
							this.doneCallback(changeDescriptor.changes.modified[0]);
						} else {
							this.doneCallback(changeDescriptor.changes.removed[0]);
						}
					}
					
					// TODO May need on off cammadn handlers?
					//m_commandsController.unregisterCommandHandler(this);
				};

				/**
				 * 
				 */
				ChangeSynchronization.prototype.done = function(doneCallback) {
					this.doneCallback = doneCallback;

					return this;
				};

				/**
				 * 
				 */
				ChangeSynchronization.prototype.fail = function(failCallback) {
					this.failCallback = failCallback;

					return this;
				};
			}
		});