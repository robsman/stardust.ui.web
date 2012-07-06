/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_propertiesPanel", "m_propertiesPage",
				"m_controlFlowBasicPropertiesPage" ],
		function(m_utils, m_constants, m_propertiesPanel, m_propertiesPage,
				m_controlFlowBasicPropertiesPage) {

			var controlFlowPropertiesPanel = null;

			return {
				initialize : function(models) {
					controlFlowPropertiesPanel = new ControlFlowPropertiesPanel(
							models);

					controlFlowPropertiesPanel.initialize();
				},
				getInstance : function(element) {
					return controlFlowPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function ControlFlowPropertiesPanel() {

				// Inheritance

				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("controlFlowPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(ControlFlowPropertiesPanel.prototype,
						propertiesPanel);

				// Constants

				// Member initialization

				this.propertiesPages = [
						m_controlFlowBasicPropertiesPage
								.createPropertiesPage(this)];

				/**
				 * 
				 */
				ControlFlowPropertiesPanel.prototype.toString = function() {
					return "[object Lightdust.ControlFlowPropertiesPanel()]";
				};

				/**
				 * 
				 */
				ControlFlowPropertiesPanel.prototype.setElement = function(
						newElement) {
					this.clearErrorMessages();

					this.element = newElement;

					if (this.element.properties == null) {
						this.element.properties = {};
					}

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};

				/**
				 * 
				 */
				ControlFlowPropertiesPanel.prototype.apply = function() {
					this.applyPropertiesPages();
					this.element.refresh();
					this.element.submitUpdate();
				};
			}
		});