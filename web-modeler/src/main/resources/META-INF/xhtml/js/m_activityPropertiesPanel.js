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
		[ "m_utils", "m_constants", "m_command",
				"m_commandsController", "m_model", "m_propertiesPanel",
				"m_propertiesPage" ],
		function(m_utils, m_constants, m_command,
				m_commandsController, m_model, m_propertiesPanel,
				m_propertiesPage) {

			var activityPropertiesPanel = null;

			return {
				initialize : function(diagram) {
					activityPropertiesPanel = new ActivityPropertiesPanel();

					m_commandsController.registerCommandHandler(activityPropertiesPanel);

					activityPropertiesPanel.initialize(diagram);
				},
				getInstance : function() {
					return activityPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function ActivityPropertiesPanel(models) {
				// Inheritance

				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("activityPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(ActivityPropertiesPanel.prototype,
						propertiesPanel);

				// Member initialization

				// TODO needed?
				
				this.models = models;

				this.helpPanel = this.mapInputId("helpPanel");

				// TODO Push to base class
				this.getModelingHelpLink = this
						.mapInputId("getModelingHelpLink");

				this.getModelingHelpLink.click({
					"callbackScope" : this
				}, function(event) {
					var link = jQuery(
							"a[id $= 'modeling_work_assignment_view_link']",
							window.parent.frames['ippPortalMain'].document);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');

					window.parent.EventHub.events.publish("OPEN_VIEW", linkId,
							formId, "modelingWorkAssignmentView");
				});

				/**
				 * 
				 */
				ActivityPropertiesPanel.prototype.toString = function() {
					return "Lightdust.ActivityPropertiesPanel";
				};

				/**
				 * 
				 */
				ActivityPropertiesPanel.prototype.setElement = function(element) {
					this.clearErrorMessages();

					this.element = element;

					if (this.element.modelElement.participantFullId != null) {
						this.participant = m_model
								.findParticipant(this.element.modelElement.participantFullId);
					}

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};
			}
		});