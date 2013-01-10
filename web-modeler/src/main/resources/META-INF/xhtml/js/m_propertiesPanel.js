/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_session", "bpm-modeler/js/m_user",
				"bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog" ],
		function(m_utils, m_constants, m_extensionManager, m_session, m_user,
				m_command, m_commandsController, m_dialog) {

			var currentPropertiesPanel = null;

			return {
				initializePropertiesPanel : function(element, page) {

					if (currentPropertiesPanel != null) {
						currentPropertiesPanel.hide();
					}

					currentPropertiesPanel = element.propertiesPanel;

					if (currentPropertiesPanel != null) {
						currentPropertiesPanel.setElement(element);
						currentPropertiesPanel.show(page);
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
				this.lastSelectedPageIndex = 0;

				/**
				 * 
				 */
				PropertiesPanel.prototype.initialize = function(diagram) {
					this.diagram = diagram;

					this.initializePropertiesPages();
					this.hide();
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.getModel = function() {
					return this.diagram.model;
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.getModelElement = function() {
					return this.element.modelElement;
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.wrapModelElementProperties = function(
						modelElementProperties) {
					return {
						modelElement : modelElementProperties
					};
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
				PropertiesPanel.prototype.getElementUuid = function() {
					return this.element.oid;
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.initializePropertiesPages = function() {
					this.propertiesPages = [];

					var propertiesPages = m_extensionManager.findExtensions(
							"propertiesPage", "panelId", this.id);
					var extensions = {};

					for ( var n = 0; n < propertiesPages.length; n++) {
						var extension = propertiesPages[n];

						extensions[extension.id] = extension;

						if (!m_session.initialize().technologyPreview
								&& extension.visibility == "preview") {

							if (extension.pageHtmlUrl == null) {
								m_dialog.makeInvisible(jQuery("#" + this.id
										+ " #" + extension.id));
							}

							continue;
						}

						m_utils.debug("Load Properties Page "
								+ extension.id);

						if (extension.pageHtmlUrl != null) {
							var pageDiv = jQuery("<div id=\""
									+ extension.id
									+ "\" class=\"propertiesPage\"></div>");

							jQuery("#" + this.id + " #propertiesPagesCell")
									.append(pageDiv);

							// TODO this variable may be overwritten in the
							// loop, find mechanism to pass data to load
							// callback

							var panel = this;

							pageDiv
									.load(
											extension.pageHtmlUrl,
											function(response, status, xhr) {
												if (status == "error") {
													var msg = "Properties Page Load Error: "
															+ xhr.status
															+ " "
															+ xhr.statusText;

													jQuery(this).append(msg);
													m_utils.debug(msg);
												} else {
													m_utils
															.debug("Page loaded: "
																	+ jQuery(
																			this)
																			.attr(
																					"id"));

													var extension = extensions[jQuery(
															this).attr("id")];
													var page = extension.provider
															.create(
																	panel,
																	extension.id,
																	extension.title);

													page.hide();
													page.profiles = extension.profiles;
													panel.propertiesPages
															.push(page);
												}
											});
						} else {
							// Embedded Markup

							var page = extension.provider
							.create(this);
							
							this.propertiesPages.push(page);
							
							page.profiles = extension.profiles;
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
						if (!m_user
								.isCurrentProfileIn(this.propertiesPages[n].profiles)) {
							continue;
						}

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
										+ "ListItem")
								.click(
										{
											callbackScope : this,
											propertiesPage : this.propertiesPages[n],
											pageIndex : n
										},
										function(event) {
											event.data.callbackScope
													.hidePropertiesPages();
											event.data.propertiesPage.show();
											event.data.callbackScope.lastSelectedPageIndex = event.data.pageIndex;
										});
					}
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.disablePropertiesPage = function(id) {
					jQuery("#" + this.id + " #" + id + "ListItem").prop(
							"disabled", true);
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.enablePropertiesPage = function(id) {
					jQuery("#" + this.id + " #" + id + "ListItem").prop(
							"disabled", false);
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
				PropertiesPanel.prototype.show = function(page) {
					m_dialog.makeVisible(this.panel);
					this.showPropertiesPageList();
					this.hidePropertiesPages();

					if (this.propertiesPages.length >= 1) {
						if (page != null) {
							for ( var n = 0; n < this.propertiesPages.length; ++n) {
								if (this.propertiesPages[n].id == page) {
									this.propertiesPages[n].show();
								}
							}
						} else {
							this.propertiesPages[this.lastSelectedPageIndex]
									.show();
						}
					}

					this.clearErrorMessages();

					require("bpm-modeler/js/m_modelerViewLayoutManager")
							.adjustPanels();
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
					if (command.type == m_constants.CHANGE_USER_PROFILE_COMMAND) {
						this.setElement(this.element);

						return;
					}

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length) {

						for ( var i = 0; i < object.changes.modified.length; i++) {
							if (object.changes.modified[i].oid == this.element.oid) {
								m_utils.inheritFields(this.element,
										object.changes.modified[i]);
								this.setElement(this.element);
							} else if (this.element.modelElement != null
									&& object.changes.modified[i].oid == this.element.modelElement.oid) {
								m_utils
										.debug("Changes to be applied to Model Element of Properties Page:");
								m_utils.debug(this.element.modelElement);
								m_utils.debug(object.changes.modified[i]);
								m_utils.inheritFields(
										this.element.modelElement,
										object.changes.modified[i]);
								m_utils.debug(this.element.modelElement);
								this.setElement(this.element);
							}
						}
					}
				};

				/**
				 * 
				 */
				PropertiesPanel.prototype.assembleChangedObjectFromProperty = function(
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
				PropertiesPanel.prototype.assembleChangedObjectFromAttribute = function(
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
				PropertiesPanel.prototype.submitChanges = function(changes) {
					m_utils.debug("Changes to be submitted for UUID "
							+ this.getElementUuid() + ":");
					m_utils.debug(changes);
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.getModel().id, this.getElementUuid(),
									changes));
				};
			}
		});