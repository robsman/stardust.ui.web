/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_command", "m_commandsController", "m_user", "m_dialog", "m_view" ],
		function(m_utils, m_constants, m_command, m_commandsController, m_user, m_dialog, m_view) {
			return {
				create : function(id) {
					var view = new ModelElementView();

					return view;
				}
			};

			/**
			 * 
			 */
			function ModelElementView() {
				// Inheritance

				var view = m_view.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(ModelElementView.prototype, view);

				/**
				 * 
				 */
				ModelElementView.prototype.initializeModelElementView = function() {
					this.modelElement = null;
					this.guidOutputRow = jQuery("#guidOutputRow");
					this.idOutputRow = jQuery("#idOutputRow");
					this.guidOutput = jQuery("#guidOutput");
					this.idOutput = jQuery("#idOutput");
					this.nameInput = jQuery("#nameInput");
					this.descriptionTextarea = jQuery("#descriptionTextarea");

					this.nameInput.change({
						"view" : this
					}, function(event) {
						var view = event.data.view;

						if (!view.validate()) {
							return;
						}

						if (view.modelElement.name != view.nameInput.val()) {
							view.submitChanges({
								name : view.nameInput.val(),
								id : m_utils.generateIDFromName(view.nameInput.val())
							});
						}
					});
					this.registerInputForModelElementChangeSubmission(this.descriptionTextarea, "description");
				};
				
				/**
				 * 
				 */
				ModelElementView.prototype.initializeModelElement = function(
						modelElement) {
					this.modelElement = modelElement;
					
					if (m_user.getCurrentRole() != m_constants.INTEGRATOR_ROLE) {
						m_dialog.makeInvisible(this.guidOutputRow);
						m_dialog.makeInvisible(this.idOutputRow);
					} else {
						m_dialog.makeVisible(this.guidOutputRow);
						m_dialog.makeVisible(this.idOutputRow);
						this.guidOutput.empty();
						this.guidOutput.append(this.modelElement.uuid);
						this.idOutput.empty();
						this.idOutput.append(this.modelElement.id);
					}

					this.nameInput.val(this.modelElement.name);
					this.descriptionTextarea.val(this.modelElement.description);

					if (this.modelElement.attributes == null) {
						this.modelElement.attributes = {};
					}
				};

				/**
				 * 
				 */
				ModelElementView.prototype.submitChanges = function(changes) {
					// Generic attributes
					// TODO Is this really needed?
					
					if (changes.attributes == null) {
						changes.attributes = {};
					}

					m_dialog.showWaitCursor();					
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementWithUUIDCommand(this.modelElement.model.id, this.modelElement.uuid, changes));
				};
				
				/**
				 * 
				 */
				ModelElementView.prototype.registerInputForModelElementChangeSubmission = function(
						input, property) {
					input
							.change(
									{
										"view" : this,
										"input" : input
									},
									function(event) {
										var view = event.data.view;
										var input = event.data.input;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement[property] != input
												.val()) {
											var modelElement = {};
											modelElement[property] = input
													.val();

											view.submitChanges(modelElement);
										}
									});
				};

				/**
				 * 
				 */
				ModelElementView.prototype.registerInputForModelElementAttributeChangeSubmission = function(
						input, attribute) {
					input
							.change(
									{
										"view" : this,
										"input" : input
									},
									function(event) {
										var view = event.data.view;
										var input = event.data.input;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes[attribute] != input
												.val()) {
											var modelElement = {
												attributes : {}
											};
											modelElement.attributes[attribute] = input
													.val();

											view.submitChanges(modelElement);
										}
									});
				};

				/**
				 * 
				 */
				ModelElementView.prototype.registerCheckboxInputForModelElementAttributeChangeSubmission = function(
						input, attribute) {
					input
							.click(
									{
										"view" : this,
										"input" : input
									},
									function(event) {
										var view = event.data.view;
										var input = event.data.input;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes[attribute] != input
												.val()) {
											var modelElement = {
												attributes : {}
											};
											modelElement.attributes[attribute] = input
													.is(":checked");

											view.submitChanges(modelElement);
										}
									});
				};
			}
		});