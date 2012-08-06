/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_command", "m_commandsController", "m_dialog" ],
		function(m_utils, m_constants, m_command, m_commandsController, m_dialog) {

			return {
				createPropertiesPage : function(propertiesPanel, id,
						titel, imageUrl) {
					return new PropertiesPage(propertiesPanel, id,
							titel, imageUrl);
				}
			};

			function PropertiesPage(newPropertiesPanel, id, title, imageUrl) {
				this.propertiesPanel = newPropertiesPanel;
				this.id = id;
				this.title = title;
				this.page = jQuery("#" + this.propertiesPanel.id + " #"
						+ this.id);

				if (imageUrl == null)
					{
					this.imageUrl = "../../images/icons/generic-properties-page.png";
					}
				else
				{
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
						this.documentationCreationLink
								.click(
										{
											"callbackScope" : this
										},
										function(event) {
											var url = event.data.callbackScope.getDocumentationCreationUrl();

											m_commandsController
													.submitImmediately(
															m_command
																	.createCommand(url,
																			event.data.callbackScope.getModelElement()),
															{
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
					var url =
					"/models/"
					+ this.propertiesPanel.element.diagram.modelId
					+ "/processes/"
					+ this.propertiesPanel.element.diagram.processId
					+ "/createDocumentation";
					
					return url;
				};
				
				/**
				 * 
				 */
				PropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element.modelElement;
				};

				/**
				 * 
				 */
				PropertiesPage.prototype.loadDocumentUrl = function() {
					this.documentUrl = this.getModelElement().attributes["carnot:engine:documentUrl"];

					if (this.documentUrl == null) {
						m_dialog.makeVisible(this.documentationCreationLinkPanel);
						m_dialog.makeInvisible(this.openDocumentViewLinkPanel);
					} else {
						m_dialog.makeInvisible(this.documentationCreationLinkPanel);
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
				PropertiesPage.prototype.submitChanges = function(
						changes) {
					m_utils.debug("Changes to be submitted: ");
					m_utils.debug(changes);
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(this.propertiesPanel.element.diagram.modelId, 
									this.propertiesPanel.element.oid, changes));
				};

			}
		});
