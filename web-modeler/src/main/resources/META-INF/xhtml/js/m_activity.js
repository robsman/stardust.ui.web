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
					var index = process.getNewActivityIndex();
					var activity = new Activity("Gateway" + index);

					activity.initialize("Gateway " + index,
							m_constants.GATEWAY_ACTIVITY_TYPE);

					activity.type = m_constants.GATEWAY;
					activity.gatewayType = m_constants.AND_GATEWAY_TYPE;

					return activity;
				},

				prototype : Activity.prototype
			};

			/**
			 * 
			 */
			function Activity(id) {
				m_utils.inheritMethods(Activity.prototype, m_modelElement
						.create());

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

					if (this.activityType == m_constants.MANUAL_ACTIVITY_TYPE) {
						var accessPoint = m_accessPoint.createDefault(
								"Default", "Default",
								m_constants.INOUT_ACCESS_POINT);

						this.accessPoints[accessPoint.id] = accessPoint;
					}

					this.subprocessFullId = null;
					this.applicationFullId = null;
					this.participantFullId = null;
				};

				/**
				 * 
				 */
				Activity.prototype.getAccessPoints = function(name,
						activityType) {

					if (this.activityType == m_constants.APPLICATION_ACTIVITY_TYPE) {
						var application = m_model
								.findApplication(this.applicationFullId);

						return application.accessPoints;
					} else if (this.activityType == m_constants.SUBPROCESS_ACTIVITY_TYPE) {
						// TODO Add logic, e.g. for Process Interfaces
						return {};
					} else {
						return this.accessPoints;
					}
				};
			}
		});