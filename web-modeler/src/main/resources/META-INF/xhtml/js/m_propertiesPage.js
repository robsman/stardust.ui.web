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

			function PropertiesPage(newPropertiesPanel, id, title, imageUrl) {
				this.propertiesPanel = newPropertiesPanel;
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
				PropertiesPage.prototype.apply = function() {
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
				PropertiesPage.prototype.reset = function() {
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
				 * 
				 */
				PropertiesPage.prototype.initializeDocumentationHandling = function() {
					this.documentationCreationLink = this
							.mapInputId("documentationCreationLink");
					this.documentationCreationLinkPanel = this
							.mapInputId("documentationCreationLinkPanel");
					this.openDocumentViewLink = this
							.mapInputId("openDocumentViewLink");
					this.openDocumentViewLinkPanel = this
							.mapInputId("openDocumentViewLinkPanel");
					this.documentUrl = null;

					if (this.documentationCreationLink != null) {
						this.documentationCreationLink.click({
							"callbackScope" : this
						}, function(event) {
							var url = event.data.callbackScope
									.getDocumentationCreationUrl();

							m_commandsController.submitImmediately(m_command
									.createCommand(url,
											event.data.callbackScope
													.getModelElement()), {
								"callbackScope" : event.data.callbackScope,
								"method" : "setDocumentUrl"
							});
						});
					}

					if (this.openDocumentViewLink != null) {
						this.openDocumentViewLink
								.click(
										{
											"callbackScope" : this
										},
										function(event) {
											var link = jQuery(
													"a[id $= 'modeling_work_assignment_view_link']",
													window.parent.frames['ippPortalMain'].document);
											var linkId = link.attr('id');
											var form = link
													.parents('form:first');
											var formId = form.attr('id');

											window.parent.EventHub.events
													.publish(
															"OPEN_VIEW",
															linkId,
															formId,
															"documentView",
															"documentOID="
																	+ event.data.callbackScope.documentUrl,
															"documentOID="
																	+ event.data.callbackScope.documentUrl);
										});
					}
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.getDocumentationCreationUrl = function() {
					var url = "/models/"
							+ this.propertiesPanel.element.diagram.modelId
							+ "/processes/"
							+ this.propertiesPanel.element.diagram.processId
							+ "/createDocumentation";

					return url;
				};

				/**
				 * Returns the model element the Properties Pages are working on. This might be the Model Element a Symbol is 
				 * representing (e.g. an Activity), a Data underneath a Data Symbol or the Process Definition itself.
				 */
				PropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element.modelElement;
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
				PropertiesPage.prototype.loadDocumentUrl = function() {
					this.documentUrl = this.getModelElement().attributes["carnot:engine:documentUrl"];

					if (this.documentUrl == null) {
						m_dialog
								.makeVisible(this.documentationCreationLinkPanel);
						m_dialog.makeInvisible(this.openDocumentViewLinkPanel);
					} else {
						m_dialog
								.makeInvisible(this.documentationCreationLinkPanel);
						m_dialog.makeVisible(this.openDocumentViewLinkPanel);
					}
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.saveDocumentUrl = function() {
					this.getModelElement().attributes["carnot:engine:documentUrl"] = this.documentUrl;
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.setDocumentUrl = function(json) {
					this.documentUrl = json.documentUrl;

					m_dialog.makeInvisible(this.documentationCreationLinkPanel);
					m_dialog.makeVisible(this.openDocumentViewLinkPanel);
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

						m_utils.debug("===> Element");
						m_utils.debug(page.getModelElement());

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
											page.submitChanges(page
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
