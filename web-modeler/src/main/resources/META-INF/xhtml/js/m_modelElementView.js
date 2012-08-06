/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_command", "m_commandsController", "m_dialog", "m_view" ],
		function(m_utils, m_constants, m_command, m_commandsController, m_dialog, m_view) {
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
					this.descriptionTextarea.change({
						"view" : this
					}, function(event) {
						var view = event.data.view;

						if (!view.validate()) {
							return;
						}

						if (view.modelElement.description != view.descriptionTextarea.val()) {
							view.submitChanges({
								description : view.descriptionTextarea.val()
							});
						}
					});					
				};
				
				/**
				 * 
				 */
				ModelElementView.prototype.initializeModelElement = function(
						modelElement) {
					this.modelElement = modelElement;
					this.guidOutput.empty();
					this.guidOutput.append(this.modelElement.uuid);
					this.idOutput.empty();
					this.idOutput.append(this.modelElement.id);
					this.nameInput.val(this.modelElement.name);
					this.descriptionTextarea.val(this.modelElement.description);

					if (this.modelElement.attributes == null) {
						this.modelElement.attributes = {};
					}
				}

				/**
				 * 
				 */
				ModelElementView.prototype.submitChanges = function(changes) {
					// Generic attributes

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					m_commandsController.submitCommand(m_command
							.createUpdateModelElementWithUUIDCommand(this.modelElement.model.id, this.modelElement.uuid, changes));
				};
			}
		});