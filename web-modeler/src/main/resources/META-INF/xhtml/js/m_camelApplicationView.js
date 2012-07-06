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
		}
	};

	/**
	 * 
	 */
	function CamelApplicationView(model, application) {
		this.model = model;
		this.application = application;
		this.requestDataInput = jQuery("#requestDataInput");
		this.responseDataInput = jQuery("#responseDataInput");

		/**
		 * 
		 */
		CamelApplicationView.prototype.toString = function() {
			return "Lightdust.CamelApplicationView";
		};
	}
	;
});