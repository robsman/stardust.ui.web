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
		[ "m_utils", "m_constants", "m_modelElement", "m_model",
				"m_accessPoint" ],
		function(m_utils, m_constants, m_modelElement, m_model, m_accessPoint) {

			return {
				createActivity : function(process, type) {
					var index = process.getNewActivityIndex();
					var activity = new Activity("Activity" + index);

					activity.initialize("Activity " + index, type);

					activity.activityType == m_constants.MANUAL_ACTIVITY_TYPE

					return activity;
				},

				createActivityFromProcess : function(process, subprocess) {
					var index = process.getNewActivityIndex();
					var activity = new Activity(subprocess.id + index);

					activity.initialize(subprocess.name + index,
							m_constants.SUBPROCESS_ACTIVITY_TYPE);

					activity.subprocessFullId = subprocess.getFullId();

					return activity;
				},

				createActivityFromApplication : function(process, application) {
					var index = process.getNewActivityIndex();
					var activity = new Activity(application.id + index);

					activity.initialize(application.name + index,
							m_constants.APPLICATION_ACTIVITY_TYPE);

					activity.applicationFullId = application.getFullId();

					return activity;
				},

				createGatewayActivity : function(process) {
					var index = process.getNewGatewayIndex();
					var activity = new Activity("Gateway" + index);

					activity.initialize("Gateway " + index,
							m_constants.GATEWAY_ACTIVITY_TYPE);

					activity.type = m_constants.GATEWAY;
					activity.gatewayType = m_constants.XOR_GATEWAY_TYPE;

					return activity;
				},

				prototype : Activity.prototype
			};

			/**
			 * 
			 */
			function Activity(id) {
				var modelElement = m_modelElement.create();

				m_utils.inheritFields(this, modelElement);
				m_utils.inheritMethods(Activity.prototype, modelElement);

				this.type = m_constants.ACTIVITY;
				this.id = id;
				this.name = null;
				this.description = null;
				this.attributes = {};
				this.activityType = null;
				this.subprocessId = null;
				this.applicationFullId = null;
				this.participantFullId = null;
				this.processingType = m_constants.SINGLE_PROCESSING_TYPE;

				/**
				 * 
				 */
				Activity.prototype.toString = function() {
					return "Lightdust.Activity";
				};

				/**
				 * 
				 */
				Activity.prototype.initialize = function(name, activityType) {
					this.name = name;
					this.description = null;
					this.activityType = activityType;
					this.accessPoints = {};
					this.subprocessFullId = null;
					this.applicationFullId = null;
					this.participantFullId = null;
				};

				/**
				 * 
				 */
				Activity.prototype.hasDefaultContext = function() {
					return this.activityType == m_constants.APPLICATION_ACTIVITY_TYPE;
				};

				/**
				 * 
				 */
				Activity.prototype.getAccessPoints = function() {
					// TODO Should/might be evaluated on the server
					if (this.activityType == m_constants.APPLICATION_ACTIVITY_TYPE) {
						var application = m_model
								.findApplication(this.applicationFullId);

						if (application.interactive) {
							for ( var id in application.contexts) {
								return application.contexts[id].accessPoints;
							}
						}
					}

					return this.accessPoints;
				};
			}
		});