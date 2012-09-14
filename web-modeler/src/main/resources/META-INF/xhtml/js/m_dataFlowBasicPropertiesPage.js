/**
 * @author Marc.Gille
 */

define(
		[ "m_utils", "m_constants", "m_user", "m_dialog", "m_propertiesPage" ],
		function(m_utils, m_constants, m_user, m_dialog, m_propertiesPage) {
			return {
				create : function(propertiesPanel) {
					var page = new DataFlowBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function DataFlowBasicPropertiesPage(propertiesPanel) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "basicPropertiesPage", "Basic");

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
					this.accessPointSelectInput = this
							.mapInputId("accessPointSelectInput");

					// Initialize callbacks

					this.inInput.click({
						"callbackScope" : this
					}, function(event) {
						if (event.data.callbackScope.inInput.is(":checked")) {
							event.data.callbackScope.dataPathInput
									.removeAttr("disabled");
						} else {
							event.data.callbackScope.dataPathInput.attr(
									"disabled", true);
							event.data.callbackScope.dataPathInput.val(null);
						}
					});

					this.outInput.click({
						"callbackScope" : this
					}, function(event) {
						if (event.data.callbackScope.outInput.is(":checked")) {
							event.data.callbackScope.dataPathOutput
									.removeAttr("disabled");
						} else {
							event.data.callbackScope.dataPathOutput.attr(
									"disabled", true);
							event.data.callbackScope.dataPathOutput.val(null);
						}
					});

					this.registerInputForModelElementChangeSubmission(
							this.descriptionInput, "description");
					this.registerCheckboxInputForModelElementChangeSubmission(
							this.inInput, "inDataMapping");
					this.registerCheckboxInputForModelElementChangeSubmission(
							this.outInput, "outDataMapping");

					this.accessPointSelectInput
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
													accessPointId : dataFlow.activity.accessPoints[page.accessPointSelectInput
															.val()].id,
													accessPointContext : dataFlow.activity.accessPoints[page.accessPointSelectInput
															.val()].context
												});
									});
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.populateAccessPointSelectInput = function(
						dataFlow) {
					this.accessPointSelectInput.empty();

					m_utils.debug("===> Data Flow Activity");
					m_utils.debug(dataFlow.activity);

					for ( var n in dataFlow.activity.accessPoints) {
						var accessPoint = dataFlow.activity.accessPoints[n];

						m_utils.debug("Access Point");
						m_utils.debug(accessPoint);

						var option = "<option value\"";

						option += accessPoint.id;
						option += "\">";
						option += accessPoint.name;
						option += " (";
						option += accessPoint.context;
						option += ")</option>";

						this.accessPointSelectInput.append(option);
					}
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.setElement = function() {
					this
							.populateAccessPointSelectInput(this.propertiesPanel.element.modelElement);
					this.descriptionInput
							.val(this.propertiesPanel.element.modelElement.description);
					this.inInput
							.attr(
									"checked",
									this.propertiesPanel.element.modelElement.inDataMapping);
					this.outInput
							.attr(
									"checked",
									this.propertiesPanel.element.modelElement.outDataMapping);
					this.dataPathInput
							.val(this.propertiesPanel.element.modelElement.dataPath);
				};
			}
		});