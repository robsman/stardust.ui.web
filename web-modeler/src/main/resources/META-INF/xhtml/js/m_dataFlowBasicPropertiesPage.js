/**
 * @author Marc.Gille
 */

define(
		[ "m_utils", "m_constants", "m_user", "m_dialog",
				"m_basicPropertiesPage" ],
		function(m_utils, m_constants, m_user, m_dialog, m_basicPropertiesPage) {
			return {
				create : function(propertiesPanel) {
					var page = new DataFlowBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function DataFlowBasicPropertiesPage(propertiesPanel) {

				// Inheritance

				var propertiesPage = m_basicPropertiesPage
						.create(propertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(DataFlowBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();

					this.inputInput = this.mapInputId("inputInput");
					this.outputInput = this.mapInputId("outputInput");
					this.descriptionInput = this.mapInputId("descriptionInput");
					this.inputDataPathInput = this
							.mapInputId("inputDataPathInput");
					this.outputDataPathInput = this
							.mapInputId("outputDataPathInput");
					this.inputAccessPointPanel = this
							.mapInputId("inputAccessPointPanel");
					this.outputAccessPointPanel = this
							.mapInputId("outputAccessPointPanel");
					this.inputAccessPointSelectInput = this
							.mapInputId("inputAccessPointSelectInput");
					this.outputAccessPointSelectInput = this
							.mapInputId("outputAccessPointSelectInput");
					this.inputAccessPointSelectInputPanel = this
							.mapInputId("inputAccessPointSelectInputPanel");
					this.outputAccessPointSelectInputPanel = this
							.mapInputId("outputAccessPointSelectInputPanel");

					this.inputInput
							.click(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;

										if (page.inputInput.is(":checked")
												&& page.propertiesPanel.element.modelElement.inputDataMapping == null) {
											page.propertiesPanel.element.modelElement.inputDataMapping = {};
										} else if (!page.outputInput
												.is(":checked")) {
											// At least one checkbox has to be
											// checked
											page.inputInput.attr("checked",
													true);

											return;
										}

										page
												.setDirection(page.inputInput
														.is(":checked"),
														page.outputInput
																.is(":checked"));

										page
												.submitChanges({
													// TODO Usually, we are not
													// submitting the object
													// itself
													modelElement : page.propertiesPanel.element.modelElement
												});
									});

					this.outputInput
							.click(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;

										if (page.inputInput.is(":checked")
												&& page.propertiesPanel.element.modelElement.outputDataMapping == null) {
											page.propertiesPanel.element.modelElement.outputDataMapping = {};
										} else if (!page.inputInput
												.is(":checked")) {
											// At least one checkbox has to be
											// checked
											page.outputInput.attr("checked",
													true);

											return;
										}

										page
												.setDirection(page.inputInput
														.is(":checked"),
														page.outputInput
																.is(":checked"));
										page
												.submitChanges({
													// TODO Usually, we are not
													// submitting the object
													// itself
													modelElement : page.propertiesPanel.element.modelElement
												});
									});

					this.registerInputForModelElementChangeSubmission(
							this.descriptionInput, "description");

					this.inputAccessPointSelectInput
							.change(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;
										var value = page.inputAccessPointSelectInput
												.val();

										if (value == "DEFAULT") {
											page.propertiesPanel.element.modelElement.inputDataMapping.accessPointContext = null;
											page.propertiesPanel.element.modelElement.inputDataMapping.accessPointId = null;
										} else {
											var data = value.split(":");

											page.propertiesPanel.element.modelElement.inputDataMapping.accessPointContext = data[0];
											page.propertiesPanel.element.modelElement.inputDataMapping.accessPointId = data[1];
										}

										page
												.submitChanges({
													// TODO Usually, we are not
													// submitting the object
													// itself
													modelElement : this.propertiesPanel.element.modelElement
												});
									});
					this.outputAccessPointSelectInput
							.change(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;
										var value = page.outputAccessPointSelectInput
												.val();

										if (value == "DEFAULT") {
											page.propertiesPanel.element.modelElement.outputDataMapping.accessPointContext = null;
											page.propertiesPanel.element.modelElement.outputDataMapping.accessPointId = null;
										} else {
											var data = value.split(":");

											page.propertiesPanel.element.modelElement.outputDataMapping.accessPointContext = data[0];
											page.propertiesPanel.element.modelElement.outputDataMapping.accessPointId = data[1];
										}

										page
												.submitChanges({
													// TODO Usually, we are not
													// submitting the object
													// itself
													modelElement : page.propertiesPanel.element.modelElement
												});
									});
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.setDirection = function(
						hasInputMapping, hasOutputMapping) {
					if (hasInputMapping) {
						m_dialog.makeVisible(this.inputAccessPointPanel);
					} else {
						m_dialog.makeInvisible(this.inputAccessPointPanel);
					}

					if (hasOutputMapping) {
						m_dialog.makeVisible(this.outputAccessPointPanel);
					} else {
						m_dialog.makeInvisible(this.outputAccessPointPanel);
					}

					this.inputInput.attr("checked", hasInputMapping);
					this.outputInput.attr("checked", hasOutputMapping);
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.populateInputAccessPointSelectInput = function(
						dataFlow) {
					this.inputAccessPointSelectInput.empty();

					var contexts = {};
					var count = 0;

					for ( var n in dataFlow.activity.accessPoints) {
						var accessPoint = dataFlow.activity.accessPoints[n];

						if (accessPoint.direction == m_constants.OUT_ACCESS_POINT) {
							continue;
						}

						if (contexts[accessPoint.context] == null) {
							contexts[accessPoint.context] = {};
						}

						contexts[accessPoint.context][accessPoint.id] = accessPoint;
						count++;
					}

					if (count == 0) {
						m_dialog
								.makeInvisible(this.inputAccessPointSelectInputPanel);

						return;
					} else {
						m_dialog
								.makeVisible(this.inputAccessPointSelectInputPanel);
					}

					// TODO Use method of m_activity; proper type binding
					// required
					if (dataFlow.activity.activityType != m_constants.APPLICATION_ACTIVITY_TYPE) {
						this.inputAccessPointSelectInput
								.append("<option value='DEFAULT'>Default</option>"); // I18N
					} else {
						this.inputAccessPointSelectInput
								.append("<option value='DEFAULT'>(To be defined)</option>"); // I18N
					}

					for ( var i in contexts) {
						var group = jQuery("<optgroup label='" + i + "'/>"); // I18N

						this.inputAccessPointSelectInput.append(group);

						for ( var m in contexts[i]) {
							var accessPoint = contexts[i][m];
							var option = "<option value='";

							option += i;
							option += ":";
							option += accessPoint.id;
							option += "'>";
							option += accessPoint.name;
							option += "</option>";

							group.append(option);
						}
					}
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.populateOutputAccessPointSelectInput = function(
						dataFlow) {
					this.outputAccessPointSelectInput.empty();

					var contexts = {};
					var count = 0;

					for ( var n in dataFlow.activity.accessPoints) {
						var accessPoint = dataFlow.activity.accessPoints[n];

						if (accessPoint.direction == m_constants.IN_ACCESS_POINT) {
							continue;
						}

						if (contexts[accessPoint.context] == null) {
							contexts[accessPoint.context] = {};
						}

						contexts[accessPoint.context][accessPoint.id] = accessPoint;
						count++;
					}

					if (count == 0) {
						m_dialog
								.makeInvisible(this.outputAccessPointSelectInputPanel);

						return;
					} else {
						m_dialog
								.makeVisible(this.outputAccessPointSelectInputPanel);
					}

					// TODO Use method of m_activity; proper type binding
					// required
					if (dataFlow.activity.activityType != m_constants.APPLICATION_ACTIVITY_TYPE) {
						this.outputAccessPointSelectInput
								.append("<option value='DEFAULT'>Default</option>");
					} else {
						this.outputAccessPointSelectInput
								.append("<option value='DEFAULT'>(To be defined)</option>"); // I18N
					}

					for ( var i in contexts) {
						var group = jQuery("<optgroup label='" + i + "'/>"); // I18N

						this.outputAccessPointSelectInput.append(group);

						for ( var m in contexts[i]) {
							var accessPoint = contexts[i][m];
							var option = "<option value='";

							option += i;
							option += ":";
							option += accessPoint.id;
							option += "'>";
							option += accessPoint.name;
							option += "</option>";

							group.append(option);
						}
					}
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					m_utils.debug("===> Data Flow");
					m_utils.debug(this.propertiesPanel.element.modelElement);

					this
							.populateInputAccessPointSelectInput(this.propertiesPanel.element.modelElement);
					this
							.populateOutputAccessPointSelectInput(this.propertiesPanel.element.modelElement);
					this.descriptionInput
							.val(this.propertiesPanel.element.modelElement.description);
					this
							.setDirection(
									this.propertiesPanel.element.modelElement.inputDataMapping != null,
									this.propertiesPanel.element.modelElement.outputDataMapping != null);

					if (this.propertiesPanel.element.modelElement.inputDataMapping) {
						this.inputDataPathInput
								.val(this.propertiesPanel.element.modelElement.inputDataMapping.dataPath);
						if (this.propertiesPanel.element.modelElement.inputDataMapping.accessPointId == null) {
							this.inputAccessPointSelectInput.val("DEFAULT");
						} else {
							this.inputAccessPointSelectInput
									.val(this.propertiesPanel.element.modelElement.inputDataMapping.accessPointContext
											+ ":"
											+ this.propertiesPanel.element.modelElement.inputDataMapping.accessPointId);
						}
					}

					if (this.propertiesPanel.element.modelElement.outputDataMapping) {
						this.outputDataPathInput
								.val(this.propertiesPanel.element.modelElement.outputDataMapping.dataPath);
						if (this.propertiesPanel.element.modelElement.outputDataMapping.accessPointId == null) {
							this.outputAccessPointSelectInput.val("DEFAULT");
						} else {
							this.outputAccessPointSelectInput
									.val(this.propertiesPanel.element.modelElement.outputDataMapping.accessPointContext
											+ ":"
											+ this.propertiesPanel.element.modelElement.outputDataMapping.accessPointId);
						}
					}
				};
			}
		});