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

					this.inputInput.click({
						page : this
					}, function(event) {
						var page = event.data.page;

						page.setDirection(page.inputInput.is(":checked"),
								page.outputInput.is(":checked"));
						page.submitChanges({
							modelElement : {
								inputDataMapping : page.inputInput
										.is(":checked")
							}
						});
					});

					this.outputInput.click({
						page : this
					}, function(event) {
						var page = event.data.page;

						page.setDirection(page.inputInput.is(":checked"),
								page.outputInput.is(":checked"));
						page.submitChanges({
							modelElement : {
								outputDataMapping : page.outputInput
										.is(":checked")
							}
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

										page
												.submitChanges({
													modelElement : {
														inputAccessPointId : page.inputAccessPointSelectInput
																.val(),
														inputAccessPointContext : "TBD"
													}
												});
									});
					this.outputAccessPointSelectInput
							.change(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;

										page
												.submitChanges({
													modelElement : {
														outputAccessPointId : page.outputAccessPointSelectInput
																.val(),
														outputAccessPointContext : "TBD"
													}
												});
									});
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.setDirection = function(
						inputMapping, outputMapping) {
					if (inputMapping) {
						m_dialog.makeVisible(this.inputAccessPointPanel);
					} else {
						m_dialog.makeInvisible(this.inputAccessPointPanel);
					}

					if (outputMapping) {
						m_dialog.makeVisible(this.outputAccessPointPanel);
					} else {
						m_dialog.makeInvisible(this.outputAccessPointPanel);
					}

					this.inputInput.attr("checked", inputMapping);
					this.outputInput.attr("checked", outputMapping);
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.populateInputAccessPointSelectInput = function(
						dataFlow) {
					this.inputAccessPointSelectInput.empty();

					var contexts = {};

					for ( var n in dataFlow.activity.accessPoints) {
						var accessPoint = dataFlow.activity.accessPoints[n];

						if (accessPoint.direction == m_constants.OUT_ACCESS_POINT) {
							continue;
						}

						if (contexts[accessPoint.context] == null) {
							contexts[accessPoint.context] = {};
						}

						contexts[accessPoint.context][accessPoint.id] = accessPoint;
					}

					for ( var i in contexts) {
						var group = jQuery("<optgroup label='" + i + "'/>"); // I18N

						this.inputAccessPointSelectInput.append(group);

						for ( var m in contexts[i]) {
							var accessPoint = contexts[i][m];
							var option = "<option value\"";

							option += accessPoint.id;
							option += "\">";
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

					for ( var n in dataFlow.activity.accessPoints) {
						var accessPoint = dataFlow.activity.accessPoints[n];

						if (accessPoint.direction == m_constants.IN_ACCESS_POINT) {
							continue;
						}

						if (contexts[accessPoint.context] == null) {
							contexts[accessPoint.context] = {};
						}

						contexts[accessPoint.context][accessPoint.id] = accessPoint;
					}

					for ( var i in contexts) {
						var group = jQuery("<optgroup label='" + i + "'/>"); // I18N

						this.outputAccessPointSelectInput.append(group);

						for ( var m in contexts[i]) {
							var accessPoint = contexts[i][m];
							var option = "<option value\"";

							option += accessPoint.id;
							option += "\">";
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
					this
							.populateInputAccessPointSelectInput(this.propertiesPanel.element.modelElement);
					this
							.populateOutputAccessPointSelectInput(this.propertiesPanel.element.modelElement);
					this.descriptionInput
							.val(this.propertiesPanel.element.modelElement.description);
					this
							.setDirection(
									this.propertiesPanel.element.modelElement.inputDataMapping,
									this.propertiesPanel.element.modelElement.outputDataMapping);
					this.inputDataPathInput
							.val(this.propertiesPanel.element.modelElement.inputDataPath);
					this.outputDataPathInput
							.val(this.propertiesPanel.element.modelElement.outputDataPath);
				};
			}
		});