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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_modelElement" ],
		function(m_utils, m_constants, m_modelElement) {
			return {
				initializeFromJson : function(model, json) {
					// TODO Ugly, use prototype
					m_utils.typeObject(json, new Application());

					json.initializeFromJson(model);

					return json;
				},
				deleteApplication : function(id, model) {
					delete model.applications[id];
				}
			};

			/**
			 * 
			 */
			function Application() {
				m_utils.inheritMethods(Application.prototype, m_modelElement
						.create());

				/**
				 * 
				 */
				Application.prototype.toString = function() {
					return "Lightdust.Application";
				};

				/**
				 * 
				 */
				Application.prototype.initializeFromJson = function(model) {
					this.model = model;

					this.model.applications[this.id] = this;
				};

				/**
				 * 
				 */
				Application.prototype.rename = function(id, name) {
					delete this.model.applications[this.id];

					this.id = id;
					this.name = name;

					this.model.applications[this.id] = this;
				};

				/**
				 * 
				 */
				Application.prototype.getAccessPointById = function(id) {
					for ( var m in this.contexts) {
						for ( var n = 0; n < this.contexts[m].accessPoints.length; ++n) {
							if (this.contexts[m].accessPoints[n].id == id) {
								return this.contexts[m].accessPoints[n];
							}
						}
					}

					return null;
				};

				/**
				 * 
				 */
				Application.prototype.getCompatibleActivityTaskType = function() {
					if (this.applicationType === "webservice"
						|| ((this.applicationType === "camelSpringProducerApplication" || this.applicationType === "camelConsumerApplication")
								&& (this.attributes["carnot:engine:camel::applicationIntegrationOverlay"] === "restServiceOverlay"
										|| this.attributes["carnot:engine:camel::applicationIntegrationOverlay"] === "mailIntegrationOverlay"))) {
					return m_constants.SERVICE_TASK_TYPE;
					}
					if (this.interactive) {
						return m_constants.USER_TASK_TYPE;
					}
					if (this.applicationType === "messageTransformationBean"
							|| ((this.applicationType === "camelSpringProducerApplication" || this.applicationType === "camelConsumerApplication")
								&& (this.attributes["carnot:engine:camel::applicationIntegrationOverlay"] === "genericEndpointOverlay"
										|| this.attributes["carnot:engine:camel::applicationIntegrationOverlay"] === "scriptingIntegrationOverlay"))) {
						return m_constants.SCRIPT_TASK_TYPE;
					}
					if ((this.applicationType === "camelSpringProducerApplication" || this.applicationType === "camelConsumerApplication")
							&& this.attributes["carnot:engine:camel::applicationIntegrationOverlay"] === "rulesIntegrationOverlay") {
						return m_constants.RULE_TASK_TYPE;
					}
					if (this.applicationType == "jms") {
						return m_constants.RECEIVE_TASK_TYPE;
					}
					if (this.applicationType == "mailBean") {
						return m_constants.SEND_TASK_TYPE;
					}
					
					// TODO - check if setting task type to service task if none of the
					// above conditions are satisfied
					return m_constants.SERVICE_TASK_TYPE;
				};
			}
		});