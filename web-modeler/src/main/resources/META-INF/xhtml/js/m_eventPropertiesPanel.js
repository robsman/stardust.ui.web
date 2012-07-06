/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_model", "m_propertiesPanel", "m_propertiesPage",
				"m_eventBasicPropertiesPage" ],
		function(m_utils, m_constants, m_model, m_propertiesPanel, m_propertiesPage,
				m_eventBasicPropertiesPage) {

			var eventPropertiesPanel = null;

			return {
				initialize : function(models) {
					eventPropertiesPanel = new EventPropertiesPanel(
							models);
					
					eventPropertiesPanel.initialize();
				},
				getInstance : function(element) {
					return eventPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function EventPropertiesPanel(models) {

				// Inheritance

				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("eventPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(EventPropertiesPanel.prototype,
						propertiesPanel);

				// Constants

				// Member initialization

				this.models = models;
				this.propertiesPages = [
						m_eventBasicPropertiesPage
								.createPropertiesPage(this)];

				/**
				 * 
				 */
				EventPropertiesPanel.prototype.toString = function() {
					return "Lightdust.EventPropertiesPanel";
				};

				/**
				 * 
				 */
				EventPropertiesPanel.prototype.setElement = function(
						element) {
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

				/**
				 * 
				 */
				EventPropertiesPanel.prototype.apply = function() {
					this.applyPropertiesPages();
					this.element.refresh();
					this.element.submitUpdate();					
				};
			}
		});