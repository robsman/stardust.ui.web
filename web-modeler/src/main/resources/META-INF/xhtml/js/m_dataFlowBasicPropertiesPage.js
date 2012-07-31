/**
 * @author Marc.Gille
 */

define(
		[ "m_utils", "m_constants", "m_user", "m_dialog", "m_propertiesPage" ],
		function(m_utils, m_constants, m_user, m_dialog, m_propertiesPage) {
			return {
				create : function(propertiesPanel) {
					return new DataFlowBasicPropertiesPage(propertiesPanel);
				}
			};

			function DataFlowBasicPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "basicPropertiesPage", "Basic");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(DataFlowBasicPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				this.inInput = this.mapInputId("inInput");
				this.outInput = this.mapInputId("outInput");
				this.descriptionInput = this.mapInputId("descriptionInput");
				this.dataPathInput = this.mapInputId("dataPathInput");
				this.dataPathOutput = this.mapInputId("dataPathOutput");
				this.inputAccessPointPanel = this
						.mapInputId("inputAccessPointPanel");
				this.outputAccessPointPanel = this
						.mapInputId("outputAccessPointPanel");
				this.inAccessPointSelectInput = this
						.mapInputId("inAccessPointSelectInput");
				this.outAccessPointSelectInput = this
						.mapInputId("outAccessPointSelectInput");

				// Initialize callbacks

				this.inInput.click({
					"callbackScope" : this
				}, function(event) {
					if (event.data.callbackScope.inInput.is(":checked")) {
						event.data.callbackScope.dataPathInput
								.removeAttr("disabled");
					} else {
						event.data.callbackScope.dataPathInput.attr("disabled",
								true);
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

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.populateInAccessPointSelectInput = function(
						dataFlow) {
					this.inAccessPointSelectInput.empty();

					m_utils.debug("===> Activity");
					m_utils.debug(dataFlow.activity);
					
					for ( var n in dataFlow.activity.getAccessPoints()) {
						var accessPoint = dataFlow.activity.getAccessPoints()[n];

						if (accessPoint.direction == m_constants.IN_ACCESS_POINT
								|| accessPoint.direction == m_constants.INOUT_ACCESS_POINT) {
							m_utils.debug("Access Point");
							m_utils.debug(accessPoint);

							var option = "<option value\"";

							option += accessPoint.id;
							option += "\">";
							option += accessPoint.name;
							option += "</option>";

							this.inAccessPointSelectInput.append(option);
						}
					}
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.populateOutAccessPointSelectInput = function(
						dataFlow) {
					this.outAccessPointSelectInput.empty();

					for ( var n in dataFlow.activity.getAccessPoints()) {
						var accessPoint = dataFlow.activity.getAccessPoints()[n];

						if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
								|| accessPoint.direction == m_constants.INOUT_ACCESS_POINT) {
							m_utils.debug("Acess Point");
							m_utils.debug(accessPoint);

							var option = "<option value\"";

							option += accessPoint.id;
							option += "\">";
							option += accessPoint.name;
							option += "</option>";

							this.outAccessPointSelectInput.append(option);
						}
					}

				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.setElement = function() {
					this
							.populateInAccessPointSelectInput(this.propertiesPanel.element.modelElement);
					this
							.populateOutAccessPointSelectInput(this.propertiesPanel.element.modelElement);
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
					// this.applicationPathInput
					// .val(this.propertiesPanel.element.modelElement.applicationPath);

					// if (m_user.currentUserHasProfileRole(m_user.INTEGRATOR))
					// {
					// m_dialog.makeVisible(this.inputAccessPointPanel);
					// m_dialog.makeVisible(this.outputAccessPointPanel);
					// } else {
					// m_dialog.makeInvisible(this.inputAccessPointPanel);
					// m_dialog.makeInvisible(this.outputAccessPointPanel);
					// }
				};

				/**
				 * 
				 */
				DataFlowBasicPropertiesPage.prototype.apply = function() {
					this.propertiesPanel.element.modelElement.description = this.descriptionInput
							.val();
					if (this.inInput.is(":checked")) {
						// TODO Workaround Consider directions
						this.propertiesPanel.element.modelElement.outDataMapping = true;
						this.propertiesPanel.element.modelElement.dataPath = this.dataPathInput
								.val();
					} else {
						// TODO Workaround Consider directions
						this.propertiesPanel.element.modelElement.outDataMapping = false;
						this.propertiesPanel.element.modelElement.dataPath = null;
					}

					if (this.outInput.is(":checked")) {
						this.propertiesPanel.element.modelElement.inDataMapping = true;
						// this.propertiesPanel.element.modelElement.applicationPath
						// = this.applicationPathInput
						// .val();
					} else {
						this.propertiesPanel.element.modelElement.inDataMapping = false;
						// this.propertiesPanel.element.modelElement.applicationPath
						// = null;
					}
				};
			}
		});