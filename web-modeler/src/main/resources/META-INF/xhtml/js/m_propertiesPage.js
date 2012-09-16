/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_command", "m_commandsController",
				"m_dialog" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_dialog) {

			return {
				createPropertiesPage : function(propertiesPanel, id, titel,
						imageUrl) {
					return new PropertiesPage(propertiesPanel, id, titel,
							imageUrl);
				}
			};

			function PropertiesPage(propertiesPanel, id, title, imageUrl) {
				this.propertiesPanel = propertiesPanel;
				this.id = id;
				this.title = title;
				this.page = jQuery("#" + this.propertiesPanel.id + " #"
						+ this.id);

				if (imageUrl == null) {
					this.imageUrl = "../../images/icons/generic-properties-page.png";
				} else {
					this.imageUrl = imageUrl;
				}
				/**
				 * 
				 */
				PropertiesPage.prototype.mapInputId = function(inputId) {
					return jQuery("#" + this.propertiesPanel.id + " #"
							+ this.id + " #" + inputId);
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.setElement = function() {
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.validate = function() {
					return true;
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.show = function() {
					m_dialog.makeVisible(this.page);
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.hide = function() {
					m_dialog.makeInvisible(this.page);
				};

				/**
				 * Returns the model element the Properties Pages are working
				 * on. This might be the Model Element a Symbol is representing
				 * (e.g. an Activity), a Data underneath a Data Symbol or the
				 * Process Definition itself.
				 */
				PropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.getModelElement();
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.assembleChangedObjectFromProperty = function(
						property, value) {
					var element = {
						modelElement : {}
					};

					element.modelElement[property] = value;

					return element;
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.assembleChangedObjectFromAttribute = function(
						attribute, value) {
					var element = {
						modelElement : {
							attributes : {}
						}
					};

					element.modelElement.attributes[attribute] = value;

					return element;
				};
				/**
				 * 
				 */
				PropertiesPage.prototype.registerInputForModelElementChangeSubmission = function(
						input, property) {
					input.change({
						"page" : this,
						"input" : input
					}, function(event) {
						var page = event.data.page;
						var input = event.data.input;

						if (!page.validate()) {
							return;
						}

						if (page.getModelElement()[property] != input.val()) {
							page.submitChanges(page
									.assembleChangedObjectFromProperty(
											property, input.val()));
						}
					});
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.registerInputForModelElementAttributeChangeSubmission = function(
						input, attribute) {
					input
							.change(
									{
										"page" : this,
										"input" : input
									},
									function(event) {
										var page = event.data.page;
										var input = event.data.input;

										if (!page.validate()) {
											return;
										}

										if (page.getModelElement().attributes[attribute] != input
												.val()) {
											page
													.submitChanges(page
															.assembleChangedObjectFromAttribute(
																	attribute,
																	input.val()));
										}
									});
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.registerCheckboxInputForModelElementChangeSubmission = function(
						input, property) {
					input.click({
						"page" : this,
						"input" : input
					}, function(event) {
						var page = event.data.page;
						var input = event.data.input;

						if (!page.validate()) {
							return;
						}

						if (page.getModelElement()[property] != input
								.is(":checked")) {
							page.submitChanges(page
									.assembleChangedObjectFromProperty(
											property, input.is(":checked")));
						}
					});
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.registerCheckboxInputForModelElementAttributeChangeSubmission = function(
						input, attribute) {
					input
							.click(
									{
										"page" : this,
										"input" : input
									},
									function(event) {
										var page = event.data.page;
										var input = event.data.input;

										if (!page.validate()) {
											return;
										}

										if (page.getModelElement().attributes[attribute] != input
												.is(":checked")) {
											page
													.submitChanges(page
															.assembleChangedObjectFromAttribute(
																	attribute,
																	input
																			.is(":checked")));
										}
									});
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.getModel = function() {
					return this.propertiesPanel.diagram.model;
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.submitChanges = function(changes) {
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.getModel().id, this.propertiesPanel
											.getElementUuid(), changes));
				};
			}
		});
