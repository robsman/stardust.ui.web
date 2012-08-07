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

define([ "m_utils", "m_constants", "m_modelElement" ], function(m_utils, m_constants, m_modelElement) {

	return {
		initializeFromJson : function(model, json) {
			return initializeFromJson(model, json);
		},
		deleteParticipantRole : function(id,model) {
			delete model.participants[id];
		}
	};

	function initializeFromJson(model, json) {
		// TODO Ugly, use prototype					
		m_utils.typeObject(json, new Participant());

		json.initializeFromJson(model);

		return json;		
	}
	/**
	 * 
	 */
	function Participant() {
		m_utils.inheritMethods(Participant.prototype, m_modelElement.create());

		this.id = null;
		this.name = null;
		this.description = null;

		/**
		 * 
		 */
		Participant.prototype.toString = function() {
			return "Lightdust.Participant";
		};

		/**
		 * 
		 */
		Participant.prototype.initializeFromJson = function(model) {
			this.model = model;
			this.model.participants[this.id] = this;
			for ( var cParticipant in this.childParticipants) {
				initializeFromJson(model, this.childParticipants[cParticipant]);
			}
		};

		/**
		 * 
		 */
		Participant.prototype.rename = function(id, name) {
			delete this.model.participants[this.id];

			this.id = id;
			this.name = name;

			this.model.participants[this.id] = this;
		};
	}
});