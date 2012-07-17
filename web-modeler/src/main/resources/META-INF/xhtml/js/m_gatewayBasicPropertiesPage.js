/**
 * @author Marc.Gille
 */

define(
		[ "m_utils", "m_constants", "m_command", "m_commandsController",
				"m_propertiesPage" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_propertiesPage) {
			return {
				createPropertiesPage : function(propertiesPanel) {
					return new GatewayBasicPropertiesPage(propertiesPanel);
				}
			};

			function GatewayBasicPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "basicPropertiesPage", "Basic");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(GatewayBasicPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				this.descriptionInput = this.mapInputId("descriptionInput");
				this.gatewayTypeInput = this.mapInputId("gatewayTypeInput");

				this.descriptionInput
						.change(
								{
									"page" : this
								},
								function(event) {
									var page = event.data.page;

									if (!page.validate()) {
										return;
									}

									if (page.propertiesPanel.element.modelElement.description != page.descriptionInput
											.val()) {
										page
												.submitChanges({
													modelElement : {
														description : page.descriptionInput
																.val()
													}
												});
									}
								});
				this.gatewayTypeInput
						.change(
								{
									"page" : this
								},
								function(event) {
									var page = event.data.page;

									if (!page.validate()) {
										return;
									}

									if (page.propertiesPanel.element.modelElement.gatewayType != page.gatewayTypeInput
											.val()) {
										page
												.submitChanges({
													modelElement : {
														gatewayType : page.gatewayTypeInput
																.val()
													}
												});
									}
								});

				/**
				 * 
				 */
				GatewayBasicPropertiesPage.prototype.setElement = function() {
					this.descriptionInput
							.val(this.propertiesPanel.element.modelElement.description);
					this.gatewayTypeInput
							.val(this.propertiesPanel.element.modelElement.attributes.gatewayType);
				};

				/**
				 * 
				 */
				GatewayBasicPropertiesPage.prototype.validate = function() {
					return true;
				};

				/**
				 * 
				 */
				GatewayBasicPropertiesPage.prototype.submitChanges = function(
						changes) {
					m_commandsController
							.submitCommand(m_command
									.createUpdateModelElementCommand(
											this.propertiesPanel.element.diagram.modelId,
											this.propertiesPanel.element.oid,
											changes));
				};
			}
		});