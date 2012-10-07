/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_extensionManager", "m_command",
				"m_commandsController", "m_user", "m_dialog", "m_view" ],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_user, m_dialog, m_view) {
			return {
				create : function(id) {
					var view = new ModelElementView();

					return view;
				}
			};

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
					this.guidOutputRow = jQuery("#guidOutputRow");
					this.idOutputRow = jQuery("#idOutputRow");
					this.guidOutput = jQuery("#guidOutput");
					this.idOutput = jQuery("#idOutput");
					this.nameInput = jQuery("#nameInput");
					this.descriptionTextarea = jQuery("#descriptionTextarea");
					this.propertiesTabs = jQuery("#propertiesTabs");
					this.propertiesTabsList = jQuery("#propertiesTabsList");

					this.nameInput.change({
						"view" : this
					}, function(event) {
						var view = event.data.view;

						if (!view.validate()) {
							return;
						}

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

						return;
					}

					var extension = propertiesPagesExtensions[n];

					extensions[extension.pageId] = extension;

					var propertiesTabHeader = "";

					propertiesTabHeader += "<li>";
					propertiesTabHeader += "<a href='";
					propertiesTabHeader += "#" + extension.pageId;
					propertiesTabHeader += "'><img src='";
					propertiesTabHeader += extension.pageIconUrl;
					propertiesTabHeader += "'></img><span class='tabLabel'>";
					propertiesTabHeader += extension.pageName;
					propertiesTabHeader += "</span></a></li>";

					this.propertiesTabsList.append(propertiesTabHeader);

					var pageDiv = jQuery("<div id='" + extension.pageId
							+ "'>Bla</div>");

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

								jQuery(this).append(msg);
								view.loadPropertiesPage(modelElement,
										propertiesPagesExtensions, ++n);
							} else {
								var extension = extensions[jQuery(this).attr(
										"id")];
								var page = extension.provider.create(view,
										extension.pageId);

								view.propertiesPages.push(page);

								m_utils.debug("Page loaded");
								m_utils.debug(view.propertiesPages);
								view.loadPropertiesPage(modelElement, extensions,
										propertiesPagesExtensions, ++n);
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
				 * All Model Elements managed via Model Views should have a UUID.
				 */
				ModelElementView.prototype.getElementUuid = function() {
					return this.getModelElement().uuid;
				};

				/**
				 * 
				 */
				ModelElementView.prototype.assembleChangedObjectFromProperty = function(
						property, value) {
					var element = {
					};

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
						name : this.nameInput.val(),
						id : m_utils.generateIDFromName(this.nameInput.val())
					});
				};

				/**
				 * 
				 */
				ModelElementView.prototype.submitChanges = function(changes) {
					// Generic attributes
					// TODO Is this really needed?

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					m_dialog.showWaitCursor();
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

						if (!view.validate()) {
							return;
						}

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
					input
							.change(
									{
										"view" : this,
										"input" : input
									},
									function(event) {
										var view = event.data.view;
										var input = event.data.input;

										if (!view.validate()) {
											return;
										}

										if (view.getModelElement().attributes[attribute] != input
												.val()) {
											var modelElement = {
												attributes : {}
											};
											modelElement.attributes[attribute] = input
													.val();

											view.submitChanges(modelElement);
										}
									});
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

										if (!view.validate()) {
											return;
										}

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
					m_utils.debug("===> Process Command for "+ this.id);
					m_utils.debug(command);

					if (command.type == m_constants.CHANGE_USER_PROFILE_COMMAND) {
						this.setModelElement(this.getModelElement());

						return;
					}

					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.getModelElement().oid) {

						m_utils.inheritFields(this.getModelElement(),
								object.changes.modified[0]);

						this.setModelElement(this.getModelElement());
					}
				};
			}
		});