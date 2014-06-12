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
				"bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_angularContextUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_session, m_user, m_command,
				 m_commandsController, m_dialog, m_communicationController, m_angularContextUtils) {

			var currentPropertiesPanel = null;

			return {
				initializePropertiesPanel : function(element, page) {

					if (currentPropertiesPanel != null) {
						currentPropertiesPanel.hide();
						m_utils.markControlsReadonly('modelerPropertiesPanelWrapper', false);
					}

					currentPropertiesPanel = element.propertiesPanel;

					if (currentPropertiesPanel != null) {
						if (currentPropertiesPanel.diagram && currentPropertiesPanel.diagram.process
								&& currentPropertiesPanel.diagram.process.isReadonly()) {
							m_utils.markControlsReadonly('modelerPropertiesPanelWrapper');
						}

						currentPropertiesPanel.setElement(element);
						currentPropertiesPanel.show(page);
					}
				},

				// TODO Homogenize calls

				initializeProcessPropertiesPanel : function(
						processPropertiesPanel) {
					if (currentPropertiesPanel != null) {
						currentPropertiesPanel.hide();
						m_utils.markControlsReadonly('modelerPropertiesPanelWrapper', false);
					}

					currentPropertiesPanel = processPropertiesPanel;

					if (currentPropertiesPanel.diagram && currentPropertiesPanel.diagram.process
							&& currentPropertiesPanel.diagram.process.isReadonly()) {
						m_utils.markControlsReadonly('modelerPropertiesPanelWrapper');
					}

					currentPropertiesPanel.show();
				},

				createPropertiesPanel : function(id, angularized) {
					var propertiesPanel = new PropertiesPanel(id, angularized);

					return propertiesPanel;
				}
			};

			/**
			 *
			 */
			function PropertiesPanel(id, angularized) {
				this.id = id;
				this.angularized = angularized;
				this.panel = m_utils.jQuerySelect("#" + this.id);
				this.propertiesPageList = m_utils.jQuerySelect("#propertiesPageList");
				this.applyButton = m_utils.jQuerySelect("#" + this.id + " #applyButton");
				this.resetButton = m_utils.jQuerySelect("#" + this.id + " #resetButton");
				this.errorMessagesRow = m_utils.jQuerySelect("#" + this.id
						+ " #errorMessagesRow");
				this.errorMessagesList = m_utils.jQuerySelect("#" + this.id
						+ " #errorMessagesList");
				this.warningMessagesList = m_utils.jQuerySelect("#" + this.id
						+ " #warningMessagesList");
				this.propertiesPages = [];
				this.errorMessages = [];
				this.warningMessages = [];
				this.helpPanel = m_utils.jQuerySelect("#" + this.id + " #helpPanel");
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
					return m_utils.jQuerySelect("#" + this.id + " #" + inputId);
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
					var dynamicExtensions = [];
					var dynamicPropertiesPages = [];

					var propertiesPanel = m_utils.jQuerySelect("#" + this.id + " #propertiesPagesCell");
					
					for ( var n = 0; n < propertiesPages.length; n++) {
						var extension = propertiesPages[n];

						extensions[extension.id] = extension;

						if (!m_session.initialize().technologyPreview
								&& extension.visibility == "preview") {

							if (extension.pageHtmlUrl == null) {
								m_dialog.makeInvisible(m_utils.jQuerySelect("#" + this.id
										+ " #" + extension.id));
							}

							continue;
						}

						m_utils.debug("Load Properties Page "
								+ extension.id);

						if (extension.pageHtmlUrl != null) {
							if (this.angularized) {
								dynamicExtensions.push(extension);	
							} else {
								var pageDiv = m_utils.jQuerySelect("<div id=\""
										+ extension.id
										+ "\" class=\"propertiesPage\"></div>");

								propertiesPanel.append(pageDiv);

								// TODO this variable may be overwritten in the
								// loop, find mechanism to pass data to load
								// callback

								var panel = this;

								// TODO - review
								// Replaced m_utils.jQuerySelect(<div>).load call with a synchronous ajax request as, the
								// async load request caused the property page tabs to not get loaded in time,
								// and in Chrome browser, these tabs didn't get displayed in the first instance.
								m_communicationController
										.syncGetData(
												{
													url : extension.pageHtmlUrl
												},
												{
													error : function(err) {
														var msg = "Properties Page Load Error: "
																+ err.status
																+ " "
																+ err.statusText;

														m_utils.jQuerySelect(this).append(msg);
														m_utils.debug(msg);
													},
													success : function(data) {
														m_utils
																.debug("Page loaded: "
																		+ pageDiv
																				.attr(
																						"id"));
														pageDiv.append(data);
														var extension = extensions[pageDiv.attr("id")];
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
							}
						} else {
							// Embedded Markup

							var page = extension.provider
							.create(this);

							this.propertiesPages.push(page);

							page.profiles = extension.profiles;
						}
					}

					if (this.angularized) {
						var self = this;
						m_angularContextUtils.runInActiveViewContext(function($scope){
							if (!$scope[self.id + "Onload"]) {
								$scope[self.id + "Onload"] = function(extension) {
									var page = extension.provider.create(self, extension.id, extension.title);
									page.hide();
									page.profiles = extension.profiles;
									
									dynamicPropertiesPages.push({extension: extension, page: page});

									// Once all propertiesPages are loaded build the properties page list
									// Sort the Pages with same order as defined in Extension
									if (dynamicPropertiesPages.length == dynamicExtensions.length) {
										for(var i in dynamicExtensions) {
											for(var j in dynamicPropertiesPages) {
												if (dynamicExtensions[i].id == dynamicPropertiesPages[j].extension.id) {
													self.propertiesPages.push(dynamicPropertiesPages[j].page);
													break;
												} 
											}
										}
										var showing = self.propertiesPageList.attr("showing");
										if (!showing || self.id == showing) {
											self.showPropertiesPageList();
										}
									}
								};
							}
							
							if (!$scope[self.id]) {
								$scope[self.id] = {};
							}
							$scope[self.id].extensions = dynamicExtensions;
						});
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

					this.propertiesPageList.attr("showing", this.id);

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
								+ "\" class=\"toolbarButton noDataChange\" />" + "</td>"
								+ "</tr>");

						m_utils.jQuerySelect(
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
					m_utils.jQuerySelect("#" + this.id + " #" + id + "ListItem").prop(
							"disabled", true);
				};

				/**
				 *
				 */
				PropertiesPanel.prototype.enablePropertiesPage = function(id) {
					m_utils.jQuerySelect("#" + this.id + " #" + id + "ListItem").prop(
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
					m_utils.debug("Clear error messages");
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
				PropertiesPanel.prototype.clearWarningMessages = function() {
					m_utils.debug("Clear warning messages");
					m_dialog.makeInvisible(this.errorMessagesRow);
					this.warningMessages = [];
					this.warningMessagesList.empty();
				};

				/**
				 *
				 */
				PropertiesPanel.prototype.showWarningMessages = function() {
					if (this.warningMessages.length != 0) {
						m_dialog.makeVisible(this.errorMessagesRow);

						this.warningMessagesList.empty();
						for ( var n in this.warningMessages) {
							this.warningMessagesList.append("<li>"+"Warning: "
									+ this.warningMessages[n] + "</li>");
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

						// Update the selectable property pages list
						// for the given profile
						currentPropertiesPanel.show();

						return;
					}

					if (!this.element) {
						return;
					}

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length) {

						for ( var i = 0; i < object.changes.modified.length; i++) {
							// TODO - improve logic
							// Ideally all comparisons should be made using UUID only
							// As some elemements may not have UUID, for now,
							// compares using UUID if present, else compares using OID as before.
							if ((object.changes.modified[i].uuid && this.element.uuid
									&& object.changes.modified[i].uuid == this.element.uuid) 
									|| ((!object.changes.modified[i].uuid || !this.element.uuid) 
											&& object.changes.modified[i].oid == this.element.oid)) {
								m_utils.inheritFields(this.element,
										object.changes.modified[i]);
								this.setElement(this.element);
							} else if (this.element.modelElement != null
									&& ((object.changes.modified[i].uuid && this.element.modelElement.uuid
											&& object.changes.modified[i].uuid == this.element.modelElement.uuid)
											|| ((!object.changes.modified[i].uuid || !this.element.modelElement.uuid)
													&& object.changes.modified[i].oid == this.element.modelElement.oid))) {
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