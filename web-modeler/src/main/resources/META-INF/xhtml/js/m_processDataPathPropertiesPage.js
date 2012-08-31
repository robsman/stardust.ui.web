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

				this.currentDataPath = null;

				this.dataPathTable = this.mapInputId("dataPathTable");
				this.addDataPathButton = this.mapInputId("addDataPathButton");
				this.deleteDataPathButton = this
						.mapInputId("deleteDataPathButton");
				this.moveDataPathUpButton = this
						.mapInputId("moveDataPathUpButton");
				this.moveDataPathDownButton = this
						.mapInputId("moveDataPathDownButton");
				this.dataPathNameInput = this.mapInputId("dataPathNameInput");
				this.dataPathDirectionSelect = this
						.mapInputId("dataPathDirectionSelect");
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
					event.data.page.removeDataPath(jQuery(
							"table#dataPathTable tr.selected").attr("id"));
				});
				this.moveDataPathUpButton.click({
					"page" : this
				}, function(event) {
					event.data.page.moveDataPathUp(jQuery(
							"table#dataPathTable tr.selected").attr("id"));
				});
				this.moveDataPathDownButton.click({
					"page" : this
				}, function(event) {
					event.data.page.moveDataPathDown(jQuery(
							"table#dataPathTable tr.selected").attr("id"));
				});
				this.dataPathDirectionSelect
						.change(
								{
									"page" : this
								},
								function(event) {
									event.data.page
											.setDataPathDirection(event.data.page.dataPathDirectionSelect
													.val());

									event.data.page.currentDataPath.direction = event.data.page.dataPathDirectionSelect
											.val();
									event.data.page.populateDataPathTable();
								});
				this.descriptorInput
						.change(
								{
									"page" : this
								},
								function(event) {
									event.data.page.currentDataPath.descriptor = event.data.page.descriptorInput
											.val();
									event.data.page.populateDataPathTable();
								});
				this.keyDescriptorInput
						.change(
								{
									"page" : this
								},
								function(event) {
									event.data.page.currentDataPath.keyDescriptor = event.data.page.keyDescriptorInput
											.val();
									event.data.page.populateDataPathTable();
								});
				this.dataPathNameInput
						.change(
								{
									page : this
								},
								function(event) {
									event.data.page.currentDataPath.name = event.data.page.dataPathNameInput
											.val();
									event.data.page.populateDataPathTable();
								});
				this.dataPathDataSelect
						.change(
								{
									page : this
								},
								function(event) {
									event.data.page.currentDataPath.dataFulId = event.data.page.dataPathDataSelect
											.val();
									event.data.page.populateDataPathTable();
								});
				this.dataPathPathInput
						.change(
								{
									page : this
								},
								function(event) {
									event.data.page.currentDataPath.path = event.data.page.dataPathPathInput
											.val();
									event.data.page.populateDataPathTable();
								});

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.setDataPathDirection = function(
						direction) {
					if (direction == "IN" || direction == "INOUT") {
						this.descriptorInput.removeAttr("disabled");
						this.keyDescriptorInput.removeAttr("disabled");
					} else {
						this.descriptorInput.attr("disabled", true);
						this.descriptorInput.attr("checked", false);
						this.keyDescriptorInput.attr("disabled", true);
						this.keyDescriptorInput.attr("checked", false);
					}
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
					var n = 1;

					if (this.getModelElement().dataPathes == null) {
						this.getModelElement().dataPathes = [];
					} else {
						for ( var m in this.getModelElement().dataPathes) {
							++n;
						}
					}

					this.currentDataPath = {
						id : "New" + n,
						name : "New " + n,
						direction : "IN",
						descriptor : false,
						keyDescriptor : false,
						dataFullId : null,
						dataPath : this.dataPathPathInput.val()
					};

					this.getModelElement().dataPathes
							.push(this.currentDataPath);

					this.populateDataPathTable();
					// this.submitChanges({
					// dataPathes : this.getModelElement().dataPathes
					// });

					this.populateDataPathFields();

					jQuery("table#dataPathTable tr#dataPath" + (n - 1)).addClass("selected");
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

					for ( var m = 0; m < this.propertiesPanel.element.dataPathes.length; ++m) {
						var dataPath = this.propertiesPanel.element.dataPathes[m];

						var item = "<tr id=\"";

						item += "dataPath" + m;

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

					jQuery("table#dataPathTable tr")
							.mousedown(
									{
										page : this
									},
									function(event) {
										event.data.page.deselectDataPathes();
										jQuery(this).addClass("selected");

										var index = jQuery(this).attr("id");

										index = index.substring(8);

										event.data.page.currentDataPath = event.data.page
												.getModelElement().dataPathes[index];
										event.data.page
												.populateDataPathFields();
									});

					// this.dataPathTable.tableScroll({
					// height : 200
					// });
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.deselectDataPathes = function(
						dataPath) {
					jQuery("table#dataPathTable tr.selected").removeClass(
							"selected");
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.populateDataPathFields = function() {
					this.dataPathNameInput.val(this.currentDataPath.name);
					this.dataPathDirectionSelect
							.val(this.currentDataPath.direction);
					this.descriptorInput.val(this.currentDataPath.descriptor);
					this.keyDescriptorInput
							.val(this.currentDataPath.keyDescriptor);
					this.dataPathDataSelect
							.val(this.currentDataPath.dataFullId);
					this.dataPathPathInput.val(this.currentDataPath.dataPath);
				};
			}
		});