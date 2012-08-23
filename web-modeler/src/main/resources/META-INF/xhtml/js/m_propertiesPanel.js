/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_extensionManager", "m_dialog" ], // ,
		// "m_modelerViewLayoutManager" ],
		function(m_utils, m_constants, m_extensionManager, m_dialog) { // ,
			// m_modelerViewLayoutManager) {

			var currentPropertiesPanel = null;

			return {
				initializePropertiesPanel : function(element) {

					if (currentPropertiesPanel != null) {
						currentPropertiesPanel.hide();
					}

					currentPropertiesPanel = element.propertiesPanel;

					if (currentPropertiesPanel != null) {
						currentPropertiesPanel.setElement(element);
						currentPropertiesPanel.show();
					}
				},

				// TODO Homogenize calls

				initializeProcessPropertiesPanel : function(
						processPropertiesPanel) {
					if (currentPropertiesPanel != null) {
						currentPropertiesPanel.hide();
					}

					currentPropertiesPanel = processPropertiesPanel;

					currentPropertiesPanel.show();
				},

				createPropertiesPanel : function(id) {
					var propertiesPanel = new PropertiesPanel(id);

					return propertiesPanel;
				}
			};

			/**
			 * 
			 */
			function PropertiesPanel(id) {
				this.id = id;
				this.panel = jQuery("#" + this.id);
				this.propertiesPageList = jQuery("#propertiesPageList");
				this.applyButton = jQuery("#" + this.id + " #applyButton");
				this.resetButton = jQuery("#" + this.id + " #resetButton");
				this.errorMessagesRow = jQuery("#" + this.id
						+ " #errorMessagesRow");
				this.errorMessagesList = jQuery("#" + this.id
						+ " #errorMessagesList");
				this.propertiesPages = [];
				this.errorMessages = [];
				this.helpPanel = jQuery("#" + this.id + " #helpPanel");

				/**
				 * 
				 */
				PropertiesPanel.prototype.initialize = function(element) {
					this.initializePropertiesPages();
					this.hide();
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.mapInputId = function(inputId) {
					return jQuery("#" + this.id + " #" + inputId);
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.getDiagram = function() {
					return this.element.diagram;
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.getElementUuid = function() {
					return this.element.oid;
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.initializePropertiesPages = function() {
					var propertiesPages = m_extensionManager.findExtensions(
							"propertiesPage", "panelId", this.id);

					for ( var n = 0; n < propertiesPages.length; n++) {
						m_utils.debug("Load Properties Page "
								+ propertiesPages[n].pageId);

						if (propertiesPages[n].pageHtmlUrl != null) {
							jQuery("#" + this.id + "Table")
									.append(
											"<tr><td><div id=\""
													+ propertiesPages[n].pageId
													+ "\" class=\"propertiesPage\"></div></td></tr>");

							var panel = this;
							var extension = propertiesPages[n];

							jQuery(
									"#" + this.id + " #"
											+ propertiesPages[n].pageId).load(
									extension.pageHtmlUrl,
									function(response, status, xhr) {
										if (status == "error") {
											var msg = "Properties Page Load Error: " + xhr.status
													+ " " + xhr.statusText;

											jQuery(
													"#" + panel.id + " #"
															+ extension.pageId)
													.append(msg);
											m_utils.debug(msg);
										} else {
											m_utils.debug("Page loaded: " + extension.pageId);
											panel.propertiesPages
													.push(extension.provider
															.create(panel));
										}
									});
						} else {
							// Embedded Markup

							this.propertiesPages
									.push(propertiesPages[n].provider
											.create(this));
						}
					}
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.showPropertiesPageList = function() {
					if (this.propertiesPages.length == 1) {
						m_dialog.makeInvisible(this.propertiesPageList);

						return;
					}

					m_dialog.makeVisible(this.propertiesPageList);

					this.propertiesPageList.empty();

					for ( var n in this.propertiesPages) {
						this.propertiesPageList.append("<tr>" + "<td>"
								+ "<input id=\"" + this.propertiesPages[n].id
								+ "ListItem\" type=\"image\" src=\""
								+ this.propertiesPages[n].imageUrl
								+ "\" title=\"" + this.propertiesPages[n].title
								+ "\" alt=\"" + this.propertiesPages[n].title
								+ "\" class=\"toolbarButton\" />" + "</td>"
								+ "</tr>");

						jQuery(
								"#propertiesPageList #"
										+ this.propertiesPages[n].id
										+ "ListItem").click({
							"callbackScope" : this,
							"propertiesPage" : this.propertiesPages[n]
						}, function(event) {
							event.data.callbackScope.hidePropertiesPages();
							event.data.propertiesPage.show();
						});
					}
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.hidePropertiesPages = function() {
					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].hide();
					}
				};

				PropertiesPanel.prototype.clearErrorMessages = function() {
					m_dialog.makeInvisible(this.errorMessagesRow);
					this.errorMessages = [];
					this.errorMessagesList.empty();
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.showErrorMessages = function() {
					if (this.errorMessages.length != 0) {
						m_dialog.makeVisible(this.errorMessagesRow);

						for ( var n in this.errorMessages) {
							this.errorMessagesList.append("<li>"
									+ this.errorMessages[n] + "</li>");
						}
					}
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.show = function() {
					m_dialog.makeVisible(this.panel);
					this.showPropertiesPageList();
					this.hidePropertiesPages();

					if (this.propertiesPages.length >= 1) {
						this.propertiesPages[0].show();
					}

					this.clearErrorMessages();

					require("m_modelerViewLayoutManager").adjustPanels();
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.hide = function() {
					m_dialog.makeInvisible(this.panel);
					this.hidePropertiesPages();
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.reset = function() {
					this.resetPropertiesPages();
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.showHelpPanel = function() {
					m_dialog.makeVisible(this.helpPanel);
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.hideHelpPanel = function() {
					m_dialog.makeInvisible(this.helpPanel);
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.processCommand = function(command) {
					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length) {
						if (object.changes.modified[0].oid == this.element.oid) {

							m_utils.inheritFields(this.element,
									object.changes.modified[0]);

							this.setElement(this.element);
						}
						else if (this.element.modelElement != null &&
								object.changes.modified[0].oid == this.element.modelElement.oid) {

							m_utils.inheritFields(this.element.modelElement,
									object.changes.modified[0]);

							this.setElement(this.element);
						}
					}
				};
				
				/**
				 * 
				 */
				PropertiesPanel.prototype.submitChanges = function(changes) {
					m_utils.debug("Changes to be submitted for UUID " + this.getElementUuid() + ":");
					m_utils.debug(changes);
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.getDiagram().modelId,
									this.getElementUuid(),
									changes));
				};
			}
		});