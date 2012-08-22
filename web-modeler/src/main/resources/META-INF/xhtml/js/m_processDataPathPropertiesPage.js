/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_propertiesPage" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_propertiesPage) {
			return {
				create : function(propertiesPanel) {
					return new ProcessDataPathPropertiesPage(propertiesPanel);
				}
			};

			function ProcessDataPathPropertiesPage(newPropertiesPanel, newId,
					newTitle) {
				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "dataPathPropertiesPage",
						"DataPath",
						"../../images/icons/data-path-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ProcessDataPathPropertiesPage.prototype,
						propertiesPage);

				this.dataPaths = [];

				this.dataPathTable = this.mapInputId("dataPathTable");
				this.addDataPathButton = this.mapInputId("addDataPathButton");
				this.deleteDataPathButton = this
						.mapInputId("deleteDataPathButton");
				this.moveDataPathUpButton = this
						.mapInputId("moveDataPathUpButton");
				this.moveDataPathDownButton = this
						.mapInputId("moveDataPathDownButton");
				this.dataPathNameInput = this.mapInputId("dataPathNameInput");
				this.inDataPathInput = this.mapInputId("inDataPathInput");
				this.outDataPathInput = this.mapInputId("outDataPathInput");
				this.descriptorInput = this.mapInputId("descriptorInput");
				this.keyDescriptorInput = this.mapInputId("keyDescriptorInput");
				this.dataPathDataSelect = this.mapInputId("dataPathDataSelect");
				this.dataPathPathInput = this.mapInputId("dataPathPathInput");

				this.addDataPathButton.click({
					"page" : this
				}, function(event) {
					event.data.page.addDataPath();
				});

				this.deleteDataPathButton.click({
					"page" : this
				}, function(event) {
					event.data.page.removeDataPath(jQuery("table#dataPathTable tr.selected")
							.attr("id"));
				});

				this.moveDataPathUpButton.click({
					"page" : this
				}, function(event) {
					event.data.page.moveDataPathUp(jQuery("table#dataPathTable tr.selected")
							.attr("id"));
				});

				this.moveDataPathDownButton.click({
					"page" : this
				}, function(event) {
					event.data.page.moveDataPathDown(jQuery("table#dataPathTable tr.selected")
							.attr("id"));
				});

				this.outDataPathInput.click({
					"page" : this
				}, function(event) {
					event.data.page.setOutDataPath();
				});

				this.inDataPathInput.click({
					"page" : this
				}, function(event) {
					event.data.page.setInDataPath();
				});

				this.descriptorInput.click({
					"page" : this
				}, function(event) {
					event.data.page.setDescriptor();
				});

				this.keyDescriptorInput.click({
					"page" : this
				}, function(event) {
					event.data.page.setKeyDescriptor();
				});

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.setInDataPath = function() {
					this.inDataPathInput.attr("checked", true);
					this.outDataPathInput.attr("checked", false);
					this.descriptorInput.removeAttr("disabled");
					this.keyDescriptorInput.removeAttr("disabled");
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.setOutDataPath = function() {
					this.outDataPathInput.attr("checked", true);
					this.inDataPathInput.attr("checked", false);
					this.descriptorInput.attr("disabled", true);
					this.descriptorInput.attr("checked", false);
					this.keyDescriptorInput.attr("disabled", true);
					this.keyDescriptorInput.attr("checked", false);
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.setDescriptor = function() {
					this.descriptorInput.attr("checked", true);
					this.keyDescriptorInput.attr("checked", false);
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.setKeyDescriptor = function() {
					this.descriptorInput.attr("checked", false);
					this.keyDescriptorInput.attr("checked", true);
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.setElement = function() {
					this.refreshDataItemsList();
					this.populateDataPathTable();
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.refreshDataItemsList = function() {
					for ( var n in this.propertiesPanel.models) {
						for ( var m in this.propertiesPanel.models[n].dataItems) {
							var dataItem = this.propertiesPanel.models[n].dataItems[m];

							this.dataPathDataSelect.append("<option value='"
									+ dataItem.getFullId() + "'>"
									+ this.propertiesPanel.models[n].name + "/"
									+ dataItem.name + "</option>");
						}
					}
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element;
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();
					this.dataPathNameInput.removeClass("error");

					if (this.dataPathNameInput.val() == null
							|| this.dataPathNameInput.val() == "") {
						this.propertiesPanel.errorMessages
								.push("Data Path name must not be empty.");
						this.dataPathNameInput.addClass("error");
						this.dataPathNameInput.focus();
						this.propertiesPanel.showErrorMessages();

						return false;
					} else {
						for ( var n = 0; n < this.getModelElement().dataPathes.length; ++n) {
							if (this.getModelElement().dataPathes[n].name == this.dataPathNameInput
									.val()) {
								this.propertiesPanel.errorMessages
										.push("Duplicate Data Path name \""
												+ this.dataPathNameInput.val()
												+ "\"");
								this.dataPathNameInput.addClass("error");
								this.dataPathNameInput.focus();
								this.propertiesPanel.showErrorMessages();

								return false;
							}
						}
					}

					return true;
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.addDataPath = function() {
					if (this.validate()) {
						this.getModelElement().dataPathes
								.push({
									name : this.dataPathNameInput.val(),
									direction : this.inDataPathInput
											.is(":checked") ? "IN" : "OUT",
									descriptor : this.descriptorInput
											.is(":checked"),
									keyDescriptor : this.keyDescriptorInput
											.is(":checked"),
									dataFullId : this.dataPathDataSelect.val(),
									dataPath : this.dataPathPathInput.val()
								});

						this.submitChanges({
							dataPathes : this.getModelElement().dataPathes
						});

						this.dataPathNameInput.val(null);
					}
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.removeDataPath = function(
						dataPathId) {
					var changedPathes = [];

					for ( var n = 0; n < this.getModelElement().dataPathes.length; ++n) {
						if (this.getModelElement().dataPathes[n].id != dataPathId) {
							changedPathes
									.push(this.getModelElement().dataPathes[n]);
						}
					}

					this.getModelElement().dataPathes = changedPathes;

					this.submitChanges({
						dataPathes : this.getModelElement().dataPathes
					});
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.moveDataPathUp = function(
						dataPathId) {
					var changedPathes = [];

					for ( var n = 0; n < this.getModelElement().dataPathes.length; ++n) {
						if (n + 1 < this.getModelElement().dataPathes.length
								&& this.getModelElement().dataPathes[n + 1].id == dataPathId)
							changedPathes
									.push(this.getModelElement().dataPathes[n + 1]);
						changedPathes
								.push(this.getModelElement().dataPathes[n]);

						++n;
					}

					this.getModelElement().dataPathes = changedPathes;

					this.submitChanges({
						dataPathes : this.getModelElement().dataPathes
					});
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.moveDataPathDown = function(
						dataPathId) {
					var changedPathes = [];

					for ( var n = 0; n < this.getModelElement().dataPathes.length; ++n) {
						if (n + 1 < this.getModelElement().dataPathes.length
								&& this.getModelElement().dataPathes[n + 1].id == dataPathId)
							changedPathes
									.push(this.getModelElement().dataPathes[n + 1]);
						changedPathes
								.push(this.getModelElement().dataPathes[n]);

						++n;
					}

					this.getModelElement().dataPathes = changedPathes;

					this.submitChanges({
						dataPathes : this.getModelElement().dataPathes
					});
				};
				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.populateDataPathTable = function() {
					if (this.propertiesPanel.element.dataPathes == null) {
						return;
					}

					this.dataPathTable.empty();

					for (var m = 0; m < this.propertiesPanel.element.dataPathes.length; ++m) {
						var dataPath = this.propertiesPanel.element.dataPathes[m];

						var item = "<tr id=\"";

						item += dataPath.id;

						item += "\"><td class=\"";

						if (dataPath.direction == "IN") {
							if (dataPath.descriptor) {
								if (dataPath.keyDescriptor) {
									item += "keyDescriptorDataPathListItem";
								} else {
									item += "descriptorDataPathListItem";
								}
							} else {
								item += "outDataPathListItem";
							}
						} else {
							item += "inDataPathListItem";
						}

						item += "\"></td><td class=\"dataPathTableCell\">";
						item += dataPath.name;
						item += "</td><td class=\"dataPathTableCell\">";
						item += dataPath.dataFullId;

						if (dataPath.dataPath != null
								&& dataPath.dataPath != "") {
							item += ".";
							item += dataPath.dataPath;
						}

						item += "</td></tr>";

						this.dataPathTable.append(item);
					}

					jQuery("table#dataPathTable tr").mousedown(function() {
						jQuery("tr.selected").removeClass("selected");
						jQuery(this).addClass("selected");
					});

					// this.dataPathTable.tableScroll({
					// height : 200
					// });
				};
			}
		});