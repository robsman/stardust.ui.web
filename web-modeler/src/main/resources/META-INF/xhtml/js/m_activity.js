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
				"bpm-modeler/js/m_modelElement", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_modelElement, m_model, m_accessPoint,
				m_i18nUtils) {

			return {
				create : function() {
					return new Activity();
				},
				createActivity : function(process, type) {
					var index = process.getNewActivityIndex();
					var activity = new Activity();

					var actNamePrefix = m_i18nUtils
							.getProperty("modeler.diagram.newActivity.namePrefix");
					activity.initialize(actNamePrefix + " " + index, type);

					activity.taskType = m_constants.MANUAL_TASK_TYPE;

					return activity;
				},

				createActivityFromProcess : function(process, subprocess) {
					var index = process.getNewActivityIndex();
					var activity = new Activity();

					activity.initialize(subprocess.name + index,
							m_constants.SUBPROCESS_ACTIVITY_TYPE);

					activity.subprocessFullId = subprocess.getFullId();

					return activity;
				},

				createActivityFromApplication : function(process, application) {
					var index = process.getNewActivityIndex();
					var activity = new Activity();

					activity.initialize(application.name + index,
							m_constants.TASK_ACTIVITY_TYPE);

					// TODO Put at a central place

					if (application.applicationType == "jms") {
						activity.taskType = m_constants.RECEIVE_TASK_TYPE;
					} else if (application.applicationType == "mailBean") {
						activity.taskType = m_constants.SEND_TASK_TYPE;
					} else if (application.applicationType == "messageTransformationBean") {
						activity.taskType = m_constants.SCRIPT_TASK_TYPE;
					} else if (application.applicationType == "interactive") {
						activity.taskType = m_constants.USER_TASK_TYPE;
					} else {
						activity.taskType = m_constants.SERVICE_TASK_TYPE;
					}

					activity.applicationFullId = application.getFullId();

					return activity;
				},

				createGatewayActivity : function(process) {
					var index = process.getNewGatewayIndex();
					var activity = new Activity("Gateway" + index);

					activity.initialize("", m_constants.GATEWAY_ACTIVITY_TYPE);

					activity.type = m_constants.ACTIVITY;
					activity.taskType = m_constants.NONE_TASK_TYPE;
					activity.gatewayType = m_constants.XOR_GATEWAY_TYPE;

					return activity;
				},
				typeObject : function(json) {
					m_utils.inheritMethods(json, new Activity());

					return json;
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
				 * TODO Needed?
				 */
				Activity.prototype.hasDefaultContext = function() {
					return this.activityType == m_constants.TASK_ACTIVITY_TYPE;
				};

				/**
				 *
				 */
				Activity.prototype.getContexts = function() {
					// TODO Should/might be evaluated on the server
					if (this.activityType == m_constants.TASK_ACTIVITY_TYPE
							&& this.applicationFullId) {
						var application = m_model
								.findApplication(this.applicationFullId);

						return application.contexts;
					}

					return this.contexts;
				};

				/**
				 *
				 */
				Activity.prototype.hasInputAccessPoints = function() {
					var contexts = this.getContexts();

					for ( var key in contexts) {
						for ( var n = 0; n < contexts[key].accessPoints.length; ++n) {
							if (contexts[key].accessPoints[n].direction == m_constants.IN_ACCESS_POINT) {
								return true;
							}
						}
					}

					return false;
				};

				/**
				 *
				 */
				Activity.prototype.hasOutputAccessPoints = function() {
					var contexts = this.getContexts();

					for ( var key in contexts) {
						for ( var n = 0; n < contexts[key].accessPoints.length; ++n) {
							if (contexts[key].accessPoints[n].direction == m_constants.OUT_ACCESS_POINT) {
								return true;
							}
						}
					}

					return false;
				};
			}
		});