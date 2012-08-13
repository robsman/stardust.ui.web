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
		[ "m_utils", "m_command", "m_commandsController", "m_dialog",
				"m_modelElementView", "m_model", "m_typeDeclaration" ],
		function(m_utils, m_command, m_commandsController, m_dialog,
				m_modelElementView, m_model, m_typeDeclaration) {
			var view;

			return {
				initialize : function(fullId) {
					var organization = m_model.findParticipant(fullId);

					view = new OrganizationView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(organization);
				}
			};

			/**
			 * 
			 */
			function OrganizationView() {
				// Inheritance

				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(OrganizationView.prototype, view);

				jQuery("#organizationTabs").tabs();

				/**
				 * 
				 */
				OrganizationView.prototype.initialize = function(organization) {
					this.initializeModelElementView();
					this.initializeModelElement(organization);

					this.organization = organization;

					m_utils.debug("===> Organization");
					m_utils.debug(organization);

					this.publicVisibilityCheckbox = jQuery("#publicVisibilityCheckbox");
					this.chooseAssignmentRadio = jQuery("#chooseAssignmentRadio");
					this.assignAutomaticallyRadio = jQuery("#assignAutomaticallyRadio");
					this.supportsDepartmentsCheckbox = jQuery("#supportsDepartmentsCheckbox");
					this.departmentDataSelect = jQuery("#departmentDataSelect");
					this.departmentDataPathInput = jQuery("#departmentDataPathInput");
					this.costCenterInput = jQuery("#costCenterInput");					

                    this.registerTextInputForModelElementAttributeChangeSubmission(
                    		this.departmentDataSelect, "carnot:engine:dataId");
                    this.registerTextInputForModelElementAttributeChangeSubmission(
                    		this.departmentDataPathInput, "carnot:engine:dataPath");
                    this.registerCheckboxInputForModelElementAttributeChangeSubmission(
                    		this.supportsDepartmentsCheckbox, "carnot:engine:bound");
                    
					this.publicVisibilityCheckbox
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
														}
													});
										}
									});
					this.chooseAssignmentRadio
							.click(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.chooseAssignmentRadio
												.is(":checked")) {
											view.assignAutomaticallyRadio.attr(
													"checked", false);

											if (view.modelElement.attributes["carnot:engine:tasks:assignment:mode"] != "assemblyLine") {
												view
														.submitChanges({
															attributes : {
																"carnot:engine:tasks:assignment:mode" : "assemblyLine"
															}
														});
											}
										}
									});
					this.assignAutomaticallyRadio
							.click(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.assignAutomaticallyRadio
												.is(":checked")) {
											view.chooseAssignmentRadio.attr(
													"checked", false);

											if (view.modelElement.attributes["carnot:engine:tasks:assignment:mode"] != "assemblyLine") {
												view
														.submitChanges({
															attributes : {
																"carnot:engine:tasks:assignment:mode" : "assemblyLine"
															}
														});
											}
										}
									});
					this.publicVisibilityCheckbox
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes["carnot:pwh:costCenter"] != view.publicVisibilityCheckbox
												.val()) {
											view
													.submitChanges({
														attributes : {
															"carnot:pwh:costCenter" : view.publicVisibilityCheckbox
																	.val()
														}
													});
										}
									});

					if (organization.attributes["carnot:engine:visibility"]
							.equals("Public")) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}

					if (organization.attributes["carnot:engine:tasks:assignment:mode"]
							.equals("assemblyLine")) {
						this.assignAutomaticallyRadio.attr("checked", true);
						this.chooseAssignmentRadio.attr("checked", false);
					} else {
						this.assignAutomaticallyRadio.attr("checked", false);
						this.chooseAssignmentRadio.attr("checked", true);
					}

                    this.departmentDataSelect.val(organization.attributes["carnot:engine:dataId"]);
                    this.departmentDataPathInput.val(organization.attributes["carnot:engine:dataPath"]);
                    this.supportsDepartmentsCheckbox.attr("checked", organization.attributes["carnot:engine:bound"]);

					this.costCenterInput
							.val(this.organization.attributes["carnot:pwh:costCenter"]);
				};

				/**
				 * 
				 */
				OrganizationView.prototype.toString = function() {
					return "Lightdust.OrganizationView";
				};

				/**
				 * 
				 */
				OrganizationView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Application name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 * 
				 */
				OrganizationView.prototype.processCommand = function(command) {
					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object
							&& null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.organization.oid) {

						m_utils.inheritFields(this.organization,
								object.changes.modified[0]);

						this.initialize(this.organization);
					}
				};
			}
		});