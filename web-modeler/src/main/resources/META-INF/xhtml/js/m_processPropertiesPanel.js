/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_model", "m_propertiesPanel", "m_propertiesPage",
				"m_processBasicPropertiesPage", "m_processDataPathPropertiesPage", "m_processDisplayPropertiesPage", "m_processProcessInterfacePropertiesPage" ],
		function(m_utils, m_constants, m_model, m_propertiesPanel, m_propertiesPage,
				m_processBasicPropertiesPage, m_processDataPathPropertiesPage, m_processDisplayPropertiesPage, m_processProcessInterfacePropertiesPage) {

			var processPropertiesPanel = null;

			return {
				initialize : function(models, diagram) {
					processPropertiesPanel = new ProcessPropertiesPanel(
							models, diagram);
					
					processPropertiesPanel.initialize();
				},
				getInstance : function(element) {
					return processPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function ProcessPropertiesPanel(models, diagram) {
				// Inheritance

				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("processPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(ProcessPropertiesPanel.prototype,
						propertiesPanel);

				// Constants

				// Member initialization

				this.models = models;
				this.diagram = diagram;
				this.propertiesPages = [
						m_processBasicPropertiesPage
								.createPropertiesPage(this), 
								m_processDataPathPropertiesPage
								.createPropertiesPage(this),
								m_processDisplayPropertiesPage
								.createPropertiesPage(this),
								m_processProcessInterfacePropertiesPage
								.createPropertiesPage(this)];

				/**
				 * 
				 */
				ProcessPropertiesPanel.prototype.toString = function() {
					return "Lightdust.ProcessPropertiesPanel";
				};

				/**
				 * 
				 */
				ProcessPropertiesPanel.prototype.setElement = function(
						element) {
					this.clearErrorMessages();

					this.element = element;

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};
			}
		});