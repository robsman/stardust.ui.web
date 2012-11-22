/**
 * @author Marc.Gille
 */

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_propertiesPage" ],
		function(m_utils, m_constants, m_propertiesPage) {
			return {
				create : function(propertiesPanel) {
					return new ActivityQualityControlPropertiesPage(
							propertiesPanel);
				}
			};

			function ActivityQualityControlPropertiesPage(newPropertiesPanel,
					newId, newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "qualityControlPropertiesPage",
						"Quality Control");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ActivityQualityControlPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				this.qualityControlProbabilityInput = this.mapInputId("qualityControlProbabilityInput");

				/**
				 * 
				 */
				ActivityQualityControlPropertiesPage.prototype.setElement = function() {
					if (this.propertiesPanel.element.properties.qualityControl == null) {
						this.propertiesPanel.element.properties.qualityControl = {};
					}

					this.qualityControlProbabilityInput
							.val(this.propertiesPanel.element.properties.qualityControl.qualityControlProbability);
				};

				/**
				 * 
				 */
				ActivityQualityControlPropertiesPage.prototype.apply = function() {
					this.propertiesPanel.properties.qualityControl.qualityControlProbability = this.qualityControlProbabilityInput
							.val();
				};
			}
		});