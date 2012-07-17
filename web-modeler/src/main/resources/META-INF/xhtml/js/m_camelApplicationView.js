/**
 * @author Marc.Gille
 */
define([ "m_utils", "m_command", "m_commandsController", "m_dialog", "m_model",
		"m_typeDeclaration" ], function(m_utils, m_command,
		m_commandsController, m_dialog, m_model, m_typeDeclaration) {
	var view;
	var typeDeclarations = m_typeDeclaration.getTestTypeDeclarations();

	return {
		initialize : function() {
			var modelId = jQuery.url.setUrl(window.location.search).param(
					"modelId");
			var applicationId = jQuery.url.setUrl(window.location.search)
					.param("applicationId");
			var model = m_model.findModel(modelId);
			var application = model.applications[applicationId];

			view = new CamelApplicationView(model, application);
			// TODO Unregister!
			// In Initializer?

			m_commandsController.registerCommandHandler(view);

			view.initialize(application);
		}
	};

	/**
	 * 
	 */
	function CamelApplicationView() {
		this.nameInput = jQuery("#nameInput");
		this.requestDataInput = jQuery("#requestDataInput");
		this.responseDataInput = jQuery("#responseDataInput");

		this.nameInput.change({
			"view" : this
		}, function(event) {
			var view = event.data.view;

			if (view.application.name != view.nameInput.val()) {
				view.submitChanges({
					name : view.nameInput.val()
				});
			}
		});

		/**
		 * 
		 */
		CamelApplicationView.prototype.initialize = function(application) {
			this.application = application;
			
			this.nameInput.val(this.application.name);
		};

		/**
		 * 
		 */
		CamelApplicationView.prototype.toString = function() {
			return "Lightdust.CamelApplicationView";
		};
		
		/**
		 * 
		 */
		CamelApplicationView.prototype.submitChanges = function(
				changes) {
			m_commandsController.submitCommand(m_command
					.createUpdateModelElementCommand(this.application.model.id,
							this.application.oid, changes));
		};

		/**
		 * 
		 */
		CamelApplicationView.prototype.processCommand = function(
				command) {
			m_utils.debug("===> Camel Process Command");
			m_utils.debug(command);

			// Parse the response JSON from command pattern

			var obj = ("string" == typeof (command)) ? jQuery
					.parseJSON(command) : command;

			if (null != obj && null != obj.changes
					&& object.changes[this.application.oid] != null) {
				this.nameInput
						.val(object.changes[this.application.oid].name);

				// Other attributes
			}
		};		
	}
});