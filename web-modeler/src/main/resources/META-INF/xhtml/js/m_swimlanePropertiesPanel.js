/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_propertiesPanel", "m_propertiesPage",
				"m_swimlaneBasicPropertiesPage" ],
		function(m_utils, m_constants, m_propertiesPanel, m_propertiesPage,
				m_swimlaneBasicPropertiesPage) {

			var swimlanePropertiesPanel = null;

			return {
				initialize : function(models) {
					swimlanePropertiesPanel = new SwimlanePropertiesPanel(
							models);
					
					swimlanePropertiesPanel.initialize();
				},				
				getInstance : function(element) {
					return swimlanePropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function SwimlanePropertiesPanel(models) {

				// Inheritance

				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("swimlanePropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(SwimlanePropertiesPanel.prototype,
						propertiesPanel);

				// Constants

				// Member initialization

				this.models = models;
				this.propertiesPages = [
						m_swimlaneBasicPropertiesPage
								.createPropertiesPage(this)];

				/**
				 * 
				 */
				SwimlanePropertiesPanel.prototype.toString = function() {
					return "Lightdust.SwimlanePropertiesPanel";
				};

				/**
				 * 
				 */
				SwimlanePropertiesPanel.prototype.setElement = function(
						element) {
					this.clearErrorMessages();

					this.element = element;

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
				SwimlanePropertiesPanel.prototype.apply = function() {
					this.applyPropertiesPages();
					this.element.refresh();
					this.element.submitUpdate();
				};
			}
		});