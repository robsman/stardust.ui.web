/**
 * @author Marc.Gille
 */

define(
		[ "m_utils", "m_constants", "m_propertiesPage" ],
		function(m_utils, m_constants, m_propertiesPage) {
			return {
				create: function(propertiesPanel) {
					return new ActivityCostPropertiesPage(propertiesPanel);
				}
			};

			function ActivityCostPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "costPropertiesPage", "Cost");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ActivityCostPropertiesPage.prototype,
						propertiesPage);

				// Field initialization
				
				this.targetCostPerExecutionInput = jQuery("#"
						+ this.propertiesPanel.id + " #" + this.id
						+ " #targetCostPerExecutionInput");

				/**
				 * 
				 */
				ActivityCostPropertiesPage.prototype.setElement = function() {
					if (this.propertiesPanel.element.properties.cost == null)
					{
					this.propertiesPanel.element.properties.cost = {};
					}

					this.targetCostPerExecutionInput
							.val(this.propertiesPanel.element.properties.cost.targetCostPerExecution);
				};

				/**
				 * 
				 */
				ActivityCostPropertiesPage.prototype.apply = function() {
					this.propertiesPanel.element.properties.cost.targetCostPerExecution = this.targetCostPerExecutionInput
							.val();
				};
			}
		});