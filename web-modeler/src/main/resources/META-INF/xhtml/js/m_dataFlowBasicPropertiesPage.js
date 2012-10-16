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
					this.inInput = this.mapInputId("inInput");
					this.outInput = this.mapInputId("outInput");
					this.descriptionInput = this.mapInputId("descriptionInput");
					this.dataPathInput = this.mapInputId("dataPathInput");
					this.dataPathOutput = this.mapInputId("dataPathOutput");
					this.inputAccessPointPanel = this
							.mapInputId("inputAccessPointPanel");
					this.outputAccessPointPanel = this
							.mapInputId("outputAccessPointPanel");
					this.inputAccessPointSelectInput = this
							.mapInputId("inputAccessPointSelectInput");
					this.outputAccessPointSelectInput = this
							.mapInputId("outputAccessPointSelectInput");

					this.inInput.click({
						"callbackScope" : this
					}, function(event) {
						event.data.callbackScope
								.setDirection(event.data.callbackScope.inInput
										.is(":checked"),
										event.data.callbackScope.outInput
												.is(":checked"));
					});

					this.outInput.click({
						"callbackScope" : this
					}, function(event) {
						event.data.callbackScope
								.setDirection(event.data.callbackScope.inInput
										.is(":checked"),
										event.data.callbackScope.outInput
												.is(":checked"));
					});

					this.registerInputForModelElementChangeSubmission(
							this.descriptionInput, "description");
					this.registerCheckboxInputForModelElementChangeSubmission(
							this.inInput, "inDataMapping");
					this.registerCheckboxInputForModelElementChangeSubmission(
							this.outInput, "outDataMapping");

					this.inputAccessPointSelectInput
							.change(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;
										var dataFlow = page.propertiesPanel.element.modelElement;

										// TODO Adjust flow direction based on
										// access point direction?
										page
												.submitChange({
													accessPointId : dataFlow.activity.accessPoints[page.inputAccessPointSelectInput
															.val()].id,
													accessPointContext : dataFlow.activity.accessPoints[page.inputAccessPointSelectInput
															.val()].context
												});
									});
					this.outputAccessPointSelectInput
							.change(
									{
										page : this
									},
									function(event) {
										var page = event.data.page;
										var dataFlow = page.propertiesPanel.element.modelElement;

										// TODO Adjust flow direction based on
										// access point direction?
										page
												.submitChange({
													accessPointId : dataFlow.activity.accessPoints[page.outputAccessPointSelectInput
															.val()].id,
													accessPointContext : dataFlow.activity.accessPoints[page.outputAccessPointSelectInput
															.val()].context
												});
									});
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.setDirection = function(
						inMapping, outMapping) {
					if (inMapping) {
						m_dialog
								.makeVisible(this.inputAccessPointPanel);
					} else {
						m_dialog
								.makeInvisible(this.inputAccessPointPanel);
					}

					if (outMapping) {
						m_dialog
								.makeVisible(this.outputAccessPointPanel);
					} else {
						m_dialog
								.makeInvisible(this.outputAccessPointPanel);
					}

					this.inInput.attr("checked", inMapping);
					this.outInput.attr("checked", outMapping);
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
									this.propertiesPanel.element.modelElement.inDataMapping,
									this.propertiesPanel.element.modelElement.outDataMapping);
					// this.dataPathInput
					// .val(this.propertiesPanel.element.modelElement.dataPath);
				};
			}
		});