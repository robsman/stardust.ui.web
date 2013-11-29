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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/ChangeSynchronization",
				"bpm-modeler/js/m_modelElement"],
		function(m_utils, m_constants, m_command, m_commandsController, ChangeSynchronization, m_modelElement) {
			return {
				createProcessFromJson : function(model, json) {
					// TODO Ugly, use prototype
					m_utils.typeObject(json, new Process());

					json.initializeFromJson(model);

					return json;
				},
				createSynchronized : function(model, name, defaultPoolName, defaultLaneName) {
					var command = m_command
					.createCreateProcessCommand(
							model.id,
							model.id,
							{
								name : name,
								defaultPoolName : defaultPoolName,
								defaultLaneName : defaultLaneName
							});
					var synchronization = ChangeSynchronization.create(command);

					m_commandsController
							.submitCommand(command);

					return synchronization;
				},
				deleteProcess : function(id, model) {
					delete model.processes[id];
				}
			};

			/**
			 *
			 */
			function Process() {
				m_utils.inheritMethods(Process.prototype, m_modelElement
						.create());

				/**
				 *
				 */
				Process.prototype.toString = function() {
					return "Lightdust.Process";
				};

				/**
				 *
				 */
				Process.prototype.initializeFromJson = function(model) {
					this.type = m_constants.PROCESS_DEFINITION;
					this.model = model;

					this.model.processes[this.id] = this;
				};

				/**
				 *
				 */
				Process.prototype.getPath = function(withId) {
					var path = "/models/" + this.model.id + "/processes";

					if (withId) {
						path += "/" + this.id;
					}

					return path;
				};

				/**
				 *
				 */
				Process.prototype.rename = function(id, name) {
					delete this.model.processes[this.id];

					this.id = id;
					this.name = name;
					this.model.processes[this.id] = this;
				};

				/**
				 *
				 */
				Process.prototype.hasProcessAttachmentsDataPathes = function() {
					for ( var n in this.dataPathes) {
						if (this.dataPathes[n].id == "PROCESS_ATTACHMENTS") {
							return true;
						}
					}

					return false;
				};
			}
		});