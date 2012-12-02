/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants" ], function(
		m_utils, m_constants) {

	return {
		createStartEvent : function(process) {
			var event = new Event();

			event.initialize("", "", m_constants.START_EVENT_TYPE);

			return event;
		},
		createIntermediateEvent : function(process) {
			var event = new Event();

			event.initialize("", "", m_constants.INTERMEDIATE_EVENT_TYPE);

			return event;
		},
		createStopEvent : function(process) {
			var event = new Event();

			event.initialize("", "", m_constants.STOP_EVENT_TYPE);

			return event;
		},
		typeObject : function(json) {
			m_utils.inheritMethods(json, new Event());

			return json;
		},
		prototype : Event.prototype
	};

	/**
	 * 
	 */
	function Event() {
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
			} else if (this.eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
				this.eventClass = m_constants.MESSAGE_EVENT_CLASS;
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
			this.bindingActivityUuid = activity.id; // TODO use UUID later
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
	}
});