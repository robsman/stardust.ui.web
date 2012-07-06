/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_dialog", "m_modelerViewLayoutManager" ],
		function(m_utils, m_constants, m_dialog, m_modelerViewLayoutManager) {

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
				
				initializeProcessPropertiesPanel : function(processPropertiesPanel) {
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
			function PropertiesPanel(newId) {
				this.id = newId;
				this.panel = jQuery("#" + this.id);
				this.propertiesPageList = jQuery("#" + this.id
						+ " #propertiesPageList");
				this.applyButton = jQuery("#" + this.id + " #applyButton");
				this.resetButton = jQuery("#" + this.id + " #resetButton");
				this.errorMessagesList = jQuery("#" + this.id
						+ " #errorMessagesList");
				this.propertiesPages = [];
				this.errorMessages = [];
				this.helpPanel = jQuery("#" + this.id + " #helpPanel");

				/**
				 * 
				 */
				PropertiesPanel.prototype.initialize = function(element) {
					this.applyButton.click({
						"callbackScope" : this
					}, function(event) {
						event.data.callbackScope.apply();
					});
					this.resetButton.click({
						"callbackScope" : this
					}, function(event) {
						event.data.callbackScope.reset();
					});

					this.createPropertiesPageList();
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
				PropertiesPanel.prototype.createPropertiesPageList = function() {
					if (this.propertiesPages.length == 1) {
						m_dialog.makeInvisible(this.propertiesPageList);

						return;
					}

					this.propertiesPageList.empty();

					// var list = this.propertiesPageList.append("<ul/>");

					for ( var n in this.propertiesPages) {
						this.propertiesPageList
								.append("<div id=\""
										+ this.propertiesPages[n].id
										+ "ListItem\" class=\"propertiesPageListItem\">"
										+ this.propertiesPages[n].title
										+ "</div");

						jQuery(
								"#" + this.id + " #"
										+ this.propertiesPages[n].id
										+ "ListItem").click(
								{
									"callbackScope" : this,
									"propertiesPage" : this.propertiesPages[n]
								},
								function(event) {
									 event.data.callbackScope
									 .hidePropertiesPages();
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
					this.errorMessages = [];
					this.errorMessagesList.empty();
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.applyPropertiesPages = function() {
					this.clearErrorMessages();

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].validate();
					}

					if (this.errorMessages.length == 0) {
						for ( var n in this.propertiesPages) {
							this.propertiesPages[n].apply();
						}
					} else {
						this.errorMessagesList.empty();

						for ( var n in this.errorMessages) {
							this.errorMessagesList.append("<li>"
									+ this.errorMessages[n] + "</li>");
						}
					}
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.resetPropertiesPages = function() {
					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].reset();
					}
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.show = function() {
					m_dialog.makeVisible(this.panel);
					this.hidePropertiesPages();

					if (this.propertiesPages.length >= 1) {
						this.propertiesPages[0].show();
					}
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
			}
			;
		});