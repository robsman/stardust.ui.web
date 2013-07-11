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
				"bpm-modeler/js/m_session", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_user",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_view",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_session,
				m_command, m_commandsController, m_user, m_dialog, m_view,
				m_i18nUtils) {
			return {
				create : function(id) {
					var view = new ModelElementView();
					i18modelelement();
					return view;
				}
			};

			function i18modelelement() {
				m_utils.jQuerySelect("#name")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));

				m_utils.jQuerySelect("label[for='descriptionTextarea']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));

				m_utils.jQuerySelect("#dataStructName")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.dataStructureName"));
				m_utils.jQuerySelect("#xsdtext")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.textInfo"));
				m_utils.jQuerySelect("#struct")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.structure"));
				m_utils.jQuerySelect("#enum")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.enumeration"));
				m_utils.jQuerySelect("#addElementButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.add"));
				m_utils.jQuerySelect("#deleteElementButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.delete"));
				m_utils.jQuerySelect("#moveElementUpButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.moveUp"));
				m_utils.jQuerySelect("#moveElementDownButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.moveDown"));
				m_utils.jQuerySelect("#moveElementDownButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.moveDown"));
				m_utils.jQuerySelect("#elementColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.propertyView.elementTable.column.element.name"));
				m_utils.jQuerySelect("#enumElementColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.propertyView.elementTable.column.enumElement.name"));
				m_utils.jQuerySelect("#typeColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.propertyView.elementTable.column.type.name"));
				m_utils.jQuerySelect("#cardinalityColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.propertyView.elementTable.column.cardinality.name"));
				m_utils.jQuerySelect("#fieldProp")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.heading.filedProperties"));
				m_utils.jQuerySelect("#prop")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.property"));
				m_utils.jQuerySelect("#val")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.value"));
				m_utils.jQuerySelect("#configuration")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.tab"));

			}

			/**
			 *
			 */
			function ModelElementView() {
				// Inheritance

				var view = m_view.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(ModelElementView.prototype, view);

				/**
				 *
				 */
				ModelElementView.prototype.initializeModelElementView = function(
						modelElement) {
					this.guidOutputRow = m_utils.jQuerySelect("#guidOutputRow");
					this.idOutputRow = m_utils.jQuerySelect("#idOutputRow");
					this.guidOutput = m_utils.jQuerySelect("#guidOutput");
					this.idOutput = m_utils.jQuerySelect("#idOutput");
					this.nameInput = m_utils.jQuerySelect("#nameInput");
					this.descriptionTextarea = m_utils.jQuerySelect("#descriptionTextarea");
					this.propertiesTabs = m_utils.jQuerySelect("#propertiesTabs");
					this.propertiesTabsList = m_utils.jQuerySelect("#propertiesTabsList");

					this.nameInput.change({
						"view" : this
					}, function(event) {
						var view = event.data.view;

						if (view.modelElement.name != view.nameInput.val()) {
							view.renameModelElement();
						}
					});

					this.registerInputForModelElementChangeSubmission(
							this.descriptionTextarea, "description");

					this.propertiesPages = [];
					var extensions = {};

					if (this.propertiesTabs != null) {
						var propertiesPagesExtensions = m_extensionManager
								.findExtensions("propertiesPage", "panelId",
										this.id);

						this.loadPropertiesPage(modelElement, extensions,
								propertiesPagesExtensions, 0);
					} else {
						this.setModelElement(modelElement);
						this.checkAndMarkIfReadonly();
					}
				};

				/**
				 *
				 */
				ModelElementView.prototype.checkAndMarkIfReadonly = function(override) {
					if (this.modelElement && (this.modelElement.isReadonly() || override)) {
						m_utils.markControlsReadonly(null, this.modelElement.isReadonly());
					}
				};

				/**
				 *
				 */
				ModelElementView.prototype.loadPropertiesPage = function(
						modelElement, extensions, propertiesPagesExtensions, n) {
					if (n == propertiesPagesExtensions.length) {
						this.propertiesTabs.tabs();
						this.setModelElement(modelElement);
						this.checkAndMarkIfReadonly();

						return;
					}

					var extension = propertiesPagesExtensions[n];

					extensions[extension.id] = extension;

					if (!m_session.initialize().technologyPreview
							&& extension.visibility == "preview") {
						// Skip page

						n += 1;

						this.loadPropertiesPage(modelElement, extensions,
								propertiesPagesExtensions, n);

						return;
					}

					var propertiesTabHeader = "";

					propertiesTabHeader += "<li>";
					propertiesTabHeader += "<a href='";
					propertiesTabHeader += "#" + extension.id;
					propertiesTabHeader += "'><img src='";
					propertiesTabHeader += extension.pageIconUrl;
					propertiesTabHeader += "'></img><span class='tabLabel'>";
					propertiesTabHeader += extension.pageName;
					propertiesTabHeader += "</span></a></li>";

					this.propertiesTabsList.append(propertiesTabHeader);

					var pageDiv = m_utils.jQuerySelect("<div id='" + extension.id
							+ "'></div>");

					this.propertiesTabs.append(pageDiv);

					if (extension.pageHtmlUrl != null) {
						// TODO this variable may be overwritten in the
						// loop, find mechanism to pass data to load
						// callback

						var view = this;

						pageDiv.load(extension.pageHtmlUrl, function(response,
								status, xhr) {
							if (status == "error") {
								var msg = "Properties Page Load Error: "
										+ xhr.status + " " + xhr.statusText;

								m_utils.jQuerySelect(this).append(msg);
								view.loadPropertiesPage(modelElement,
										propertiesPagesExtensions, ++n);
							} else {
								var extension = extensions[m_utils.jQuerySelect(this).attr(
										"id")];
								var page = extension.provider.create(view,
										extension.id);

								view.propertiesPages.push(page);

								m_utils.debug("Page loaded");
								m_utils.debug(view.propertiesPages);
								view.loadPropertiesPage(modelElement,
										extensions, propertiesPagesExtensions,
										++n);
							}
						});
					} else {
						// Embedded Markup

						// this.propertiesPages.push(extension.provider
						// .create(this));
						view.loadPropertiesPage(modelElement, extensions,
								propertiesPagesExtensions, ++n);
					}
				};

				/**
				 *
				 */
				ModelElementView.prototype.initializeModelElement = function(
						modelElement) {
					this.modelElement = modelElement;

					if (m_user.getCurrentRole() != m_constants.INTEGRATOR_ROLE) {
						m_dialog.makeInvisible(this.guidOutputRow);
						m_dialog.makeInvisible(this.idOutputRow);
					} else {
						m_dialog.makeVisible(this.guidOutputRow);
						m_dialog.makeVisible(this.idOutputRow);
						this.guidOutput.empty();
						this.guidOutput.append(this.modelElement.uuid);
						this.idOutput.empty();
						this.idOutput.append(this.modelElement.id);
					}

					this.nameInput.val(this.modelElement.name);
					this.descriptionTextarea.val(this.modelElement.description);

					if (this.modelElement.attributes == null) {
						this.modelElement.attributes = {};
					}

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};

				/**
				 *
				 */
				ModelElementView.prototype.getModelElement = function() {
					return this.modelElement;
				};

				/**
				 *
				 */
				ModelElementView.prototype.getModel = function() {
					return this.getModelElement().model;
				};

				/**
				 * All Model Elements managed via Model Views should have a
				 * UUID.
				 */
				ModelElementView.prototype.getElementUuid = function() {
					return this.getModelElement().uuid;
				};

				/**
				 *
				 */
				ModelElementView.prototype.assembleChangedObjectFromProperty = function(
						property, value) {
					var element = {};

					element[property] = value;

					return element;
				};

				/**
				 *
				 */
				ModelElementView.prototype.assembleChangedObjectFromAttribute = function(
						attribute, value) {
					var element = {
						attributes : {}
					};

					element.attributes[attribute] = value;

					return element;
				};

				/**
				 *
				 */
				ModelElementView.prototype.renameModelElement = function(name) {
					this.submitChanges({
						name : this.nameInput.val()
					});
				};

				/**
				 *
				 */
				ModelElementView.prototype.submitChanges = function(changes, skipValidation) {
					if (!skipValidation && !this.validate()) {
						return;
					}

					// Generic attributes
					// TODO Is this really needed?

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					m_commandsController.submitCommand(m_command
							.createUpdateModelElementWithUUIDCommand(this
									.getModelElement().model.id, this
									.getModelElement().uuid, changes));
				};

				/**
				 *
				 */
				ModelElementView.prototype.registerInputForModelElementChangeSubmission = function(
						input, property) {
					input.change({
						"view" : this,
						"input" : input
					}, function(event) {
						var view = event.data.view;
						var input = event.data.input;

						if (view.getModelElement()[property] != input.val()) {
							var modelElement = {};
							modelElement[property] = input.val();

							view.submitChanges(modelElement);
						}
					});
				};

				/**
				 *
				 */
				ModelElementView.prototype.registerInputForModelElementAttributeChangeSubmission = function(
						input, attribute) {
					input.change({
						"view" : this,
						"input" : input
					}, function(event) {
						var view = event.data.view;
						var input = event.data.input;

						view.submitModelElementAttributeChange(attribute, input
								.val());
					});
				};

				/**
				 *
				 */
				ModelElementView.prototype.submitModelElementAttributeChange = function(
						attribute, value) {
					if (this.getModelElement().attributes[attribute] != value) {
						var modelElement = {
							attributes : {}
						};
						modelElement.attributes[attribute] = value;
						this.submitChanges(modelElement);
					}
				};

				/**
				 *
				 */
				ModelElementView.prototype.registerCheckboxInputForModelElementAttributeChangeSubmission = function(
						input, attribute) {
					input
							.click(
									{
										"view" : this,
										"input" : input
									},
									function(event) {
										var view = event.data.view;
										var input = event.data.input;

										if (view.getModelElement().attributes[attribute] != input
												.val()) {
											var modelElement = {
												attributes : {}
											};
											modelElement.attributes[attribute] = input
													.is(":checked");

											view.submitChanges(modelElement);
										}
									});
				};

				/**
				 *
				 */
				ModelElementView.prototype.processCommand = function(command) {
					m_utils.debug("===> Process Command for " + this.id);
					m_utils.debug(command);

					if (command.type == m_constants.CHANGE_USER_PROFILE_COMMAND) {
						this.setModelElement(this.getModelElement());
						this.checkAndMarkIfReadonly();

						return;
					}

					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (object && object.changes && object.changes.modified) {
						for ( var i = 0; i < object.changes.modified.length; i++) {
							if (this.getModelElement().uuid == object.changes.modified[i].uuid) {
								m_utils.inheritFields(this.getModelElement(),
										object.changes.modified[i]);

								this.setModelElement(this.getModelElement());
							}
						}
						if (command.commandId === "modelLockStatus.update") {
							this.checkAndMarkIfReadonly(true);
						}
					}

					this.postProcessCommand(command);
				};

				/**
				 * In case individual views want to do additional stuff they can
				 * over ride this function.
				 */
				ModelElementView.prototype.postProcessCommand = function(
						command) {
				};
			}
		});