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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_model",
		  "bpm-modeler/js/m_modelElement"],
		function(m_utils, m_constants, m_i18nUtils, m_model, m_modelElement) {

			return {
				createStartEvent : function(process) {
					var event = new Event();

					event.initialize("", "", m_constants.START_EVENT_TYPE);

					return event;
				},
				createIntermediateEvent : function(process) {
					var event = new Event();

					event.initialize("", "",
							m_constants.INTERMEDIATE_EVENT_TYPE);

					return event;
				},
				createStopEvent : function(process) {
					var event = new Event();

					event.initialize("", m_i18nUtils.getProperty("modeler.diagram.endEvent.defaultName"), m_constants.STOP_EVENT_TYPE);

					return event;
				},
				typeObject : function(json) {
					m_utils.inheritMethods(json, new Event());

					return json;
				},
				getPossibleEventClasses : getPossibleEventClasses,
				prototype : Event.prototype
			};

			/**
			 *
			 */
			function Event() {
				var modelElement = m_modelElement.create();
				m_utils.inheritFields(this, modelElement);
				m_utils.inheritMethods(Event.prototype, modelElement);

				this.type = m_constants.EVENT;
				this.id = null;
				this.name = null;
				this.description = null;
				this.eventType = null;
				this.eventClass = null;
				this.participantId = null;
				this.documentDataId = null;

				/**
				 *
				 */
				Event.prototype.toString = function() {
					return "Lightdust.Event";
				};

				/**
				 *
				 */
				Event.prototype.initialize = function(id, name, eventType) {
					this.id = id;
					this.name = name;
					this.eventType = eventType;
					this.attributes = {};

					if (this.eventType == m_constants.START_EVENT_TYPE) {
						this.eventClass = m_constants.NONE_EVENT_CLASS;
						this.interrupting = true;
						this.throwing = false;

						// Convenience setting to immediately create Manual
						// Triggers

						this.attributes["carnot:engine:eventIntegrationOverlay"] = "manualTrigger";
					} else if (this.eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
						this.eventClass = m_constants.TIMER_EVENT_CLASS;
						this.interrupting = true;
						this.throwing = false;
					} else {
						this.eventClass = m_constants.NONE_EVENT_CLASS;
						this.interrupting = true;
						this.throwing = true;
					}
				};

				/**
				 *
				 */
				Event.prototype.bindWithActivity = function(activity) {
					this.bindingActivityUuid = activity.id; // TODO use UUID
					// later
				};

				/**
				 *
				 */
				Event.prototype.unbindFromActivity = function() {
					this.bindingActivityUuid = null;
				};

				/**
				 *
				 */
				Event.prototype.isBoundaryEvent = function() {
					return this.bindingActivityUuid != null;
				};

				/**
				 *
				 */
				Event.prototype.getProcess = function() {
					var model = m_model.findModelByUuid(this.modelUUID);
					var process;
					for ( var i in model.processes) {
						var events = model.processes[i].events;
						for ( var j in events) {
							if (events[j].name === this.name
									&& events[j].id === this.id) {
								return model.processes[i];
							}
						}
					}

					return undefined;
				};
			}

			/**
			 *
			 */
			function getPossibleEventClasses(eventType, interrupting, throwing,
					boundary, subProcess) {
				if (eventType == m_constants.START_EVENT_TYPE) {
					if (subProcess) {
						if (interrupting) {
							return [ m_constants.MESSAGE_EVENT_CLASS,
										m_constants.TIMER_EVENT_CLASS,
										m_constants.ERROR_EVENT_CLASS];
						} else {
							return [ m_constants.MESSAGE_EVENT_CLASS,
										m_constants.TIMER_EVENT_CLASS];
						}
					} else {
						return [ m_constants.NONE_EVENT_CLASS,
								m_constants.MESSAGE_EVENT_CLASS,
								m_constants.TIMER_EVENT_CLASS ];
					}
				} else if (eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
					if (interrupting) {
						return [m_constants.NONE_EVENT_CLASS,
						        m_constants.TIMER_EVENT_CLASS,
								m_constants.ERROR_EVENT_CLASS ];
					} else {
						return [ m_constants.NONE_EVENT_CLASS,
						         m_constants.TIMER_EVENT_CLASS ];
					}

				} else if (eventType == m_constants.STOP_EVENT_TYPE) {
					return [ m_constants.NONE_EVENT_CLASS,
							m_constants.MESSAGE_EVENT_CLASS ];
				}
			}
		});