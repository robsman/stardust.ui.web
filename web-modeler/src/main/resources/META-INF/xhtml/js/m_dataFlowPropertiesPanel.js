/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_propertiesPanel", "m_propertiesPage",
				"m_dataFlowBasicPropertiesPage" ],
		function(m_utils, m_constants, m_propertiesPanel, m_propertiesPage,
				m_dataFlowBasicPropertiesPage) {

			var dataFlowPropertiesPanel = null;

			return {
				initialize : function(models) {
					dataFlowPropertiesPanel = new DataFlowPropertiesPanel(
							models);

					dataFlowPropertiesPanel.initialize();
				},
				getInstance : function(element) {
					return dataFlowPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function DataFlowPropertiesPanel() {

				// Inheritance

				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("dataFlowPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(DataFlowPropertiesPanel.prototype,
						propertiesPanel);

				// Constants

				// Member initialization

				this.propertiesPages = [
						m_dataFlowBasicPropertiesPage
								.createPropertiesPage(this)];

				/**
				 * 
				 */
				DataFlowPropertiesPanel.prototype.toString = function() {
					return "[object Lightdust.DataFlowPropertiesPanel()]";
				};

				/**
				 * 
				 */
				DataFlowPropertiesPanel.prototype.setElement = function(
						element) {
					this.clearErrorMessages();

					this.element = element;

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};

				/**
				 * 
				 */
				DataFlowPropertiesPanel.prototype.apply = function() {
					this.applyPropertiesPages();
					this.element.refresh();
					this.element.submitUpdate();
				};
			}
		});