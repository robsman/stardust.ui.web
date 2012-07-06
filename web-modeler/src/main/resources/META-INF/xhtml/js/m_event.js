/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

define([ "m_utils", "m_constants" ], function(m_utils, m_constants) {

	return {
		createStartEvent : function(process) {
			var event = new Event();
			var index = process.getNewEventIndex();

			event.initialize("Event" + index, "Event " + index, m_constants.START_EVENT_TYPE);
			
			return event;
		},

		createStopEvent : function(process) {
			var event = new Event();
			var index = process.getNewEventIndex();

			event.initialize("Event" + index, "Event " + index, m_constants.STOP_EVENT_TYPE);
			
			return event;
		},
		
		prototype: Event.prototype
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
		};
	}
});